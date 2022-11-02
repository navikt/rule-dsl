package no.nav.system.rule.dsl.treevisitor.visitor

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.AbstractRuleflow
import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.rettsregel.AbstractSubsumtion
import no.nav.system.rule.dsl.rettsregel.Fact

/**
 * Lists the complete tree of [AbstractRuleComponent] in XML format.
 */
class XmlDebugVisitor : TreeVisitor {
    val debugString = StringBuilder()
    private var level = 0

    override fun visit(ruleComponent: AbstractRuleComponent) {
        debugString.append(" ".repeat(level * 2))

        var tagName = ruleComponent.name()
        val relevantChildren = ruleComponent.children.filterNot { it is Fact<*> }
        var leafElement = relevantChildren.isEmpty()

        when (ruleComponent) {
            is AbstractRuleflow.Decision.Branch -> {
                if (leafElement) {
                    openAndCloseContentTag(tagName, "", " fired=\"${ruleComponent.fired()}\"")
                } else {
                    openTag(tagName, " fired=${ruleComponent.fired()}")
                }
            }
            is Rule<*> -> {
                tagName = tagName.replace("${ruleComponent.parent!!.name()}.", "").replace(" ", "_")
                val comment =
                    if (ruleComponent.prettyDoc().isNotBlank()) " comment=\"${ruleComponent.prettyDoc()}\"" else ""
                if (leafElement) {
                    openAndCloseContentTag(tagName, "", " fired=\"${ruleComponent.fired()}\"", comment)
                } else {
                    openTag(tagName, " fired=\"${ruleComponent.fired()}\"", comment)
                }
            }
            is AbstractSubsumtion -> {
                leafElement = true
                openAndCloseContentTag(ruleComponent.type().toString(), ruleComponent.toString(), " fired=\"${ruleComponent.fired()}\"")
            }
            is Predicate -> {}
            else -> openTag(tagName)
        }

        level++
        relevantChildren.forEach { it.accept(this) }
        level--

        if (!leafElement) {
            debugString.append(" ".repeat(level * 2))
            closeTag(tagName)
        }
    }

    private fun openTag(tagName: String, vararg attribs: String) {
        debugString.append("<").append(tagName)
        attribs.forEach { debugString.append(it) }
        debugString.append(">")
        debugString.append("\n")
    }

    private fun closeTag(tagName: String) {
        debugString.append("</").append(tagName)
        debugString.append(">")
        debugString.append("\n")
    }

    private fun openAndCloseContentTag(tagName: String, content: String, vararg attribs: String) {
        debugString.append("<").append(tagName)
        attribs.forEach { debugString.append(it) }
        debugString.append(">")
        debugString.append(content)
        debugString.append("</$tagName>\n")
    }
}

fun AbstractRuleComponent.xmlDebug(): String {
    val xrd = XmlDebugVisitor()
    this.accept(xrd)
    return xrd.debugString.toString().trim()
}