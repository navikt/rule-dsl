package no.nav.pensjon.regler.rdsl2.sliterordning

import no.nav.system.rdsl2.Faktum
import no.nav.system.rdsl2.Uttrykk
import no.nav.system.rdsl2.div
import no.nav.system.rdsl2.erMindreEnn
import no.nav.system.rdsl2.erStørreEllerLik
import no.nav.system.rdsl2.funksjon
import no.nav.system.rdsl2.minus
import no.nav.system.rdsl2.regel
import no.nav.system.rdsl2.times
import kotlin.math.roundToLong

private const val MND_36 = 36

/**
 * Beregner slitertillegg.
 *
 * - A og B fastsetter justeringsfaktoren ut fra hvor lenge etter nedre pensjonsdato uttaket
 *   skjer. Betingelsene utelukker hverandre.
 * - C bruker justeringsfaktoren A eller B fastsatte.
 *
 * @param antallMnd måneder fra nedre pensjonsdato til uttak
 * @param trygdetid faktisk trygdetid i år
 * @param G grunnbeløpet
 */
fun beregnSlitertillegg(
    antallMnd: Faktum<Int>,
    trygdetid: Faktum<Int>,
    G: Faktum<Int>,
): Faktum<Double> {
    val fullTrygdetid = Faktum("fullTrygdetid", 40)
    val fulltSlitertillegg = avrund2(G * 0.25 / 12)
    lateinit var justeringsFaktor: Faktum<Double>

    // A
    regel("UTTAK-TIDLIG") {
        HVIS { antallMnd erMindreEnn MND_36 }
        SÅ {
            justeringsFaktor = faktum("justeringsFaktor", (MND_36 - antallMnd) / MND_36)
        }
    }

    // B – utelukker A
    regel("UTTAK-SENT") {
        HVIS { antallMnd erStørreEllerLik MND_36 }
        SÅ {
            justeringsFaktor = faktum("justeringsFaktor", 0.0)
        }
    }

    // C – bruker det A eller B produserte
    lateinit var slitertillegg: Faktum<Double>
    regel("AVKORTING-TRYGDETID") {
        SÅ {
            slitertillegg = faktum(
                "slitertillegg",
                avrund2(fulltSlitertillegg * justeringsFaktor * (trygdetid / fullTrygdetid))
            )
        }
    }
    return slitertillegg
}

/** Avrunder til to desimaler (half-up). */
fun avrund2(x: Uttrykk<Number>): Uttrykk<Double> =
    funksjon("avrund2", x) { (x.verdi.toDouble() * 100).roundToLong() / 100.0 }
