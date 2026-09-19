package cl.baldomeronapoli.navigation.data.datasource

import androidx.navigation.NavHostController
import cl.baldomeronapoli.navigation.domain.model.NavigateBack
import cl.baldomeronapoli.navigation.domain.model.NavigateBackTo
import cl.baldomeronapoli.navigation.domain.model.NavigateToRoute
import cl.baldomeronapoli.navigation.domain.model.NavigationCommand

/**
 * Handler genérico que maneja comandos comunes de navegación.
 *
 * Este handler está incluido por defecto en la librería y maneja
 * los comandos básicos de navegación que no requieren lógica específica del feature.
 */
class CommonNavigationHandler : NavigationHandler<NavHostController> {
    override val featureName: String = "common"

    override fun handle(
        command: NavigationCommand,
        navController: NavHostController,
    ): Boolean =
        when (command) {
            is NavigateBack -> {
                navController.navigateUp()
                true
            }

            is NavigateBackTo -> {
                val route = command.route
                if (route == null) {
                    navController.popBackStack(
                        navController.graph.startDestinationId,
                        inclusive = false,
                    )
                } else {
                    navController.popBackStack(route, command.inclusive)
                }
                true
            }

            is NavigateToRoute -> {
                navController.navigate(command.route) {
                    command.popUpTo?.let { popUpToRoute ->
                        popUpTo(popUpToRoute) {
                            inclusive = command.inclusive
                        }
                    }
                    launchSingleTop = command.singleTop
                }
                true
            }

            else -> {
                false
            }
        }
}
