package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*
import no.nav.system.rule.dsl.formel.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.erLik
import no.nav.system.rule.dsl.rettsregel.erMindreEnn
import no.nav.system.rule.dsl.rettsregel.erStørre
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ForklaringTest {

    @Test
    fun `enkel formel skal gi korrekt HVA forklaring`() {
        val G = Formel.variable("G", 110000)
        val forklaring = G.forklarHva()

        assertEquals("G", forklaring.navn)
        assertEquals("G", forklaring.symbolskUttrykk)
        assertEquals("110000", forklaring.konkretUttrykk)
        assertEquals(110000, forklaring.resultat)
    }

    @Test
    fun `sammensatt formel skal gi korrekt HVORDAN forklaring`() {
        val G = Formel.variable("G", 110000)
        val sats = Formel.variable("sats", 0.25)

        val beregning = FormelBuilder.create<Double>()
            .name("beregning")
            .expression(sats * G / 12)
            .build()

        val forklaring = beregning.forklar(maxDybde = 2)

        assertNotNull(forklaring)
        assertEquals("beregning", forklaring.hvaForklaring.navn)
        assertTrue(forklaring.hvaForklaring.resultat.toString().startsWith("2291"))
    }

    @Test
    fun `faktum skal gi korrekt forklaring`() {
        val alder = Faktum("alder", 65)
        val forklaring = alder.forklar()

        assertTrue(forklaring.contains("alder"))
        assertTrue(forklaring.contains("65"))
    }

    @Test
    fun `domain predicate skal gi FORDI forklaring`() {
        val alder = Faktum("alder", 65)
        val grense = Faktum("nedreAldersgrense", 62)

        val betingelse = alder erStørre grense
        assertTrue(betingelse.fired)

        val forklaring = betingelse.forklar()
        assertNotNull(forklaring)
        assertTrue(forklaring.subsumsjoner.isNotEmpty())
        assertTrue(forklaring.subsumsjoner[0].oppfylt)
    }

    @Test
    fun `kompleks formel med subformler skal gi detaljert forklaring`() {
        val G = Formel.variable("G", 110000)
        val FULL_TRYGDETID = Formel.variable("FULL_TRYGDETID", 40)
        val faktiskTrygdetid = Formel.variable("faktiskTrygdetid", 20)

        val fulltSlitertillegg = FormelBuilder.create<Double>()
            .name("fulltSlitertillegg")
            .expression(0.25 * G / 12)
            .locked()
            .build()

        val trygdetidFaktor = FormelBuilder.create<Double>()
            .name("trygdetidFaktor")
            .expression(faktiskTrygdetid / FULL_TRYGDETID)
            .locked()
            .build()

        val resultat = FormelBuilder.create<Double>()
            .name("slitertillegg")
            .expression(fulltSlitertillegg * trygdetidFaktor)
            .build()

        val forklaring = resultat.forklarDetaljert(maxDybde = 3)

        assertNotNull(forklaring)
        assertTrue(forklaring.contains("HVORDAN"))
        assertTrue(forklaring.contains("slitertillegg"))
    }

    @Test
    fun `komplett forklaring skal inneholde alle elementer`() {
        val verdi = Formel.variable("verdi", 100)
        val betingelse = verdi erLik 100

        val hva = verdi.forklarHva()
        val hvorfor = betingelse.forklar()
        val hvordan = verdi.forklarHvordan()

        val komplett = KomplettForklaring(
            hva = hva,
            hvorfor = hvorfor,
            hvordan = hvordan,
            referanser = listOf("REF-123")
        )

        val tekst = komplett.toText()
        assertTrue(tekst.contains("verdi"))
        assertTrue(tekst.contains("100"))
        assertTrue(tekst.contains("FORDI"))
        assertTrue(tekst.contains("HVORDAN"))
        assertTrue(tekst.contains("REFERANSE"))
        assertTrue(tekst.contains("REF-123"))
    }

    @Test
    fun `formelkompakt skal gi kortfattet forklaring`() {
        val G = Formel.variable("G", 110000)
        val resultat = FormelBuilder.create<Double>()
            .name("beregning")
            .expression(0.25 * G / 12)
            .build()

        val forklaring = resultat.forklarKompakt()

        assertTrue(forklaring.contains("beregning"))
        assertTrue(forklaring.contains("="))
        val lines = forklaring.trim().split("\n")
        assertEquals(3, lines.size) // Tre linjer: symbolsk, konkret, resultat
    }

    @Test
    fun `strukturtre skal vise hierarki`() {
        val a = Formel.variable("a", 10)
        val b = Formel.variable("b", 20)

        val locked = FormelBuilder.create<Int>()
            .name("locked")
            .expression(a + b)
            .locked()
            .build()

        val resultat = FormelBuilder.create<Int>()
            .name("total")
            .expression(locked + 5)
            .build()

        val tre = resultat.strukturTre()

        assertTrue(tre.contains("total"))
        assertTrue(tre.contains("locked"))
        assertTrue(tre.contains("├─"))
    }

    @Test
    fun `HTML output skal inneholde HTML tags`() {
        val formel = Formel.variable("test", 42)
        val forklaring = formel.forklarHva()

        val html = forklaring.toHTML()

        assertTrue(html.contains("<span"))
        assertTrue(html.contains("</span>"))
        assertTrue(html.contains("test"))
        assertTrue(html.contains("42"))
    }

    @Test
    fun `flere domain predicates kan kombineres til FORDI forklaring`() {
        val alder = Faktum("alder", 65)
        val trygdetid = Faktum("trygdetid", 35)

        val betingelser = listOf(
            alder erStørre 62,
            trygdetid erMindreEnn 40
        )

        val forklaring = betingelser.forklarFordi()

        assertNotNull(forklaring)
        assertEquals(2, forklaring.subsumsjoner.size)
        assertTrue(forklaring.subsumsjoner.all { it.oppfylt })
    }
}
