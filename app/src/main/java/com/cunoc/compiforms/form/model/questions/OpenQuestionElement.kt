package com.cunoc.compiforms.form.model.questions

import com.cunoc.compiforms.form.model.elements.LayoutProps
import com.cunoc.compiforms.form.model.styles.StyleSet

data class OpenQuestionElement(
    override val layout: LayoutProps = LayoutProps(),
    val label: String,
    override val styles: StyleSet? = null
) : QuestionElement
