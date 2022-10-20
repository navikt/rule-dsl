package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.*
import org.jetbrains.annotations.TestOnly
import svarord
import java.util.*

/**
 * Common ruleflow behaviour used by all ruleflow implementations.
 * Defines branching logic DSL (decision, branch, condition, flow).
 */
abstract class AbstractRuleflow : AbstractResourceHolder() {
    /**
     * Tracks the full name of nested branches.
     */
    private val branchNameStack = Stack<String>()

    /**
     * Tests the ruleflow without a parent ruleComponent.
     */
    @TestOnly
    open fun test() {
        branchNameStack.push(this.javaClass.simpleName)
        ruleflow.invoke()
    }

    /**
     * Runs the ruleflow
     */
    open fun run(parent: AbstractRuleComponent) {
        parent.children.add(this)
        this.parent = parent

        branchNameStack.push(this.javaClass.simpleName)
        ruleflow.invoke()
    }

    protected abstract var ruleflow: () -> Unit

    private fun branchName(): String = branchNameStack.elements().toList().joinToString(".")

    /**
     * DSL: Ruleflow Decision entry.
     */
    fun decision(name: String, init: Decision.() -> Unit) {
        branchNameStack.push(name)
        val d = Decision(branchName())
        this.children.add(d)
        d.parent = this
        d.init()
        d.run()
        branchNameStack.pop()
    }

    override fun name(): String = this.javaClass.simpleName
    override fun fired(): Boolean = true
    override fun type(): RuleComponentType = REGELFLYT
    override fun toString(): String = "${type()}: ${name()}"

    /**
     *
     */
    class Decision(
        private val name: String,
    ) : AbstractRuleComponent() {

        private var branchList = mutableListOf<Branch>()

        fun run() {
            val flowsToRun = mutableListOf<Branch>()
            branchList.forEach {
                it.fired = it.condition.invoke()
                if (it.fired) {
                    flowsToRun.add(it)
                }
            }
            flowsToRun.forEach {
                it.flowFunction.invoke()
            }
        }

        /**
         * DSL: Decision branch entry.
         * Defines a single branch inside a Decision.
         */
        fun branch(init: Branch.() -> Unit): Branch {
            val b = Branch("$name/branch ${branchList.size}")
            this.children.add(b)
            b.parent = this
            b.init()
            branchList.add(b)
            return b
        }

        override fun name(): String = name
        override fun fired(): Boolean = true
        override fun type(): RuleComponentType = DECISION
        override fun toString(): String = "${type()}: ${name()}"

        class Branch(
            private val name: String,
        ) : AbstractRuleComponent() {
            lateinit var condition: () -> Boolean
            lateinit var flowFunction: () -> Unit
            var fired = false

            /**
             * DSL: Branch condition entry.
             * Defines a boolean condition that must be evaluated to true for the following [flow] to be run.
             */
            fun condition(init: () -> Boolean) {
                condition = init
            }

            /**
             * DSL: Branch flow entry.
             * Contains the code to be run.
             */
            fun flow(flowInit: () -> Unit) {
                flowFunction = flowInit
            }

            override fun name(): String = name
            override fun fired(): Boolean = fired
            override fun type(): RuleComponentType = BRANCH
            override fun toString(): String = "${type()}: ${fired().svarord()} ${name()}"
        }
    }
}





