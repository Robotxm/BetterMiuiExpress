package com.moefactory.bettermiuiexpress.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.highcapable.yukihookapi.YukiHookAPI
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
    }
}