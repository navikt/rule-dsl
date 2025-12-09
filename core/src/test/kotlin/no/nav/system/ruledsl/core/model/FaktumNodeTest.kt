package no.nav.system.ruledsl.core.model

import no.nav.system.ruledsl.core.enums.RuleComponentType
import no.nav.system.ruledsl.core.model.arc.AbstractRuleset
import no.nav.system.ruledsl.core.model.uttrykk.Const
import no.nav.system.ruledsl.core.operators.erLik
import no.nav.system.ruledsl.core.operators.plus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

/**
 * Tests for FaktumNode wrapper functionality.
 *
 * Tests the adapter pattern that allows Faktum to participate in ARC tree.
 */
class FaktumNodeTest {

    /**
     * Test that faktum created via sporing() has a wrapper node
     */
    @Test
    fun `sporing creates FaktumNode wrapper with backlink`() {
        class SporingRS : AbstractRuleset<Faktum<Int>>() {
            override fun create() {
                regel("create") {
                    HVIS { true }
                    SÅ {
                        val f = sporing("count", 42)
                        RETURNER(f)
                    }
                }
            }
        }

        val faktum = SporingRS().test()

        assertNotNull(faktum.wrapperNode, "Faktum should have wrapper node")
        assertSame(faktum, faktum.wrapperNode?.faktum, "Wrapper should reference same faktum")
    }

    /**
     * Test that inline Faktum without sporing has no wrapper
     */
    @Test
    fun `inline Faktum without sporing has no wrapper`() {
        val faktum = Faktum("inline", 123)

        assertNull(faktum.wrapperNode, "Inline faktum should have no wrapper")
    }

    /**
     * Test FaktumNode name matches Faktum name
     */
    @Test
    fun `FaktumNode name matches Faktum navn`() {
        class NameTestRS : AbstractRuleset<Unit>() {
            lateinit var node: FaktumNode<String>

            override fun create() {
                regel("test") {
                    HVIS { true }
                    SÅ {
                        val f = sporing("testName", "value")
                        node = f.wrapperNode!!
                    }
                }
            }
        }

        val rs = NameTestRS()
        rs.test()

        assertEquals("testName", rs.node.name())
        assertEquals("testName", rs.node.faktum.navn)
    }

    /**
     * Test FaktumNode type
     */
    @Test
    fun `FaktumNode has correct type`() {
        class TypeTestRS : AbstractRuleset<Unit>() {
            lateinit var node: FaktumNode<Int>

            override fun create() {
                regel("test") {
                    HVIS { true }
                    SÅ {
                        val f = sporing("number", 99)
                        node = f.wrapperNode!!
                    }
                }
            }
        }

        val rs = TypeTestRS()
        rs.test()

        assertEquals(RuleComponentType.FAKTUM, rs.node.type())
    }

    /**
     * Test FaktumNode toString
     */
    @Test
    fun `FaktumNode toString shows faktum name and value`() {
        class ToStringTestRS : AbstractRuleset<Unit>() {
            lateinit var node: FaktumNode<String>

            override fun create() {
                regel("test") {
                    HVIS { true }
                    SÅ {
                        val f = sporing("greeting", "hello")
                        node = f.wrapperNode!!
                    }
                }
            }
        }

        val rs = ToStringTestRS()
        rs.test()

        val str = rs.node.toString()
        assertEquals("faktum: greeting = hello", str)
    }

    /**
     * Test that FaktumNode is in the ARC tree
     */
    @Test
    fun `FaktumNode is added as child in ARC tree`() {
        class TreeTestRS : AbstractRuleset<Unit>() {
            lateinit var faktumNode: FaktumNode<Int>

            override fun create() {
                regel("test") {
                    HVIS { true }
                    SÅ {
                        val f = sporing("value", 77)
                        faktumNode = f.wrapperNode!!
                    }
                }
            }
        }

        val rs = TreeTestRS()
        rs.test()

        assertNotNull(rs.faktumNode.parent, "FaktumNode should have parent")
        // Rule name is prefixed with ruleset name
        assertEquals("TreeTestRS.test", rs.faktumNode.parent?.name(), "Parent should be the rule")
    }

    /**
     * Test sporing with Uttrykk
     */
    @Test
    fun `sporing with Uttrykk creates wrapper`() {
        class UttrykkSporingRS : AbstractRuleset<Faktum<Int>>() {
            override fun create() {
                regel("test") {
                    HVIS { true }
                    SÅ {
                        val uttrykk = Const(10) + Const(5)
                        val f = sporing("calculated", uttrykk)
                        RETURNER(f)
                    }
                }
            }
        }

        val faktum = UttrykkSporingRS().test()

        assertNotNull(faktum.wrapperNode)
        assertEquals(15, faktum.verdi)
        assertEquals("calculated", faktum.navn)
    }

    /**
     * Test that Faktum value is accessible through wrapper
     */
    @Test
    fun `Faktum value accessible through FaktumNode`() {
        class ValueAccessRS : AbstractRuleset<Unit>() {
            var nodeValue: Int = 0

            override fun create() {
                regel("test") {
                    HVIS { true }
                    SÅ {
                        val f = sporing("data", 888)
                        nodeValue = f.wrapperNode!!.faktum.verdi
                    }
                }
            }
        }

        val rs = ValueAccessRS()
        rs.test()

        assertEquals(888, rs.nodeValue)
    }

    /**
     * Test multiple faktum with wrappers in same rule
     */
    @Test
    fun `multiple sporing calls create multiple wrappers`() {
        class MultipleFactsRS : AbstractRuleset<Int>() {
            val wrappers = mutableListOf<FaktumNode<*>>()

            override fun create() {
                regel("test") {
                    HVIS { true }
                    SÅ {
                        val f1 = sporing("first", 1)
                        val f2 = sporing("second", 2)
                        val f3 = sporing("third", 3)

                        wrappers.add(f1.wrapperNode!!)
                        wrappers.add(f2.wrapperNode!!)
                        wrappers.add(f3.wrapperNode!!)

                        RETURNER(f1.verdi + f2.verdi + f3.verdi)
                    }
                }
            }
        }

        val rs = MultipleFactsRS()
        val result = rs.test()

        assertEquals(6, result)
        assertEquals(3, rs.wrappers.size)
        assertEquals("first", rs.wrappers[0].name())
        assertEquals("second", rs.wrappers[1].name())
        assertEquals("third", rs.wrappers[2].name())
    }

    /**
     * Test FaktumNode is distinct from Faktum
     */
    @Test
    fun `FaktumNode is adapter not same as Faktum`() {
        class AdapterTestRS : AbstractRuleset<Unit>() {
            lateinit var faktum: Faktum<String>
            lateinit var node: FaktumNode<String>

            override fun create() {
                regel("test") {
                    HVIS { true }
                    SÅ {
                        faktum = sporing("text", "adapter-pattern")
                        node = faktum.wrapperNode!!
                    }
                }
            }
        }

        val rs = AdapterTestRS()
        rs.test()

        // They are different objects
        assert(rs.faktum !== rs.node)
        // But node wraps faktum
        assertSame(rs.faktum, rs.node.faktum)
    }

    /**
     * Test that FaktumNode can be used in ARC tree traversal
     */
    @Test
    fun `FaktumNode participates in ARC tree structure`() {
        class TreeStructureRS : AbstractRuleset<Unit>() {
            override fun create() {
                regel("parent") {
                    HVIS { true }
                    SÅ {
                        val f = sporing("child", 100)
                        // The wrapper node is now a child of this rule
                    }
                }
            }
        }

        val rs = TreeStructureRS()
        rs.test()

        // Find the rule
        val rule = rs.children.first()

        // Rule should have FaktumNode as child
        assertEquals(1, rule.children.size)

        val childNode = rule.children.first()
        assert(childNode is FaktumNode<*>)

        val faktumNode = childNode as FaktumNode<*>
        assertEquals("child", faktumNode.name())
        assertEquals(100, faktumNode.faktum.verdi)
    }
}
