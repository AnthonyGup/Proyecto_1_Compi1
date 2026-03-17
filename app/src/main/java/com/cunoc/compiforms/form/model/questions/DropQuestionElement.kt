package com.cunoc.compiforms.form.model.questions

import com.cunoc.compiforms.form.model.elements.LayoutProps
import com.cunoc.compiforms.form.model.styles.StyleSet

data class DropQuestionElement(
    override val layout: LayoutProps = LayoutProps(),
    val label: String,
    val optionsSource: OptionsSource,
    val correctIndex: Int = -1,
    override val styles: StyleSet? = null
) : QuestionElement
