package cl.baldomeronapoli.kmm.navigation.di

import cl.baldomeronapoli.kmm.base.navigation.NavigationCoordinator
import cl.baldomeronapoli.kmm.navigation.data.repository.NavigationCoordinatorImpl
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
