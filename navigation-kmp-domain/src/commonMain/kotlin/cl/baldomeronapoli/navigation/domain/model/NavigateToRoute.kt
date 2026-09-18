package cl.baldomeronapoli.navigation.domain.model

import cl.baldomeronapoli.base.domain.models.Destination
import cl.baldomeronapoli.base.navigation.NavigationCommand

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
    val route: Destination,
    val popUpTo: Destination? = null,
    val inclusive: Boolean = false,
    val singleTop: Boolean = true
) : NavigationCommand
