package no.nav.pensjon.sliterordning.resultat

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