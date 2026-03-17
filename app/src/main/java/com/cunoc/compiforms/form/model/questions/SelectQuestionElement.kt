package com.cunoc.compiforms.form.model.questions

import com.cunoc.compiforms.form.model.elements.LayoutProps
import com.cunoc.compiforms.form.model.styles.StyleSet

data class SelectQuestionElement(
    override val layout: LayoutProps = LayoutProps(),
    val options: List<String>,
    val correctIndex: Int? = null,
    override val styles: StyleSet? = null
) : QuestionElement
