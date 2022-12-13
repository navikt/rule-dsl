package no.nav.system.rule.dsl.inspections

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.AbstractRuleflow
import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.rettsregel.AbstractSubsumtion
import no.nav.system.rule.dsl.rettsregel.Faktum

/**
 * Lists the complete tree of [AbstractRuleComponent] in XML format.
 */

fun AbstractRuleComponent.xmlDebug(): String {
    val debugString = StringBuilder()
    inspect(this, debugString, 0)
    return debugString.toString().trim()
}

private fun inspect(arc: AbstractRuleComponent, debugString: StringBuilder, level: Int) {
    debugString.append(" ".repeat(level * 2))
    var tagName = arc.name()
    val relevantChildren = arc.children.filterNot { it is Faktum<*> }
    var leafElement = relevantChildren.isEmpty()

    when (arc) {
        is AbstractRuleflow.Decision.Branch -> {
            if (leafElement) {
                openAndCloseContentTag(debugString, tagName, "", " fired=\"${arc.fired()}\"")
            } else {
                openTag(debugString, tagName, " fired=${arc.fired()}")
            }
        }

        is Rule<*> -> {
            tagName = tagName.substringAfter(".").replace(" ", "_")
            val comment =
                if (arc.prettyDoc().isNotBlank()) " comment=\"${arc.prettyDoc()}\"" else ""
            if (leafElement) {
                openAndCloseContentTag(debugString, tagName, "", " fired=\"${arc.fired()}\"", comment)
            } else {
                openTag(debugString, tagName, " fired=\"${arc.fired()}\"", comment)
            }
        }

        is AbstractSubsumtion -> {
            leafElement = true
            openAndCloseContentTag(debugString, arc.type().toString(), arc.toString(), " fired=\"${arc.fired()}\"")
        }

        is Predicate -> {}
        else -> openTag(debugString, tagName)
    }
    relevantChildren.forEach { inspect(it, debugString, level + 1) }

    if (!leafElement) {
        debugString.append(" ".repeat(level * 2))
        closeTag(debugString, tagName)
    }
}

private fun openTag(debugString: StringBuilder, tagName: String, vararg attribs: String) {
    debugString.append("<").append(tagName)
    attribs.forEach { debugString.append(it) }
    debugString.append(">")
    debugString.append("\n")
}

private fun closeTag(debugString: StringBuilder, tagName: String) {
    debugString.append("</").append(tagName)
    debugString.append(">")
    debugString.append("\n")
}

private fun openAndCloseContentTag(
    debugString: StringBuilder,
    tagName: String,
    content: String,
    vararg attribs: String
) {
    debugString.append("<").append(tagName)
    attribs.forEach { debugString.append(it) }
    debugString.append(">")
    debugString.append(content)
    debugString.append("</$tagName>\n")
}

