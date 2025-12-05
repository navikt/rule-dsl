package no.nav.system.ruledsl.core.rettsregel

import no.nav.system.ruledsl.core.operators.erEtter
import no.nav.system.ruledsl.core.operators.erEtterEllerLik
import no.nav.system.ruledsl.core.operators.erFør
import no.nav.system.ruledsl.core.operators.erFørEllerLik
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Test for dato-sammenligningsoperatorer i Uttrykk.
 */
class UttrykkDatoTest {

    private val dato1 = LocalDate.of(2020, 1, 1)
    private val dato2 = LocalDate.of(2021, 1, 1)
    private val dato3 = LocalDate.of(2021, 1, 1) // Samme som dato2

    @Test
    fun `erEtter med Faktum2`() {
        val tidligereDato = Faktum("tidligereDato", Const(dato1))
        val senereDato = Faktum("senereDato", Const(dato2))

        val resultat = senereDato erEtter tidligereDato

        assertTrue(resultat.verdi)
        assertEquals("JA 'senereDato' er etter 'tidligereDato'", resultat.notasjon())
        assertEquals("JA '2021-01-01' er etter '2020-01-01'", resultat.konkret())
    }

    @Test
    fun `erEtter med Const og LocalDate`() {
        val dato = Faktum("dato", Const(dato2))

        val resultat = dato erEtter dato1

        assertTrue(resultat.verdi)
        assertEquals("JA 'dato' er etter '2020-01-01'", resultat.notasjon())
    }

    @Test
    fun `erEtter returnerer false når datoer er like`() {
        val dato = Faktum("dato", Const(dato2))

        val resultat = dato erEtter dato3

        assertFalse(resultat.verdi)
    }

    @Test
    fun `erEtterEllerLik med Faktum2`() {
        val dato = Faktum("dato", Const(dato2))
        val sammenligningsDato = Faktum("sammenligningsDato", Const(dato3))

        val resultat = dato erEtterEllerLik sammenligningsDato

        assertTrue(resultat.verdi)
        assertEquals("JA 'dato' er etter eller lik 'sammenligningsDato'", resultat.notasjon())
    }

    @Test
    fun `erEtterEllerLik returnerer true når datoer er like`() {
        val dato = Faktum("dato", Const(dato2))

        val resultat = dato erEtterEllerLik dato3

        assertTrue(resultat.verdi)
    }

    @Test
    fun `erEtterEllerLik returnerer true når dato er senere`() {
        val dato = Faktum("dato", Const(dato2))

        val resultat = dato erEtterEllerLik dato1

        assertTrue(resultat.verdi)
    }

    @Test
    fun `erEtterEllerLik returnerer false når dato er tidligere`() {
        val dato = Faktum("dato", Const(dato1))

        val resultat = dato erEtterEllerLik dato2

        assertFalse(resultat.verdi)
    }

    @Test
    fun `erFør med Faktum2`() {
        val tidligereDato = Faktum("tidligereDato", Const(dato1))
        val senereDato = Faktum("senereDato", Const(dato2))

        val resultat = tidligereDato erFør senereDato

        assertTrue(resultat.verdi)
        assertEquals("JA 'tidligereDato' er før 'senereDato'", resultat.notasjon())
        assertEquals("JA '2020-01-01' er før '2021-01-01'", resultat.konkret())
    }

    @Test
    fun `erFør returnerer false når datoer er like`() {
        val dato = Faktum("dato", Const(dato2))

        val resultat = dato erFør dato3

        assertFalse(resultat.verdi)
    }

    @Test
    fun `erFørEllerLik med Faktum2`() {
        val tidligereDato = Faktum("tidligereDato", Const(dato1))
        val senereDato = Faktum("senereDato", Const(dato2))

        val resultat = tidligereDato erFørEllerLik senereDato

        assertTrue(resultat.verdi)
        assertEquals("JA 'tidligereDato' er før eller lik 'senereDato'", resultat.notasjon())
    }

    @Test
    fun `erFørEllerLik returnerer true når datoer er like`() {
        val dato = Faktum("dato", Const(dato2))

        val resultat = dato erFørEllerLik dato3

        assertTrue(resultat.verdi)
    }

}
