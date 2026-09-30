package cl.baldomeronapoli.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavGraphNavigator
import androidx.navigation.NavigatorProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class NavGraphContributorTest {
    private class RecordingContributor(
        private val name: String,
        private val calls: MutableList<Pair<String, NavGraphBuilder>>,
    ) : NavGraphContributor {
        override fun NavGraphBuilder.contribute() {
            calls += name to this
        }
    }

    private fun graphBuilder(): NavGraphBuilder {
        val provider = NavigatorProvider().apply { addNavigator(NavGraphNavigator(this)) }
        return NavGraphBuilder(provider, startDestination = "start", route = null)
    }

    @Test
    fun contributeAllAppliesEveryContributorOnTheSameBuilder() {
        val calls = mutableListOf<Pair<String, NavGraphBuilder>>()
        val builder = graphBuilder()

        builder.contributeAll(listOf(RecordingContributor("home", calls), RecordingContributor("shop", calls)))

        assertEquals(listOf("home", "shop"), calls.map { it.first })
        calls.forEach { (_, receiver) -> assertSame(builder, receiver) }
    }
}
