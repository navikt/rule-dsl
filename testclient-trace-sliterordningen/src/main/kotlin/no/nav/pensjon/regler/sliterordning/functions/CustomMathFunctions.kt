package no.nav.pensjon.regler.sliterordning.functions

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.math.UnaryOperation
import kotlin.math.roundToLong

/**
 * Domain-specific mathematical functions for Sliterordningen (pension calculation rules).
 *
 * These extend the core DSL with NAV-specific rounding and formatting operations.
 */

/**
 * Rounds a number to 2 decimal places using standard rounding (half-up).
 *
 * This is a NAV-specific business rule for pension amounts that must be precise to 2 decimals.
 *
 * Example:
 * ```kotlin
 * val beløp = Faktum("beløp", 1234.5678)
 * val avrundet = Faktum("avrundet", avrund2desimal(beløp))
 * // avrundet.value = 1234.57
 * // avrundet.notation() = "avrund2desimal(beløp)"
 * ```
 */
fun avrund2desimal(value: Expression<Number>): Expression<Double> =
    UnaryOperation(value, "avrund2desimal") {
        (value.value.toDouble() * 100).roundToLong() / 100.0
    }
