package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.demo.ruleservice.GrunnbeløpSatsResource
import no.nav.system.rule.dsl.tracker.IndentedTextTracker
import no.nav.system.rule.dsl.tracker.TrackerResource

abstract class AbstractDemoRuleset<Response : Any> : AbstractRuleset<Response>() {
    override fun test(): Response {
        putResource(GrunnbeløpSatsResource::class, GrunnbeløpSatsResource())
        putResource(TrackerResource::class, IndentedTextTracker())
        return internalRun()
    }
}