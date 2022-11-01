package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.enums.ListComparator.*
import no.nav.system.rule.dsl.rettsregel.Fact
import no.nav.system.rule.dsl.rettsregel.ListSubsumtion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ListSubsumtionMedPatternRSTest {

    @Test
    fun `test skal returnere OPPFYLT når ingen regler treffer`() {

        val factListes = listOf(
            Fact("bool1", false),
            Fact("bool2", false),
            Fact("bool3", false),
            Fact("bool4", false)
        )

        val rule = ListSubsumtionMedPatternRS(factListes).test().get()

        assertTrue(rule.fired())
        val ingenSubsumsjon = rule.children.first() as ListSubsumtion
        assertTrue(ingenSubsumsjon.fired())
        assertEquals(INGEN, ingenSubsumsjon.comparator)
        assertTrue(ingenSubsumsjon.children.all { !it.fired() })

    }

    @Test
    fun `test skal returnere OPPFYLT når minst èn regel treffer`() {

        val factListes = listOf(
            Fact("bool1", false),
            Fact("bool2", false),
            Fact("bool3", false),
            Fact("bool4", true)
        )

        val regel = ListSubsumtionMedPatternRS(factListes).test().get()

        assertTrue(regel.fired())
        val minstEnSubsumsjon = regel.children.first() as ListSubsumtion
        assertTrue(minstEnSubsumsjon.fired())
        assertEquals(MINST_EN_AV, minstEnSubsumsjon.comparator)
        assertTrue(minstEnSubsumsjon.children.any { !it.fired() })
    }

    @Test
    fun `test skal returnere OPPFYLT når alle regler treffer`() {

        val factListes = listOf(
            Fact("bool1", true),
            Fact("bool2", true),
            Fact("bool3", true),
            Fact("bool4", true)
        )

        val regel = ListSubsumtionMedPatternRS(factListes).test().get()

        assertTrue(regel.fired())
        val alleSubsumsjon = regel.children.first() as ListSubsumtion
        assertTrue(alleSubsumsjon.fired())
        assertEquals(ALLE, alleSubsumsjon.comparator)
        assertTrue(alleSubsumsjon.children.all { it.fired() })
    }
}