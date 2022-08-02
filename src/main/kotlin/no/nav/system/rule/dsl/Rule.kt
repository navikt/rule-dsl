package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.pattern.Pattern
import no.nav.system.rule.dsl.treevisitor.Conclusion
import no.nav.system.rule.dsl.treevisitor.svarord
import java.util.*

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
open class Rule<T : Any>(
    private val name: String,
    internal val sequence: Int
) : Comparable<Rule<T>>, AbstractRuleComponent() {

    /**
     * Functional description of the rule
     */
    private var comment: String = ""

    /**
     * Each predicate function in the rule.
     */
    private val predicateList: ArrayList<Predicate> = ArrayList()

    /**
     * True if at least one predicate is a Domain predicate.
     */
    private var containsDomainPredicate: Boolean = false

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
    private var evaluated = false

    /**
     * Set to true if every predicate is true.
     */
    private var fired = false

    /**
     * The code that executes if the rule is [fired]
     */
    private var actionStatement: () -> Unit = {}

    /**
     * The value this rule will return.
     */
    var returnValue: Optional<T> = Optional.empty()

    /**
     * Set to true if rule has a return value. When set to true this rule will stop ruleset evaluation if fired.
     */
    var returnRule = false

    /**
     * DSL: Predicate entry.
     */
    fun HVIS(predicate: () -> Boolean) {
        OG(predicate)
    }

    /**
     * DSL: Domain Predicate entry.
     */
    @DslDomainPredicate
    fun HVIS(fag: String, predicate: () -> Boolean) {
        OG(fag, predicate)
    }

    /**
     * DSL: Technical Predicate entry.
     */
    fun OG(predicateFunction: () -> Boolean) {
        predicateList.add(Predicate(function = predicateFunction))
    }

    /**
     * DSL: Domain Predicate entry.
     */
    @DslDomainPredicate
    fun OG(domainText: String, predicateFunction: () -> Boolean) {
        containsDomainPredicate = true
        Predicate(domainText, predicateFunction).also {
            children.add(it)
            predicateList.add(it)
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
    fun RETURNER(returnValue: T? = null) {
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
     * Evaluates the [predicateList]. A rule is considered [fired] once all predicates are evaluated to true.
     * Rules that fire invoke their [actionStatement].
     */
    fun evaluate() {
        fired = predicateList.isNotEmpty()
        evaluated = true

        predicateList.forEach { predicate ->
            val stopEval = predicate.evaluate()
            fired = fired && predicate.fired()

            if (stopEval) {
                return
            }
        }

        if (fired) {
            actionStatement.invoke()
        }
    }

    /**
     * Creates a conclusion based on this rule object.
     * Includes domaintext if available.
     */
    fun conclusion() = Conclusion(
        evaluated = evaluated,
        fired = fired,
        ruleName = name(),
        documentation = prettyDoc(),
        reasons = predicateList
            .filter { it.isDomainPredicate() }
            .map { p -> "${p.fired().svarord()}: ${p.evaluatedDomainText()}" }
    )

    /**
     * Ruleset ordering by rule sequence
     */
    override fun compareTo(other: Rule<T>): Int {
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
