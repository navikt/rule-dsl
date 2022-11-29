package no.nav.system.rule.dsl.visitor

import no.nav.system.rule.dsl.AbstractRuleComponent

interface TreeVisitor {
    fun visit(arc: AbstractRuleComponent)
}