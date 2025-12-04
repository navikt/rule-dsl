package no.nav.system.ruledsl.core.model

import no.nav.system.ruledsl.core.enums.RuleComponentType
import no.nav.system.ruledsl.core.rettsregel.Const
import no.nav.system.ruledsl.core.rettsregel.Faktum
import no.nav.system.ruledsl.core.rettsregel.Uttrykk
import org.jetbrains.annotations.TestOnly
import java.util.*
import kotlin.experimental.ExperimentalTypeInference

/**
 * Common ruleflow behaviour used by all ruleflow implementations.
 * Defines branching logic DSL (decision, branch, condition, flow).
 */
abstract class AbstractRuleflow<T : Any> : AbstractRuleComponent() {
    /**
     * Tracks the full name of nested branches.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val branchNameStack = Stack<String>()

    /**
     * Tests the ruleflow without a parent ruleComponent.
     */
    @TestOnly
    open fun test(): T {
        branchNameStack.push(this.javaClass.simpleName)
        return ruleflow.invoke()
    }

    /**
     * Runs the ruleflow
     */
    open fun run(parent: AbstractRuleComponent): T {
        parent.addChild(this)
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
        addChild(d)
        d.init()
        d.run()
        branchNameStack.pop()
    }

    override fun name(): String = this.javaClass.simpleName
    override fun fired(): Boolean = true
    override fun type(): RuleComponentType = RuleComponentType.REGELFLYT
    override fun toString(): String = "${type()}: ${name()}"

    /**
     * Represents a split in ruleflow logic. Each [Decision] can have multiple outcomes ([Branch]).
     */
    class Decision(
        private val name: String,
    ) : AbstractRuleComponent() {

        private var branchList = mutableListOf<Branch>()

        fun run() {
            val flowsToRun = mutableListOf<Branch>()
            branchList.forEach {
                it.fired = it.condition.verdi
                if (it.fired) {
                    flowsToRun.add(it)
                }
            }
            flowsToRun.forEach { branch ->
                branch.flowFunction.invoke()
            }
        }

        /**
         * DSL: Decision branch entry.
         * Defines a single branch inside a Decision.
         */
        fun gren(init: Branch.() -> Unit): Branch {
            val b = Branch("$name/gren ${branchList.size}")
            addChild(b)
            b.init()
            branchList.add(b)
            return b
        }

        override fun name(): String = name
        override fun fired(): Boolean = true
        override fun type(): RuleComponentType = RuleComponentType.FORGRENING
        override fun toString(): String = "${type()}: ${name()}"

        class Branch(
            defaultName: String,
        ) : AbstractRuleComponent() {
            lateinit var condition: Uttrykk<Boolean>
            lateinit var flowFunction: () -> Unit
            private var betingelseName: String = defaultName

            var fired = false

            /**
             * DSL: Branch condition entry.
             * Defines a boolean condition that must be evaluated to true for the following [flyt] to be run.
             */
            fun betingelse(booleanFunction: () -> Boolean) {
                condition = Const(booleanFunction.invoke())
            }

            fun betingelse(name: String, booleanFunction: () -> Boolean) {
                condition = Faktum(name, booleanFunction())
                betingelseName = name
            }

            @OptIn(ExperimentalTypeInference::class)
            @OverloadResolutionByLambdaReturnType
            @JvmName("UttrykkBooleanBetingelse")
            @DslDomainPredicate
            fun betingelse(name: String, uttrykkFunction: () -> Uttrykk<Boolean>) {
                condition = uttrykkFunction()
                betingelseName = name
            }

            /**
             * DSL: Branch flow entry.
             * Contains the code to be run.
             */
            fun flyt(flowInit: () -> Unit) {
                flowFunction = flowInit
            }

            override fun name(): String = betingelseName
            override fun fired(): Boolean = fired
            override fun type(): RuleComponentType = RuleComponentType.GREN

            override fun toString(): String = "${type()}: $condition"

        }
    }
}





