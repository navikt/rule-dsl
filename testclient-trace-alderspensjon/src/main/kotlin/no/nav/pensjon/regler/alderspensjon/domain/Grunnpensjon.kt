package no.nav.pensjon.regler.alderspensjon.domain

data class Grunnpensjon(
    val grunnbeløp: Int,
    val prosentsats: Double,
    val netto: Int
)
