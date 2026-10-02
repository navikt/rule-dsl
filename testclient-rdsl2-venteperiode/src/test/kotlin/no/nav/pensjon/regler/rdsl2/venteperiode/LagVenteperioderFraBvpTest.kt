package no.nav.pensjon.regler.rdsl2.venteperiode

import no.nav.pensjon.regler.rdsl2.venteperiode.PeriodeSluttEnum.TIL_AFP
import no.nav.pensjon.regler.rdsl2.venteperiode.PeriodeStartEnum.FRA_AFP
import no.nav.pensjon.regler.rdsl2.venteperiode.VenteperiodeBegrunnelseEnum.FØRSTEGANGSBEHANDLING
import no.nav.pensjon.regler.rdsl2.venteperiode.VenteperiodeBegrunnelseEnum.GRADSØKNING
import no.nav.system.rdsl2.forklar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.time.LocalDate

class LagVenteperioderFraBvpTest {

    private val jan = bvp("2020-01-01", "2020-01-31", 50)
    private val feb = bvp("2020-02-01", "2020-02-29", 50)

    @Test
    fun `første BVP gir førstegangsbehandling`() {
        val vp = lagVenteperioderFraBvp(listOf(jan)).getValue(jan)

        assertEquals(Venteperiode(jan.fomDato, FØRSTEGANGSBEHANDLING), vp.verdi)
        assertEquals("FØRSTE-BVP", vp.regel?.navn)
    }

    @Test
    fun `sammenhengende BVP uten gradsøkning viderefører forrige venteperiode`() {
        val resultat = lagVenteperioderFraBvp(listOf(feb, jan))

        assertSame(resultat.getValue(jan), resultat.getValue(feb))
    }

    @Test
    fun `opphold uten AFP gir opphør og ny førstegangsbehandling`() {
        val mar = bvp("2020-03-15", "2020-03-31", 50)
        val vp = lagVenteperioderFraBvp(listOf(jan, mar)).getValue(mar)
        println(vp.forklar())

        assertEquals(Venteperiode(mar.fomDato, FØRSTEGANGSBEHANDLING), vp.verdi)
        assertEquals("OPPHØR", vp.regel?.navn)
        assertEquals("fomDato > 2020-02-01 og ikke(AFP-MELLOM)", vp.regel?.uttrykk?.notasjon())
    }

    @Test
    fun `opphold med AFP mellom gir videreføring`() {
        val førAfp = bvp("2020-01-01", "2020-01-31", 50, slutt = TIL_AFP)
        val etterAfp = bvp("2020-06-01", "2020-06-30", 50, start = FRA_AFP)
        val resultat = lagVenteperioderFraBvp(listOf(førAfp, etterAfp))

        assertSame(resultat.getValue(førAfp), resultat.getValue(etterAfp))
    }

    @Test
    fun `økt uføregrad gir gradsøkning`() {
        val økt = bvp("2020-02-01", "2020-02-29", 70)
        val vp = lagVenteperioderFraBvp(listOf(jan, økt)).getValue(økt)
        println(vp.forklar())

        assertEquals(Venteperiode(økt.fomDato, GRADSØKNING), vp.verdi)
        assertEquals("GRADSØKNING", vp.regel?.navn)
        assertEquals("70 > 50 og ikke(nei)", vp.regel?.uttrykk?.konkret())
    }

    @Test
    fun `økt uføregrad uten vurdering av restarbeidsevne er ikke gradsøkning`() {
        val økt = bvp("2020-02-01", "2020-02-29", 70, utenVurdering = true)
        val resultat = lagVenteperioderFraBvp(listOf(jan, økt))

        assertSame(resultat.getValue(jan), resultat.getValue(økt))
    }

    @Test
    fun `videreføring gir samme faktum som venteperioden ble fastsatt i`() {
        val mar = bvp("2020-03-01", "2020-03-31", 70)
        val apr = bvp("2020-04-01", "2020-04-30", 70)
        val resultat = lagVenteperioderFraBvp(listOf(jan, feb, mar, apr))

        assertSame(resultat.getValue(mar), resultat.getValue(apr))
        assertEquals("GRADSØKNING", resultat.getValue(apr).regel?.navn)
    }

    @Test
    fun `gir samme resultat som opprinnelig implementasjon`() {
        val scenarier = listOf(
            listOf(jan),
            listOf(feb, jan),
            listOf(jan, bvp("2020-03-15", "2020-03-31", 50)),
            listOf(bvp("2020-01-01", "2020-01-31", 50, slutt = TIL_AFP), bvp("2020-06-01", "2020-06-30", 50, start = FRA_AFP)),
            listOf(bvp("2020-01-01", "2020-01-31", 50, slutt = TIL_AFP), bvp("2020-06-01", "2020-06-30", 80, start = FRA_AFP)),
            listOf(jan, bvp("2020-02-01", "2020-02-29", 70)),
            listOf(jan, bvp("2020-02-01", "2020-02-29", 70, utenVurdering = true)),
            listOf(jan, bvp("2020-02-01", "2020-02-29", 30)),
            listOf(jan, feb, bvp("2020-03-01", "2020-03-31", 70), bvp("2020-04-01", "2020-04-30", 70), bvp("2020-08-01", null, 70)),
        )

        scenarier.forEach { liste ->
            assertEquals(
                lagVenteperioderFraBvpOriginal(liste),
                lagVenteperioderFraBvp(liste).mapValues { it.value.verdi },
            )
        }
    }

    private fun bvp(
        fom: String,
        tom: String?,
        grad: Int,
        utenVurdering: Boolean = false,
        start: PeriodeStartEnum = PeriodeStartEnum.ANNET,
        slutt: PeriodeSluttEnum = PeriodeSluttEnum.ANNET,
    ) = BeregningsvilkarPeriode(
        fomDato = LocalDate.parse(fom),
        tomDato = tom?.let(LocalDate::parse),
        uforegrad = Uforegrad(grad, utenVurdering),
        periodeStartEnum = start,
        periodeSluttEnum = slutt,
    )
}
