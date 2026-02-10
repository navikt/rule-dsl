package no.nav.pensjon.regler.alderspensjon.domain

import no.nav.pensjon.regler.alderspensjon.domain.koder.UnntakEnum
import no.nav.system.ruledsl.core.expression.Verdi

data class Unntak(var unntak: Verdi<Boolean>, val unntakType: Verdi<UnntakEnum>)
