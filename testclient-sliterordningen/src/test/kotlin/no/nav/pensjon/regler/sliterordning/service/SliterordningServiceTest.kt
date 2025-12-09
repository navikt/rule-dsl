package no.nav.pensjon.regler.sliterordning.service

import no.nav.pensjon.regler.sliterordning.domain.NormertPensjonsalder
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.domain.Trygdetid
import no.nav.pensjon.regler.sliterordning.to.SliterordningRequest
import no.nav.pensjon.regler.sliterordning.to.SliterordningResponse
import no.nav.system.ruledsl.core.forklaring.forklar
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.YearMonth

/**
 * WIP
 */
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
            normertPensjonsalder = NormertPensjonsalder.Companion.default()
        )

        // Act: Run the ruleflow through the service (which provides necessary resources)
        val result = SliterordningService(
            SliterordningRequest(
                uttakstidspunkt = uttakstidspunkt,
                virkningstidspunkt = virkningstidspunkt,
                person = person
            )
        ).run()

        // Assert: Verify result is Innvilget (since VilkårsprøvSlitertilleggRS always returns true)
        assertTrue(result is SliterordningResponse.Innvilget, "Expected Innvilget result")

        val innvilget = result as SliterordningResponse.Innvilget
        assertTrue(innvilget.slitertillegg.verdi > 0.0, "Expected positive slitertillegg amount")

        val txt = innvilget.slitertillegg.forklar()
        println(txt)
        val x = 0
    }

    @Test
    fun `branch condition faktum should recursively show its ruleset origin`() {
        // Arrange: Create test data
        val fødselsdato = YearMonth.of(1958, 1)
        val uttakstidspunkt = YearMonth.of(2020, 2)
        val virkningstidspunkt = YearMonth.of(2020, 2)

        val person = Person(
            fødselsdato = fødselsdato,
            trygdetid = Trygdetid(faktiskTrygdetid = 40),
            normertPensjonsalder = NormertPensjonsalder.default()
        )

        // Act: Run the service
        val service = SliterordningService(
            SliterordningRequest(
                uttakstidspunkt = uttakstidspunkt,
                virkningstidspunkt = virkningstidspunkt,
                person = person
            )
        )
        val result = service.run()

        // Assert
        assertTrue(result is SliterordningResponse.Innvilget)

        val innvilget = result as SliterordningResponse.Innvilget
        // The slitertillegg Faktum's HVORFOR trace should include vilkårStatus with its origin
        val explanation = innvilget.slitertillegg.forklar()
        println(explanation)


        // Verify that the trace recursively includes the vilkårStatus Faktum from the branch
        assertTrue(
            explanation.contains("Vilkår Slitertillegg"),
            "Should show the vilkårStatus Faktum in the HVORFOR trace (branch condition)"
        )
        assertTrue(
            explanation.contains("VilkårsprøvSlitertilleggRS"),
            "Should recursively show the origin ruleset of vilkårStatus"
        )
        assertTrue(
            explanation.contains("ALLTID-INNVILGET"),
            "Should show the specific rule that created vilkårStatus"
        )
    }

}
