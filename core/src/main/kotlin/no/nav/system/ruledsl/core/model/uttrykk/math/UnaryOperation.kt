package no.nav.system.ruledsl.core.model.uttrykk.math

import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.model.Uttrykk

/**
 * Unary funksjon for operasjoner på ett enkelt element (avrund, min, max, etc)
 */
data class UnaryOperation<T : Number>(
    val uttrykk: Uttrykk<Number>,
    val navn: String,
    val evaluator: () -> T
) : Uttrykk<T> {

    override val verdi: T by lazy { evaluator() }

    override fun notasjon(): String = "$navn(${uttrykk.notasjon()})"

    override fun konkret(): String = "$navn(${uttrykk.konkret()})"

    override fun faktumSet(): Set<Faktum<*>> = uttrykk.faktumSet()
}
