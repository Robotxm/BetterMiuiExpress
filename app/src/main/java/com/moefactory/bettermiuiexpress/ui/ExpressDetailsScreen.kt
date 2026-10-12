package com.moefactory.bettermiuiexpress.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberPlatformOverscrollFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Velocity
import com.moefactory.bettermiuiexpress.R
import com.moefactory.bettermiuiexpress.model.ExpressDetails
import com.moefactory.bettermiuiexpress.model.ExpressTrace
import com.moefactory.bettermiuiexpress.model.MiuiExpress
import com.moefactory.bettermiuiexpress.viewmodel.ExpressDetailsUiState
import com.vipulasri.timelineview.compose.LineStyle
import com.vipulasri.timelineview.compose.Timeline
import com.vipulasri.timelineview.compose.getLineType
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.overlay.OverlayListPopup

@Composable
fun ExpressDetailsScreen(
    summary: MiuiExpress,
    state: ExpressDetailsUiState,
    canJump: Boolean,
    onBack: () -> Unit,
    onJump: () -> Unit,
    onCopyDebug: () -> Unit,
    onDial: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    var phoneToCall by rememberSaveable { mutableStateOf("") }

    var showCallDialog by rememberSaveable { mutableStateOf(false) }

    val timelineState = rememberLazyListState()

    val appBarState = rememberTopAppBarState()

    val canCoordinateScroll by remember(timelineState, appBarState, state is ExpressDetailsUiState.Content) {
        derivedStateOf {
            state is ExpressDetailsUiState.Content && timelineState.layoutInfo.totalItemsCount > 0 &&
                (timelineState.canScrollBackward || timelineState.canScrollForward || appBarState.heightOffset < 0f)
        }
    }

    val scrollEnabled = rememberUpdatedState(canCoordinateScroll)

    val scrollBehavior = MiuixScrollBehavior(state = appBarState, canScroll = { scrollEnabled.value })

    val scrollConnection = remember(appBarState, scrollBehavior) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!scrollEnabled.value) {
                    return Offset.Zero
                }

                val previousOffset = appBarState.heightOffset
                appBarState.heightOffset += available.y
                return Offset(0f, appBarState.heightOffset - previousOffset)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (scrollEnabled.value) {
                    appBarState.contentOffset += consumed.y
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!scrollEnabled.value) {
                    return Velocity.Zero
                }
                return scrollBehavior.nestedScrollConnection.onPostFling(consumed, available)
            }
        }
    }

    LaunchedEffect(state is ExpressDetailsUiState.Content) {
        if (state !is ExpressDetailsUiState.Content) {
            appBarState.heightOffset = 0f
            appBarState.contentOffset = 0f
        }
    }

    val backgroundColor = colorResource(R.color.express_details_background)

    Scaffold(containerColor = backgroundColor, topBar = {
        TopAppBar(
            title = summary.companyName,
            modifier = Modifier.combinedClickable(onClick = {}, onLongClick = onCopyDebug),
            color = backgroundColor,
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = MiuixIcons.ChevronBackward,
                        contentDescription = stringResource(R.string.navigate_back)
                    )
                }
            },
            actions = {
                if (canJump) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = MiuixIcons.More,
                                contentDescription = stringResource(R.string.more_options)
                            )
                        }

                        OverlayListPopup(
                            show = showMenu,
                            alignment = PopupPositionProvider.Align.End,
                            onDismissRequest = { showMenu = false }
                        ) {
                            ListPopupColumn {
                                DropdownImpl(
                                    text = stringResource(R.string.jump_to_third_party_app),
                                    optionSize = 1,
                                    isSelected = false,
                                    index = 0,
                                    onSelectedIndexChange = {
                                        showMenu = false
                                        onJump()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }) { padding ->
        when (state) {
            is ExpressDetailsUiState.Content -> {
                val density = LocalDensity.current
                val statusTextSize = dimensionResource(R.dimen.pa_sp_16)

                val summaryTextSize = dimensionResource(R.dimen.pa_sp_13)

                val summaryTextStyle = TextStyle(
                    fontSize = with(density) { summaryTextSize.toSp() },
                    platformStyle = PlatformTextStyle(includeFontPadding = true)
                )

                Column(
                    modifier = Modifier.fillMaxSize().padding(padding)
                        .padding(start = 28.dp, top = 8.dp, end = 28.dp)
                ) {
                    Text(
                        text = state.details.status,
                        color = colorResource(R.color.receivedTextColor),
                        style = TextStyle(
                            fontSize = with(density) { statusTextSize.toSp() },
                            fontWeight = FontWeight.Bold,
                            platformStyle = PlatformTextStyle(includeFontPadding = true)
                        )
                    )

                    SelectionContainer {
                        Text(
                            stringResource(R.string.express_details_mail_number, summary.mailNumber),
                            color = colorResource(R.color.pa_express_detail_express_number_color),
                            style = summaryTextStyle
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = colorResource(R.color.pa_express_detail_split_line_color)
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        stringResource(R.string.data_provider_tips, state.details.dataSource),
                        color = colorResource(R.color.pa_express_detail_provider_color),
                        style = summaryTextStyle
                    )

                    Spacer(Modifier.height(20.dp))

                    CompositionLocalProvider(LocalOverscrollFactory provides rememberPlatformOverscrollFactory()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(bottom = 24.dp).nestedScroll(scrollConnection),
                            state = timelineState
                        ) {
                            items(state.details.traces.size) { index ->
                                TraceRow(
                                    trace = state.details.traces[index],
                                    index = index,
                                    count = state.details.traces.size,
                                    onPhoneNumberClick = { phoneNumber ->
                                        phoneToCall = phoneNumber
                                        showCallDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            ExpressDetailsUiState.Loading -> {
                val transition = rememberInfiniteTransition(label = "expressLoading")

                val rotation by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1440f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "expressLoadingRotation"
                )

                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.miuix_sbl_loading_progress_circle),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).rotate(rotation)
                                .semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate }
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.express_loading),
                            color = colorResource(R.color.app_express_detail_empty_text_color),
                            style = TextStyle(
                                fontSize = 14.sp,
                                platformStyle = PlatformTextStyle(includeFontPadding = true)
                            )
                        )
                    }
                }
            }

            ExpressDetailsUiState.Empty, is ExpressDetailsUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.app_express_detail_list_empty),
                            contentDescription = null
                        )

                        Spacer(Modifier.height(17.dp))

                        Text(
                            text = stringResource(R.string.app_express_detail_list_empty_text),
                            textAlign = TextAlign.Center,
                            color = colorResource(R.color.app_express_detail_empty_text_color),
                            style = TextStyle(
                                fontSize = 14.sp,
                                platformStyle = PlatformTextStyle(includeFontPadding = true)
                            )
                        )
                    }
                }
            }
        }

        OverlayDialog(
            show = showCallDialog,
            title = phoneToCall,
            onDismissRequest = { showCallDialog = false }
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    text = stringResource(R.string.cancel),
                    modifier = Modifier.weight(1f),
                    textStyle = MiuixTheme.textStyles.main.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    onClick = { showCallDialog = false }
                )

                TextButton(
                    text = stringResource(R.string.call),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    textStyle = MiuixTheme.textStyles.main.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    onClick = {
                        if (showCallDialog) {
                            showCallDialog = false
                            onDial(phoneToCall)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TraceRow(trace: ExpressTrace, index: Int, count: Int, onPhoneNumberClick: (String) -> Unit) {
    val textColor = colorResource(
        if (index == 0) R.color.pa_express_progress_item_first_text else R.color.pa_express_progress_item_text
    )

    val traceTextSize = dimensionResource(R.dimen.pa_express_progress_item_text)

    val textStyle = TextStyle(
        // The XML timeline used 13dp text, independently of the system font scale.
        fontSize = with(LocalDensity.current) { traceTextSize.toSp() },
        platformStyle = PlatformTextStyle(includeFontPadding = true)
    )

    val dateTimeWidth = dimensionResource(R.dimen.pa_express_detail_list_item_datetime_width)

    val lineColor = colorResource(R.color.timelineLineColor)

    Row(
        // TimelineView's onMeasure ignored android:minHeight; the XML row was
        // sized by its text and 12dp vertical margins, not a 100dp minimum.
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.express_trace_date_time, trace.date, trace.time),
            modifier = Modifier.width(dateTimeWidth),
            textAlign = TextAlign.End,
            maxLines = 2,
            style = textStyle,
            color = textColor
        )

        // The Compose library derives the line gap from its width, so keep the
        // 10dp marker width inside the XML's 32dp timeline column.
        Box(Modifier.width(32.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Timeline(
                modifier = Modifier.width(10.dp).fillMaxHeight(),
                lineType = getLineType(index, count),
                lineStyle = LineStyle.solid(color = lineColor, width = 4.dp),
                marker = {
                    Image(
                        painterResource(if (index == 0) R.drawable.dot_current else R.drawable.dot_previous),
                        contentDescription = null,
                        modifier = Modifier.size(10.dp)
                    )
                }
            )
        }

        val linkColor = colorResource(R.color.miuiUrlSpanColor)

        val description = remember(trace.description, linkColor, onPhoneNumberClick) {
            buildTraceDescription(trace.description, linkColor, onPhoneNumberClick)
        }

        Text(
            text = description,
            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
            color = textColor,
            style = textStyle
        )
    }
}

@Preview
@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExpressDetailsPreview() {
    BetterMiuiExpressTheme {
        ExpressDetailsScreen(
            MiuiExpress("shunfeng", "顺丰速运", "SF1234567890", null),
            ExpressDetailsUiState.Content(ExpressDetails("快递 100", "派送中", listOf(
                ExpressTrace("2026-10-13 10:00:00", "10-13", "10:00", "快件正在派送，联系快递员 13800138000"),
                ExpressTrace("2026-10-12 10:00:00", "10-12", "10:00", "快件已到达转运中心")
            ))), true, {}, {}, {}, {}
        )
    }
}
