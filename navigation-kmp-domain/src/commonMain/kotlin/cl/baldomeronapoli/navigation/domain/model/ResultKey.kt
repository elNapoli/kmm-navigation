package cl.baldomeronapoli.navigation.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Identifica un canal de resultado tipado entre el módulo llamado y el módulo que llama.
 * Lo declara el módulo llamado en el módulo de contratos compartido, junto a su contrato:
 * ```kotlin
 * val PinResultKey = ResultKey("pin.result", PinResult.serializer())
 * ```
 *
 * @param name Clave única en el saved state del módulo que llama.
 * @param serializer Serializer del tipo de resultado.
 */
class ResultKey<R : NavigationResult>(
    val name: String,
    private val serializer: KSerializer<R>,
) {
    fun encode(result: R): String = json.encodeToString(serializer, result)

    /**
     * Retorna null si [encoded] no corresponde al tipo actual (ej. saved
     * state restaurado tras actualizar la app con un esquema distinto).
     */
    fun decodeOrNull(encoded: String): R? = runCatching { json.decodeFromString(serializer, encoded) }.getOrNull()

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
