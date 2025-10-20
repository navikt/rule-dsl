package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.grunnlag.NormertPensjonsalder
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.grunnlag.Trygdetid
import no.nav.pensjon.sliterordning.resultat.SlitertilleggVårVersjon
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.YearMonth

class BeregnSlitertilleggRSVårVersjonTest {

    private fun person(fodselsdato: YearMonth, trygdetidMnd: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidMnd), NormertPensjonsalder(0,0,0,0,62,0))

    @Test
    fun `beregning ved uttak på nedre aldersgrense - full faktor og 50pct trygdetid`() {
        val virkningstidspunkt = YearMonth.of(2002,2)
        val person = person(YearMonth.of(1940,1), 20)
        val rs = BeregnSlitertilleggRSVårVersjon(virkningstidspunkt, person)
        val resultat = rs.test()

        val fullt = 0.25 * 110000 / 12
        val forventet = fullt * 1.0 * (20.0 / FULL_TRYGDETID)
        assertEquals(forventet, resultat.slitertilleggBeregnet, 1e-9)
    }

    @Test
    fun `beregning 35 mnd etter nedre aldersgrense - faktor 1_36`() {
        val virkningstidspunkt = YearMonth.of(2005,1) // 35 måneder etter
        val person = person(YearMonth.of(1940,1), 40)
        val rs = BeregnSlitertilleggRSVårVersjon(virkningstidspunkt, person)
        val resultat = rs.test()

        val fullt = 0.25 * 110000 / 12
        val faktorMnd = (36.0 - 35.0) / 36.0
        assertEquals(fullt * faktorMnd, resultat.slitertilleggBeregnet, 1e-9)
    }

    @Test
    fun `beregning 36 mnd etter nedre aldersgrense - faktor 0 og beregnet 0`() {
        val virkningstidspunkt = YearMonth.of(2005,2) // 36 måneder etter
        val person = person(YearMonth.of(1940,1), 40)
        val rs = BeregnSlitertilleggRSVårVersjon(virkningstidspunkt, person)
        val resultat = rs.test()

        assertEquals(0.0, resultat.slitertilleggBeregnet, 1e-9)
    }

    @Test
    fun `beregning 50 mnd etter nedre aldersgrense - faktor 0 og beregnet 0`() {
        val virkningstidspunkt = YearMonth.of(2006,4) // 50 måneder etter
        val person = person(YearMonth.of(1940,1), 40)
        val rs = BeregnSlitertilleggRSVårVersjon(virkningstidspunkt, person)
        val resultat = rs.test()

        assertEquals(0.0, resultat.slitertilleggBeregnet, 1e-9)
    }

    @Test
    fun `trygdetid 0 gir beregnet 0 selv om tidsfaktor 1`() {
        val virkningstidspunkt = YearMonth.of(2002,2)
        val person = person(YearMonth.of(1940,1), 0)
        val rs = BeregnSlitertilleggRSVårVersjon(virkningstidspunkt, person)
        val resultat = rs.test()

        assertEquals(0.0, resultat.slitertilleggBeregnet, 1e-9)
    }

    @Test
    fun `alle regler evaluerer og har fired`() {
        val rs = BeregnSlitertilleggRSVårVersjon(YearMonth.of(2002,2), person(YearMonth.of(1940,1), 40))
        rs.test()
        assertTrue(rs.children.filterIsInstance<no.nav.system.rule.dsl.Rule<SlitertilleggVårVersjon>>().all { it.evaluated })
        assertTrue(rs.children.filterIsInstance<no.nav.system.rule.dsl.Rule<SlitertilleggVårVersjon>>().all { it.fired() })
    }
}
