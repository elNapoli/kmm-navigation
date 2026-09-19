package cl.baldomeronapoli.navigation.data.repository

import cl.baldomeronapoli.navigation.data.datasource.NavigationHandler
import cl.baldomeronapoli.navigation.domain.model.NavigationCommand

/**
 * Coordinador central de navegación.
 * Esta es la interfaz - la implementación es [NavigationCoordinatorImpl].
 *
 * [NavController] es el tipo de controlador de navegación concreto de la
 * plataforma UI (ej. `androidx.navigation.NavHostController` en Compose),
 * para que este módulo no dependa de ninguna librería de navegación.
 */
interface NavigationCoordinator<NavController> {
    /**
     * Establece el NavController a usar.
     */
    fun setNavController(navController: NavController)

    /**
     * Registra un handler de navegación.
     */
    fun registerHandler(handler: NavigationHandler<NavController>)

    /**
     * Registra múltiples handlers.
     */
    fun registerHandlers(vararg handlers: NavigationHandler<NavController>)

    /**
     * Navega usando un comando de navegación.
     */
    fun navigate(command: NavigationCommand): Boolean

    /**
     * Obtiene un handler por nombre de feature.
     */
    fun getHandler(featureName: String): NavigationHandler<NavController>?

    /**
     * Verifica si existe un handler registrado.
     */
    fun hasHandler(featureName: String): Boolean

    /**
     * Limpia todos los handlers y el NavController.
     */
    fun clear()
}
