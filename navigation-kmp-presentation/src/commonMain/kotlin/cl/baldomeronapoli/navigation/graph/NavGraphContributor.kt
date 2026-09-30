package cl.baldomeronapoli.navigation.graph

import androidx.navigation.NavGraphBuilder

/**
 * Aporte de un feature al grafo de navegación de la app.
 *
 * Cada feature implementa uno y lo registra en su módulo de Koin como binding
 * secundario (un `single<NavGraphContributor>` por feature se pisarían entre sí):
 * ```kotlin
 * singleOf(::HomeNavGraph) bind NavGraphContributor::class
 * ```
 *
 * El host los junta con `getAll<NavGraphContributor>()` y los aplica dentro del
 * `NavHost`, sin conocer a ningún feature:
 * ```kotlin
 * NavHost(navController, startDestination) { contributeAll(contributors) }
 * ```
 *
 * `getAll` no garantiza orden. Navigation Compose no depende del orden en que se
 * declaran los destinos, pero dos features no pueden registrar la misma ruta.
 */
interface NavGraphContributor {
    fun NavGraphBuilder.contribute()
}

/**
 * Aplica en este grafo el aporte de cada [NavGraphContributor].
 */
fun NavGraphBuilder.contributeAll(contributors: Iterable<NavGraphContributor>) {
    contributors.forEach { contributor -> with(contributor) { contribute() } }
}
