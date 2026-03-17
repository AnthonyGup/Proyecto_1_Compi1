package com.cunoc.compiforms.form.model.styles

sealed interface ColorValue {
    data class Named(val value: NamedColor) : ColorValue
    data class Hex(val value: String) : ColorValue
    data class Rgb(val r: Int, val g: Int, val b: Int) : ColorValue
    data class Hsl(val h: Int, val s: Int, val l: Int) : ColorValue
}
