package no.nav.system.ruledsl.core.reference

import no.nav.system.ruledsl.core.expression.Faktum

/**
 * Add a reference to a Faktum, returning a new Faktum with the reference attached.
 *
 * Example:
 * ```
 * val aldersgrense = Faktum("aldersgrense", 67)
 *     .ref(Reference("FTL-20-7", "https://lovdata.no/..."))
 * ```
 */
fun <T : Any> Faktum<T>.ref(reference: Reference): Faktum<T> {
    return this.copy(references = this.references + reference)
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
