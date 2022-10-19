package no.nav.system.rule.dsl.enums

import no.nav.system.rule.dsl.rettsregel.Faktum

interface SuperEnum {
    fun faktum(): Faktum<*>
    fun navn(): String
}