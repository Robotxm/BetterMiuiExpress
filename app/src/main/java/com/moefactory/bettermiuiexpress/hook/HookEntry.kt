package com.moefactory.bettermiuiexpress.hook

import android.content.Context
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.moefactory.bettermiuiexpress.BuildConfig
import com.moefactory.bettermiuiexpress.activity.ExpressDetailsActivity
import com.moefactory.bettermiuiexpress.base.app.*
import com.moefactory.bettermiuiexpress.ktx.*
import com.moefactory.bettermiuiexpress.model.*
import com.moefactory.bettermiuiexpress.repository.ExpressActualRepository
import com.moefactory.bettermiuiexpress.utils.ExpressCompanyUtils
import de.robv.android.xposed.XSharedPreferences
import kotlinx.coroutines.runBlocking

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    private val pref by lazy {
        val p = XSharedPreferences(BuildConfig.APPLICATION_ID, PREF_NAME)
        if (p.file.canRead()) p else null
    }

    private val secretKey: String?
        get() = pref?.getString(PREF_KEY_SECRET_KEY, null)
    private val customer: String?
        get() = pref?.getString(PREF_KEY_CUSTOMER, null)
    private val shouldFetchFromCaiNiao: Boolean
        get() = secretKey.isNullOrBlank() || customer.isNullOrBlank()

    override fun onInit() = configs {
        debugTag = "BetterMiuiExpress"
        isDebug = BuildConfig.DEBUG
    }

    override fun onHook() = encase {
        loadApp(name = PA_PACKAGE_NAME) {
            val expressEntryClass = if (PA_EXPRESS_ENTRY.hasClass) PA_EXPRESS_ENTRY.clazz
            else PA_EXPRESS_ENTRY_OLD.clazz

            // Old version
            // public static String gotoExpressDetailPage(Context context, ExpressEntry expressEntry, boolean z, boolean z2)
            // New version
            // public static String gotoExpressDetailPage(Context context, View view, ExpressEntry expressEntry, boolean z, boolean z2, Intent intent, int i2)
            findClass(PA_EXPRESS_INTENT_UTILS, PA_EXPRESS_INTENT_UTILS_OLD)
                .hook {
                    injectMember {
                        var isNewVersion = false

                        method {
                            name = "gotoExpressDetailPage"
                            param(ContextClass, expressEntryClass, BooleanType, BooleanType)
                        }.remedys {
                            method {
                                name = "gotoExpressDetailPage"
                                param(
                                    ContextClass,
                                    ViewClass,
                                    expressEntryClass,
                                    BooleanType,
                                    BooleanType,
                                    IntentClass,
                                    IntType
                                )
                            }.onFind { isNewVersion = true }
                        }.ignoredError()

                        replaceAny {
                            val context = args().first().cast<Context>()!!
                            val expressEntry = args(index = if (isNewVersion) 2 else 1).any()!!
                            val expressEntryWrapper = expressEntry.toExpressEntryWrapper()
                            val companyCode = expressEntryWrapper.companyCode
                            val companyName = expressEntryWrapper.companyName
                            val mailNumber = expressEntryWrapper.orderNumber
                            val phoneNumber = expressEntryWrapper.phone
                            // Check if the details will be showed in third-party apps(taobao, cainiao, etc.)
                            val uris = expressEntryWrapper.uris
                            val caiNiaoSecretKey = expressEntryWrapper.secretKey
                            if (!uris.isNullOrEmpty()) {
                                // Store urls for future use such as jumping to third-party apps
                                val uriList = arrayListOf<String>()
                                for (uriEntity in uris) {
                                    val uriString = uriEntity!!.javaClass.getMethod("getLink")
                                        .invoke(uriEntity) as String
                                    uriList.add(uriString)
                                }
                                ExpressDetailsActivity.gotoDetailsActivity(
                                    context,
                                    MiuiExpress(companyCode, companyName, mailNumber, phoneNumber, caiNiaoSecretKey),
                                    uriList
                                )
                                return@replaceAny null
                            } else {
                                val provider = expressEntry.javaClass.getMethod("getProvider")
                                    .invoke(expressEntry) as? String
                                val isXiaomi = provider == "Miguo" || provider == "MiMall"
                                val isJingDong = companyCode == "JDKD"
                                // Details of packages from Xiaomi or JingDong will be showed in built-in app
                                if (!isXiaomi && !isJingDong) {
                                    ExpressDetailsActivity.gotoDetailsActivity(
                                        context,
                                        MiuiExpress(
                                            companyCode,
                                            companyName,
                                            mailNumber,
                                            phoneNumber,
                                            caiNiaoSecretKey
                                        ),
                                        null
                                    )
                                    return@replaceAny null
                                }
                            }

                            // Other details will be processed normally
                            return@replaceAny method.invokeOriginal(*args)
                        }
                    }
                }

            // Hook ExpressRepository$saveExpress
            // Save the latest detail
            findClass(PA_EXPRESS_REPOSITOIRY, PA_EXPRESS_REPOSITORY_OLD)
                .hook {
                    injectMember {
                        method {
                            name = "saveExpress"
                            param(JavaListClass)
                        }
                        beforeHook {
                            runBlocking {
                                args().first().cast<java.util.List<*>>()?.let { expressInfoList ->
                                    for (expressInfo in expressInfoList) {
                                        // Skip packages from Xiaomi and JingDong
                                        val expressInfoWrapper = expressInfo.toExpressInfoWrapper()
                                        val provider = expressInfoWrapper.provider
                                        val isXiaomi = provider == "Miguo" || provider == "MiMall"
                                        val companyCode = expressInfoWrapper.companyCode
                                        val companyName = expressInfoWrapper.companyName
                                        val isJingDong = companyCode == "JDKD"
                                        if (isXiaomi || isJingDong) {
                                            continue
                                        }

                                        val mailNumber = expressInfoWrapper.orderNumber
                                        val caiNiaoSecretKey = expressInfoWrapper.secretKey

                                        val detailList = if (shouldFetchFromCaiNiao) {
                                            ExpressActualRepository.queryExpressDetailsFromCaiNiaoActual(
                                                mailNumber
                                            )?.fullTraceDetails?.map { it.toExpressTrace() }
                                        } else {
                                            // Get the company code
                                            val convertedCompanyCode =
                                                ExpressCompanyUtils.convertCode(companyCode)
                                                    ?: ExpressActualRepository.queryCompanyActual(mailNumber, secretKey!!)[0].companyCode

                                            // Get the details
                                            val phoneNumber = expressInfoWrapper.phone
                                            val response = ExpressActualRepository.queryExpressDetailsFromKuaiDi100Actual(
                                                convertedCompanyCode,
                                                mailNumber,
                                                phoneNumber,
                                                secretKey!!,
                                                customer!!
                                            )

                                            response.data?.map { it.toExpressTrace() }
                                        }?.sortedDescending()

                                        // Ignore invalid result
                                        if (detailList.isNullOrEmpty()) {
                                            continue
                                        }

                                        // Prevent detail from disappearing
                                        expressInfoWrapper.clickDisappear = false
                                        val originalDetails = expressInfoWrapper.details
                                        val detailClass =
                                            if (PA_EXPRESS_INFO_DETAIL.hasClass) PA_EXPRESS_INFO_DETAIL.clazz
                                            else PA_EXPRESS_INFO_DETAIL_OLD.clazz
                                        when {
                                            originalDetails == null -> {
                                                // Null list, create a new instance and put the latest detail
                                                val newDetail = detailClass.newInstance()
                                                val newDetailWrapper = newDetail.toExpressInfoDetailWrapper()
                                                newDetailWrapper.desc = detailList[0].description
                                                newDetailWrapper.time = detailList[0].fullDateTime
                                                val newDetails = ArrayList<Any>(1)
                                                newDetails.add(newDetail)
                                                expressInfoWrapper.details = newDetails
                                            }
                                            originalDetails.isEmpty() -> {
                                                // Empty list, put the latest detail
                                                val newDetail = detailClass.newInstance()
                                                val newDetailWrapper = newDetail.toExpressInfoDetailWrapper()
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
                                }
                            }
                        }
                    }
                }
        }
    }
}