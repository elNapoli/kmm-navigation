package cl.baldomeronapoli.navigation.domain.model

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResultKeyTest {
    @Serializable
    sealed interface FakeResult : NavigationResult {
        @Serializable
        data class Verified(val purpose: String) : FakeResult

        @Serializable
        data class Failed(val reason: String) : FakeResult
    }

    private val key = ResultKey("fake.result", FakeResult.serializer())

    @Test
    fun decodesTheSameSubtypeItEncoded() {
        val result: FakeResult = FakeResult.Failed(reason = "blocked")

        assertEquals(result, key.decodeOrNull(key.encode(result)))
    }

    @Test
    fun returnsNullForPayloadOfAnotherSchema() {
        assertNull(key.decodeOrNull("""{"type":"unknown.Subtype"}"""))
        assertNull(key.decodeOrNull("not json"))
    }
}
