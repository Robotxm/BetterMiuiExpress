package com.moefactory.bettermiuiexpress.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.highcapable.yukihookapi.YukiHookAPI
import com.moefactory.bettermiuiexpress.R
import com.moefactory.bettermiuiexpress.base.ui.BaseActivity
import com.moefactory.bettermiuiexpress.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(false) {

    override val viewBinding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(viewBinding.mtToolbar)

        viewBinding.btnGithub.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/Robotxm/BetterMiuiExpress")))
        }
        viewBinding.btnCoolapk.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://coolapk.com/apk/com.moefactory.bettermiuiexpress")))
        }
        viewBinding.btnBlog.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://moefactory.com")))
        }
        viewBinding.mcvYuki.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/fankes/YukiHookAPI")))
        }

        viewBinding.tvYukiVersion.text =
            getString(R.string.yuki_version, YukiHookAPI.API_VERSION_NAME, YukiHookAPI.API_VERSION_CODE)

        if (YukiHookAPI.Status.isModuleActive) {
            viewBinding.tvStatus.setText(R.string.active)
            viewBinding.tvStatusDescription.text = getString(
                R.string.active_hook_framework_version,
                YukiHookAPI.Status.executorName,
                YukiHookAPI.Status.executorVersion
            )
            viewBinding.ivStatus.setImageResource(R.drawable.ic_active)
        } else {
            viewBinding.tvStatus.setText(R.string.inactive)
            viewBinding.tvStatusDescription.setText(R.string.inactive_description)
            viewBinding.ivStatus.setImageResource(R.drawable.ic_inactive)
        }
    }
}