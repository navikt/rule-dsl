package no.nav.pensjon.regler.sliterordning.functions

import no.nav.system.ruledsl.core.model.Uttrykk
import no.nav.system.ruledsl.core.model.uttrykk.math.UnaryOperation
import kotlin.math.roundToLong

/**
 * Domain-specific mathematical functions for Sliterordningen (pension calculation rules).
 *
 * These extend the core DSL with NAV-specific rounding and formatting operations.
 * Generic mathematical functions (floor, ceil, abs, etc.) are in core.functions.MathFunctions.
 */

/**
 * Rounds a number to 2 decimal places using standard rounding (half-up).
 *
 * This is a NAV-specific business rule for pension amounts that must be precise to 2 decimals.
 *
 * Example:
 * ```kotlin
 * val beløp = Faktum("beløp", 1234.5678)
 * val avrundet = Faktum("avrundet", roundTo2Decimals(beløp))
 * // avrundet.verdi = 1234.57
 * // avrundet.uttrykk.notasjon() = "roundTo2Decimals(beløp)"
 * ```
 *
 * This appears in HVORDAN explanations as:
 * ```
 * avrundet = roundTo2Decimals(beløp)
 * avrundet = roundTo2Decimals(1234.5678)
 * avrundet = 1234.57
 * ```
 */
fun avrund2desimal(value: Uttrykk<Number>): Uttrykk<Double> =
    UnaryOperation(value, "avrund2desimal") {
        (value.verdi.toDouble() * 100).roundToLong() / 100.0
    }