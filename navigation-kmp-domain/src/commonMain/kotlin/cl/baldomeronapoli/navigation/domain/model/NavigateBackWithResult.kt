package cl.baldomeronapoli.navigation.domain.model

/**
 * Comando con el que el módulo llamado se cierra y entrega [result] al módulo que llama.
 *
 * @param key Canal de resultado que el módulo que llama observa.
 * @param result Desenlace del flujo.
 * @param popUpTo Ruta raíz del flujo (típicamente su grafo). Se hace pop
 * inclusive hasta ella y el resultado se entrega a la entrada que queda
 * arriba del stack. Si es null, el flujo es de una sola pantalla: se entrega
 * a la entrada anterior y se hace un pop simple.
 */
data class NavigateBackWithResult<R : NavigationResult>(
    val key: ResultKey<R>,
    val result: R,
    val popUpTo: Destination? = null,
) : NavigationCommand {
    fun encodedResult(): String = key.encode(result)
}
