package no.nav.system.rule.dsl.treevisitor.visitor

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.Rule

/**
 * Searches the ruleComponent tree for a target rule using [searchFunction].
 */
class RuleVisitor(
    private val searchFunction: (Rule<*>) -> Boolean,
) : TreeVisitor {
    var rule: Rule<*>? = null

    override fun visit(ruleComponent: AbstractRuleComponent) {
        if (ruleComponent is Rule<*> && searchFunction.invoke(ruleComponent)) {
            rule = ruleComponent
        } else {
            ruleComponent.children.forEach { it.accept(this) }
        }
    }
}