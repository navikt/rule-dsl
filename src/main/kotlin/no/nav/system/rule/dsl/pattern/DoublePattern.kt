package no.nav.system.rule.dsl.pattern

import no.nav.system.rule.dsl.Rule

/**
 * Creates a combination of two [SinglePattern] by combining each
 * element in the [firstPattern] with each element in the [secondPattern].
 *
 * Elements are combined using the [pairItemFunction].
 */
class DoublePattern<K, V>(
    private var firstPattern: SinglePattern<K>,
    private var secondPattern: SinglePattern<V>,
    private var pairItemFunction: (K, V) -> Boolean
) : Pattern<Pair<K, V>> {

    override val ruleResultMap: HashMap<Rule, Pair<K, V>> = HashMap()

    override fun get(): List<Pair<K, V>> {
        return pairItemsByFunction(firstPattern.get(), secondPattern.get(), pairItemFunction).toList()
    }

    fun remove(first: K, second: V) {
        firstPattern.remove(first)
        secondPattern.remove(second)
    }
}

