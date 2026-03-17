package com.cunoc.compiforms.data

// Basado en el PDF del proyecto: number (enteros y decimales), string y special
enum class VariableType { NUMBER, STRING, SPECIAL }

class Variable<T>(
    var value: T,
    val type: VariableType,
    val name: String
) {

    fun evaluateType(nuevoValor: Any?): Boolean {
        return when (type) {
            VariableType.NUMBER -> nuevoValor is Number
            VariableType.STRING -> nuevoValor is String
            VariableType.SPECIAL -> true
        }
    }
}
