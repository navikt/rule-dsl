package no.nav.pensjon.regler.sliterordning.config

import no.nav.system.ruledsl.core.model.AbstractRuleset

abstract class AbstractDemoRuleset<Response : Any> : AbstractRuleset<Response>() {

    override fun test(): Response {
        putResource(GrunnbeløpSatsResource::class, GrunnbeløpSatsResource())
        return internalRun()
    }
}