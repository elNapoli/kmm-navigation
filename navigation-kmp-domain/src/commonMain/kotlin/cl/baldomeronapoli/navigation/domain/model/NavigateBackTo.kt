package cl.baldomeronapoli.navigation.domain.model

/**
 * Comando para navegar hacia atrás hasta una ruta específica.
 *
 * @param route Ruta de destino. Si es null, navega hasta el root.
 * @param inclusive Si true, también elimina la ruta de destino del stack.
 */
data class NavigateBackTo(
    val route: Destination? = null,
    val inclusive: Boolean = false,
) : NavigationCommand
