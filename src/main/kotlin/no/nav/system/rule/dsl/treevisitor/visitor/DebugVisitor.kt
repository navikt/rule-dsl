package no.nav.system.rule.dsl.treevisitor.visitor

import no.nav.system.rule.dsl.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.MengdeSubsumsjon
import no.nav.system.rule.dsl.rettsregel.ParSubsumsjon
import svarord

/**
 * Lists the complete tree of [AbstractRuleComponent]
 */
class DebugVisitor : TreeVisitor {
    val debugString = StringBuilder()
    private var level = 0

    override fun visit(ruleComponent: AbstractRuleComponent) {
        debugString.append(" ".repeat(level * 2))
        debugString.append(ruleComponent.toString()).append("\n")
        when (ruleComponent) {
            is Faktum<*> -> {
                debugString.append("faktum: $ruleComponent\n")
            }
            is ParSubsumsjon -> {
                debugString.append("parsubsumsjon: $ruleComponent\n")
            }
            is MengdeSubsumsjon -> {
                debugString.append("mengdesubsumsjon: $ruleComponent\n")
            }
            is Predicate -> {
                debugString.append("predicate: ${ruleComponent.fired().svarord()}\n")
            }
            is Rule -> {
                val utfallTekst = ruleComponent.utfall?.let { " utfallType: ${it.utfallType}" } ?: ""
                debugString.append("rule: ${ruleComponent.fired().svarord()} ${ruleComponent.name()} $utfallTekst\n")
            }
            is AbstractRuleset<*> -> {
                debugString.append("ruleset: ${ruleComponent.rulesetName}\n")
            }
            is AbstractRuleflow -> {
                debugString.append("ruleflow: ${ruleComponent.javaClass.simpleName}\n")
            }
            is AbstractRuleflow.Decision -> {
                debugString.append("decision: ${ruleComponent.name()}\n")
            }
            is AbstractRuleflow.Decision.Branch -> {
                debugString.append("branch: ${ruleComponent.fired().svarord()} ${ruleComponent.name()}\n")
            }
            is AbstractRuleService<*> -> {
                debugString.append("ruleservice: ${ruleComponent.name()}\n")
            }
        }

        level++
        ruleComponent.children.forEach { it.accept(this) }
        level--
    }
}

fun AbstractRuleComponent.debug(): String {
    val fdv = DebugVisitor()
    this.accept(fdv)
    return fdv.debugString.toString().trim()
}