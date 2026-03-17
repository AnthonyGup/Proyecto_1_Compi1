package com.cunoc.compiforms.form.model.questions

import com.cunoc.compiforms.form.model.elements.FormElement
import com.cunoc.compiforms.form.model.elements.LayoutProps
import com.cunoc.compiforms.form.model.styles.StyleSet

sealed interface QuestionElement : FormElement {
    val layout: LayoutProps
    val styles: StyleSet?
}
