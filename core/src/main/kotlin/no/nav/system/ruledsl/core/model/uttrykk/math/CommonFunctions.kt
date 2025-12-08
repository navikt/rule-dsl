package no.nav.system.ruledsl.core.model.uttrykk.math

import no.nav.system.ruledsl.core.model.Uttrykk
import kotlin.math.abs as kotlinAbs
import kotlin.math.ceil as kotlinCeil
import kotlin.math.floor as kotlinFloor
import kotlin.math.max as kotlinMax
import kotlin.math.min as kotlinMin

/**
 * Core mathematical functions for the Rule DSL.
 *
 * These are fundamental mathematical operations that work with Uttrykk<Number> and maintain
 * full traceability in rule explanations.
 *
 * Domain-specific rounding or formatting functions should be implemented in client modules.
 */

/**
 * Floor function - returns the largest integer less than or equal to the value.
 *
 * Useful for integer division: `floor(months / 12)` instead of manual truncation.
 *
 * Example:
 * ```kotlin
 * val months = Faktum("months", 37)
 * val years = Faktum("years", floor(months / 12))
 * // years.verdi = 3.0
 * // years.uttrykk.notasjon() = "floor(months / 12)"
 * ```
 */
fun floor(value: Uttrykk<Number>): Uttrykk<Double> =
    UnaryOperation(value, "floor") { kotlinFloor(value.verdi.toDouble()) }

/**
 * Ceiling function - returns the smallest integer greater than or equal to the value.
 *
 * Example: `ceil(37.2)` returns `38.0`
 */
fun ceil(value: Uttrykk<Number>): Uttrykk<Double> =
    UnaryOperation(value, "ceil") { kotlinCeil(value.verdi.toDouble()) }

/**
 * Absolute value function - returns the non-negative value.
 *
 * Example: `abs(-5)` returns `5.0`, `abs(5)` returns `5.0`
 */
fun abs(value: Uttrykk<Number>): Uttrykk<Double> =
    UnaryOperation(value, "abs") { kotlinAbs(value.verdi.toDouble()) }

/**
 * Minimum of two values.
 *
 * Example:
 * ```kotlin
 * val trygdetid = Faktum("trygdetid", 45)
 * val maksimal = Faktum("maksimal", min(trygdetid, 40))
 * // maksimal.verdi = 40.0
 * // maksimal.uttrykk.notasjon() = "min(trygdetid, 40)"
 * ```
 */
fun min(a: Uttrykk<Number>, b: Number): Uttrykk<Double> =
    UnaryOperation(a, "min") { kotlinMin(a.verdi.toDouble(), b.toDouble()) }

fun min(a: Number, b: Uttrykk<Number>): Uttrykk<Double> =
    UnaryOperation(b, "min") { kotlinMin(a.toDouble(), b.verdi.toDouble()) }

fun min(a: Uttrykk<Number>, b: Uttrykk<Number>): Uttrykk<Double> =
    UnaryOperation(a, "min") { kotlinMin(a.verdi.toDouble(), b.verdi.toDouble()) }

/**
 * Maximum of two values.
 *
 * Example:
 * ```kotlin
 * val trygdetid = Faktum("trygdetid", 25)
 * val minimum = Faktum("minimum", max(trygdetid, 30))
 * // minimum.verdi = 30.0
 * // minimum.uttrykk.notasjon() = "max(trygdetid, 30)"
 * ```
 */
fun max(a: Uttrykk<Number>, b: Number): Uttrykk<Double> =
    UnaryOperation(a, "max") { kotlinMax(a.verdi.toDouble(), b.toDouble()) }

fun max(a: Number, b: Uttrykk<Number>): Uttrykk<Double> =
    UnaryOperation(b, "max") { kotlinMax(a.toDouble(), b.verdi.toDouble()) }

fun max(a: Uttrykk<Number>, b: Uttrykk<Number>): Uttrykk<Double> =
    UnaryOperation(a, "max") { kotlinMax(a.verdi.toDouble(), b.verdi.toDouble()) }
