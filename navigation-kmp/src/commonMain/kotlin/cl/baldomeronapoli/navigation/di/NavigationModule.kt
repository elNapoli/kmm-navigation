package cl.baldomeronapoli.navigation.di

import cl.baldomeronapoli.base.navigation.NavigationCoordinator
import cl.baldomeronapoli.navigation.data.repository.NavigationCoordinatorImpl
import org.koin.core.module.Module
import org.koin.dsl.module

object NavigationModule {

    fun getModules(): List<Module> {
        return listOf(
            commonModule()
        )
    }

    private fun commonModule() = module {
        single<NavigationCoordinator> { NavigationCoordinatorImpl() }
    }
}
