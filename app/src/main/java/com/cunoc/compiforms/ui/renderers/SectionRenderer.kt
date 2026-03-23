package com.cunoc.compiforms.ui.renderers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cunoc.compiforms.form.model.elements.Orientacion
import com.cunoc.compiforms.form.model.elements.SectionElement
import com.cunoc.compiforms.form.model.styles.StyleSet
import com.cunoc.compiforms.ui.borderStyle
import com.cunoc.compiforms.ui.toComposeColor

@Composable
fun SectionRenderer(
    element: SectionElement,
    inheritedWidth: Double? = null,
    inheritedHeight: Double? = null,
    isParentHorizontal: Boolean = false,
    parentStyle: StyleSet? = null
) {
    val effectiveStyle = element.styles?.mergeWith(parentStyle) ?: parentStyle
    val currentSectionWidth = element.layout.width ?: inheritedWidth ?: 350.0
    val childrenSpaceWidth = maxOf(0.0, currentSectionWidth - 24.0)
    val currentSectionHeight = element.layout.height ?: inheritedHeight
    val childrenSpaceHeight = if (currentSectionHeight != null) maxOf(0.0, currentSectionHeight - 24.0) else null

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

    Card(
        modifier = Modifier
            .then(widthModifier)
            .then(heightModifier)
            .borderStyle(effectiveStyle?.border),
        colors = CardDefaults.cardColors(
            containerColor = effectiveStyle?.backgroundColor?.toComposeColor() ?: CardDefaults.cardColors().containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val containerModifier = Modifier.padding(12.dp).fillMaxWidth()
        if (element.orientacion == Orientacion.HORIZONTAL) {
            Row(
                modifier = containerModifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                element.elements.forEach { subElement ->
                    ElementRenderer(subElement, childrenSpaceWidth, childrenSpaceHeight, isParentHorizontal = true, parentStyle = effectiveStyle)
                }
            }
        } else {
            Column(
                modifier = containerModifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                element.elements.forEach { subElement ->
                    ElementRenderer(subElement, childrenSpaceWidth, childrenSpaceHeight, isParentHorizontal = false, parentStyle = effectiveStyle)
                }
            }
        }
    }
}
