package com.cunoc.compiforms.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.cunoc.compiforms.tokens.TokenInfo
import com.cunoc.compiforms.tokens.TokenType

class CodeVisualTransformation(private val tokens: List<TokenInfo>) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = buildAnnotatedString {
            append(text.text)
            
            tokens.forEach { token ->
                val color = when (token.type) {
                    TokenType.KEYWORD -> Color(0xFFBB86FC) // Morado
                    TokenType.OPERATOR -> Color(0xFF4CAF50) // Verde
                    TokenType.STRING -> Color(0xFFFFB74D) // Naranja
                    TokenType.NUMBER -> Color(0xFF81D4FA) // Celeste
                    TokenType.SYMBOL -> Color(0xFF64B5F6) // Azul
                    TokenType.EMOJI -> Color(0xFFFFF176) // Amarillo
                    TokenType.COMMENT -> Color(0xFF888888) // Gris
                    TokenType.VARIABLE -> Color.White // Blanco
                    TokenType.ERROR -> Color(0xFFF44336) // Rojo
                    else -> Color.White
                }
                val start = token.start.coerceIn(0, text.length)
                val end = token.end.coerceIn(0, text.length)
                
                if (start < end) {
                    addStyle(
                        style = SpanStyle(color = color),
                        start = start,
                        end = end
                    )
                }
            }
        }
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }
}
