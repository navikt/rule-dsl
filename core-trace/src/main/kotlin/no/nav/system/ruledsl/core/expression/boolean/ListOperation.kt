package no.nav.system.ruledsl.core.expression.boolean

import no.nav.system.ruledsl.core.helper.yesNo
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.NegatableOperator

/**
 * Hvordan et uttrykk, [expression] forholder seg til en mengde av uttrykk (erBlant, erIkkeBlant).
 */
internal data class ListOperation(
    val expression: Expression<*>,
    val list: Expression<List<*>>,
    val operator: ListOperator,
    private val evaluator: () -> Boolean
) : Expression<Boolean> {

    override val value: Boolean by lazy { evaluator() }

    override fun notation(): String = "${value.yesNo()} '${expression.notation()}'${operatorText()}'${list.notation()}'"

    override fun concrete(): String = "${value.yesNo()} '${expression.concrete()}'${operatorText()}'${list.value.map { it.toString() }}'"

    override fun toString(): String = "${value.yesNo()} ${expression}${operatorText()}${list}"

    private fun operatorText(): String = if (value) operator.text else operator.negated()

    override fun faktumSet(): Set<Faktum<*>> = expression.faktumSet() + list.faktumSet()
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