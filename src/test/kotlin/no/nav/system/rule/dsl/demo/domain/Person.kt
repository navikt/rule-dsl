package no.nav.system.rule.dsl.demo.domain

import java.time.LocalDate

data class Person(
    val id: Int,
    val rolle: String = "UKJENT",
    val fødselsdato: LocalDate,
    val erGift: Boolean = false,
    val boperioder: List<Boperiode> = mutableListOf(),
    val flyktning: Boolean = false,
    var trygdetidK19: Trygdetid? = null,
    var trygdetidK20: Trygdetid? = null,
    var inngangOgEksportgrunnlag: InngangOgEksportgrunnlag? = null
)