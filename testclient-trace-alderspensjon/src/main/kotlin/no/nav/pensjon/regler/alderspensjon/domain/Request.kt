package no.nav.pensjon.regler.alderspensjon.domain

import java.time.LocalDate

data class Request(
    val virkningstidspunkt: LocalDate,
    val person: Person
)
