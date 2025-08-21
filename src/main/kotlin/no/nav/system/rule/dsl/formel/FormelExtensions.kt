package no.nav.system.rule.dsl.formel

/**
 * Extension functions to make working with formulas more convenient
 */

/**
 * Creates a named copy of this formula
 */
fun <T : Number> Formel<T>.named(name: String): Formel<T> = copy(emne = name)

/**
 * Creates a locked copy of this formula  
 */
fun <T : Number> Formel<T>.locked(): Formel<T> = copy(locked = true)

/**
 * Creates an unlocked copy of this formula
 */
fun <T : Number> Formel<T>.unlocked(): Formel<T> = copy(locked = false)

/**
 * Creates a copy with the specified prefix
 */
fun <T : Number> Formel<T>.withPrefix(prefix: String): Formel<T> = copy(prefix = prefix)

/**
 * Creates a copy with the specified postfix
 */
fun <T : Number> Formel<T>.withPostfix(postfix: String): Formel<T> = copy(postfix = postfix)

/**
 * Operator overloading for += (returns new formula, doesn't mutate)
 */
operator fun <T : Number> Formel<T>.plusAssign(right: Number): Unit =
    throw UnsupportedOperationException("Formulas are immutable. Use 'formula = formula + value' instead of 'formula += value'")

/**
 * Operator overloading for -= (returns new formula, doesn't mutate)  
 */
operator fun <T : Number> Formel<T>.minusAssign(right: Number): Unit =
    throw UnsupportedOperationException("Formulas are immutable. Use 'formula = formula - value' instead of 'formula -= value'")

/**
 * Operator overloading for *= (returns new formula, doesn't mutate)
 */
operator fun <T : Number> Formel<T>.timesAssign(right: Number): Unit =
    throw UnsupportedOperationException("Formulas are immutable. Use 'formula = formula * value' instead of 'formula *= value'")

/**
 * Operator overloading for /= (returns new formula, doesn't mutate)
 */
operator fun <T : Number> Formel<T>.divAssign(right: Number): Unit =
    throw UnsupportedOperationException("Formulas are immutable. Use 'formula = formula / value' instead of 'formula /= value'")