package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.enums.ListComparator.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.ListSubsumtion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ListSubsumtionMedPatternRSTest {

    @Test
    fun `test skal returnere OPPFYLT når ingen regler treffer`() {

        val faktumListe = listOf(
            Faktum("bool1", false),
            Faktum("bool2", false),
            Faktum("bool3", false),
            Faktum("bool4", false)
        )

        val rule = ListSubsumtionMedPatternRS(faktumListe).test()

        assertTrue(rule.fired())
        val ingenSubsumsjon = rule.children.first() as ListSubsumtion
        assertTrue(ingenSubsumsjon.fired())
        assertEquals(INGEN, ingenSubsumsjon.comparator)
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

        val regel = ListSubsumtionMedPatternRS(faktumListe).test()

        assertTrue(regel.fired())
        val minstEnSubsumsjon = regel.children.first() as ListSubsumtion
        assertTrue(minstEnSubsumsjon.fired())
        assertEquals(MINST_EN_AV, minstEnSubsumsjon.comparator)
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

        val regel = ListSubsumtionMedPatternRS(faktumListe).test()

        assertTrue(regel.fired())
        val alleSubsumsjon = regel.children.first() as ListSubsumtion
        assertTrue(alleSubsumsjon.fired())
        assertEquals(ALLE, alleSubsumsjon.comparator)
        assertTrue(alleSubsumsjon.children.all { it.fired() })
    }
}