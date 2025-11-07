package no.nav.system.rule.dsl.rettsregel.operators

import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.rettsregel.Faktum
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PredikatOperatorTest {

    companion object {
        val dato1990 = localDate(1990, 1, 1)
        val dato2000 = localDate(2000, 1, 1)
        val dato1990f = Faktum("En dato på nittitallet", dato1990)
        val dato2000f = Faktum("Y2K", dato2000)

        const val YEAR1996 = 1996

        const val TJUE = 20
        const val FEM = 5

        val flaggF = Faktum("flagg", true)

        val list = listOf("A", "B", "C")
        val Af = Faktum("A", "A")
        val Df = Faktum("D", "D")

        val SANNf = Faktum("SANN", true)
    }

    /**
     * Datoer
     */
    @Test
    fun erFørEllerLik() {
        (dato1990 erFørEllerLik dato2000).apply {
            assertTrue(evaluer())
            assertEquals("JA '1990-01-01' er før eller lik '2000-01-01'", toString())
            assertEquals("JA '1990-01-01' er før eller lik '2000-01-01'", konkret())
        }
        (dato2000 erFørEllerLik dato1990).apply {
            assertFalse(evaluer())
            assertEquals("NEI '2000-01-01' må være før eller lik '1990-01-01'", toString())
            assertEquals("NEI '2000-01-01' må være før eller lik '1990-01-01'", konkret())
        }
    }

    @Test
    fun erFørMedFaktum() {
        (dato1990f erFør dato2000f).apply {
            assertTrue(evaluer())
            assertEquals("JA 'En dato på nittitallet' (1990-01-01) er før 'Y2K' (2000-01-01)", toString())
            assertEquals("JA 'En dato på nittitallet' er før 'Y2K'", notasjon())
            assertEquals("JA '1990-01-01' er før '2000-01-01'", konkret())
        }
        (dato2000f erFør dato1990f).apply {
            assertFalse(evaluer())
            assertEquals("NEI 'Y2K' (2000-01-01) må være før 'En dato på nittitallet' (1990-01-01)", toString())
            assertEquals("NEI 'Y2K' må være før 'En dato på nittitallet'", notasjon())
            assertEquals("NEI '2000-01-01' må være før '1990-01-01'", konkret())
        }
    }

    @Test
    fun erFørUtenFaktum() {
        (dato1990 erFør dato2000).apply {
            assertTrue(evaluer())
            assertEquals("JA '1990-01-01' er før '2000-01-01'", toString())
            assertEquals("JA '1990-01-01' er før '2000-01-01'", konkret())
        }
        (dato2000 erFør dato1990).apply {
            assertFalse(evaluer())
            assertEquals("NEI '2000-01-01' må være før '1990-01-01'", toString())
            assertEquals("NEI '2000-01-01' må være før '1990-01-01'", konkret())
        }
    }

    @Test
    fun erEtterEllerLik() {
        (dato2000f erEtterEllerLik dato1990f).apply {
            assertTrue(evaluer())
            assertEquals("JA 'Y2K' (2000-01-01) er etter eller lik 'En dato på nittitallet' (1990-01-01)", toString())
            assertEquals("JA 'Y2K' er etter eller lik 'En dato på nittitallet'", notasjon())
            assertEquals("JA '2000-01-01' er etter eller lik '1990-01-01'", konkret())
        }
        (dato1990f erEtterEllerLik dato2000f).apply {
            assertFalse(evaluer())
            assertEquals("NEI 'En dato på nittitallet' (1990-01-01) må være etter eller lik 'Y2K' (2000-01-01)", toString())
            assertEquals("NEI 'En dato på nittitallet' må være etter eller lik 'Y2K'", notasjon())
            assertEquals("NEI '1990-01-01' må være etter eller lik '2000-01-01'", konkret())
        }
    }

    @Test
    fun erEtter() {
        (dato2000f erEtter dato1990f).apply {
            assertTrue(evaluer())
            assertEquals("JA 'Y2K' (2000-01-01) er etter 'En dato på nittitallet' (1990-01-01)", toString())
            assertEquals("JA 'Y2K' er etter 'En dato på nittitallet'", notasjon())
            assertEquals("JA '2000-01-01' er etter '1990-01-01'", konkret())
        }
        (dato1990f erEtter dato2000f).apply {
            assertFalse(evaluer())
            assertEquals("NEI 'En dato på nittitallet' (1990-01-01) må være etter 'Y2K' (2000-01-01)", toString())
            assertEquals("NEI 'En dato på nittitallet' må være etter 'Y2K'", notasjon())
            assertEquals("NEI '1990-01-01' må være etter '2000-01-01'", konkret())
        }
    }

    /**
     * Tall
     */
    @Test
    fun erMindreEllerLik() {
        (FEM erMindreEllerLik TJUE).apply {
            assertTrue(evaluer())
            assertEquals("JA '5' er mindre eller lik '20'", toString())
            assertEquals("JA '5' er mindre eller lik '20'", konkret())
        }
        (TJUE erMindreEllerLik FEM).apply {
            assertFalse(evaluer())
            assertEquals("NEI '20' må være mindre eller lik '5'", toString())
            assertEquals("NEI '20' må være mindre eller lik '5'", konkret())
        }
    }

    @Test
    fun erMindreEnn() {
        (FEM erMindreEnn TJUE).apply {
            assertTrue(evaluer())
            assertEquals("JA '5' er mindre enn '20'", toString())
            assertEquals("JA '5' er mindre enn '20'", konkret())
        }
        (TJUE erMindreEnn FEM).apply {
            assertFalse(evaluer())
            assertEquals("NEI '20' må være mindre enn '5'", toString())
            assertEquals("NEI '20' må være mindre enn '5'", konkret())
        }
    }

    @Test
    fun erStørreEllerLik() {
        (TJUE erStørreEllerLik FEM).apply {
            assertTrue(evaluer())
            assertEquals("JA '20' er større eller lik '5'", toString())
            assertEquals("JA '20' er større eller lik '5'", konkret())
        }
        (FEM erStørreEllerLik TJUE).apply {
            assertFalse(evaluer())
            assertEquals("NEI '5' må være større eller lik '20'", toString())
            assertEquals("NEI '5' må være større eller lik '20'", konkret())
        }
    }

    @Test
    fun erStørre() {
        (TJUE erStørreEnn FEM).apply {
            assertTrue(evaluer())
            assertEquals("JA '20' er større enn '5'", toString())
            assertEquals("JA '20' er større enn '5'", konkret())
        }
        (FEM erStørreEnn TJUE).apply {
            assertFalse(evaluer())
            assertEquals("NEI '5' må være større enn '20'", toString())
            assertEquals("NEI '5' må være større enn '20'", konkret())
        }
    }

    @Test
    fun erMindreEllerLikNumber() {
        (FEM erMindreEllerLik 20).apply {
            assertTrue(evaluer())
            assertEquals("JA '5' er mindre eller lik '20'", toString())
            assertEquals("JA '5' er mindre eller lik '20'", konkret())
        }
        (TJUE erMindreEllerLik 5).apply {
            assertFalse(evaluer())
            assertEquals("NEI '20' må være mindre eller lik '5'", toString())
            assertEquals("NEI '20' må være mindre eller lik '5'", konkret())
        }
    }

    @Test
    fun erMindreEnnNumber() {
        (FEM erMindreEnn 20).apply {
            assertTrue(evaluer())
            assertEquals("JA '5' er mindre enn '20'", toString())
            assertEquals("JA '5' er mindre enn '20'", konkret())
        }
        (TJUE erMindreEnn 5).apply {
            assertFalse(evaluer())
            assertEquals("NEI '20' må være mindre enn '5'", toString())
            assertEquals("NEI '20' må være mindre enn '5'", konkret())
        }
    }

    @Test
    fun erStørreEllerLikNumber() {
        (TJUE erStørreEllerLik 5).apply {
            assertTrue(evaluer())
            assertEquals("JA '20' er større eller lik '5'", toString())
            assertEquals("JA '20' er større eller lik '5'", konkret())
        }
        (FEM erStørreEllerLik 20).apply {
            assertFalse(evaluer())
            assertEquals("NEI '5' må være større eller lik '20'", toString())
            assertEquals("NEI '5' må være større eller lik '20'", konkret())
        }
    }

    @Test
    fun erStørreNumber() {
        (TJUE erStørreEnn 5).apply {
            assertTrue(evaluer())
            assertEquals("JA '20' er større enn '5'", toString())
            assertEquals("JA '20' er større enn '5'", konkret())
        }
        (FEM erStørreEnn 20).apply {
            assertFalse(evaluer())
            assertEquals("NEI '5' må være større enn '20'", toString())
            assertEquals("NEI '5' må være større enn '20'", konkret())
        }
    }

    /**
     * Dato > Tall
     */
    @Test
    fun erMindreEllerLikDatoOgTall() {
        (dato1990f erMindreEllerLik YEAR1996).apply {
            assertTrue(evaluer())
            assertEquals("JA 'En dato på nittitallet' (1990-01-01) er mindre eller lik '1996'", toString())
            assertEquals("JA 'En dato på nittitallet' er mindre eller lik '1996'", notasjon())
            assertEquals("JA '1990-01-01' er mindre eller lik '1996'", konkret())
        }
        (dato2000f erMindreEllerLik YEAR1996).apply {
            assertFalse(evaluer())
            assertEquals("NEI 'Y2K' (2000-01-01) må være mindre eller lik '1996'", toString())
            assertEquals("NEI 'Y2K' må være mindre eller lik '1996'", notasjon())
            assertEquals("NEI '2000-01-01' må være mindre eller lik '1996'", konkret())
        }
    }

    @Test
    fun erMindreEnnDatoOgTall() {
        (dato1990f erMindreEnn YEAR1996).apply {
            assertTrue(evaluer())
            assertEquals("JA 'En dato på nittitallet' (1990-01-01) er mindre enn '1996'", toString())
            assertEquals("JA 'En dato på nittitallet' er mindre enn '1996'", notasjon())
            assertEquals("JA '1990-01-01' er mindre enn '1996'", konkret())
        }
        (dato2000f erMindreEnn YEAR1996).apply {
            assertFalse(evaluer())
            assertEquals("NEI 'Y2K' (2000-01-01) må være mindre enn '1996'", toString())
            assertEquals("NEI 'Y2K' må være mindre enn '1996'", notasjon())
            assertEquals("NEI '2000-01-01' må være mindre enn '1996'", konkret())
        }
    }

    @Test
    fun erStørreEllerLikDatoOgTall() {
        (dato2000f erStørreEllerLik YEAR1996).apply {
            assertTrue(evaluer())
            assertEquals("JA 'Y2K' (2000-01-01) er større eller lik '1996'", toString())
            assertEquals("JA 'Y2K' er større eller lik '1996'", notasjon())
            assertEquals("JA '2000-01-01' er større eller lik '1996'", konkret())
        }
        (dato1990f erStørreEllerLik YEAR1996).apply {
            assertFalse(evaluer())
            assertEquals("NEI 'En dato på nittitallet' (1990-01-01) må være større eller lik '1996'", toString())
            assertEquals("NEI 'En dato på nittitallet' må være større eller lik '1996'", notasjon())
            assertEquals("NEI '1990-01-01' må være større eller lik '1996'", konkret())
        }
    }

    @Test
    fun erStørreEnnDatoOgTall() {
        (dato2000f erStørreEnn YEAR1996).apply {
            assertTrue(evaluer())
            assertEquals("JA 'Y2K' (2000-01-01) er større enn '1996'", toString())
            assertEquals("JA 'Y2K' er større enn '1996'", notasjon())
            assertEquals("JA '2000-01-01' er større enn '1996'", konkret())
        }
        (dato1990f erStørreEnn YEAR1996).apply {
            assertFalse(evaluer())
            assertEquals("NEI 'En dato på nittitallet' (1990-01-01) må være større enn '1996'", toString())
            assertEquals("NEI 'En dato på nittitallet' må være større enn '1996'", notasjon())
            assertEquals("NEI '1990-01-01' må være større enn '1996'", konkret())
        }
    }

    /**
     * Generisk
     */
    @Test
    fun erLik() {
        (TJUE erLik 20).apply {
            assertTrue(evaluer())
            assertEquals("JA '20' er lik '20'", toString())
            assertEquals("JA '20' er lik '20'", konkret())
        }
        (TJUE erLik 5).apply {
            assertFalse(evaluer())
            assertEquals("NEI '20' må være lik '5'", toString())
            assertEquals("NEI '20' må være lik '5'", konkret())
        }
    }

    @Test
    fun erUlik() {
        (TJUE erUlik 3).apply {
            assertTrue(evaluer())
            assertEquals("JA '20' er ulik '3'", toString())
            assertEquals("JA '20' er ulik '3'", konkret())
        }
        (TJUE erUlik 20).apply {
            assertFalse(evaluer())
            assertEquals("NEI '20' må være ulik '20'", toString())
            assertEquals("NEI '20' må være ulik '20'", konkret())
        }
    }

    @Test
    fun erLikFaktum() {
        (flaggF erLik SANNf).apply {
            assertTrue(evaluer())
            assertEquals("JA 'flagg' (true) er lik 'SANN' (true)", toString())
            assertEquals("JA 'flagg' er lik 'SANN'", notasjon())
            assertEquals("JA 'true' er lik 'true'", konkret())
        }
        (TJUE erLik Faktum("TJUE!", 20)).apply {
            assertTrue(evaluer())
            assertEquals("JA '20' er lik 'TJUE!' (20)", toString())
            assertEquals("JA '20' er lik 'TJUE!'", notasjon())
            assertEquals("JA '20' er lik '20'", konkret())
        }
        (Faktum("niks", false) erLik SANNf).apply {
            assertFalse(evaluer())
            assertEquals("NEI 'niks' (false) må være lik 'SANN' (true)", toString())
            assertEquals("NEI 'niks' må være lik 'SANN'", notasjon())
            assertEquals("NEI 'false' må være lik 'true'", konkret())
        }
        (FEM erLik TJUE).apply {
            assertFalse(evaluer())
            assertEquals("NEI '5' må være lik '20'", toString())
            assertEquals("NEI '5' må være lik '20'", konkret())
        }
    }

    @Test
    fun erUlikFaktum() {
        (flaggF erUlik Faktum("niks", false)).apply {
            assertTrue(evaluer())
            assertEquals("JA 'flagg' (true) er ulik 'niks' (false)", toString())
            assertEquals("JA 'flagg' er ulik 'niks'", notasjon())
            assertEquals("JA 'true' er ulik 'false'", konkret())
        }
        (TJUE erUlik Faktum("tre", 3)).apply {
            assertTrue(evaluer())
            assertEquals("JA '20' er ulik 'tre' (3)", toString())
            assertEquals("JA '20' er ulik 'tre'", notasjon())
            assertEquals("JA '20' er ulik '3'", konkret())
        }
        (Faktum("niks", false) erUlik Faktum("niks!!", false)).apply {
            assertFalse(evaluer())
            assertEquals("NEI 'niks' (false) må være ulik 'niks!!' (false)", toString())
            assertEquals("NEI 'niks' må være ulik 'niks!!'", notasjon())
            assertEquals("NEI 'false' må være ulik 'false'", konkret())
        }
        (FEM erUlik FEM).apply {
            assertFalse(evaluer())
            assertEquals("NEI '5' må være ulik '5'", toString())
            assertEquals("NEI '5' må være ulik '5'", konkret())
        }
    }

    @Test
    fun erBlant() {
        (Af erBlant list).apply {
            assertTrue(evaluer())
            assertEquals("JA 'A' (A) er blandt '[A, B, C]'", toString())
            assertEquals("JA 'A' er blandt '[A, B, C]'", notasjon())
            assertEquals("JA 'A' er blandt '[A, B, C]'", konkret())
        }
        (Df erBlant list).apply {
            assertFalse(evaluer())
            assertEquals("NEI 'D' (D) må være blandt '[A, B, C]'", toString())
            assertEquals("NEI 'D' må være blandt '[A, B, C]'", notasjon())
            assertEquals("NEI 'D' må være blandt '[A, B, C]'", konkret())
        }
    }

    @Test
    fun erIkkeBlant() {
        (Df erIkkeBlant list).apply {
            assertTrue(evaluer())
            assertEquals("JA 'D' (D) er ikke blandt '[A, B, C]'", toString())
            assertEquals("JA 'D' er ikke blandt '[A, B, C]'", notasjon())
            assertEquals("JA 'D' er ikke blandt '[A, B, C]'", konkret())
        }
        (Af erIkkeBlant list).apply {
            assertFalse(evaluer())
            assertEquals("NEI 'A' (A) må ikke være blandt '[A, B, C]'", toString())
            assertEquals("NEI 'A' må ikke være blandt '[A, B, C]'", notasjon())
            assertEquals("NEI 'A' må ikke være blandt '[A, B, C]'", konkret())
        }
    }

}