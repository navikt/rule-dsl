package no.nav.system.rule.dsl.pattern

/**
 * Function to pair item by given function, or a default one which pairs everything.
 */
inline fun <K, V> pairItemsByFunction(
    first: List<K>,
    second: List<V>,
    pairingFunction: (K, V) -> Boolean = { _, _ -> true }
): MutableList<Pair<K, V>> {
    val result = mutableListOf<Pair<K, V>>()
    for (itemInFirst in first) {
        for (itemInSecond in second) {
            if (pairingFunction(itemInFirst, itemInSecond)) {
                result.add(Pair(itemInFirst, itemInSecond))
            }
        }
    }
    return result
}

/**
 * Helper extensionfunction on all lists to create pattern using optional filter.
 *
 * @param filter optional
 */
fun <P> List<P>.createPattern(filter: (P) -> Boolean = { true }): SinglePattern<P> {
    return SinglePattern(this.toMutableList(), filter)
}