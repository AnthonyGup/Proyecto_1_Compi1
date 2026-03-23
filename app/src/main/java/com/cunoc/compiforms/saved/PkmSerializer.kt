package com.cunoc.compiforms.saved

import android.os.Build
import androidx.annotation.RequiresApi
import com.cunoc.compiforms.form.model.FormDocument
import com.cunoc.compiforms.form.model.elements.*
import com.cunoc.compiforms.form.model.questions.*
import com.cunoc.compiforms.form.model.styles.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PkmSerializer {
    companion object {
        private const val PKM_TEXT_PREFIX = "__PKM_TEXT__::"
    }

    private fun hasRenderableBorder(border: BorderStyle?): Boolean {
        return border?.size != null
    }

    private fun hasRenderableStyles(styles: StyleSet?): Boolean {
        return styles != null && (
            styles.textColor != null ||
            styles.backgroundColor != null ||
            styles.fontFamily != null ||
            styles.textSize != null ||
            hasRenderableBorder(styles.border)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun serialize(document: FormDocument, author: String, description: String): String {
        val sb = StringBuilder()

        // 1. Metadatos
        sb.append(generarMetaData(document, author, description))
        sb.append("\n")

        // 2. Elementos
        document.elements.forEach { element ->
            sb.append(serializeElement(element))
            sb.append("\n")
        }

        return sb.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generarMetaData(document: FormDocument, author: String, description: String): String {
        val ahora = LocalDateTime.now()
        val fecha = ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val hora = ahora.format(DateTimeFormatter.ofPattern("HH:mm"))

        var secciones = 0
        var abiertas = 0
        var desplegables = 0
        var seleccionUnica = 0
        var multiples = 0

        fun contar(elementos: List<FormElement>) {
            elementos.forEach { el ->
                when (el) {
                    is SectionElement -> {
                        secciones++
                        contar(el.elements)
                    }
                    is OpenQuestionElement -> abiertas++
                    is DropQuestionElement -> desplegables++
                    is SelectQuestionElement -> seleccionUnica++
                    is MultipleQuestionElement -> multiples++
                }
            }
        }

        contar(document.elements)
        val totalPreguntas = abiertas + desplegables + seleccionUnica + multiples

        return """
        ###
        Author: "$author"
        Fecha: "$fecha"
        Hora: "$hora"
        Description: "$description"
        Total de Secciones: $secciones
        Total de Preguntas: $totalPreguntas
            Abiertas: $abiertas
            Desplegables: $desplegables
            Selección: $seleccionUnica
            Múltiples: $multiples
        ###
    """.trimIndent()
    }

    private fun serializeElement(element: FormElement): String {
        return when (element) {
            is SectionElement -> serializeSection(element)
            is TableElement -> serializeTable(element)
            is TextElement -> serializeText(element)
            is OpenQuestionElement -> serializeOpenQuestion(element)
            is DropQuestionElement -> serializeDropQuestion(element)
            is SelectQuestionElement -> serializeSelectQuestion(element)
            is MultipleQuestionElement -> serializeMultipleQuestion(element)
            else -> ""
        }
    }

    private fun serializeSection(section: SectionElement): String {
        val l = section.layout
        val header = "<section=${l.width?.toInt() ?: 0},${l.height?.toInt() ?: 0},${l.pointX?.toInt() ?: 0},${l.pointY?.toInt() ?: 0},${section.orientacion}>"
        
        val sb = StringBuilder()
        sb.append(header).append("\n")
        if (hasRenderableStyles(section.styles)) {
            sb.append(serializeStyles(section.styles!!)).append("\n")
        }
        sb.append("<content>\n")
        section.elements.forEach { sb.append(serializeElement(it)).append("\n") }
        sb.append("</content>\n")
        sb.append("</section>")
        return sb.toString()
    }

    private fun serializeTable(table: TableElement): String {
        val sb = StringBuilder()
        sb.append("<table>\n")
        if (hasRenderableStyles(table.styles)) {
            sb.append(serializeStyles(table.styles!!)).append("\n")
        }
        sb.append("<content>\n")
        table.rows.forEach { row ->
            sb.append("<line>\n")
            row.cells.forEach { cell ->
                sb.append("<element>\n").append(serializeElement(cell.element)).append("\n</element>\n")
            }
            sb.append("</line>\n")
        }
        sb.append("</content>\n")
        sb.append("</table>")
        return sb.toString()
    }

    private fun serializeText(text: TextElement): String {
        val l = text.layout
        val tag = "open"
        val content = "\"$PKM_TEXT_PREFIX${text.content}\""
        val base = "<$tag=${l.width?.toInt() ?: 0},${l.height?.toInt() ?: 0},$content"
        
        return if (!hasRenderableStyles(text.styles)) {
            "$base/>"
        } else {
            "$base>\n${serializeStyles(text.styles!!)}\n</$tag>"
        }
    }

    private fun serializeOpenQuestion(q: OpenQuestionElement): String {
        val l = q.layout
        val label = "\"${q.label}\""
        val base = "<open=${l.width?.toInt() ?: 0},${l.height?.toInt() ?: 0},$label"
        
        return if (!hasRenderableStyles(q.styles)) {
            "$base/>"
        } else {
            "$base>\n${serializeStyles(q.styles!!)}\n</open>"
        }
    }

    private fun serializeDropQuestion(q: DropQuestionElement): String {
        val l = q.layout
        val optionsStr = when (val src = q.optionsSource) {
            is OptionsSource.Static -> "{${src.options.joinToString(",") { "\"$it\"" }}}"
            is OptionsSource.PokemonRange -> "WHO_IS_THAT_POKEMON(${src.selector},${src.start},${src.end})" 
        }

        val base = "<drop=${l.width?.toInt() ?: 0},${l.height?.toInt() ?: 0},\"${q.label}\",$optionsStr,${q.correctIndex}"
        
        return if (!hasRenderableStyles(q.styles)) {
            "$base/>"
        } else {
            "$base>\n${serializeStyles(q.styles!!)}\n</drop>"
        }
    }

    private fun serializeSelectQuestion(q: SelectQuestionElement): String {
        val l = q.layout
        val options = q.options.joinToString(",") { "\"$it\"" }
        val correct = q.correctIndex ?: -1
        val base = "<select=${l.width?.toInt() ?: 0},${l.height?.toInt() ?: 0},\"Selecciona\",{$options},$correct"
        
        return if (!hasRenderableStyles(q.styles)) {
            "$base/>"
        } else {
            "$base>\n${serializeStyles(q.styles!!)}\n</select>"
        }
    }

    private fun serializeMultipleQuestion(q: MultipleQuestionElement): String {
        val l = q.layout
        val options = q.options.joinToString(",") { "\"$it\"" }
        val corrects = q.correctIndexes.joinToString(",")
        val base = "<multiple=${l.width?.toInt() ?: 0},${l.height?.toInt() ?: 0},\"Selecciona\",{$options},{$corrects}"
        
        return if (!hasRenderableStyles(q.styles)) {
            "$base/>"
        } else {
            "$base>\n${serializeStyles(q.styles!!)}\n</multiple>"
        }
    }

    private fun serializeStyles(styles: StyleSet): String {
        val sb = StringBuilder()
        sb.append("<style>\n")
        styles.textColor?.let { sb.append("  <color=${serializeColor(it)}/>\n") }
        styles.backgroundColor?.let { sb.append("  <background color=${serializeColor(it)}/>\n") }
        styles.fontFamily?.let { sb.append("  <font family=$it/>\n") }
        styles.textSize?.let { sb.append("  <text size=${it.toInt()}/>\n") }
        styles.border?.takeIf { hasRenderableBorder(it) }?.let { b ->
            sb.append("  <border,${b.size!!.toInt()},${b.type},${serializeColor(b.color)}/>\n")
        }
        sb.append("</style>")
        return sb.toString()
    }

    private fun serializeColor(color: ColorValue): String {
        return when (color) {
            is ColorValue.Hex -> color.value
            is ColorValue.Named -> color.value.name
            is ColorValue.Rgb -> "(${color.r},${color.g},${color.b})"
            is ColorValue.Hsl -> "<${color.h},${color.s},${color.l}>"
        }
    }
}
