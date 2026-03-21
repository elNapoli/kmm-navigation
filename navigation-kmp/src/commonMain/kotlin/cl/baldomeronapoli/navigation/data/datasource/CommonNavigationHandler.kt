package cl.baldomeronapoli.navigation.data.datasource

import androidx.navigation.NavHostController
import cl.baldomeronapoli.base.navigation.NavigationCommand
import cl.baldomeronapoli.base.navigation.NavigationHandler
import cl.baldomeronapoli.navigation.domain.model.NavigateBack
import cl.baldomeronapoli.navigation.domain.model.NavigateBackTo
import cl.baldomeronapoli.navigation.domain.model.NavigateToRoute

/**
 * Handler genérico que maneja comandos comunes de navegación.
 *
 * Este handler está incluido por defecto en la librería y maneja
 * los comandos básicos de navegación que no requieren lógica específica del feature.
 */
class CommonNavigationHandler : NavigationHandler {
    override val featureName: String = "common"

    override fun handle(command: NavigationCommand, navController: NavHostController): Boolean {
        return when (command) {
            is NavigateBack -> {
                navController.navigateUp()
                true
            }

            is NavigateBackTo -> {
                if (command.route == null) {
                    navController.popBackStack(
                        navController.graph.startDestinationId,
                        inclusive = false
                    )
                } else {
                    navController.popBackStack(command.route, command.inclusive)
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

            else -> false
        }
    }
}
