package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.REGELTJENESTE

/**
 * Top level ruleComponent encapusaltes a complete ruleservice.
 */
abstract class AbstractRuleService<out T> : AbstractResourceAccessor() {
    protected abstract val ruleService: () -> T

    /**
     * Runs the ruleservice.
     *
     * @return value [T]
     */
    open fun run(): T {
        return ruleService.invoke()
    }

    override fun name(): String = this.javaClass.simpleName
    override fun fired(): Boolean = true
    override fun type(): RuleComponentType = REGELTJENESTE
    override fun toString(): String = "${type()}: ${name()}"
}