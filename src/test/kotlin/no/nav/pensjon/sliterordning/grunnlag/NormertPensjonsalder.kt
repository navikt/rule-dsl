package no.nav.pensjon.sliterordning.grunnlag

import java.time.YearMonth

class NormertPensjonsalder(
    val øvreÅr: Int,
    val øvreMnd: Int,
    val normertÅr: Int,
    val normertMnd: Int,
    val nedreÅr: Int,
    val nedreMnd: Int,
) {

    fun øvre(fødselsDato: YearMonth): YearMonth = pensjonsDatoMedOffset(fødselsDato, øvreÅr, øvreMnd)
    fun normert(fødselsDato: YearMonth): YearMonth =
        pensjonsDatoMedOffset(fødselsDato, normertÅr, normertMnd)
    fun nedre(fødselsDato: YearMonth): YearMonth = pensjonsDatoMedOffset(fødselsDato, nedreÅr, nedreMnd)

    private fun pensjonsDatoMedOffset(fødselsDato: YearMonth, offsetÅr: Int, offsetMnd: Int): YearMonth = fødselsDato
        .plusYears(offsetÅr.toLong())
        .plusMonths(offsetMnd.toLong() + 1)

}