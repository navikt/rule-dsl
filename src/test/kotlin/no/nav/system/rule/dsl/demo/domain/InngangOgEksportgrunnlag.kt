package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum

data class InngangOgEksportgrunnlag(val unntakFraForutgaendeMedlemskap: Unntak, val unntakFraForutgaendeTT: Unntak)

data class Unntak(val unntak: Boolean, val unntakType: UnntakEnum)


