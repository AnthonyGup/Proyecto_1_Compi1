package com.cunoc.compiforms.ui.renderers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cunoc.compiforms.form.model.elements.TableElement
import com.cunoc.compiforms.form.model.styles.BorderStyle
import com.cunoc.compiforms.form.model.styles.BorderType
import com.cunoc.compiforms.form.model.styles.ColorValue
import com.cunoc.compiforms.form.model.styles.NamedColor
import com.cunoc.compiforms.form.model.styles.StyleSet
import com.cunoc.compiforms.ui.borderStyle
import com.cunoc.compiforms.ui.toComposeColor

@Composable
fun TableRenderer(
    element: TableElement,
    inheritedWidth: Double? = null,
    inheritedHeight: Double? = null,
    parentStyle: StyleSet? = null
) {
    val effectiveStyle = element.styles?.mergeWith(parentStyle) ?: parentStyle
    
    val finalWidth = element.layout.width ?: inheritedWidth ?: 300.0
    val finalHeight = element.layout.height ?: inheritedHeight ?: 200.0
    
    Column(
        modifier = Modifier
            .size(finalWidth.dp, finalHeight.dp)
            .borderStyle(effectiveStyle?.border ?: BorderStyle(1.0, BorderType.LINE, ColorValue.Named(NamedColor.BLACK)))
            .then(
                if (effectiveStyle?.backgroundColor != null) 
                    Modifier.background(effectiveStyle.backgroundColor.toComposeColor()) 
                else Modifier
            )
    ) {
        element.rows.forEach { row ->
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                row.cells.forEach { cell ->
                    Box(modifier = Modifier.weight(1f)) {
                        ElementRenderer(
                            element = cell.element, 
                            inheritedWidth = finalWidth / row.cells.size, 
                            inheritedHeight = finalHeight / element.rows.size, 
                            isParentHorizontal = true,
                            parentStyle = effectiveStyle
                        )
                    }
                }
            }
        }
    }
}
