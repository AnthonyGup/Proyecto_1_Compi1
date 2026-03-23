package com.cunoc.compiforms.saved

import com.cunoc.compiforms.form.model.FormDocument
import com.cunoc.compiforms.form.model.elements.*
import com.cunoc.compiforms.form.model.questions.*
import com.cunoc.compiforms.form.model.styles.*

class FormSerializer {

    fun serialize(document: FormDocument): String {
        val sb = StringBuilder()
        document.elements.forEach { element ->
            sb.append(serializeElement(element, 0))
            sb.append("\n\n")
        }
        return sb.toString().trim()
    }

    private fun serializeElement(element: FormElement, indent: Int): String {
        val padding = "    ".repeat(indent)
        return when (element) {
            is SectionElement -> serializeSection(element, indent)
            is TableElement -> serializeTable(element, indent)
            is TextElement -> serializeText(element, indent)
            is OpenQuestionElement -> serializeOpenQuestion(element, indent)
            is DropQuestionElement -> serializeDropQuestion(element, indent)
            is SelectQuestionElement -> serializeSelectQuestion(element, indent)
            is MultipleQuestionElement -> serializeMultipleQuestion(element, indent)
            else -> ""
        }
    }

    private fun serializeSection(section: SectionElement, indent: Int): String {
        val padding = "    ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${padding}SECTION [\n")
        
        val attrs = mutableListOf<String>()
        section.layout.width?.let { attrs.add("width: ${it.toInt()}") }
        section.layout.height?.let { attrs.add("height: ${it.toInt()}") }
        section.layout.pointX?.let { attrs.add("pointX: ${it.toInt()}") }
        section.layout.pointY?.let { attrs.add("pointY: ${it.toInt()}") }
        attrs.add("orientation: ${section.orientacion}")
        
        sb.append(attrs.joinToString(",\n") { "    ${padding}$it" })
        
        if (section.elements.isNotEmpty()) {
            sb.append(",\n${padding}    elements: {\n")
            sb.append(section.elements.joinToString(",\n") { serializeElement(it, indent + 2) })
            sb.append("\n${padding}    }")
        }
        
        section.styles?.let {
            sb.append(",\n")
            sb.append(serializeStyles(it, indent + 1))
        }
        
        sb.append("\n${padding}]")
        return sb.toString()
    }

    private fun serializeTable(table: TableElement, indent: Int): String {
        val padding = "    ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${padding}TABLE [\n")
        
        val attrs = mutableListOf<String>()
        table.layout.width?.let { attrs.add("width: ${it.toInt()}") }
        table.layout.height?.let { attrs.add("height: ${it.toInt()}") }
        table.layout.pointX?.let { attrs.add("pointX: ${it.toInt()}") }
        table.layout.pointY?.let { attrs.add("pointY: ${it.toInt()}") }
        
        sb.append(attrs.joinToString(",\n") { "    ${padding}$it" })
        
        if (table.rows.isNotEmpty()) {
            sb.append(",\n${padding}    elements: {\n")
            val rowsStr = table.rows.joinToString(",\n") { row ->
                val rowPadding = "    ".repeat(indent + 2)
                "${rowPadding}[\n" + row.cells.joinToString(",\n") { cell ->
                    val cellPadding = "    ".repeat(indent + 3)
                    "${cellPadding}{\n" + serializeElement(cell.element, indent + 4) + "\n${cellPadding}}"
                } + "\n${rowPadding}]"
            }
            sb.append(rowsStr)
            sb.append("\n${padding}    }")
        }
        
        table.styles?.let {
            sb.append(",\n")
            sb.append(serializeStyles(it, indent + 1))
        }
        
        sb.append("\n${padding}]")
        return sb.toString()
    }

    private fun serializeText(text: TextElement, indent: Int): String {
        val padding = "    ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${padding}TEXT [\n")
        val attrs = mutableListOf<String>()
        text.layout.width?.let { attrs.add("width: ${it.toInt()}") }
        text.layout.height?.let { attrs.add("height: ${it.toInt()}") }
        attrs.add("content: \"${text.content}\"")
        sb.append(attrs.joinToString(",\n") { "    ${padding}$it" })
        text.styles?.let {
            sb.append(",\n")
            sb.append(serializeStyles(it, indent + 1))
        }
        sb.append("\n${padding}]")
        return sb.toString()
    }

    private fun serializeOpenQuestion(q: OpenQuestionElement, indent: Int): String {
        val padding = "    ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${padding}OPEN_QUESTION [\n")
        val attrs = mutableListOf<String>()
        q.layout.width?.let { attrs.add("width: ${it.toInt()}") }
        q.layout.height?.let { attrs.add("height: ${it.toInt()}") }
        attrs.add("label: \"${q.label}\"")
        sb.append(attrs.joinToString(",\n") { "    ${padding}$it" })
        q.styles?.let {
            sb.append(",\n")
            sb.append(serializeStyles(it, indent + 1))
        }
        sb.append("\n${padding}]")
        return sb.toString()
    }

    private fun serializeDropQuestion(q: DropQuestionElement, indent: Int): String {
        val padding = "    ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${padding}DROP_QUESTION [\n")
        val attrs = mutableListOf<String>()
        q.layout.width?.let { attrs.add("width: ${it.toInt()}") }
        q.layout.height?.let { attrs.add("height: ${it.toInt()}") }
        attrs.add("label: \"${q.label}\"")
        
        val optionsStr = when (val src = q.optionsSource) {
            is OptionsSource.Static -> "options: {" + src.options.joinToString(", ") { "\"$it\"" } + "}"
            is OptionsSource.PokemonRange -> "options: who_is_that_pokemon(number, ${src.start}, ${src.end})"
        }
        attrs.add(optionsStr)
        if (q.correctIndex != -1) attrs.add("correct: ${q.correctIndex}")
        
        sb.append(attrs.joinToString(",\n") { "    ${padding}$it" })
        q.styles?.let {
            sb.append(",\n")
            sb.append(serializeStyles(it, indent + 1))
        }
        sb.append("\n${padding}]")
        return sb.toString()
    }

    private fun serializeSelectQuestion(q: SelectQuestionElement, indent: Int): String {
        val padding = "    ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${padding}SELECT_QUESTION [\n")
        val attrs = mutableListOf<String>()
        q.layout.width?.let { attrs.add("width: ${it.toInt()}") }
        q.layout.height?.let { attrs.add("height: ${it.toInt()}") }
        attrs.add("options: {" + q.options.joinToString(", ") { "\"$it\"" } + "}")
        q.correctIndex?.let { attrs.add("correct: $it") }
        sb.append(attrs.joinToString(",\n") { "    ${padding}$it" })
        q.styles?.let {
            sb.append(",\n")
            sb.append(serializeStyles(it, indent + 1))
        }
        sb.append("\n${padding}]")
        return sb.toString()
    }

    private fun serializeMultipleQuestion(q: MultipleQuestionElement, indent: Int): String {
        val padding = "    ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${padding}MULTIPLE_QUESTION [\n")
        val attrs = mutableListOf<String>()
        q.layout.width?.let { attrs.add("width: ${it.toInt()}") }
        q.layout.height?.let { attrs.add("height: ${it.toInt()}") }
        attrs.add("options: {" + q.options.joinToString(", ") { "\"$it\"" } + "}")
        attrs.add("correct: {" + q.correctIndexes.joinToString(", ") + "}")
        sb.append(attrs.joinToString(",\n") { "    ${padding}$it" })
        q.styles?.let {
            sb.append(",\n")
            sb.append(serializeStyles(it, indent + 1))
        }
        sb.append("\n${padding}]")
        return sb.toString()
    }

    private fun serializeStyles(styles: StyleSet, indent: Int): String {
        val padding = "    ".repeat(indent)
        val sb = StringBuilder()
        sb.append("${padding}styles [\n")
        val s = mutableListOf<String>()
        styles.textColor?.let { s.add("\"color\": ${serializeColor(it)}") }
        styles.backgroundColor?.let { s.add("\"background color\": ${serializeColor(it)}") }
        styles.fontFamily?.let { s.add("\"font family\": $it") }
        styles.textSize?.let { s.add("\"text size\": ${it.toInt()}") }
        styles.border?.let { b ->
            s.add("\"border\": (${b.size?.toInt()}, ${b.type}, ${serializeColor(b.color)})")
        }
        sb.append(s.joinToString(",\n") { "    ${padding}$it" })
        sb.append("\n${padding}]")
        return sb.toString()
    }

    private fun serializeColor(color: ColorValue): String {
        return when (color) {
            is ColorValue.Hex -> color.value
            is ColorValue.Named -> color.value.name
            is ColorValue.Rgb -> "(${color.r}, ${color.g}, ${color.b})"
            is ColorValue.Hsl -> "<${color.h}, ${color.s}, ${color.l}>"
        }
    }
}
