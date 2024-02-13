package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.AbstractRuleflow.Decision.Branch
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.*
import no.nav.system.rule.dsl.rettsregel.helper.svarord
import org.jetbrains.annotations.TestOnly
import java.util.*

/**
 * Common ruleflow behaviour used by all ruleflow implementations.
 * Defines branching logic DSL (decision, branch, condition, flow).
 */
abstract class AbstractRuleflow<T : Any> : AbstractResourceAccessor() {
    /**
     * Tracks the full name of nested branches.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val branchNameStack = Stack<String>()

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
    open fun run(parent: AbstractRuleComponent): T {
        if (parent is AbstractResourceAccessor) {
            this.resourceMap = parent.resourceMap
        }
        parent.children.add(this)

        branchNameStack.push(this.javaClass.simpleName)
        return ruleflow.invoke()
    }

    protected abstract var ruleflow: () -> T

    private fun branchName(): String = branchNameStack.elements().toList().joinToString(".")

    /**
     * DSL: Ruleflow Decision entry.
     */
    fun forgrening(name: String, init: Decision.() -> Unit) {
        branchNameStack.push(name)
        val d = Decision(branchName())
        d.resourceMap = this.resourceMap
        this.children.add(d)
        d.init()
        d.run()
        branchNameStack.pop()
    }

    override fun name(): String = this.javaClass.simpleName
    override fun fired(): Boolean = true
    override fun type(): RuleComponentType = REGELFLYT
    override fun toString(): String = "${type()}: ${name()}"

    /**
     * Represents a split in ruleflow logic. Each [Decision] can have multiple outcomes ([Branch]).
     */
    class Decision(
        private val name: String,
    ) : AbstractResourceAccessor() {

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
        fun gren(init: Branch.() -> Unit): Branch {
            val b = Branch("$name/gren ${branchList.size}")
            b.resourceMap = this.resourceMap
            this.children.add(b)
            b.init()
            branchList.add(b)
            return b
        }

        override fun name(): String = name
        override fun fired(): Boolean = true
        override fun type(): RuleComponentType = FORGRENING
        override fun toString(): String = "${type()}: ${name()}"

        class Branch(
            private val name: String,
        ) : AbstractResourceAccessor() {
            lateinit var condition: () -> Boolean
            lateinit var flowFunction: () -> Unit
            var fired = false

            /**
             * DSL: Branch condition entry.
             * Defines a boolean condition that must be evaluated to true for the following [flyt] to be run.
             */
            fun betingelse(init: () -> Boolean) {
                condition = init
            }

            /**
             * DSL: Branch flow entry.
             * Contains the code to be run.
             */
            fun flyt(flowInit: () -> Unit) {
                flowFunction = flowInit
            }

            override fun name(): String = name
            override fun fired(): Boolean = fired
            override fun type(): RuleComponentType = GREN
            override fun toString(): String = "${type()}: ${fired().svarord()} ${name()}"
        }
    }
}





