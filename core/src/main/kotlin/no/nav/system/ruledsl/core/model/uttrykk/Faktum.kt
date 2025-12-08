package no.nav.system.ruledsl.core.model

import no.nav.system.ruledsl.core.model.uttrykk.Const
import no.nav.system.ruledsl.core.reference.Reference
import java.io.Serializable

/**
 * Numeric expression tree.
 *
 * IMPORTANT: This is now INTERNAL to Faktum<Number>.
 * Users do not work with Uttrykk directly - they use Faktum operators.
 *
 */
interface Uttrykk<out T : Any> : Serializable {
    val verdi: T
    fun notasjon(): String
    fun konkret(): String

    /**
     * Hvilke navngitte faktum bidrar til dette uttrykket.
     */
    fun faktumSet(): Set<Faktum<*>>
}

/**
 * Named expression - treated as atomic unit for fact tracking.
 *
 * Faktum wraps any [Uttrykk] and gives it a name, making it user-visible and trackable.
 * It acts as the boundary between anonymous calculations (Const, BinaryOperation) and
 * named business facts that appear in rule explanations.
 *
 * Pure data class - presentation is handled separately via extension functions.
 * This separation allows users to write custom explanation/presentation logic.
 *
 * Example:
 * ```
 * val alder = Faktum("Alder", 67)              // Named fact
 * val sats = Faktum("Sats", 1000)              // Named fact
 * val produkt = alder * sats                   // Anonymous BinaryOperation
 * val tillegg = Faktum("Tillegg", produkt)     // Wrap as named result
 *
 * produkt.faktumSet()  // → { alder, sats }    Input facts
 * tillegg.faktumSet()  // → { tillegg }         Result fact (stops at boundary)
 * tillegg.uttrykk.faktumSet()  // → { alder, sats }  Contributing facts inside
 * ```
 *
 * This is can be created by users.
 */
data class Faktum<T : Any>(
    val navn: String,
    val uttrykk: Uttrykk<T>,
    val references: List<Reference> = emptyList()
) : Uttrykk<T> {

    constructor(
        navn: String,
        verdi: T,
        references: List<Reference> = emptyList()
    ) : this(
        navn = navn,
        uttrykk = Const(verdi),
        references = references
    )

    /**
     * Backlink to the FaktumNode wrapper when this Faktum is added to the ARC tree.
     * Used by transformers to walk up the tree for HVORFOR explanations.
     */
    @Transient
    var wrapperNode: FaktumNode<T>? = null
        internal set

    /**
     * True if this Faktum holds a constant value (not a calculated expression).
     * Useful for transformers to determine if HVORDAN section should be shown.
     */
    val isConstant: Boolean
        get() = uttrykk is Const<*>

    override val verdi: T by lazy { uttrykk.verdi }

    override fun notasjon(): String = navn

    override fun konkret(): String = verdi.toString()

    /**
     * Returns this Faktum as the atomic unit.
     *
     * IMPORTANT: Returns `setOf(this)` rather than `uttrykk.faktumSet()` because
     * Faktum defines the boundary for named facts. This stops recursion at the name,
     * enabling layered explanations (result facts vs. input facts).
     *
     * To access contributing facts inside this Faktum, use `uttrykk.faktumSet()`.
     */
    override fun faktumSet(): Set<Faktum<*>> = setOf(this)

    override fun toString(): String = "'$navn' ($verdi)"
}
