package cl.baldomeronapoli.kmm.navigation.commands

import cl.baldomeronapoli.kmm.base.navigation.NavigationCommand

/**
 * Comando para navegar hacia atrás hasta una ruta específica.
 *
 * @param route Ruta de destino. Si es null, navega hasta el root.
 * @param inclusive Si true, también elimina la ruta de destino del stack.
 */
data class NavigateBackTo(
    val route: String? = null,
    val inclusive: Boolean = false
) : NavigationCommand
