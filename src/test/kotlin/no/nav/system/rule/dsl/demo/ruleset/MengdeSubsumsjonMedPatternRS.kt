package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.pattern.createPattern
import no.nav.system.rule.dsl.rettsregel.Faktum

class MengdeSubsumsjonMedPatternRS(
    inputFakta: List<Faktum<Boolean>>,
) : AbstractRuleset<Rule>() {
    private val faktumListe = inputFakta.createPattern()

    @OptIn(DslDomainPredicate::class)
    override fun create() {
        regel("sann", faktumListe) { bool ->
            HVIS { bool }
        }
        regel("alleHarTruffet") {
            HVIS { "sann".alleHarTruffet() }
            SÅ {
                RETURNER(this)
            }
        }
        regel("ingenHarTruffet") {
            HVIS { "sann".ingenHarTruffet() }
            SÅ {
                RETURNER(this)
            }
        }
        regel("minstEnHarTruffet") {
            HVIS { "sann".minstEnHarTruffet() }
            SÅ {
                RETURNER(this)
            }
        }
    }
}