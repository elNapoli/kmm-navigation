package cl.baldomeronapoli.navigation.result

import androidx.lifecycle.SavedStateHandle
import cl.baldomeronapoli.navigation.domain.model.NavigationResult
import cl.baldomeronapoli.navigation.domain.model.ResultKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull

/**
 * Resultados entregados por `NavigateBackWithResult` a esta entrada del back
 * stack. Cada resultado se emite una sola vez: se limpia al leerlo, así que
 * no se re-emite al recrear el ViewModel ni al restaurar el proceso.
 *
 * Usar desde el ViewModel del módulo que llama con su propio [SavedStateHandle]:
 * ```kotlin
 * savedStateHandle.navigationResults(PinResultKey)
 *     .onEach { result -> ... }
 *     .launchIn(viewModelScope)
 * ```
 */
fun <R : NavigationResult> SavedStateHandle.navigationResults(key: ResultKey<R>): Flow<R> =
    getStateFlow<String?>(key.name, null)
        .filterNotNull()
        .mapNotNull { consumeNavigationResult(key) }

/**
 * Lee y limpia el resultado pendiente para [key]. Retorna null si no hay
 * resultado o si no corresponde al esquema actual de [key].
 */
fun <R : NavigationResult> SavedStateHandle.consumeNavigationResult(key: ResultKey<R>): R? {
    val encoded = get<String>(key.name) ?: return null
    // set(null) en vez de remove(): remove() desvincula el StateFlow de
    // getStateFlow y los resultados siguientes ya no se emitirían.
    set<String?>(key.name, null)
    return key.decodeOrNull(encoded)
}
