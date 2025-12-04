package no.nav.system.ruledsl.core.resource

import no.nav.system.ruledsl.core.model.AbstractRuleComponent

/**
 * Global resource.
 * Captures and stores root [AbstractRuleComponent].
 */
class Root(val arc: () -> AbstractRuleComponent) : AbstractResource()

fun AbstractRuleComponent.root(): AbstractRuleComponent = getResource(Root::class).arc.invoke()