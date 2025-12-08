package no.nav.system.ruledsl.core.model

import no.nav.system.ruledsl.core.model.arc.AbstractRuleset
import no.nav.system.ruledsl.core.pattern.createPattern
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Test that should demonstrate rulechaining.
 * Rulechaining refers to having rules check if previous rules have fired, example [endreInntekt.harTruffet()]
 */
class PatternRuleChainingRSTest {
    /**
     * Test that should demonstrate rulechaining.
     * Rulechaining refers to having rules check if previous rules have fired, example [endreInntekt.harTruffet()]
     */
    class PatternRuleChainingRS(innInntektListe: List<Inntekt>) : AbstractRuleset<Boolean>() {

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

    class Inntekt(var beløp : Int)

    @Test
    fun `skal endre beløp for siste innslag i inntektslisten og returnere true`() {
        val inntektsListe = listOf(Inntekt(10), Inntekt(15), Inntekt(20))
        val patternRuleChainingRS = PatternRuleChainingRS(inntektsListe).test()

        assertTrue(patternRuleChainingRS)
        assertEquals(10, inntektsListe[0].beløp)
        assertEquals(15, inntektsListe[1].beløp)
        assertEquals(25, inntektsListe[2].beløp)
    }

    @Test
    fun `skal ikke endre noen innslag i inntektslisten og returnere false`() {
        val inntektsListe = listOf(Inntekt(5), Inntekt(10), Inntekt(15))
        val patternRuleChainingRS = PatternRuleChainingRS(inntektsListe).test()
//        val patternRuleChainingRS = PatternRuleChainingRS(inntektsListe).test().get()

        assertFalse(patternRuleChainingRS)
        assertEquals(5, inntektsListe[0].beløp)
        assertEquals(10, inntektsListe[1].beløp)
        assertEquals(15, inntektsListe[2].beløp)
    }

    @Test
    fun `skal ikke krasje dersom listen er tom`() {
        val inntektsListe = listOf<Inntekt>()
        val patternRuleChainingRS = PatternRuleChainingRS(inntektsListe).test()

        assertFalse(patternRuleChainingRS)
    }
}