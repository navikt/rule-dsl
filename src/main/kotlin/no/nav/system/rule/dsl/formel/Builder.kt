package no.nav.system.rule.dsl.formel

/**
 * @deprecated Use FormelBuilder instead
 */
@Deprecated("Use FormelBuilder instead", ReplaceWith("FormelBuilder<T>"))
typealias Builder<T> = FormelBuilder<T>

/**
 * Builder for creating complex formulas with metadata like name, prefix, and postfix.
 * 
 * Examples:
 * ```
 * val basicFormula = FormelBuilder.create<Int>()
 *     .name("total")
 *     .expression(grunnbeløp + tillegg)
 *     .build()
 *     
 * val lockedFormula = FormelBuilder.create<Double>()
 *     .name("brutto_beløp")
 *     .prefix("TP")
 *     .postfix("årlig")
 *     .expression(someComplexCalculation)
 *     .locked()
 *     .build()
 * ```
 */
class FormelBuilder<T : Number> @PublishedApi internal constructor() {
    companion object {
        /**
         * Creates a new formula builder.
         * Only Int and Double types are supported.
         */
        inline fun <reified T : Number> create(): FormelBuilder<T> {
            if (T::class == Int::class || T::class == Double::class) {
                return FormelBuilder<T>()
            } else {
                throw IllegalArgumentException("Unsupported type ${T::class}. Only Int and Double are supported.")
            }
        }
        
        /**
         * @deprecated Use FormelBuilder.create<T>() instead
         */
        @Deprecated("Use FormelBuilder.create<T>() instead", ReplaceWith("FormelBuilder.create<T>()"))
        inline fun <reified T : Number> kmath(): FormelBuilder<T> = create<T>()
    }

    private var builderPrefix: String = ""
    private var builderName: String = ""
    private var builderPostfix: String = ""
    private var builderLocked: Boolean = true
    private var builderExpression: Formel<T>? = null

    /**
     * Sets the prefix for the formula name (e.g., "TP" for "TP_brutto_årlig")
     */
    fun prefix(prefix: String): FormelBuilder<T> = apply { this.builderPrefix = prefix }
    
    /**
     * Sets the main name for the formula
     */
    fun name(name: String): FormelBuilder<T> = apply { this.builderName = name }
    
    /**
     * Sets the postfix for the formula name (e.g., "årlig" for "TP_brutto_årlig")
     */
    fun postfix(postfix: String): FormelBuilder<T> = apply { this.builderPostfix = postfix }
    
    /**
     * Makes the formula unlocked (default is locked)
     */
    fun unlocked(): FormelBuilder<T> = apply { this.builderLocked = false }
    
    /**
     * Makes the formula locked (this is the default)
     */
    fun locked(): FormelBuilder<T> = apply { this.builderLocked = true }
    
    /**
     * Sets the mathematical expression for this formula
     */
    fun expression(expression: Formel<T>): FormelBuilder<T> = apply { this.builderExpression = expression }
    
    /**
     * @deprecated Use name() instead
     */
    @Deprecated("Use name() instead", ReplaceWith("name(emne)"))
    fun emne(emne: String): FormelBuilder<T> = apply { this.builderName = emne }
    
    /**
     * @deprecated Use unlocked() instead
     */
    @Deprecated("Use unlocked() instead", ReplaceWith("unlocked()"))
    fun unlock(): FormelBuilder<T> = unlocked()
    
    /**
     * @deprecated Use expression() instead
     */
    @Deprecated("Use expression() instead", ReplaceWith("expression(kFormel)"))
    fun formel(kFormel: Formel<T>): FormelBuilder<T> = expression(kFormel)

    /**
     * Builds the formula with the specified configuration
     */
    fun build(): Formel<T> {
        val cleanName = builderName.trim().replace(" ", "_")
        val expression = builderExpression ?: throw IllegalStateException("No expression specified. Use .expression() to set the mathematical expression.")
        validateState(cleanName, expression)
        return expression.copy(
            prefix = builderPrefix.trim(),
            emne = cleanName,
            postfix = builderPostfix.trim(),
            locked = builderLocked
        )
    }

    private fun validateState(name: String, expression: Formel<*>) {
        if (expression.namedVarMap.containsKey(name)) {
            throw IllegalArgumentException("Circular reference detected: Formula name '$name' cannot contain variables with the same name.")
        }
    }
}