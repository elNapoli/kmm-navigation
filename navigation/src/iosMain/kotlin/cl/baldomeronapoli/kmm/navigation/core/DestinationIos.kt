package cl.baldomeronapoli.kmm.navigation.core

import androidx.navigation.NavBackStackEntry

/**
 * Implementación iOS para extraer argumentos del NavBackStackEntry.
 *
 * IMPORTANTE: En iOS, Bundle.getString/getInt/etc NO están disponibles.
 * Los path parameters se acceden directamente como un Map<String, Any?>.
 *
 * Esta es una limitación conocida de androidx.navigation multiplataforma en iOS.
 */
actual fun getStringArgument(backStackEntry: NavBackStackEntry, key: String): String? {
    // En iOS, arguments es un Map, no un Bundle con métodos tipados
    val args = backStackEntry.arguments as? Map<*, *>
    return args?.get(key)?.toString()
}

actual fun getIntArgument(backStackEntry: NavBackStackEntry, key: String): Int? {
    val args = backStackEntry.arguments as? Map<*, *>
    return when (val value = args?.get(key)) {
        is Int -> value
        is String -> value.toIntOrNull()
        else -> null
    }
}

actual fun getLongArgument(backStackEntry: NavBackStackEntry, key: String): Long? {
    val args = backStackEntry.arguments as? Map<*, *>
    return when (val value = args?.get(key)) {
        is Long -> value
        is Int -> value.toLong()
        is String -> value.toLongOrNull()
        else -> null
    }
}

actual fun getBooleanArgument(backStackEntry: NavBackStackEntry, key: String): Boolean? {
    val args = backStackEntry.arguments as? Map<*, *>
    return when (val value = args?.get(key)) {
        is Boolean -> value
        is String -> value.toBooleanStrictOrNull()
        else -> null
    }
}

actual fun getFloatArgument(backStackEntry: NavBackStackEntry, key: String): Float? {
    val args = backStackEntry.arguments as? Map<*, *>
    return when (val value = args?.get(key)) {
        is Float -> value
        is Double -> value.toFloat()
        is String -> value.toFloatOrNull()
        else -> null
    }
}
