package no.nav.pensjon.common.ruleset

import no.nav.pensjon.common.resource.GrunnbeløpSatsResource
import no.nav.system.ruledsl.core.model.AbstractRuleset
import no.nav.system.ruledsl.core.resource.tracker.IndentedTextTracker
import no.nav.system.ruledsl.core.resource.tracker.TrackerResource

abstract class AbstractDemoRuleset<Response : Any> : AbstractRuleset<Response>() {
    override fun test(): Response {
        putResource(GrunnbeløpSatsResource::class, GrunnbeløpSatsResource())
        putResource(TrackerResource::class, IndentedTextTracker())
        return internalRun()
    }
}