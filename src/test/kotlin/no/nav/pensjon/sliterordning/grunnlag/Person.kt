package no.nav.pensjon.sliterordning.grunnlag

import java.time.YearMonth

data class Person(val fødselsdato: YearMonth, val trygdetid: Trygdetid, val normertPensjonsalder: NormertPensjonsalder) {
}