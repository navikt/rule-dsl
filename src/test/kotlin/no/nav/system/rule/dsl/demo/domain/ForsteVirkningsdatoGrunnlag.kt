package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import java.time.LocalDate
import java.util.*

data class ForsteVirkningsdatoGrunnlag(
    val virkningsdato: LocalDate,
    val kravFremsattDato: LocalDate,
    val kravlinjeType: YtelseEnum
)