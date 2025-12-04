package no.nav.pensjon.regler.sliterordning.service

import no.nav.pensjon.regler.sliterordning.domain.NormertPensjonsalder
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.domain.Trygdetid
import no.nav.pensjon.regler.sliterordning.to.BeregnSlitertilleggRequest
import no.nav.pensjon.regler.sliterordning.to.Response
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.YearMonth
import kotlin.run

class BeregnSlitertilleggServiceTest {

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
        val result = BeregnSlitertilleggService(
            BeregnSlitertilleggRequest(
                uttakstidspunkt = uttakstidspunkt,
                virkningstidspunkt = virkningstidspunkt,
                person = person
            )
        ).run()

        // Assert: Verify result is Innvilget (since VilkårsprøvSlitertilleggRS always returns true)
        Assertions.assertTrue(result is Response.Sliterordning.Innvilget, "Expected Innvilget result")

        val innvilget = result as Response.Sliterordning.Innvilget
        Assertions.assertTrue(innvilget.slitertillegg.slitertilleggBeregnet > 0.0, "Expected positive slitertillegg amount")

        val txt = innvilget.slitertillegg
    }
}