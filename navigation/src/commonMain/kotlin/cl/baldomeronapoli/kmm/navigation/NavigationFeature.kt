package cl.baldomeronapoli.kmm.navigation

import cl.baldomeronapoli.kmm.base.feature.Feature
import cl.baldomeronapoli.kmm.navigation.di.navigationModule
import org.koin.core.module.Module

/**
 * Feature de navegación.
 * Provee el NavigationCoordinator como servicio singleton.
 */
class NavigationFeature : Feature {

    override val featureName: String = "navigation"

    override val priority: Int = 1

    override fun provideDependencies(): List<Module> = listOf(
        navigationModule
    )

    override fun initialize() {
        // Nada que inicializar
    }

    override fun dispose() {
        // Cleanup si es necesario
    }
}