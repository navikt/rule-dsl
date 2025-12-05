package no.nav.system.ruledsl.core.model

import no.nav.system.ruledsl.core.enums.RuleComponentType
import no.nav.system.ruledsl.core.error.InvalidRulesetException
import no.nav.system.ruledsl.core.inspections.debug
import no.nav.system.ruledsl.core.pattern.Pattern
import no.nav.system.ruledsl.core.rettsregel.Faktum
import no.nav.system.ruledsl.core.rettsregel.ListOperation
import no.nav.system.ruledsl.core.rettsregel.Uttrykk
import no.nav.system.ruledsl.core.operators.ListOperator
import org.jetbrains.annotations.TestOnly

/**
 * Abstract Ruleset manages creation, ordering and execution of rules specified in implementing classes.
 *
 * @param T the return type of the ruleset
 *
 */
abstract class AbstractRuleset<T : Any> : AbstractRuleComponent() {

    /**
     * Ruleset name
     */
    @PublishedApi
    internal var rulesetName: String = this.javaClass.simpleName

    /**
     * Map from sequence to a function that create Rule object.
     *
     * The map key sorts the functions into their evaluation order using their sequence number (map key).
     * Functions either create a single Rule object, or, if the function is created using the pattern, a rule object for each element in the pattern.
     */
    @PublishedApi
    internal val ruleFunctionMap = mutableMapOf<Int, () -> List<Rule<T>>>()

    /**
     * Value of first fired rule with returnValue.
     * TODO Vurder om denne kan igjen bli private. Behovet for sporing at regelsett, og deretter uthenting av returverdien, er kanskje ikke lenger nødvendig.
     * (Det var PersonenErFlyktningRS som benyttet dette en gang).
     */
    lateinit var returnValue: T

    /**
     * Creates a standard rule using the rule mini-DSL.
     *
     * @param navn the rule name
     * @param createRuleContent the Rule function that populates the Rule object.
     */
    inline fun regel(navn: String, crossinline createRuleContent: Rule<T>.() -> Unit) {
        val sequence = nextSequence()
        ruleFunctionMap[sequence] = {
            val rule = Rule<T>("$rulesetName.$navn", sequence)
            addChild(rule)
            rule.createRuleContent()
            listOf(rule)
        }
    }

    /**
     * Creates a pattern rule for each applicable element in the provided [pattern] using the rule mini-DSL.
     *
     * @param navn the rule name
     * @param pattern pattern used in this rule
     * @param createRuleContent the Rule function that populates the Rule object using the pattern element.
     */
    inline fun <P> regel(
        navn: String,
        pattern: Pattern<P>,
        crossinline createRuleContent: Rule<T>.(P) -> Unit,
    ) {
        val sequence = nextSequence() // starting sequence for all the rules that will be created using this pattern
        ruleFunctionMap[sequence] = {
            val rulesInPattern = mutableListOf<Rule<T>>()
            var offset = 1

            for (patternElement in pattern.get()) {
                val rule = Rule<T>("$rulesetName.$navn.$offset", sequence + offset).apply {
                    nameWithoutPatternOffset = "$rulesetName.$navn"
                }
                pattern.registerRule(rule, patternElement)
                rule.patternOffset = offset
                addChild(rule)
                rule.createRuleContent(patternElement)
                offset++
                rulesInPattern.add(rule)
            }

            /**
             * An empty pattern rule without content is created for empty patterns.
             * This makes it possible for other rules to chain this rule.
             */
            if (pattern.get().isEmpty()) {
                val rule = Rule<T>("$rulesetName.$navn", sequence)
                addChild(rule)
                rulesInPattern.add(rule)
            }

            rulesInPattern
        }
    }

    @TestOnly
    open fun test(): T {
        return internalRun()
    }

    @TestOnly
    internal fun testAndDebug(): T {
        val ret = internalRun()
        println(this.debug())
        return ret
    }

    /**
     * Runs the ruleset
     *
     * @return value T wrapped in Optional
     */
    fun run(parent: AbstractRuleComponent): T {
        parent.addChild(this)
        return internalRun()
    }

    /**
     * Creates, sorts and evaluates the rules of the ruleset.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun internalRun(): T {
        create()

        ruleFunctionMap.values.forEach { ruleSpawn ->
            ruleSpawn.invoke().forEach {
                it.resourceMap = this.resourceMap
                it.evaluate()
                if (it.returnRule) {
                    returnValue = it.returnValue
                    return it.returnValue
                }
            }
        }

        /**
         * Ruleset must be of type Unit if no rules have returned a value.
         * If the ruleset is not of type Unit, a ClassCastException is thrown _when the value is used_.
         */
        @Suppress("UNCHECKED_CAST")
        return Unit as T
    }

    /**
     * Implementing class must define rule in this method.
     */
    abstract fun create()

    /**
     * Create next available sequence number.
     *
     * @return the resulting 100-series sequence number
     */
    @PublishedApi
    internal fun nextSequence(): Int {
        val maxSequence = ruleFunctionMap.keys.maxOrNull() ?: 0
        return maxSequence + 100
    }


    protected fun String.minstEnHarTruffet(): Uttrykk<Boolean> {
        val list = findRulesByNameStartsWith(this)
        return ListOperation(
            uttrykk = Faktum("Regelreferanse", this),
            mengdeUttrykk = Faktum(
                "uaktuelle regler",
                list.map { it.shortName() }
            ),
            operator = ListOperator.MINST_EN_AV,
            evaluator = { list.any { it.fired() } }
        )
    }

    /**
     * "AngittFlyktning".alleHarTruffet()
     *      ListSubsumsjon ALLE
     *          "JA AngittFlyktning_r1"
     *          "JA AngittFlyktning_r2"
     *          "JA AngittFlyktning_r3"
     */
    protected fun String.alleHarTruffet(): Uttrykk<Boolean> {
        val list = findRulesByNameStartsWith(this)
        return ListOperation(
            uttrykk = Faktum("Regelreferanse", this),
            mengdeUttrykk = Faktum(
                "uaktuelle regler",
                list.map { it.shortName() }
            ),
            operator = ListOperator.ALLE,
            evaluator = { list.all { it.fired() } }
        )
    }

    protected fun String.ingenHarTruffet(): Uttrykk<Boolean> {
        val list = findRulesByNameStartsWith(this)
        return ListOperation(
            uttrykk = Faktum("Regelreferanse", this),
            mengdeUttrykk = Faktum(
                "uaktuelle regler",
                list.map { it.shortName() }
            ),
            operator = ListOperator.INGEN,
            evaluator = { list.none { it.fired() } }
        )
    }

    /**
     * Checks if a rule with name equal to receiver has fired.
     *
     * @receiver the rule name
     * @return returns true if the rule is found and has fired.
     */
    protected fun String.harTruffet(): Boolean {
        validateRuleExistance(this)
        return children.filterIsInstance<Rule<T>>()
            .any { rule -> rule.nameWithoutPatternOffset == "$rulesetName.$this" && rule.fired() }
    }

    /**
     * Checks if a rule with name equal to receiver has not fired.
     *
     * @receiver the rule name
     * @return returns true if the rule is found and has not fired.
     */
    protected fun String.harIkkeTruffet(): Boolean = !this.harTruffet()

    /**
     * Checks if a pattern rule with name equal to receiver has fired on the specified [patternElement].
     *
     * @receiver the rule name
     * @param patternElement the pattern element to check
     * @return returns true if the rule is found and has fired.
     */
    protected fun <P> String.harTruffet(patternElement: P): Boolean {
        validateRuleExistance(this)
        return children.filterIsInstance<Rule<T>>()
            .any { rule ->
                rule.nameWithoutPatternOffset == "$rulesetName.$this"
                        && rule.pattern.ruleResultMap.containsKey(rule)
                        && rule.pattern.ruleResultMap[rule] == patternElement
                        && rule.fired()
            }
    }

    /**
     * Checks if a pattern rule with name equal to receiver has not fired on the specified [patternElement].
     *
     * @receiver the rule name
     * @param patternElement the pattern element to check
     * @return returns true if the rule is found and has not fired.
     */
    protected fun <P> String.harIkkeTruffet(patternElement: P): Boolean {
        return !harTruffet(patternElement)
    }

    /**
     * Function that validates the existance of given a [ruleName].
     */
    private fun validateRuleExistance(ruleName: String) = findRulesByNameStartsWith(ruleName)

    private fun findRulesByNameStartsWith(rettsregelNavn: String): List<Rule<T>> {
        return children.filterIsInstance<Rule<T>>()
            .filter { rule -> rule.nameWithoutPatternOffset.startsWith("$rulesetName.$rettsregelNavn") }
            .ifEmpty {
                throw InvalidRulesetException("No rule with name that starts with ['$rettsregelNavn'] found during rule chaining.")
            }
    }

    override fun name(): String = rulesetName
    override fun fired(): Boolean = true
    override fun type(): RuleComponentType = RuleComponentType.REGELSETT
    override fun toString(): String = "${type()}: $rulesetName"

}