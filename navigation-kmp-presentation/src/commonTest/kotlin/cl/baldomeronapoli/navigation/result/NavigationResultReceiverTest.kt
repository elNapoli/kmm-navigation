package cl.baldomeronapoli.navigation.result

import androidx.lifecycle.SavedStateHandle
import cl.baldomeronapoli.navigation.domain.model.NavigationResult
import cl.baldomeronapoli.navigation.domain.model.ResultKey
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NavigationResultReceiverTest {
    @Serializable
    sealed interface FakeResult : NavigationResult {
        @Serializable
        data object Cancelled : FakeResult

        @Serializable
        data class Verified(val purpose: String) : FakeResult
    }

    private val key = ResultKey("fake.result", FakeResult.serializer())

    @Test
    fun deliversPendingResultOnlyOnce() {
        val handle = SavedStateHandle(mapOf(key.name to key.encode(FakeResult.Verified("transfer"))))

        assertEquals(FakeResult.Verified("transfer"), handle.consumeNavigationResult(key))
        assertNull(handle.consumeNavigationResult(key))
    }

    @Test
    fun deliversEachNewResultAfterConsumingThePrevious() {
        val handle = SavedStateHandle()
        val flow = handle.getStateFlow<String?>(key.name, null)

        handle[key.name] = key.encode(FakeResult.Cancelled)
        assertEquals(FakeResult.Cancelled, handle.consumeNavigationResult(key))

        handle[key.name] = key.encode(FakeResult.Cancelled)
        assertEquals(key.encode(FakeResult.Cancelled), flow.value)
    }

    @Test
    fun dropsPayloadThatDoesNotMatchTheSchema() {
        val handle = SavedStateHandle(mapOf(key.name to "{\"type\":\"removed.Subtype\"}"))

        assertNull(handle.consumeNavigationResult(key))
        assertNull(handle.get<String>(key.name))
    }
}
