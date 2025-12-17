package no.nav.pensjon.regler.alderspensjon.domain

import no.nav.pensjon.regler.alderspensjon.domain.koder.YtelseEnum
import java.time.LocalDate

data class ForsteVirkningsdatoGrunnlag(val virkningsdato: LocalDate, val kravlinjeType: YtelseEnum)
