package com.cunoc.compiforms.form.model.questions

import com.cunoc.compiforms.form.model.elements.LayoutProps
import com.cunoc.compiforms.form.model.styles.StyleSet
import java.util.ArrayList

data class MultipleQuestionElement(
    override val layout: LayoutProps = LayoutProps(),
    val options: List<String>,
    val correctIndexes: List<Int> = ArrayList(),
    override val styles: StyleSet? = null
) : QuestionElement
