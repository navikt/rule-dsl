package no.nav.system.rule.dsl

/**
 * A single boolean statement used in evaluation of a rule.
 */
class Predicate(
    private var domainText: String? = null,
    private val function: () -> Boolean
) : AbstractRuleComponent() {
    private var fired: Boolean = false

    /**
     * Evaluates the predicate function.
     *
     * @return returns true if further evaluation of remaining predicates in the rule should be prevented.
     */
    internal fun evaluate(): Boolean {
        fired = function.invoke()
        return !fired && !isDomainPredicate()
    }

    /**
     * Example of Domain Text to be evaluated:
     * - "Faktisk trygdetid,er [lavere|høyere] enn fire-femtedelskravet."
     *  TODO: Support multiple bracket groups! :o
     */
    internal fun evaluatedDomainText(): String {
        val indexStart = domainText!!.indexOf("[")
        val indexStop = domainText!!.indexOf("]")

        val choices = domainText!!.substring(indexStart + 1, indexStop).split("|")

        val result: String = if (fired) choices[0] else choices[1]

        return domainText!!.replaceRange(indexStart..indexStop, result)
    }

    internal fun isDomainPredicate(): Boolean = !domainText.isNullOrBlank()

    override fun name(): String = ""
    override fun type(): String = "predikat"
    override fun fired(): Boolean = fired
}