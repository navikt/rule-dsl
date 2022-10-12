package no.nav.system.rule.dsl.treevisitor.visitor

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.treevisitor.Rettsregel
import no.nav.system.rule.dsl.treevisitor.TomRettsregel

/**
 * Searches the ruleComponent tree for a target rule using [searchFunction].
 */
class RuleVisitor(
    private val searchFunction: (Rule) -> Boolean
) : TreeVisitor {
    var rule: Rule = TomRettsregel()

    override fun visit(ruleComponent: AbstractRuleComponent) {
        if (ruleComponent is Rule && searchFunction.invoke(ruleComponent)) {
            rule = ruleComponent
        } else {
            ruleComponent.children.forEach { it.accept(this) }
        }
    }
}

/**
 * Searches the ruleComponent tree for a target rettsregel using [searchFunction].
 */
class RettsregelVisitor(
    private val searchFunction: (Rettsregel) -> Boolean
) : TreeVisitor {
    var rettsregel: Rettsregel = TomRettsregel()

    override fun visit(ruleComponent: AbstractRuleComponent) {
        if (ruleComponent is Rettsregel && searchFunction.invoke(ruleComponent)) {
            rettsregel = ruleComponent
        } else {
            ruleComponent.children.forEach { it.accept(this) }
        }
    }
}