package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Test for dato-sammenligningsoperatorer i Uttrykk.
 */
class UttrykkDatoTest {

    private val dato1 = LocalDate.of(2020, 1, 1)
    private val dato2 = LocalDate.of(2021, 1, 1)
    private val dato3 = LocalDate.of(2021, 1, 1) // Samme som dato2

    @Test
    fun `erEtter med Grunnlag`() {
        val tidligereDato = Grunnlag("tidligereDato", Const(dato1))
        val senereDato = Grunnlag("senereDato", Const(dato2))

        val resultat = senereDato erEtter tidligereDato

        assertTrue(resultat.evaluer())
        assertEquals("senereDato > tidligereDato", resultat.notasjon())
        assertEquals("2021-01-01 > 2020-01-01", resultat.konkret())
    }

    @Test
    fun `erEtter med Const og LocalDate`() {
        val dato = Grunnlag("dato", Const(dato2))

        val resultat = dato erEtter dato1

        assertTrue(resultat.evaluer())
        assertEquals("dato > 2020-01-01", resultat.notasjon())
    }

    @Test
    fun `erEtter returnerer false når datoer er like`() {
        val dato = Grunnlag("dato", Const(dato2))

        val resultat = dato erEtter dato3

        assertFalse(resultat.evaluer())
    }

    @Test
    fun `erEtterEllerLik med Grunnlag`() {
        val dato = Grunnlag("dato", Const(dato2))
        val sammenligningsDato = Grunnlag("sammenligningsDato", Const(dato3))

        val resultat = dato erEtterEllerLik sammenligningsDato

        assertTrue(resultat.evaluer())
        assertEquals("dato ≥ sammenligningsDato", resultat.notasjon())
    }

    @Test
    fun `erEtterEllerLik returnerer true når datoer er like`() {
        val dato = Grunnlag("dato", Const(dato2))

        val resultat = dato erEtterEllerLik dato3

        assertTrue(resultat.evaluer())
    }

    @Test
    fun `erEtterEllerLik returnerer true når dato er senere`() {
        val dato = Grunnlag("dato", Const(dato2))

        val resultat = dato erEtterEllerLik dato1

        assertTrue(resultat.evaluer())
    }

    @Test
    fun `erEtterEllerLik returnerer false når dato er tidligere`() {
        val dato = Grunnlag("dato", Const(dato1))

        val resultat = dato erEtterEllerLik dato2

        assertFalse(resultat.evaluer())
    }

    @Test
    fun `erFør med Grunnlag`() {
        val tidligereDato = Grunnlag("tidligereDato", Const(dato1))
        val senereDato = Grunnlag("senereDato", Const(dato2))

        val resultat = tidligereDato erFør senereDato

        assertTrue(resultat.evaluer())
        assertEquals("tidligereDato < senereDato", resultat.notasjon())
        assertEquals("2020-01-01 < 2021-01-01", resultat.konkret())
    }

    @Test
    fun `erFør returnerer false når datoer er like`() {
        val dato = Grunnlag("dato", Const(dato2))

        val resultat = dato erFør dato3

        assertFalse(resultat.evaluer())
    }

    @Test
    fun `erFørEllerLik med Grunnlag`() {
        val tidligereDato = Grunnlag("tidligereDato", Const(dato1))
        val senereDato = Grunnlag("senereDato", Const(dato2))

        val resultat = tidligereDato erFørEllerLik senereDato

        assertTrue(resultat.evaluer())
        assertEquals("tidligereDato ≤ senereDato", resultat.notasjon())
    }

    @Test
    fun `erFørEllerLik returnerer true når datoer er like`() {
        val dato = Grunnlag("dato", Const(dato2))

        val resultat = dato erFørEllerLik dato3

        assertTrue(resultat.evaluer())
    }

    @Test
    fun `dato-operatorer i betinget uttrykk`() {
        val idag = Grunnlag("idag", Const(LocalDate.of(2021, 6, 15)))
        val frist = Grunnlag("frist", Const(LocalDate.of(2021, 12, 31)))

        val resultat = (idag erFørEllerLik frist)
            .så { Const("INNENFOR_FRIST") }
            .ellers { Const("UTGÅTT") }

        assertEquals("INNENFOR_FRIST", resultat.evaluer())
    }

    @Test
    fun `dato-operatorer i logisk OG uttrykk`() {
        val fødselsdato = Grunnlag("fødselsdato", Const(LocalDate.of(1957, 5, 17)))
        val virkningsdato = Grunnlag("virkningsdato", Const(LocalDate.of(2024, 6, 1)))

        val aldersgrense = LocalDate.of(1960, 1, 1)
        val tidligsteVirk = LocalDate.of(2024, 1, 1)

        val oppfyllerKrav =
            (fødselsdato erFør aldersgrense) og
            (virkningsdato erEtterEllerLik tidligsteVirk)

        assertTrue(oppfyllerKrav.evaluer())
    }

    @Test
    fun `dato-operatorer med navngi og id`() {
        val dato67mnd = Grunnlag("dato67mnd", Const(LocalDate.of(2024, 6, 1)))
        val virk = Grunnlag("virk", Const(LocalDate.of(2024, 7, 1)))

        val sammenligning = (virk erEtterEllerLik dato67mnd)
            .navngi("virkErEtterEllerLikDato67m")
            .id("VIRK_ETTER_ELLER_LIK_DATO_67M")

        assertEquals("virkErEtterEllerLikDato67m", sammenligning.navn)
        assertEquals("VIRK_ETTER_ELLER_LIK_DATO_67M", sammenligning.rvsId)
        assertTrue(sammenligning.evaluer())
    }

    @Test
    fun `forklar med dato-operatorer`() {
        val fødselsdato = Grunnlag("fødselsdato", Const(LocalDate.of(1957, 5, 17)))
        val aldersgrense = Grunnlag("aldersgrense", Const(LocalDate.of(1960, 1, 1)))

        val uttrykk = fødselsdato erFør aldersgrense
        val forklaring = uttrykk.forklar("erFødtFørGrense")

        assertTrue(forklaring.hvaForklaring.symbolskUttrykk.contains("<"))
        assertEquals(true, forklaring.hvaForklaring.resultat)
    }

    @Test
    fun `LocalDate kan brukes direkte på venstre side`() {
        val dato = LocalDate.of(2020, 1, 1)
        val sammenligningsDato = Grunnlag("sammenligningsDato", Const(LocalDate.of(2021, 1, 1)))

        val resultat = dato erFør sammenligningsDato

        assertTrue(resultat.evaluer())
        assertEquals("2020-01-01 < sammenligningsDato", resultat.notasjon())
    }

    @Test
    fun `kompleks dato-sammenligning med flere operatorer`() {
        val fødselsdato = Grunnlag("fødselsdato", Const(LocalDate.of(1957, 5, 17)))
        val virk = Grunnlag("virk", Const(LocalDate.of(2024, 7, 1)))
        val dato67år = Const(fødselsdato.evaluer().plusYears(67))

        val erGammelNok = virk erEtterEllerLik dato67år

        assertTrue(erGammelNok.evaluer())
    }
}
