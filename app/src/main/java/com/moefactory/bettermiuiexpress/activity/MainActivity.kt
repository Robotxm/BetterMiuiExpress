package com.moefactory.bettermiuiexpress.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.YukiHookAPI.Status.Executor
import com.highcapable.yukihookapi.hook.factory.prefs
import com.moefactory.bettermiuiexpress.R
import com.moefactory.bettermiuiexpress.base.app.PREF_KEY_DEVICE_TRACK_ID
import com.moefactory.bettermiuiexpress.base.ui.BaseActivity
import com.moefactory.bettermiuiexpress.databinding.ActivityMainBinding
import com.moefactory.bettermiuiexpress.ktx.hideLauncherIcon
import com.moefactory.bettermiuiexpress.ktx.isLauncherIconEnabled
import com.moefactory.bettermiuiexpress.repository.ExpressActualRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@SuppressLint("WorldReadableFiles")
class MainActivity : BaseActivity<ActivityMainBinding>(false) {

    override val viewBinding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(viewBinding.mtToolbar)

        viewBinding.btnGithub.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).setData("https://github.com/Robotxm/BetterMiuiExpress".toUri()))
        }
        viewBinding.btnBlog.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).setData("https://moefactory.com".toUri()))
        }
        viewBinding.mcvYuki.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).setData("https://github.com/HighCapable/YukiHookAPI".toUri()))
        }

        viewBinding.tvYukiVersion.text = getString(R.string.yuki_version, YukiHookAPI.VERSION)

        if (YukiHookAPI.Status.isModuleActive) {
            viewBinding.tvStatus.setText(R.string.active)
            viewBinding.tvStatusDescription.text = getString(
                R.string.active_hook_framework_version,
                Executor.name,
                Executor.apiLevel
            )
            viewBinding.ivStatus.setImageResource(R.drawable.ic_active)
        } else {
            viewBinding.tvStatus.setText(R.string.inactive)
            viewBinding.tvStatusDescription.setText(R.string.inactive_description)
            viewBinding.ivStatus.setImageResource(R.drawable.ic_inactive)
        }

        if (YukiHookAPI.Status.isModuleActive) {
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
    }
}