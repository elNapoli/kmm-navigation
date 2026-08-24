# Napoli KMM Navigation

Librería de navegación desacoplada entre features para Kotlin Multiplatform (Android e iOS),
construida sobre Navigation Compose Multiplatform y el sistema de features de
[`napoli-kmm-base`](https://github.com/elNapoli/kmm-base).

## Características

- **Navegación desacoplada entre features**: un feature navega a otro sin conocer su implementación
- **Coordinador central**: `NavigationCoordinator` enruta comandos al handler correcto
- **Comandos tipados**: cada feature define su propio `NavigationContract` con sus comandos posibles
- **Comandos comunes incluidos**: `NavigateBack`, `NavigateBackTo`, `NavigateToRoute` ya vienen resueltos
- **Integración con Koin**: se registra con un módulo (`NavigationModule`) listo para usar
- **Multiplataforma**: Android e iOS (`iosArm64`, `iosSimulatorArm64`)

## Instalación

Publicada en GitHub Packages (repo privado). Requiere PAT con scope `read:packages` — ver
[Instalación en el README de napoli-kmm-base](https://github.com/elNapoli/kmm-base#instalación)
para el detalle de cómo generar el token y configurar `local.properties`.

En `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/elNapoli/kmm-navigation")
            credentials {
                val localProperties = java.util.Properties().apply {
                    val file = File(rootDir, "local.properties")
                    if (file.exists()) load(file.inputStream())
                }
                username = localProperties.getProperty("gpr.user")
                    ?: System.getenv("GITHUB_ACTOR")
                password = localProperties.getProperty("gpr.token")
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

En `libs.versions.toml`:

```toml
[versions]
napoli-navigation = "1.0.0"

[libraries]
napoli-kmm-navigation = { module = "cl.baldomeronapoli:navigation-kmp", version.ref = "napoli-navigation" }
```

En el `build.gradle.kts` de tu módulo:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.napoli.kmm.navigation)
        }
    }
}
```

> Esta librería depende de `cl.baldomeronapoli:base-kmp` y `cl.baldomeronapoli:logger-kmp` —
> asegúrate de tener también sus repos de GitHub Packages configurados
> (ver [Instalación en base-kmp](https://github.com/elNapoli/kmm-base#instalación)).

## Arquitectura

Cada feature define sus propios comandos de navegación (`NavigationContract`) y su propio
handler (`NavigationHandler`). El `NavigationCoordinator` central recibe cualquier comando y lo
enruta al handler del feature correspondiente — así un feature puede pedirle a otro que navegue
sin importar su implementación (pantallas, rutas internas, etc).

```
ViewModel (Feature A)
   |
   | navigationCoordinator.navigate(HomeContract.NavigateToHome)
   v
NavigationCoordinator
   |
   | busca el handler registrado para featureName = "home"
   v
HomeNavigationHandler
   |
   | navController.navigate(...)
   v
NavController ejecuta la navegación
```

### Piezas clave (viven en `cl.baldomeronapoli.base.navigation`, dentro de `base-kmp`)

- **`NavigationCommand`**: interface marcadora, cualquier comando de navegación la implementa
- **`NavigationHandler`**: contrato que cada feature implementa para procesar sus comandos
  ```kotlin
  interface NavigationHandler {
      val featureName: String
      fun handle(command: NavigationCommand, navController: NavHostController): Boolean
  }
  ```
- **`NavigationCoordinator`**: coordina el envío de comandos al handler correcto
  ```kotlin
  interface NavigationCoordinator {
      fun setNavController(navController: NavHostController)
      fun registerHandler(handler: NavigationHandler)
      fun registerHandlers(vararg handlers: NavigationHandler)
      fun navigate(command: NavigationCommand): Boolean
      fun getHandler(featureName: String): NavigationHandler?
      fun hasHandler(featureName: String): Boolean
      fun clear()
  }
  ```

### Piezas que aporta esta librería (`cl.baldomeronapoli.navigation`)

- **`NavigationCoordinatorImpl`**: implementación de `NavigationCoordinator`. Registra
  automáticamente un `CommonNavigationHandler` al crearse.
- **`CommonNavigationHandler`**: handler incluido por defecto (`featureName = "common"`) que
  resuelve los comandos genéricos sin que cada feature tenga que reimplementarlos:
  - `NavigateBack`: navega hacia atrás (`navController.navigateUp()`)
  - `NavigateBackTo(route, inclusive)`: hace pop hasta una ruta específica (o hasta el root si `route == null`)
  - `NavigateToRoute(route, popUpTo, inclusive, singleTop)`: navega a una `Destination` directamente
- **`NavigationContract`** / **`TypedNavigationContract<T>`**: contratos base para que cada
  feature defina sus propios comandos tipados
- **`NavigationModule`**: módulo de Koin que registra `NavigationCoordinator` como singleton

### `Destination` (de `base-kmp`)

Marker interface (`cl.baldomeronapoli.base.domain.models.Destination`) que deben implementar tus
rutas `@Serializable` para poder usarse con `NavigateToRoute` y con `composable<T>()` /
`navigation<T>()` de Navigation Compose:

```kotlin
@Serializable
sealed class SettingsDestination : Destination {
    @Serializable
    data object Graph : SettingsDestination()

    @Serializable
    data object Main : SettingsDestination()
}
```

## Uso

### 1. Definir el contrato de navegación del feature

Cada feature declara qué comandos de navegación expone hacia el resto de la app:

```kotlin
sealed interface HomeContract : NavigationCommand {
    data object NavigateToHome : HomeContract
    data class NavigateToProfile(val userId: String) : HomeContract
}
```

### 2. Implementar el `NavigationHandler` del feature

```kotlin
class HomeNavigationHandler : NavigationHandler {
    override val featureName = "home"

    override fun handle(
        command: NavigationCommand,
        navController: NavHostController
    ): Boolean = when (command) {
        is HomeContract.NavigateToHome -> {
            navController.navigate(HomeDestination.Main)
            true
        }
        is HomeContract.NavigateToProfile -> {
            navController.navigate(HomeDestination.Profile(command.userId))
            true
        }
        else -> false
    }
}
```

### 3. Implementar `NavigableFeature` (de `base-kmp`)

```kotlin
class HomeFeature : NavigableFeature, KoinComponent {
    override val featureName = "home"
    override val priority = 100

    override var navigationCoordinator: NavigationCoordinator? = null
        get() = field ?: inject<NavigationCoordinator>().value.also { field = it }

    override fun provideDependencies(): List<Module> = HomeModule.getModules()

    override fun NavGraphBuilder.registerNavigation() {
        navigation<HomeDestination.Graph>(startDestination = HomeDestination.Main::class) {
            composable<HomeDestination.Main> {
                LazyFeatureLoader(featureName = featureName) { MainRoute() }
            }
        }
    }

    override fun onNavigationReady(navController: NavHostController) {}
    override fun dispose() { navigationCoordinator = null }
}
```

### 4. Registrar Koin y los handlers en el punto de entrada de la app

```kotlin
startKoin {
    modules(AppModule.getModules())
    modules(NavigationModule.getModules())       // registra NavigationCoordinator
    modules(featureManager.getCriticalDependencyModules(maxPriority = 50))
}
```

```kotlin
@Composable
fun AppRoute() {
    val navController = rememberNavController()
    val navigationCoordinator: NavigationCoordinator = koinInject()
    val featureManager: FeatureManager = koinInject()

    LaunchedEffect(navController) {
        navigationCoordinator.setNavController(navController)
        navigationCoordinator.registerHandlers(
            HomeNavigationHandler(),
            SettingsNavigationHandler(),
            // ... un handler por feature que necesite manejar comandos propios
        )
    }

    LaunchedEffect(navController) {
        featureManager.notifyNavigationReady(navController)
    }

    MainRoute(navController = navController)
}
```

### 5. Navegar desde cualquier feature

```kotlin
// Navegar con un comando propio del feature
navigationCoordinator.navigate(HomeContract.NavigateToProfile(userId = "123"))

// Navegar a una ruta directa sin contrato
navigationCoordinator.navigate(NavigateToRoute(HomeDestination.Main))

// Volver atrás
navigationCoordinator.navigate(NavigateBack)

// Volver hasta una ruta específica
navigationCoordinator.navigate(NavigateBackTo(route = HomeDestination.Main, inclusive = false))
```

Si el comando implementa `NavigationContract` (tiene `featureName`), el coordinador lo despacha
directo al handler de ese feature. Si no lo implementa (como `NavigateBack`/`NavigateToRoute`),
prueba primero con el handler `"common"` y, si ninguno lo maneja, recorre el resto de handlers
registrados como fallback.

## Troubleshooting

**"No handler found for feature 'x'"** (log de warning): no se registró un `NavigationHandler`
para ese `featureName` en `navigationCoordinator.registerHandlers(...)`, o el feature todavía no
implementó lógica real en su handler (comúnmente queda con `else -> false` mientras se desarrolla).

**"Cannot navigate, NavController not set"**: se llamó a `navigate()` antes de que
`navigationCoordinator.setNavController(navController)` corriera. Asegúrate de que el
`LaunchedEffect` que setea el NavController corra antes de disparar navegación (incluyendo deep
links en el cold start).

## Referencias

- [Navigation Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation.html)
- [Type Safety in Navigation](https://developer.android.com/guide/navigation/design/type-safety)
- [napoli-kmm-base](https://github.com/elNapoli/kmm-base) — define `NavigationCommand`,
  `NavigationHandler`, `NavigationCoordinator`, `NavigableFeature`, `Destination`, `FeatureManager`

## Licencia

MIT License
