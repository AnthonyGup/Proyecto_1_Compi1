package com.cunoc.compiforms.form.model.questions

sealed interface OptionsSource {
    data class Static(val options: List<String>) : OptionsSource

    data class PokemonRange(
        val selector: String,
        val start: Int,
        val end: Int
    ) : OptionsSource
}
