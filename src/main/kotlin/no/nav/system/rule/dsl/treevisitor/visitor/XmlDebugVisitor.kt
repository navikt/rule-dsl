package no.nav.system.rule.dsl.treevisitor.visitor

import no.nav.system.rule.dsl.*
import no.nav.system.rule.dsl.rettsregel.AbstractSubsumsjon
import no.nav.system.rule.dsl.rettsregel.Faktum

/**
 * Lists the complete tree of [AbstractRuleComponent] in XML format.
 */
class XmlDebugVisitor : TreeVisitor {
    val debugString = StringBuilder()
    private var level = 0

    private fun createTag2(tagName: String, vararg attribs: Any) {
        debugString
            .append("<").append(tagName)
        var hasAbstractSubsumsjon = false
        attribs.forEach {
            if (it is AbstractSubsumsjon) {
                debugString.append(">").append(it)
                hasAbstractSubsumsjon = true
            } else
                debugString.append(it)
        }
        if (hasAbstractSubsumsjon)
            debugString.append("</").append(tagName)
                .append(">").append("\n")
        else
            debugString
                .append(">").append("\n")
    }

    private fun createTag(tagName: String, vararg attribs: String) {
        debugString.append("<").append(tagName)
        attribs.forEach { debugString.append(it) }
        debugString.append(">")
        debugString.append("\n")
    }

    private fun createContentTag(tagName: String, content: String, vararg attribs: String) {
        debugString.append("<").append(tagName)
        attribs.forEach { debugString.append(it) }
        debugString.append(">")
        debugString.append(content)
        debugString.append("</$tagName>\n")
    }

    override fun visit(ruleComponent: AbstractRuleComponent) {
        debugString.append(" ".repeat(level * 2))

        when (ruleComponent) {
            is AbstractRuleflow.Decision.Branch -> createTag(ruleComponent.name(), " fired=\"${ruleComponent.fired()}\"")
            is Rule -> {
                val tagName = ruleComponent.name().replace("${ruleComponent.parent!!.name()}.", "").replace(" ", "_")
                val comment =
                    if (ruleComponent.prettyDoc().isNotBlank()) " comment=\"${ruleComponent.prettyDoc()}\"" else ""
                createTag("/$tagName", " fired=${ruleComponent.fired()}", comment)
            }

            is AbstractSubsumsjon -> createContentTag("subsumsjon", ruleComponent.toString()," type=\"${ruleComponent.type().name}\""," fired=\"${ruleComponent.fired()}\"") //{
            else -> createTag(ruleComponent.name())
        }

        level++
        ruleComponent.children.forEach { if (it !is Faktum<*>) it.accept(this) }
        level--

        if (ruleComponent !is Rule && ruleComponent !is AbstractSubsumsjon) {
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