package com.moefactory.bettermiuiexpress.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.YukiHookAPI.Status.Executor
import com.highcapable.yukihookapi.hook.factory.prefs
import com.moefactory.bettermiuiexpress.R
import com.moefactory.bettermiuiexpress.base.app.PREF_KEY_DEVICE_TRACK_ID
import com.moefactory.bettermiuiexpress.base.ui.BaseActivity
import com.moefactory.bettermiuiexpress.ui.BetterMiuiExpressTheme
import com.moefactory.bettermiuiexpress.ui.MainScreen
import com.moefactory.bettermiuiexpress.ktx.hideLauncherIcon
import com.moefactory.bettermiuiexpress.ktx.isLauncherIconEnabled
import com.moefactory.bettermiuiexpress.repository.ExpressActualRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@SuppressLint("WorldReadableFiles")
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val moduleActive = YukiHookAPI.Status.isModuleActive
        if (moduleActive) {
            lifecycleScope.launch(Dispatchers.IO) {
                val currentGeneratedTrackId = prefs().getString(PREF_KEY_DEVICE_TRACK_ID)
                if (currentGeneratedTrackId.isNotEmpty()) {
                    return@launch
                }

                val generatedTrackId = UUID.randomUUID().toString()
                if (ExpressActualRepository.registerDeviceTrackIdActual(generatedTrackId)) {
                    prefs().edit {
                        putString(PREF_KEY_DEVICE_TRACK_ID, generatedTrackId)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, R.string.init_success_and_hide, Toast.LENGTH_SHORT).show()
                    }

                    delay(5000)

                    if (isLauncherIconEnabled()) {
                        hideLauncherIcon()
                    }
                }
            }
        }

        setContent {
            BetterMiuiExpressTheme {
                MainScreen(
                    isModuleActive = moduleActive,
                    frameworkName = if (moduleActive) Executor.name else "",
                    frameworkApiLevel = if (moduleActive) Executor.apiLevel else 0,
                    yukiVersion = YukiHookAPI.VERSION,
                    onOpenLink = { url ->
                        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    }
                )
            }
        }
    }
}
