package no.nav.system.ruledsl.core.expression

/**
 * Base interface for all operators (pair-, list and math operations).
 */
interface Operator {
    val text: String
}

interface NegatableOperator : Operator {
    fun negated(): String
}