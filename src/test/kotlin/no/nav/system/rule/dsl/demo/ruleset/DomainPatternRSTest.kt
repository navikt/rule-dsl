package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.enums.Komparator.*
import no.nav.system.rule.dsl.enums.UtfallType.IKKE_OPPFYLT
import no.nav.system.rule.dsl.enums.UtfallType.OPPFYLT
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.Subsumsjon
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DomainPatternRSTest {

    @Test
    fun `test skal returnere OPPFYLT når ingen regler treffer`() {

        val faktumListe = listOf(
            Faktum("bool1", false),
            Faktum("bool2", false),
            Faktum("bool3", false),
            Faktum("bool4", false)
        )

        val utfallList = DomainPatternRS(faktumListe).testAndDebug().get()

        assertEquals(OPPFYLT, utfallList[0].utfallType)
        val ingenSubsumsjon = utfallList[0].regel.children.first() as Subsumsjon
        assertTrue(ingenSubsumsjon.fired())
        assertEquals(INGEN, ingenSubsumsjon.komparator)
        assertTrue(ingenSubsumsjon.children.all { !it.fired() })

    }

    @Test
    fun `test skal returnere OPPFYLT når minst èn regel treffer`() {

        val faktumListe = listOf(
            Faktum("bool1", false),
            Faktum("bool2", false),
            Faktum("bool3", false),
            Faktum("bool4", true)
        )

        val utfallList = DomainPatternRS(faktumListe).testAndDebug().get()

        assertEquals(OPPFYLT, utfallList[1].utfallType)
        val minstEnSubsumsjon = utfallList[1].regel.children.first() as Subsumsjon
        assertTrue(minstEnSubsumsjon.fired())
        assertEquals(MINST_EN_AV, minstEnSubsumsjon.komparator)
        assertTrue(minstEnSubsumsjon.children.any { !it.fired() })
    }

    @Test
    fun `test skal returnere OPPFYLT når alle regler treffer`() {

        val faktumListe = listOf(
            Faktum("bool1", true),
            Faktum("bool2", true),
            Faktum("bool3", true),
            Faktum("bool4", true)
        )

        val utfallList = DomainPatternRS(faktumListe).testAndDebug().get()

        assertEquals(OPPFYLT, utfallList[2].utfallType)
        val alleSubsumsjon = utfallList[2].regel.children.first() as Subsumsjon
        assertTrue(alleSubsumsjon.fired())
        assertEquals(ALLE, alleSubsumsjon.komparator)
        assertTrue(alleSubsumsjon.children.all { it.fired() })
    }


    @Test
    fun `test skal returnere IKKE_OPPFYLT når en eller flere regler treffer`() {

        val faktumListe = listOf(
            Faktum("bool1", false),
            Faktum("bool2", false),
            Faktum("bool3", true),
            Faktum("bool4", false)
        )

        val utfallList = DomainPatternRS(faktumListe).testAndDebug().get()

        assertEquals(IKKE_OPPFYLT, utfallList[0].utfallType)
        val ingenSubsumsjon = utfallList[0].regel.children.first() as Subsumsjon
        assertFalse(ingenSubsumsjon.fired())
        assertEquals(INGEN, ingenSubsumsjon.komparator)
        assertTrue(ingenSubsumsjon.children.any { it.fired() })

    }

    @Test
    fun `test skal returnere IKKE_OPPFYLT når ingen regel treffer`() {

        val faktumListe = listOf(
            Faktum("bool1", false),
            Faktum("bool2", false),
            Faktum("bool3", false),
            Faktum("bool4", false)
        )

        val utfallList = DomainPatternRS(faktumListe).testAndDebug().get()

        assertEquals(IKKE_OPPFYLT, utfallList[1].utfallType)
        val minstEnSubsumsjon = utfallList[1].regel.children.first() as Subsumsjon
        assertFalse(minstEnSubsumsjon.fired())
        assertEquals(MINST_EN_AV, minstEnSubsumsjon.komparator)
        assertTrue(minstEnSubsumsjon.children.all { !it.fired() })
    }

    @Test
    fun `test skal returnere IKKE_OPPFYLT når en eller fler regler ikke treffer`() {

        val faktumListe = listOf(
            Faktum("bool1", true),
            Faktum("bool2", true),
            Faktum("bool3", false),
            Faktum("bool4", true)
        )

        val utfallList = DomainPatternRS(faktumListe).testAndDebug().get()

        assertEquals(IKKE_OPPFYLT, utfallList[2].utfallType)
        val alleSubsumsjon = utfallList[2].regel.children.first() as Subsumsjon
        assertFalse(alleSubsumsjon.fired())
        assertEquals(ALLE, alleSubsumsjon.komparator)
        assertTrue(alleSubsumsjon.children.any { !it.fired() })
    }
}