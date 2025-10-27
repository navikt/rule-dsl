package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Test for konvertering fra Faktum til Grunnlag.
 */
class FaktumToGrunnlagTest {

    @Test
    fun `toGrunnlag konverterer Faktum til Grunnlag`() {
        val faktum = Faktum("alder", 67)
        val grunnlag = faktum.toGrunnlag()

        // Sjekk at navn og verdi er bevart
        assertEquals("alder", grunnlag.navn)
        assertEquals(67, grunnlag.evaluer())
        assertEquals("alder", grunnlag.notasjon())
        assertEquals("67", grunnlag.konkret())
    }

    @Test
    fun `toGrunnlag med heltall kan brukes i beregninger`() {
        val alder = Faktum("alder", 67)
        val grunnbeløp = Faktum("grunnbeløp", 110000)

        val beregning = alder.toGrunnlag() * grunnbeløp.toGrunnlag()

        assertEquals(7370000, beregning.evaluer())
        assertEquals("alder * grunnbeløp", beregning.notasjon())
    }

    @Test
    fun `toGrunnlag med LocalDate`() {
        val dato = Faktum("fødselsdato", LocalDate.of(1957, 5, 17))
        val grunnlag = dato.toGrunnlag()

        assertEquals("fødselsdato", grunnlag.navn)
        assertEquals(LocalDate.of(1957, 5, 17), grunnlag.evaluer())
    }

    @Test
    fun `toGrunnlag med String`() {
        val navn = Faktum("fornavn", "Ola")
        val grunnlag = navn.toGrunnlag()

        assertEquals("fornavn", grunnlag.navn)
        assertEquals("Ola", grunnlag.evaluer())
    }

    @Test
    fun `toGrunnlag kan brukes i sammenligninger`() {
        val alder = Faktum("alder", 67)
        val minstealder = Faktum("minstealder", 62)

        val sammenligning = alder.toGrunnlag() erStørreEllerLik minstealder.toGrunnlag()

        assertTrue(sammenligning.evaluer())
        assertEquals("alder ≥ minstealder", sammenligning.notasjon())
    }

    @Test
    fun `toGrunnlag kan brukes i erBlant`() {
        val status = Faktum("status", "AKTIV")
        val grunnlag = status.toGrunnlag()

        val uttrykk = grunnlag erBlant listOf("AKTIV", "PENDING", "COMPLETED")

        assertTrue(uttrykk.evaluer())
        assertEquals("status ER BLANT [AKTIV, PENDING, COMPLETED]", uttrykk.notasjon())
    }

    @Test
    fun `toGrunnlag kan brukes i betingede uttrykk`() {
        val alder = Faktum("alder", 67)
        val grunnlag = alder.toGrunnlag()

        val resultat = (grunnlag erStørreEllerLik 67)
            .så { Const("PENSJONIST") }
            .ellers { Const("ARBEIDSTAKER") }

        assertEquals("PENSJONIST", resultat.evaluer())
    }

    @Test
    fun `toGrunnlag med navngi og id`() {
        val alder = Faktum("alder", 67)
        val grunnlag = alder.toGrunnlag()

        val beregning = (grunnlag * 12)
            .navngi("alderIMåneder")
            .id("ALDER_I_MÅNEDER")

        assertEquals("alderIMåneder", beregning.navn)
        assertEquals("ALDER_I_MÅNEDER", beregning.rvsId)
        assertEquals(804, beregning.evaluer())
    }

    @Test
    fun `toGrunnlag bevarer strukturen for forklaring`() {
        val sats = Faktum("sats", 0.25)
        val grunnbeløp = Faktum("G", 110000)

        val beregning = sats.toGrunnlag() * grunnbeløp.toGrunnlag()
        val forklaring = beregning.forklar("resultat")

        assertEquals("sats * G", forklaring.hvaForklaring.symbolskUttrykk)
        assertEquals("0.25 * 110000", forklaring.hvaForklaring.konkretUttrykk)
        assertEquals(27500.0, forklaring.hvaForklaring.resultat)
    }

    @Test
    fun `toGrunnlag i komplekst uttrykk`() {
        val alder = Faktum("alder", 67)
        val inntekt = Faktum("inntekt", 250000)
        val fribeløp = Faktum("fribeløp", 300000)

        val harKravPåPensjon =
            (alder.toGrunnlag() erStørreEllerLik 67) og
            (inntekt.toGrunnlag() erMindreEnn fribeløp.toGrunnlag())

        assertTrue(harKravPåPensjon.evaluer())

        val notasjon = harKravPåPensjon.notasjon()
        assertTrue(notasjon.contains("alder"))
        assertTrue(notasjon.contains("inntekt"))
        assertTrue(notasjon.contains("fribeløp"))
    }
}
