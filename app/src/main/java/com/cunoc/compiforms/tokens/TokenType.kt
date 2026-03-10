package com.cunoc.compiforms.tokens

enum class TokenType {
    KEYWORD,        // SECTION, TABLE, IF, FOR, WHILE, OPEN_QUESTION...
    VARIABLE,       // identificadores / nombres de variables
    STRING,         // "texto"
    NUMBER,         // 123, 4.5
    OPERATOR,       // + - * / ^ % == != && ||
    SYMBOL,         // { } [ ] ( ) , : .
    COMMENT,        // $ comentario o /* */
    OTHER,          // otros...
    EMOJI,          // @[:emoji:]
    ERROR           // token inválido
}