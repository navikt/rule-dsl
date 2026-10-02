package no.nav.pensjon.regler.rdsl2.venteperiode

import java.time.LocalDate

enum class PeriodeStartEnum { FRA_AFP, ANNET }

enum class PeriodeSluttEnum { TIL_AFP, ANNET }

enum class VenteperiodeBegrunnelseEnum { FØRSTEGANGSBEHANDLING, GRADSØKNING }

data class Uforegrad(
    val uforegrad: Int,
    val øktUføregradUtenVurderingAvRestarbeidsevne: Boolean = false,
)

data class BeregningsvilkarPeriode(
    val fomDato: LocalDate,
    val tomDato: LocalDate?,
    val uforegrad: Uforegrad?,
    val periodeStartEnum: PeriodeStartEnum = PeriodeStartEnum.ANNET,
    val periodeSluttEnum: PeriodeSluttEnum = PeriodeSluttEnum.ANNET,
)

data class Venteperiode(
    val fomDato: LocalDate,
    val begrunnelse: VenteperiodeBegrunnelseEnum,
)
