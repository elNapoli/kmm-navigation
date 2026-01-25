# Napoli KMM Navigation

Librería de navegación type-safe para Kotlin Multiplatform (Android e iOS) usando Compose
Multiplatform y Navigation 2.9+.

## 🚀 Características

- ✅ **Type-safe**: Uso de `@Serializable` data classes para rutas
- ✅ **Multiplataforma**: Funciona nativamente en Android e iOS
- ✅ **Moderno**: Usa `toRoute<T>()` API (Navigation 2.9+)
- ✅ **Simple**: Menos boilerplate que el enfoque tradicional
- ✅ **Seguro**: Errores de compilación en lugar de errores en runtime

## 📦 Instalación

```kotlin
// En tu build.gradle.kts
commonMain.dependencies {
    implementation("cl.baldomeronapoli.kmm:navigation:VERSION")
}
```

## 🎯 Uso Básico

### 1. Definir Rutas con @Serializable

```kotlin
import kotlinx.serialization.Serializable

sealed class AppDestinations {
    // Ruta sin argumentos
    @Serializable
    data object Home

    // Ruta con un argumento
    @Serializable
    data class Profile(val userId: String)

    // Ruta con múltiples argumentos de diferentes tipos
    @Serializable
    data class PostDetail(
        val postId: String,
        val commentId: Int,
        val isHighlighted: Boolean = false
    )
}
```

### 2. Navegar a un Destino

```kotlin
// Navegar a Home
navController.navigate(AppDestinations.Home)

// Navegar a Profile con argumentos
navController.navigate(AppDestinations.Profile(userId = "user123"))

// Navegar a PostDetail con múltiples argumentos
navController.navigate(
    AppDestinations.PostDetail(
        postId = "post456",
        commentId = 789,
        isHighlighted = true
    )
)
```

### 3. Definir Composables y Extraer Argumentos

```kotlin
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.Home
    ) {
        // Ruta sin argumentos
        composable<AppDestinations.Home> {
            HomeScreen(
                onNavigateToProfile = { userId ->
                    navController.navigate(AppDestinations.Profile(userId))
                }
            )
        }

        // Ruta con argumentos - usar toRoute() para extraer
        composable<AppDestinations.Profile> { backStackEntry ->
            val args = backStackEntry.toRoute<AppDestinations.Profile>()
            ProfileScreen(
                userId = args.userId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable<AppDestinations.PostDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<AppDestinations.PostDetail>()
            PostDetailScreen(
                postId = args.postId,
                commentId = args.commentId,
                isHighlighted = args.isHighlighted,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
```

## 🔥 Ejemplos Avanzados

### Nested Navigation (Graphs anidados)

```kotlin
@Serializable
sealed class AuthDestinations {
    @Serializable
    data object Login

    @Serializable
    data class Otp(val phone: String)
}

@Serializable
sealed class MainDestinations {
    @Serializable
    data object Home

    @Serializable
    data object Settings
}

NavHost(navController, startDestination = AuthDestinations.Login) {
    // Auth flow
    navigation<AuthDestinations.Login>(
        startDestination = AuthDestinations.Login
    ) {
        composable<AuthDestinations.Login> {
            LoginScreen(
                onNavigateToOtp = { phone ->
                    navController.navigate(AuthDestinations.Otp(phone))
                }
            )
        }

        composable<AuthDestinations.Otp> { backStackEntry ->
            val args = backStackEntry.toRoute<AuthDestinations.Otp>()
            OtpScreen(phone = args.phone)
        }
    }

    // Main flow
    navigation<MainDestinations.Home>(
        startDestination = MainDestinations.Home
    ) {
        composable<MainDestinations.Home> {
            HomeScreen()
        }

        composable<MainDestinations.Settings> {
            SettingsScreen()
        }
    }
}
```

### Valores por Defecto

```kotlin
@Serializable
data class Filter(
    val category: String = "all",
    val sortBy: String = "date",
    val ascending: Boolean = true
)

// Navegar con valores por defecto
navController.navigate(Filter()) // Usa todos los defaults

// Navegar con algunos valores personalizados
navController.navigate(Filter(category = "tech"))

// En el composable
composable<Filter> { backStackEntry ->
    val args = backStackEntry.toRoute<Filter>()
    FilterScreen(
        category = args.category,      // "tech" o "all" (default)
        sortBy = args.sortBy,           // "date" (default)
        ascending = args.ascending      // true (default)
    )
}
```

### Tipos Complejos

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class SearchQuery(
    val query: String,
    val filters: List<String> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 20
)

// Navegar
navController.navigate(
    SearchQuery(
        query = "kotlin",
        filters = listOf("tutorial", "advanced"),
        page = 2
    )
)
```

## 🎨 Integración con ViewModel

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute

class ProfileViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Extraer argumentos directamente desde SavedStateHandle
    private val args: AppDestinations.Profile = savedStateHandle.toRoute()

    val userId: String = args.userId

    // ... resto del ViewModel
}
```

## 📚 Migración desde el Enfoque Antiguo

### Antes (String-based con placeholders)

```kotlin
// ANTES ❌
sealed class MessagingDestinations {
    data object Conversation : Destination("conversation/{chatId}") {
        fun createRoute(chatId: String) = "conversation/$chatId"

        fun getChatId(backStackEntry: NavBackStackEntry): String {
            return getArgString(backStackEntry, "chatId") ?: ""
        }
    }
}

// Navegar
navController.navigate(
    MessagingDestinations.Conversation.createRoute(chatId = "abc123")
)

// Definir composable
destinationComposable(MessagingDestinations.Conversation) { backStackEntry ->
    val chatId = MessagingDestinations.Conversation.getChatId(backStackEntry)
    ConversationScreen(chatId = chatId)
}
```

### Ahora (Type-safe con @Serializable)

```kotlin
// AHORA ✅
@Serializable
data class Conversation(val chatId: String)

// Navegar (mucho más simple)
navController.navigate(Conversation(chatId = "abc123"))

// Definir composable (type-safe)
composable<Conversation> { backStackEntry ->
    val args = backStackEntry.toRoute<Conversation>()
    ConversationScreen(chatId = args.chatId)
}
```

## 🔧 Troubleshooting

### Error: "Cannot access class... it is private in file"

**Solución**: Asegúrate de que tus rutas sean `data class` o `data object` y estén marcadas con
`@Serializable`.

### Error: "No serializer found for class"

**Solución**: Verifica que tienes el plugin de serialización configurado:

```kotlin
plugins {
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
```

### Los argumentos son null

**Solución**: Usa `toRoute<T>()` en lugar de acceder manualmente a `arguments`.

## 📖 Referencias

- [Navigation Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation.html)
- [Type Safety in Navigation](https://developer.android.com/guide/navigation/design/type-safety)
- [Kotlin Serialization](https://kotlinlang.org/docs/serialization.html)

## 📄 Licencia

MIT License
