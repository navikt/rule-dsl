package no.nav.pensjon.regler.alderspensjon.domain

import no.nav.pensjon.regler.alderspensjon.domain.koder.UnntakEnum
import no.nav.system.ruledsl.core.expression.Faktum


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
