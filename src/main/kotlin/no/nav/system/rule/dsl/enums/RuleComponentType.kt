package no.nav.system.rule.dsl.enums

enum class RuleComponentType {
    PAR_SUBSUMSJON,
    MENGDE_SUBSUMSJON,
    FAKTUM,
    PREDIKAT,
    DECISION,
    BRANCH,
    REGEL,
    REGELFLYT,
    REGELSETT,
    REGELTJENESTE;

    override fun toString(): String {
        return name.lowercase()
    }

}