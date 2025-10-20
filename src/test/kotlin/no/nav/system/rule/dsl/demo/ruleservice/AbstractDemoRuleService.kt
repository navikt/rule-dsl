package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.AbstractResourceAccessor
import no.nav.system.rule.dsl.AbstractRuleService
import java.time.LocalDate
import java.time.YearMonth

abstract class AbstractDemoRuleService<Response> : AbstractRuleService<Response>() {

    override fun run(): Response {
        putResource(
            GrunnbeløpSatsResource::class,
            GrunnbeløpSatsResource()
        )
        return ruleService.invoke()
    }
}

fun AbstractResourceAccessor.grunnbeløpByDate(dato: LocalDate) =
    getResource(GrunnbeløpSatsResource::class).grunnbeløpMap.entries
        .find { entry -> dato in entry.key }?.value ?: 0

fun AbstractResourceAccessor.grunnbeløpByYearMonth(dato: YearMonth) =
    getResource(GrunnbeløpSatsResource::class).grunnbeløpMap.entries
        .find { entry -> dato.atDay(1) in entry.key }?.value ?: 0