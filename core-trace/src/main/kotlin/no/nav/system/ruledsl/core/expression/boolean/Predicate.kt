package no.nav.system.ruledsl.core.expression.boolean

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum

class GuardExpression(
    private val evaluator: () -> Boolean
) : Expression<Boolean> {
    override val value: Boolean by lazy { evaluator() }
    override fun notation() = value.toString()
    override fun concrete() = value.toString()
    override fun faktumSet() = emptySet<Faktum<*>>()
}