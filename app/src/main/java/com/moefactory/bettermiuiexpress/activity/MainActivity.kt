package com.moefactory.bettermiuiexpress.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.moefactory.bettermiuiexpress.R
import com.moefactory.bettermiuiexpress.base.ui.BaseActivity
import com.moefactory.bettermiuiexpress.databinding.ActivityMainBinding
import com.moefactory.bettermiuiexpress.model.Credential
import com.moefactory.bettermiuiexpress.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>(false) {

    override val viewBinding by viewBinding(ActivityMainBinding::inflate)
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(viewBinding.mtToolbar)

        viewModel.getSavedCredential().observe(this) {
            viewBinding.tietCustomer.setText(it.customer)
            viewBinding.tietKey.setText(it.secretKey)
        }
        viewBinding.btnSave.setOnClickListener {
            if (viewBinding.tietCustomer.text.isNullOrEmpty() || viewBinding.tietKey.text.isNullOrEmpty()) {
                Toast.makeText(this, R.string.empty_tips, Toast.LENGTH_SHORT).show()
            } else {
                viewModel.saveCredential(
                    Credential(
                        viewBinding.tietCustomer.text.toString(),
                        viewBinding.tietKey.text.toString()
                    )
                )
                Toast.makeText(this, R.string.save_successfully, Toast.LENGTH_SHORT).show()
            }
        }
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