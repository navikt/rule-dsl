package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.rettsregel.Faktum

data class Tilleggspensjon(
    var netto: Faktum<Int> = Faktum("netto", 0),
    var nettoPerAr: Faktum<Double> = Faktum("nettoPerAr", 0.0),
    var apKap19MedGJR: Faktum<Int> = Faktum("apKap19MedGJR", 0),
    var apKap19UtenGJR: Faktum<Int> = Faktum("apKap19UtenGJR", 0),
    var referansebelop: Faktum<Int> = Faktum("referansebelop", 0)
)