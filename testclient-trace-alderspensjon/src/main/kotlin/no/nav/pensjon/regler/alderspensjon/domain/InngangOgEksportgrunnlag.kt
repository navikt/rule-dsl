package no.nav.pensjon.regler.alderspensjon.domain

import no.nav.pensjon.regler.alderspensjon.domain.koder.UnntakEnum
import no.nav.system.ruledsl.core.expression.Verdi


data class InngangOgEksportgrunnlag(
    val unntakFraForutgaendeMedlemskap: Unntak = Unntak(
        Verdi("UnntakFraForutgaendeMedlemskap", false),
        Verdi("UnntakFraForutgaendeMedlemskap type", UnntakEnum.FLYKT_ALDER)
    ),
    val unntakFraForutgaendeTT: Unntak = Unntak(
        Verdi("unntakFraForutgaendeTT", false),
        Verdi("unntakFraForutgaendeTT type", UnntakEnum.FLYKT_ALDER)
    )
)
