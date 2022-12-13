package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType

/**
 * Common functionality across all components of the DSL.
 *
 * All rulecomponents are organized in a tree of rulecomponents using [children]. Navigate the
 * rulecomponenttree by providing a [TreeVisitor] in the [accept] method.
 *
 * A [AbstractResourceAccessor.resourceMap] keeps track of all instantiated resources for convinient access during rule processing.
 * Resources are all classes that needs to be instantiated once per ruleService call, typically resources
 * are Rates (norsk "Sats"), Console-capture or anything else non-static.
 */
abstract class AbstractRuleComponent {
    val children: MutableList<AbstractRuleComponent> = mutableListOf()

    abstract fun name(): String
    abstract fun type(): RuleComponentType
    abstract fun fired(): Boolean

}