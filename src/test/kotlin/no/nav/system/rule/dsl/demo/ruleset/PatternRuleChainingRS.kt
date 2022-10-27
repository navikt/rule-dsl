package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.demo.domain.Inntekt
import no.nav.system.rule.dsl.pattern.createPattern

/**
 * Test that should demonstrate rulechaining.
 * Rulechaining refers to having rules check if previous rules have fired, example [endreInntekt.harTruffet()]
 */
class PatternRuleChainingRS(innInntektListe: List<Inntekt>) :
    AbstractRuleset<Boolean>() {

    private var noenInntektEtterUforhet = innInntektListe.createPattern()

    override fun create() {
        regel("endreInntekt", noenInntektEtterUforhet) {
            HVIS { it.beløp > 15 }
            SÅ {
                it.beløp = 25
            }
            kommentar("Regel som ser på hvert innslag av 'noenInntektEtterUforhet'.")
        }

        regel("harEndreInntektTruffet", noenInntektEtterUforhet) {
            HVIS { "endreInntekt".harTruffet(it) }
            SÅ {
                RETURNER(true)
            }
            kommentar("Hvis ett innslag i 'noenInntektEtterUforhet' har truffet regelen 'endreInntekt', vil det returneres true.")
        }

        regel("skalKunneRefererePatternregel") {
            HVIS { "endreInntekt".harTruffet() }
        }

        regel("defaultRegel") {
            HVIS { true }
            SÅ {
                RETURNER(false)
            }
            kommentar("Default regel som returnerer 'false' hvis ingen andre retur-regler treffer.")
        }
    }
}