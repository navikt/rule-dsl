package no.nav.pensjon.common.service

import no.nav.pensjon.common.resource.GrunnbeløpSatsResource
import no.nav.system.ruledsl.core.model.AbstractRuleComponent
import no.nav.system.ruledsl.core.model.AbstractRuleService
import no.nav.system.ruledsl.core.resource.tracker.IndentedTextTracker
import no.nav.system.ruledsl.core.resource.tracker.TrackerResource
import java.time.LocalDate
import java.time.YearMonth

abstract class AbstractDemoRuleService<Response> : AbstractRuleService<Response>() {

    override fun run(): Response {
        putResource(GrunnbeløpSatsResource::class, GrunnbeløpSatsResource())
        putResource(TrackerResource::class, IndentedTextTracker())
        return super.run()
    }
}

fun AbstractRuleComponent.grunnbeløpByDate(dato: LocalDate) =
    getResource(GrunnbeløpSatsResource::class).grunnbeløpMap.entries
        .find { entry -> dato in entry.key }?.value ?: 0

fun AbstractRuleComponent.grunnbeløpByYearMonth(dato: YearMonth) =
    getResource(GrunnbeløpSatsResource::class).grunnbeløpMap.entries
        .find { entry -> dato.atDay(1) in entry.key }?.value ?: 0