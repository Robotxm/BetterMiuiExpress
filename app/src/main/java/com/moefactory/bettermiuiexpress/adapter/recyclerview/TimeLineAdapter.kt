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
            if (expressDetails.status == KuaiDi100ExpressState.Trouble.categoryCode.toString()) {
                holder.timelineNode.marker =
                    AppCompatResources.getDrawable(context, R.drawable.dot_trouble)
            } else {
                holder.timelineNode.marker =
                    AppCompatResources.getDrawable(context, R.drawable.dot_current)
            }
            holder.tvDatetime.setTextColor(context.getColor(R.color.currentNodeNormalTextColor))
            holder.tvCurrentStatus.setTextColor(context.getColor(R.color.currentNodeNormalTextColor))
        } else {
            holder.timelineNode.marker =
                AppCompatResources.getDrawable(context, R.drawable.dot_previous)
        }

        val originalSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        val newSdf = SimpleDateFormat("MM-dd\nHH:mm", Locale.CHINA)
        val datetime = originalSdf.parse(expressDetails.formatedTime)
        val newDatetime = newSdf.format(datetime!!)
        holder.tvDatetime.text = newDatetime
        holder.tvCurrentStatus.text = expressDetails.context
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
