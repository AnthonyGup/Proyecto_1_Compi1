package com.cunoc.compiforms.saved

import com.cunoc.compiforms.form.model.FormDocument
import com.cunoc.compiforms.form.model.elements.LayoutProps
import com.cunoc.compiforms.form.model.elements.TextElement
import com.cunoc.compiforms.form.model.questions.DropQuestionElement
import com.cunoc.compiforms.form.model.questions.OpenQuestionElement
import com.cunoc.compiforms.form.model.questions.OptionsSource
import com.cunoc.compiforms.form.model.questions.SelectQuestionElement
import com.cunoc.compiforms.form.model.styles.BorderStyle
import com.cunoc.compiforms.form.model.styles.BorderType
import com.cunoc.compiforms.form.model.styles.ColorValue
import com.cunoc.compiforms.form.model.styles.StyleSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PkmRoundTripTest {

    @Test
    fun roundTrip_preservesTextElementAndNegativeCorrectIndex() {
        val sharedStyle = StyleSet(
            textColor = ColorValue.Hex("#112233"),
            backgroundColor = ColorValue.Rgb(20, 30, 40),
            textSize = 18.0,
            border = BorderStyle(2.0, BorderType.DOTTED, ColorValue.Named(com.cunoc.compiforms.form.model.styles.NamedColor.BLUE))
        )

        val doc = FormDocument(
            mutableListOf(
                TextElement(LayoutProps(40.0, 10.0, 0.0, 0.0), "Texto libre", sharedStyle),
                OpenQuestionElement(LayoutProps(50.0, 12.0, null, null), "Pregunta abierta", sharedStyle),
                DropQuestionElement(
                    LayoutProps(60.0, 12.0, null, null),
                    "Selecciona una opcion",
                    OptionsSource.Static(listOf("A", "B")),
                    -1,
                    sharedStyle
                )
            )
        )

        val pkm = PkmSerializer().serialize(doc, "tester", "roundtrip")
        val reparsed = PkmDeserializer().deserialize(pkm)

        assertNotNull(reparsed)
        val elements = reparsed!!.elements
        assertEquals(3, elements.size)
        assertTrue(elements[0] is TextElement)
        assertTrue(elements[1] is OpenQuestionElement)
        assertTrue(elements[2] is DropQuestionElement)

        val drop = elements[2] as DropQuestionElement
        assertEquals(-1, drop.correctIndex)
        assertTrue(drop.optionsSource is OptionsSource.Static)
    }

    @Test
    fun roundTrip_supportsPokemonRangeOptionsSource() {
        val doc = FormDocument(
            mutableListOf(
                DropQuestionElement(
                    LayoutProps(50.0, 10.0, null, null),
                    "Pokemon",
                    OptionsSource.PokemonRange("NUMBER", 1, 10),
                    -1,
                    null
                )
            )
        )

        val pkm = PkmSerializer().serialize(doc, "tester", "pokemon")
        val reparsed = PkmDeserializer().deserialize(pkm)

        assertNotNull(reparsed)
        val drop = reparsed!!.elements.first() as DropQuestionElement
        assertTrue(drop.optionsSource is OptionsSource.PokemonRange)

        val source = drop.optionsSource as OptionsSource.PokemonRange
        assertEquals("NUMBER", source.selector)
        assertEquals(1, source.start)
        assertEquals(10, source.end)
        assertEquals(-1, drop.correctIndex)
    }

    @Test
    fun roundTrip_preservesNullQuestionDimensions() {
        val doc = FormDocument(
            mutableListOf(
                OpenQuestionElement(
                    LayoutProps(null, null, null, null),
                    "Pregunta sin tamano fijo",
                    null
                ),
                SelectQuestionElement(
                    LayoutProps(null, null, null, null),
                    listOf("A", "B"),
                    0,
                    null
                )
            )
        )

        val pkm = PkmSerializer().serialize(doc, "tester", "null-layout")
        val reparsed = PkmDeserializer().deserialize(pkm)

        assertNotNull(reparsed)
        val open = reparsed!!.elements[0] as OpenQuestionElement
        val select = reparsed.elements[1] as SelectQuestionElement

        assertEquals(null, open.layout.width)
        assertEquals(null, open.layout.height)
        assertEquals(null, select.layout.width)
        assertEquals(null, select.layout.height)
    }
}
