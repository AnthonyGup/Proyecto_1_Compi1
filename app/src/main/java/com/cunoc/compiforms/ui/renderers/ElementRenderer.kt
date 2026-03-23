package com.cunoc.compiforms.ui.renderers

import androidx.compose.runtime.Composable
import com.cunoc.compiforms.form.model.elements.*
import com.cunoc.compiforms.form.model.questions.QuestionElement
import com.cunoc.compiforms.form.model.styles.StyleSet
import com.cunoc.compiforms.ui.QuestionRenderer

@Composable
fun ElementRenderer(
    element: FormElement,
    inheritedWidth: Double? = null,
    inheritedHeight: Double? = null,
    isParentHorizontal: Boolean = false,
    parentStyle: StyleSet? = null
) {
    when (element) {
        is SectionElement -> {
            SectionRenderer(element, inheritedWidth, inheritedHeight, isParentHorizontal, parentStyle)
        }
        is TextElement -> {
            TextRenderer(element, inheritedWidth, isParentHorizontal, parentStyle)
        }
        is TableElement -> {
            TableRenderer(element, inheritedWidth, inheritedHeight, parentStyle)
        }
        is QuestionElement -> {
            QuestionRenderer(element, parentStyle)
        }
    }
}
