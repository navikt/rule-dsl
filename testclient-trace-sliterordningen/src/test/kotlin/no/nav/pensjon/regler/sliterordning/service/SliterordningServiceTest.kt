package no.nav.pensjon.regler.sliterordning.service

import no.nav.pensjon.regler.sliterordning.domain.NormertPensjonsalder
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.domain.Trygdetid
import no.nav.pensjon.regler.sliterordning.to.SliterordningRequest
import no.nav.pensjon.regler.sliterordning.to.SliterordningResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.YearMonth

class SliterordningServiceTest {

    private fun person(fodselsdato: YearMonth, trygdetidMnd: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidMnd), NormertPensjonsalder.default())

    @Test
    fun `ruleflow innvilger slitertillegg når vilkår er oppfylt`() {
        // Arrange: Create test data
        val fødselsdato = YearMonth.of(1958, 1)
        val uttakstidspunkt = YearMonth.of(2020, 2)  // 62 years + 1 month
        val virkningstidspunkt = YearMonth.of(2020, 2)

        val person = Person(
            fødselsdato = fødselsdato,
            trygdetid = Trygdetid(faktiskTrygdetid = 40),
            normertPensjonsalder = NormertPensjonsalder.default()
        )

        // Act: Run the service
        val (result, trace) = SliterordningService(
            SliterordningRequest(
                uttakstidspunkt = uttakstidspunkt,
                virkningstidspunkt = virkningstidspunkt,
                person = person
            )
        ).run()

        // Assert: Verify result is Innvilget
        assertTrue(result is SliterordningResponse.Innvilget, "Expected Innvilget result")

        val innvilget = result as SliterordningResponse.Innvilget
        assertTrue(innvilget.slitertillegg.value > 0.0, "Expected positive slitertillegg amount")

        // Print trace for debugging
        println("=== Sliterordning Trace ===")
        println(trace.debugTree())
    }

    @Test
    fun `trace shows rule evaluations and faktum values`() {
        // Arrange
        val fødselsdato = YearMonth.of(1958, 1)
        val uttakstidspunkt = YearMonth.of(2020, 2)
        val virkningstidspunkt = YearMonth.of(2020, 2)

        val person = Person(
            fødselsdato = fødselsdato,
            trygdetid = Trygdetid(faktiskTrygdetid = 40),
            normertPensjonsalder = NormertPensjonsalder.default()
        )

        // Act
        val (result, trace) = SliterordningService(
            SliterordningRequest(
                uttakstidspunkt = uttakstidspunkt,
                virkningstidspunkt = virkningstidspunkt,
                person = person
            )
        ).run()

        // Assert
        assertTrue(result is SliterordningResponse.Innvilget)
        
        val traceOutput = trace.debugTree()
        
        // Should contain key rules
        assertTrue(traceOutput.contains("Vilkårsprøving slitertillegg"), "Should show vilkårsprøving rule")
        assertTrue(traceOutput.contains("Innvilget slitertillegg"), "Should show branch decision rule")
        assertTrue(traceOutput.contains("SLITERTILLEGG-JUSTERING"), "Should show beregning rules")
        
        println("=== Full Trace Output ===")
        println(traceOutput)
    }

    @Test
    fun `early withdrawal gets full justeringsfaktor`() {
        // Person takes pension immediately at nedre pensjonsdato (0 months after)
        val fødselsdato = YearMonth.of(1958, 1)
        val uttakstidspunkt = YearMonth.of(2020, 2)  // Right at nedre pensjonsdato
        val virkningstidspunkt = YearMonth.of(2020, 2)

        val person = Person(
            fødselsdato = fødselsdato,
            trygdetid = Trygdetid(faktiskTrygdetid = 40),
            normertPensjonsalder = NormertPensjonsalder.default()
        )

        val (result, trace) = SliterordningService(
            SliterordningRequest(
                uttakstidspunkt = uttakstidspunkt,
                virkningstidspunkt = virkningstidspunkt,
                person = person
            )
        ).run()

        assertTrue(result is SliterordningResponse.Innvilget)
        val innvilget = result as SliterordningResponse.Innvilget
        
        // With full trygdetid (40) and early withdrawal (0 months), should get maximum amount
        // fulltSlitertillegg = avrund2desimal(0.25 * G / 12) where G = 120000 (2020 rate)
        // justeringsFaktor = (36 - 0) / 36 = 1.0
        // trygdetidFaktor = 40 / 40 = 1.0
        // slitertillegg should be significant (> 2000)
        assertTrue(innvilget.slitertillegg.value > 2000, "Should get high slitertillegg for early withdrawal, got: ${innvilget.slitertillegg.value}")
        
        println("=== Early Withdrawal Trace ===")
        println(trace.debugTree())
    }

    @Test
    fun `late withdrawal gets zero justeringsfaktor`() {
        // Person takes pension 36+ months after nedre pensjonsdato
        val fødselsdato = YearMonth.of(1958, 1)
        val uttakstidspunkt = YearMonth.of(2023, 3)  // 36+ months after nedre
        val virkningstidspunkt = YearMonth.of(2023, 3)

        val person = Person(
            fødselsdato = fødselsdato,
            trygdetid = Trygdetid(faktiskTrygdetid = 40),
            normertPensjonsalder = NormertPensjonsalder.default()
        )

        val (result, trace) = SliterordningService(
            SliterordningRequest(
                uttakstidspunkt = uttakstidspunkt,
                virkningstidspunkt = virkningstidspunkt,
                person = person
            )
        ).run()

        assertTrue(result is SliterordningResponse.Innvilget)
        val innvilget = result as SliterordningResponse.Innvilget
        
        // With late withdrawal, justeringsFaktor = 0, so slitertillegg = 0
        assertEquals(0.0, innvilget.slitertillegg.value, "Should get zero slitertillegg for late withdrawal")
        
        println("=== Late Withdrawal Trace ===")
        println(trace.debugTree())
    }
}
