package cl.baldomeronapoli.kmm.navigation.core

import androidx.navigation.NavBackStackEntry

/**
 * Implementación Android para extraer argumentos del NavBackStackEntry.
 */
actual fun getStringArgument(backStackEntry: NavBackStackEntry, key: String): String? {
    return backStackEntry.arguments?.getString(key)
}

actual fun getIntArgument(backStackEntry: NavBackStackEntry, key: String): Int? {
    return backStackEntry.arguments?.getInt(key)
}

actual fun getLongArgument(backStackEntry: NavBackStackEntry, key: String): Long? {
    return backStackEntry.arguments?.getLong(key)
}

actual fun getBooleanArgument(backStackEntry: NavBackStackEntry, key: String): Boolean? {
    return backStackEntry.arguments?.getBoolean(key)
}

actual fun getFloatArgument(backStackEntry: NavBackStackEntry, key: String): Float? {
    return backStackEntry.arguments?.getFloat(key)
}