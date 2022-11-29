package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.AbstractResourceAccessor
import no.nav.system.rule.dsl.AbstractRuleService
import java.time.LocalDate

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