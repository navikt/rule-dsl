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

    private fun createTag(tagName: String, vararg attribs: Any) {
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

    override fun visit(ruleComponent: AbstractRuleComponent) {
        debugString.append(" ".repeat(level * 2))
//        debugString.append("<")

//        if (ruleComponent !is Faktum<*>) {
//            debugString.append("<")
//        }

        when (ruleComponent) {
            is AbstractRuleService<*> -> createTag(ruleComponent.name())   // {
//                debugString.append(ruleComponent.name()).append(">").append("\n")
//            }
            is AbstractRuleflow -> createTag(ruleComponent.name())//{
//                debugString.append(ruleComponent.name()).append(">").append("\n")
//            }
            is AbstractRuleflow.Decision -> createTag(ruleComponent.name()) // {
//                debugString.append(ruleComponent.name()).append(">").append("\n")
//            }
            is AbstractRuleflow.Decision.Branch -> createTag(
                ruleComponent.name(),
                " fired=${ruleComponent.fired()}"
            ) //{
//                debugString.append(ruleComponent.name()).append(" fired=${ruleComponent.fired()}").append(">").append("\n")
//            }
            is AbstractRuleset<*> -> createTag(ruleComponent.name()) //{
//                debugString.append(ruleComponent.name()).append(">").append("\n")
//            }
            is Rule -> {
                val tagName = ruleComponent.name().replace("${ruleComponent.parent!!.name()}.", "").replace(" ", "_")
                val comment =
                    if (ruleComponent.prettyDoc().isNotBlank()) " comment=\"${ruleComponent.prettyDoc()}\"" else ""
                createTag("/$tagName", " fired=${ruleComponent.fired()}", comment)
//                debugString.append("/")
//                    .append(ruleComponent.name().replace("${ruleComponent.parent!!.name()}.", "").replace(" ", "_"))
//                    .append(" fired=${ruleComponent.fired()}")
//                if (ruleComponent.prettyDoc().isNotBlank()) {
//                    debugString.append(" comment=\"${ruleComponent.prettyDoc()}\"")
//                }
//                debugString.append(">").append("\n")
            }

            is AbstractSubsumsjon -> createTag("subsumsjon", " fired=${ruleComponent.fired()}", ruleComponent) //{
//                debugString
//                    .append("subsumsjon")
//                    .append(" fired=${ruleComponent.fired()}").append(">")
//                    .append(ruleComponent)
//                    .append("</subsumsjon>")
//                    .append("\n")
//            }
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