package no.nav.system.rule.dsl

import java.util.regex.Matcher
import java.util.regex.Pattern

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
     */
    internal fun evaluatedDomainText(): String {
        val openingBrackets = countOccurrences(domainText!!, '[')
        val closingBrackets = countOccurrences(domainText!!, ']')

        if (openingBrackets != closingBrackets) {
            throw IllegalArgumentException("Antall start- og slutt brackets må stemme overens.")
        }

        val findChoices = findChoices(domainText!!)

        for (i in 0 until openingBrackets) {
            val indexStart = domainText!!.indexOf("[")
            val indexStop = domainText!!.indexOf("]")
            val result = findChoices[i]
            domainText = domainText!!.replaceRange(indexStart..indexStop, result)
        }
        return domainText!!
    }

    private fun findChoices(domainText: String): MutableList<String> {
        val p = Pattern.compile("\\[(.*?)]")
        val m: Matcher = p.matcher(domainText)

        val choiceList = mutableListOf<String>()

        while (m.find()) {
            val choices = m.group(1).split("|")
            if (fired) {
                choiceList.add(choices[0])
            } else {
                choiceList.add(choices[1])
            }
        }
        return choiceList
    }

    private fun countOccurrences(s: String, ch: Char): Int {
        return s.count { it == ch }
    }


    internal fun isDomainPredicate(): Boolean = !domainText.isNullOrBlank()

    override fun name(): String = ""
    override fun type(): String = "predikat"
    override fun fired(): Boolean = fired
}