# Napoli KMM Navigation

Librería de navegación desacoplada entre features para Kotlin Multiplatform (Android e iOS),
construida sobre Navigation Compose Multiplatform y el sistema de features de
[`napoli-kmm-base`](https://github.com/elNapoli/kmm-base).

## Características

- **Navegación desacoplada entre features**: un feature navega a otro sin conocer su implementación
- **Coordinador central**: `NavigationCoordinator` enruta comandos al handler correcto
- **Comandos tipados**: cada feature define su propio `NavigationContract` con sus comandos posibles
- **Comandos comunes incluidos**: `NavigateBack`, `NavigateBackTo`, `NavigateToRoute` ya vienen
  resueltos
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
    - `NavigateBackTo(route, inclusive)`: hace pop hasta una ruta específica (o hasta el root si
      `route == null`)
    - `NavigateToRoute(route, popUpTo, inclusive, singleTop)`: navega a una `Destination`
      directamente
- **`NavigationContract`** / **`TypedNavigationContract<T>`**: contratos base para que cada
  feature defina sus propios comandos tipados
- **`NavigationModule`**: módulo de Koin que registra `NavigationCoordinator` como singleton
- **`NavGraphContributor`** + **`NavGraphBuilder.contributeAll()`**: cada feature aporta su parte
  del grafo y el host las junta con `getAll<NavGraphContributor>()` de Koin, sin una lista manual
  de features

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

### 3. Aportar el grafo del feature con `NavGraphContributor`

```kotlin
class HomeNavGraph : NavGraphContributor {
    override fun NavGraphBuilder.contribute() {
        navigation<HomeDestination.Graph>(startDestination = HomeDestination.Main::class) {
            composable<HomeDestination.Main> { MainRoute() }
        }
    }
}
```

Se registra en el módulo de Koin del feature como binding **secundario**: varios
`single<NavGraphContributor>` sin qualifier se pisarían entre sí, con `bind` cada uno conserva
su propia clave y `getAll` los devuelve todos.

```kotlin
val homeModule = module {
    singleOf(::HomeNavGraph) bind NavGraphContributor::class
}
```

`getAll` no garantiza orden. A Navigation Compose no le importa el orden de los destinos, pero
dos features no pueden registrar la misma ruta.

> `NavigableFeature` + `FeatureManager.registerAllNavigationRoutes()` (de `base-kmp`) siguen
> funcionando, pero exigen instanciar cada feature a mano en la app.

### 4. Registrar Koin y los handlers en el punto de entrada de la app

```kotlin
startKoin {
    modules(AppModule.getModules())
    modules(NavigationModule.getModules())       // registra NavigationCoordinator
    modules(HomeModule.getModules() + SettingsModule.getModules())
}
```

```kotlin
@Composable
fun AppRoute() {
    val navController = rememberNavController()
    val navigationCoordinator: NavigationCoordinator = koinInject()
    val contributors = remember { getKoin().getAll<NavGraphContributor>() }

    LaunchedEffect(navController) {
        navigationCoordinator.setNavController(navController)
        navigationCoordinator.registerHandlers(
            HomeNavigationHandler(),
            SettingsNavigationHandler(),
            // ... un handler por feature que necesite manejar comandos propios
        )
    }

    NavHost(navController = navController, startDestination = HomeDestination.Graph) {
        contributeAll(contributors)
    }
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

### 6. Devolver un resultado a quien abrió el flujo

Cuando un módulo abre a otro y necesita saber cómo terminó (ej. Dashboard abre PIN) hay dos roles:

- **Módulo que llama** (Dashboard): abre el flujo y consume el resultado.
- **Módulo llamado** (PIN): produce el resultado. Es el dueño de su tipo.

El módulo llamado declara el resultado tipado **en el módulo de contratos compartido, junto a su
`NavigationContract`**, no dentro de su implementación. Así el módulo que llama ve el tipo sin
depender del módulo llamado:

```
base/navigation/contracts/
  └── PinContract.kt   // PinContract + PinResult + PinResultKey (dueño: módulo llamado)
feature-pin/           // módulo llamado: emite NavigateBackWithResult
feature-dashboard/     // módulo que llama: observa navigationResults(PinResultKey)
```

Solo desenlaces terminales: lo que el módulo llamado resuelve por sí mismo (ej. PIN incorrecto
con reintento) no se reporta.

```kotlin
// base/navigation/contracts/PinContract.kt
sealed interface PinContract : TypedNavigationContract<PinContract> {
    override val featureName: String get() = "pin"

    data class Verify(val purpose: PinPurpose) : PinContract
}

@Serializable
sealed interface PinResult : NavigationResult {
    @Serializable
    data class Verified(val purpose: PinPurpose) : PinResult
    @Serializable
    data class Cancelled(val purpose: PinPurpose) : PinResult
    @Serializable
    data class Failed(val purpose: PinPurpose, val reason: PinFailure) : PinResult
}

val PinResultKey = ResultKey("pin.result", PinResult.serializer())
```

El módulo llamado termina con `NavigateBackWithResult`. `popUpTo` es su ruta raíz (hace pop
inclusive y entrega el resultado a la entrada que queda arriba); con `popUpTo = null` entrega a la
entrada anterior y hace un pop simple. El back del sistema también debe terminar en `Cancelled`.

```kotlin
// PinViewModel (módulo llamado)
navigationCoordinator.navigate(
    NavigateBackWithResult(
        PinResultKey,
        PinResult.Verified(purpose),
        popUpTo = PinDestination.Graph
    ),
)
```

El módulo que llama lo recibe desde el `SavedStateHandle` de su ViewModel. Cada resultado se emite
una sola
vez y sobrevive a la muerte del proceso:

```kotlin
// DashboardViewModel (módulo que llama)
fun onTransfer() = navigationCoordinator.navigate(PinContract.Verify(PinPurpose.Transfer))

savedStateHandle.navigationResults(PinResultKey)
    .onEach { result ->
        when (result) {
            is PinResult.Verified -> {
                TODO()
            }
            is PinResult.Cancelled -> {
                TODO()
            }
            is PinResult.Failed -> {
                TODO()
            }
        }
    }
    .launchIn(viewModelScope)
```

> El resultado se guarda en el saved state del módulo que llama (en disco y sin cifrar tras la
> muerte del
> proceso): nunca incluyas el PIN, tokens ni datos personales. `Verified` es una señal de UI, no una
> autorización; la operación sensible la valida el backend.

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
