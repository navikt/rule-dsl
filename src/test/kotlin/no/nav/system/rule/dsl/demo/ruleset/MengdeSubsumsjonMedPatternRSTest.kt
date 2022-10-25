package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.enums.MengdeKomparator.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.MengdeSubsumsjon
import no.nav.system.rule.dsl.treevisitor.visitor.debug
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MengdeSubsumsjonMedPatternRSTest {

    @Test
    fun `test skal returnere OPPFYLT når ingen regler treffer`() {

        val faktumListe = listOf(
            Faktum("bool1", false),
            Faktum("bool2", false),
            Faktum("bool3", false),
            Faktum("bool4", false)
        )

        val rule = MengdeSubsumsjonMedPatternRS(faktumListe).testAndDebug().get()

        println(rule.debug())

        assertTrue(rule.fired())
        val ingenSubsumsjon = rule.children.first() as MengdeSubsumsjon
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

        val regel = MengdeSubsumsjonMedPatternRS(faktumListe).testAndDebug().get()

        assertTrue(regel.fired())
        val minstEnSubsumsjon = regel.children.first() as MengdeSubsumsjon
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

        val regel = MengdeSubsumsjonMedPatternRS(faktumListe).test().get()

        assertTrue(regel.fired())
        val alleSubsumsjon = regel.children.first() as MengdeSubsumsjon
        assertTrue(alleSubsumsjon.fired())
        assertEquals(ALLE, alleSubsumsjon.komparator)
        assertTrue(alleSubsumsjon.children.all { it.fired() })
    }
//
//
//    @Test
//    fun `test skal returnere IKKE_OPPFYLT når en eller flere regler treffer`() {
//
//        val faktumListe = listOf(
//            Faktum("bool1", false),
//            Faktum("bool2", false),
//            Faktum("bool3", true),
//            Faktum("bool4", false)
//        )
//
//        val utfallList = DomainPatternRS(faktumListe).testAndDebug().get()
//
//        assertEquals(IKKE_OPPFYLT, utfallList[0].verdi)
//        val ingenSubsumsjon = utfallList[0].children[0].children.first() as MengdeSubsumsjon
//        assertFalse(ingenSubsumsjon.fired())
//        assertEquals(INGEN, ingenSubsumsjon.komparator)
//        assertTrue(ingenSubsumsjon.children.any { it.fired() })
//
//    }
//
//    @Test
//    fun `test skal returnere IKKE_OPPFYLT når ingen regel treffer`() {
//
//        val faktumListe = listOf(
//            Faktum("bool1", false),
//            Faktum("bool2", false),
//            Faktum("bool3", false),
//            Faktum("bool4", false)
//        )
//
//        val utfallList = DomainPatternRS(faktumListe).testAndDebug().get()
//
//        assertEquals(IKKE_OPPFYLT, utfallList[1].verdi)
//        val minstEnSubsumsjon = utfallList[1].children[0].children.first() as MengdeSubsumsjon
//        assertFalse(minstEnSubsumsjon.fired())
//        assertEquals(MINST_EN_AV, minstEnSubsumsjon.komparator)
//        assertTrue(minstEnSubsumsjon.children.all { !it.fired() })
//    }
//
//    @Test
//    fun `test skal returnere IKKE_OPPFYLT når en eller fler regler ikke treffer`() {
//
//        val faktumListe = listOf(
//            Faktum("bool1", true),
//            Faktum("bool2", true),
//            Faktum("bool3", false),
//            Faktum("bool4", true)
//        )
//
//        val utfallList = DomainPatternRS(faktumListe).testAndDebug().get()
//
//        assertEquals(IKKE_OPPFYLT, utfallList[2].verdi)
//        val alleSubsumsjon = utfallList[2].children[0].children.first() as MengdeSubsumsjon
//        assertFalse(alleSubsumsjon.fired())
//        assertEquals(ALLE, alleSubsumsjon.komparator)
//        assertTrue(alleSubsumsjon.children.any { !it.fired() })
//    }
}