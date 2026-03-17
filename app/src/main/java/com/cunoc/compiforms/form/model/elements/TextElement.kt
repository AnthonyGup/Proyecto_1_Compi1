package com.cunoc.compiforms.form.model.elements

import com.cunoc.compiforms.form.model.styles.StyleSet

data class TextElement(
    val layout: LayoutProps = LayoutProps(),
    val content: String = "",
    val styles: StyleSet? = null
) : FormElement
