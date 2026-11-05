package no.nav.system.prototype.regelsett.eksempel

import no.nav.system.prototype.regelsett.Faktum
import no.nav.system.prototype.regelsett.div
import no.nav.system.prototype.regelsett.erMindreEnn
import no.nav.system.prototype.regelsett.erStørreEllerLik
import no.nav.system.prototype.regelsett.erStørreEnn
import no.nav.system.prototype.regelsett.minus
import no.nav.system.prototype.regelsett.regelsett
import no.nav.system.prototype.regelsett.times

// ---------- Eksempel 1: regelsett som returnerer ett faktum ----------

/** A og B utelukker hverandre. Første regel som returnerer, avslutter regelsettet. */
fun justeringsFaktor(antallMnd: Faktum<Int>): Faktum<Double> = regelsett("justeringsFaktor") {
    regel("UTTAK-TIDLIG") {
        HVIS { antallMnd erMindreEnn 36 }
        SÅ { RETURNER(Faktum("justeringsFaktor", (36 - antallMnd) / 36)) }
    }
    regel("UTTAK-SENT") {
        HVIS { antallMnd erStørreEllerLik 36 }
        SÅ { RETURNER(Faktum("justeringsFaktor", 0.0)) }
    }
}

/** C: beregning uten betingelser. Faktum laget utenfor en regel får DefaultRegel. */
fun beregnSlitertillegg(antallMnd: Faktum<Int>, trygdetid: Faktum<Int>, G: Faktum<Int>): Faktum<Double> {
    val fullTrygdetid = Faktum("fullTrygdetid", 40)
    val justeringsFaktor = justeringsFaktor(antallMnd)
    return Faktum("slitertillegg", G * 0.25 / 12 * justeringsFaktor * (trygdetid / fullTrygdetid))
}

// ---------- Eksempel 2: regelsett som returnerer flere faktum ----------

fun mndIgjenOgTidlig(antallMnd: Faktum<Int>): Pair<Faktum<Double>, Faktum<Boolean>> = regelsett("mndIgjenOgTidlig") {
    regel("TIDLIG") {
        HVIS { antallMnd erMindreEnn 36 }
        SÅ { RETURNER(Faktum("mndIgjen", 36 - antallMnd) to Faktum("tidlig", true)) }
    }
}

// ---------- Eksempel 3: regelsett<Unit> som endrer et objekt ----------

class Vedtak(val antallMnd: Faktum<Int>, val G: Faktum<Int>) {
    var justeringsFaktor: Faktum<Double>? = null
    var reduksjon: Faktum<Double>? = null
    var merknad: Faktum<String>? = null
}

/**
 * Viser de tre måtene to regler kan henge sammen på. En regel som ikke brukes av andre
 * (UAVHENGIG), kommer ikke med i noen forklaring.
 */
fun fastsett(v: Vedtak) = regelsett<Unit>("Fastsett") {
    // A: lager justeringsFaktor
    val uttakTidlig = regel("UTTAK-TIDLIG") {
        HVIS { v.antallMnd erMindreEnn 36 }
        SÅ { v.justeringsFaktor = Faktum("justeringsFaktor", (36 - v.antallMnd) / 36) }
    }

    // Ingen kobling til A
    regel("UAVHENGIG") {
        HVIS { v.G erStørreEnn 100000 }
        SÅ { val _ = Faktum("ubrukt", true) }
    }

    // 1) bruker faktum fra A i betingelsen
    // 2) bruker faktum fra A i utregningen
    regel("REDUKSJON") {
        HVIS { v.justeringsFaktor!! erStørreEnn 0.5 }
        SÅ { v.reduksjon = Faktum("reduksjon", v.G * v.justeringsFaktor!!) }
    }

    // 3) bruker regelen A (Faktum<Boolean>) i betingelsen
    regel("SVÆRT-TIDLIG") {
        HVIS { uttakTidlig }
        OG { v.antallMnd erMindreEnn 24 }
        SÅ { v.merknad = Faktum("merknad", "svært tidlig uttak") }
    }
}
