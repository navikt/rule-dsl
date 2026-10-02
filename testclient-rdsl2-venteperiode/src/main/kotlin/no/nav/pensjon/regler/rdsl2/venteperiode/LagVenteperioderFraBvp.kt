package no.nav.pensjon.regler.rdsl2.venteperiode

import no.nav.pensjon.regler.rdsl2.venteperiode.PeriodeSluttEnum.TIL_AFP
import no.nav.pensjon.regler.rdsl2.venteperiode.PeriodeStartEnum.FRA_AFP
import no.nav.pensjon.regler.rdsl2.venteperiode.VenteperiodeBegrunnelseEnum.FØRSTEGANGSBEHANDLING
import no.nav.pensjon.regler.rdsl2.venteperiode.VenteperiodeBegrunnelseEnum.GRADSØKNING
import no.nav.system.rdsl2.Faktum
import no.nav.system.rdsl2.erLik
import no.nav.system.rdsl2.erStørreEnn
import no.nav.system.rdsl2.not
import no.nav.system.rdsl2.regel

/**
 * Fastsetter venteperiode for hver beregningsvilkårsperiode (BVP).
 *
 * Samme struktur som den opprinnelige funksjonen. Bare betingelsene i `when` er byttet ut med regler.
 */
fun lagVenteperioderFraBvp(bvpListe: List<BeregningsvilkarPeriode>): Map<BeregningsvilkarPeriode, Faktum<Venteperiode>> {
    val resultat = mutableListOf<Pair<BeregningsvilkarPeriode, Faktum<Venteperiode>>>()
    val erAFPmellomToBvper: (BeregningsvilkarPeriode, BeregningsvilkarPeriode) -> Faktum<Boolean> = { forrigeBvp, aktuellBvp ->
        regel("AFP-MELLOM") {
            HVIS { Faktum("forrigePeriodeSlutt", forrigeBvp.periodeSluttEnum) erLik TIL_AFP }
            OG { Faktum("periodeStart", aktuellBvp.periodeStartEnum) erLik FRA_AFP }
        }
    }

    for (bvp in bvpListe.sortedBy { it.fomDato }) {
        val forrige = resultat.lastOrNull()
        lateinit var venteperiode: Faktum<Venteperiode>
        resultat += bvp to when {
            regel("FØRSTE-BVP") {
                HVIS { forrige == null }
                SÅ { venteperiode = faktum("venteperiode", Venteperiode(bvp.fomDato, FØRSTEGANGSBEHANDLING)) }
            }.verdi -> venteperiode

            regel("OPPHØR") {
                HVIS { Faktum("fomDato", bvp.fomDato) erStørreEnn forrige!!.first.tomDato!!.plusDays(1) }
                OG { !erAFPmellomToBvper(forrige!!.first, bvp) }
                SÅ { venteperiode = faktum("venteperiode", Venteperiode(bvp.fomDato, FØRSTEGANGSBEHANDLING)) }
            }.verdi -> venteperiode

            regel("GRADSØKNING") {
                HVIS { Faktum("uføregrad", bvp.uforegrad!!.uforegrad) erStørreEnn forrige!!.first.uforegrad!!.uforegrad }
                OG { !Faktum("øktUføregradUtenVurderingAvRestarbeidsevne", bvp.uforegrad!!.øktUføregradUtenVurderingAvRestarbeidsevne) }
                SÅ { venteperiode = faktum("venteperiode", Venteperiode(bvp.fomDato, GRADSØKNING)) }
            }.verdi -> venteperiode

            else -> forrige!!.second
        }
    }

    return resultat.toMap()
}
