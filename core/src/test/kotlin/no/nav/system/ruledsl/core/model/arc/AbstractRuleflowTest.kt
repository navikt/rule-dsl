package no.nav.system.ruledsl.core.model.arc

import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.operators.erLik
import no.nav.system.ruledsl.core.operators.erMindreEnn
import no.nav.system.ruledsl.core.operators.erStørreEnn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for AbstractRuleflow core functionality.
 *
 * Uses simple Int and String values to avoid domain-specific concepts.
 */
class AbstractRuleflowTest {

    /**
     * Test basic decision with single branch
     */
    class SimpleBranchFlow(private val value: Int) : AbstractRuleflow<String>() {
        override var ruleflow: () -> String = {
            var result = "none"

            forgrening("checkValue") {
                gren {
                    betingelse { value > 0 }
                    flyt {
                        result = "positive"
                    }
                }
            }

            result
        }
    }

    @Test
    fun `single branch executes when condition is true`() {
        assertEquals("positive", SimpleBranchFlow(5).test())
    }

    @Test
    fun `single branch does not execute when condition is false`() {
        assertEquals("none", SimpleBranchFlow(-5).test())
    }

    /**
     * Test decision with multiple branches
     */
    class MultipleBranchFlow(private val value: Int) : AbstractRuleflow<String>() {
        override var ruleflow: () -> String = {
            var result = "unknown"

            forgrening("classify") {
                gren {
                    betingelse { value < 0 }
                    flyt {
                        result = "negative"
                    }
                }

                gren {
                    betingelse { value == 0 }
                    flyt {
                        result = "zero"
                    }
                }

                gren {
                    betingelse { value > 0 }
                    flyt {
                        result = "positive"
                    }
                }
            }

            result
        }
    }

    @Test
    fun `first matching branch executes`() {
        assertEquals("negative", MultipleBranchFlow(-10).test())
        assertEquals("zero", MultipleBranchFlow(0).test())
        assertEquals("positive", MultipleBranchFlow(10).test())
    }

    /**
     * Test that all matching branches execute (not just first)
     */
    class AllMatchingBranchesFlow(private val value: Int) : AbstractRuleflow<Int>() {
        override var ruleflow: () -> Int = {
            var counter = 0

            forgrening("additive") {
                gren {
                    betingelse { value > 0 }
                    flyt {
                        counter += 1
                    }
                }

                gren {
                    betingelse { value > 5 }
                    flyt {
                        counter += 10
                    }
                }

                gren {
                    betingelse { value > 10 }
                    flyt {
                        counter += 100
                    }
                }
            }

            counter
        }
    }

    @Test
    fun `all matching branches execute`() {
        assertEquals(0, AllMatchingBranchesFlow(0).test())
        assertEquals(1, AllMatchingBranchesFlow(3).test())
        assertEquals(11, AllMatchingBranchesFlow(7).test())
        assertEquals(111, AllMatchingBranchesFlow(15).test())
    }

    /**
     * Test betingelse with name
     */
    class NamedConditionFlow(private val age: Int) : AbstractRuleflow<String>() {
        override var ruleflow: () -> String = {
            var result = "unknown"

            forgrening("checkAge") {
                gren {
                    betingelse("isAdult") { age >= 18 }
                    flyt {
                        result = "adult"
                    }
                }

                gren {
                    betingelse("isMinor") { age < 18 }
                    flyt {
                        result = "minor"
                    }
                }
            }

            result
        }
    }

    @Test
    fun `named conditions work correctly`() {
        assertEquals("adult", NamedConditionFlow(25).test())
        assertEquals("minor", NamedConditionFlow(15).test())
    }

    /**
     * Test domain predicate in betingelse
     */
    class DomainPredicateFlow(private val score: Int) : AbstractRuleflow<String>() {
        override var ruleflow: () -> String = {
            val grades = mutableListOf<String>()

            forgrening("grading") {
                gren {
                    betingelse("excellent") { Faktum("score", score) erStørreEnn Faktum("threshold", 90) }
                    flyt {
                        grades.add("A")
                    }
                }

                gren {
                    betingelse("good") { Faktum("score", score) erStørreEnn Faktum("threshold", 70) }
                    flyt {
                        grades.add("B")
                    }
                }

                gren {
                    betingelse("pass") { Faktum("score", score) erStørreEnn Faktum("threshold", 50) }
                    flyt {
                        grades.add("C")
                    }
                }
            }

            if (grades.isEmpty()) "F" else grades.joinToString(",")
        }
    }

    @Test
    fun `domain predicates work in betingelse`() {
        // Note: All matching branches execute, so high scores get multiple grades
        assertEquals("A,B,C", DomainPredicateFlow(95).test())
        assertEquals("B,C", DomainPredicateFlow(75).test())
        assertEquals("C", DomainPredicateFlow(55).test())
        assertEquals("F", DomainPredicateFlow(30).test())
    }

    /**
     * Test nested decisions
     */
    class NestedDecisionFlow(private val x: Int, private val y: Int) : AbstractRuleflow<String>() {
        override var ruleflow: () -> String = {
            var result = "none"

            forgrening("outer") {
                gren {
                    betingelse { x > 0 }
                    flyt {
                        forgrening("inner") {
                            gren {
                                betingelse { y > 0 }
                                flyt {
                                    result = "both-positive"
                                }
                            }

                            gren {
                                betingelse { y <= 0 }
                                flyt {
                                    result = "x-positive-y-not"
                                }
                            }
                        }
                    }
                }

                gren {
                    betingelse { x <= 0 }
                    flyt {
                        result = "x-not-positive"
                    }
                }
            }

            result
        }
    }

    @Test
    fun `nested decisions work correctly`() {
        assertEquals("both-positive", NestedDecisionFlow(5, 3).test())
        assertEquals("x-positive-y-not", NestedDecisionFlow(5, -3).test())
        assertEquals("x-not-positive", NestedDecisionFlow(-5, 3).test())
        assertEquals("x-not-positive", NestedDecisionFlow(-5, -3).test())
    }

    /**
     * Test multiple forgrening in same flow
     */
    class MultipleDecisionsFlow(private val a: Int, private val b: Int) : AbstractRuleflow<Int>() {
        override var ruleflow: () -> Int = {
            var result = 0

            forgrening("first") {
                gren {
                    betingelse { a > 0 }
                    flyt {
                        result += a
                    }
                }
            }

            forgrening("second") {
                gren {
                    betingelse { b > 0 }
                    flyt {
                        result += b
                    }
                }
            }

            result
        }
    }

    @Test
    fun `multiple sequential decisions execute independently`() {
        assertEquals(15, MultipleDecisionsFlow(5, 10).test())
        assertEquals(5, MultipleDecisionsFlow(5, -10).test())
        assertEquals(10, MultipleDecisionsFlow(-5, 10).test())
        assertEquals(0, MultipleDecisionsFlow(-5, -10).test())
    }

    /**
     * Test that branch flow can execute complex logic including rulesets
     */
    class FlowWithRulesetBranch(private val value: Int) : AbstractRuleflow<Int>() {
        class DoubleRS(private val input: Int) : AbstractRuleset<Int>() {
            override fun create() {
                regel("double") {
                    HVIS { true }
                    SÅ {
                        RETURNER(input * 2)
                    }
                }
            }
        }

        override var ruleflow: () -> Int = {
            var result = 0

            forgrening("process") {
                gren {
                    betingelse { value > 0 }
                    flyt {
                        result = DoubleRS(value).run(this)
                    }
                }

                gren {
                    betingelse { value <= 0 }
                    flyt {
                        result = -1
                    }
                }
            }

            result
        }
    }

    @Test
    fun `branch flow can execute rulesets`() {
        assertEquals(20, FlowWithRulesetBranch(10).test())
        assertEquals(-1, FlowWithRulesetBranch(-5).test())
    }

    /**
     * Test empty decision (no branches)
     */
    class EmptyDecisionFlow : AbstractRuleflow<String>() {
        override var ruleflow: () -> String = {
            var result = "before"

            forgrening("empty") {
                // No branches
            }

            result = "after"
            result
        }
    }

    @Test
    fun `empty decision does nothing and flow continues`() {
        assertEquals("after", EmptyDecisionFlow().test())
    }

    /**
     * Test that non-matching branches don't execute
     */
    class NonMatchingBranchesFlow(private val value: Int) : AbstractRuleflow<String>() {
        var branchAExecuted = false
        var branchBExecuted = false

        override var ruleflow: () -> String = {
            forgrening("test") {
                gren {
                    betingelse { value == 1 }
                    flyt {
                        branchAExecuted = true
                    }
                }

                gren {
                    betingelse { value == 2 }
                    flyt {
                        branchBExecuted = true
                    }
                }
            }

            "done"
        }
    }

    @Test
    fun `non-matching branches are not executed`() {
        val flow = NonMatchingBranchesFlow(3)
        flow.test()
        assertEquals(false, flow.branchAExecuted)
        assertEquals(false, flow.branchBExecuted)
    }

    /**
     * Test branch with multiple operations in flyt
     */
    class ComplexFlowLogic(private val value: Int) : AbstractRuleflow<String>() {
        override var ruleflow: () -> String = {
            val items = mutableListOf<String>()

            forgrening("process") {
                gren {
                    betingelse { value > 0 }
                    flyt {
                        items.add("positive")
                        items.add("value=$value")
                        items.add("doubled=${value * 2}")
                    }
                }
            }

            items.joinToString(";")
        }
    }

    @Test
    fun `branch flyt can contain multiple statements`() {
        assertEquals("positive;value=7;doubled=14", ComplexFlowLogic(7).test())
        assertEquals("", ComplexFlowLogic(-7).test())
    }

    /**
     * Test return value directly from ruleflow
     */
    class DirectReturnFlow(private val text: String) : AbstractRuleflow<String>() {
        override var ruleflow: () -> String = {
            var result = ""

            forgrening("setResult") {
                gren {
                    betingelse { text.isNotEmpty() }
                    flyt {
                        result = text.uppercase()
                    }
                }
            }

            result
        }
    }

    @Test
    fun `ruleflow returns last expression`() {
        assertEquals("HELLO", DirectReturnFlow("hello").test())
        assertEquals("", DirectReturnFlow("").test())
    }
}
