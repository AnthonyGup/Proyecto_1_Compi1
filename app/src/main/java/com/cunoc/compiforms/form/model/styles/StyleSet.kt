package com.cunoc.compiforms.form.model.styles

data class StyleSet(
    val textColor: ColorValue? = null,
    val backgroundColor: ColorValue? = null,
    val fontFamily: FontFamily? = null,
    val textSize: Double? = null,
    val border: BorderStyle? = null
)
