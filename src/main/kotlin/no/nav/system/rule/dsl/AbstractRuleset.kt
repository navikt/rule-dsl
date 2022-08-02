package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.pattern.Pattern
import no.nav.system.rule.dsl.error.InvalidRulesetException
import java.util.*

/**
 * Abstract Ruleset manages creation, ordering and execution of rules specified in implementing classes.
 *
 * @param T the return type of the ruleset
 *
 */
abstract class AbstractRuleset<T : Any> : AbstractRuleComponent() {

    /**
     * List of rules that have been evaluated regardless of the rule has fired or not.
     */
    internal var evaluatedRuleList: MutableList<Rule<T>> = mutableListOf()

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
     * Creates a standard rule using the rule mini-DSL.
     *
     * @param navn the rule name
     * @param createRuleContent the Rule function that populates the Rule object.
     */
    inline fun regel(navn: String, crossinline createRuleContent: Rule<T>.() -> Unit) {
        val sequence = nextSequence()
        ruleFunctionMap[sequence] = {
            val rule = Rule<T>("$rulesetName.$navn", sequence)
            rule.parent = this
            children.add(rule)
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
        crossinline createRuleContent: Rule<T>.(P) -> Unit
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
                rule.parent = this
                children.add(rule)
                rule.createRuleContent(patternElement)
                offset++
                rulesInPattern.add(rule)
            }
            rulesInPattern
        }
    }

    open fun test(): Optional<T> {
        return internalRun()
    }

    /**
     * Runs the ruleset
     *
     * @return value T wrapped in Optional
     */
    fun run(parent: AbstractRuleComponent): Optional<T> {
        parent.children.add(this)
        this.parent = parent

        return internalRun()
    }

    /**
     * Creates, sorts and evaluates the rules of the ruleset.
     */
    fun internalRun(): Optional<T> {
        create()

        ruleFunctionMap.values.forEach { ruleSpawn ->
            ruleSpawn.invoke().forEach {
                it.evaluate()
                evaluatedRuleList.add(it)
                if (it.returnRule) {
                    return it.returnValue
                }
            }
        }
        return Optional.empty()
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

    /**
     * Checks if a rule with name equal to receiver has fired.
     *
     * @receiver the rule name
     * @return returns true if the rule is found and has fired.
     */
    protected fun String.harTruffet(): Boolean {
        validateRuleExistance(this)

        return evaluatedRuleList.stream()
            .anyMatch { rule -> rule.nameWithoutPatternOffset == "$rulesetName.$this" && rule.fired() }
    }

    /**
     * Checks if a rule with name equal to receiver has not fired.
     *
     * @receiver the rule name
     * @return returns true if the rule is found and has not fired.
     */
    protected fun String.harIkkeTruffet(): Boolean {
        return !this.harTruffet()
    }

    /**
     * Checks if a pattern rule with name equal to receiver has fired on the specified [patternElement].
     *
     * @receiver the rule name
     * @param patternElement the pattern element to check
     * @return returns true if the rule is found and has fired.
     */
    protected fun <P> String.harTruffet(patternElement: P): Boolean {
        validateRuleExistance(this)

        return evaluatedRuleList.stream()
            .anyMatch { rule ->
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
    private fun validateRuleExistance(ruleName: String) {
        evaluatedRuleList.stream()
            .filter { rule -> rule.nameWithoutPatternOffset == "$rulesetName.$ruleName" }
            .findAny()
            .orElseThrow { InvalidRulesetException("No rule with name ['$rulesetName.$ruleName'] found during rule chaining.") }
    }

    override fun name(): String = rulesetName
    override fun fired(): Boolean = true
    override fun type(): String = AbstractRuleset::class.java.name
}