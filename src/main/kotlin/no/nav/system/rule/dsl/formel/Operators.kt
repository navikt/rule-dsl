package no.nav.system.rule.dsl.formel

import no.nav.system.rule.dsl.formel.OperatorEnum.*


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
 * Operators with syntax
 */
enum class OperatorEnum(val syntax: String) {
    PLUS(" + "),
    MINUS(" - "),
    TIMES(" * "),
    DIVIDE(" / "),
    MODULUS(" % ")
}

/**
 * Plus
 */
@JvmName("plusIntFormelInt")
operator fun Int.plus(right: Formel<Int>): Formel<Int> = Formel.constant(this).expand(PLUS, right)

@JvmName("plusIntFormelDouble")
operator fun Int.plus(right: Formel<Double>): Formel<Double> = Formel.constant(this).expand(PLUS, right)

@JvmName("plusDoubleFormelInt")
operator fun Double.plus(right: Formel<Int>): Formel<Double> = Formel.constant(this).expand(PLUS, right)

@JvmName("plusDoubleFormelDouble")
operator fun Double.plus(right: Formel<Double>): Formel<Double> = Formel.constant(this).expand(PLUS, right)

@JvmName("plusFormelInt")
operator fun Formel<Int>.plus(right: Int): Formel<Int> = this.expand(PLUS, Formel.constant(right))

@JvmName("plusFormelDouble")
operator fun Formel<Int>.plus(right: Double): Formel<Double> = this.expand(PLUS, Formel.constant(right))

@JvmName("plusFormelInt")
operator fun Formel<Int>.plus(right: Formel<Int>): Formel<Int> = this.expand(PLUS, right)

@JvmName("plusFormelDouble")
operator fun Formel<Int>.plus(right: Formel<Double>): Formel<Double> = this.expand(PLUS, right)

@JvmName("plusFormelDoubleInt")
operator fun Formel<Double>.plus(right: Formel<Int>): Formel<Double> = this.expand(PLUS, right)

@JvmName("plusFormelDoubleDouble")
operator fun Formel<Double>.plus(right: Formel<Double>): Formel<Double> = this.expand(PLUS, right)

@JvmName("plusFormelDoubleInt")
operator fun Formel<Double>.plus(right: Int): Formel<Double> = this.expand(PLUS, Formel.constant(right))

@JvmName("plusFormelDoubleDouble")
operator fun Formel<Double>.plus(right: Double): Formel<Double> = this.expand(PLUS, Formel.constant(right))

/**
 * Minus
 */
@JvmName("minusIntFormelInt")
operator fun Int.minus(right: Formel<Int>): Formel<Int> = Formel.constant(this).expand(MINUS, right)

@JvmName("minusIntFormelDouble")
operator fun Int.minus(right: Formel<Double>): Formel<Double> = Formel.constant(this).expand(MINUS, right)

@JvmName("minusDoubleFormelInt")
operator fun Double.minus(right: Formel<Int>): Formel<Double> = Formel.constant(this).expand(MINUS, right)

@JvmName("minusDoubleFormelDouble")
operator fun Double.minus(right: Formel<Double>): Formel<Double> = Formel.constant(this).expand(MINUS, right)

@JvmName("minusFormelInt")
operator fun Formel<Int>.minus(right: Int): Formel<Int> = this.expand(MINUS, Formel.constant(right))

@JvmName("minusFormelDouble")
operator fun Formel<Int>.minus(right: Double): Formel<Double> = this.expand(MINUS, Formel.constant(right))

@JvmName("minusFormelInt")
operator fun Formel<Int>.minus(right: Formel<Int>): Formel<Int> = this.expand(MINUS, right)

@JvmName("minusFormelDouble")
operator fun Formel<Int>.minus(right: Formel<Double>): Formel<Double> = this.expand(MINUS, right)

@JvmName("minusFormelDoubleInt")
operator fun Formel<Double>.minus(right: Formel<Int>): Formel<Double> = this.expand(MINUS, right)

@JvmName("minusFormelDoubleDouble")
operator fun Formel<Double>.minus(right: Formel<Double>): Formel<Double> = this.expand(MINUS, right)

@JvmName("minusFormelDoubleInt")
operator fun Formel<Double>.minus(right: Int): Formel<Double> = this.expand(MINUS, Formel.constant(right))

@JvmName("minusFormelDoubleDouble")
operator fun Formel<Double>.minus(right: Double): Formel<Double> = this.expand(MINUS, Formel.constant(right))

/**
 * Times
 */
@JvmName("timesIntFormelInt")
operator fun Int.times(right: Formel<Int>): Formel<Int> = Formel.constant(this).expand(TIMES, right)

@JvmName("timesIntFormelDouble")
operator fun Int.times(right: Formel<Double>): Formel<Double> = Formel.constant(this).expand(TIMES, right)

@JvmName("timesDoubleFormelInt")
operator fun Double.times(right: Formel<Int>): Formel<Double> = Formel.constant(this).expand(TIMES, right)

@JvmName("timesDoubleFormelDouble")
operator fun Double.times(right: Formel<Double>): Formel<Double> = Formel.constant(this).expand(TIMES, right)

@JvmName("timesFormelInt")
operator fun Formel<Int>.times(right: Int): Formel<Int> = this.expand(TIMES, Formel.constant(right))

@JvmName("timesFormelDouble")
operator fun Formel<Int>.times(right: Double): Formel<Double> = this.expand(TIMES, Formel.constant(right))

@JvmName("timesFormelInt")
operator fun Formel<Int>.times(right: Formel<Int>): Formel<Int> = this.expand(TIMES, right)

@JvmName("timesFormelDouble")
operator fun Formel<Int>.times(right: Formel<Double>): Formel<Double> = this.expand(TIMES, right)

@JvmName("timesFormelDoubleInt")
operator fun Formel<Double>.times(right: Formel<Int>): Formel<Double> = this.expand(TIMES, right)

@JvmName("timesFormelDoubleDouble")
operator fun Formel<Double>.times(right: Formel<Double>): Formel<Double> = this.expand(TIMES, right)

@JvmName("timesFormelDoubleInt")
operator fun Formel<Double>.times(right: Int): Formel<Double> = this.expand(TIMES, Formel.constant(right))

@JvmName("timesFormelDoubleDouble")
operator fun Formel<Double>.times(right: Double): Formel<Double> = this.expand(TIMES, Formel.constant(right))

/**
 * Division
 */
@JvmName("divIntFormelInt")
operator fun Int.div(right: Formel<Int>): Formel<Double> = Formel.constant(this).expand(DIVIDE, right)

@JvmName("divIntFormelDouble")
operator fun Int.div(right: Formel<Double>): Formel<Double> = Formel.constant(this).expand(DIVIDE, right)

@JvmName("divDoubleFormelInt")
operator fun Double.div(right: Formel<Int>): Formel<Double> = Formel.constant(this).expand(DIVIDE, right)

@JvmName("divDoubleFormelDouble")
operator fun Double.div(right: Formel<Double>): Formel<Double> = Formel.constant(this).expand(DIVIDE, right)

@JvmName("divFormelInt")
operator fun Formel<Int>.div(right: Int): Formel<Double> = this.expand(DIVIDE, Formel.constant(right))

@JvmName("divFormelDouble")
operator fun Formel<Int>.div(right: Double): Formel<Double> = this.expand(DIVIDE, Formel.constant(right))

@JvmName("divFormelInt")
operator fun Formel<Int>.div(right: Formel<Int>): Formel<Double> = this.expand(DIVIDE, right)

@JvmName("divFormelDouble")
operator fun Formel<Int>.div(right: Formel<Double>): Formel<Double> = this.expand(DIVIDE, right)

@JvmName("divFormelDoubleInt")
operator fun Formel<Double>.div(right: Formel<Int>): Formel<Double> = this.expand(DIVIDE, right)

@JvmName("divFormelDoubleDouble")
operator fun Formel<Double>.div(right: Formel<Double>): Formel<Double> = this.expand(DIVIDE, right)

@JvmName("divFormelDoubleInt")
operator fun Formel<Double>.div(right: Int): Formel<Double> = this.expand(DIVIDE, Formel.constant(right))

@JvmName("divFormelDoubleDouble")
operator fun Formel<Double>.div(right: Double): Formel<Double> = this.expand(DIVIDE, Formel.constant(right))
