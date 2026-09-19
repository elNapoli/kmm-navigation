package cl.baldomeronapoli.navigation.data.datasource

import cl.baldomeronapoli.navigation.domain.model.NavigationCommand

/**
 * Handler que procesa comandos de navegación.
 * Esta es la interfaz - cada feature implementa su propio handler.
 *
 * [NavController] es el tipo de controlador de navegación concreto de la
 * plataforma UI (ej. `androidx.navigation.NavHostController` en Compose),
 * para que este módulo no dependa de ninguna librería de navegación.
 */
interface NavigationHandler<NavController> {
    /**
     * Nombre del feature que este handler maneja.
     */
    val featureName: String

    /**
     * Procesa un comando de navegación.
     */
    fun handle(
        command: NavigationCommand,
        navController: NavController,
    ): Boolean
}
