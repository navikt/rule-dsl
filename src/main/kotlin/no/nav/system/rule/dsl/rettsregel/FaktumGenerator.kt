package no.nav.system.rule.dsl.rettsregel

interface FaktumGenerator {
    fun  toFaktum(): Faktum<*>
}