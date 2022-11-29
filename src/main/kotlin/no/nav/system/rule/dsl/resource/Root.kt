package no.nav.system.rule.dsl.resource

import no.nav.system.rule.dsl.AbstractResource
import no.nav.system.rule.dsl.AbstractResourceAccessor
import no.nav.system.rule.dsl.AbstractRuleComponent

/**
 * Global resource.
 * Captures and stores root [AbstractRuleComponent].
 */
class Root(val arc: () -> AbstractRuleComponent) : AbstractResource()

fun AbstractResourceAccessor.root(): AbstractRuleComponent = getResource(Root::class).arc.invoke()