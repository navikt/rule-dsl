package no.nav.system.rule.dsl.formel

/**
 * DSL convenience functions for creating formulas.
 * 
 * These functions provide a more concise syntax for common operations:
 * ```kotlin
 * // Instead of: Formel.variable("grunnbeløp", 118620)  
 * val grunnbeløp = variable("grunnbeløp", 118620)
 * 
 * // Instead of: Formel.constant(42)
 * val constant = constant(42)
 * 
 * // Instead of: FormelBuilder.create<Int>().name("total")...
 * val complex = formula<Int>("total") {
 *     prefix("TP")
 *     postfix("årlig") 
 *     expression(grunnbeløp * sats)
 *     locked()
 * }
 * ```
 */

/**
 * Creates a named variable formula
 */
fun <T : Number> variable(name: String, value: T): Formel<T> = Formel.variable(name, value)

/**
 * Creates a constant value formula  
 */
fun <T : Number> constant(value: T): Formel<T> = Formel.constant(value)

/**
 * DSL builder for complex formulas
 */
inline fun <reified T : Number> formula(name: String, builder: FormelBuilder<T>.() -> Unit): Formel<T> {
    return FormelBuilder.create<T>()
        .name(name)
        .apply(builder)
        .build()
}

/**
 * DSL builder for anonymous complex formulas
 */
inline fun <reified T : Number> formula(builder: FormelBuilder<T>.() -> Unit): Formel<T> {
    return FormelBuilder.create<T>()
        .apply(builder)  
        .build()
}