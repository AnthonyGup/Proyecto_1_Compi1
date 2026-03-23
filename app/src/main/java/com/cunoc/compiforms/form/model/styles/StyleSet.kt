package com.cunoc.compiforms.form.model.styles

data class StyleSet(
    val textColor: ColorValue? = null,
    val backgroundColor: ColorValue? = null,
    val fontFamily: FontFamily? = null,
    val textSize: Double? = null,
    val border: BorderStyle? = null
) {
    /**
     * Merges this StyleSet with another, using this set's values as priority.
     * If a property is null in this set, it takes the value from the other set.
     */
    fun mergeWith(parent: StyleSet?): StyleSet {
        if (parent == null) return this
        return StyleSet(
            textColor = this.textColor ?: parent.textColor,
            backgroundColor = this.backgroundColor ?: parent.backgroundColor,
            fontFamily = this.fontFamily ?: parent.fontFamily,
            textSize = this.textSize ?: parent.textSize,
            border = this.border ?: parent.border
        )
    }
}
