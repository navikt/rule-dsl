package no.nav.pensjon.regler.sliterordning.config

import no.nav.system.ruledsl.core.resource.AbstractResource
import java.time.LocalDate

class GrunnbeløpSatsResource : AbstractResource() {

    val grunnbeløpMap: Map<ClosedRange<LocalDate>, Int> = mapOf(
        LocalDate.of(2000, 5, 1)..LocalDate.of(2010, 4, 30) to 110000,
        LocalDate.of(2010, 5, 1)..LocalDate.of(2020, 4, 30) to 120000,
        LocalDate.of(2020, 5, 1)..LocalDate.of(2030, 4, 30) to 130000,
    )

}