package com.moefactory.bettermiuiexpress.hook

import android.content.Context
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.type.android.ContextClass
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
        isDebug = BuildConfig.DEBUG
        debugLog {
            tag = "BetterMiuiExpress"
        }
    }

    override fun onHook() = encase {
        loadApp(name = PA_PACKAGE_NAME) {
            // Old version
            // public static String gotoExpressDetailPage(Context context, ExpressEntry expressEntry, boolean z, boolean z2)
            // New version
            // public static String gotoExpressDetailPage(Context context, View view, ExpressEntry expressEntry, boolean z, boolean z2, Intent intent, int i2)
            findClass(PA_EXPRESS_INTENT_UTILS, PA_EXPRESS_INTENT_UTILS_OLD)
                .hook {
                    val expressEntryClass =
                        PA_EXPRESS_ENTRY.toClassOrNull() ?: PA_EXPRESS_ENTRY_OLD.toClassOrNull()
                        ?: return@hook
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
                        }

                        replaceAny {
                            val context = args().first().cast<Context>()!!
                            val expressEntry = args(index = if (isNewVersion) 2 else 1).any()!!
                            val expressEntryWrapper = expressEntry.toExpressEntryWrapper()
                            if (jumpToDetailsActivity(context, expressEntryWrapper)) {
                                return@replaceAny null
                            }

                            // Other details will be processed normally
                            return@replaceAny invokeOriginal(*args)
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
                                val expressInfoList = args().first().cast<java.util.List<*>>()
                                    ?.map { it.toExpressInfoWrapper() }
                                    ?.onEach { loggerD(msg = it.toString()) }
                                    ?.filter { !it.isXiaomiOrJingDong } // Skip packages from Xiaomi and JingDong
                                    ?: return@runBlocking
                                for (expressInfoWrapper in expressInfoList) {
                                    val companyCode = expressInfoWrapper.companyCode
                                    val mailNumber = expressInfoWrapper.orderNumber
                                    val phoneNumber = expressInfoWrapper.phone

                                    val detailList = fetchExpressDetails(
                                        mailNumber, companyCode, phoneNumber
                                    )

                                    // Ignore invalid result
                                    if (detailList.isNullOrEmpty()) {
                                        continue
                                    }

                                    // Save latest trace
                                    val detailClass =
                                        PA_EXPRESS_INFO_DETAIL.toClassOrNull()
                                            ?: PA_EXPRESS_INFO_DETAIL_OLD.toClassOrNull()
                                            ?: return@runBlocking
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

            findClass(
                PA_EXPRESS_SEARCH_FRAGMENT_RESULT_VIEW,
                PA_EXPRESS_SEARCH_FRAGMENT_RESULT_VIEW_OLD
            ).hook {
                val expressEntryClass =
                    PA_EXPRESS_ENTRY.toClassOrNull() ?: PA_EXPRESS_ENTRY_OLD.toClassOrNull()
                    ?: return@hook
                injectMember {
                    method {
                        name = "onActionSearch"
                        param(CharSequenceClass)
                    }
                    afterHook {
                        val queryExpress = field { name = "mQueryExpress" }
                            .ignored()
                            .get(instance)
                            .any() ?: return@afterHook

                        val cacheClass =
                            PA_EXPRESS_CACHE.toClassOrNull() ?: PA_EXPRESS_CACHE_OLD.toClassOrNull()
                            ?: return@afterHook

                        // Save express queried manually
                        cacheClass.method {
                            name = "putExpressEntry"
                            param(ContextClass, expressEntryClass)
                            returnType(BooleanPrimitiveType)
                        }.get().call(appContext, queryExpress)

                        val originalList = cacheClass.method {
                            name = "getServerCache"
                            param(ContextClass)
                            returnType(JavaListClass)
                        }.get().invoke<java.util.List<String>>(appContext) as? java.util.ArrayList<String> // java.util.Arrays$ArrayList is private so we have to cast twice

                        val queryExpressEntryWrapper = ExpressEntryWrapper(queryExpress)
                        val newList = originalList?.map { it }?.toMutableList() ?: mutableListOf()
                        newList.add(queryExpressEntryWrapper.orderNumber)

                        // TODO: Only express in server cache will be processed by MIUI. However,
                        //       the server cache will be refresh automatically so that all modification
                        //       will be discarded

                        cacheClass.method {
                            name = "putServerCache"
                            param(ContextClass, JavaArrayListClass)
                            returnType(BooleanPrimitiveType)
                        }.get().call(appContext, newList)

                        val repositoryClass =
                            PA_EXPRESS_REPOSITOIRY.toClassOrNull()
                                ?: PA_EXPRESS_REPOSITORY_OLD.toClassOrNull()
                                ?: return@afterHook

                        repositoryClass.method {
                            name = "getInstance"
                            param(ContextClass)
                        }.get().call(appContext)?.let { repositoryInstance ->
                            repositoryClass.method { name = "requestExpressAll" }
                                .get(repositoryInstance)
                                .call()
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
                val newDetail = detailClass.newInstance()
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
                val newDetail = detailClass.newInstance()
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

    private fun jumpToDetailsActivity(
        context: Context, expressEntryWrapper: ExpressEntryWrapper
    ): Boolean {
        val companyCode = expressEntryWrapper.companyCode
        val companyName = expressEntryWrapper.companyName
        val mailNumber = expressEntryWrapper.orderNumber
        val phoneNumber = expressEntryWrapper.phone
        // Check if the details will be showed in third-party apps(taobao, cainiao, etc.)
        val uris = expressEntryWrapper.uris
        if (!uris.isNullOrEmpty()) {
            // Store urls for future use such as jumping to third-party apps
            ExpressDetailsActivity.gotoDetailsActivity(
                context,
                MiuiExpress(companyCode, companyName, mailNumber, phoneNumber),
                ArrayList(uris.map { it!!.toExpressInfoUriWrapper().link })
            )
            return true
        } else {
            // Details of packages from Xiaomi or JingDong will be showed in built-in app
            if (!expressEntryWrapper.isXiaomiOrJingDong) {
                ExpressDetailsActivity.gotoDetailsActivity(
                    context,
                    MiuiExpress(companyCode, companyName, mailNumber, phoneNumber),
                    null
                )
                return true
            }
        }

        return false
    }

    private suspend fun fetchExpressDetails(
        mailNumber: String, originalCompanyCode: String, phoneNumber: String
    ): List<ExpressTrace>? {
        return if (shouldFetchFromCaiNiao) {
            ExpressActualRepository.queryExpressDetailsFromCaiNiaoActual(mailNumber)
                ?.fullTraceDetails?.map { it.toExpressTrace() }
        } else {
            // Get the company code
            val convertedCompanyCode = ExpressCompanyUtils.convertCode(originalCompanyCode)
                ?: ExpressActualRepository.queryCompanyActual(
                    mailNumber,
                    secretKey!!
                )[0].companyCode

            // Get the details
            val response = ExpressActualRepository.queryExpressDetailsFromKuaiDi100Actual(
                convertedCompanyCode, mailNumber, phoneNumber, secretKey!!, customer!!
            )

            response.data?.map { it.toExpressTrace() }
        }?.sortedDescending()
    }
}