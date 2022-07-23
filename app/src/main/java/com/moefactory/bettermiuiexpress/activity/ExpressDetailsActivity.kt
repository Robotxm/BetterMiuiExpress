package com.moefactory.bettermiuiexpress.activity

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.github.vipulasri.timelineview.TimelineView
import com.moefactory.bettermiuiexpress.R
import com.moefactory.bettermiuiexpress.base.app.secretKey
import com.moefactory.bettermiuiexpress.base.ui.BaseActivity
import com.moefactory.bettermiuiexpress.databinding.ActivityExpressDetailsBinding
import com.moefactory.bettermiuiexpress.databinding.ItemTimelineNodeBinding
import com.moefactory.bettermiuiexpress.ktx.dp
import com.moefactory.bettermiuiexpress.model.ExpressDetails
import com.moefactory.bettermiuiexpress.model.KuaiDi100ExpressState
import com.moefactory.bettermiuiexpress.model.MiuiExpress
import com.moefactory.bettermiuiexpress.model.TimelineAttributes
import com.moefactory.bettermiuiexpress.utils.ExpressCompanyUtils
import com.moefactory.bettermiuiexpress.viewmodel.ExpressDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*

class ExpressDetailsActivity : BaseActivity<ActivityExpressDetailsBinding>(false) {

    companion object {
        private const val ACTION_GO_TO_DETAILS = "com.moefactory.bettermiuiexpress.details"
        const val INTENT_EXPRESS_SUMMARY = "express_summary"
        const val INTENT_URL_CANDIDATES = "url_candidates"

        fun gotoDetailsActivity(
            context: Context,
            miuiExpress: MiuiExpress,
            urlList: ArrayList<String>?
        ) {
            if (context is Activity) { // Click items in details activity
                context.startActivity(
                    Intent(ACTION_GO_TO_DETAILS)
                        .putExtra(INTENT_EXPRESS_SUMMARY, miuiExpress)
                        .putExtra(INTENT_URL_CANDIDATES, urlList)
                )
            } else { // Click items in card
                context.startActivity(
                    Intent(ACTION_GO_TO_DETAILS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(INTENT_EXPRESS_SUMMARY, miuiExpress)
                        .putExtra(INTENT_URL_CANDIDATES, urlList)
                )
            }
        }
    }

    override val viewBinding by viewBinding(ActivityExpressDetailsBinding::inflate)
    private val miuiExpress by lazy { intent.getParcelableExtra<MiuiExpress>(INTENT_EXPRESS_SUMMARY) }
    private val urlCandidates by lazy { intent.getStringArrayListExtra(INTENT_URL_CANDIDATES) }
    private val viewModel by viewModels<ExpressDetailsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTaskDescription(ActivityManager.TaskDescription(getString(R.string.express_details_title)))

        if (miuiExpress == null) {
            Toast.makeText(this, R.string.unexpected_error, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewBinding.stateLayout.apply {
            loadingLayout = R.layout.loading_layout
            emptyLayout = R.layout.empty_layout
            errorLayout = R.layout.empty_layout

            onContent {
                viewBinding.rvTimeline.doOnNextLayout {
                    val areAllItemsVisible =
                        (viewBinding.rvTimeline.layoutManager as LinearLayoutManager)
                            .findLastCompletelyVisibleItemPosition() == (viewBinding.rvTimeline.adapter?.itemCount
                            ?: 1) - 1
                    if (areAllItemsVisible) {
                        viewBinding.rvTimeline.overScrollMode = View.OVER_SCROLL_NEVER
                    }
                }
            }
        }

        setSupportActionBar(viewBinding.mtToolbar)
        viewBinding.actionBarTitle.text = miuiExpress?.companyName
        viewBinding.up.setOnClickListener { onBackPressed() }

        viewBinding.tvMailNumber.text =
            getString(R.string.express_details_mail_number, miuiExpress!!.mailNumber)

        initTimeline()

        viewModel.queryCompanyResult.observe(this) {
            if (it.isSuccess) {
                viewModel.queryExpressDetails(
                    it.getOrNull()!![0].companyCode,
                    miuiExpress!!.mailNumber
                )
            } else {
                viewBinding.tvStatus.setText(R.string.express_state_unknown)
                viewBinding.stateLayout.showError()
            }
        }
        viewModel.queryExpressResult.observe(this) {
            if (it.isSuccess) {
                val response = it.getOrNull()
                if (response == null) {
                    viewBinding.stateLayout.showEmpty()
                    return@observe
                }
                if (response.result != null) {
                    viewBinding.stateLayout.showEmpty()
                    return@observe
                }
                val state = KuaiDi100ExpressState.statesMap.find { state ->
                    state.categoryCode == response.state
                }!!
                viewBinding.tvStatus.setText(state.categoryNameId)
                viewBinding.rvTimeline.models = response.data
                viewBinding.stateLayout.showContent()
            }
        }

        viewBinding.stateLayout.showLoading()
        // Try to convert CaiNiao company code to KuaiDi100 company code
        val companyCode = ExpressCompanyUtils.convertCode(miuiExpress!!.companyCode)
        if (companyCode != null) {
            viewModel.queryExpressDetails(
                companyCode,
                miuiExpress!!.mailNumber
            )
        } else {
            viewModel.queryCompany(secretKey, miuiExpress!!.mailNumber)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_jump -> {
                startThirdAppByList(urlCandidates!!)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!urlCandidates.isNullOrEmpty()) {
            menuInflater.inflate(R.menu.details_menu, menu)
        }
        return true
    }

    private fun startThirdAppByList(urlCandidates: ArrayList<String>) {
        for (url in urlCandidates) {
            try {
                startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .setData(Uri.parse(url))
                )
                return
            } catch (_: Exception) {
                // No need to process
            }
        }
    }

    private fun initTimeline() {
        val lineColor = ContextCompat.getColor(this, R.color.timelineLineColor)
        val attributes = TimelineAttributes(
            markerSize = 10.dp,
            markerColor = Color.TRANSPARENT,
            markerInCenter = true,
            markerLeftPadding = 0.dp,
            markerTopPadding = 0.dp,
            markerRightPadding = 0.dp,
            markerBottomPadding = 0.dp,
            linePadding = 0.dp,
            startLineColor = lineColor,
            endLineColor = lineColor,
            lineStyle = TimelineView.LineStyle.NORMAL,
            lineWidth = 4.dp,
            lineDashWidth = 4.dp,
            lineDashGap = 2.dp
        )

        viewBinding.rvTimeline.linear().setup {
            addType<ExpressDetails>(R.layout.item_timeline_node)

            onBind {
                val binding = ItemTimelineNodeBinding.bind(itemView)
                val expressDetails = getModel<ExpressDetails>()

                binding.node.apply {
                    markerSize = attributes.markerSize
                    setMarkerColor(attributes.markerColor)
                    isMarkerInCenter = attributes.markerInCenter
                    markerPaddingLeft = attributes.markerLeftPadding
                    markerPaddingTop = attributes.markerTopPadding
                    markerPaddingRight = attributes.markerRightPadding
                    markerPaddingBottom = attributes.markerBottomPadding
                    linePadding = attributes.linePadding

                    lineWidth = attributes.lineWidth
                    lineStyle = attributes.lineStyle
                    lineStyleDashLength = attributes.lineDashWidth
                    lineStyleDashGap = attributes.lineDashGap

                    val nodeType =
                        TimelineView.getTimeLineViewType(absoluteAdapterPosition, itemCount)
                    when (absoluteAdapterPosition) {
                        0 -> setEndLineColor(attributes.endLineColor, nodeType)
                        itemCount -> setStartLineColor(attributes.startLineColor, nodeType)
                        else -> {
                            setStartLineColor(attributes.startLineColor, nodeType)
                            setEndLineColor(attributes.endLineColor, nodeType)
                        }
                    }
                }

                if (absoluteAdapterPosition == 0) {
                    if (expressDetails.status == KuaiDi100ExpressState.Trouble.toString()) {
                        binding.node.marker =
                            AppCompatResources.getDrawable(context, R.drawable.dot_trouble)
                    } else {
                        binding.node.marker =
                            AppCompatResources.getDrawable(context, R.drawable.dot_current)
                    }
                    binding.tvDatetime.setTextColor(context.getColor(R.color.pa_express_progress_item_first_text))
                    binding.tvCurrentStatus.setTextColor(context.getColor(R.color.pa_express_progress_item_first_text))
                } else {
                    binding.node.marker =
                        AppCompatResources.getDrawable(context, R.drawable.dot_previous)
                    binding.tvDatetime.setTextColor(context.getColor(R.color.pa_express_progress_item_text))
                    binding.tvCurrentStatus.setTextColor(context.getColor(R.color.pa_express_progress_item_text))
                }

                val originalSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                val newSdf = SimpleDateFormat("MM-dd\nHH:mm", Locale.CHINA)
                val datetime = originalSdf.parse(expressDetails.formattedTime)
                val newDatetime = newSdf.format(datetime!!)
                binding.tvDatetime.text = newDatetime

                val currentStatus = expressDetails.context
                val spannableStringBuilder = SpannableStringBuilder(currentStatus)
                val regex = "1[3|4|5|7|8][0-9]\\d{8}".toRegex()
                val matches = regex.findAll(currentStatus)
                for (match in matches) {
                    spannableStringBuilder.setSpan(
                        URLSpan("tel:${match.value}"),
                        match.range.first,
                        match.range.last + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                binding.tvCurrentStatus.text = spannableStringBuilder
                binding.tvCurrentStatus.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }
}