package no.nav.system.ruledsl.core.model.uttrykk

import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.model.Uttrykk

/**
 * Unnamed constant - treated as atomic unit.
 * This is can NOT be created by users.
 */
internal data class Const<T : Any>(
    override val verdi: T
) : Uttrykk<T> {

    override fun notasjon(): String = verdi.toString()

    override fun konkret(): String = verdi.toString()

    override fun faktumSet(): Set<Faktum<out Any>> = emptySet()

    override fun toString(): String = "'$verdi'"
}