package cl.baldomeronapoli.kmm.navigation.core

/**
 * Destination con query parameters opcionales.
 *
 * Usado cuando necesitas pasar parámetros opcionales via query string.
 *
 * Ejemplo:
 * ```kotlin
 * sealed class SettingsDestinations {
 *     data object Settings : DestinationWithQuery(
 *         baseRoute = "settings",
 *         queryParams = listOf("section", "highlight")
 *     ) {
 *         fun createRoute(section: String? = null, highlight: Boolean = false): String {
 *             return buildRoute(
 *                 "section" to section,
 *                 "highlight" to if (highlight) "true" else null
 *             )
 *         }
 *
 *         fun getSection(backStackEntry: NavBackStackEntry): String? {
 *             return getArg(backStackEntry, "section")
 *         }
 *
 *         fun isHighlight(backStackEntry: NavBackStackEntry): Boolean {
 *             return getArg<String>(backStackEntry, "highlight") == "true"
 *         }
 *     }
 * }
 * ```
 *
 * Uso en NavHost:
 * ```kotlin
 * composable(SettingsDestinations.Settings.route) { backStackEntry ->
 *     val section = SettingsDestinations.Settings.getSection(backStackEntry)
 *     val highlight = SettingsDestinations.Settings.isHighlight(backStackEntry)
 *     SettingsScreen(initialSection = section, highlight = highlight)
 * }
 * ```
 */
abstract class DestinationWithQuery(
    private val baseRoute: String,
    private val queryParams: List<String>
) : Destination(baseRoute) {

    /**
     * Ruta completa para registrar en el NavHost (con placeholders de query params).
     * Ejemplo: "settings?section={section}&highlight={highlight}"
     */
    val routeWithQuery: String = buildString {
        append(baseRoute)
        if (queryParams.isNotEmpty()) {
            append("?")
            append(queryParams.joinToString("&") { "$it={$it}" })
        }
    }

    /**
     * Helper para construir la ruta con query parameters.
     * Solo incluye los parámetros no nulos/no vacíos.
     *
     * @param params Pares de key-value para los query parameters
     * @return Ruta completa con query string
     */
    protected fun buildRoute(vararg params: Pair<String, String?>): String {
        val query = params
            .filter { it.second != null && it.second?.isNotEmpty() == true }
            .joinToString("&") { "${it.first}=${it.second}" }

        return if (query.isEmpty()) baseRoute else "$baseRoute?$query"
    }
}
