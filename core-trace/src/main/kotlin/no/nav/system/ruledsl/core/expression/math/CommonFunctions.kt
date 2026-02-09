package no.nav.system.ruledsl.core.expression.math

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Verdi
import kotlin.math.abs as kotlinAbs
import kotlin.math.ceil as kotlinCeil
import kotlin.math.floor as kotlinFloor
import kotlin.math.max as kotlinMax
import kotlin.math.min as kotlinMin

/**
 * Core mathematical functions for the Rule DSL.
 *
 * These are fundamental mathematical operations that work with Expression<Number> and maintain
 * full traceability in rule explanations.
 */

fun floor(value: Expression<Number>): Expression<Double> =
    UnaryOperation(value, "floor") { kotlinFloor(value.value.toDouble()) }

fun ceil(value: Expression<Number>): Expression<Double> =
    UnaryOperation(value, "ceil") { kotlinCeil(value.value.toDouble()) }

fun abs(value: Expression<Number>): Expression<Double> =
    UnaryOperation(value, "abs") { kotlinAbs(value.value.toDouble()) }

fun min(a: Expression<Number>, b: Number): Expression<Double> =
    BinaryFunction(a, Verdi(b), "min") { kotlinMin(a.value.toDouble(), b.toDouble()) }

fun min(a: Number, b: Expression<Number>): Expression<Double> =
    BinaryFunction(Verdi(a), b, "min") { kotlinMin(a.toDouble(), b.value.toDouble()) }

fun min(a: Expression<Number>, b: Expression<Number>): Expression<Double> =
    BinaryFunction(a, b, "min") { kotlinMin(a.value.toDouble(), b.value.toDouble()) }

fun max(a: Expression<Number>, b: Number): Expression<Double> =
    BinaryFunction(a, Verdi(b), "max") { kotlinMax(a.value.toDouble(), b.toDouble()) }

fun max(a: Number, b: Expression<Number>): Expression<Double> =
    BinaryFunction(Verdi(a), b, "max") { kotlinMax(a.toDouble(), b.value.toDouble()) }

fun max(a: Expression<Number>, b: Expression<Number>): Expression<Double> =
    BinaryFunction(a, b, "max") { kotlinMax(a.value.toDouble(), b.value.toDouble()) }
