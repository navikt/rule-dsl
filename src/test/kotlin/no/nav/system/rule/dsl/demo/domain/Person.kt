package no.nav.system.rule.dsl.demo.domain

import java.time.LocalDate

data class Person(
    val id: Int,
    val rolle: String = "UKJENT",
    val fødselsdato: LocalDate,
    val erGift: Boolean = false,
    val boperioder: List<Boperiode> = mutableListOf()
)