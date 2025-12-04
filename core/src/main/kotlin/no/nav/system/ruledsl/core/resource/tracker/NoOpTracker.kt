package no.nav.system.ruledsl.core.resource.tracker

import no.nav.system.ruledsl.core.rettsregel.Faktum

/**
 * Default no-op tracker used as fallback when no tracker is registered.
 *
 * Provides graceful degradation - returns informative messages instead of throwing exceptions.
 * This allows forklar() and other API methods to work even when no tracker is registered.
 */
class NoOpTracker : TrackerResource<String>() {

    override fun explainFaktum(faktum: Faktum<*>, filter: Filter): String {
        return "No tracker implementation found. Register a tracker via putResource(TrackerResource::class, IndentedTextTracker())"
    }

    override fun explainFaktumAsString(faktum: Faktum<*>, filter: Filter): String {
        return explainFaktum(faktum, filter)
    }
}
