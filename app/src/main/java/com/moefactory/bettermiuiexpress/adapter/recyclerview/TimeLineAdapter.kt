package com.moefactory.bettermiuiexpress.adapter.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.moefactory.bettermiuiexpress.R
import com.moefactory.bettermiuiexpress.model.ExpressDetails
import com.moefactory.bettermiuiexpress.model.KuaiDi100ExpressState
import com.moefactory.bettermiuiexpress.model.TimelineAttributes
import java.text.SimpleDateFormat
import java.util.*
import android.text.method.LinkMovementMethod
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.URLSpan

class TimeLineAdapter(
    private val detailList: List<ExpressDetails>,
    private var attributes: TimelineAttributes
) : RecyclerView.Adapter<TimeLineAdapter.TimeLineViewHolder>() {

    private lateinit var layoutInflater: LayoutInflater

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {

        if (!::layoutInflater.isInitialized) {
            layoutInflater = LayoutInflater.from(parent.context)
        }

        val view = layoutInflater.inflate(R.layout.item_timeline_node, parent, false)
        return TimeLineViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        val expressDetails = detailList[position]
        val context = holder.itemView.context

        if (position == 0) {
            if (expressDetails.status == KuaiDi100ExpressState.Trouble.toString()) {
                holder.timelineNode.marker =
                    AppCompatResources.getDrawable(context, R.drawable.dot_trouble)
            } else {
                holder.timelineNode.marker =
                    AppCompatResources.getDrawable(context, R.drawable.dot_current)
            }
            holder.tvDatetime.setTextColor(context.getColor(R.color.pa_express_progress_item_first_text))
            holder.tvCurrentStatus.setTextColor(context.getColor(R.color.pa_express_progress_item_first_text))
        } else {
            holder.timelineNode.marker =
                AppCompatResources.getDrawable(context, R.drawable.dot_previous)
            holder.tvDatetime.setTextColor(context.getColor(R.color.pa_express_progress_item_text))
            holder.tvCurrentStatus.setTextColor(context.getColor(R.color.pa_express_progress_item_text))
        }

        val originalSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        val newSdf = SimpleDateFormat("MM-dd\nHH:mm", Locale.CHINA)
        val datetime = originalSdf.parse(expressDetails.formattedTime)
        val newDatetime = newSdf.format(datetime!!)
        holder.tvDatetime.text = newDatetime

        val currentStatus = expressDetails.context
        val spannableStringBuilder = SpannableStringBuilder(currentStatus)
        val regex = "1[3|4|5|7|8][0-9]\\d{8}".toRegex()
        val matches = regex.findAll(currentStatus)
        for (match in matches) {
            spannableStringBuilder.setSpan(
                URLSpan("tel:${match.value}"),
                match.range.first,
                match.range.last + 1,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        holder.tvCurrentStatus.text = spannableStringBuilder
        holder.tvCurrentStatus.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun getItemCount() = detailList.size

    inner class TimeLineViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView) {

        val tvDatetime = itemView.findViewById<TextView>(R.id.tv_datetime)
        val timelineNode = itemView.findViewById<TimelineView>(R.id.node)
        val tvCurrentStatus = itemView.findViewById<TextView>(R.id.tv_current_status)

        init {
            timelineNode.initLine(viewType)
            timelineNode.markerSize = attributes.markerSize
            timelineNode.setMarkerColor(attributes.markerColor)
            timelineNode.isMarkerInCenter = attributes.markerInCenter
            timelineNode.markerPaddingLeft = attributes.markerLeftPadding
            timelineNode.markerPaddingTop = attributes.markerTopPadding
            timelineNode.markerPaddingRight = attributes.markerRightPadding
            timelineNode.markerPaddingBottom = attributes.markerBottomPadding
            timelineNode.linePadding = attributes.linePadding

            timelineNode.lineWidth = attributes.lineWidth
            timelineNode.setStartLineColor(attributes.startLineColor, viewType)
            timelineNode.setEndLineColor(attributes.endLineColor, viewType)
            timelineNode.lineStyle = attributes.lineStyle
            timelineNode.lineStyleDashLength = attributes.lineDashWidth
            timelineNode.lineStyleDashGap = attributes.lineDashGap
        }
    }
}
