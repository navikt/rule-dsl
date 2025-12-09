package no.nav.system.ruledsl.core.resource

import no.nav.system.ruledsl.core.error.ResourceAccessException
import no.nav.system.ruledsl.core.model.arc.AbstractRuleset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Tests for resource management in AbstractRuleComponent.
 *
 * Tests resource propagation, type safety, and access patterns.
 */
class ResourceTest {

    /**
     * Simple custom resource for testing
     */
    class Counter(var count: Int = 0) : AbstractResource()

    /**
     * Another custom resource
     */
    class Config(val maxValue: Int) : AbstractResource()

    /**
     * Test that resources can be added and retrieved
     */
    @Test
    fun `resources can be stored and retrieved`() {
        class SimpleRS : AbstractRuleset<Int>() {
            override fun create() {
                putResource(Counter::class, Counter(10))

                regel("test") {
                    HVIS { true }
                    SÅ {
                        val counter = getResource(Counter::class)
                        RETURNER(counter.count)
                    }
                }
            }
        }

        val result = SimpleRS().test()
        assertEquals(10, result)
    }

    /**
     * Test that resources propagate to child components
     */
    @Test
    fun `resources propagate to child components`() {
        class ChildRS : AbstractRuleset<Int>() {
            override fun create() {
                regel("useCounter") {
                    HVIS { true }
                    SÅ {
                        val counter = getResource(Counter::class)
                        RETURNER(counter.count)
                    }
                }
            }
        }

        class ParentRS : AbstractRuleset<Int>() {
            override fun create() {
                putResource(Counter::class, Counter(42))

                regel("runChild") {
                    HVIS { true }
                    SÅ {
                        RETURNER(ChildRS().run(this))
                    }
                }
            }
        }

        val result = ParentRS().test()
        assertEquals(42, result, "Child should access parent's resource")
    }

    /**
     * Test that resource changes are visible
     */
    @Test
    fun `resources are shared`() {
        class ModifyingRS : AbstractRuleset<Int>() {
            override fun create() {
                putResource(Counter::class, Counter(0))

                regel("increment1") {
                    HVIS { true }
                    SÅ {
                        getResource(Counter::class).count += 5
                    }
                }

                regel("increment2") {
                    HVIS { true }
                    SÅ {
                        getResource(Counter::class).count += 10
                    }
                }

                regel("return") {
                    HVIS { true }
                    SÅ {
                        RETURNER(getResource(Counter::class).count)
                    }
                }
            }
        }

        val result = ModifyingRS().test()
        assertEquals(15, result, "Resource modifications should accumulate")
    }

    /**
     * Test that accessing non-existent resource throws exception
     */
    @Test
    fun `accessing non-existent resource throws exception`() {
        class NoResourceRS : AbstractRuleset<Unit>() {
            override fun create() {
                regel("fail") {
                    HVIS { true }
                    SÅ {
                        getResource(Counter::class)
                    }
                }
            }
        }

        assertThrows<ResourceAccessException> {
            NoResourceRS().test()
        }
    }

    /**
     * Test multiple different resources
     */
    @Test
    fun `multiple different resource types can coexist`() {
        class MultiResourceRS : AbstractRuleset<String>() {
            override fun create() {
                putResource(Counter::class, Counter(7))
                putResource(Config::class, Config(100))

                regel("combine") {
                    HVIS { true }
                    SÅ {
                        val count = getResource(Counter::class).count
                        val max = getResource(Config::class).maxValue
                        RETURNER("count=$count,max=$max")
                    }
                }
            }
        }

        val result = MultiResourceRS().test()
        assertEquals("count=7,max=100", result)
    }

    /**
     * Test that same resource instance is shared across tree
     */
    @Test
    fun `same resource instance is shared not copied`() {
        class SharedInstanceRS : AbstractRuleset<Boolean>() {
            lateinit var firstInstance: Counter
            lateinit var secondInstance: Counter

            override fun create() {
                putResource(Counter::class, Counter(99))

                regel("first") {
                    HVIS { true }
                    SÅ {
                        firstInstance = getResource(Counter::class)
                    }
                }

                regel("second") {
                    HVIS { true }
                    SÅ {
                        secondInstance = getResource(Counter::class)
                        RETURNER(true)
                    }
                }
            }
        }

        val rs = SharedInstanceRS()
        rs.test()

        assertSame(rs.firstInstance, rs.secondInstance, "Should be same instance")
    }

    /**
     * Test resource type safety via casting
     */
    @Test
    fun `resource retrieval is type-safe`() {
        class TypeSafeRS : AbstractRuleset<Int>() {
            override fun create() {
                putResource(Counter::class, Counter(123))

                regel("typed") {
                    HVIS { true }
                    SÅ {
                        // Type is inferred correctly
                        val counter: Counter = getResource(Counter::class)
                        RETURNER(counter.count)
                    }
                }
            }
        }

        val result = TypeSafeRS().test()
        assertEquals(123, result)
    }

    /**
     * Test that resources can be replaced
     */
    @Test
    fun `resources can be overwritten`() {
        class OverwriteRS : AbstractRuleset<Int>() {
            override fun create() {
                putResource(Counter::class, Counter(10))
                putResource(Counter::class, Counter(20)) // Overwrite

                regel("get") {
                    HVIS { true }
                    SÅ {
                        RETURNER(getResource(Counter::class).count)
                    }
                }
            }
        }

        val result = OverwriteRS().test()
        assertEquals(20, result, "Should get the overwritten value")
    }

    /**
     * Test resource access from nested components
     */
    @Test
    fun `deeply nested components can access resources`() {
        class InnerRS : AbstractRuleset<Int>() {
            override fun create() {
                regel("inner") {
                    HVIS { true }
                    SÅ {
                        RETURNER(getResource(Counter::class).count)
                    }
                }
            }
        }

        class MiddleRS : AbstractRuleset<Int>() {
            override fun create() {
                regel("middle") {
                    HVIS { true }
                    SÅ {
                        RETURNER(InnerRS().run(this))
                    }
                }
            }
        }

        class OuterRS : AbstractRuleset<Int>() {
            override fun create() {
                putResource(Counter::class, Counter(777))

                regel("outer") {
                    HVIS { true }
                    SÅ {
                        RETURNER(MiddleRS().run(this))
                    }
                }
            }
        }

        val result = OuterRS().test()
        assertEquals(777, result, "Deeply nested component should access resource")
    }

    /**
     * Test that root() returns initial rulecomponent from sub-rulecomponent.
     */
    @Test
    fun `root() returns same instance from all tree levels`() {
        class SubRS : AbstractRuleset<String>() {
            override fun create() {
                regel("sub") {
                    HVIS { true }
                    SÅ {
                        RETURNER(root().name())
                    }
                }
            }
        }

        class MainRS : AbstractRuleset<String>() {
            override fun create() {
                regel("main") {
                    HVIS { true }
                    SÅ {
                        RETURNER(SubRS().run(this))
                    }
                }
            }
        }

        assertEquals("MainRS", MainRS().test())
    }
}
