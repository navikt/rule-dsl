package no.nav.pensjon.regler.rdsl2.venteperiode

/**
 * Opprinnelig implementasjon uten sporing. Brukes som fasit for den sporede versjonen.
 */
fun lagVenteperioderFraBvpOriginal(bvpListe: List<BeregningsvilkarPeriode>): Map<BeregningsvilkarPeriode, Venteperiode> {
    val resultat = mutableListOf<Pair<BeregningsvilkarPeriode, Venteperiode>>()
    val erAFPmellomToBvper: (BeregningsvilkarPeriode, BeregningsvilkarPeriode) -> Boolean = { forrigeBvp, aktuellBvp ->
        forrigeBvp.periodeSluttEnum == PeriodeSluttEnum.TIL_AFP && aktuellBvp.periodeStartEnum == PeriodeStartEnum.FRA_AFP
    }

    for (bvp in bvpListe.sortedBy { it.fomDato }) {
        val forrige = resultat.lastOrNull()
        resultat += bvp to when {
            forrige == null ->
                Venteperiode(bvp.fomDato, VenteperiodeBegrunnelseEnum.FØRSTEGANGSBEHANDLING)

            bvp.fomDato != forrige.first.tomDato?.plusDays(1) && !erAFPmellomToBvper(forrige.first, bvp) ->
                Venteperiode(bvp.fomDato, VenteperiodeBegrunnelseEnum.FØRSTEGANGSBEHANDLING)

            bvp.uforegrad!!.uforegrad > forrige.first.uforegrad!!.uforegrad && !bvp.uforegrad!!.øktUføregradUtenVurderingAvRestarbeidsevne ->
                Venteperiode(bvp.fomDato, VenteperiodeBegrunnelseEnum.GRADSØKNING)

            else -> forrige.second
        }
    }

    return resultat.toMap()
}
