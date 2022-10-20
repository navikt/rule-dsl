package no.nav.system.rule.dsl.enums

import no.nav.system.rule.dsl.enums.Komparator as Komparator

interface Komparator {
    fun negated(): String
}

enum class ParKomparator( val text: String): Komparator {
    FØR_ELLER_LIK(" er tom "),
    FØR(" er før "),
    ETTER_ELLER_LIK(" er fom "),
    ETTER(" er etter "),
    MINDRE_ELLER_LIK(" er mindre eller lik "),
    MINDRE(" er mindre enn "),
    STØRRE_ELLER_LIK(" er større eller lik "),
    STØRRE(" er større enn "),
    LIK(" er lik "),
    ULIK(" er ulik ");

    override fun negated(): String {
        return when (this) {
            FØR_ELLER_LIK -> " må være tom "
            FØR -> " må være før "
            ETTER_ELLER_LIK -> " må være fom "
            ETTER -> " må være etter "
            MINDRE_ELLER_LIK -> " må være mindre eller lik"
            MINDRE -> " må være mindre enn "
            STØRRE_ELLER_LIK -> " må være større eller lik "
            STØRRE -> " må være større enn "
            LIK -> " må være lik "
            ULIK -> " må være ulik "
        }
    }
}

enum class MengdeKomparator( val text: String): Komparator {
    ER_BLANDT(" er blandt "),
    ER_IKKE_BLANDT(" er ikke blandt "),
    ALLE(" gjelder samtlige"),
    INGEN(" ingen "),
    MINST_EN_AV(" minst én er JA ");

    override fun negated(): String {
        return when (this) {
            ER_BLANDT -> " må være blandt "
            ER_IKKE_BLANDT -> " må ikke være blandt "
            ALLE -> " ingen "
            INGEN -> " minst én er JA "
            MINST_EN_AV -> " ingen er JA "
        }
    }
}