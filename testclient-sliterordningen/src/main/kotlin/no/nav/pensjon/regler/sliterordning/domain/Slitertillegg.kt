package no.nav.pensjon.regler.sliterordning.domain

import no.nav.system.ruledsl.core.rettsregel.Faktum

data class Slitertillegg(
    val grunnbeløp: Int,
    val antallMånederEtterNedreAldersgrense: Long,
    var fulltSlitertillegg: Double,
    var justertSlitertillegg: Double,
    var avkortetSlitertilleggEtterTrygdetid: Double,
    val slitertilleggBeregnet: Double
)

data class SlitertilleggVårVersjon(
    val grunnbeløp: Int,
    val antallMånederEtterNedreAldersgrense: Long,
    val slitertilleggBeregnet: Double
)

data class SlitertilleggFaktum(
    val slitertilleggBeregnet: Faktum<Double>
)