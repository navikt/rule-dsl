package no.nav.system.ruledsl.core.model.arc

import no.nav.system.ruledsl.core.enums.RuleComponentType

/**
 * Top level ruleComponent encapusaltes a complete ruleservice.
 */
abstract class AbstractRuleService<out T> : AbstractRuleComponent() {
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
    override fun type(): RuleComponentType = RuleComponentType.REGELTJENESTE
    override fun toString(): String = "${type()}: ${name()}"
}