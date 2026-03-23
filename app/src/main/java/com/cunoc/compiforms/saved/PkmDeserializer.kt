package com.cunoc.compiforms.saved

import com.cunoc.compiforms.form.model.FormDocument
import com.cunoc.compiforms.pkm.PKMLexer
import com.cunoc.compiforms.pkm.PKMParser
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.StringReader

class PkmDeserializer {

    data class DeserializeResult(
        val document: FormDocument?,
        val errorMessage: String?
    )

    /**
     * Convierte el texto de un archivo .pkm en un objeto FormDocument funcional
     * @param pkmContent El contenido del archivo .pkm a deserializar
     * @return El FormDocument resultante, o null si ocurrió un error durante el parsereo
     */
    fun deserialize(pkmContent: String): FormDocument? {
        return deserializeWithDiagnostics(pkmContent).document
    }

    fun deserializeWithDiagnostics(pkmContent: String): DeserializeResult {
        return try {
            val normalizedContent = pkmContent.removePrefix("\uFEFF")

            val reader = StringReader(normalizedContent)
            val lexer = PKMLexer(reader)
            val parser = PKMParser(lexer)
            val originalErr = System.err
            val errBuffer = ByteArrayOutputStream()
            val captureErr = PrintStream(errBuffer, true, Charsets.UTF_8.name())

            val result = try {
                System.setErr(captureErr)
                parser.parse()
            } finally {
                System.setErr(originalErr)
                captureErr.close()
            }

            val parserErrorText = errBuffer
                .toString(Charsets.UTF_8.name())
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .lastOrNull()

            if (parser.hasSyntaxErrors()) {
                DeserializeResult(
                    document = null,
                    errorMessage = parserErrorText ?: "Error sintactico en archivo .pkm"
                )
            } else {
                DeserializeResult(
                    document = result.value as? FormDocument,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            DeserializeResult(
                document = null,
                errorMessage = "Error al parsear .pkm: ${e.message ?: "desconocido"}"
            )
        }
    }
}
