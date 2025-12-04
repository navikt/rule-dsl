package no.nav.pensjon.regler.alderspensjon.config

import no.nav.system.ruledsl.core.model.AbstractRuleComponent
import no.nav.system.ruledsl.core.model.AbstractRuleService
import java.time.LocalDate

abstract class AbstractDemoRuleService<Response> : AbstractRuleService<Response>() {

    override fun run(): Response {
        putResource(GrunnbeløpSatsResource::class, GrunnbeløpSatsResource())
        return super.run()
    }
}

fun AbstractRuleComponent.grunnbeløpByDate(dato: LocalDate) =
    getResource(GrunnbeløpSatsResource::class).grunnbeløpMap.entries
        .find { entry -> dato in entry.key }?.value ?: 0
