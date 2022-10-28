package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum
import no.nav.system.rule.dsl.rettsregel.Faktum

data class InngangOgEksportgrunnlag(
    val unntakFraForutgaendeMedlemskap: Unntak = Unntak(
        Faktum("UnntakFraForutgaendeMedlemskap", false),
        Faktum("UnntakFraForutgaendeMedlemskap type", UnntakEnum.FLYKT_ALDER)
    ),
    val unntakFraForutgaendeTT: Unntak = Unntak(
        Faktum("unntakFraForutgaendeTT", false),
        Faktum("unntakFraForutgaendeTT type", UnntakEnum.FLYKT_ALDER)
    )
)