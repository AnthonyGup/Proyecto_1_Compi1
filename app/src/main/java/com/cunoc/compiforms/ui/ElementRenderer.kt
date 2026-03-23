package com.cunoc.compiforms.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cunoc.compiforms.form.model.elements.*
import com.cunoc.compiforms.form.model.questions.QuestionElement
import com.cunoc.compiforms.form.model.styles.*

@Composable
fun ElementRenderer(
    element: FormElement,
    inheritedWidth: Double? = null,
    inheritedHeight: Double? = null,
    inheritedStyle: StyleSet? = null,
    isParentHorizontal: Boolean = false
) {
    when (element) {
        is SectionElement -> {
            val currentSectionWidth = element.layout.width ?: inheritedWidth ?: 350.0
            val childrenSpaceWidth = maxOf(0.0, currentSectionWidth - 24.0)
            val currentColorStyle = element.styles?.textColor.toComposeColor() ?: inheritedStyle?.textColor.toComposeColor()
            val currentBackgroundColorStyle = element.styles?.backgroundColor.toComposeColor() ?: inheritedStyle?.backgroundColor.toComposeColor()
            val currentFontFamilyStyle = element.styles?.fontFamily ?: inheritedStyle?.fontFamily
            val currentTextSizeStyle = element.styles?.textSize ?: inheritedStyle?.textSize
            val currentBorderStyle = element.styles?.border ?: inheritedStyle?.border
            // Altura: si no hay fija ni heredada, será wrapContent
            val currentSectionHeight = element.layout.height ?: inheritedHeight
            val childrenSpaceHeight = if (currentSectionHeight != null) maxOf(0.0, currentSectionHeight - 24.0) else null

            val widthModifier = if (element.layout.width != null) {
                Modifier.width(element.layout.width.dp)
            } else if (inheritedWidth != null && !isParentHorizontal) {
                Modifier.fillMaxWidth() // Llena el ancho de la sección inmediata respetando su padding
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
                    .borderStyle(currentBorderStyle),
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
                            ElementRenderer(subElement, childrenSpaceWidth, childrenSpaceHeight, isParentHorizontal = true)
                        }
                    }
                } else {
                    Column(
                        modifier = containerModifier,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        element.elements.forEach { subElement ->
                            ElementRenderer(subElement, childrenSpaceWidth, childrenSpaceHeight, isParentHorizontal = false)
                        }
                    }
                }
            }
        }
        is TextElement -> {
            TextRenderer(element, inheritedWidth, isParentHorizontal)
        }
        is TableElement -> {

        }
        is QuestionElement -> {
            QuestionRenderer(element)
        }
    }
}
