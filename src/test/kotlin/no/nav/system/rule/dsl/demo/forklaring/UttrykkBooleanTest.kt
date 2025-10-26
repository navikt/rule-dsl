package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tester for Boolean-uttrykk, sammenligninger og Hvis-logikk.
 */
class UttrykkBooleanTest {

    // ========================================================================
    // Logiske operatorer: OG, ELLER, IKKE
    // ========================================================================

    @Test
    fun `Og skal evaluere til true når begge sider er true`() {
        val a = Grunnlag("a", Const(true))
        val b = Grunnlag("b", Const(true))
        val uttrykk = a og b

        assertTrue(uttrykk.evaluer())
        assertEquals("a OG b", uttrykk.notasjon())
        assertEquals("true OG true", uttrykk.konkret())
    }

    @Test
    fun `Og skal evaluere til false når en side er false`() {
        val a = Grunnlag("a", Const(true))
        val b = Grunnlag("b", Const(false))
        val uttrykk = a og b

        assertFalse(uttrykk.evaluer())
        assertEquals("a OG b", uttrykk.notasjon())
    }

    @Test
    fun `Eller skal evaluere til true når minst en side er true`() {
        val a = Grunnlag("a", Const(false))
        val b = Grunnlag("b", Const(true))
        val uttrykk = a eller b

        assertTrue(uttrykk.evaluer())
        assertEquals("a ELLER b", uttrykk.notasjon())
    }

    @Test
    fun `Eller skal evaluere til false når begge sider er false`() {
        val a = Grunnlag("a", Const(false))
        val b = Grunnlag("b", Const(false))
        val uttrykk = a eller b

        assertFalse(uttrykk.evaluer())
        assertEquals("a ELLER b", uttrykk.notasjon())
    }

    @Test
    fun `Ikke skal negere Boolean verdi`() {
        val a = Grunnlag("a", Const(true))
        val uttrykk = ikke(a)

        assertFalse(uttrykk.evaluer())
        assertEquals("IKKE a", uttrykk.notasjon())
    }

    @Test
    fun `Komplekst logisk uttrykk med parenteser`() {
        val a = Grunnlag("a", Const(true))
        val b = Grunnlag("b", Const(false))
        val c = Grunnlag("c", Const(true))

        // (a OG b) ELLER c
        val uttrykk = (a og b) eller c

        assertTrue(uttrykk.evaluer())
        assertEquals("(a OG b) ELLER c", uttrykk.notasjon())
    }

    // ========================================================================
    // Sammenligninger
    // ========================================================================

    @Test
    fun `erLik skal sammenligne verdier`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(10))
        val uttrykk = a erLik b

        assertTrue(uttrykk.evaluer())
        assertEquals("a = b", uttrykk.notasjon())
        assertEquals("10 = 10", uttrykk.konkret())
    }

    @Test
    fun `erUlik skal detektere ulike verdier`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val uttrykk = a erUlik b

        assertTrue(uttrykk.evaluer())
        assertEquals("a ≠ b", uttrykk.notasjon())
    }

    @Test
    fun `erStørreEnn skal sammenligne tall`() {
        val a = Grunnlag("a", Const(20))
        val b = Grunnlag("b", Const(10))
        val uttrykk = a erStørreEnn b

        assertTrue(uttrykk.evaluer())
        assertEquals("a > b", uttrykk.notasjon())
    }

    @Test
    fun `erMindreEnn skal sammenligne tall`() {
        val a = Grunnlag("a", Const(5))
        val b = Grunnlag("b", Const(10))
        val uttrykk = a erMindreEnn b

        assertTrue(uttrykk.evaluer())
        assertEquals("a < b", uttrykk.notasjon())
    }

    @Test
    fun `erStørreEllerLik skal håndtere lik verdi`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(10))
        val uttrykk = a erStørreEllerLik b

        assertTrue(uttrykk.evaluer())
        assertEquals("a ≥ b", uttrykk.notasjon())
    }

    @Test
    fun `erMindreEllerLik skal håndtere lik verdi`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(10))
        val uttrykk = a erMindreEllerLik b

        assertTrue(uttrykk.evaluer())
        assertEquals("a ≤ b", uttrykk.notasjon())
    }

    @Test
    fun `sammenligning med konstant`() {
        val inntektsavvik = Grunnlag("inntektsavvik", Const(100))
        val uttrykk = inntektsavvik erMindreEnn 1000

        assertTrue(uttrykk.evaluer())
        assertEquals("inntektsavvik < 1000", uttrykk.notasjon())
    }

    // ========================================================================
    // Kombinerte Boolean-uttrykk med sammenligninger
    // ========================================================================

    @Test
    fun `kombinert sammenligning og logikk`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val c = Grunnlag("c", Const(15))

        // a < c OG c < b
        val uttrykk = (a erMindreEnn c) og (c erMindreEnn b)

        assertTrue(uttrykk.evaluer())
        assertEquals("a < c OG c < b", uttrykk.notasjon())
    }

    @Test
    fun `AFP avvik under sats eksempel`() {
        val inntektsavvik = Grunnlag("inntektsavvik", Const(100))
        val avviksSats = Grunnlag("avviksSats", Const(1000))

        // inntektsavvik >= 0 OG inntektsavvik <= avviksSats
        val avvikUnderSats = (inntektsavvik erStørreEllerLik 0) og (inntektsavvik erMindreEllerLik avviksSats)

        assertTrue(avvikUnderSats.evaluer())
        assertEquals("inntektsavvik ≥ 0 OG inntektsavvik ≤ avviksSats", avvikUnderSats.notasjon())
    }

    @Test
    fun `navngitt Boolean-uttrykk med id`() {
        val inntektsavvik = Grunnlag("inntektsavvik", Const(100))
        val avviksSats = Grunnlag("avviksSats", Const(1000))

        val avvikUnderSats: Grunnlag<Boolean> = ((inntektsavvik erStørreEllerLik 0) og (inntektsavvik erMindreEllerLik avviksSats))
            .navngi("avvikUnderSats")
            .id("AVVIK-UNDER-SATS")

        assertTrue(avvikUnderSats.evaluer())
        assertEquals("avvikUnderSats", avvikUnderSats.notasjon())
        assertEquals("AVVIK-UNDER-SATS", avvikUnderSats.rvsId)
    }

    // ========================================================================
    // Hvis-uttrykk
    // ========================================================================

    @Test
    fun `Hvis skal velge SÅ-gren når betingelse er sann`() {
        val betingelse = Grunnlag("betingelse", Const(true))
        val uttrykk = hvis(
            betingelse = betingelse,
            så = { Const(100) },
            ellers = { Const(200) }
        )

        assertEquals(100, uttrykk.evaluer())
        assertEquals("HVIS betingelse SÅ 100 ELLERS 200", uttrykk.notasjon())
        assertEquals("HVIS true SÅ 100", uttrykk.konkret())  // Viser kun valgt gren
    }

    @Test
    fun `Hvis skal velge ELLERS-gren når betingelse er usann`() {
        val betingelse = Grunnlag("betingelse", Const(false))
        val uttrykk = hvis(
            betingelse = betingelse,
            så = { Const(100) },
            ellers = { Const(200) }
        )

        assertEquals(200, uttrykk.evaluer())
        assertEquals("HVIS betingelse SÅ 100 ELLERS 200", uttrykk.notasjon())
        assertEquals("HVIS false ELLERS 200", uttrykk.konkret())  // Viser kun valgt gren
    }

    @Test
    fun `Hvis med sammenligning som betingelse`() {
        val trygdetid = Grunnlag("trygdetid", Const(35))
        val fullTrygdetid = Grunnlag("fullTrygdetid", Const(40))

        val resultat = hvis(
            betingelse = trygdetid erStørreEllerLik fullTrygdetid,
            så = { Const("Full pensjon") },
            ellers = { Const("Redusert pensjon") }
        )

        assertEquals("Redusert pensjon", resultat.evaluer())
    }

    @Test
    fun `Hvis med navngitt resultat og id`() {
        val inntektsavvik = Grunnlag("inntektsavvik", Const(500))

        val kategori: Grunnlag<String> = hvis(
            betingelse = inntektsavvik erMindreEllerLik 1000,
            så = { Const("AVVIK_UNDER_SATS") },
            ellers = { Const("ANDRE_AVVIK") }
        )
            .navngi("kategori")
            .id("AFP-KATEGORI")

        assertEquals("AVVIK_UNDER_SATS", kategori.evaluer())
        assertEquals("kategori", kategori.notasjon())
        assertEquals("AFP-KATEGORI", kategori.rvsId)
    }

    @Test
    fun `Nøstede Hvis-uttrykk`() {
        val verdi = Grunnlag("verdi", Const(15))

        val resultat = hvis(
            betingelse = verdi erMindreEnn 10,
            så = { Const("Lav") },
            ellers = {
                hvis(
                    betingelse = verdi erMindreEnn 20,
                    så = { Const("Middels") },
                    ellers = { Const("Høy") }
                )
            }
        )

        assertEquals("Middels", resultat.evaluer())
    }

    @Test
    fun `Hvis med Number-resultat`() {
        val alder = Grunnlag("alder", Const(67))

        val pensjon: Hvis<Int> = hvis(
            betingelse = alder erStørreEllerLik 67,
            så = { Const(300000) },
            ellers = { Const(0) }
        )

        assertEquals(300000, pensjon.evaluer())
    }

    // ========================================================================
    // Grunnlag med Boolean-typer
    // ========================================================================

    @Test
    fun `grunnlagListe skal inkludere Boolean-grunnlag`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val sammenligning = a erMindreEnn b
        val grunnlagListe = sammenligning.grunnlagListe()

        assertEquals(2, grunnlagListe.size)
        assertTrue(grunnlagListe.any { it.navn == "a" })
        assertTrue(grunnlagListe.any { it.navn == "b" })
    }

    @Test
    fun `dybde skal beregnes korrekt for Boolean-uttrykk`() {
        val a = Grunnlag("a", Const(true))
        val b = Grunnlag("b", Const(false))

        assertEquals(1, a.dybde())
        assertEquals(2, (a og b).dybde())
        assertEquals(2, ikke(a).dybde())
    }

    @Test
    fun `visitor skal traverse Boolean-tre`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val uttrykk = (a erStørreEnn b) og (b erMindreEnn 30)

        val typer = uttrykk.visit { expr ->
            listOf(expr::class.simpleName ?: "Unknown")
        }

        assertTrue(typer.contains("Og"))
        assertTrue(typer.contains("StørreEnn"))
        assertTrue(typer.contains("MindreEnn"))
    }

    @Test
    fun `forenkel skal evaluere konstante Boolean-subtre`() {
        val konstant1 = Const(true)
        val konstant2 = Const(false)

        val uttrykk = konstant1 og konstant2
        val forenklet = uttrykk.forenkel()

        assertTrue(forenklet is Const)
        assertFalse((forenklet as Const).verdi as Boolean)
    }

    @Test
    fun `forenkel skal evaluere konstant Hvis-betingelse`() {
        val uttrykk = hvis(
            betingelse = Const(true),
            så = { Const(100) },
            ellers = { Const(200) }
        )

        val forenklet = uttrykk.forenkel()

        assertTrue(forenklet is Const)
        assertEquals(100, (forenklet as Const).verdi)
    }

    // ========================================================================
    // Komplekse realistiske eksempler
    // ========================================================================

    @Test
    fun `AFP kategori eksempel med flere betingelser`() {
        val inntektsavvik = Grunnlag("inntektsavvik", Const(1500))
        val avviksSats = Grunnlag("avviksSats", Const(1000))
        val IIAP = Grunnlag("IIAP", Const(50000))
        val FPI = Grunnlag("FPI", Const(48000))
        val UtbetaltAFP = Grunnlag("UtbetaltAFP", Const(5000))

        // Avvik under sats
        val avvikUnderSats = ((inntektsavvik erStørreEllerLik 0) og (inntektsavvik erMindreEllerLik avviksSats))
            .navngi("avvikUnderSats")

        // Positivt avvik
        val positivtAvvik = ((inntektsavvik erStørreEnn avviksSats) og
                            (IIAP erStørreEnn FPI) og
                            (UtbetaltAFP erStørreEnn 0))
            .navngi("positivtAvvik")

        // Negativt avvik
        val negativtAvvik = ((inntektsavvik erStørreEnn avviksSats) og (FPI erStørreEnn IIAP))
            .navngi("negativtAvvik")

        // Test
        assertFalse(avvikUnderSats.evaluer())
        assertTrue(positivtAvvik.evaluer())
        assertFalse(negativtAvvik.evaluer())
    }

    @Test
    fun `slitertillegg med betinget verdi basert på trygdetid`() {
        val faktiskTrygdetid = Grunnlag("faktiskTrygdetid", Const(35))
        val fullTrygdetid = Grunnlag("FULL_TRYGDETID", Const(40))
        val fulltBeløp = Grunnlag("fulltBeløp", Const(100000.0))

        val slitertillegg: Grunnlag<Double> = hvis(
            betingelse = faktiskTrygdetid erStørreEllerLik fullTrygdetid,
            så = { fulltBeløp },
            ellers = { fulltBeløp * faktiskTrygdetid / fullTrygdetid }
        )
            .navngi("slitertillegg")
            .id("SLITERTILLEGG")

        assertEquals(87500.0, slitertillegg.evaluer(), 0.01)
        assertEquals("SLITERTILLEGG", slitertillegg.rvsId)
    }

    // ========================================================================
    // Fluent syntax med .så og .ellers
    // ========================================================================

    @Test
    fun `fluent syntax med så og ellers skal være identisk med hvis funksjon`() {
        val alder = Grunnlag("alder", Const(65))

        // Gammel syntax
        val pensjonHvis = hvis(
            betingelse = alder erStørreEllerLik 67,
            så = { Const(300000) },
            ellers = { Const(0) }
        )

        // Ny fluent syntax
        val pensjonFluent = (alder erStørreEllerLik 67)
            .så { Const(300000) }
            .ellers { Const(0) }

        // Verifiser at begge gir samme resultat
        assertEquals(pensjonHvis.evaluer(), pensjonFluent.evaluer())
        assertEquals(0, pensjonHvis.evaluer())
        assertEquals(0, pensjonFluent.evaluer())

        // Verifiser at begge har samme notasjon
        assertEquals(pensjonHvis.notasjon(), pensjonFluent.notasjon())
    }

    @Test
    fun `fluent syntax med nøstet hvis skal fungere`() {
        val trygdetid = Grunnlag("trygdetid", Const(25))

        // Gammel syntax
        val pensjonstypeHvis = hvis(
            betingelse = trygdetid erStørreEllerLik 40,
            så = { Const("Full pensjon") },
            ellers = {
                hvis(
                    betingelse = trygdetid erStørreEllerLik 20,
                    så = { Const("Redusert pensjon") },
                    ellers = { Const("Ingen pensjon") }
                )
            }
        )

        // Ny fluent syntax
        val pensjonstypeFluent = (trygdetid erStørreEllerLik 40)
            .så { Const("Full pensjon") }
            .ellers {
                (trygdetid erStørreEllerLik 20)
                    .så { Const("Redusert pensjon") }
                    .ellers { Const("Ingen pensjon") }
            }

        // Verifiser at begge gir samme resultat
        assertEquals("Redusert pensjon", pensjonstypeHvis.evaluer())
        assertEquals(pensjonstypeHvis.evaluer(), pensjonstypeFluent.evaluer())
    }

    @Test
    fun `fluent syntax skal fungere med navngiving og id`() {
        val alder = Grunnlag("alder", Const(70))

        val pensjon: Grunnlag<Int> = (alder erStørreEllerLik 67)
            .så { Const(300000) }
            .ellers { Const(0) }
            .navngi("pensjon")
            .id("PENSJON-BEREGNET")

        assertEquals(300000, pensjon.evaluer())
        assertEquals("pensjon", pensjon.navn)
        assertEquals("PENSJON-BEREGNET", pensjon.rvsId)
    }
}
