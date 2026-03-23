package com.cunoc.compiforms.ui.renderers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cunoc.compiforms.form.model.elements.TextElement
import com.cunoc.compiforms.form.model.styles.StyleSet
import com.cunoc.compiforms.ui.borderStyle
import com.cunoc.compiforms.ui.processEmojis
import com.cunoc.compiforms.ui.toComposeColor
import com.cunoc.compiforms.ui.toComposeFontFamily

@Composable
fun TextRenderer(
    element: TextElement,
    inheritedWidth: Double? = null,
    isParentHorizontal: Boolean = false,
    parentStyle: StyleSet? = null
) {
    val effectiveStyle = element.styles?.mergeWith(parentStyle) ?: parentStyle

    val modifier = Modifier
        .offset(
            x = (element.layout.pointX ?: 0.0).dp,
            y = (element.layout.pointY ?: 0.0).dp
        )
        .then(
            if (element.layout.width != null) Modifier.width(element.layout.width.dp)
            else if (inheritedWidth != null && !isParentHorizontal) Modifier.fillMaxWidth()
            else Modifier.wrapContentWidth()
        )
        .then(
            if (element.layout.height != null) Modifier.height(element.layout.height.dp)
            else Modifier.wrapContentHeight()
        )
        .borderStyle(effectiveStyle?.border)
        .then(
            if (effectiveStyle?.backgroundColor != null)
                Modifier.background(effectiveStyle.backgroundColor.toComposeColor())
            else Modifier
        )
        .padding(horizontal = 4.dp, vertical = 2.dp)

    Text(
        text = element.content.processEmojis(),
        modifier = modifier,
        fontSize = (effectiveStyle?.textSize?.toFloat() ?: 16f).sp,
        color = effectiveStyle?.textColor?.toComposeColor() ?: LocalContentColor.current,
        fontFamily = effectiveStyle?.fontFamily?.toComposeFontFamily()
    )
}
