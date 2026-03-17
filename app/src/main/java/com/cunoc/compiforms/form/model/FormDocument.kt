package com.cunoc.compiforms.form.model

import com.cunoc.compiforms.form.model.elements.FormElement
import java.util.ArrayList

data class FormDocument(
    val elements: MutableList<FormElement> = ArrayList()
)
