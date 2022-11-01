package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum
import no.nav.system.rule.dsl.rettsregel.Fact

data class InngangOgEksportgrunnlag(
    val unntakFraForutgaendeMedlemskap: Unntak = Unntak(
        Fact("UnntakFraForutgaendeMedlemskap", false),
        Fact("UnntakFraForutgaendeMedlemskap type", UnntakEnum.FLYKT_ALDER)
    ),
    val unntakFraForutgaendeTT: Unntak = Unntak(
        Fact("unntakFraForutgaendeTT", false),
        Fact("unntakFraForutgaendeTT type", UnntakEnum.FLYKT_ALDER)
    )
)