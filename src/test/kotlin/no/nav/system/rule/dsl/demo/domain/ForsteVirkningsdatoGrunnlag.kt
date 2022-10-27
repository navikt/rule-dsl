package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.FaktumGenerator
import java.time.LocalDate

data class ForsteVirkningsdatoGrunnlag(
    val virkningsdato: LocalDate,
    val kravlinjeType: YtelseEnum,
) : FaktumGenerator {

    override fun toFaktum(): Faktum<ForsteVirkningsdatoGrunnlag> {
        return Faktum("ForsteVirkningsdatoGrunnlag", this)
    }
}