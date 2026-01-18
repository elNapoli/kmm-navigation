package cl.baldomeronapoli.kmm.navigation.commands

import cl.baldomeronapoli.kmm.base.navigation.NavigationCommand

/**
 * Comando para navegar a una ruta específica sin tipo (fallback).
 * Úsalo solo si no tienes un contrato definido.
 *
 * @param route Ruta de navegación
 * @param popUpTo Ruta hasta la cual hacer pop (opcional)
 * @param inclusive Si true, también elimina la ruta popUpTo del stack
 * @param singleTop Si true, evita múltiples copias de la misma ruta
 */
data class NavigateToRoute(
    val route: String,
    val popUpTo: String? = null,
    val inclusive: Boolean = false,
    val singleTop: Boolean = true
) : NavigationCommand
