package no.nav.system.rule.dsl.enums

enum class UtfallType {
    OPPFYLT,
    IKKE_OPPFYLT,
    IKKE_RELEVANT;

    fun motsatt(): UtfallType {
        return when (this) {
            OPPFYLT -> IKKE_OPPFYLT
            IKKE_OPPFYLT -> OPPFYLT
            IKKE_RELEVANT -> IKKE_RELEVANT
        }
    }
}