package com.cunoc.compiforms.form.model.elements

import com.cunoc.compiforms.form.model.styles.StyleSet
import java.util.ArrayList

data class TableElement(
    val layout: LayoutProps,
    val rows: MutableList<TableRow> = ArrayList(),
    val styles: StyleSet? = null
) : FormElement

data class TableRow(
    val cells: MutableList<TableCell> = ArrayList()
)

data class TableCell(
    val element: FormElement
)
