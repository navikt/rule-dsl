package no.nav.pensjon.regler.alderspensjon.config

import no.nav.system.ruledsl.core.model.arc.AbstractRuleset

abstract class AbstractDemoRuleset<Response : Any> : AbstractRuleset<Response>() {

    override fun test(): Response {
        putResource(GrunnbeløpSatsResource::class, GrunnbeløpSatsResource())
        return internalRun()
    }
}