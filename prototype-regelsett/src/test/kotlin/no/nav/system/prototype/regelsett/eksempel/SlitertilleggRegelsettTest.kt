package no.nav.system.prototype.regelsett.eksempel

import no.nav.system.prototype.regelsett.Faktum
import no.nav.system.prototype.regelsett.forklar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SlitertilleggRegelsettTest {

    private val G = Faktum("G", 118620)

    private fun beregn(antallMnd: Int, trygdetid: Int?): Faktum<Double> =
        beregnSlitertilleggRS(
            Faktum("antallMnd", antallMnd),
            trygdetid?.let { Faktum("trygdetid", it) },
            G,
        ).also { println(it.forklar()); println() }

    @Test
    fun `manglende trygdetid gir null via kjedet regel`() {
        val s = beregn(antallMnd = 12, trygdetid = null)
        assertEquals(0.0, s.verdi)
        val forklaring = s.forklar()
        assertTrue("regel: MANGLER-TRYGDETID" in forklaring)
        assertTrue("betingelse: harTrygdetid = false" in forklaring)
        assertFalse("FULL-TRYGDETID" in forklaring)
        assertFalse("REDUSERT-TRYGDETID" in forklaring)
        assertFalse("teknisk betingelse ikke oppfylt" in forklaring)
    }

    @Test
    fun `tidlig uttak med redusert trygdetid`() {
        val s = beregn(antallMnd = 12, trygdetid = 30)
        assertEquals(1235.625, s.verdi, 1e-9)
        assertEquals("BEREGN-SLITERTILLEGG", s.regel.navn)
        val forklaring = s.forklar()
        assertTrue("UTTAK-TIDLIG" in forklaring)
        assertTrue("REDUSERT-TRYGDETID" in forklaring)
        assertTrue("regel: BEREGN-SLITERTILLEGG" in forklaring)
        assertTrue("referanse: §3-2 Folketrygdloven https://lovdata.no" in forklaring)
        assertFalse("betingelse: true" in forklaring)
    }

    @Test
    fun `tidlig uttak med full trygdetid`() {
        val s = beregn(antallMnd = 12, trygdetid = 40)
        assertEquals(1647.5, s.verdi, 1e-9)
        assertTrue("FULL-TRYGDETID" in s.forklar())
        assertFalse("REDUSERT-TRYGDETID" in s.forklar())
    }

    @Test
    fun `sent uttak gir null`() {
        val s = beregn(antallMnd = 40, trygdetid = 30)
        assertEquals(0.0, s.verdi)
        assertTrue("UTTAK-SENT" in s.forklar())
    }
}
