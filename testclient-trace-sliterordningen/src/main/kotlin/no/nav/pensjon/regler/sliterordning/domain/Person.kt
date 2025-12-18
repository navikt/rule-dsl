package no.nav.pensjon.regler.sliterordning.domain

import java.time.YearMonth

data class Person(val fødselsdato: YearMonth, val trygdetid: Trygdetid, val normertPensjonsalder: NormertPensjonsalder) {
    fun nedrePensjonsDato() = normertPensjonsalder.nedre(fødselsdato)
}
