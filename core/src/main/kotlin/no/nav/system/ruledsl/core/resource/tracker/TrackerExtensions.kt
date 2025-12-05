package no.nav.system.ruledsl.core.resource.tracker

import no.nav.system.ruledsl.core.format.IndentedTextFormatter
import no.nav.system.ruledsl.core.rettsregel.Faktum


/**
 * Generate complete explanation of a Faktum showing HVA/HVORDAN/HVORFOR.
 *
 * Walks the ARC tree to build the explanation. The ARC tree is the complete execution trace -
 * all information needed (rules, predicates, values, parent relationships) is already stored in the tree.
 *
 * @param filter Which components to include (default: FUNCTIONAL)
 * @return Formatted explanation string
 * @throws IllegalStateException if Faktum not in ARC tree
 */
fun <T : Any> Faktum<T>.forklar(filter: Filter = Filters.FUNCTIONAL): String {
    wrapperNode ?: throw IllegalStateException(
        "Faktum '$navn' is not in the ARC tree. Only Faktum created via sporing() can be explained."
    )

    return IndentedTextFormatter.format(this, filter)
}
