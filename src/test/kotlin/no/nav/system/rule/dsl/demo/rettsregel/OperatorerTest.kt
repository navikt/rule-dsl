package no.nav.system.rule.dsl.demo.rettsregel

import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.rettsregel.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OperatorerTest {

    companion object {
        val dato1990 = Faktum(localDate(1990,1,1))
        val dato2000 = Faktum(localDate(2000,1,1))

        val tjue = Faktum(20)
        val fem = Faktum(5)
    }

    @Test
    fun erFørEllerLik() {
        (dato1990 erFørEllerLik dato2000).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '1990-01-01' er før eller lik '2000-01-01'", toString())
        }
        (dato2000 erFørEllerLik dato1990).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '2000-01-01' må være før eller lik '1990-01-01'", toString())
        }
    }
    @Test
    fun erFør() {
        (dato1990 erFør dato2000).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '1990-01-01' er før '2000-01-01'", toString())
        }
        (dato2000 erFør dato1990).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '2000-01-01' må være før '1990-01-01'", toString())
        }
    }
    @Test
    fun erEtterEllerLik() {
        (dato2000 erEtterEllerLik dato1990).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '2000-01-01' er etter eller lik '1990-01-01'", toString())
        }
        (dato1990 erEtterEllerLik dato2000).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '1990-01-01' må være etter eller lik '2000-01-01'", toString())
        }
    }
    @Test
    fun erEtter() {
        (dato2000 erEtter dato1990).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '2000-01-01' er etter '1990-01-01'", toString())
        }
        (dato1990 erEtter dato2000).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '1990-01-01' må være etter '2000-01-01'", toString())
        }
    }

    @Test
    fun erMindreEllerLik() {
        (fem erMindreEllerLik tjue).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '5' er mindre eller lik '20'", toString())
        }
        (tjue erMindreEllerLik fem).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '20' må være mindre eller lik '5'", toString())
        }
    }
    @Test
    fun erMindreEnn() {
        (fem erMindreEnn tjue).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '5' er mindre enn '20'", toString())
        }
        (tjue erMindreEnn fem).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '20' må være mindre enn '5'", toString())
        }
    }



}