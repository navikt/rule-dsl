package no.nav.system.ruledsl.core.expression.math

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Const
import no.nav.system.ruledsl.core.expression.Operator

/**
 * Internal helper functions to reduce boilerplate in math operator definitions.
 * These functions handle the creation of BinaryOperation objects with appropriate wrapping of constants.
 */
private inline fun <T : Number> binaryMathOp(
    left: Expression<Number>,
    right: Expression<Number>,
    operator: MathOperator,
    crossinline eval: () -> T
): Expression<T> = BinaryOperation(left, right, operator) { eval() }

private inline fun <T : Number> numberUttrykk(
    num: Number,
    expression: Expression<Number>,
    operator: MathOperator,
    crossinline eval: () -> T
): Expression<T> = binaryMathOp(Const(num), expression, operator, eval)

private inline fun <T : Number> uttrykkNumber(
    expression: Expression<Number>,
    num: Number,
    operator: MathOperator,
    crossinline eval: () -> T
): Expression<T> = binaryMathOp(expression, Const(num), operator, eval)

enum class MathOperator(override val text: String) : Operator {
    ADD(" + "),
    SUB(" - "),
    MUL(" * "),
    DIV(" / ");
}

/**
 * Plus operators
 */
@JvmName("plusIntFaktumInt")
operator fun Int.plus(right: Expression<Int>): Expression<Int> =
    numberUttrykk(this, right, MathOperator.ADD) { this + right.value }

@JvmName("plusIntFaktumDouble")
operator fun Int.plus(right: Expression<Double>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.ADD) { this + right.value }

@JvmName("plusDoubleFaktumInt")
operator fun Double.plus(right: Expression<Int>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.ADD) { this + right.value }

@JvmName("plusDoubleFaktumDouble")
operator fun Double.plus(right: Expression<Double>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.ADD) { this + right.value }

@JvmName("plusFaktumInt")
operator fun Expression<Int>.plus(right: Int): Expression<Int> =
    uttrykkNumber(this, right, MathOperator.ADD) { this.value + right }

@JvmName("plusFaktumDouble")
operator fun Expression<Int>.plus(right: Double): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.ADD) { this.value + right }

@JvmName("plusFaktumIntInt")
operator fun Expression<Int>.plus(right: Expression<Int>): Expression<Int> =
    binaryMathOp(this, right, MathOperator.ADD) { this.value + right.value }

@JvmName("plusFaktumIntDouble")
operator fun Expression<Int>.plus(right: Expression<Double>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.ADD) { this.value + right.value }

@JvmName("plusFaktumDoubleInt")
operator fun Expression<Double>.plus(right: Expression<Int>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.ADD) { this.value + right.value }

@JvmName("plusFaktumDoubleDouble")
operator fun Expression<Double>.plus(right: Expression<Double>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.ADD) { this.value + right.value }

@JvmName("plusFaktumDoubleInt2")
operator fun Expression<Double>.plus(right: Int): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.ADD) { this.value + right }

@JvmName("plusFaktumDoubleDouble2")
operator fun Expression<Double>.plus(right: Double): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.ADD) { this.value + right }

/**
 * Minus operators
 */
@JvmName("minusIntFaktumInt")
operator fun Int.minus(right: Expression<Int>): Expression<Int> =
    numberUttrykk(this, right, MathOperator.SUB) { this - right.value }

@JvmName("minusIntFaktumDouble")
operator fun Int.minus(right: Expression<Double>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.SUB) { this - right.value }

@JvmName("minusDoubleFaktumInt")
operator fun Double.minus(right: Expression<Int>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.SUB) { this - right.value }

@JvmName("minusDoubleFaktumDouble")
operator fun Double.minus(right: Expression<Double>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.SUB) { this - right.value }

@JvmName("minusFaktumInt")
operator fun Expression<Int>.minus(right: Int): Expression<Int> =
    uttrykkNumber(this, right, MathOperator.SUB) { this.value - right }

@JvmName("minusFaktumDouble")
operator fun Expression<Int>.minus(right: Double): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.SUB) { this.value - right }

@JvmName("minusFaktumIntInt")
operator fun Expression<Int>.minus(right: Expression<Int>): Expression<Int> =
    binaryMathOp(this, right, MathOperator.SUB) { this.value - right.value }

@JvmName("minusFaktumIntDouble")
operator fun Expression<Int>.minus(right: Expression<Double>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.SUB) { this.value - right.value }

@JvmName("minusFaktumDoubleInt")
operator fun Expression<Double>.minus(right: Expression<Int>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.SUB) { this.value - right.value }

@JvmName("minusFaktumDoubleDouble")
operator fun Expression<Double>.minus(right: Expression<Double>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.SUB) { this.value - right.value }

@JvmName("minusFaktumDoubleInt2")
operator fun Expression<Double>.minus(right: Int): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.SUB) { this.value - right }

@JvmName("minusFaktumDoubleDouble2")
operator fun Expression<Double>.minus(right: Double): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.SUB) { this.value - right }

/**
 * Times operators
 */
@JvmName("timesIntFaktumInt")
operator fun Int.times(right: Expression<Int>): Expression<Int> =
    numberUttrykk(this, right, MathOperator.MUL) { this * right.value }

@JvmName("timesIntFaktumDouble")
operator fun Int.times(right: Expression<Double>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.MUL) { this * right.value }

@JvmName("timesDoubleFaktumInt")
operator fun Double.times(right: Expression<Int>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.MUL) { this * right.value }

@JvmName("timesDoubleFaktumDouble")
operator fun Double.times(right: Expression<Double>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.MUL) { this * right.value }

@JvmName("timesFaktumInt")
operator fun Expression<Int>.times(right: Int): Expression<Int> =
    uttrykkNumber(this, right, MathOperator.MUL) { this.value * right }

@JvmName("timesFaktumDouble")
operator fun Expression<Int>.times(right: Double): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.MUL) { this.value * right }

@JvmName("timesFaktumIntInt")
operator fun Expression<Int>.times(right: Expression<Int>): Expression<Int> =
    binaryMathOp(this, right, MathOperator.MUL) { this.value * right.value }

@JvmName("timesFaktumIntDouble")
operator fun Expression<Int>.times(right: Expression<Double>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.MUL) { this.value * right.value }

@JvmName("timesFaktumDoubleInt")
operator fun Expression<Double>.times(right: Expression<Int>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.MUL) { this.value * right.value }

@JvmName("timesFaktumDoubleDouble")
operator fun Expression<Double>.times(right: Expression<Double>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.MUL) { this.value * right.value }

@JvmName("timesFaktumDoubleInt2")
operator fun Expression<Double>.times(right: Int): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.MUL) { this.value * right }

@JvmName("timesFaktumDoubleDouble2")
operator fun Expression<Double>.times(right: Double): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.MUL) { this.value * right }

/**
 * Division operators
 * Note: Division always returns Double to preserve precision.
 * Division-by-zero is checked centrally in BinaryOperation to prevent silent NaN/Infinity.
 */
@JvmName("divIntFaktumInt")
operator fun Int.div(right: Expression<Int>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.DIV) { this / right.value.toDouble() }

@JvmName("divIntFaktumDouble")
operator fun Int.div(right: Expression<Double>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.DIV) { this / right.value }

@JvmName("divDoubleFaktumInt")
operator fun Double.div(right: Expression<Int>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.DIV) { this / right.value.toDouble() }

@JvmName("divDoubleFaktumDouble")
operator fun Double.div(right: Expression<Double>): Expression<Double> =
    numberUttrykk(this, right, MathOperator.DIV) { this / right.value }

@JvmName("divFaktumInt")
operator fun Expression<Int>.div(right: Int): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.DIV) { this.value / right.toDouble() }

@JvmName("divFaktumDouble")
operator fun Expression<Int>.div(right: Double): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.DIV) { this.value / right }

@JvmName("divFaktumIntInt")
operator fun Expression<Int>.div(right: Expression<Int>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.DIV) { this.value / right.value.toDouble() }

@JvmName("divFaktumIntDouble")
operator fun Expression<Int>.div(right: Expression<Double>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.DIV) { this.value / right.value }

@JvmName("divFaktumDoubleInt")
operator fun Expression<Double>.div(right: Expression<Int>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.DIV) { this.value / right.value.toDouble() }

@JvmName("divFaktumDoubleDouble")
operator fun Expression<Double>.div(right: Expression<Double>): Expression<Double> =
    binaryMathOp(this, right, MathOperator.DIV) { this.value / right.value }

@JvmName("divFaktumDoubleInt2")
operator fun Expression<Double>.div(right: Int): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.DIV) { this.value / right.toDouble() }

@JvmName("divFaktumDoubleDouble2")
operator fun Expression<Double>.div(right: Double): Expression<Double> =
    uttrykkNumber(this, right, MathOperator.DIV) { this.value / right }
