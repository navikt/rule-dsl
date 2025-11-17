package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.grunnlag.NormertPensjonsalder
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.grunnlag.Trygdetid
import no.nav.pensjon.sliterordning.resultat.SlitertilleggFaktum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.YearMonth

class BeregnSlitertilleggRSVårFaktumVersjonTest {

    private fun person(fodselsdato: YearMonth, trygdetidMnd: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidMnd), NormertPensjonsalder.default())

    @Test
    fun `beregning ved uttak på nedre aldersgrense - full faktor og 50pct trygdetid`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 20)
        val grunnbeløp = 110000
        val rs = BeregnSlitertilleggRSVårFaktumVersjon(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person,
            grunnbeløp = grunnbeløp
        )
        val resultat = rs.test()

        val fullt = 0.25 * grunnbeløp / 12
        val forventet = fullt * 1.0 * (20.0 / FULL_TRYGDETID)
        assertEquals(forventet, resultat.slitertilleggBeregnet.verdi, 1e-9)
    }

    @Test
    fun `beregning 35 mnd etter nedre aldersgrense - faktor 1_36`() {
        val virkningstidspunkt = YearMonth.of(2005, 1) // 35 måneder etter
        val person = person(YearMonth.of(1940, 1), 40)
        val grunnbeløp = 110000
        val rs = BeregnSlitertilleggRSVårFaktumVersjon(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person,
            grunnbeløp = grunnbeløp
        )
        val resultat = rs.test()

        val fullt = 0.25 * grunnbeløp / 12
        val faktorMnd = (36.0 - 35.0) / 36.0
        val faktorTrygdetid = 40.0 / FULL_TRYGDETID
        assertEquals(fullt * faktorMnd * faktorTrygdetid, resultat.slitertilleggBeregnet.verdi, 1e-9)
    }

    @Test
    fun `beregning 36 mnd etter nedre aldersgrense - faktor 0 og beregnet 0`() {
        val virkningstidspunkt = YearMonth.of(2005, 2) // 36 måneder etter
        val person = person(YearMonth.of(1940, 1), 40)
        val grunnbeløp = 110000
        val rs = BeregnSlitertilleggRSVårFaktumVersjon(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person,
            grunnbeløp = grunnbeløp
        )
        val resultat = rs.test()

        assertEquals(0.0, resultat.slitertilleggBeregnet.verdi, 1e-9)
    }

    @Test
    fun `beregning 50 mnd etter nedre aldersgrense - faktor 0 og beregnet 0`() {
        val virkningstidspunkt = YearMonth.of(2006, 4) // 50 måneder etter
        val person = person(YearMonth.of(1940, 1), 40)
        val grunnbeløp = 110000
        val rs = BeregnSlitertilleggRSVårFaktumVersjon(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person,
            grunnbeløp = grunnbeløp
        )
        val resultat = rs.test()

        assertEquals(0.0, resultat.slitertilleggBeregnet.verdi, 1e-9)
    }

    @Test
    fun `trygdetid 0 gir beregnet 0 selv om tidsfaktor 1`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 0)
        val grunnbeløp = 110000
        val rs = BeregnSlitertilleggRSVårFaktumVersjon(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person,
            grunnbeløp = grunnbeløp
        )
        val resultat = rs.test()

        assertEquals(0.0, resultat.slitertilleggBeregnet.verdi, 1e-9)
    }

    @Test
    fun `beregning med forskjellige grunnbeløp gir proporsjonale resultater`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 40)

        val rs1 = BeregnSlitertilleggRSVårFaktumVersjon(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person,
            grunnbeløp = 100000
        )
        val resultat1 = rs1.test()

        val rs2 = BeregnSlitertilleggRSVårFaktumVersjon(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person,
            grunnbeløp = 200000
        )
        val resultat2 = rs2.test()

        // Resultat2 should be exactly double resultat1
        assertEquals(resultat1.slitertilleggBeregnet.verdi * 2, resultat2.slitertilleggBeregnet.verdi, 1e-9)
    }

    @Test
    fun `alle regler evaluerer og har fired`() {
        val rs = BeregnSlitertilleggRSVårFaktumVersjon(
            uttakstidspunkt = YearMonth.of(2002, 2),
            virkningstidspunkt = YearMonth.of(2002, 2),
            person = person(YearMonth.of(1940, 1), 40),
            grunnbeløp = 110000
        )
        rs.test()
        assertTrue(rs.children.filterIsInstance<no.nav.system.rule.dsl.Rule<SlitertilleggFaktum>>().all { it.evaluated })
        assertTrue(rs.children.filterIsInstance<no.nav.system.rule.dsl.Rule<SlitertilleggFaktum>>().all { it.fired() })
    }
}
