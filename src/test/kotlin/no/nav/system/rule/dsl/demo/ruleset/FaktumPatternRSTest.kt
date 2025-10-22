package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.rettsregel.Faktum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FaktumPatternRSTest {

    @Test
    fun `test skal returnere OPPFYLT når ingen regler treffer`() {
        val faktumListe = listOf(
            Faktum("bool1", false),
            Faktum("bool2", false),
            Faktum("bool3", false),
            Faktum("bool4", false)
        )

        val rs = FaktumPatternRS(faktumListe).also { it.test() }

        assertTrue(rs.children.none { it.fired() })
    }

    @Test
    fun `test skal returnere OPPFYLT når minst èn regel treffer`() {
        val faktumListe = listOf(
            Faktum("bool1", false),
            Faktum("bool2", false),
            Faktum("bool3", false),
            Faktum("bool4", true)
        )

        val rs = FaktumPatternRS(faktumListe).also { it.test() }

        assertEquals(3, rs.children.count { !it.fired() })
        assertTrue(rs.children.last().fired())
    }

    @Test
    fun `test skal returnere PPFYLT når alle regler treffer`() {

        val faktumListe = listOf(
            Faktum("bool1", true),
            Faktum("bool2", true),
            Faktum("bool3", true),
            Faktum("bool4", true)
        )

        val rs = FaktumPatternRS(faktumListe).also { it.test() }

        assertTrue(rs.children.all { it.fired() })
    }
}