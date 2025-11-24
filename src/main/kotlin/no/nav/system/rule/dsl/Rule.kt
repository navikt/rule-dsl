package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.REGEL
import no.nav.system.rule.dsl.pattern.Pattern
import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Uttrykk
import no.nav.system.rule.dsl.rettsregel.helper.svarord
import kotlin.experimental.ExperimentalTypeInference

/**
 * A rule that evaluates a set of predicates and, if all predicates are true, executes an action statement.
 *
 * Rules provide a "mini-DSL" where users can enter rule predicates using special keywords.
 * @see [HVIS], [OG], [RETURNER]
 *
 * Once all predicates are evaluated to true the [actionStatement] is run.
 *
 * @param name the rule name
 * @param sequence the rule sequence
 */
@OptIn(ExperimentalTypeInference::class)
open class Rule<T : Any>(
    private val name: String,
    private val sequence: Int,
) : Comparable<Rule<T>>, AbstractRuleComponent() {

    /**
     * Functional description of the rule
     */
    private var comment: String = ""

    /**
     * Reference to optional pattern.
     */
    lateinit var pattern: Pattern<*>

    /**
     * Rules created with a Pattern specifies their individual ordering in [patternOffset].
     */
    var patternOffset: Int? = null

    /**
     * Same as [name] but without the sequence numbers added by [Pattern].
     */
    var nameWithoutPatternOffset: String = name

    /**
     * Set to true if the rule has been evaluated.
     */
    internal var evaluated = false

    /**
     * Set to true if every predicate is true.
     */
    protected var fired = false

    /**
     * The code that executes if the rule is [fired]
     */
    private var actionStatement: () -> Unit = {}

    /**
     * The code that executes if the rule is not [fired]
     */
    private var elseStatement: () -> Unit = {}

    /**
     * The value this rule will return.
     */
    internal lateinit var returnValue: T

    /**
     * Set to true if rule has a return value. When set to true this rule will stop ruleset evaluation if fired.
     */
    internal var returnRule = false

    private val predicateFunctionList = mutableListOf<() -> Predicate>()

    /**
     * DSL: Technical Predicate entry.
     */
    fun HVIS(booleanFunction: () -> Boolean) {
        OG(booleanFunction)
    }

    /**
     * DSL: Technical Predicate entry.
     */
    fun OG(booleanFunction: () -> Boolean) {
        predicateFunctionList.add {
            Predicate(
                function = booleanFunction
            )
        }
    }

    /**
     * DSL: Functional Predicate entry (Domain predicates - boolean expressions).
     */
    @OverloadResolutionByLambdaReturnType
    @JvmName("UttrykkBooleanHVIS")
    @DslDomainPredicate
    fun HVIS(uttrykkFunction: () -> Uttrykk<Boolean>) {
        OG(uttrykkFunction)
    }

    /**
     * DSL: Functional Predicate entry (Domain predicates - boolean expressions).
     */
    @OverloadResolutionByLambdaReturnType
    @JvmName("UttrykkBooleanOG")
    @DslDomainPredicate
    fun OG(uttrykkFunction: () -> Uttrykk<Boolean>) {
        predicateFunctionList.add {
            TrackablePredicate(
                uttrykk = uttrykkFunction()
            )
        }
    }

    /**
     * DSL: Action statement entry.
     */
    fun SÅ(action: () -> Unit) {
        this.actionStatement = action
    }

    /**
     * DSL: Else statement entry.
     */
    fun ELLERS(action: () -> Unit) {
        this.elseStatement = action
    }

    /**
     * DSL: Return value entry.
     */
    fun RETURNER(returnValue: T) {
        this.returnValue = returnValue
        returnRule = true
    }

    /**
     * Provides a function for rule documentation.
     */
    fun kommentar(kommentar: String) {
        comment = kommentar
    }

    /**
     * Evaluates the [children]. A rule is considered [fired] once all predicates are evaluated to true.
     * Rules that fire invoke their [actionStatement].
     */
    fun evaluate() {
        fired = predicateFunctionList.isNotEmpty()
        evaluated = true

        run predLoop@{
            predicateFunctionList.forEach { predicateFunction ->
                val predicate = predicateFunction.invoke()

                if (predicate is TrackablePredicate) {
                    addChild(predicate)
                }

                /**
                 * Predicate must be evaluated first or terminateEvaluation would not be set.
                 */
                fired = predicate.fired && fired

                if (!fired && predicate.terminateEvaluation) {
                    return@predLoop
                }
            }
        }

        if (fired) {
            actionStatement.invoke()
        } else {
            elseStatement.invoke()
        }
    }

    /**
     * Ruleset ordering by rule sequence
     */
    override fun compareTo(other: Rule<T>): Int {
        return compareValues(this.sequence, other.sequence)
    }

    internal fun prettyDoc(): String {
        return comment.trim().replace('\n', ' ').replace("\"", "\\\"").replace(" +".toRegex(), " ")
    }

    internal fun shortName(): String {
        return name.substringAfter('.', name).substringBefore('.')
    }

    /**
     * Full rule name including pattern offset.
     */
    override fun name(): String = name
    override fun fired(): Boolean = fired
    override fun type(): RuleComponentType = REGEL
    override fun toString(): String = "${type()}: ${fired().svarord()} ${name()}"

}

