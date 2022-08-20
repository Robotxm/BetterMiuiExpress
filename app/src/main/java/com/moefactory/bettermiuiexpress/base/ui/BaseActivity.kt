package com.moefactory.bettermiuiexpress.base.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.moefactory.bettermiuiexpress.R

/**
 * A base activity class for application.
 *
 * All activities must inherit from this class.
 *
 * @property showActionBar Whether to show action bar
 * @property title Title for action bar
 */
abstract class BaseActivity<T : ViewBinding>(
    private val showActionBar: Boolean,
    private val showBackButton: Boolean = false,
    @StringRes private val title: Int? = null
) : AppCompatActivity() {

    protected inline fun <T : ViewBinding> AppCompatActivity.viewBinding(crossinline bindingInflater: (LayoutInflater) -> T) =
        lazy {
            val invoke = bindingInflater.invoke(layoutInflater)
            invoke
        }

    /**
     * View binding for activity. Must be overridden in sub classes.
     */
    protected abstract val viewBinding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        if (supportActionBar == null) {
            return
        }

        if (showActionBar) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(showBackButton)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_actionbar_back)
            if (title != null) {
                supportActionBar!!.setTitle(title)
            }
        } else {
            supportActionBar!!.hide()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            exitActivityWithAnimation()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /**
     * A united method to start an activity, which provides consistent animation.
     *
     * If animation is not needed, use [AppCompatActivity.startActivity] instead.
     */
    fun startActivityWithAnimation(targetIntent: Intent) {
        startActivity(targetIntent)
        doEnterTransition()
    }

    /**
     * A united method to exit an activity, which provides consistent animation.
     */
    fun exitActivityWithAnimation() {
        finish()
        doExitTransition()
    }

    fun doEnterTransition() {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun doExitTransition() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}