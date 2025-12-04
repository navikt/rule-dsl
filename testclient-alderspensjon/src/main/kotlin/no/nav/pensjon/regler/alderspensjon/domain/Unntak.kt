package no.nav.pensjon.regler.alderspensjon.domain

import no.nav.pensjon.regler.alderspensjon.domain.koder.UnntakEnum
import no.nav.system.ruledsl.core.rettsregel.Faktum

data class Unntak(var unntak: Faktum<Boolean>, val unntakType: Faktum<UnntakEnum>)