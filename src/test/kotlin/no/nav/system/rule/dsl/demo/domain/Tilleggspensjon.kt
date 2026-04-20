package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.formel.Formel

data class Tilleggspensjon(
    var netto: Formel<Int> = Formel("netto", 0),
    var nettoPerAr: Formel<Double> = Formel("nettoPerAr", 0.0),
    var apKap19MedGJR: Formel<Int> = Formel("apKap19MedGJR", 0),
    var apKap19UtenGJR: Formel<Int> = Formel("apKap19UtenGJR", 0),
    var referansebelop: Formel<Int> = Formel("referansebelop", 0)
)