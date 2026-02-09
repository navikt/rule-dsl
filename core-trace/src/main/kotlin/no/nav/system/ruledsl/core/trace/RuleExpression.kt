package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.Verdi
import no.nav.system.ruledsl.core.expression.boolean.ListOperator
import no.nav.system.ruledsl.core.expression.boolean.ListOperation

/**
 * Expression representing the result of a rule evaluation.
 * 
 * Can be used directly in HVIS/OG predicates of subsequent rules:
 * ```
 * val r1 = regel("First") { ... }
 * regel("Second") {
 *     HVIS { r1 }  // Traces as "regel 'First' har truffet" or "har ikke truffet"
 * }
 * ```
 */
class RuleExpression internal constructor(
    private val name: String,
    private val fired: Boolean
) : Expression<Boolean> {
    override val value: Boolean get() = fired
    override fun notation() = "regel '$name'"
    override fun concrete() = if (fired) "har truffet" else "har ikke truffet"
    override fun faktumSet(): Set<Faktum<*>> = emptySet()
    override fun toString() = "${notation()} ${concrete()}"
    
    fun harTruffet() = fired
    fun harIkkeTruffet() = !fired
}

/**
 * Wraps a list of RuleExpressions as an Expression for use in list operations.
 */
private class RuleExpressionList(
    private val results: List<RuleExpression>
) : Expression<List<Boolean>> {
    override val value: List<Boolean> get() = results.map { it.value }
    override fun notation() = "${results.size} regler"
    override fun concrete() = results.map { if (it.value) "✓" else "✗" }.toString()
    override fun faktumSet(): Set<Faktum<*>> = emptySet()
}

/**
 * Extension for checking if at least one rule in a group fired.
 * Uses ListOperation with MINST_EN_AV operator for consistent notation.
 */
fun List<RuleExpression>.minstEnHarTruffet(): Expression<Boolean> = ListOperation(
    expression = Verdi("truffet", true),
    list = RuleExpressionList(this),
    operator = ListOperator.MINST_EN_AV,
    evaluator = { this.any { it.harTruffet() } }
)

/**
 * Extension for checking if no rules in a group fired.
 * Uses ListOperation with INGEN operator for consistent notation.
 */
fun List<RuleExpression>.ingenHarTruffet(): Expression<Boolean> = ListOperation(
    expression = Verdi("truffet", true),
    list = RuleExpressionList(this),
    operator = ListOperator.INGEN,
    evaluator = { this.none { it.harTruffet() } }
)

/**
 * Extension for checking if all rules in a group fired.
 * Uses ListOperation with ALLE operator for consistent notation.
 */
fun List<RuleExpression>.alleHarTruffet(): Expression<Boolean> = ListOperation(
    expression = Verdi("truffet", true),
    list = RuleExpressionList(this),
    operator = ListOperator.ALLE,
    evaluator = { this.all { it.harTruffet() } }
)
