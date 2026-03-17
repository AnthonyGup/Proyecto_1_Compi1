package com.cunoc.compiforms.form.model.elements

import com.cunoc.compiforms.form.model.styles.StyleSet
import java.util.ArrayList

data class SectionElement(
    val layout: LayoutProps,
    val orientacion: Orientacion = Orientacion.VERTICAL,
    val elements: MutableList<FormElement> = ArrayList(),
    val styles: StyleSet? = null
) : FormElement
