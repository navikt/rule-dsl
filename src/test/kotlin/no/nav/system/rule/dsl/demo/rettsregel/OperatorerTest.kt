package no.nav.system.rule.dsl.demo.rettsregel

import no.nav.system.rule.dsl.demo.domain.ForsteVirkningsdatoGrunnlag
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.rettsregel.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OperatorerTest {

    companion object {
        val dato1990 = Faktum(localDate(1990, 1, 1))
        val dato2000 = Faktum(localDate(2000, 1, 1))

        val year1996 = 1996

        val tjue = Faktum(20)
        val fem = Faktum(5)

        val flagg = Faktum("flagg", true)

        val list = listOf("A", "B", "C")
        val fvdgList = listOf(
            ForsteVirkningsdatoGrunnlag(
                virkningsdato = localDate(2000, 1, 1),
                kravlinjeType = YtelseEnum.GJP
            ),
            ForsteVirkningsdatoGrunnlag(
                virkningsdato = localDate(2010, 1, 1),
                kravlinjeType = YtelseEnum.UT
            ),
            ForsteVirkningsdatoGrunnlag(
                virkningsdato = localDate(2020, 1, 1),
                kravlinjeType = YtelseEnum.AP
            )
        )
        val A = Faktum("A", "A")
        val D = Faktum("D", "D")
    }

    /**
     * Datoer
     */
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
    fun erFørMedFaktum() {
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
    fun erFørUtenFaktum() {
        (dato1990 erFør localDate(2000, 1, 1)).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '1990-01-01' er før '2000-01-01'", toString())
        }
        (dato2000 erFør localDate(1990, 1, 1)).apply {
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

    /**
     * Tall
     */
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

    @Test
    fun erStørreEllerLik() {
        (tjue erStørreEllerLik fem).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '20' er større eller lik '5'", toString())
        }
        (fem erStørreEllerLik tjue).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '5' må være større eller lik '20'", toString())
        }
    }

    @Test
    fun erStørre() {
        (tjue erStørre fem).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '20' er større enn '5'", toString())
        }
        (fem erStørre tjue).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '5' må være større enn '20'", toString())
        }
    }

    @Test
    fun erMindreEllerLikNumber() {
        (fem erMindreEllerLik 20).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '5' er mindre eller lik '20'", toString())
        }
        (tjue erMindreEllerLik 5).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '20' må være mindre eller lik '5'", toString())
        }
    }

    @Test
    fun erMindreEnnNumber() {
        (fem erMindreEnn 20).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '5' er mindre enn '20'", toString())
        }
        (tjue erMindreEnn 5).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '20' må være mindre enn '5'", toString())
        }
    }

    @Test
    fun erStørreEllerLikNumber() {
        (tjue erStørreEllerLik 5).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '20' er større eller lik '5'", toString())
        }
        (fem erStørreEllerLik 20).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '5' må være større eller lik '20'", toString())
        }
    }

    @Test
    fun erStørreNumber() {
        (tjue erStørre 5).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '20' er større enn '5'", toString())
        }
        (fem erStørre 20).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '5' må være større enn '20'", toString())
        }
    }

    /**
     * Boolean
     */
    @Test
    fun flagg() {
        (flagg).apply {
            assertEquals("faktum: 'flagg' (true)", toString())
        }
        (!flagg).apply {
            assertEquals("faktum: 'flagg' (false)", toString())
        }

    }

    /**
     * Dato > Tall
     */
    @Test
    fun erMindreEllerLikDatoOgTall() {
        (dato1990 erMindreEllerLik year1996).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '1990-01-01' er mindre eller lik '1996'", toString())
        }
        (dato2000 erMindreEllerLik year1996).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '2000-01-01' må være mindre eller lik '1996'", toString())
        }
    }

    @Test
    fun erMindreEnnDatoOgTall() {
        (dato1990 erMindreEnn year1996).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '1990-01-01' er mindre enn '1996'", toString())
        }
        (dato2000 erMindreEnn year1996).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '2000-01-01' må være mindre enn '1996'", toString())
        }
    }

    @Test
    fun erStørreEllerLikDatoOgTall() {
        (dato2000 erStørreEllerLik year1996).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '2000-01-01' er større eller lik '1996'", toString())
        }
        (dato1990 erStørreEllerLik year1996).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '1990-01-01' må være større eller lik '1996'", toString())
        }
    }

    @Test
    fun erStørreEnnDatoOgTall() {
        (dato2000 erStørreEnn year1996).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '2000-01-01' er større enn '1996'", toString())
        }
        (dato1990 erStørreEnn year1996).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '1990-01-01' må være større enn '1996'", toString())
        }
    }

    /**
     * Generisk
     */
    @Test
    fun erLik() {
        (tjue erLik 20).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '20' er lik '20'", toString())
        }
        (tjue erLik 5).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '20' må være lik '5'", toString())
        }
    }

    @Test
    fun erLikFaktum() {
        (flagg erLik Faktum(true)).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA 'flagg' (true) er lik 'true'", toString())
        }
        (tjue erLik Faktum(20)).apply {
            assertTrue(fired())
            assertEquals("par_subsumsjon: JA '20' er lik '20'", toString())
        }
        (Faktum(false) erLik Faktum(true)).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI 'false' må være lik 'true'", toString())
        }
        (fem erLik tjue).apply {
            assertFalse(fired())
            assertEquals("par_subsumsjon: NEI '5' må være lik '20'", toString())
        }
    }

    @Test
    fun erBlant() {
        (A erBlant list).apply {
            assertTrue(fired())
            assertEquals("mengde_subsumsjon: JA 'A' (A) er blandt [faktum: 'A', faktum: 'B', faktum: 'C']", toString())
        }
        (D erBlant list).apply {
            assertFalse(fired())
            assertEquals(
                "mengde_subsumsjon: NEI 'D' (D) må være blandt [faktum: 'A', faktum: 'B', faktum: 'C']",
                toString()
            )
        }
    }

    @Test
    fun erIkkeBlant() {
        (D erIkkeBlant list).apply {
            assertTrue(fired())
            assertEquals(
                "mengde_subsumsjon: JA 'D' (D) er ikke blandt [faktum: 'A', faktum: 'B', faktum: 'C']",
                toString()
            )
        }
        (A erIkkeBlant list).apply {
            assertFalse(fired())
            assertEquals(
                "mengde_subsumsjon: NEI 'A' (A) må ikke være blandt [faktum: 'A', faktum: 'B', faktum: 'C']",
                toString()
            )
        }
    }


    /**
     * Lister
     *
     * Predikat:
     *      innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.minstEn {
     *         it.kravlinjeType == UT && it.virkningsdato < localDate(2021, 1, 1)
     *      }
     *
     * MendeSubsumsjon:
     *      Minst 1 ForsteVirkningsdatoGrunnlag opp
     *
     */
//    @Test
//    fun minst() {
//        fvdgList.xminst(1) { it == A.verdi }.apply {
//            assertTrue(fired())
//            assertEquals("mengde_subsumsjon: JA '1' blandt [faktum: 'A', faktum: 'B', faktum: 'C']", toString())
//        }
//    }
//
//    fun <FaktumGenerator> Iterable<FaktumGenerator>.xminst(target: Int, quantifier: (T) -> Boolean) = MengdeSubsumsjon(
//        MengdeKomparator.MINST,
//        Faktum("mål antall", target),
//        this.map { Faktum(it) },
//    ) { this.count(quantifier) >= target }


}