package no.nav.system.rule.dsl.rettsregel.operators

import no.nav.system.rule.dsl.rettsregel.Add
import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Sub
import no.nav.system.rule.dsl.rettsregel.Mul
import no.nav.system.rule.dsl.rettsregel.Div
import no.nav.system.rule.dsl.rettsregel.Uttrykk


/**
 * Comparators
 */
operator fun Number.compareTo(i: Int): Int {
    return when (this) {
        is Int -> this.compareTo(i)
        is Double -> this.compareTo(i.toDouble())
        else -> {
            this.compareTo(i)
        }
    }
}

operator fun Int.compareTo(i: Number): Int = i.compareTo(this)

/**
 * Plus
 */
@JvmName("plusIntFaktumInt")
operator fun Int.plus(right: Uttrykk<Int>): Uttrykk<Int> = Add(Const(this), right)

@JvmName("plusIntFaktumDouble")
operator fun Int.plus(right: Uttrykk<Double>): Uttrykk<Double> = Add(Const(this), right)

@JvmName("plusDoubleFaktumInt")
operator fun Double.plus(right: Uttrykk<Int>): Uttrykk<Double> = Add(Const(this), right)

@JvmName("plusDoubleFaktumDouble")
operator fun Double.plus(right: Uttrykk<Double>): Uttrykk<Double> = Add(Const(this), right)

@JvmName("plusFaktumInt")
operator fun Uttrykk<Int>.plus(right: Int): Uttrykk<Int> = Add(this, Const(right))

@JvmName("plusFaktumDouble")
operator fun Uttrykk<Int>.plus(right: Double): Uttrykk<Double> = Add(this, Const(right))

@JvmName("plusFaktumIntInt")
operator fun Uttrykk<Int>.plus(right: Uttrykk<Int>): Uttrykk<Int> = Add(this, right)

@JvmName("plusFaktumIntDouble")
operator fun Uttrykk<Int>.plus(right: Uttrykk<Double>): Uttrykk<Double> = Add(this, right)

@JvmName("plusFaktumDoubleInt")
operator fun Uttrykk<Double>.plus(right: Uttrykk<Int>): Uttrykk<Double> = Add(this, right)

@JvmName("plusFaktumDoubleDouble")
operator fun Uttrykk<Double>.plus(right: Uttrykk<Double>): Uttrykk<Double> = Add(this, right)

@JvmName("plusFaktumDoubleInt2")
operator fun Uttrykk<Double>.plus(right: Int): Uttrykk<Double> = Add(this, Const(right))

@JvmName("plusFaktumDoubleDouble2")
operator fun Uttrykk<Double>.plus(right: Double): Uttrykk<Double> = Add(this, Const(right))

/**
 * Minus
 */
@JvmName("minusIntFaktumInt")
operator fun Int.minus(right: Uttrykk<Int>): Uttrykk<Int> = Sub(Const(this), right)

@JvmName("minusIntFaktumDouble")
operator fun Int.minus(right: Uttrykk<Double>): Uttrykk<Double> = Sub(Const(this), right)

@JvmName("minusDoubleFaktumInt")
operator fun Double.minus(right: Uttrykk<Int>): Uttrykk<Double> = Sub(Const(this), right)

@JvmName("minusDoubleFaktumDouble")
operator fun Double.minus(right: Uttrykk<Double>): Uttrykk<Double> = Sub(Const(this), right)

@JvmName("minusFaktumInt")
operator fun Uttrykk<Int>.minus(right: Int): Uttrykk<Int> = Sub(this, Const(right))

@JvmName("minusFaktumDouble")
operator fun Uttrykk<Int>.minus(right: Double): Uttrykk<Double> = Sub(this, Const(right))

@JvmName("minusFaktumIntInt")
operator fun Uttrykk<Int>.minus(right: Uttrykk<Int>): Uttrykk<Int> = Sub(this, right)

@JvmName("minusFaktumIntDouble")
operator fun Uttrykk<Int>.minus(right: Uttrykk<Double>): Uttrykk<Double> = Sub(this, right)

@JvmName("minusFaktumDoubleInt")
operator fun Uttrykk<Double>.minus(right: Uttrykk<Int>): Uttrykk<Double> = Sub(this, right)

@JvmName("minusFaktumDoubleDouble")
operator fun Uttrykk<Double>.minus(right: Uttrykk<Double>): Uttrykk<Double> = Sub(this, right)

@JvmName("minusFaktumDoubleInt2")
operator fun Uttrykk<Double>.minus(right: Int): Uttrykk<Double> = Sub(this, Const(right))

@JvmName("minusFaktumDoubleDouble2")
operator fun Uttrykk<Double>.minus(right: Double): Uttrykk<Double> = Sub(this, Const(right))

/**
 * Times
 */
@JvmName("timesIntFaktumInt")
operator fun Int.times(right: Uttrykk<Int>): Uttrykk<Int> = Mul(Const(this), right)

@JvmName("timesIntFaktumDouble")
operator fun Int.times(right: Uttrykk<Double>): Uttrykk<Double> = Mul(Const(this), right)

@JvmName("timesDoubleFaktumInt")
operator fun Double.times(right: Uttrykk<Int>): Uttrykk<Double> = Mul(Const(this), right)

@JvmName("timesDoubleFaktumDouble")
operator fun Double.times(right: Uttrykk<Double>): Uttrykk<Double> = Mul(Const(this), right)

@JvmName("timesFaktumInt")
operator fun Uttrykk<Int>.times(right: Int): Uttrykk<Int> = Mul(this, Const(right))

@JvmName("timesFaktumDouble")
operator fun Uttrykk<Int>.times(right: Double): Uttrykk<Double> = Mul(this, Const(right))

@JvmName("timesFaktumIntInt")
operator fun Uttrykk<Int>.times(right: Uttrykk<Int>): Uttrykk<Int> = Mul(this, right)

@JvmName("timesFaktumIntDouble")
operator fun Uttrykk<Int>.times(right: Uttrykk<Double>): Uttrykk<Double> = Mul(this, right)

@JvmName("timesFaktumDoubleInt")
operator fun Uttrykk<Double>.times(right: Uttrykk<Int>): Uttrykk<Double> = Mul(this, right)

@JvmName("timesFaktumDoubleDouble")
operator fun Uttrykk<Double>.times(right: Uttrykk<Double>): Uttrykk<Double> = Mul(this, right)

@JvmName("timesFaktumDoubleInt2")
operator fun Uttrykk<Double>.times(right: Int): Uttrykk<Double> = Mul(this, Const(right))

@JvmName("timesFaktumDoubleDouble2")
operator fun Uttrykk<Double>.times(right: Double): Uttrykk<Double> = Mul(this, Const(right))

/**
 * Division
 */
@JvmName("divIntFaktumInt")
operator fun Int.div(right: Uttrykk<Int>): Uttrykk<Double> = Div(Const(this), right)

@JvmName("divIntFaktumDouble")
operator fun Int.div(right: Uttrykk<Double>): Uttrykk<Double> = Div(Const(this), right)

@JvmName("divDoubleFaktumInt")
operator fun Double.div(right: Uttrykk<Int>): Uttrykk<Double> = Div(Const(this), right)

@JvmName("divDoubleFaktumDouble")
operator fun Double.div(right: Uttrykk<Double>): Uttrykk<Double> = Div(Const(this), right)

@JvmName("divFaktumInt")
operator fun Uttrykk<Int>.div(right: Int): Uttrykk<Double> = Div(this, Const(right))

@JvmName("divFaktumDouble")
operator fun Uttrykk<Int>.div(right: Double): Uttrykk<Double> = Div(this, Const(right))

@JvmName("divFaktumIntInt")
operator fun Uttrykk<Int>.div(right: Uttrykk<Int>): Uttrykk<Double> = Div(this, right)

@JvmName("divFaktumIntDouble")
operator fun Uttrykk<Int>.div(right: Uttrykk<Double>): Uttrykk<Double> = Div(this, right)

@JvmName("divFaktumDoubleInt")
operator fun Uttrykk<Double>.div(right: Uttrykk<Int>): Uttrykk<Double> = Div(this, right)

@JvmName("divFaktumDoubleDouble")
operator fun Uttrykk<Double>.div(right: Uttrykk<Double>): Uttrykk<Double> = Div(this, right)

@JvmName("divFaktumDoubleInt2")
operator fun Uttrykk<Double>.div(right: Int): Uttrykk<Double> = Div(this, Const(right))

@JvmName("divFaktumDoubleDouble2")
operator fun Uttrykk<Double>.div(right: Double): Uttrykk<Double> = Div(this, Const(right))