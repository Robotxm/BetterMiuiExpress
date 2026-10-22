package com.moefactory.bettermiuiexpress.hook.subhooks

import android.os.Looper
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.extension.kavaref.hook
import com.highcapable.yukihookapi.extension.kavaref.toClassOrNull
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_INFO_DETAIL
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_REPOSITOIRY
import com.moefactory.bettermiuiexpress.base.app.PREF_KEY_DEVICE_TRACK_ID
import com.moefactory.bettermiuiexpress.ktx.JavaListClass
import com.moefactory.bettermiuiexpress.model.ExpressInfoWrapper
import com.moefactory.bettermiuiexpress.model.ExpressTrace
import com.moefactory.bettermiuiexpress.model.isXiaomiOrJingDong
import com.moefactory.bettermiuiexpress.model.toExpressInfoDetailWrapper
import com.moefactory.bettermiuiexpress.model.toExpressInfoWrapper
import com.moefactory.bettermiuiexpress.model.toExpressTrace
import com.moefactory.bettermiuiexpress.repository.ExpressActualRepository
import com.moefactory.bettermiuiexpress.utils.ExpressCompanyUtils
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

// Hook ExpressRepository$saveExpress
// Save the latest detail
object PAExpressRepositoryHook : YukiBaseHooker() {

    private val refreshExecutor by lazy {
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "BetterMiuiExpress-refresh")
        }
    }

    private val deviceTrackId: String
        get() = preferences.getString(PREF_KEY_DEVICE_TRACK_ID)

    override fun onHook() {
        val repositoryClass = PA_EXPRESS_REPOSITOIRY.toClassOrNull() ?: return

        // requestExpressAll posts this callback to the main thread. Keep saving and
        // notifying in their original order, but allow Retrofit callbacks to run.
        val refreshResultMethod = repositoryClass.declaredMethods.singleOrNull {
            it.name == "lambda\$requestExpressAll\$1" && it.parameterCount == 1 && it.returnType == Void.TYPE
        }
        if (refreshResultMethod != null) {
            refreshResultMethod.hook {
                intercept {
                    val repository = instance
                    val responseArgs = args.copyOf()
                    refreshExecutor.execute {
                        runCatching {
                            refreshResultMethod.invokeOriginal(repository, *responseArgs)
                        }.onFailure {
                            YLog.error("Failed to save express refresh result", e = it)
                        }
                    }
                    null
                }
            }
        } else {
            YLog.warn("Express refresh callback not found; main-thread saves will skip detail queries")
        }

        repositoryClass.resolve().firstMethod {
            name = "saveExpress"
            parameters(JavaListClass)
        }.hook {
            before {
                // Also protect other callers and host versions with a different callback.
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    YLog.warn("Skipping express detail queries on the main thread")
                    return@before
                }

                runBlocking {
                    val expressInfoList = firstArg().get<List<*>>()
                        ?.mapNotNull { it?.toExpressInfoWrapper() }
                        ?.filter { !it.isXiaomiOrJingDong } // Skip packages from Xiaomi and JingDong
                        ?: return@runBlocking
                    for (expressInfoWrapper in expressInfoList) {
                        try {
                            val companyCode = expressInfoWrapper.companyCode
                            val mailNumber = expressInfoWrapper.orderNumber
                            val phoneNumber = expressInfoWrapper.phone ?: expressInfoWrapper.sendPhone

                            val detailList = fetchExpressDetails(mailNumber, companyCode, phoneNumber)

                            // Ignore invalid result
                            if (detailList.isNullOrEmpty()) {
                                continue
                            }

                            // Save latest trace
                            val detailClass = PA_EXPRESS_INFO_DETAIL.toClassOrNull() ?: return@runBlocking
                            saveLatestExpressTrace(
                                expressInfoWrapper,
                                detailClass,
                                detailList
                            )
                        } catch (exception: Exception) {
                            YLog.warn("Failed to enrich express details; keeping host data", e = exception)
                        }
                    }
                }
            }
        }
    }

    private fun saveLatestExpressTrace(
        expressInfoWrapper: ExpressInfoWrapper,
        detailClass: Class<out Any>,
        detailList: List<ExpressTrace>
    ) {
        // Prevent detail from disappearing
        expressInfoWrapper.clickDisappear = false
        val originalDetails = expressInfoWrapper.details
        when {
            originalDetails == null -> {
                // Null list, create a new instance and put the latest detail
                val newDetail = detailClass.getDeclaredConstructor().newInstance()
                val newDetailWrapper =
                    newDetail.toExpressInfoDetailWrapper()
                newDetailWrapper.desc = detailList[0].description
                newDetailWrapper.time = detailList[0].fullDateTime
                val newDetails = ArrayList<Any>(1)
                newDetails.add(newDetail)
                expressInfoWrapper.details = newDetails
            }
            originalDetails.isEmpty() -> {
                // Empty list, put the latest detail
                val newDetail = detailClass.getDeclaredConstructor().newInstance()
                val newDetailWrapper =
                    newDetail.toExpressInfoDetailWrapper()
                newDetailWrapper.desc = detailList[0].description
                newDetailWrapper.time = detailList[0].fullDateTime
                originalDetails.add(newDetail)
            }
            else -> {
                // Normally, the original details contains one item
                expressInfoWrapper.details?.getOrNull(0)
                    ?.toExpressInfoDetailWrapper()
                    ?.desc = detailList[0].description
            }
        }
    }

    private suspend fun fetchExpressDetails(
        mailNumber: String, originalCompanyCode: String, phoneNumber: String?
    ): List<ExpressTrace>? {
        if (deviceTrackId.isEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            val currentDateTimeString = sdf.format(Date())

            return listOf(
                ExpressTrace(
                    fullDateTime = currentDateTimeString,
                    date = currentDateTimeString.split(" ")[0],
                    time = currentDateTimeString.split(" ")[1],
                    description = "请先打开模块主界面完成初始化"
                )
            )
        }

        val convertedCompanyCode = ExpressCompanyUtils.convertCode(originalCompanyCode)
            ?: ExpressActualRepository.queryCompanyActual(mailNumber).firstOrNull()?.companyCode
            ?: return null

        val response = ExpressActualRepository.queryExpressDetailsFromKuaiDi100Actual(convertedCompanyCode, mailNumber, phoneNumber, deviceTrackId)
        return response?.lastResult?.data?.map { it.toExpressTrace() }?.sortedDescending()
    }
}
