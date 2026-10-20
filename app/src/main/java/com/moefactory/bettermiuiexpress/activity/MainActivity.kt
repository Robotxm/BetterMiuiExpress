package com.moefactory.bettermiuiexpress.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.highcapable.yukihookapi.YukiHook
import com.highcapable.yukihookapi.YukiHook.Status.Executor
import com.moefactory.bettermiuiexpress.R
import com.moefactory.bettermiuiexpress.ktx.deviceTrackId
import com.moefactory.bettermiuiexpress.base.ui.BaseActivity
import com.moefactory.bettermiuiexpress.ui.BetterMiuiExpressTheme
import com.moefactory.bettermiuiexpress.ui.MainScreen
import com.moefactory.bettermiuiexpress.ktx.hideLauncherIcon
import com.moefactory.bettermiuiexpress.ktx.isLauncherIconEnabled
import com.moefactory.bettermiuiexpress.repository.ExpressActualRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainActivity : BaseActivity() {

    private var initializationJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var moduleActive by mutableStateOf(false)
        var frameworkName by mutableStateOf("")
        var frameworkApiVersion by mutableStateOf(0)
        YukiHook.Status.observeFrameworkService(lifecycle) { available ->
            moduleActive = available && YukiHook.Status.isModuleActive
            frameworkName = if (moduleActive) Executor.name else ""
            frameworkApiVersion = if (moduleActive) Executor.apiVersion else 0
            if (moduleActive && initializationJob?.isActive != true) {
                initializationJob = lifecycleScope.launch(Dispatchers.IO) {
                    if (deviceTrackId.isNotEmpty()) {
                        return@launch
                    }

                    val generatedTrackId = UUID.randomUUID().toString()
                    if (ExpressActualRepository.registerDeviceTrackIdActual(generatedTrackId)) {
                        deviceTrackId = generatedTrackId

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, R.string.init_success_and_hide, Toast.LENGTH_SHORT).show()
                        }

                        delay(5000)

                        if (YukiHook.Status.isModuleActive && isLauncherIconEnabled()) {
                            hideLauncherIcon()
                        }
                    }
                }
            }
        }

        setContent {
            BetterMiuiExpressTheme {
                MainScreen(
                    isModuleActive = moduleActive,
                    frameworkName = frameworkName,
                    frameworkApiLevel = frameworkApiVersion,
                    yukiVersion = YukiHook.VERSION,
                    onOpenLink = { url ->
                        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    }
                )
            }
        }
    }
}
