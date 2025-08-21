package no.nav.system.rule.dsl.formel

import no.nav.system.rule.dsl.formel.OperatorEnum.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import redempt.crunch.Crunch.compileExpression
import java.io.Serializable

class Formel<T : Number>(
    var emne: String,
    var prefix: String,
    var postfix: String,
    var notasjon: String,
    var innhold: String,
) : Faktum<T>(), Serializable {

    /**
     * Private Copy constructor
     */
    constructor(formel: Formel<T>) : this(
        emne = formel.emne,
        prefix = formel.prefix,
        postfix = formel.postfix,
        notasjon = formel.notasjon,
        innhold = formel.innhold
    ) {
        this.locked = formel.locked
        this.shouldBeDouble = formel.shouldBeDouble
        formel.subFormelList.forEach {
            this.subFormelList.add(Formel(it))
        }
        this.namedVarMap.putAll(formel.namedVarMap)
    }

    /**
     * Named Variable constructor
     */
    constructor(emne: String, num: T) : this(
        emne = emne,
        prefix = "",
        postfix = "",
        notasjon = emne,
        innhold = num.toString()
    ) {
        // Set shouldBeDouble based on the actual type of the number
        shouldBeDouble = num is Double
        namedVarMap[emne] = num
    }

    /**
     * Anonymous Variable constructor
     */
    constructor(num: T) : this(
        emne = num.toString(),
        prefix = "",
        postfix = "",
        notasjon = num.toString(),
        innhold = num.toString()
    ) {
        // Set shouldBeDouble based on the actual type of the number
        shouldBeDouble = num is Double
    }

    val subFormelList: LinkedHashSet<Formel<out Number>> = linkedSetOf()

    val namedVarMap: MutableMap<String, Number> = mutableMapOf()

    /**
     * Formulas that are locked behave differently when used in composition of a new formula.
     * The new formula will not expand the locked formulas content into its own content, instead
     * it will reference the locked formula by name only, and add it as a subformula.
     */
    var locked = false

    fun navn(): String {
        return listOf(prefix, emne, postfix)
            .filter { it.isNotEmpty() }
            .joinToString(separator = "_")
            .ifBlank { "anonymous#${this.hashCode()}" }
    }

    fun resultat(): T = lazyResultat

    @Suppress("UNCHECKED_CAST")
    private val lazyResultat: T by lazy {
        val result = compileExpression(innhold, evalEnv).evaluate()
        if (shouldBeDouble) {
            result as T
        } else {
            // Convert to Int if this formula should be an integer
            result.toInt() as T
        }
    }

    /**
     * The Math framework (redempt.crunch) always evaluates to a double.
     * For mostly display reasons, we keep track of whether the formula should be displayed as a double or int.
     * This is true when:
     * - Any operand is a Double type
     * - Division operation is used (always produces decimals)
     * - Functions that explicitly return Double
     */
    internal var shouldBeDouble: Boolean = false

    @Suppress("UNCHECKED_CAST")
    internal fun <K : Number> expand(operator: OperatorEnum, right: Formel<out Number>): Formel<K> {
        val left = this
        addParanthesisIfNeeded(left, operator, right)

        // Determine if result should be double:
        // 1. If either operand is already a double
        // 2. If operation is division (always produces decimal results)
        val resultShouldBeDouble = left.shouldBeDouble || right.shouldBeDouble || operator == DIVIDE

        val expandedFormel: Formel<out Number> = Formel<K>(
            emne = left.emne,
            prefix = left.prefix,
            postfix = left.postfix,
            notasjon = "${left.finalNotasjon()}${operator.syntax}${right.finalNotasjon()}",
            innhold = "${left.finalInnhold()}${operator.syntax}${right.finalInnhold()}"
        )
        expandedFormel.apply {
            shouldBeDouble = resultShouldBeDouble
            if (left.locked) {
                subFormelList.add(left)
            } else {
                subFormelList.addAll(left.subFormelList)
            }
            if (right.locked) {
                subFormelList.add(right)
            } else {
                subFormelList.addAll(right.subFormelList)
            }
            this.emne = "anonymous#${this.hashCode()}"
            this.verifyAndUpdateVarMap(left, right)
        }
        return expandedFormel as Formel<K>
    }

    /**
     * Sjekker at variabler fra to forskjellige formler ikke er i konflikt med hverandre, dvs har samme navn men forskjellige verdier.
     */
    private fun verifyAndUpdateVarMap(
        left: Formel<*>,
        right: Formel<*>,
    ) {
        if (left.navn() == right.navn() && left.resultat() != right.resultat()) {
            throw IllegalArgumentException("Formula conflict: '${left.navn()}' with value ${left.resultat()} would be reassigned to value ${right.resultat()}")
        }
        if (!left.locked && !right.locked) {
            left.namedVarMap.forEach { (leftName, leftValue) ->
                if (right.namedVarMap.containsKey(leftName) && right.namedVarMap[leftName] != leftValue) {
                    throw IllegalArgumentException("Variable conflict: '$leftName' with value $leftValue would be reassigned to value ${right.namedVarMap[leftName]}")
                }
            }
        }
        /**
         * If a formula ([left] or [right]) is locked, its variables will not be used in resulting formula, only its name will be present.
         */
        if (!left.locked) namedVarMap += left.namedVarMap
        if (!right.locked) namedVarMap += right.namedVarMap
    }

    fun toDouble(): Formel<Double> {
        val doubleFormel = Formel<Double>(
            emne = this.emne,
            prefix = this.prefix,
            postfix = this.postfix,
            notasjon = if (this.notasjon == "0") "0.0" else this.notasjon,
            innhold = if (this.innhold == "0") "0.0" else this.innhold
        )
        doubleFormel.shouldBeDouble = true  // Explicitly set to double
        return doubleFormel
    }

    private fun addParanthesisIfNeeded(
        left: Formel<T>,
        operator: OperatorEnum,
        right: Formel<out Number>,
    ) {
        if (operator == TIMES || operator == DIVIDE) {
            checkAndAddParanthesis(left)
            checkAndAddParanthesis(right)
        } else if (operator == MINUS) {
            checkAndAddParanthesis(right)
        }
    }

    private fun checkAndAddParanthesis(expr: Formel<out Number>) {
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
        if (needsPara) {
            expr.notasjon = "(${expr.notasjon})"
            expr.innhold = "(${expr.innhold})"
        }
    }

    fun toBuilder(): Builder<T> = Builder<T>().formel(Formel(this)).emne(this.emne)
    fun emne(newEmne: String): Formel<T> = this.apply { emne = newEmne }

    /**
     * TODO Kanskje denne implementasjonen skal erstatte [notasjon] feltet. Det er forvirrende at det finnes to måter å hente ut notasjonen på.
     * Gjelder også [innhold]
     */
    private fun finalNotasjon(): String = if (locked) navn() else notasjon
    private fun finalInnhold(): String = if (locked) resultat().toString() else innhold

    override fun toString(): String = toTreeString(0, Int.MAX_VALUE)


}


