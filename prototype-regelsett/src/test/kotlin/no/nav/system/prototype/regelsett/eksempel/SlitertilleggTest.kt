package no.nav.system.prototype.regelsett.eksempel

import no.nav.system.prototype.regelsett.DefaultRegel
import no.nav.system.prototype.regelsett.Faktum
import no.nav.system.prototype.regelsett.forklar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SlitertilleggTest {

    private val G = Faktum("G", 118620)
    private val trygdetid = Faktum("trygdetid", 30)

    @Test
    fun `tidlig uttak gir avkortet slitertillegg`() {
        val s = beregnSlitertillegg(Faktum("antallMnd", 12), trygdetid, G)
        println(s.forklar())
        assertEquals(1235.625, s.verdi)
        assertSame(DefaultRegel, s.regel)
    }

    @Test
    fun `Faktum i SÅ får regelen automatisk`() {
        val j = justeringsFaktor(Faktum("antallMnd", 40))
        println(j.forklar())
        assertEquals(0.0, j.verdi)
        assertEquals("UTTAK-SENT", j.regel.navn)
        assertFalse("UTTAK-TIDLIG" in j.forklar())
    }

    @Test
    fun `regelsett kan returnere flere faktum`() {
        val (mndIgjen, tidlig) = mndIgjenOgTidlig(Faktum("antallMnd", 12))
        assertEquals(24.0, mndIgjen.verdi)
        assertTrue(tidlig.verdi)
        assertEquals("TIDLIG", tidlig.regel.navn)
    }

    @Test
    fun `regelsett som ikke er Unit feiler når ingen regel returnerer`() {
        val e = assertThrows<IllegalStateException> { mndIgjenOgTidlig(Faktum("antallMnd", 40)) }
        assertEquals("Regelsett 'mndIgjenOgTidlig': ingen regel returnerte en verdi", e.message)
    }

    @Test
    fun `regler kobles bare via faktum de bruker`() {
        val v = Vedtak(Faktum("antallMnd", 12), G)
        fastsett(v)
        println(v.reduksjon!!.forklar())
        println()
        println(v.merknad!!.forklar())

        assertEquals(79080.0, v.reduksjon!!.verdi)
        assertFalse("UAVHENGIG" in v.reduksjon!!.forklar())
        assertFalse("UAVHENGIG" in v.merknad!!.forklar())
        assertTrue("UTTAK-TIDLIG" in v.merknad!!.forklar())
    }
}
