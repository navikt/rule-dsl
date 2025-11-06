package no.nav.system.rule.dsl.inspections

import no.nav.system.rule.dsl.AbstractRuleComponent


fun AbstractRuleComponent.debug(): String {
    val debugString = StringBuilder()
    inspect(this, 0, debugString)
    return debugString.toString().trim()
}

private fun inspect(arc: AbstractRuleComponent, level: Int, debugString: StringBuilder) {
    debugString.append(" ".repeat(level * 2))
    debugString.append(arc.toString()).append("\n")

    arc.children.forEach {
        inspect(it, level + 1, debugString)
    }
}
