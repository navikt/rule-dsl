package no.nav.system.ruledsl.core.enums

enum class RuleComponentType {
    REGELTJENESTE,
    REGELFLYT,
    FORGRENING,
    GREN,
    REGELSETT,
    REGEL,
    PREDIKAT,
    BETINGELSE,
    FAKTUM;

    override fun toString(): String {
        return name.lowercase()
    }

}