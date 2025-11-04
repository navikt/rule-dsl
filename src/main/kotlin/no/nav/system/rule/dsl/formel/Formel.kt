package no.nav.system.rule.dsl.formel

import no.nav.system.rule.dsl.formel.OperatorEnum.*
import no.nav.system.rule.dsl.rettsregel.Verdi
import redempt.crunch.Crunch.compileExpression
import java.io.Serializable

/**
 * Represents a mathematical formula with traceable computation and human-readable notation.
 *
 * Formel provides:
 * - **Immutable mathematical expressions** that preserve both human-readable notation and computational content
 * - **Type safety** for Int and Double operations
 * - **Variable tracking** to prevent naming conflicts
 * - **Locking mechanism** for performance and encapsulation
 *
 * ## Locking Behavior
 *
 * Formulas can be either **locked** or **unlocked**:
 *
 * ### Unlocked Formulas (default for simple operations)
 * - Variables are expanded into parent formulas
 * - Full mathematical expression is visible in notation
 * - More detailed tracing but potentially verbose
 *
 * ### Locked Formulas (default for builder-created formulas)
 * - Treated as atomic units in parent formulas
 * - Referenced by name only, not expanded
 * - Stored as sub-formulas for hierarchical structure
 * - Better performance and cleaner notation for complex expressions
 *
 * ## Examples
 * ```kotlin
 * // Simple variables (unlocked)
 * val grunnbeløp = Formel.variable("G", 118620)
 * val sats = Formel.variable("sats", 0.45)
 *
 * // Simple expression (unlocked)
 * val beregning = grunnbeløp * sats  // Shows: "G * sats" in notation
 *
 * // Complex locked formula
 * val kompleksBeløp = FormelBuilder.create<Int>()
 *     .name("bruttoAlderspensjon")
 *     .prefix("AP")
 *     .expression(someVeryComplexCalculation)
 *     .locked()  // This is the default
 *     .build()
 *
 * // When used in another formula:
 * val total = kompleksBeløp + tillegg  // Shows: "AP_bruttoAlderspensjon + tillegg"
 *                                        // (not the full complex calculation)
 * ```
 *
 * @param T The numeric type (Int or Double)
 */
open class Formel<T : Number> internal constructor(
    val emne: String,
    val prefix: String,
    val postfix: String,
    val notasjon: String,
    val innhold: String,
    val subFormelList: Set<Formel<out Number>>,
    val namedVarMap: Map<String, Number>,
    /**
     * When true, this formula is treated as an atomic unit in parent formulas.
     * See class documentation for detailed explanation.
     */
    val locked: Boolean,
    internal val shouldBeDouble: Boolean
) : Verdi<T>, Serializable {
    /**
     * Copy constructor for creating modified versions
     */
    private constructor(formel: Formel<T>) : this(
        emne = formel.emne,
        prefix = formel.prefix,
        postfix = formel.postfix,
        notasjon = formel.notasjon,
        innhold = formel.innhold,
        subFormelList = formel.subFormelList.map { Formel(it) }.toSet(),
        namedVarMap = formel.namedVarMap.toMap(),
        locked = formel.locked,
        shouldBeDouble = formel.shouldBeDouble
    )

    /**
     * Named Variable constructor
     * @deprecated Use Formel.variable(name, value) instead
     */
    @Deprecated("Use Formel.variable(name, value) instead", ReplaceWith("Formel.variable(emne, num)"))
    constructor(emne: String, num: T) : this(
        emne = emne,
        prefix = "",
        postfix = "",
        notasjon = emne,
        innhold = num.toString(),
        subFormelList = emptySet(),
        namedVarMap = mapOf(emne to num),
        locked = false,
        shouldBeDouble = num is Double
    )

    /**
     * Anonymous Variable constructor  
     * @deprecated Use Formel.constant(value) instead
     */
    @Deprecated("Use Formel.constant(value) instead", ReplaceWith("Formel.constant(num)"))
    constructor(num: T) : this(
        emne = num.toString(),
        prefix = "",
        postfix = "",
        notasjon = num.toString(),
        innhold = num.toString(),
        subFormelList = emptySet(),
        namedVarMap = emptyMap(),
        locked = false,
        shouldBeDouble = num is Double
    )

    override val name: String
        get() = listOf(prefix, emne, postfix)
            .filter { it.isNotEmpty() }
            .joinToString(separator = "_")
            .ifBlank { "anonymous#${this.hashCode()}" }

    override val value: T get() = lazyResultat

    @Suppress("UNCHECKED_CAST")
    private val lazyResultat: T by lazy {
        try {
            // Validate the expression before evaluation
            validateExpression()
            
            val result = compileExpression(innhold, evalEnv).evaluate()
            
            // Check for mathematical errors
            if (result.isNaN() || result.isInfinite()) {
                throw ArithmeticException("Mathematical operation resulted in $result for expression: $innhold")
            }
            
            if (shouldBeDouble) {
                result as T
            } else {
                // Convert to Int if this formula should be an integer
                result.toInt() as T
            }
        } catch (e: Exception) {
            throw ArithmeticException("Error evaluating formula '$name': ${e.message}").initCause(e)
        }
    }
    
    /**
     * Validates the mathematical expression for common errors
     */
    private fun validateExpression() {
        // Check for division by zero patterns
        if (innhold.contains("/ 0") || innhold.contains("/0")) {
            throw ArithmeticException("Division by zero detected in formula '$name': $innhold")
        }
        
        // Check for obvious invalid expressions
        if (innhold.isBlank()) {
            throw IllegalStateException("Formula '$name' has empty content")
        }
    }


    internal fun <K : Number> expand(operator: OperatorEnum, right: Formel<out Number>): Formel<K> {
        val left = this
        val (leftWithParanthesis, rightWithParanthesis) = addParanthesisIfNeeded(left, operator, right)

        // Determine if result should be double:
        // 1. If either operand is already a double
        // 2. If operation is division (always produces decimal results)
        val resultShouldBeDouble = left.shouldBeDouble || right.shouldBeDouble || operator == DIVIDE

        // Build subformula list
        val newSubFormulas = buildSet {
            if (left.locked) {
                add(left)
            } else {
                addAll(left.subFormelList)
            }
            if (right.locked) {
                add(right)
            } else {
                addAll(right.subFormelList)
            }
        }

        // Build and validate variable map in one step
        val newVarMap = mergeAndValidateVarMaps(left, right)

        // Create the result with proper type safety
        val result = createTypedFormel<K>(
            emne = "anonymous#${System.identityHashCode(left)}${System.identityHashCode(right)}",
            prefix = left.prefix,
            postfix = left.postfix,
            notasjon = "${leftWithParanthesis.finalNotasjon()}${operator.syntax}${rightWithParanthesis.finalNotasjon()}",
            innhold = "${leftWithParanthesis.finalInnhold()}${operator.syntax}${rightWithParanthesis.finalInnhold()}",
            subFormelList = newSubFormulas,
            namedVarMap = newVarMap,
            locked = false,
            shouldBeDouble = resultShouldBeDouble
        )
        
        return result
    }

    /**
     * Merges variable maps from two formulas while validating for conflicts.
     * Only unlocked formulas contribute variables and can create conflicts.
     * Locked formulas are treated as atomic units and don't participate in variable merging.
     */
    internal fun mergeAndValidateVarMaps(
        left: Formel<*>,
        right: Formel<*>
    ): Map<String, Number> {
        // Check for formula name conflicts first
        if (left.name == right.name && left.value != right.value) {
            throw IllegalArgumentException("Formula conflict: '${left.name}' with value ${left.value} would be reassigned to value ${right.value}")
        }
        
        // Build merged variable map with conflict detection
        return buildMap {
            // Add variables from left formula if it's unlocked
            if (!left.locked) {
                putAll(left.namedVarMap)
            }
            
            // Add variables from right formula if it's unlocked, checking for conflicts
            if (!right.locked) {
                right.namedVarMap.forEach { (rightName, rightValue) ->
                    val existingValue = this[rightName]
                    if (existingValue != null && existingValue != rightValue) {
                        throw IllegalArgumentException("Variable conflict: '$rightName' with value $existingValue would be reassigned to value $rightValue")
                    }
                    put(rightName, rightValue)
                }
            }
        }
    }

    /**
     * Validates that variables from two different formulas are not in conflict
     * @deprecated Use mergeAndValidateVarMaps instead
     */
    @Deprecated("Use mergeAndValidateVarMaps instead", ReplaceWith("mergeAndValidateVarMaps(left, right)"))
    private fun validateNoConflicts(
        left: Formel<*>,
        right: Formel<*>
    ) {
        // Check for formula name conflicts
        if (left.name == right.name && left.value != right.value) {
            throw IllegalArgumentException("Formula conflict: '${left.name}' with value ${left.value} would be reassigned to value ${right.value}")
        }
        
        // Check for variable conflicts if both formulas are unlocked
        if (!left.locked && !right.locked) {
            left.namedVarMap.forEach { (leftName, leftValue) ->
                if (right.namedVarMap.containsKey(leftName) && right.namedVarMap[leftName] != leftValue) {
                    throw IllegalArgumentException("Variable conflict: '$leftName' with value $leftValue would be reassigned to value ${right.namedVarMap[leftName]}")
                }
            }
        }
    }

    fun toDouble(): Formel<Double> {
        return Formel(
            emne = this.emne,
            prefix = this.prefix,
            postfix = this.postfix,
            notasjon = if (this.notasjon == "0") "0.0" else this.notasjon,
            innhold = if (this.innhold == "0") "0.0" else this.innhold,
            subFormelList = this.subFormelList,
            namedVarMap = this.namedVarMap,
            locked = this.locked,
            shouldBeDouble = true  // Explicitly set to double
        )
    }

    /**
     * Creates a copy of this formula with modified properties
     */
    internal fun copy(
        emne: String = this.emne,
        prefix: String = this.prefix,
        postfix: String = this.postfix,
        notasjon: String = this.notasjon,
        innhold: String = this.innhold,
        subFormelList: Set<Formel<out Number>> = this.subFormelList,
        namedVarMap: Map<String, Number> = this.namedVarMap,
        locked: Boolean = this.locked,
        shouldBeDouble: Boolean = this.shouldBeDouble
    ): Formel<T> {
        return Formel<T>(
            emne = emne,
            prefix = prefix,
            postfix = postfix,
            notasjon = notasjon,
            innhold = innhold,
            subFormelList = subFormelList,
            namedVarMap = namedVarMap,
            locked = locked,
            shouldBeDouble = shouldBeDouble
        )
    }
    
    companion object {
        /**
         * Creates a named variable formula.
         * Example: Formel.variable("grunnbeløp", 118620)
         */
        fun <T : Number> variable(name: String, value: T): Formel<T> {
            return Formel(
                emne = name,
                prefix = "",
                postfix = "",
                notasjon = name,
                innhold = value.toString(),
                subFormelList = emptySet(),
                namedVarMap = mapOf(name to value),
                locked = false,
                shouldBeDouble = value is Double
            )
        }
        
        /**
         * Creates a constant value formula.
         * Example: Formel.constant(42)
         */
        fun <T : Number> constant(value: T): Formel<T> {
            return Formel(
                emne = value.toString(),
                prefix = "",
                postfix = "",
                notasjon = value.toString(),
                innhold = value.toString(),
                subFormelList = emptySet(),
                namedVarMap = emptyMap(),
                locked = false,
                shouldBeDouble = value is Double
            )
        }
        
        /**
         * Type-safe factory method for creating Formel instances (internal use)
         */
        internal fun <K : Number> createTypedFormel(
            emne: String,
            prefix: String,
            postfix: String,
            notasjon: String,
            innhold: String,
            subFormelList: Set<Formel<out Number>>,
            namedVarMap: Map<String, Number>,
            locked: Boolean,
            shouldBeDouble: Boolean
        ): Formel<K> {
            return Formel<K>(
                emne = emne,
                prefix = prefix,
                postfix = postfix,
                notasjon = notasjon,
                innhold = innhold,
                subFormelList = subFormelList,
                namedVarMap = namedVarMap,
                locked = locked,
                shouldBeDouble = shouldBeDouble
            )
        }
    }

    private fun addParanthesisIfNeeded(
        left: Formel<*>,
        operator: OperatorEnum,
        right: Formel<out Number>
    ): Pair<Formel<*>, Formel<out Number>> {
        val leftWithParens = if (operator == TIMES || operator == DIVIDE) {
            addParanthesisIfNeeded(left)
        } else left
        
        val rightWithParens = if (operator == TIMES || operator == DIVIDE || operator == MINUS) {
            addParanthesisIfNeeded(right)
        } else right
        
        return leftWithParens to rightWithParens
    }

    private fun addParanthesisIfNeeded(expr: Formel<out Number>): Formel<out Number> {
        var level = 0
        val plus = PLUS.syntax.trim().first()
        val minus = MINUS.syntax.trim().first()
        var needsPara = false
        
        expr.notasjon.toCharArray().forEach {
            if (level == 0 && (it == plus || it == minus)) {
                needsPara = true
            } else if (it == '(') {
                level++
            } else if (it == ')') level--
        }
        
        return if (needsPara) {
            Formel(
                emne = expr.emne,
                prefix = expr.prefix,
                postfix = expr.postfix,
                notasjon = "(${expr.notasjon})",
                innhold = "(${expr.innhold})",
                subFormelList = expr.subFormelList,
                namedVarMap = expr.namedVarMap,
                locked = expr.locked,
                shouldBeDouble = expr.shouldBeDouble
            )
        } else expr
    }

    fun toBuilder(): FormelBuilder<T> {
        @Suppress("UNCHECKED_CAST")
        return FormelBuilder<T>().expression(this).name(this.emne)
    }
    
    /**
     * Creates a copy of this formula with a new emne (name)
     */
    fun emne(newEmne: String): Formel<T> = this.copy(emne = newEmne)

    /**
     * TODO Kanskje denne implementasjonen skal erstatte [notasjon] feltet. Det er forvirrende at det finnes to måter å hente ut notasjonen på.
     * Gjelder også [innhold]
     */
    private fun finalNotasjon(): String = if (locked) name else notasjon
    private fun finalInnhold(): String = if (locked) value.toString() else innhold

    override fun hva(): String {
        return "'$name' ($value)"
    }
    override fun toString(): String = toTreeString(0, Int.MAX_VALUE)


}


