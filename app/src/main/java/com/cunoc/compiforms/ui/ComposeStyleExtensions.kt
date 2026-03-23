package com.cunoc.compiforms.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily as ComposeFontFamily
import androidx.compose.ui.unit.dp
import com.cunoc.compiforms.form.model.styles.*

/**
 * Procesa la cadena de texto para convertir los tokens de emoji en caracteres reales.
 */
fun String.processEmojis(): String {
    var text = this
    
    // Mapeo simple de palabras clave
    val simpleMappings = mapOf(
        "@[:smile:]" to "😀",
        "@[:sad:]" to "🥲",
        "@[:serious:]" to "😐",
        "@[:heart:]" to "❤",
        "@[:cat:]" to "😺",
        "@[:star:]" to "⭐"
    )
    
    simpleMappings.forEach { (token, emoji) ->
        text = text.replace(token, emoji)
    }

    // Procesar patrones con repeticiones (ej: @[:)))] o @[<<<333])
    text = Regex("""@\[:\)+\]""").replace(text, "😀")
    text = Regex("""@\[:\(+\]""").replace(text, "🥲")
    text = Regex("""@\[:\|+\]""").replace(text, "😐")
    text = Regex("""@\[<+3+\]""").replace(text, "❤")
    text = Regex("""@\[:\^+:\]""").replace(text, "😺")

    // Procesar contador de estrellas (ej: @[:star:3:] o @[:star-5:])
    val starRegex = Regex("""@\[:star[:-](\d+):\]""")
    text = starRegex.replace(text) { matchResult ->
        val count = matchResult.groupValues[1].toIntOrNull() ?: 1
        "⭐".repeat(count)
    }

    return text
}

/**
 * Aplica un estilo de borde basado en el modelo de dominio a un Modifier de Compose.
 */
fun Modifier.borderStyle(style: BorderStyle?): Modifier {
    if (style == null) return this
    
    val width = (style.size ?: 1.0).dp
    val color = style.color.toComposeColor()
    
    return when (style.type) {
        BorderType.LINE -> this.border(width, color)
        BorderType.DOTTED -> this.drawBehind {
            val strokeWidth = width.toPx()
            drawRect(
                color = color,
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
        }
        BorderType.DOUBLE -> this.border(width, color).padding(width / 2).border(width / 2, color)
    }
}

/**
 * Convierte el enum de FontFamily del dominio al FontFamily de Compose.
 */
fun com.cunoc.compiforms.form.model.styles.FontFamily?.toComposeFontFamily(): ComposeFontFamily {
    return when (this) {
        com.cunoc.compiforms.form.model.styles.FontFamily.MONO -> ComposeFontFamily.Monospace
        com.cunoc.compiforms.form.model.styles.FontFamily.SANS_SERIF -> ComposeFontFamily.SansSerif
        com.cunoc.compiforms.form.model.styles.FontFamily.CURSIVE -> ComposeFontFamily.Cursive
        else -> ComposeFontFamily.Default
    }
}

/**
 * Convierte un ColorValue del dominio a un Color de Compose.
 */
fun ColorValue?.toComposeColor(): Color {
    return when (this) {
        is ColorValue.Named -> {
            when (this.value) {
                NamedColor.RED -> Color.Red
                NamedColor.BLUE -> Color.Blue
                NamedColor.GREEN -> Color.Green
                NamedColor.PURPLE -> Color.Magenta
                NamedColor.SKY -> Color(0xFF87CEEB)
                NamedColor.YELLOW -> Color.Yellow
                NamedColor.BLACK -> Color.Black
                NamedColor.WHITE -> Color.White
            }
        }
        is ColorValue.Hex -> {
            try {
                val colorString = if (this.value.startsWith("#")) this.value else "#${this.value}"
                Color(android.graphics.Color.parseColor(colorString))
            } catch (e: Exception) {
                Color.Black
            }
        }
        else -> Color.Black
    }
}
