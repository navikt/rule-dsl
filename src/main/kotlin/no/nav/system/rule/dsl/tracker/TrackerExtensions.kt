package no.nav.system.rule.dsl.tracker

import no.nav.system.rule.dsl.rettsregel.Faktum

/**
 * Generate complete explanation of a Faktum showing HVA/HVORDAN/HVORFOR.
 *
 * Works with any registered TrackerResource:
 * ```
 * override fun run(): Response {
 *     putResource(TrackerResource::class, IndentedTextTracker())
 *     return super.run()
 * }
 * ```
 *
 * If no tracker is registered, uses NoOpTracker which returns an informative message.
 *
 * @param filter Which components to include (default: FUNCTIONAL)
 * @return Formatted explanation string
 * @throws IllegalStateException if Faktum not in ARC tree
 */
fun <T : Any> Faktum<T>.forklar(filter: Filter = Filters.FUNCTIONAL): String {
    val node = wrapperNode ?: throw IllegalStateException(
        "Faktum '$navn' is not in the ARC tree. Only Faktum created via sporing() can be explained."
    )

    return node.tracker().explainFaktumAsString(this, filter)
}
