package cl.baldomeronapoli.kmm.navigation.data.repository

import androidx.navigation.NavHostController
import cl.baldomeronapoli.kmm.base.navigation.NavigationCommand
import cl.baldomeronapoli.kmm.base.navigation.NavigationCoordinator
import cl.baldomeronapoli.kmm.base.navigation.NavigationHandler
import cl.baldomeronapoli.kmm.navigation.domain.model.NavigationContract
import cl.baldomeronapoli.kmm.navigation.data.datasource.CommonNavigationHandler
import io.github.aakira.napier.Napier

/**
 * Coordinador central de navegación.
 *
 * Recibe comandos de navegación y los delega al handler apropiado.
 * Esto permite navegación desacoplada entre features.
 *
 * Flujo de navegación:
 * ```
 * ViewModel (Feature A)
 *    |
 *    | navigate(HomeNavigationContract.NavigateToHome)
 *    v
 * NavigationCoordinator
 *    |
 *    | Encuentra el handler del feature "home"
 *    v
 * HomeNavigationHandler
 *    |
 *    | navController.navigate("home")
 *    v
 * NavController ejecuta navegación
 * ```
 *
 * Uso:
 * ```kotlin
 * // En tu MainActivity/NavHost
 * val navigationCoordinator = remember { NavigationCoordinator() }
 *
 * // Registrar handlers
 * LaunchedEffect(navController) {
 *     navigationCoordinator.setNavController(navController)
 *     navigationCoordinator.registerHandler(HomeNavigationHandler())
 *     navigationCoordinator.registerHandler(LoginNavigationHandler())
 * }
 *
 * // En ViewModels
 * navigationCoordinator.navigate(HomeNavigationContract.NavigateToHome)
 * ```
 */
class NavigationCoordinatorImpl : NavigationCoordinator {

    private val handlers = mutableMapOf<String, NavigationHandler>()
    private var navController: NavHostController? = null

    init {
        registerHandler(CommonNavigationHandler())
    }

    override fun setNavController(navController: NavHostController) {
        this.navController = navController
        Napier.d("NavigationCoordinator: NavController set")
    }

    override fun registerHandler(handler: NavigationHandler) {
        if (handlers.containsKey(handler.featureName)) {
            Napier.d("NavigationCoordinator: Handler for '${handler.featureName}' already registered, skipping")
            return
        }
        handlers[handler.featureName] = handler
        Napier.d("NavigationCoordinator: Registered handler for feature '${handler.featureName}'")
    }

    override fun registerHandlers(vararg handlers: NavigationHandler) {
        handlers.forEach { registerHandler(it) }
    }

    override fun navigate(command: NavigationCommand): Boolean {
        val currentNavController = navController
        if (currentNavController == null) {
            Napier.e("NavigationCoordinator: Cannot navigate, NavController not set")
            return false
        }

        // Si el comando implementa NavigationContract, buscar handler específico
        if (command is NavigationContract) {
            val handler = handlers[command.featureName]
            if (handler != null) {
                val handled = handler.handle(command, currentNavController)
                if (handled) {
                    Napier.d("NavigationCoordinator: Command $command handled by ${handler.featureName}")
                    return true
                }
            } else {
                Napier.w("NavigationCoordinator: No handler found for feature '${command.featureName}'")
            }
        }

        // Intentar con el handler común
        val commonHandler = handlers["common"]
        if (commonHandler != null && commonHandler.handle(command, currentNavController)) {
            Napier.d("NavigationCoordinator: Command $command handled by common handler")
            return true
        }

        // Intentar con todos los handlers (fallback)
        for (handler in handlers.values) {
            if (handler.handle(command, currentNavController)) {
                Napier.d("NavigationCoordinator: Command $command handled by ${handler.featureName} (fallback)")
                return true
            }
        }

        Napier.w("NavigationCoordinator: No handler found for command: $command")
        return false
    }

    override fun getHandler(featureName: String): NavigationHandler? {
        return handlers[featureName]
    }

    override fun hasHandler(featureName: String): Boolean {
        return handlers.containsKey(featureName)
    }

    override fun clear() {
        handlers.clear()
        registerHandler(CommonNavigationHandler())
        navController = null
        Napier.d("NavigationCoordinator: Cleared all handlers")
    }
}
