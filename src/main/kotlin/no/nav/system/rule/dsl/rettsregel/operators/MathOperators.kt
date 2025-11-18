package no.nav.system.rule.dsl.rettsregel.operators

import no.nav.system.rule.dsl.enums.MathOperator
import no.nav.system.rule.dsl.rettsregel.MathOperation
import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Uttrykk

/**
 * Plus
 */
@JvmName("plusIntFaktumInt")
operator fun Int.plus(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(Const(this), right, MathOperator.ADD) {
    this + right.verdi
}

@JvmName("plusIntFaktumDouble")
operator fun Int.plus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.ADD) {
    this + right.verdi
}

@JvmName("plusDoubleFaktumInt")
operator fun Double.plus(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.ADD) {
    this + right.verdi
}

@JvmName("plusDoubleFaktumDouble")
operator fun Double.plus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.ADD) {
    this + right.verdi
}

@JvmName("plusFaktumInt")
operator fun Uttrykk<Int>.plus(right: Int): Uttrykk<Int> = MathOperation(this, Const(right), MathOperator.ADD) {
    this.verdi + right
}

@JvmName("plusFaktumDouble")
operator fun Uttrykk<Int>.plus(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.ADD) {
    this.verdi + right
}

@JvmName("plusFaktumIntInt")
operator fun Uttrykk<Int>.plus(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(this, right, MathOperator.ADD) {
    this.verdi + right.verdi
}

@JvmName("plusFaktumIntDouble")
operator fun Uttrykk<Int>.plus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.ADD) {
    this.verdi + right.verdi
}

@JvmName("plusFaktumDoubleInt")
operator fun Uttrykk<Double>.plus(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(this, right, MathOperator.ADD) {
    this.verdi + right.verdi
}

@JvmName("plusFaktumDoubleDouble")
operator fun Uttrykk<Double>.plus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.ADD) {
    this.verdi + right.verdi
}

@JvmName("plusFaktumDoubleInt2")
operator fun Uttrykk<Double>.plus(right: Int): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.ADD) {
    this.verdi + right
}

@JvmName("plusFaktumDoubleDouble2")
operator fun Uttrykk<Double>.plus(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.ADD) {
    this.verdi + right
}

/**
 * Minus
 */
@JvmName("minusIntFaktumInt")
operator fun Int.minus(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(Const(this), right, MathOperator.SUB) {
    this - right.verdi
}

@JvmName("minusIntFaktumDouble")
operator fun Int.minus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.SUB) {
    this - right.verdi
}

@JvmName("minusDoubleFaktumInt")
operator fun Double.minus(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.SUB) {
    this - right.verdi
}

@JvmName("minusDoubleFaktumDouble")
operator fun Double.minus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.SUB) {
    this - right.verdi
}

@JvmName("minusFaktumInt")
operator fun Uttrykk<Int>.minus(right: Int): Uttrykk<Int> = MathOperation(this, Const(right), MathOperator.SUB) {
    this.verdi - right
}

@JvmName("minusFaktumDouble")
operator fun Uttrykk<Int>.minus(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.SUB) {
    this.verdi - right
}

@JvmName("minusFaktumIntInt")
operator fun Uttrykk<Int>.minus(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(this, right, MathOperator.SUB) {
    this.verdi - right.verdi
}

@JvmName("minusFaktumIntDouble")
operator fun Uttrykk<Int>.minus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.SUB) {
    this.verdi - right.verdi
}

@JvmName("minusFaktumDoubleInt")
operator fun Uttrykk<Double>.minus(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(this, right, MathOperator.SUB) {
    this.verdi - right.verdi
}

@JvmName("minusFaktumDoubleDouble")
operator fun Uttrykk<Double>.minus(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.SUB) {
    this.verdi - right.verdi
}

@JvmName("minusFaktumDoubleInt2")
operator fun Uttrykk<Double>.minus(right: Int): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.SUB) {
    this.verdi - right
}

@JvmName("minusFaktumDoubleDouble2")
operator fun Uttrykk<Double>.minus(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.SUB) {
    this.verdi - right
}

/**
 * Times
 */
@JvmName("timesIntFaktumInt")
operator fun Int.times(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(Const(this), right, MathOperator.MUL) {
    this * right.verdi
}

@JvmName("timesIntFaktumDouble")
operator fun Int.times(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.MUL) {
    this * right.verdi
}

@JvmName("timesDoubleFaktumInt")
operator fun Double.times(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.MUL) {
    this * right.verdi
}

@JvmName("timesDoubleFaktumDouble")
operator fun Double.times(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.MUL) {
    this * right.verdi
}

@JvmName("timesFaktumInt")
operator fun Uttrykk<Int>.times(right: Int): Uttrykk<Int> = MathOperation(this, Const(right), MathOperator.MUL) {
    this.verdi * right
}

@JvmName("timesFaktumDouble")
operator fun Uttrykk<Int>.times(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.MUL) {
    this.verdi * right
}

@JvmName("timesFaktumIntInt")
operator fun Uttrykk<Int>.times(right: Uttrykk<Int>): Uttrykk<Int> = MathOperation(this, right, MathOperator.MUL) {
    this.verdi * right.verdi
}

@JvmName("timesFaktumIntDouble")
operator fun Uttrykk<Int>.times(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.MUL) {
    this.verdi * right.verdi
}

@JvmName("timesFaktumDoubleInt")
operator fun Uttrykk<Double>.times(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(this, right, MathOperator.MUL) {
    this.verdi * right.verdi
}

@JvmName("timesFaktumDoubleDouble")
operator fun Uttrykk<Double>.times(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.MUL) {
    this.verdi * right.verdi
}

@JvmName("timesFaktumDoubleInt2")
operator fun Uttrykk<Double>.times(right: Int): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.MUL) {
    this.verdi * right
}

@JvmName("timesFaktumDoubleDouble2")
operator fun Uttrykk<Double>.times(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.MUL) {
    this.verdi * right
}

/**
 * Division
 */
@JvmName("divIntFaktumInt")
operator fun Int.div(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.DIV) {
    val h = right.verdi.toDouble()
    if (h == 0.0) throw ArithmeticException("Divisjon med null: $this / $h")
    this / h
}

@JvmName("divIntFaktumDouble")
operator fun Int.div(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.DIV) {
    val h = right.verdi
    if (h == 0.0) throw ArithmeticException("Divisjon med null: $this / $h")
    this / h
}

@JvmName("divDoubleFaktumInt")
operator fun Double.div(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.DIV) {
    val h = right.verdi.toDouble()
    if (h == 0.0) throw ArithmeticException("Divisjon med null: $this / $h")
    this / h
}

@JvmName("divDoubleFaktumDouble")
operator fun Double.div(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(Const(this), right, MathOperator.DIV) {
    val h = right.verdi
    if (h == 0.0) throw ArithmeticException("Divisjon med null: $this / $h")
    this / h
}

@JvmName("divFaktumInt")
operator fun Uttrykk<Int>.div(right: Int): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.DIV) {
    val h = right.toDouble()
    if (h == 0.0) throw ArithmeticException("Divisjon med null: ${this.verdi} / $h")
    this.verdi / h
}

@JvmName("divFaktumDouble")
operator fun Uttrykk<Int>.div(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.DIV) {
    if (right == 0.0) throw ArithmeticException("Divisjon med null: ${this.verdi} / $right")
    this.verdi / right
}

@JvmName("divFaktumIntInt")
operator fun Uttrykk<Int>.div(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(this, right, MathOperator.DIV) {
    val h = right.verdi.toDouble()
    if (h == 0.0) throw ArithmeticException("Divisjon med null: ${this.verdi} / $h")
    this.verdi / h
}

@JvmName("divFaktumIntDouble")
operator fun Uttrykk<Int>.div(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.DIV) {
    val h = right.verdi
    if (h == 0.0) throw ArithmeticException("Divisjon med null: ${this.verdi} / $h")
    this.verdi / h
}

@JvmName("divFaktumDoubleInt")
operator fun Uttrykk<Double>.div(right: Uttrykk<Int>): Uttrykk<Double> = MathOperation(this, right, MathOperator.DIV) {
    val h = right.verdi.toDouble()
    if (h == 0.0) throw ArithmeticException("Divisjon med null: ${this.verdi} / $h")
    this.verdi / h
}

@JvmName("divFaktumDoubleDouble")
operator fun Uttrykk<Double>.div(right: Uttrykk<Double>): Uttrykk<Double> = MathOperation(this, right, MathOperator.DIV) {
    val h = right.verdi
    if (h == 0.0) throw ArithmeticException("Divisjon med null: ${this.verdi} / $h")
    this.verdi / h
}

@JvmName("divFaktumDoubleInt2")
operator fun Uttrykk<Double>.div(right: Int): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.DIV) {
    val h = right.toDouble()
    if (h == 0.0) throw ArithmeticException("Divisjon med null: ${this.verdi} / $h")
    this.verdi / h
}

@JvmName("divFaktumDoubleDouble2")
operator fun Uttrykk<Double>.div(right: Double): Uttrykk<Double> = MathOperation(this, Const(right), MathOperator.DIV) {
    if (right == 0.0) throw ArithmeticException("Divisjon med null: ${this.verdi} / $right")
    this.verdi / right
}
