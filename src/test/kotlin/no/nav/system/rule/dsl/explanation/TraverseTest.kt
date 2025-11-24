package no.nav.system.rule.dsl.explanation

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.operators.erStørreEllerLik
import org.junit.jupiter.api.Test

class TraverseTest {

    class SimpleRuleset(private val alder: Int) : AbstractRuleset<Boolean>() {
        override fun create() {
            regel("VILKÅR-ALDER") {
                HVIS { Faktum("alder", alder) erStørreEllerLik Faktum("aldersgrense", 67) }
                SÅ {
                    val resultat = sporing("vilkårOppfylt", true)
                    RETURNER(resultat.verdi)
                }
                ELLERS {
                    val resultat = sporing("vilkårOppfylt", false)
                    RETURNER(resultat.verdi)
                }
            }
        }
    }

    @Test
    fun `skal kunne traverse ARC tree med Hva interface`() {
        // Arrange
        val ruleset = SimpleRuleset(alder = 70)

        // Act
        val result = ruleset.test()
        val output = ruleset.traverseHva()

        // Print the tree
        println("=== Tree Traversal (HVA) ===")
        println(output)
        println("=== End ===")

        // Verify the tree contains expected nodes
        assert(output.contains("regelsett: SimpleRuleset"))
        assert(output.contains("regel: SimpleRuleset.VILKÅR-ALDER"))
        assert(output.contains("faktum: vilkårOppfylt"))
    }

    @Test
    fun `skal kunne traverse med full forklaring`() {
        // Arrange
        val ruleset = SimpleRuleset(alder = 70)

        // Act
        val result = ruleset.test()
        val output = ruleset.traverseFull()

        // Print the full explanation
        println("=== Full Traversal (HVA + HVORFOR + HVORDAN) ===")
        println(output)
        println("=== End ===")
    }

    @Test
    fun `skal kunne samle alle Faktum noder`() {
        // Arrange
        val ruleset = SimpleRuleset(alder = 70)

        // Act
        val result = ruleset.test()
        val faktumNodes = ruleset.collectFaktum()

        // Print collected Faktum
        println("=== Collected Faktum ===")
        faktumNodes.forEach { node ->
            println("${node.faktum.navn} = ${node.faktum.verdi}")
        }
        println("=== End ===")

        // Verify we found the Faktum
        assert(faktumNodes.isNotEmpty())
        assert(faktumNodes.any { it.faktum.navn == "vilkårOppfylt" })
    }
}
