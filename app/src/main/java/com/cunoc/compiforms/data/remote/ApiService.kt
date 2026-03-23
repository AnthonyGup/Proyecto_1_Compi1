package com.cunoc.compiforms.data.remote

import android.os.Build
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.URLEncoder
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class ApiService {
    companion object {
        @Volatile
        private var customServerHost: String? = null
    }

    private val baseApiUrl: String
        get() = "http://${resolveServerHost()}:8080/ApiCompiForms/api"

    private fun resolveServerHost(): String {
        val userHost = customServerHost?.trim().orEmpty()
        if (userHost.isNotEmpty()) return userHost
        return if (isRunningOnEmulator()) "10.0.2.2" else "192.168.100.147"
    }

    private fun isRunningOnEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
            Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("Emulator", ignoreCase = true) ||
            Build.MANUFACTURER.contains("Genymotion", ignoreCase = true)
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        return stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() } ?: ""
    }

    /**
     * Llama a PokéAPI sin Ktor para conservar who_is_that_pokemon.
     */
    suspend fun getPokemonName(id: Int): String? {
        if (id <= 0) return null
        return withContext(Dispatchers.IO) {
            val url = URL("https://pokeapi.co/api/v2/pokemon/$id")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            try {
                if (connection.responseCode !in 200..299) return@withContext null

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                
                val json = JSONObject(body)
                val name = json.optString("name")

                if (name.isNotEmpty()) {
                    name.replaceFirstChar { it.uppercase() }
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            } finally {
                connection.disconnect()
            }
        }
    }

    fun getCurrentServerHost(): String = resolveServerHost()

    fun updateServerHost(host: String) {
        customServerHost = host.trim().ifBlank { null }
    }

    /**
     * Obtiene una lista de nombres de pokemon en un rango
     */
    suspend fun getPokemonRange(start: Int, end: Int): List<String> = coroutineScope {
        (start..end).map { id ->
            async { getPokemonName(id) }
        }.awaitAll().filterNotNull()
    }

    /**
     * Sube archivo .pkm al servidor Tomcat.
     */
    suspend fun uploadPkmFile(fileName: String, content: String): String {
        return withContext(Dispatchers.IO) {
            val boundary = "----ApiCompiFormsBoundary${System.currentTimeMillis()}"
            val lineEnd = "\r\n"
            val connection = (URL("$baseApiUrl/pkm/upload").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }

            try {
                DataOutputStream(connection.outputStream).use { output ->
                    output.writeBytes("--$boundary$lineEnd")
                    output.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$lineEnd")
                    output.writeBytes("Content-Type: text/plain$lineEnd$lineEnd")
                    output.write(content.toByteArray(StandardCharsets.UTF_8))
                    output.writeBytes(lineEnd)

                    output.writeBytes("--$boundary--$lineEnd")
                    output.flush()
                }

                val body = readResponse(connection)
                if (connection.responseCode in 200..299) {
                    try {
                        JSONObject(body).optString("message", "Archivo guardado")
                    } catch (_: Exception) {
                        "Archivo guardado"
                    }
                } else {
                    "Error ${connection.responseCode}: $body"
                }
            } catch (e: Exception) {
                "Error al conectar con el servidor: ${e.message}"
            } finally {
                connection.disconnect()
            }
        }
    }

    /**
     * Lista archivos .pkm desde el servidor Tomcat.
     */
    suspend fun listPkmFiles(): List<String> {
        return withContext(Dispatchers.IO) {
            val connection = (URL("$baseApiUrl/pkm/files").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
            }

            try {
                if (connection.responseCode !in 200..299) return@withContext emptyList()
                val body = readResponse(connection)
                val array = JSONArray(body)
                buildList {
                    for (i in 0 until array.length()) {
                        val obj = array.optJSONObject(i) ?: continue
                        val name = obj.optString("fileName")
                        if (name.isNotEmpty()) add(name)
                    }
                }
            } catch (_: Exception) {
                emptyList()
            } finally {
                connection.disconnect()
            }
        }
    }

    /**
     * Descarga archivo .pkm desde el servidor Tomcat.
     */
    suspend fun downloadPkmFile(fileName: String): String? {
        return withContext(Dispatchers.IO) {
            val encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20")
            val connection = (URL("$baseApiUrl/pkm/download/$encodedName").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
            }

            try {
                if (connection.responseCode !in 200..299) return@withContext null
                connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                    .removePrefix("\uFEFF")
            } catch (_: Exception) {
                null
            } finally {
                connection.disconnect()
            }
        }
    }

    fun close() {
        // Sin recursos remotos que cerrar.
    }
}
