package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.AbstractRuleService
import java.time.LocalDate

abstract class AbstractDemoRuleService<Response> : AbstractRuleService<Response>() {

    fun grunnbeløpByDate(dato: LocalDate) =
        getResource(GrunnbeløpSatsResource::class).grunnbeløpMap.entries
            .find { entry -> dato in entry.key }?.value ?: 0

    override fun run(): Response {
        putResource(
            GrunnbeløpSatsResource::class,
            GrunnbeløpSatsResource()
        )
        return ruleService.invoke()
    }
}