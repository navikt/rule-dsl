package no.nav.system.rule.dsl.enums

enum class RuleComponentType {
    PAR_SUBSUMSJON,
    LISTE_SUBSUMSJON,
    FAKTUM,
    PREDIKAT,
    FORGRENING,
    GREN,
    REGEL,
    REGELFLYT,
    REGELSETT,
    REGELTJENESTE;

    override fun toString(): String {
        return name.lowercase()
    }

}