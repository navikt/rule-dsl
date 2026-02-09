package no.nav.system.ruledsl.core.expression.boolean

import no.nav.system.ruledsl.core.expression.Operator

enum class PairOperator(override val text: String) : Operator {
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