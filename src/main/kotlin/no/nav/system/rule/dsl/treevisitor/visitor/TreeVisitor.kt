package no.nav.system.rule.dsl.treevisitor.visitor

import no.nav.system.rule.dsl.AbstractRuleComponent

interface TreeVisitor {
    fun visit(ruleComponent: AbstractRuleComponent)
}