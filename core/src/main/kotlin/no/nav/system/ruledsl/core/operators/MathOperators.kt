package no.nav.system.ruledsl.core.operators

import no.nav.system.ruledsl.core.model.Const
import no.nav.system.ruledsl.core.model.MathOperation
import no.nav.system.ruledsl.core.model.Uttrykk

/**
 * Internal helper functions to reduce boilerplate in math operator definitions.
 * These functions handle the creation of MathOperation objects with appropriate wrapping of constants.
 */
private inline fun <T : Number> binaryMathOp(
    left: Uttrykk<Number>,
    right: Uttrykk<Number>,
    operator: MathOperator,
    crossinline eval: () -> T
): Uttrykk<T> = MathOperation(left, right, operator) { eval() }

private inline fun <T : Number> numberUttrykk(
    num: Number,
    uttrykk: Uttrykk<Number>,
    operator: MathOperator,
    crossinline eval: () -> T
): Uttrykk<T> = binaryMathOp(Const(num), uttrykk, operator, eval)

private inline fun <T : Number> uttrykkNumber(
    uttrykk: Uttrykk<Number>,
    num: Number,
    operator: MathOperator,
    crossinline eval: () -> T
): Uttrykk<T> = binaryMathOp(uttrykk, Const(num), operator, eval)

/**
 * Plus operators
 */
@JvmName("plusIntFaktumInt")
operator fun Int.plus(right: Uttrykk<Int>): Uttrykk<Int> =
    numberUttrykk(this, right, MathOperator.ADD) { this + right.verdi }

@JvmName("plusIntFaktumDouble")
operator fun Int.plus(right: Uttrykk<Double>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.ADD) { this + right.verdi }

@JvmName("plusDoubleFaktumInt")
operator fun Double.plus(right: Uttrykk<Int>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.ADD) { this + right.verdi }

@JvmName("plusDoubleFaktumDouble")
operator fun Double.plus(right: Uttrykk<Double>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.ADD) { this + right.verdi }

@JvmName("plusFaktumInt")
operator fun Uttrykk<Int>.plus(right: Int): Uttrykk<Int> =
    uttrykkNumber(this, right, MathOperator.ADD) { this.verdi + right }

@JvmName("plusFaktumDouble")
operator fun Uttrykk<Int>.plus(right: Double): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.ADD) { this.verdi + right }

@JvmName("plusFaktumIntInt")
operator fun Uttrykk<Int>.plus(right: Uttrykk<Int>): Uttrykk<Int> =
    binaryMathOp(this, right, MathOperator.ADD) { this.verdi + right.verdi }

@JvmName("plusFaktumIntDouble")
operator fun Uttrykk<Int>.plus(right: Uttrykk<Double>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.ADD) { this.verdi + right.verdi }

@JvmName("plusFaktumDoubleInt")
operator fun Uttrykk<Double>.plus(right: Uttrykk<Int>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.ADD) { this.verdi + right.verdi }

@JvmName("plusFaktumDoubleDouble")
operator fun Uttrykk<Double>.plus(right: Uttrykk<Double>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.ADD) { this.verdi + right.verdi }

@JvmName("plusFaktumDoubleInt2")
operator fun Uttrykk<Double>.plus(right: Int): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.ADD) { this.verdi + right }

@JvmName("plusFaktumDoubleDouble2")
operator fun Uttrykk<Double>.plus(right: Double): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.ADD) { this.verdi + right }

/**
 * Minus operators
 */
@JvmName("minusIntFaktumInt")
operator fun Int.minus(right: Uttrykk<Int>): Uttrykk<Int> =
    numberUttrykk(this, right, MathOperator.SUB) { this - right.verdi }

@JvmName("minusIntFaktumDouble")
operator fun Int.minus(right: Uttrykk<Double>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.SUB) { this - right.verdi }

@JvmName("minusDoubleFaktumInt")
operator fun Double.minus(right: Uttrykk<Int>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.SUB) { this - right.verdi }

@JvmName("minusDoubleFaktumDouble")
operator fun Double.minus(right: Uttrykk<Double>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.SUB) { this - right.verdi }

@JvmName("minusFaktumInt")
operator fun Uttrykk<Int>.minus(right: Int): Uttrykk<Int> =
    uttrykkNumber(this, right, MathOperator.SUB) { this.verdi - right }

@JvmName("minusFaktumDouble")
operator fun Uttrykk<Int>.minus(right: Double): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.SUB) { this.verdi - right }

@JvmName("minusFaktumIntInt")
operator fun Uttrykk<Int>.minus(right: Uttrykk<Int>): Uttrykk<Int> =
    binaryMathOp(this, right, MathOperator.SUB) { this.verdi - right.verdi }

@JvmName("minusFaktumIntDouble")
operator fun Uttrykk<Int>.minus(right: Uttrykk<Double>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.SUB) { this.verdi - right.verdi }

@JvmName("minusFaktumDoubleInt")
operator fun Uttrykk<Double>.minus(right: Uttrykk<Int>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.SUB) { this.verdi - right.verdi }

@JvmName("minusFaktumDoubleDouble")
operator fun Uttrykk<Double>.minus(right: Uttrykk<Double>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.SUB) { this.verdi - right.verdi }

@JvmName("minusFaktumDoubleInt2")
operator fun Uttrykk<Double>.minus(right: Int): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.SUB) { this.verdi - right }

@JvmName("minusFaktumDoubleDouble2")
operator fun Uttrykk<Double>.minus(right: Double): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.SUB) { this.verdi - right }

/**
 * Times operators
 */
@JvmName("timesIntFaktumInt")
operator fun Int.times(right: Uttrykk<Int>): Uttrykk<Int> =
    numberUttrykk(this, right, MathOperator.MUL) { this * right.verdi }

@JvmName("timesIntFaktumDouble")
operator fun Int.times(right: Uttrykk<Double>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.MUL) { this * right.verdi }

@JvmName("timesDoubleFaktumInt")
operator fun Double.times(right: Uttrykk<Int>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.MUL) { this * right.verdi }

@JvmName("timesDoubleFaktumDouble")
operator fun Double.times(right: Uttrykk<Double>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.MUL) { this * right.verdi }

@JvmName("timesFaktumInt")
operator fun Uttrykk<Int>.times(right: Int): Uttrykk<Int> =
    uttrykkNumber(this, right, MathOperator.MUL) { this.verdi * right }

@JvmName("timesFaktumDouble")
operator fun Uttrykk<Int>.times(right: Double): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.MUL) { this.verdi * right }

@JvmName("timesFaktumIntInt")
operator fun Uttrykk<Int>.times(right: Uttrykk<Int>): Uttrykk<Int> =
    binaryMathOp(this, right, MathOperator.MUL) { this.verdi * right.verdi }

@JvmName("timesFaktumIntDouble")
operator fun Uttrykk<Int>.times(right: Uttrykk<Double>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.MUL) { this.verdi * right.verdi }

@JvmName("timesFaktumDoubleInt")
operator fun Uttrykk<Double>.times(right: Uttrykk<Int>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.MUL) { this.verdi * right.verdi }

@JvmName("timesFaktumDoubleDouble")
operator fun Uttrykk<Double>.times(right: Uttrykk<Double>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.MUL) { this.verdi * right.verdi }

@JvmName("timesFaktumDoubleInt2")
operator fun Uttrykk<Double>.times(right: Int): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.MUL) { this.verdi * right }

@JvmName("timesFaktumDoubleDouble2")
operator fun Uttrykk<Double>.times(right: Double): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.MUL) { this.verdi * right }

/**
 * Division operators
 * Note: Division always returns Double to preserve precision.
 * Division-by-zero is checked centrally in MathOperation to prevent silent NaN/Infinity.
 */
@JvmName("divIntFaktumInt")
operator fun Int.div(right: Uttrykk<Int>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.DIV) { this / right.verdi.toDouble() }

@JvmName("divIntFaktumDouble")
operator fun Int.div(right: Uttrykk<Double>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.DIV) { this / right.verdi }

@JvmName("divDoubleFaktumInt")
operator fun Double.div(right: Uttrykk<Int>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.DIV) { this / right.verdi.toDouble() }

@JvmName("divDoubleFaktumDouble")
operator fun Double.div(right: Uttrykk<Double>): Uttrykk<Double> =
    numberUttrykk(this, right, MathOperator.DIV) { this / right.verdi }

@JvmName("divFaktumInt")
operator fun Uttrykk<Int>.div(right: Int): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.DIV) { this.verdi / right.toDouble() }

@JvmName("divFaktumDouble")
operator fun Uttrykk<Int>.div(right: Double): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.DIV) { this.verdi / right }

@JvmName("divFaktumIntInt")
operator fun Uttrykk<Int>.div(right: Uttrykk<Int>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.DIV) { this.verdi / right.verdi.toDouble() }

@JvmName("divFaktumIntDouble")
operator fun Uttrykk<Int>.div(right: Uttrykk<Double>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.DIV) { this.verdi / right.verdi }

@JvmName("divFaktumDoubleInt")
operator fun Uttrykk<Double>.div(right: Uttrykk<Int>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.DIV) { this.verdi / right.verdi.toDouble() }

@JvmName("divFaktumDoubleDouble")
operator fun Uttrykk<Double>.div(right: Uttrykk<Double>): Uttrykk<Double> =
    binaryMathOp(this, right, MathOperator.DIV) { this.verdi / right.verdi }

@JvmName("divFaktumDoubleInt2")
operator fun Uttrykk<Double>.div(right: Int): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.DIV) { this.verdi / right.toDouble() }

@JvmName("divFaktumDoubleDouble2")
operator fun Uttrykk<Double>.div(right: Double): Uttrykk<Double> =
    uttrykkNumber(this, right, MathOperator.DIV) { this.verdi / right }
