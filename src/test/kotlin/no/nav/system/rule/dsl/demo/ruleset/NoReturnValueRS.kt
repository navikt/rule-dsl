package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset

/**
 * Regelsett med type Int uten returnverdi er ugyldig.
 */
class NoReturnValueRS : AbstractRuleset<Int>() {
    override fun create() {}
}
