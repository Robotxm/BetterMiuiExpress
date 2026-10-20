package com.moefactory.bettermiuiexpress.activity

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moefactory.bettermiuiexpress.ktx.deviceTrackId
import com.moefactory.bettermiuiexpress.BuildConfig
import com.moefactory.bettermiuiexpress.R
import com.moefactory.bettermiuiexpress.base.ui.BaseActivity
import com.moefactory.bettermiuiexpress.model.MiuiExpress
import com.moefactory.bettermiuiexpress.model.ExpressInfoJumpListWrapper
import com.moefactory.bettermiuiexpress.ui.BetterMiuiExpressTheme
import com.moefactory.bettermiuiexpress.ui.ExpressDetailsScreen
import com.moefactory.bettermiuiexpress.viewmodel.ExpressDetailsViewModel

class ExpressDetailsActivity : BaseActivity() {
    companion object {
        private const val ACTION_GO_TO_DETAILS = "com.moefactory.bettermiuiexpress.details"
        private const val INTENT_CUSTOM_TRANSITION = "custom_transition"
        const val INTENT_EXPRESS_SUMMARY = "express_summary"
        const val INTENT_URL_CANDIDATES = "url_candidates"

        fun gotoDetailsActivity(
            context: Context,
            miuiExpress: MiuiExpress,
            uris: ArrayList<ExpressInfoJumpListWrapper>?
        ) {
            if (context is Activity) { // Click items in details activity
                val moduleContext = context.createPackageContext(BuildConfig.APPLICATION_ID, 0)
                val options = ActivityOptions.makeCustomAnimation(
                    moduleContext,
                    R.anim.express_details_fade_in,
                    R.anim.express_details_hold
                )
                context.startActivity(
                    Intent(ACTION_GO_TO_DETAILS)
                        .putExtra(INTENT_EXPRESS_SUMMARY, miuiExpress)
                        .putExtra(INTENT_URL_CANDIDATES, uris)
                        .putExtra(INTENT_CUSTOM_TRANSITION, true),
                    options.toBundle()
                )
            } else { // Click items in card
                context.startActivity(
                    Intent(ACTION_GO_TO_DETAILS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(INTENT_EXPRESS_SUMMARY, miuiExpress)
                        .putExtra(INTENT_URL_CANDIDATES, uris)
                )
            }
        }
    }

    private val miuiExpress by lazy {
        IntentCompat.getParcelableExtra(intent, INTENT_EXPRESS_SUMMARY, MiuiExpress::class.java)
    }
    private val uris by lazy {
        IntentCompat.getParcelableArrayListExtra(intent, INTENT_URL_CANDIDATES, ExpressInfoJumpListWrapper::class.java)
    }
    private val viewModel by viewModels<ExpressDetailsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && intent.getBooleanExtra(INTENT_CUSTOM_TRANSITION, false)) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                R.anim.express_details_hold,
                R.anim.express_details_fade_out
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setTaskDescription(ActivityManager.TaskDescription(getString(R.string.express_details_title), R.drawable.ic_pa))
        } else {
            setTaskDescription(ActivityManager.TaskDescription(getString(R.string.express_details_title)))
        }
        val summary = miuiExpress
        if (summary == null) {
            Toast.makeText(this, R.string.unexpected_error, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContent {
            BetterMiuiExpressTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                ExpressDetailsScreen(
                    summary = summary,
                    state = state,
                    canJump = !uris.isNullOrEmpty(),
                    onBack = { onBackPressedDispatcher.onBackPressed() },
                    onJump = { startThirdAppByUris(uris) },
                    onCopyDebug = {
                        (getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(
                            ClipData.newPlainText("BME-Debug", summary.copy(phoneNumber = null).toString())
                        )
                        Toast.makeText(this, R.string.debug_info_copied, Toast.LENGTH_SHORT).show()
                    },
                    onDial = { number -> startActivity(Intent(Intent.ACTION_DIAL, "tel:$number".toUri())) }
                )
            }
        }
        queryExpressDetails()
    }

    @Suppress("DEPRECATION")
    override fun finish() {
        super.finish()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE && intent.getBooleanExtra(INTENT_CUSTOM_TRANSITION, false)) {
            overridePendingTransition(R.anim.express_details_hold, R.anim.express_details_fade_out)
        }
    }

    private fun queryExpressDetails() {
        val summary = miuiExpress ?: return
        viewModel.queryExpressDetails(
            summary.mailNumber, summary.companyCode, summary.phoneNumber,
            deviceTrackId
        ) { generatedTrackId ->
            deviceTrackId = generatedTrackId
            Toast.makeText(this, R.string.init_success, Toast.LENGTH_SHORT).show()
        }
    }
    private fun startThirdAppByUris(jumpList: ArrayList<ExpressInfoJumpListWrapper>?) {
        if (jumpList.isNullOrEmpty()) {
            return
        }

        val sortedList = jumpList.sorted()

        for (uri in sortedList) {
            if (uri.type == "applet" && !resolvePackage("com.tencent.mm")) {
                // Missing wechat
            } else if (uri.link.isNullOrEmpty()) {
                // Missing jump link
            } else {
                val intent = Intent("android.intent.action.VIEW")
                intent.data = uri.link.toUri()
                if (tryToStartThirdPartyActivity(intent)) {
                    finish()
                    return
                }
            }
        }

        Toast.makeText(this, R.string.failed_to_jump, Toast.LENGTH_SHORT).show()
    }

    private fun resolvePackage(packageName: String?): Boolean {
        return try {
            packageName != null && packageManager.getPackageInfo(packageName, 0) != null
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun tryToStartThirdPartyActivity(intent: Intent): Boolean {
        return try {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            true
        } catch (_: Exception) {
            false
        }
    }

}
