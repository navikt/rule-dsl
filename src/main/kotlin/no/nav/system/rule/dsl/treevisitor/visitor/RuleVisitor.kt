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

    override fun visit(arc: AbstractRuleComponent) {
        if (arc is Rule<*> && searchFunction.invoke(arc)) {
            rule = arc
        } else {
            arc.children.forEach { it.accept(this) }
        }
    }
}