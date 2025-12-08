package no.nav.system.ruledsl.core.model.uttrykk.boolean

import no.nav.system.ruledsl.core.helper.svarord
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.model.Uttrykk
import no.nav.system.ruledsl.core.model.uttrykk.NegatableOperator

/**
 * Sammenlignings operasjon for par av uttrykk (erLik, erMindreEnn, erFør, etc.).
 */
internal data class Sammenligning(
    val venstre: Uttrykk<*>,
    val høyre: Uttrykk<*>,
    val operator: PairOperator,
    val evaluator: () -> Boolean
) : Uttrykk<Boolean> {

    override val verdi: Boolean by lazy { evaluator() }

    override fun notasjon(): String = "${verdi.svarord()} '${venstre.notasjon()}'${operatorText()}'${høyre.notasjon()}'"

    override fun konkret(): String = "${verdi.svarord()} '${venstre.konkret()}'${operatorText()}'${høyre.konkret()}'"

    override fun toString(): String = "${verdi.svarord()} ${venstre}${operatorText()}${høyre}"

    private fun operatorText(): String = if (verdi) operator.text else operator.negated()

    override fun faktumSet(): Set<Faktum<*>> = venstre.faktumSet() + høyre.faktumSet()
}

enum class PairOperator(override val text: String) : NegatableOperator {
    FØR_ELLER_LIK(" er før eller lik "),
    FØR(" er før "),
    ETTER_ELLER_LIK(" er etter eller lik "),
    ETTER(" er etter "),
    MINDRE_ELLER_LIK(" er mindre eller lik "),
    MINDRE(" er mindre enn "),
    STØRRE_ELLER_LIK(" er større eller lik "),
    STØRRE(" er større enn "),
    LIK(" er lik "),
    ULIK(" er ulik ");

    override fun negated(): String {
        return when (this) {
            FØR_ELLER_LIK -> " må være før eller lik "
            FØR -> " må være før "
            ETTER_ELLER_LIK -> " må være etter eller lik "
            ETTER -> " må være etter "
            MINDRE_ELLER_LIK -> " må være mindre eller lik "
            MINDRE -> " må være mindre enn "
            STØRRE_ELLER_LIK -> " må være større eller lik "
            STØRRE -> " må være større enn "
            LIK -> " må være lik "
            ULIK -> " må være ulik "
        }
    }
}
