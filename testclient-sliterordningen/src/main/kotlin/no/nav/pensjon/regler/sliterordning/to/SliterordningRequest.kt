package no.nav.pensjon.regler.sliterordning.to

import no.nav.pensjon.regler.sliterordning.domain.Person
import java.time.YearMonth

class SliterordningRequest(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
)