package cl.baldomeronapoli.kmm.navigation.core

import androidx.navigation.NavBackStackEntry

/**
 * Destination representa una ruta de navegación de forma type-safe.
 *
 * Esta clase unificada maneja tanto rutas simples como rutas con argumentos.
 *
 * Uso en NavHost:
 * ```kotlin
 * // Sin argumentos
 * composable(LoginDestinations.Login.route) {
 *     LoginScreen()
 * }
 *
 * // Con argumentos
 * composable(HomeDestinations.Profile.route) { backStackEntry ->
 *     val userId = HomeDestinations.Profile.getArgString(backStackEntry, "userId")
 *     ProfileScreen(userId = userId)
 * }
 * ```
 *
 * Ejemplos de uso:
 * ```kotlin
 * sealed class LoginDestinations {
 *     // Ruta simple sin argumentos
 *     data object Login : Destination("login")
 *
 *     // Ruta con argumentos en el path
 *     data object Otp : Destination(
 *         route = "login/otp/{phone}"
 *     ) {
 *         fun createRoute(phone: String) = "login/otp/$phone"
 *
 *         fun getPhone(backStackEntry: NavBackStackEntry): String {
 *             return getArgString(backStackEntry, "phone") ?: ""
 *         }
 *     }
 *
 *     // Ruta con múltiples argumentos
 *     data object PostDetail : Destination(
 *         route = "post/{postId}/{commentId}"
 *     ) {
 *         fun createRoute(postId: String, commentId: String) =
 *             "post/$postId/$commentId"
 *
 *         fun getPostId(backStackEntry: NavBackStackEntry) =
 *             getArgString(backStackEntry, "postId") ?: ""
 *
 *         fun getCommentId(backStackEntry: NavBackStackEntry) =
 *             getArgString(backStackEntry, "commentId") ?: ""
 *     }
 * }
 * ```
 */
abstract class Destination(
    /**
     * Ruta de navegación.
     * - Sin argumentos: "login"
     * - Con argumentos: "login/otp/{phone}"
     * - Con query params: usa DestinationWithQuery
     */
    val route: String
) {
    /**
     * Helper para extraer argumentos String del backStackEntry.
     */
    protected fun getArgString(
        backStackEntry: NavBackStackEntry,
        key: String
    ): String? = getStringArgument(backStackEntry, key)

    /**
     * Helper para extraer argumentos Int del backStackEntry.
     */
    protected fun getArgInt(
        backStackEntry: NavBackStackEntry,
        key: String
    ): Int? = getIntArgument(backStackEntry, key)

    /**
     * Helper para extraer argumentos Long del backStackEntry.
     */
    protected fun getArgLong(
        backStackEntry: NavBackStackEntry,
        key: String
    ): Long? = getLongArgument(backStackEntry, key)

    /**
     * Helper para extraer argumentos Boolean del backStackEntry.
     */
    protected fun getArgBoolean(
        backStackEntry: NavBackStackEntry,
        key: String
    ): Boolean? = getBooleanArgument(backStackEntry, key)

    /**
     * Helper para extraer argumentos Float del backStackEntry.
     */
    protected fun getArgFloat(
        backStackEntry: NavBackStackEntry,
        key: String
    ): Float? = getFloatArgument(backStackEntry, key)
}

/**
 * Funciones expect para extraer argumentos de forma multiplataforma.
 * Cada plataforma implementará su propia lógica.
 */
expect fun getStringArgument(backStackEntry: NavBackStackEntry, key: String): String?
expect fun getIntArgument(backStackEntry: NavBackStackEntry, key: String): Int?
expect fun getLongArgument(backStackEntry: NavBackStackEntry, key: String): Long?
expect fun getBooleanArgument(backStackEntry: NavBackStackEntry, key: String): Boolean?
expect fun getFloatArgument(backStackEntry: NavBackStackEntry, key: String): Float?