package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.demo.ruleservice.GrunnbeløpSatsResource

abstract class AbstractDemoRuleset<Response : Any> : AbstractRuleset<Response>() {
    override fun test(): Response {
        putResource(
            GrunnbeløpSatsResource::class,
            GrunnbeløpSatsResource()
        )
        return this.run(this)
    }
}