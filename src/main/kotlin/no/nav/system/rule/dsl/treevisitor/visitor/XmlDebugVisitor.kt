package no.nav.system.rule.dsl.treevisitor.visitor

import no.nav.system.rule.dsl.*
import no.nav.system.rule.dsl.rettsregel.AbstractSubsumsjon
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.ParSubsumsjon

/**
 * Lists the complete tree of [AbstractRuleComponent] in XML format.
 */
class XmlDebugVisitor : TreeVisitor {
    val debugString = StringBuilder()
    private var level = 0

    override fun visit(ruleComponent: AbstractRuleComponent) {
        debugString.append(" ".repeat(level * 2))
        debugString.append("<")

        if (ruleComponent !is Faktum<*>) {
            debugString.append("<")
        }

        when (ruleComponent) {
            is AbstractRuleService<*> -> {
                debugString.append(ruleComponent.name()).append(">").append("\n")
            }
            is AbstractRuleflow -> {
                debugString.append(ruleComponent.name()).append(">").append("\n")
            }
            is AbstractRuleflow.Decision -> {
                debugString.append(ruleComponent.name()).append(">").append("\n")
            }
            is AbstractRuleflow.Decision.Branch -> {
                debugString.append(ruleComponent.name()).append(" fired=${ruleComponent.fired()}").append(">").append("\n")
            }
            is AbstractRuleset<*> -> {
                debugString.append(ruleComponent.name()).append(">").append("\n")
            }
            is Rule -> {
                debugString.append("/")
                    .append(ruleComponent.name().replace("${ruleComponent.parent!!.name()}.", "").replace(" ", "_"))
                    .append(" fired=${ruleComponent.fired()}")
                if (ruleComponent.prettyDoc().isNotBlank()) {
                    debugString.append(" comment=\"${ruleComponent.prettyDoc()}\"")
                }
                debugString.append(">").append("\n")
            }
            is AbstractSubsumsjon -> {
                debugString
                    .append("subsumsjon")
                    .append(" fired=${ruleComponent.fired()}").append(">")
                    .append(ruleComponent)
                    .append("</subsumsjon>")
                    .append("\n")
            }
        }

        level++
        ruleComponent.children.forEach { it.accept(this) }
        level--

        if (ruleComponent !is Rule && ruleComponent !is Faktum<*>) {
            debugString.append(" ".repeat(level * 2))
            debugString.append("</${ruleComponent.name()}>\n")
        }
    }
}

fun AbstractRuleComponent.xmlDebug(): String {
    val xrd = XmlDebugVisitor()
    this.accept(xrd)
    return xrd.debugString.toString().trim()
}