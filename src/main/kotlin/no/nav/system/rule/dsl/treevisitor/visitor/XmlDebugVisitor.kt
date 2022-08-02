package no.nav.system.rule.dsl.treevisitor.visitor

import no.nav.system.rule.dsl.*

/**
 * Lists the complete tree of [AbstractRuleComponent] in XML format.
 */
class XmlDebugVisitor : TreeVisitor {
    val debugString = StringBuilder()
    private var level = 0

    override fun visit(ruleComponent: AbstractRuleComponent) {
        debugString.append(" ".repeat(level * 2))
        debugString.append("<")

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
            is Rule<*> -> {
                debugString.append("/")
                    .append(ruleComponent.name().replace("${ruleComponent.parent!!.name()}.", ""))
                    .append(" fired=${ruleComponent.fired()}")
                if (ruleComponent.prettyDoc().isNotBlank()) {
                    debugString.append(" comment=\"${ruleComponent.prettyDoc()}\"")
                }
                debugString.append(">").append("\n")
            }
            is Predicate -> {
                debugString
                    .append("predicate")
                    .append(" fired=${ruleComponent.fired()}").append(">")
                    .append(ruleComponent.evaluatedDomainText())
                    .append("</predicate>")
                    .append("\n")
            }
        }

        level++
        ruleComponent.children.forEach { it.accept(this) }
        level--

        if (ruleComponent !is Rule<*> && ruleComponent !is Predicate) {
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