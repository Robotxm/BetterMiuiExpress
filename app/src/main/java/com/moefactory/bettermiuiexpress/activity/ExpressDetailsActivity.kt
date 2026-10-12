package com.moefactory.bettermiuiexpress.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
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
import com.highcapable.yukihookapi.hook.factory.prefs
import com.moefactory.bettermiuiexpress.R
import com.moefactory.bettermiuiexpress.base.app.PREF_KEY_DEVICE_TRACK_ID
import com.moefactory.bettermiuiexpress.base.ui.BaseActivity
import com.moefactory.bettermiuiexpress.model.MiuiExpress
import com.moefactory.bettermiuiexpress.model.ExpressInfoJumpListWrapper
import com.moefactory.bettermiuiexpress.ui.BetterMiuiExpressTheme
import com.moefactory.bettermiuiexpress.ui.ExpressDetailsScreen
import com.moefactory.bettermiuiexpress.viewmodel.ExpressDetailsViewModel

@SuppressLint("WorldReadableFiles")
class ExpressDetailsActivity : BaseActivity() {
    companion object {
        private const val ACTION_GO_TO_DETAILS = "com.moefactory.bettermiuiexpress.details"
        const val INTENT_EXPRESS_SUMMARY = "express_summary"
        const val INTENT_URL_CANDIDATES = "url_candidates"

        fun gotoDetailsActivity(
            context: Context,
            miuiExpress: MiuiExpress,
            uris: ArrayList<ExpressInfoJumpListWrapper>?
        ) {
            if (context is Activity) { // Click items in details activity
                context.startActivity(
                    Intent(ACTION_GO_TO_DETAILS)
                        .putExtra(INTENT_EXPRESS_SUMMARY, miuiExpress)
                        .putExtra(INTENT_URL_CANDIDATES, uris)
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
                    onRetry = { queryExpressDetails(refresh = true) },
                    onDial = { number -> startActivity(Intent(Intent.ACTION_DIAL, "tel:$number".toUri())) }
                )
            }
        }
        queryExpressDetails()
    }

    private fun queryExpressDetails(refresh: Boolean = false) {
        val summary = miuiExpress ?: return
        viewModel.queryExpressDetails(
            summary.mailNumber, summary.companyCode, summary.phoneNumber,
            prefs().getString(PREF_KEY_DEVICE_TRACK_ID), refresh
        ) { generatedTrackId ->
            prefs().edit { putString(PREF_KEY_DEVICE_TRACK_ID, generatedTrackId) }
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
