package com.cunoc.compiforms.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cunoc.compiforms.form.model.elements.TextElement

@Composable
fun TextRenderer(
    element: TextElement,
    inheritedWidth: Double? = null,
    isParentHorizontal: Boolean = false
) {
    val widthModifier = if (element.layout.width != null) {
        Modifier.width(element.layout.width.dp)
    } else if (inheritedWidth != null && !isParentHorizontal) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.wrapContentWidth()
    }

    val heightModifier = if (element.layout.height != null) {
        Modifier.height(element.layout.height.dp)
    } else {
        Modifier.wrapContentHeight()
    }

    Text(
        text = element.content.processEmojis(),
        modifier = Modifier
            .offset(x = (element.layout.pointX ?: 0.0).dp, y = (element.layout.pointY ?: 0.0).dp)
            .then(widthModifier)
            .then(heightModifier)
            .borderStyle(element.styles?.border)
            .background(element.styles?.backgroundColor.toComposeColor()),
        fontSize = (element.styles?.textSize?.toFloat() ?: 16f).sp,
        color = element.styles?.textColor.toComposeColor(),
        fontFamily = element.styles?.fontFamily.toComposeFontFamily()
    )
}
