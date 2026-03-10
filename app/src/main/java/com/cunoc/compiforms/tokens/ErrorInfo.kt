package com.cunoc.compiforms.tokens

data class ErrorInfo(
    val type: ErrorType,
    val lexeme: String,
    val line: Int,
    val column: Int,
    val description: String
)

enum class ErrorType {
    LEXICAL,
    SYNTACTIC
}
