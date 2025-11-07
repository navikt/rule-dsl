package no.nav.system.rule.dsl.rettsregel.operators

import no.nav.system.rule.dsl.enums.MathOperator
import no.nav.system.rule.dsl.rettsregel.MathOperation
import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Uttrykk

/**
 * Plus
 */
@JvmName("plusIntFaktumInt")
operator fun Int.plus(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(Const(this), right, MathOperator.ADD)

@JvmName("plusIntFaktumDouble")
operator fun Int.plus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.ADD)

@JvmName("plusDoubleFaktumInt")
operator fun Double.plus(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.ADD)

@JvmName("plusDoubleFaktumDouble")
operator fun Double.plus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.ADD)

@JvmName("plusFaktumInt")
operator fun Uttrykk<Int>.plus(right: Int): Uttrykk<Int> = MathOperation(this, Const(right), MathOperator.ADD)

@JvmName("plusFaktumDouble")
operator fun Uttrykk<Int>.plus(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.ADD)

@JvmName("plusFaktumIntInt")
operator fun Uttrykk<Int>.plus(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(this, right, MathOperator.ADD)

@JvmName("plusFaktumIntDouble")
operator fun Uttrykk<Int>.plus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.ADD)

@JvmName("plusFaktumDoubleInt")
operator fun Uttrykk<Double>.plus(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(this, right, MathOperator.ADD)

@JvmName("plusFaktumDoubleDouble")
operator fun Uttrykk<Double>.plus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.ADD)

@JvmName("plusFaktumDoubleInt2")
operator fun Uttrykk<Double>.plus(right: Int): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.ADD)

@JvmName("plusFaktumDoubleDouble2")
operator fun Uttrykk<Double>.plus(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.ADD)

/**
 * Minus
 */
@JvmName("minusIntFaktumInt")
operator fun Int.minus(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(Const(this), right, MathOperator.SUB)

@JvmName("minusIntFaktumDouble")
operator fun Int.minus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.SUB)

@JvmName("minusDoubleFaktumInt")
operator fun Double.minus(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.SUB)

@JvmName("minusDoubleFaktumDouble")
operator fun Double.minus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.SUB)

@JvmName("minusFaktumInt")
operator fun Uttrykk<Int>.minus(right: Int): Uttrykk<Int> = MathOperation(this, Const(right), MathOperator.SUB)

@JvmName("minusFaktumDouble")
operator fun Uttrykk<Int>.minus(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.SUB)

@JvmName("minusFaktumIntInt")
operator fun Uttrykk<Int>.minus(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(this, right, MathOperator.SUB)

@JvmName("minusFaktumIntDouble")
operator fun Uttrykk<Int>.minus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.SUB)

@JvmName("minusFaktumDoubleInt")
operator fun Uttrykk<Double>.minus(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(this, right, MathOperator.SUB)

@JvmName("minusFaktumDoubleDouble")
operator fun Uttrykk<Double>.minus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.SUB)

@JvmName("minusFaktumDoubleInt2")
operator fun Uttrykk<Double>.minus(right: Int): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.SUB)

@JvmName("minusFaktumDoubleDouble2")
operator fun Uttrykk<Double>.minus(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.SUB)

/**
 * Times
 */
@JvmName("timesIntFaktumInt")
operator fun Int.times(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(Const(this), right, MathOperator.MUL)

@JvmName("timesIntFaktumDouble")
operator fun Int.times(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.MUL)

@JvmName("timesDoubleFaktumInt")
operator fun Double.times(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.MUL)

@JvmName("timesDoubleFaktumDouble")
operator fun Double.times(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.MUL)

@JvmName("timesFaktumInt")
operator fun Uttrykk<Int>.times(right: Int): Uttrykk<Int> = MathOperation(this, Const(right), MathOperator.MUL)

@JvmName("timesFaktumDouble")
operator fun Uttrykk<Int>.times(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.MUL)

@JvmName("timesFaktumIntInt")
operator fun Uttrykk<Int>.times(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(this, right, MathOperator.MUL)

@JvmName("timesFaktumIntDouble")
operator fun Uttrykk<Int>.times(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.MUL)

@JvmName("timesFaktumDoubleInt")
operator fun Uttrykk<Double>.times(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(this, right, MathOperator.MUL)

@JvmName("timesFaktumDoubleDouble")
operator fun Uttrykk<Double>.times(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.MUL)

@JvmName("timesFaktumDoubleInt2")
operator fun Uttrykk<Double>.times(right: Int): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.MUL)

@JvmName("timesFaktumDoubleDouble2")
operator fun Uttrykk<Double>.times(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.MUL)

/**
 * Division
 */
@JvmName("divIntFaktumInt")
operator fun Int.div(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.DIV)

@JvmName("divIntFaktumDouble")
operator fun Int.div(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.DIV)

@JvmName("divDoubleFaktumInt")
operator fun Double.div(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.DIV)

@JvmName("divDoubleFaktumDouble")
operator fun Double.div(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.DIV)

@JvmName("divFaktumInt")
operator fun Uttrykk<Int>.div(right: Int): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.DIV)

@JvmName("divFaktumDouble")
operator fun Uttrykk<Int>.div(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.DIV)

@JvmName("divFaktumIntInt")
operator fun Uttrykk<Int>.div(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(this, right, MathOperator.DIV)

@JvmName("divFaktumIntDouble")
operator fun Uttrykk<Int>.div(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.DIV)

@JvmName("divFaktumDoubleInt")
operator fun Uttrykk<Double>.div(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(this, right, MathOperator.DIV)

@JvmName("divFaktumDoubleDouble")
operator fun Uttrykk<Double>.div(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.DIV)

@JvmName("divFaktumDoubleInt2")
operator fun Uttrykk<Double>.div(right: Int): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.DIV)

@JvmName("divFaktumDoubleDouble2")
operator fun Uttrykk<Double>.div(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.DIV)