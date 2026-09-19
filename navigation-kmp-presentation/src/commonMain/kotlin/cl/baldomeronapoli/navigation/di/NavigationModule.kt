package cl.baldomeronapoli.navigation.di

import androidx.navigation.NavHostController
import cl.baldomeronapoli.navigation.data.repository.NavigationCoordinator
import cl.baldomeronapoli.navigation.data.repository.NavigationCoordinatorImpl
import org.koin.core.module.Module
import org.koin.dsl.module

object NavigationModule {
    fun getModules(): List<Module> =
        listOf(
            commonModule(),
        )

    private fun commonModule() =
        module {
            single<NavigationCoordinator<NavHostController>> { NavigationCoordinatorImpl() }
        }
}
