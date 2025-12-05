package no.nav.system.ruledsl.core.model

import no.nav.system.ruledsl.core.pattern.createPattern
import no.nav.system.ruledsl.core.model.Faktum
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FaktumPatternRSTest {

    class FaktumPatternRS(
        inputFakta: List<Faktum<Boolean>>,
    ) : AbstractRuleset<Unit>() {
        private val faktumListe = inputFakta.createPattern()

        @OptIn(DslDomainPredicate::class)
        override fun create() {
            regel("sann", faktumListe) { bool ->
                HVIS { bool }
            }
        }
    }

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

        Assertions.assertEquals(3, rs.children.count { !it.fired() })
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