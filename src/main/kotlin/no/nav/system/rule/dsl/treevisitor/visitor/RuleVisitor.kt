package no.nav.system.rule.dsl.treevisitor.visitor

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.treevisitor.Conclusion

/**
 * Searches the ruleComponent tree for a target rule using [searchFunction] and collects the conclusion.
 */
class RuleVisitor(
    private val searchFunction: (Rule<*>) -> Boolean
) : TreeVisitor {
    var conclusion: Conclusion = Conclusion()

    override fun visit(ruleComponent: AbstractRuleComponent) {
        if (ruleComponent is Rule<*> && searchFunction.invoke(ruleComponent)) {
            conclusion = ruleComponent.conclusion()
        } else {
            ruleComponent.children.forEach { it.accept(this) }
        }
    }
}