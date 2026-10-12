package com.moefactory.bettermiuiexpress.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink

private val mobileNumberPattern = "(?<!\\d)1[3-9]\\d{9}(?!\\d)".toRegex()

internal fun buildTraceDescription(
    description: String,
    linkColor: Color,
    onDial: (String) -> Unit
): AnnotatedString = buildAnnotatedString {
    var offset = 0
    for (match in mobileNumberPattern.findAll(description)) {
        append(description.substring(offset, match.range.first))
        withLink(LinkAnnotation.Clickable(
            tag = "tel:${match.value}",
            styles = TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
            linkInteractionListener = { onDial(match.value) }
        )) { append(match.value) }
        offset = match.range.last + 1
    }
    append(description.substring(offset))
}
