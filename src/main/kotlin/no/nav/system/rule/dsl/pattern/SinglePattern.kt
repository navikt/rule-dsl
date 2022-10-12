package no.nav.system.rule.dsl.pattern

import no.nav.system.rule.dsl.Rule

/**
 * The [SinglePattern] class facilitates evaluating a [Rule] object on every element [P] in [innListe].
 *
 * @param innListe the list of elements the rule should evaluate
 * @param filter applied to [innListe]
 */
class SinglePattern<P>(
    private val innListe: MutableList<P> = mutableListOf(),
    private val filter: (P) -> Boolean = { true }
) : MutableList<P> by innListe, Pattern<P> {

    override val ruleResultMap: HashMap<Rule, P> = HashMap()

    /**
     * Combining a secondary [SinglePattern] creates a [DoublePattern] where patternElements are paired using [pairItemFunction]
     *
     * @return a [DoublePattern] of pairs of elements of both patterns (Pair<K, V>).
     */
    fun <V> combineWithPattern(
        otherPattern: SinglePattern<V>,
        pairItemFunction: (P, V) -> Boolean
    ): DoublePattern<P, V> {
        return DoublePattern(this, otherPattern, pairItemFunction)
    }

    /**
     * Resets the pattern content
     *
     * @param elements new pattern content
     */
    fun clearAndAddAll(elements: Collection<P>) {
        innListe.clear()
        innListe.addAll(elements)
    }

    /**
     * Get pattern contents
     *
     * @return the filtered [innListe] pattern contents
     */
    override fun get(): List<P> {
        return innListe.filter(filter)
    }
}
