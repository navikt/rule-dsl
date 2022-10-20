package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.*
import no.nav.system.rule.dsl.enums.UtfallType
import no.nav.system.rule.dsl.enums.UtfallType.OPPFYLT
import no.nav.system.rule.dsl.pattern.Pattern
import no.nav.system.rule.dsl.rettsregel.AbstractSubsumsjon
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.ParSubsumsjon
import no.nav.system.rule.dsl.rettsregel.erLik
import svarord
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
    private val sequence: Int
) : Comparable<Rule>, AbstractResourceHolder() {

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

    @OverloadResolutionByLambdaReturnType
    @JvmName("FaktumHVIS")
    @DslDomainPredicate
    fun HVIS(predicateFunction: () -> Faktum<Boolean>) {
        OG(predicateFunction)
    }

    @OverloadResolutionByLambdaReturnType
    @JvmName("FaktumOG")
    @DslDomainPredicate
    fun OG(predicateFunction: () -> Faktum<Boolean>) {
        predicateFunctionList.add { predicateFunction.invoke() erLik Faktum(true) }
    }

    /**
     * DSL: Functional Predicate entry.
     */
    @OverloadResolutionByLambdaReturnType
    @JvmName("FagHVIS")
    @DslDomainPredicate
    fun HVIS(arcFunction: () -> AbstractSubsumsjon) {
        OG(arcFunction)
    }

    /**
     * DSL: Functional Predicate entry.
     */
    @OverloadResolutionByLambdaReturnType
    @JvmName("FagOG")
    fun OG(arcFunction: () -> AbstractSubsumsjon) {
        predicateFunctionList.add(arcFunction)
    }

    // TODO Slett 'utfall' og bytt ut alle kall mot denne med 'konstruerUtfall'. Fjern også kall til 'konstruerUtfall' fra 'evaluate'.
    internal var utfall: Utfall? = null
    private var utfallFunksjon: (() -> Utfall)? = null

    fun SVAR(utfallType: UtfallType = OPPFYLT, svarFunction: () -> Utfall) {
        utfallFunksjon = {
            svarFunction.invoke().also {
                it.regel = this
                it.utfallType = if (fired) utfallType else utfallType.motsatt()
            }
        }
    }

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
    override fun type(): RuleComponentType = REGEL
    override fun toString(): String {
        val utfallTekst = utfall?.let { " utfallType: ${it.utfallType}" } ?: ""
        return "${type()}: ${fired().svarord()} ${name()} $utfallTekst"
    }
}

