package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.grunnlag.NormertPensjonsalder
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.grunnlag.Trygdetid
import no.nav.pensjon.sliterordning.resultat.Slitertillegg
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.YearMonth

class BeregnSlitertilleggRSTest {

    private fun person(fodselsdato: YearMonth, trygdetidÅr: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidÅr), NormertPensjonsalder(0,0,0,0,62,0))

    @Test
    fun `beregning ved uttak på nedre aldersgrense - justeringsfaktor 1 og trygdetid 50pct`() {
        val virkningstidspunkt = YearMonth.of(2002,2) // nedre pensjonsdato
        val person = person(YearMonth.of(1940,1), 20) // 20 av 40 = 50 %
        val rs = BeregnSlitertilleggRS(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val resultat = rs.test()

        val forventetFullt = 0.25 * 110000 / 12
        val forventetJustert = forventetFullt
        val forventetAvkortetTrygdetid = forventetFullt * (20.0 / FULL_TRYGDETID)
        val forventetBeregnet = forventetFullt * 1.0 * (20.0 / FULL_TRYGDETID)

        assertEquals(110000, resultat.grunnbeløp)
        assertEquals(0, resultat.antallMånederEtterNedreAldersgrense)
        assertEquals(forventetFullt, resultat.fulltSlitertillegg, 1e-9)
        assertEquals(forventetJustert, resultat.justertSlitertillegg, 1e-9)
        assertEquals(forventetAvkortetTrygdetid, resultat.avkortetSlitertilleggEtterTrygdetid, 1e-9)
        assertEquals(forventetBeregnet, resultat.slitertilleggBeregnet, 1e-9)

        // Alle regler skal ha evaluert og fired
        assertTrue(rs.children.filterIsInstance<no.nav.system.rule.dsl.Rule<Slitertillegg>>().all { it.evaluated })
        assertTrue(rs.children.filterIsInstance<no.nav.system.rule.dsl.Rule<Slitertillegg>>().all { it.fired() })
    }

    @Test
    fun `beregning 35 mnd etter nedre aldersgrense - minimal justeringfaktor 1_36`() {
        val virkningstidspunkt = YearMonth.of(2005,1) // 35 måneder etter 2002-02
        val person = person(YearMonth.of(1940,1), 40) // full trygdetid
        val rs = BeregnSlitertilleggRS(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val resultat = rs.test()

        val fullt = 0.25 * 110000 / 12
        val faktorMnd = (36.0 - 35.0) / 36.0
        assertEquals(35, resultat.antallMånederEtterNedreAldersgrense)
        assertEquals(fullt * faktorMnd, resultat.justertSlitertillegg, 1e-9)
        assertEquals(fullt, resultat.avkortetSlitertilleggEtterTrygdetid, 1e-9)
        assertEquals(fullt * faktorMnd, resultat.slitertilleggBeregnet, 1e-9)
    }

    @Test
    fun `beregning 36 mnd etter nedre aldersgrense - ingen justering og beregnet 0`() {
        val virkningstidspunkt = YearMonth.of(2005,2) // 36 måneder etter
        val person = person(YearMonth.of(1940,1), 40)
        val rs = BeregnSlitertilleggRS(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val resultat = rs.test()

        val fullt = 0.25 * 110000 / 12
        assertEquals(36, resultat.antallMånederEtterNedreAldersgrense)
        assertEquals(0.0, resultat.justertSlitertillegg, 1e-9)
        // Avkortet trygdetid fortsatt fullt (reglen uavhengig av måneder)
        assertEquals(fullt, resultat.avkortetSlitertilleggEtterTrygdetid, 1e-9)
        assertEquals(0.0, resultat.slitertilleggBeregnet, 1e-9)
    }

    @Test
    fun `beregning 50 mnd etter nedre aldersgrense - justering 0 men beregnet blir negativt pga formel`() {
        val virkningstidspunkt = YearMonth.of(2006,4) // 50 måneder etter 2002-02
        val person = person(YearMonth.of(1940,1), 40)
        val rs = BeregnSlitertilleggRS(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val resultat = rs.test() as Slitertillegg

        val fullt = 0.25 * 110000 / 12
        val forventetNegativ = fullt * ((36.0 - 50.0) / 36.0) * (40.0 / FULL_TRYGDETID) // viser potensielt avvik/bug

        assertEquals(50, resultat.antallMånederEtterNedreAldersgrense)
        assertEquals(0.0, resultat.justertSlitertillegg, 1e-9)
        assertEquals(forventetNegativ, resultat.slitertilleggBeregnet, 1e-9)
        assertTrue(resultat.slitertilleggBeregnet < 0, "Forventet negativt beregnet beløp ved > 36 mnd i dagens implementasjon")
    }

    @Test
    fun `trygdetid 0 gir avkortet og beregnet 0 selv om justering 1`() {
        val virkningstidspunkt = YearMonth.of(2002,2)
        val person = person(YearMonth.of(1940,1), 0)
        val rs = BeregnSlitertilleggRS(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val resultat = rs.test() as Slitertillegg

        val fullt = 0.25 * 110000 / 12
        assertEquals(fullt, resultat.fulltSlitertillegg, 1e-9)
        assertEquals(fullt, resultat.justertSlitertillegg, 1e-9)
        assertEquals(0.0, resultat.avkortetSlitertilleggEtterTrygdetid, 1e-9)
        assertEquals(0.0, resultat.slitertilleggBeregnet, 1e-9)
    }
}
