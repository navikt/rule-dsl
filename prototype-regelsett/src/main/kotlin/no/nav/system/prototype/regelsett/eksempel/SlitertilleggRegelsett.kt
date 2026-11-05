package no.nav.system.prototype.regelsett.eksempel

import no.nav.system.prototype.regelsett.*

/**
 * Beregner slitertillegg (forenklet). Viser hele virkemåten til et regelsett:
 *
 * - tidlig retur som stopper resten av regelsettet (MANGLER-TRYGDETID)
 * - regel uten betingelse (FULLT-SLITERTILLEGG)
 * - regler som skriver til lokale variabler (UTTAK-*, *-TRYGDETID)
 * - regel brukt som betingelse i en senere regel (UTTAK-SENT, FULL-TRYGDETID)
 * - senere regler som leser de lokale variablene og returnerer (GRADERT-UTTAK, HELT-UTTAK)
 *
 * @param antallMnd måneder fra nedre pensjonsdato til uttak
 * @param trygdetid faktisk trygdetid i år
 * @param G grunnbeløpet
 */
fun beregnSlitertilleggRS(
    antallMnd: Faktum<Int>,
    trygdetid: Faktum<Int>?,
    G: Faktum<Int>,
): Faktum<Double> = regelsett("beregnSlitertilleggRS") {

    // Faktum laget utenfor SÅ får DefaultRegel
    val fullTrygdetid = Faktum("fullTrygdetid", 40)

    /**
     * Bakoverkompatibel teknikk:
     */
    var justeringsFaktor: Faktum<Double> = Faktum("justeringsFaktor", 0.0) // evt. bruk lateinit
    regel("UTTAK-TIDLIG") {
        HVIS { antallMnd erMindreEnn 36 }
        SÅ { justeringsFaktor = Faktum("justeringsFaktor", (36 - antallMnd) / 36) }
    }
    regel("UTTAK-SENT") {
        HVIS { antallMnd erStørreEllerLik 36 }
        SÅ { justeringsFaktor = Faktum("justeringsFaktor", 0.0) }
    }

    /**
     * Bakoverkompatibel teknikk, lateinit variant:
     */
    // Settes av regler under og leses av senere regler
    lateinit var fulltSlitertillegg: Faktum<Double>
    regel("FULLT-SLITERTILLEGG") {
        SÅ { fulltSlitertillegg = Faktum("fulltSlitertillegg", G * 0.25 / 12) }
    }

    val harTrygdetid = regel("harTrygdetid") {
        HVIS { trygdetid != null }
    }

    /**
     * Ny teknikk:
     */
    val trygdetidsbrøk: Faktum<Double> = regelsett("BeregnTrygdetidsbrøkRS") {
        regel("MANGLER-TRYGDETID") {
            HVIS { harTrygdetid erLik false }
            SÅ { RETURNER(Faktum("trygdetidsbrøk", 0.0)) } // avbryter videre prosessering
        }
        regel("FULL-TRYGDETID") {
            HVIS { harTrygdetid }
            OG { trygdetid!! erStørreEllerLik fullTrygdetid }
            SÅ { RETURNER(Faktum("trygdetidsbrøk", 1.0)) }
        }
        regel("REDUSERT-TRYGDETID") {
            HVIS { harTrygdetid }
            OG { trygdetid!! erMindreEnn fullTrygdetid }
            SÅ { RETURNER(Faktum("trygdetidsbrøk", trygdetid!! / fullTrygdetid)) }
        }
    }

    // Regel uten betingelse
    regel("BEREGN-SLITERTILLEGG") {
        REF("§3-2 Folketrygdloven", "https://lovdata.no")
        SÅ {
            RETURNER(
                Faktum("slitertillegg", fulltSlitertillegg * justeringsFaktor * trygdetidsbrøk)
            )
        }
    }
}
