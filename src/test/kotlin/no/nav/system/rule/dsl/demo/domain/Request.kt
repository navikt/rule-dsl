package no.nav.system.rule.dsl.demo.domain

import java.time.LocalDate

data class Request(
    val virkningstidspunkt: LocalDate,
    val person: Person
)