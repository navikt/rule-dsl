package no.nav.system.rule.dsl.inspections

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.rettsregel.helper.isLeafPairSubsumtion


fun AbstractRuleComponent.debug(includeLeafFaktum: Boolean = false) : String {
    val debugString = StringBuilder()
    inspect(this, 0, includeLeafFaktum, debugString)
    return debugString.toString().trim()
}

private fun inspect(arc: AbstractRuleComponent, level: Int, includeLeafFaktum: Boolean, debugString: StringBuilder) {
    debugString.append(" ".repeat(level * 2))
    debugString.append(arc.toString()).append("\n")

    if (!includeLeafFaktum && arc.isLeafPairSubsumtion()) return

    arc.children.forEach { inspect(it, level + 1, includeLeafFaktum, debugString) }
}
