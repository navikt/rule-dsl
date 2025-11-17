package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.REGELTJENESTE
import no.nav.system.rule.dsl.resource.ExecutionTrace
import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Uttrykk

/**
 * Top level ruleComponent encapusaltes a complete ruleservice.
 */
abstract class AbstractRuleService<out T> : AbstractRuleComponent() {
    protected abstract val ruleService: () -> T

    /**
     * Runs the ruleservice.
     *
     * Pushes itself to ExecutionTrace (if enabled) before running, pops after.
     *
     * @return value [T]
     */
    open fun run(): T {
        // Get trace if it exists (may not be enabled)
        val trace = try {
            getResource(ExecutionTrace::class)
        } catch (e: Exception) {
            null
        }

        trace?.push(this)
        try {
            return ruleService.invoke()
        } finally {
            trace?.pop()
        }
    }

    override fun name(): String = this.javaClass.simpleName
    override fun fired(): Boolean = true
    override fun type(): RuleComponentType = REGELTJENESTE
    override fun toString(): String = "${type()}: ${name()}"
    override fun toUttrykk(): Uttrykk<*> = Const("${type()}: ${name()}")
}