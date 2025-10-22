package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.pattern.createPattern
import no.nav.system.rule.dsl.rettsregel.Faktum

class FaktumPatternRS(
    inputFakta: List<Faktum<Boolean>>,
) : AbstractRuleset<Unit>() {
    private val faktumListe = inputFakta.createPattern()

    @OptIn(DslDomainPredicate::class)
    override fun create() {
        regel("sann", faktumListe) { bool ->
            HVIS { bool }
        }

    }
}