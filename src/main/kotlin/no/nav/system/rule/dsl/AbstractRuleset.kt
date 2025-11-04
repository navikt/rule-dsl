package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.ListComparator
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.REGELSETT
import no.nav.system.rule.dsl.error.InvalidRulesetException
import no.nav.system.rule.dsl.inspections.debug
import no.nav.system.rule.dsl.pattern.Pattern
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.ListDomainPredicate
import no.nav.system.rule.dsl.rettsregel.forklartfaktum.ForklartResultat
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
                children.add(rule)
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
                children.add(rule)
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
        this.resourceMap = parent.resourceMap
        parent.children.add(this)

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

    protected fun String.minstEnHarTruffet(): ListDomainPredicate {
        val list = findRulesByNameStartsWith(this)
        return ListDomainPredicate(
            comparator = ListComparator.MINST_EN_AV,
            verdi = Faktum("Regelreferanse", this),
            function = { list.any { it.fired() } },
            verdiList = list.filter { it.children.isNotEmpty() }.map { Faktum(it.name()) },
        )
    }

    /**
     * "AngittFlyktning".alleHarTruffet()
     *      ListSubsumsjon ALLE
     *          "JA AngittFlyktning_r1"
     *          "JA AngittFlyktning_r2"
     *          "JA AngittFlyktning_r3"
     */
    protected fun String.alleHarTruffet(): ListDomainPredicate {
        val list = findRulesByNameStartsWith(this)
        return ListDomainPredicate(
            comparator = ListComparator.ALLE,
            verdi = Faktum("Regelreferanse", this),
            function = { list.all { it.fired() } },
            verdiList = list.filter { it.children.isNotEmpty() }.map { Faktum(it.name()) }
        )
    }

    protected fun String.ingenHarTruffet(): ListDomainPredicate {
        val list = findRulesByNameStartsWith(this)
        return ListDomainPredicate(
            comparator = ListComparator.INGEN,
            verdi = Faktum("Regelreferanse", this),
            function = { list.none { it.fired() } },
            verdiList = list.filter { it.children.isNotEmpty() }.map { Faktum(it.name()) }
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

    /**
     * Returnerer resultat med regelflyt-forklaring.
     *
     * Denne metoden er tilgjengelig for alle regelsett og gir en forklaring på hvordan
     * resultatet ble nådd ved å vise hvilke regler som ble evaluert og truffet.
     *
     * For numeriske verdier med komplekse beregninger, vurder å bruke
     * faktum(grunnlag: Grunnlag<T>) i stedet for å få både HVORFOR (regelflyt)
     * og HVORDAN (AST-basert beregningsforklaring).
     *
     * @return [ForklartResultat] med resultatverdi og regelflyt-forklaring
     *
     * @see ForklartResultat for struktur på forklaringen
     * @see no.nav.system.rule.dsl.AbstractRuleComponent.faktum for numeriske verdier med AST-forklaring
     */
    fun medForklaring(): ForklartResultat<T> {
        val resultat = returnValue

        // Bygg regelflyt-forklaring ved å vise alle regler som truffet
        val regelflytForklaring = buildString {
            appendLine("regelsett: $rulesetName")

            // Finn og vis alle regler som truffet
            children
                .filter { it.fired() }
                .forEach { regel ->
                    appendLine("  regel: ${if (regel.fired()) "JA" else "NEI"} ${regel.name()}")
                }
        }.trim()

        // Hvis returnValue er Faktum, bruk navnet og verdien derfra
        return if (resultat is Faktum<*>) {
            ForklartResultat(
                name = resultat.name,
                value = resultat.value as T,
                hvorfor = regelflytForklaring
            )
        } else {
            ForklartResultat(
                name = rulesetName,
                value = resultat,
                hvorfor = regelflytForklaring
            )
        }
    }

    override fun name(): String = rulesetName
    override fun fired(): Boolean = true
    override fun type(): RuleComponentType = REGELSETT
    override fun toString(): String = "${type()}: $rulesetName"

}