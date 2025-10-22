package no.nav.system.rule.dsl.enums

enum class RuleComponentType {
    REGELTJENESTE,
    REGELFLYT,
    FORGRENING,
    GREN,
    REGELSETT,
    REGEL,
    PREDIKAT,
    DOMENE_PREDIKAT_PAR,
    DOMENE_PREDIKAT_LISTE;

    override fun toString(): String {
        return name.lowercase()
    }

}