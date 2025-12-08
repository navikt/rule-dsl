package no.nav.system.ruledsl.core.forklaring

import no.nav.system.ruledsl.core.model.Faktum

/**
 * Generate complete explanation of a Faktum showing HVA/HVORDAN/HVORFOR.
 *
 * Walks the ARC tree to build the explanation. The ARC tree is the complete execution trace -
 * all information needed (rules, predicates, values, parent relationships) is already stored in the tree.
 *
 * Uses the default IndentedTextFormatter to produce text output.
 *
 * @param filter Which components to include (default: FUNCTIONAL)
 * @return Formatted explanation string
 * @throws IllegalStateException if Faktum not in ARC tree
 */
fun <T : Any> Faktum<T>.forklar(filter: Filter = Filters.FUNCTIONAL): String {
    return forklarMed(IndentedTextFormatter, filter)
}



fun <T : Any> Faktum<*>.forklarMed(faktumTransformer : FaktumTransformer<T>, filter: Filter = Filters.FUNCTIONAL): T {
    wrapperNode ?: throw IllegalStateException(
        "Faktum '$navn' is not in the ARC tree. Only Faktum created via sporing() can be explained."
    )

    return faktumTransformer.transform(this, filter)
}