package cl.baldomeronapoli.navigation.deeplink

import cl.baldomeronapoli.navigation.data.repository.NavigationCoordinator
import cl.baldomeronapoli.navigation.domain.deeplink.DeepLinkRequirement
import cl.baldomeronapoli.navigation.domain.deeplink.PendingDeepLinkRepository
import cl.baldomeronapoli.navigation.domain.model.Destination
import cl.baldomeronapoli.navigation.domain.model.NavigateToRoute

/**
 * Top-level entry point for incoming deep links. Decides whether to navigate
 * immediately, defer until [requirement] is satisfied, or drop the URI
 * entirely.
 *
 * Behaviour matrix:
 *  - [parse] returns null (unmappable/unrecognized route) -> log + drop.
 *  - [mapToDestination] returns null (pass-through route, e.g. a payment
 *    bridge callback that isn't a navigation target) -> drop.
 *  - requirement satisfied + mappable route -> coordinator.navigate(...).
 *  - requirement NOT satisfied + mappable route -> persist URI, navigate to
 *    [fallbackDestination]; the consumer app drains the URI once the
 *    requirement is met (e.g. after login).
 *
 * [parse] and [mapToDestination] are plain functions so the consumer app's
 * route model and URI shape stay entirely out of this module — this class
 * only knows the requirement-gate + persist-or-navigate policy. The
 * requirement itself is generic on purpose: session/auth is the typical
 * case, but any binary precondition works (feature flag, onboarding, etc).
 */
class HandleDeepLinkUseCase<Route, NavController>(
    private val parse: (String) -> Route?,
    private val mapToDestination: (Route) -> Destination?,
    private val requirement: DeepLinkRequirement,
    private val pendingRepository: PendingDeepLinkRepository,
    private val navigationCoordinator: NavigationCoordinator<NavController>,
    private val fallbackDestination: Destination,
) {
    suspend operator fun invoke(uri: String) {
        val route = parse(uri) ?: return
        val destination = mapToDestination(route) ?: return

        if (!requirement.isSatisfied()) {
            // Persist + bounce to the fallback. Draining the URI once the
            // requirement is met is the consumer app's responsibility.
            pendingRepository.save(uri)
            navigationCoordinator.navigate(NavigateToRoute(fallbackDestination))
            return
        }

        // Requirement met. Try direct navigation; the coordinator returns
        // false when the NavController is not attached yet (cold start
        // race: the intent fires before the root composable mounts). In
        // that case persist so the root composable can drain the URI right
        // after setNavController.
        val handled = navigationCoordinator.navigate(NavigateToRoute(destination))
        if (!handled) {
            pendingRepository.save(uri)
        }
    }
}
