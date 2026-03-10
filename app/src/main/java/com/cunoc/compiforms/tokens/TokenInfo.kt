package com.cunoc.compiforms.tokens

data class TokenInfo(
    val type: TokenType,
    val lexeme: String,
    val start: Int,
    val end: Int,
    val line: Int,
    val column: Int

)
