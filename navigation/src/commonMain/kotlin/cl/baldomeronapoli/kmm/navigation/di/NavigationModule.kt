package cl.baldomeronapoli.kmm.navigation.di

import cl.baldomeronapoli.kmm.base.navigation.NavigationCoordinator
import cl.baldomeronapoli.kmm.navigation.coordinator.NavigationCoordinatorImpl
import org.koin.dsl.module


val navigationModule = module {
    single<NavigationCoordinator> { NavigationCoordinatorImpl() }
}