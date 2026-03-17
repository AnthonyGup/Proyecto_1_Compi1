package com.cunoc.compiforms.form.model

import com.cunoc.compiforms.data.Variable
import com.cunoc.compiforms.tokens.ErrorInfo
import com.cunoc.compiforms.tokens.TokenInfo
import java.util.ArrayList
import java.util.LinkedHashMap

data class ParseResult(
    val document: FormDocument = FormDocument(),
    val globalVariables: MutableMap<String, Variable<*>> = LinkedHashMap(),
    val tokens: List<TokenInfo> = ArrayList(),
    val lexicalErrors: List<ErrorInfo> = ArrayList(),
    val syntaxErrors: List<String> = ArrayList(),
    val semanticErrors: List<String> = ArrayList(),
    val warnings: List<String> = ArrayList()
) {
    val isSuccess: Boolean
        get() = lexicalErrors.isEmpty() && syntaxErrors.isEmpty() && semanticErrors.isEmpty()
}
