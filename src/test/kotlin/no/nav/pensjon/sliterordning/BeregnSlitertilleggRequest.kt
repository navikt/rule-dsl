package no.nav.pensjon.sliterordning

import no.nav.pensjon.sliterordning.grunnlag.Person
import java.time.YearMonth

class BeregnSlitertilleggRequest(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) {

}