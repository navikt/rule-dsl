package no.nav.pensjon.regler.sliterordning.domain

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

    companion object {
        fun default() = NormertPensjonsalder(
            øvreÅr = 75, øvreMnd = 0,
            normertÅr = 67, normertMnd = 0,
            nedreÅr = 62, nedreMnd = 0
        )
    }
}