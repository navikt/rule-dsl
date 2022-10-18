package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.pattern.Pattern
import no.nav.system.rule.dsl.rettsregel.Subsumsjon
import no.nav.system.rule.dsl.rettsregel.UtfallType
import java.util.*
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
open class Rule(
    private val name: String,
    private val sequence: Int,
) : Comparable<Rule>, AbstractRuleComponent() {

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
     * The value this rule will return.
     */
    internal var returnValue: Optional<Any> = Optional.empty()

    /**
     * Set to true if rule has a return value. When set to true this rule will stop ruleset evaluation if fired.
     */
    internal var returnRule = false

    private val predicateFunctionList = mutableListOf<() -> Predicate>()

    /**
     * DSL: Technical Predicate entry.
     */
    fun HVIS(predicate: () -> Boolean) {
        OG(predicate)
    }

    /**
     * DSL: Technical Predicate entry.
     */
    fun OG(predicateFunction: () -> Boolean) {
        predicateFunctionList.add { Predicate(function = predicateFunction) }
    }

    /**
     * DSL: Functional Predicate entry.
     */
    @OverloadResolutionByLambdaReturnType
    @JvmName("FagHVIS")
    @DslDomainPredicate
    fun HVIS(arcFunction: () -> Subsumsjon) {
        OG(arcFunction)
    }

    /**
     * DSL: Functional Predicate entry.
     */
    @OverloadResolutionByLambdaReturnType
    @JvmName("FagOG")
    fun OG(arcFunction: () -> Subsumsjon) {
        predicateFunctionList.add(arcFunction)
    }

    internal var utfall: Utfall? = null
    private var utfallFunksjon: (() -> Utfall)? = null

    fun SVAR(utfallType: UtfallType? = null, svarFunction: () -> Utfall) {
        utfallFunksjon = {
            svarFunction.invoke().also {
                it.regel = this
                val tempUtfallType = utfallType ?: defaultUtfallType()
                it.utfallType = if (fired) tempUtfallType else tempUtfallType.motsatt()
            }
        }
    }

    private fun defaultUtfallType(): UtfallType = if (fired) UtfallType.OPPFYLT else UtfallType.IKKE_OPPFYLT

    /**
     * DSL: Action statement entry.
     */
    fun SÅ(action: () -> Unit) {
        this.actionStatement = action
    }

    /**
     * DSL: Return value entry.
     */
    fun RETURNER(returnValue: Any? = null) {
        if (returnValue == null) {
            this.returnValue = Optional.empty()
        } else {
            this.returnValue = Optional.of(returnValue)
        }
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
                val predicate = predicateFunction.invoke().apply {
                    parent = this@Rule
                }

                val stopEval = predicate.evaluate()
                fired = fired && predicate.fired()

                if (stopEval) {
                    return@predLoop
                }
            }
        }

        konstruerUtfall()

        if (fired) {
            actionStatement.invoke()
        }
    }

    private fun konstruerUtfall() {
        utfall = utfallFunksjon?.invoke()
//        utfall.doc = prettyDoc()
//        utfall.kilde = kilde. ..
    }

    /**
     * Ruleset ordering by rule sequence
     */
    override fun compareTo(other: Rule): Int {
        return compareValues(this.sequence, other.sequence)
    }

    internal fun prettyDoc(): String {
        return comment.trim().replace('\n', ' ').replace("\"", "\\\"").replace(" +".toRegex(), " ")
    }

    /**
     * Full rule name including pattern offset.
     */
    override fun name(): String = name
    override fun fired(): Boolean = fired
    override fun type(): String = "regel"

}

