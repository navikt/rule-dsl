package no.nav.system.rule.dsl.demo.formel

import no.nav.system.rule.dsl.demo.domain.Tilleggspensjon
import no.nav.system.rule.dsl.formel.*
import no.nav.system.rule.dsl.formel.FormelBuilder.Companion.kmath
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import redempt.crunch.Crunch.compileExpression
import kotlin.math.roundToInt

class FormelTest {

    @Test
    fun simpleIntFormel() {
        val grunnbeløp = Formel<Int>("Grunnbeløp", 5000)
        val grunnbeløpPlussTusen: Formel<Int> = 1000 + grunnbeløp

        assertEquals("1000 + Grunnbeløp", grunnbeløpPlussTusen.notasjon)
        assertEquals("1000 + 5000", grunnbeløpPlussTusen.innhold)
        val res = grunnbeløpPlussTusen.resultat()
        assertEquals(6000, res)
    }

    @Test
    fun simpleDoubleFormel() {
        val fem = Formel<Int>("fem", 5)
        val toOgEnHalv: Formel<Double> = fem / 2

        assertEquals("fem / 2", toOgEnHalv.notasjon)
        assertEquals("5 / 2", toOgEnHalv.innhold)
        assertEquals(2.5, toOgEnHalv.resultat())
    }

    @Test
    fun gjenbrukAvFormel() {
        val G = Formel("G", 1000)
        val SPT = Formel("SPT", 2.0)

        val brutto: Formel<Double> = G * SPT

        val plus200: Formel<Double> = brutto + 200
        assertEquals(2200.0, plus200.resultat())

        val minus200: Formel<Double> = brutto - 200
        assertEquals(1800.0, minus200.resultat())
    }

    @Test
    fun immutableFormel() {

        val desimal = Formel("tiKommaTo", 10.2)

        val heltall = kmath<Int>()
            .formel(avrund(desimal))
            .build()

        assertEquals(10, heltall.resultat())
        assertEquals(10.2, desimal.resultat())
    }

    @Test
    fun copyDefaultFormelFromFelt_auto() {
        val brutto = Formel(2000)
        var netto = brutto

        assertEquals(2000, brutto.resultat())
        assertEquals(2000, netto.resultat())

        netto = brutto - 500

        assertEquals(2000, brutto.resultat())
        assertEquals(1500, netto.resultat())
    }

    @Test
    fun integerDivisionResultsInDouble() {
        val a = Formel("a", 1)
        val b = Formel("b", 4)

        assertEquals(0.25, (a / b).resultat())
        assertEquals(0.25, (a / 4).resultat())
        assertEquals(0.25, (1 / b).resultat())
    }

    @Test
    fun paranteser_enkel() {
        val SPT = Formel("SPT", 4.3)
        val OPT = Formel("OPT", 2.3)
        val PÅ = Formel("PÅ", 20)

        val formel1 = kmath<Double>()
            .emne("f1")
            .formel((SPT - OPT) * PÅ)
            .build()
        assertEquals(40.0, formel1.resultat())
        assertEquals("(SPT - OPT) * PÅ", formel1.notasjon)
        assertEquals("(4.3 - 2.3) * 20", formel1.innhold)

        val formel2 = kmath<Double>()
            .emne("f2")
            .formel(PÅ * (SPT - OPT))
            .build()
        assertEquals(40.0, formel2.resultat())
        assertEquals("PÅ * (SPT - OPT)", formel2.notasjon)
        assertEquals("20 * (4.3 - 2.3)", formel2.innhold)
    }

    @Test
    fun paranteser_middels() {
        val SPT = Formel("SPT", 4.3)
        val OPT = Formel("OPT", 2.3)
        val PÅ = Formel("PÅ", 20)

        val formel1 = kmath<Double>()
            .emne("f1")
            .formel((SPT - OPT) * (PÅ) * PÅ)
            .build()
        assertEquals(800.0, formel1.resultat())
        assertEquals("(SPT - OPT) * PÅ * PÅ", formel1.notasjon)
        assertEquals("(4.3 - 2.3) * 20 * 20", formel1.innhold)
    }

    @Test
    fun paranteser_negativeVerdier() {
        val a = Formel("a", -2)
        val b = Formel("b", 1)
        val f = kmath<Int>()
            .emne("hello")
            .formel(4 - (a + b))
            .build()

        assertEquals(5, f.resultat())
        assertEquals("4 - (a + b)", f.notasjon)
        assertEquals("4 - (-2 + 1)", f.innhold)
    }

    @Test
    fun paranteser_negativeVerdier2() {
        val a = Formel("a", -2)
        val b = Formel("b", 1)
        val f = kmath<Int>()
            .emne("hello")
            .formel(-4 - (a - b))
            .build()

        assertEquals(-1, f.resultat())
        assertEquals("-4 - (a - b)", f.notasjon)
        assertEquals("-4 - (-2 - 1)", f.innhold)
    }

    @Test
    fun paranteser_positiveVerdier() {
        val a = Formel("a", 2)
        val b = Formel("b", 1)
        val f = kmath<Int>()
            .emne("hello")
            .formel(4 - (a + b))
            .build()

        assertEquals(1, f.resultat())
        assertEquals("4 - (a + b)", f.notasjon)
        assertEquals("4 - (2 + 1)", f.innhold)
    }

    @Test
    fun paranteser_negativeVerdier_reversed() {
        val a = Formel("a", -2)
        val b = Formel("b", 1)
        val f = kmath<Int>()
            .emne("hello")
            .formel((a + b) - 4)
            .build()

        assertEquals(-5, f.resultat())
        assertEquals("a + b - 4", f.notasjon)
        assertEquals("-2 + 1 - 4", f.innhold)
    }

    @Test
    fun paranteser_positiveVerdier_reversed() {
        val a = Formel("a", 2)
        val b = Formel("b", 1)
        val f = kmath<Int>()
            .emne("hello")
            .formel(4 - (a + b))
            .build()

        assertEquals(1, f.resultat())
        assertEquals("4 - (a + b)", f.notasjon)
        assertEquals("4 - (2 + 1)", f.innhold)
    }

    @Test
    fun funksjon_avrund() {
        val YPT = Formel("YPT", 7.33)

        val formel1 = kmath<Int>()
            .emne("avrund YPT")
            .formel(avrund(YPT))
            .build()
        assertEquals(7, formel1.resultat())
        assertEquals("avrund( YPT )", formel1.notasjon)
        assertEquals("avrund( 7.33 )", formel1.innhold)

        val SPT = Formel("SPT", 4.4)

        val formel2 = kmath<Int>()
            .emne("avrund SPT")
            .formel(avrund(avrund(SPT) * 0.4))
            .build()
        assertEquals(2, formel2.resultat())
        assertEquals("avrund( avrund( SPT ) * 0.4 )", formel2.notasjon)
        assertEquals("avrund( avrund( 4.4 ) * 0.4 )", formel2.innhold)
    }

    @Test
    fun copyFormelWithSubFormel() {
        val G = Formel(200000)

        val tpF92 = kmath<Double>()
            .emne("tp_f92")
            .formel(0.5 * G)
            .build()
        val tpE91 = kmath<Int>()
            .emne("tp_e91")
            .formel(1 * G)
            .build()
        val tp = kmath<Double>()
            .emne("tp")
            .formel(tpF92 + tpE91)
            .build()
        assertEquals(2, tp.subFormelList.size)

        val tpPlus = kmath<Double>()
            .emne("tpPlus")
            .formel(tp + 1)
            .build()

        assertEquals(300001.0, tpPlus.resultat())
        assertEquals("tp + 1", tpPlus.notasjon)
        assertEquals("300000.0 + 1", tpPlus.innhold)
        assertEquals(1, tpPlus.subFormelList.size)
        assertEquals(2, tpPlus.subFormelList.first().subFormelList.size)
    }

    @Test
    fun conflictingVars() {
        val G = Formel("G", 100000)
        val alsoGbutDifferent = Formel("G", 200000)

        assertThrows<IllegalArgumentException> {
            kmath<Int>()
                .emne("double G is bad")
                .formel(G + 1 + alsoGbutDifferent)
                .build()
        }.also {
            assertEquals("Variable conflict: 'G' with value 100000 would be reassigned to value 200000", it.message)
        }

        assertThrows<IllegalArgumentException> {
            G + alsoGbutDifferent
        }.also {
            assertEquals("Formula conflict: 'G' with value 100000 would be reassigned to value 200000", it.message)
        }
    }

    @Test
    fun conflictingVars2() {
        val G1 = Formel("G", 95000)
        val G2 = Formel("G", 95001)
        val SPT = Formel("SPT", 4.23)
        val påF92 = Formel("PÅ_F92", 25)
        val påE91 = Formel("PÅ_E91", 15)

        val tpF92 = avrund(0.45 * G1 * SPT * påF92 / 40)
        val tpE91 = avrund(0.45 * G2 * SPT * påE91 / 40)

        assertThrows<IllegalArgumentException> {
            tpF92 + tpE91
        }.also {
            assertEquals("Variable conflict: 'G' with value 95000 would be reassigned to value 95001", it.message)
        }
    }

    @Test
    fun noConflictWhenLocked() {
        val G1 = Formel("G", 95000)
        val G2 = Formel("G", 95001)
        val SPT = Formel("SPT", 4.23)
        val påF92 = Formel("PÅ_F92", 25)
        val påE91 = Formel("PÅ_E91", 15)

        val tpF92 = kmath<Int>().emne("tp_f92").formel(avrund(0.45 * G1 * SPT * påF92 / 40)).build()
        val tpE91 = kmath<Int>().emne("tp_e91").formel(avrund(0.45 * G2 * SPT * påE91 / 40)).build()

        assertDoesNotThrow {
            tpF92 + tpE91
        }
    }

    @Test
    fun conflictWhenUnlocked() {
        val G1 = Formel("G", 95000)
        val G2 = Formel("G", 95001)
        val SPT = Formel("SPT", 4.23)
        val påF92 = Formel("PÅ_F92", 25)
        val påE91 = Formel("PÅ_E91", 15)

        val tpF92 = kmath<Int>().emne("tp_f92").formel(avrund(0.45 * G1 * SPT * påF92 / 40)).unlock().build()
        val tpE91 = kmath<Int>().emne("tp_e91").formel(avrund(0.45 * G2 * SPT * påE91 / 40)).unlock().build()

        assertThrows<IllegalArgumentException> {
            tpF92 + tpE91
        }.also {
            assertEquals("Variable conflict: 'G' with value 95000 would be reassigned to value 95001", it.message)
        }
    }

    @Test
    fun conflictingFormulas() {
        val G = Formel("G", 40000)
        val tpF92 = kmath<Double>()
            .emne("duplikatnavn")
            .formel(0.5 * G)
            .build()
        val tpE91 = kmath<Int>()
            .emne("duplikatnavn")
            .formel(2 * G)
            .build()

        assertThrows<IllegalArgumentException> {
            kmath<Double>()
                .emne("sum")
                .formel(tpF92 + tpE91)
                .build().toString()
        }.also {
            assertEquals(
                "Formula conflict: 'duplikatnavn' with value 20000.0 would be reassigned to value 80000",
                it.message
            )
        }
    }

    @Test
    fun conflictingVars_renameToExistingNamedVariable() {
        val G = Formel("G", 95000)

        val tpE91 = kmath<Int>()
            .emne("tp")
            .formel(2 * G)
            .build()

        assertThrows<IllegalArgumentException> {
            tpE91.toBuilder().emne("G").build()
        }.also {
            assertEquals(
                "Circular reference detected: Formula name 'G' cannot contain variables with the same name.",
                it.message
            )
        }
    }

    @Test
    fun conflictingVars_renameToExistingFormel() {
        val G = Formel("G", 95000)
        val tpF92 = kmath<Double>()
            .emne("tp_f92")
            .formel(0.5 * G)
            .build()
        val tpE91 = kmath<Int>()
            .emne("tp_e91")
            .formel(2 * G)
            .build()

        val copy = tpE91.toBuilder().emne("tp_f92").build()

        assertThrows<IllegalArgumentException> {
            kmath<Double>()
                .emne("sum")
                //  .rename(tp_e91, "tp_f92")
                .formel(tpF92 + copy)
                .build().toString()
        }.also {
            assertEquals(
                "Formula conflict: 'tp_f92' with value 47500.0 would be reassigned to value 190000",
                it.message
            )
        }
    }

    @Test
    fun noConflict_renamedFormel() {
        val G = Formel("G", 40000)
        val tpF92 = kmath<Double>()
            .emne("duplikatnavn")
            .formel(0.5 * G)
            .build()
        val tpE91 = kmath<Int>()
            .emne("duplikatnavn")
            .formel(2 * G)
            .build()

        val copy = tpE91.toBuilder().emne("e91").build()
        assertDoesNotThrow {
            kmath<Double>()
                .emne("sum")
                .formel(tpF92 + copy)
                .build().toString()
        }
        assertEquals("duplikatnavn", tpE91.emne)
        assertEquals("duplikatnavn", tpF92.emne)
    }

    /**
     * TODO Test feiler, vurder behov for støtte.
     * Uklart om vi burde lage støtte for dette da løsningen virker tungvindt.
     * Problemstillingen er:
     *   Man har to formeler som begge benytter en variabel med samme navn ("G") men med forskjellige verdi.
     *   En ny formel referer begge de tidligere formelene og variabel "G" blir da tvetydig.
     *
     *  Gammel rammeverk løste dette ved å ikke la den ene subformelen bli ekspandert inn i hovedformelen.
     */
    @Test
    @Disabled
    fun shouldNotFailWhenConsumeSubformulaWithConflictingVar() {
        val G = Formel("G", 100000)
        val alsoG = Formel("G", 200000)

        val tpF92 = 0.5 * G
        val tpE91 = 1 * alsoG
        val tp = tpF92 + tpE91

        assertEquals("tp_f92 + 1 * G", tp.notasjon)
        assertEquals("100000 + 1 * 100000", tp.innhold)
    }

    @Test
    fun shouldShowSeperateFormulas() {
        val G = Formel("G", 95000)
        val SPT = Formel("SPT", 4.23)
        val påF92 = Formel("PÅ_F92", 25)
        val påE91 = Formel("PÅ_E91", 15)

        val tpF92 = kmath<Int>()
            .emne("tp_f92")
            .formel(avrund(0.45 * G * SPT * påF92 / 40))
            .build()
        val tpE91 = kmath<Int>()
            .emne("tp_e91")
            .formel(avrund(0.45 * G * SPT * påE91 / 40))
            .build()

        val sum = tpF92 + tpE91

        assertEquals(
            (0.45 * 95000 * 4.23 * 25 / 40).roundToInt() + (0.45 * 95000 * 4.23 * 15 / 40).roundToInt(),
            sum.resultat()
        )
        assertEquals("tp_f92 + tp_e91", sum.notasjon)
        assertEquals("113020 + 67812", sum.innhold)

        assertEquals(2, sum.subFormelList.size, "Should have two subformulas in formula 'brutto' ")
        assertFalse(sum.innhold.contains(".0"), "Should not contain desimals in int variables ")
        assertEquals(5, sum.toHTML(0).split("\\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray().size, "Limited depth presentation ")
    }

    @Test
    @Throws
    fun shouldFailOnCircularReference() {
        assertThrows<IllegalArgumentException> {
            kmath<Int>()
                .emne("a")
                .formel(Formel("a", 1) + 1)
                .build()
        }.also {
            assertEquals(
                "Circular reference detected: Formula name 'a' cannot contain variables with the same name.",
                it.message
            )
        }
    }

    @Test
    fun shouldResolveSubformulaOnce() {
        val a = kmath<Int>()
            .emne("a")
            .formel(Formel("x", 10) + 5)
            .build()
        val sum = kmath<Int>()
            .emne("sum")
            .formel(a + a)
            .build()
        assertEquals(1, sum.subFormelList.size, "Should have one subformula in formula 'sum' ")
        assertTrue(sum.namedVarMap.isEmpty(), "namedVarMap should be empty.")
    }

    @Test
    fun anonymousFormelAllowed() {
        assertDoesNotThrow {
            kmath<Int>()
                .formel(Formel("a", 1))
                .build().also {
                    assertTrue(it.navn().startsWith("anonymous"))
                }
        }
    }

    @Test
    fun revurderYtelse1967_SIR250725_2() {
        val G = Formel("G", 79216)
        var OPT = Formel("OPT", 2.47)
        var PÅ = Formel("PÅ", 16)
        var SPT = Formel("SPT", 2.47)
        var OÅ = Formel("OÅ", 20)

        val tpBrukerUtenPTBrutto = kmath<Int>()
            .emne("bruker_utenPT")
            .formel(avrund(0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * 1 / 12))
            .build()
        assertEquals(5870, tpBrukerUtenPTBrutto.resultat())
        assertEquals(
            "avrund( 0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * 1 / 12 )",
            tpBrukerUtenPTBrutto.notasjon
        )
        assertEquals(
            "avrund( 0.45 * 79216 * (2.47 * 16 / 20 + (2.47 - 2.47) * 16 / 40) * 1 / 12 )",
            tpBrukerUtenPTBrutto.innhold
        )

        OPT = Formel("OPT", 4.0)
        PÅ = Formel("PÅ", 17)
        SPT = Formel("SPT", 6.46)
        OÅ = Formel("OÅ", 20)
        val tpPst = Formel("tp_pst", 0.55)
        val UFG = Formel("UFG", 100)

        val tpAvdodBrutto = kmath<Int>()
            .emne("avdød")
            .formel(avrund(0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * UFG / 100 * 1 / 12 * tpPst))
            .build()
        assertEquals(7263, tpAvdodBrutto.resultat())
        assertEquals(
            "avrund( 0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * UFG / 100 * 1 / 12 * tp_pst )",
            tpAvdodBrutto.notasjon
        )
        assertEquals(
            "avrund( 0.45 * 79216 * (4.0 * 17 / 20 + (6.46 - 4.0) * 17 / 40) * 100 / 100 * 1 / 12 * 0.55 )",
            tpAvdodBrutto.innhold
        )

        val tpSammenstillBrutto = kmath<Int>()
            .emne("brutto")
            .formel(avrund(tpBrukerUtenPTBrutto * 0.55 + tpAvdodBrutto))
            .build()

        assertEquals(10492, tpSammenstillBrutto.resultat())
        assertEquals("avrund( bruker_utenPT * 0.55 + avdød )", tpSammenstillBrutto.notasjon)
        assertEquals("avrund( 5870 * 0.55 + 7263 )", tpSammenstillBrutto.innhold)

        assertEquals("bruker_utenPT", tpBrukerUtenPTBrutto.emne)
        assertEquals(
            "avrund( 0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * 1 / 12 )",
            tpBrukerUtenPTBrutto.notasjon
        )
        assertEquals(
            "avrund( 0.45 * 79216 * (2.47 * 16 / 20 + (2.47 - 2.47) * 16 / 40) * 1 / 12 )",
            tpBrukerUtenPTBrutto.innhold
        )

        assertEquals("avdød", tpAvdodBrutto.emne)
        assertEquals(
            "avrund( 0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * UFG / 100 * 1 / 12 * tp_pst )",
            tpAvdodBrutto.notasjon
        )
        assertEquals(
            "avrund( 0.45 * 79216 * (4.0 * 17 / 20 + (6.46 - 4.0) * 17 / 40) * 100 / 100 * 1 / 12 * 0.55 )",
            tpAvdodBrutto.innhold
        )
    }

    @Test
    fun anonymousSubformulaAreRenamedInHighlevelContext() {
        val to = Formel(2)
        val tre = Formel(3)

        /**
         * anonyme formler uten kjennskap til kontekst
         */
        val anonF1 = kmath<Int>()
            .formel(to * tre)
            .build()
        val anonF2 = kmath<Int>()
            .formel(tre + to)
            .build()

        /**
         * Høyere nivå kjenner kontekst og navngir formlene.
         */
        val copyAnonF1 = anonF1.emne("poengtillegg")
        val copyAnonF2 = anonF2.emne("avdod")
        val tpSumBrutto = kmath<Double>()
            .emne("tpSum")
            .formel(copyAnonF1 * 0.5 + copyAnonF2)
            .build()

        assertEquals("poengtillegg * 0.5 + avdod", tpSumBrutto.notasjon)
        assertEquals("6 * 0.5 + 5", tpSumBrutto.innhold)
        assertEquals("poengtillegg", tpSumBrutto.subFormelList.toList()[0].emne)
        assertEquals("2 * 3", tpSumBrutto.subFormelList.toList()[0].notasjon)
        assertEquals("2 * 3", tpSumBrutto.subFormelList.toList()[0].innhold)
        assertEquals("avdod", tpSumBrutto.subFormelList.toList()[1].emne)
        assertEquals("3 + 2", tpSumBrutto.subFormelList.toList()[1].notasjon)
        assertEquals("3 + 2", tpSumBrutto.subFormelList.toList()[1].innhold)
    }

    @Test
    fun shouldCopySubFormelList() {
        val a = kmath<Int>()
            .emne("a")
            .formel(Formel("x", 10) + 5)
            .build()
        val sum = kmath<Int>()
            .emne("sum")
            .formel(a + a)
            .build()

        val sumCopy = sum.copy()
        assertEquals(1, sumCopy.subFormelList.size, "Should have one subformula in formula 'sum' after copying")
        assertTrue(sumCopy.namedVarMap.isEmpty(), "Should have no vars in formula 'sum' after copying")
    }

    @Test
    fun gamleResultatMåIkkeOverskrive() {
        val orginal = kmath<Double>()
            .emne("hundre")
            .formel(Formel("bpa", 100.0))
            .build()

        assertEquals(100.0, orginal.resultat())

        val tohundreOgTjue = kmath<Double>()
            .emne("tohundreOgTjue")
            .formel(orginal + 120)
            .build()

        assertEquals(220.0, tohundreOgTjue.resultat())

        orginal.resultat() // kall til orginal resultat skal ikke overskrive resultatet i feltet (ja dette har skjedd).

        assertEquals(220.0, tohundreOgTjue.resultat())
    }

    @Test
    fun beregnYtelse_BERYP_04_forhoyelseUtenNyttUft() {
        val G = Formel("G", 75000)
        val påF92 = Formel("PÅ_F92", 0)
        val påE91 = Formel("PÅ_E91", 40)
        val SPT = Formel("SPT", 1.6)
        val UFG = Formel("UFG", 30)
        val YUG = Formel("YUG", 30)
        val YPT = Formel("YPT", 5.08)
        val påYskE91 = Formel("PÅ_YSK_E91", 40)
        val påYsk = Formel("PÅ_YSK", 40)
        val påYskF92 = Formel("PÅ_YSK_F92", 0)

        /**
         * Uføre andelen
         */
        val e91 = kmath<Double>()
            .emne("e91")
            .formel(0.42 * G * SPT * påE91 / 40 * UFG / 100)
            .build()
        val f92 = kmath<Double>()
            .emne("f92")
            .formel(0.45 * G * SPT * påF92 / 40 * UFG / 100)
            .build()
        val uføreTpSumBrutto = kmath<Int>()
            .emne("uføre")
            .formel(avrund((f92 + e91) * 1 / 12))
            .build()
        assertEquals(1260, uføreTpSumBrutto.resultat())
        assertEquals(2, uføreTpSumBrutto.subFormelList.size)
        assertEquals("avrund( (f92 + e91) * 1 / 12 )", uføreTpSumBrutto.notasjon)
        assertEquals("avrund( (0.0 + 15120.0) * 1 / 12 )", uføreTpSumBrutto.innhold)

        /**
         * Restandel Uføre
         */
        val upF92 = kmath<Double>()
            .emne("up_f92")
            .formel(0.45 * G * SPT * påF92 / 40 * (UFG - YUG) / 100)
            .build()
        val upE91 = kmath<Double>()
            .emne("up_e91")
            .formel(0.42 * G * SPT * påE91 / 40 * (UFG - YUG) / 100)
            .build()

        /**
         * Andel yrkesskade
         */
        val ypF92 = kmath<Double>()
            .emne("yp_f92")
            .formel(0.45 * G * YPT * påYskF92 / påYsk * YUG / 100)
            .build()
        val ypE91 = kmath<Double>()
            .emne("yp_e91")
            .formel(0.42 * G * YPT * påYskE91 / påYsk * YUG / 100)
            .build()

        /**
         * Sammenstill restandel uføre og andel yrkesskade
         */
        val yrkeBerTpSumBrutto = kmath<Int>()
            .emne("ysk")
            .formel(avrund((ypF92 + ypE91) * 1 / 12) + avrund((upF92 + upE91) * 1 / 12))
            .build()
        assertEquals(4001, yrkeBerTpSumBrutto.resultat())
        assertEquals(4, yrkeBerTpSumBrutto.subFormelList.size)
        assertEquals(
            "avrund( (yp_f92 + yp_e91) * 1 / 12 ) + avrund( (up_f92 + up_e91) * 1 / 12 )",
            yrkeBerTpSumBrutto.notasjon
        )
        assertEquals(
            "avrund( (0.0 + 48006.0) * 1 / 12 ) + avrund( (0.0 + 0.0) * 1 / 12 )",
            yrkeBerTpSumBrutto.innhold
        )

        /**
         * Kontekst er 'ukjent'. Dvs generellt regelsett for SAM_BER som legger sammen to delberegninger uten å vite detaljer.
         * Som i regelkoden er SAMBER skapt på bakgrunn av kopi av uføreber.
         */
        val samberTpsumBrutto = kmath<Int>()
            .emne("sum")
            .formel(uføreTpSumBrutto + yrkeBerTpSumBrutto)
            .build()
        assertEquals(2, samberTpsumBrutto.subFormelList.size)
        assertEquals("uføre + ysk", samberTpsumBrutto.notasjon)
        assertEquals("1260 + 4001", samberTpsumBrutto.innhold)
    }

    @Test
    fun toHtml() {
        val femti = Formel(50)
        val formel = kmath<Double>()
            .prefix("TP")
            .emne("eksportforbud")
            .postfix("bruttoPerAr")
            .formel(femti * 2.0)
            .build()

        val expected = """
                <formel navn='TP_eksportforbud_bruttoPerAr' level='0' resultat='100.0' locked='true' antSubFormler='0'>
                  <fl>eksportforbud = 50 * 2.0</fl>
                  <fl>eksportforbud = 50 * 2.0</fl>
                  <fl>eksportforbud = 100.0</fl>
                </formel>

        """.trimIndent()
        assertEquals(expected, formel.toHTML())
    }

    @Test
    fun customFunction_afpAvrundNetto() {
        val bpa = Formel("bpa", 140452.77285)
        val bpaUtb = kmath<Double>()
            .emne("avkortet")
            .formel(afpAvrundNetto(bpa, Formel("utbetalingsprosent", 57)) * 12.0)
            .build()

        assertEquals(80064.0, bpaUtb.resultat())
        assertEquals(140452.77285, bpa.resultat())
    }

    @Test
    fun `skal returnere høyeste verdi ved bruk av kMax med to Formler som input`() {
        val G = Formel("G", 2000)
        val f = 100 + G

        val kMax = kMax(G, f)
        assertEquals(2100, kMax.resultat())
    }

    @Test
    fun `skal returnere høyeste verdi ved bruk av kMax med Number og Formlel som input`() {
        val G = Formel("G", 2000)

        val kMax = kMax(3000, G)
        assertEquals(3000, kMax.resultat())
    }

    @Test
    fun `skal returnere lavest verdi ved bruk av kMax med to Formler som input`() {
        val G = Formel("G", 2000)
        val f = 100 + G

        val kMax = kMin(G, f)
        assertEquals(2000, kMax.resultat())
    }

    @Test
    fun `skal returnere lavest verdi ved bruk av kMax med Number og Formlel som input`() {
        val G = Formel("G", 2000)

        val kMax = kMin(3000, G)
        assertEquals(2000, kMax.resultat())
    }

    @Test
    fun `skal kunne utvide formel`() {
        val gp = Formel("Grunnpensjon", 200.0)
        val tp = Formel("Tilleggspensjon", 300.0)

        var sum = kmath<Int>().formel(avrund(gp + tp)).build()

        sum += Formel("Minstenivåtillegg", 500)

        assertEquals(1000, sum.resultat())
    }

    @Test
    fun `skal feile ved ustøttet type`() {
        assertThrows<IllegalArgumentException> {
            kmath<Float>()
        }.also {
            assertEquals(
                "Unsupported type class kotlin.Float. Only Int and Double are supported.",
                it.message
            )
        }
    }

    @Test
    fun `plus etter funksjon`() {
        assertEquals(14.0, compileExpression("max(15, 3) - 1", evalEnv).evaluate())
    }

    @Test
    fun `minus etter funksjon`() {
        assertEquals(16.0, compileExpression("max(15, 3) + 1", evalEnv).evaluate())
    }

    @Test
    fun subformelMedEgneFelt() {
        val tp = Tilleggspensjon()

        tp.apKap19MedGJR = kmath<Int>()
            .formel(Formel("MedGJR", 34000))
            .build()

        tp.apKap19UtenGJR = kmath<Int>()
            .formel(Formel("MedGJR", 28000))
            .build()

        val uttaksgrad = Formel("uttaksgrad", 50)

        tp.referansebelop = kmath<Int>()
            .formel(avrund((tp.apKap19MedGJR - tp.apKap19UtenGJR) * 100 / uttaksgrad))
            .build()

        assertEquals(34000, tp.apKap19MedGJR.resultat())
        assertEquals(28000, tp.apKap19UtenGJR.resultat())
        assertEquals(12000, tp.referansebelop.resultat())
    }
}