package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum
import no.nav.system.rule.dsl.rettsregel.Faktum

data class InngangOgEksportgrunnlag(val unntakFraForutgaendeMedlemskap: Unntak, val unntakFraForutgaendeTT: Unntak)

data class Unntak(val unntak: Faktum<Boolean>, val unntakType: Faktum<UnntakEnum>)


