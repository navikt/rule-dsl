package no.nav.pensjon.regler.rdsl2.sliterordning

import no.nav.system.rdsl2.Faktum
import no.nav.system.rdsl2.forklar
import no.nav.system.rdsl2.times
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BeregnSlitertilleggTest {

    private val trygdetid = Faktum("trygdetid", 30)
    private val G = Faktum("G", 118620)

    @Test
    fun `tidlig uttak gir justert slitertillegg`() {
        val slitertillegg = beregnSlitertillegg(Faktum("antallMnd", 18), trygdetid, G)
        println(slitertillegg.forklar())

        assertEquals(926.72, slitertillegg.verdi)
        assertEquals("AVKORTING-TRYGDETID", slitertillegg.regel?.navn)
        assertEquals(
            "avrund2(avrund2(G * 0.25 / 12) * justeringsFaktor * trygdetid / fullTrygdetid)",
            slitertillegg.uttrykk.notasjon()
        )
        assertEquals(
            "avrund2(avrund2(118620 * 0.25 / 12) * 0.5 * 30 / 40)",
            slitertillegg.uttrykk.konkret()
        )

        val justeringsFaktor = slitertillegg.uttrykk.grunnlag().single { it.navn == "justeringsFaktor" }
        assertEquals(0.5, justeringsFaktor.verdi)
        assertEquals("UTTAK-TIDLIG", justeringsFaktor.regel?.navn)
        assertEquals("antallMnd < 36", justeringsFaktor.regel?.uttrykk?.notasjon())
        assertEquals("18 < 36", justeringsFaktor.regel?.uttrykk?.konkret())
    }

    @Test
    fun `sent uttak gir ingen justering`() {
        val antallMnd = Faktum("antallMnd", 40)
        val slitertillegg = beregnSlitertillegg(antallMnd, trygdetid, G)
        println(slitertillegg.forklar())

        assertEquals(0.0, slitertillegg.verdi)

        val justeringsFaktor = slitertillegg.uttrykk.grunnlag().single { it.navn == "justeringsFaktor" }
        assertEquals("UTTAK-SENT", justeringsFaktor.regel?.navn)
        assertSame(antallMnd, justeringsFaktor.regel?.uttrykk?.grunnlag()?.single())
    }

    @Test
    fun `utledet faktum kan ikke lages utenfor en regel`() {
        assertThrows<IllegalArgumentException> { Faktum("feil", G * 0.25) }
    }
}
