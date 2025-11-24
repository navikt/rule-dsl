package no.nav.system.rule.dsl.enums

enum class RuleComponentType {
    REGELTJENESTE,
    REGELFLYT,
    FORGRENING,
    GREN,
    REGELSETT,
    REGEL,
    PREDIKAT,
    FAKTUM;

    override fun toString(): String {
        return name.lowercase()
    }

}