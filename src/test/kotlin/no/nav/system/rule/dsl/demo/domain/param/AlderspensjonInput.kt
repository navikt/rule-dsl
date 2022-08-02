package no.nav.system.rule.dsl.demo.domain.param

import no.nav.system.rule.dsl.demo.domain.Person
import java.time.LocalDate

data class AlderspensjonInput(
    val person: Person,
    val virkningstidspunkt: LocalDate,
    val grunnbeløpVedVirk: Int
)