package cl.baldomeronapoli.kmm.navigation.core

import cl.baldomeronapoli.kmm.base.navigation.NavigationCommand

/**
 * Contrato de navegación que cada feature debe definir en el módulo base.
 *
 * Este contrato permite que los features naveguen entre sí sin acoplamiento directo.
 *
 * Arquitectura:
 * ```
 * base/
 *   └── navigation/
 *       └── contracts/
 *           ├── LoginContract.kt   // Define comandos de login
 *           └── HomeContract.kt    // Define comandos de home
 *
 * feature-login/
 *   └── navigation/
 *       └── LoginNavigationHandler.kt       // Implementa el handler
 *
 * feature-home/
 *   └── navigation/
 *       └── HomeNavigationHandler.kt        // Implementa el handler
 * ```
 *
 * Ejemplo de definición de contrato:
 * ```kotlin
 * // En base/navigation/contracts/HomeContract.kt
 * sealed interface HomeContract : NavigationCommand {
 *     data object NavigateToHome : HomeContract
 *     data class NavigateToProfile(val userId: String) : HomeContract
 *     data class NavigateToSettings(val section: String? = null) : HomeContract
 * }
 * ```
 *
 * Ejemplo de uso desde otro feature (sin conocer implementación):
 * ```kotlin
 * // Desde LoginViewModel
 * fun onLoginSuccess() {
 *     navigate(HomeContract.NavigateToHome)
 * }
 * ```
 */
interface NavigationContract {
    /**
     * Identificador único del feature que maneja este contrato.
     * Debe ser un string único que identifique el feature (ej: "login", "home", etc.).
     */
    val featureName: String
}

/**
 * Extensión para crear contratos de navegación más fácilmente.
 *
 * Ejemplo:
 * ```kotlin
 * sealed interface LoginContract : TypedNavigationContract<LoginContract> {
 *     override val featureName: String get() = "login"
 *
 *     data object NavigateToLogin : LoginContract
 *     data class NavigateToOtp(val phone: String) : LoginContract
 * }
 * ```
 */
interface TypedNavigationContract<out T : NavigationCommand> : NavigationContract, NavigationCommand
