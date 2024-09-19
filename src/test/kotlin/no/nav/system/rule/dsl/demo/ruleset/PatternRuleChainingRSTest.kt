package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.demo.domain.Inntekt
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Test that should demonstrate rulechaining.
 * Rulechaining refers to having rules check if previous rules have fired, example [endreInntekt.harTruffet()]
 */
class PatternRuleChainingRSTest {

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