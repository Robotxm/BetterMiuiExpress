package com.moefactory.bettermiuiexpress.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.highcapable.yukihookapi.YukiHook
import com.highcapable.yukihookapi.YukiHook.Status.Executor
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.reload.entity.ModuleReloadResult
import com.highcapable.yukihookapi.hook.xposed.reload.entity.ModuleReloadStatus
import com.moefactory.bettermiuiexpress.R
import com.moefactory.bettermiuiexpress.base.app.PA_PACKAGE_NAME
import com.moefactory.bettermiuiexpress.ktx.deviceTrackId
import com.moefactory.bettermiuiexpress.base.ui.BaseActivity
import com.moefactory.bettermiuiexpress.ui.BetterMiuiExpressTheme
import com.moefactory.bettermiuiexpress.ui.MainScreen
import com.moefactory.bettermiuiexpress.ktx.hideLauncherIcon
import com.moefactory.bettermiuiexpress.ktx.isLauncherIconEnabled
import com.moefactory.bettermiuiexpress.repository.ExpressActualRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume

class MainActivity : BaseActivity() {

    private var initializationJob: Job? = null
    private var reloadAvailable by mutableStateOf(false)
    private var reloading by mutableStateOf(false)
    private var reloadStatus by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var moduleActive by mutableStateOf(false)
        var frameworkName by mutableStateOf("")
        var frameworkApiVersion by mutableStateOf(0)
        YukiHook.Status.observeFrameworkService(lifecycle) { available ->
            moduleActive = available && YukiHook.Status.isModuleActive
            frameworkName = if (moduleActive) Executor.name else ""
            frameworkApiVersion = if (moduleActive) Executor.apiVersion else 0
            reloadAvailable = moduleActive && YukiHook.Module.reload.isAvailable
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
                    reloadAvailable = reloadAvailable,
                    reloading = reloading,
                    reloadStatus = reloadStatus,
                    onReload = ::reloadModule,
                    onOpenLink = { url ->
                        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    }
                )
            }
        }
    }

    private fun reloadModule() {
        if (reloading) {
            return
        }
        reloading = true
        reloadStatus = getString(R.string.module_reload_running)
        lifecycleScope.launch {
            try {
                val reload = YukiHook.Module.reload
                if (!reload.isAvailable) {
                    YLog.warn("Module reload unavailable: framework service does not support reload", tag = "BetterMiuiExpress")
                    reloadAvailable = false
                    reloadStatus = getString(R.string.module_reload_unavailable)
                    return@launch
                }
                val targets = withContext(Dispatchers.IO) {
                    reload.targets.filter { it.processName == PA_PACKAGE_NAME || it.processName.startsWith("$PA_PACKAGE_NAME:") }
                }
                if (targets.isEmpty()) {
                    YLog.warn("Module reload: no running Personal Assistant targets", tag = "BetterMiuiExpress")
                    reloadStatus = getString(R.string.module_reload_no_target)
                    return@launch
                }
                val messages = mutableListOf<String>()
                for (target in targets) {
                    YLog.info("Requesting module reload: $target", tag = "BetterMiuiExpress")
                    val result = withContext(Dispatchers.IO) {
                        suspendCancellableCoroutine<ModuleReloadResult> { continuation ->
                            reload.request(target) { continuation.resume(it) }
                        }
                    }
                    val message = when (result.status) {
                        ModuleReloadStatus.SUCCEEDED -> R.string.module_reload_success
                        ModuleReloadStatus.SKIPPED -> R.string.module_reload_up_to_date
                        ModuleReloadStatus.UNSUPPORTED -> R.string.module_reload_unsupported
                        ModuleReloadStatus.IN_PROGRESS -> R.string.module_reload_in_progress
                        ModuleReloadStatus.PROCESS_DIED -> R.string.module_reload_process_died
                        ModuleReloadStatus.FAILED -> R.string.module_reload_failed
                    }
                    val diagnostic = result.message?.takeIf { it.isNotBlank() }
                    val logMessage = "Module reload result: target=$target, status=${result.status}, message=${diagnostic ?: "<none>"}"
                    if (result.status == ModuleReloadStatus.SUCCEEDED || result.status == ModuleReloadStatus.SKIPPED) {
                        YLog.info(logMessage, tag = "BetterMiuiExpress")
                    } else {
                        YLog.warn(logMessage, tag = "BetterMiuiExpress")
                    }
                    messages += buildString {
                        append("${target.processName}: ${getString(message)}")
                        if (diagnostic != null) {
                            append("\n${getString(R.string.module_reload_diagnostic, diagnostic)}")
                        }
                    }
                    reloadStatus = messages.joinToString("\n")
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                YLog.error("Failed to request module reload", e = exception, tag = "BetterMiuiExpress")
                reloadStatus = getString(R.string.module_reload_failed) + "\n" +
                    getString(R.string.module_reload_diagnostic, "${exception.javaClass.simpleName}: ${exception.message.orEmpty()}")
            } finally {
                reloading = false
            }
        }
    }
}

@Preview(name = "Active - Light", showBackground = true)
@Preview(name = "Active - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainActivityActivePreview() {
    BetterMiuiExpressTheme {
        MainScreen(
            isModuleActive = true,
            frameworkName = "LSPosed",
            frameworkApiLevel = 102,
            yukiVersion = "1.5.0",
            reloadAvailable = true,
            reloading = false,
            reloadStatus = "",
            onReload = {},
            onOpenLink = {}
        )
    }
}

@Preview(name = "Inactive - Light", showBackground = true)
@Preview(name = "Inactive - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainActivityInactivePreview() {
    BetterMiuiExpressTheme {
        MainScreen(
            isModuleActive = false,
            frameworkName = "",
            frameworkApiLevel = 0,
            yukiVersion = "1.5.0",
            reloadAvailable = false,
            reloading = false,
            reloadStatus = "",
            onReload = {},
            onOpenLink = {}
        )
    }
}
