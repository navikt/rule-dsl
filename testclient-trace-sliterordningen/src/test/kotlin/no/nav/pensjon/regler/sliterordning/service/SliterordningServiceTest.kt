package no.nav.pensjon.regler.sliterordning.service

import no.nav.pensjon.regler.sliterordning.domain.NormertPensjonsalder
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.domain.Trygdetid
import no.nav.pensjon.regler.sliterordning.to.SliterordningRequest
import no.nav.pensjon.regler.sliterordning.to.SliterordningResponse
import no.nav.system.ruledsl.core.trace.debugTree
import no.nav.system.ruledsl.core.trace.forklar
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.YearMonth

class SliterordningServiceTest {

    private fun person(fodselsdato: YearMonth, trygdetidMnd: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidMnd), NormertPensjonsalder.default())

    @Test
    fun `ruleflow innvilger slitertillegg når vilkår er oppfylt`() {
        val (result, trace) = SliterordningService(
            SliterordningRequest(
                uttakstidspunkt = YearMonth.of(2020, 2),
                virkningstidspunkt = YearMonth.of(2020, 2),
                person = person(YearMonth.of(1958, 1), 40)
            )
        ).run()

        assertTrue(result is SliterordningResponse.Innvilget, "Expected Innvilget result")

        val innvilget = result as SliterordningResponse.Innvilget
        assertTrue(innvilget.slitertillegg.value > 0.0, "Expected positive slitertillegg amount")

        println("=== Sliterordning Trace ===")
        println(trace.debugTree())
    }

    @Test
    fun `trace shows rule evaluations and faktum values`() {
        val (result, trace) = SliterordningService(
            SliterordningRequest(
                uttakstidspunkt = YearMonth.of(2020, 2),
                virkningstidspunkt = YearMonth.of(2020, 2),
                person = person(YearMonth.of(1958, 1), 40)
            )
        ).run()

        assertTrue(result is SliterordningResponse.Innvilget)
        
        val traceOutput = trace.debugTree()
        
        assertTrue(traceOutput.contains("Vilkårsprøving slitertillegg"), "Should show vilkårsprøving rule")
        assertTrue(traceOutput.contains("Innvilget slitertillegg"), "Should show branch decision rule")
        assertTrue(traceOutput.contains("SLITERTILLEGG-JUSTERING"), "Should show beregning rules")
        
        println("=== Full Trace Output ===")
        println(traceOutput)
    }

    @Test
    fun `early withdrawal gets full justeringsfaktor`() {
        val (result, trace) = SliterordningService(
            SliterordningRequest(
                uttakstidspunkt = YearMonth.of(2020, 2),
                virkningstidspunkt = YearMonth.of(2020, 2),
                person = person(YearMonth.of(1958, 1), 40)
            )
        ).run()

        assertTrue(result is SliterordningResponse.Innvilget)
        val innvilget = result as SliterordningResponse.Innvilget
        
        // With full trygdetid (40) and early withdrawal (0 months), should get maximum amount
        assertTrue(innvilget.slitertillegg.value > 2000, "Should get high slitertillegg for early withdrawal, got: ${innvilget.slitertillegg.value}")
        
        println("=== Early Withdrawal Trace ===")
        println(trace.debugTree())
    }

    @Test
    fun `late withdrawal gets zero justeringsfaktor`() {
        val (result, trace) = SliterordningService(
            SliterordningRequest(
                uttakstidspunkt = YearMonth.of(2023, 3),
                virkningstidspunkt = YearMonth.of(2023, 3),
                person = person(YearMonth.of(1958, 1), 40)
            )
        ).run()

        assertTrue(result is SliterordningResponse.Innvilget)
        val innvilget = result as SliterordningResponse.Innvilget
        
        // With late withdrawal, justeringsFaktor = 0, so slitertillegg = 0
        assertEquals(0.0, innvilget.slitertillegg.value, "Should get zero slitertillegg for late withdrawal")
        
        println("=== Late Withdrawal Trace ===")
        println(trace.debugTree())
    }

    @Test
    fun `forklar produces inverse explanation starting from slitertillegg result`() {
        val (result, _) = SliterordningService(
            SliterordningRequest(
                uttakstidspunkt = YearMonth.of(2020, 2),
                virkningstidspunkt = YearMonth.of(2020, 2),
                person = person(YearMonth.of(1958, 1), 30)  // Partial trygdetid
            )
        ).run()

        assertTrue(result is SliterordningResponse.Innvilget)
        val innvilget = result as SliterordningResponse.Innvilget

        val explanation = innvilget.slitertillegg.forklar()
        
        println("=== Inverse Explanation (forklar) ===")
        println(explanation)

        assertTrue(explanation.contains("HVA"), "Should have HVA section")
        assertTrue(explanation.contains("slitertillegg"), "Should show result name")
        assertTrue(explanation.contains("HVORFOR"), "Should have HVORFOR section")
        assertTrue(explanation.contains("HVORDAN"), "Should have HVORDAN section")
        assertTrue(explanation.contains("avrund2desimal"), "Should show the rounding function")
    }
}
