package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.TomtUtfall
import no.nav.system.rule.dsl.enums.UtfallType
import no.nav.system.rule.dsl.pattern.createPattern
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.erSann

class DomainPatternRS(
    inputFakta: List<Faktum<Boolean>>,
) : AbstractRuleset<List<Faktum<UtfallType>>>() {

    private val faktumListe = inputFakta.createPattern()
    private val utfallListe = listOf(TomtUtfall(), TomtUtfall(), TomtUtfall())

    @OptIn(DslDomainPredicate::class)
    override fun create() {

        regel("sann", faktumListe) { bool ->
            HVIS { bool.erSann() }
        }

//        regel("usann", faktumListe) { bool ->
//            HVIS { bool.erUsann() }
//        }

        regel("ingenHarTruffet") {
            HVIS { "sann".ingenHarTruffet() }
            SVAR { utfallListe[0] }
        }

        regel("minstEnHarTruffet") {
            HVIS { "sann".minstEnHarTruffet() }
            SVAR { utfallListe[1] }
        }

        regel("alleHarTruffet") {
            HVIS { "sann".alleHarTruffet() }
            SVAR { utfallListe[2] }
        }

        regel("returner utfall") {
            HVIS { true }
            RETURNER(utfallListe)
        }
    }
}