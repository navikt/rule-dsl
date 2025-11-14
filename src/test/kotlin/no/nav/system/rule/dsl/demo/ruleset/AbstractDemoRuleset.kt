package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.demo.ruleservice.GrunnbeløpSatsResource
import no.nav.system.rule.dsl.resource.ExecutionTrace

abstract class AbstractDemoRuleset<Response : Any> : AbstractRuleset<Response>() {
    override fun test(): Response {
        // Enable tracing for standalone tests
        putResource(ExecutionTrace::class, ExecutionTrace())
        putResource(
            GrunnbeløpSatsResource::class,
            GrunnbeløpSatsResource()
        )
        return this.run(this)
    }
}