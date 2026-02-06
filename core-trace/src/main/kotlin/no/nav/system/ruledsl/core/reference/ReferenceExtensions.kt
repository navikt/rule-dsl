package no.nav.system.ruledsl.core.reference

import no.nav.system.ruledsl.core.expression.Faktum

/**
 * Add a reference to a Faktum, returning a new Faktum with the reference attached.
 *
 * Note: This creates a new Faktum with the same expression but updated references.
 * The new Faktum will need to be recorded separately if tracing is required.
 *
 * Example:
 * ```
 * val aldersgrense = faktum("aldersgrense", Verdi(67))
 *     .ref(Reference("FTL-20-7", "https://lovdata.no/..."))
 * ```
 */
fun <T : Any> Faktum<T>.ref(reference: Reference): Faktum<T> {
    return Faktum.create(name, expression, references + reference)
}

/**
 * Convenience: Add reference by id and url directly without creating Reference object.
 *
 * Example:
 * ```
 * val aldersgrense = Faktum("aldersgrense", 67)
 *     .ref("FTL-20-7", "https://lovdata.no/...")
 * ```
 */
fun <T : Any> Faktum<T>.ref(id: String, url: String): Faktum<T> {
    return this.ref(Reference(id, url))
}
