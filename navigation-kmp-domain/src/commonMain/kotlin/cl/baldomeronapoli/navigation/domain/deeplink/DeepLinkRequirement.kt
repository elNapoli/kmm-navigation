package cl.baldomeronapoli.navigation.domain.deeplink

/**
 * Condición que un deep link debe cumplir antes de navegar directo al
 * destino mapeado. El caso típico es sesión iniciada, pero el contrato es
 * genérico a propósito — cualquier chequeo binario vale (feature flag,
 * onboarding completo, etc). Si no se cumple, [HandleDeepLinkUseCase]
 * persiste la URI y redirige a un destino de respaldo en su lugar.
 */
interface DeepLinkRequirement {
    fun isSatisfied(): Boolean
}
