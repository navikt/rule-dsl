package no.nav.system.ruledsl.core.inspections

import no.nav.system.ruledsl.core.model.arc.AbstractRuleComponent
import no.nav.system.ruledsl.core.model.arc.TrackableCondition

fun AbstractRuleComponent.debug(): String {
    val debugString = StringBuilder()
    debug(this, 0, debugString)
    return debugString.toString().trim()
}

private fun debug(arc: AbstractRuleComponent, level: Int, debugString: StringBuilder) {
    // Skip TrackableCondition - it's an internal implementation detail for forklaring
    if (arc is TrackableCondition) return

    debugString.append(" ".repeat(level * 2))
    debugString.append(arc.toString()).append("\n")

    arc.children.forEach {
        debug(it, level + 1, debugString)
    }
}
