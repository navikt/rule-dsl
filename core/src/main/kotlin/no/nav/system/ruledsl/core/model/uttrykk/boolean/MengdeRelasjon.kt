package no.nav.system.ruledsl.core.model.uttrykk.boolean

import no.nav.system.ruledsl.core.helper.svarord
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.model.Uttrykk
import no.nav.system.ruledsl.core.model.uttrykk.NegatableOperator

/**
 * Hvordan et uttrykk, [uttrykk] forholder seg til en mengde av uttrykk (erBlant, erIkkeBlant).
 */
internal data class MengdeRelasjon(
    val uttrykk: Uttrykk<*>,
    val mengde: Uttrykk<List<*>>,
    val operator: ListOperator,
    private val evaluator: () -> Boolean
) : Uttrykk<Boolean> {

    override val verdi: Boolean by lazy { evaluator() }

    override fun notasjon(): String = "${verdi.svarord()} '${uttrykk.notasjon()}'${operatorText()}'${mengde.notasjon()}'"

    override fun konkret(): String = "${verdi.svarord()} '${uttrykk.konkret()}'${operatorText()}'${mengde.verdi.map { it.toString() }}'"

    override fun toString(): String = "${verdi.svarord()} ${uttrykk}${operatorText()}${mengde}"

    private fun operatorText(): String = if (verdi) operator.text else operator.negated()

    override fun faktumSet(): Set<Faktum<*>> = uttrykk.faktumSet() + mengde.faktumSet()
}

enum class ListOperator(override val text: String) : NegatableOperator {
    ER_BLANDT(" er blandt "),
    ER_IKKE_BLANDT(" er ikke blandt "),
    ALLE(" gjelder samtlige"),
    INGEN(" ingen "),
    MINST_EN_AV(" minst én er JA "),
    MINST(" minst "),
    MAKS(" maks "),
    AKKURAT(" akkurat ");

    override fun negated(): String {
        return when (this) {
            ER_BLANDT -> " må være blandt "
            ER_IKKE_BLANDT -> " må ikke være blandt "
            ALLE -> " ingen "
            INGEN -> " minst én er JA "
            MINST_EN_AV -> " ingen er JA "
            MINST -> " under "
            MAKS -> " over "
            AKKURAT -> " ikke akkurat "
        }
    }
}