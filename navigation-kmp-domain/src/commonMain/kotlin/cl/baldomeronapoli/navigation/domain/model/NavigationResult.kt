package cl.baldomeronapoli.navigation.domain.model

/**
 * Resultado que el módulo llamado devuelve al módulo que llama al terminar.
 *
 * El módulo llamado define su propio tipo sellado `@Serializable` en el
 * módulo de contratos compartido (junto a su `NavigationContract`), con sus
 * desenlaces terminales, típicamente éxito / cancelado / fallido:
 * ```kotlin
 * @Serializable
 * sealed interface PinResult : NavigationResult {
 *     @Serializable data class Verified(val purpose: PinPurpose) : PinResult
 *     @Serializable data class Cancelled(val purpose: PinPurpose) : PinResult
 *     @Serializable data class Failed(val purpose: PinPurpose, val reason: PinFailure) : PinResult
 * }
 * ```
 *
 * Solo desenlaces terminales: lo que el flujo puede resolver por sí mismo
 * (ej. PIN incorrecto con reintento) no se reporta.
 *
 * El resultado se persiste en el saved state del módulo que llama (sobrevive a la
 * muerte del proceso, en disco y sin cifrar): no incluir secretos, tokens
 * ni datos personales.
 */
interface NavigationResult
