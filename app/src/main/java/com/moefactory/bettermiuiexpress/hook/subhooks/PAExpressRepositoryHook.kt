package com.moefactory.bettermiuiexpress.hook.subhooks

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
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

// Hook ExpressRepository$saveExpress
// Save the latest detail
object PAExpressRepositoryHook : YukiBaseHooker() {

    private val deviceTrackId: String
        get() = prefs.getString(PREF_KEY_DEVICE_TRACK_ID)

    override fun onHook() {
        PA_EXPRESS_REPOSITOIRY.toClassOrNull()?.resolve()?.firstMethod {
            name = "saveExpress"
            parameters(JavaListClass)
        }?.hook {
            before {
                runBlocking {
                    val expressInfoList = args().first().cast<java.util.List<*>>()
                        ?.map { it.toExpressInfoWrapper() }
                        ?.filter { !it.isXiaomiOrJingDong } // Skip packages from Xiaomi and JingDong
                        ?: return@runBlocking
                    for (expressInfoWrapper in expressInfoList) {
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
        val convertedCompanyCode = ExpressCompanyUtils.convertCode(originalCompanyCode)
            ?: ExpressActualRepository.queryCompanyActual(mailNumber).firstOrNull()?.companyCode

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

        val response = ExpressActualRepository.queryExpressDetailsFromKuaiDi100Actual(convertedCompanyCode!!, mailNumber, phoneNumber, deviceTrackId)
        return response?.lastResult?.data?.map { it.toExpressTrace() }?.sortedDescending()
    }
}