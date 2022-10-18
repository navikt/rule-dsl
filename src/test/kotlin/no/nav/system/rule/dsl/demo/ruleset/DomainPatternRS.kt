package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.TomtUtfall
import no.nav.system.rule.dsl.Utfall
import no.nav.system.rule.dsl.pattern.createPattern
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.UtfallType.IKKE_OPPFYLT
import no.nav.system.rule.dsl.rettsregel.UtfallType.OPPFYLT
import no.nav.system.rule.dsl.rettsregel.erSann
import no.nav.system.rule.dsl.rettsregel.erUsann

class DomainPatternRS(
    inputFakta: MutableList<Faktum<Boolean>>
) : AbstractRuleset<Utfall>() {

    private val faktumListe = inputFakta.createPattern()
    private val utfallDemo = TomtUtfall()

    @OptIn(DslDomainPredicate::class)
    override fun create() {

        regel("sann", faktumListe) { bool ->
            HVIS { bool.erSann() }
        }

        regel("usann", faktumListe) { bool ->
            HVIS { bool.erUsann() }
        }

        regel("alleHarTruffet") {
            HVIS { "sann".alleHarTruffet() }
            SVAR { utfallDemo }
            RETURNER(utfallDemo)
        }
    }
}