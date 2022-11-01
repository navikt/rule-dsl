package no.nav.system.rule.dsl.demo.domain.param

import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.rettsregel.Fact
import java.time.LocalDate

data class AlderspensjonInput(
    val person: Person,
    val virkningstidspunkt: Fact<LocalDate>,
    val grunnbeløpVedVirk: Int
)