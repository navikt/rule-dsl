package no.nav.system.rule.dsl.demo.formel

import no.nav.system.rule.dsl.demo.domain.Tilleggspensjon
import no.nav.system.rule.dsl.formel.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import redempt.crunch.Crunch.compileExpression
import kotlin.math.roundToInt

class FormelTest {

    @Test
    fun simpleIntFormel() {
        val grunnbeløp = Formel.variable("Grunnbeløp", 5000)
        val grunnbeløpPlussTusen: Formel<Int> = 1000 + grunnbeløp

        assertEquals("1000 + Grunnbeløp", grunnbeløpPlussTusen.notasjon)
        assertEquals("1000 + 5000", grunnbeløpPlussTusen.innhold)
        val res = grunnbeløpPlussTusen.value
        assertEquals(6000, res)
    }

    @Test
    fun simpleDoubleFormel() {
        val fem = Formel.variable("fem", 5)
        val toOgEnHalv: Formel<Double> = fem / 2

        assertEquals("fem / 2", toOgEnHalv.notasjon)
        assertEquals("5 / 2", toOgEnHalv.innhold)
        assertEquals(2.5, toOgEnHalv.value)
    }

    @Test
    fun gjenbrukAvFormel() {
        val G = Formel.variable("G", 1000)
        val SPT = Formel.variable("SPT", 2.0)

        val brutto: Formel<Double> = G * SPT

        val plus200: Formel<Double> = brutto + 200
        assertEquals(2200.0, plus200.value)

        val minus200: Formel<Double> = brutto - 200
        assertEquals(1800.0, minus200.value)
    }

    @Test
    fun immutableFormel() {

        val desimal = Formel.variable("tiKommaTo", 10.2)

        val heltall = FormelBuilder.create<Int>()
            .expression(avrund(desimal))
            .build()

        assertEquals(10, heltall.value)
        assertEquals(10.2, desimal.value)
    }

    @Test
    fun copyDefaultFormelFromFelt_auto() {
        val brutto = Formel.constant(2000)
        var netto = brutto

        assertEquals(2000, brutto.value)
        assertEquals(2000, netto.value)

        netto = brutto - 500

        assertEquals(2000, brutto.value)
        assertEquals(1500, netto.value)
    }

    @Test
    fun integerDivisionResultsInDouble() {
        val a = Formel.variable("a", 1)
        val b = Formel.variable("b", 4)

        assertEquals(0.25, (a / b).value)
        assertEquals(0.25, (a / 4).value)
        assertEquals(0.25, (1 / b).value)
    }

    @Test
    fun paranteser_enkel() {
        val SPT = Formel.variable("SPT", 4.3)
        val OPT = Formel.variable("OPT", 2.3)
        val PÅ = Formel.variable("PÅ", 20)

        val formel1 = FormelBuilder.create<Double>()
            .name("f1")
            .expression((SPT - OPT) * PÅ)
            .build()
        assertEquals(40.0, formel1.value)
        assertEquals("(SPT - OPT) * PÅ", formel1.notasjon)
        assertEquals("(4.3 - 2.3) * 20", formel1.innhold)

        val formel2 = FormelBuilder.create<Double>()
            .name("f2")
            .expression(PÅ * (SPT - OPT))
            .build()
        assertEquals(40.0, formel2.value)
        assertEquals("PÅ * (SPT - OPT)", formel2.notasjon)
        assertEquals("20 * (4.3 - 2.3)", formel2.innhold)
    }

    @Test
    fun paranteser_middels() {
        val SPT = Formel.variable("SPT", 4.3)
        val OPT = Formel.variable("OPT", 2.3)
        val PÅ = Formel.variable("PÅ", 20)

        val formel1 = FormelBuilder.create<Double>()
            .name("f1")
            .expression((SPT - OPT) * (PÅ) * PÅ)
            .build()
        assertEquals(800.0, formel1.value)
        assertEquals("(SPT - OPT) * PÅ * PÅ", formel1.notasjon)
        assertEquals("(4.3 - 2.3) * 20 * 20", formel1.innhold)
    }

    @Test
    fun paranteser_negativeVerdier() {
        val a = Formel.variable("a", -2)
        val b = Formel.variable("b", 1)
        val f = FormelBuilder.create<Int>()
            .name("hello")
            .expression(4 - (a + b))
            .build()

        assertEquals(5, f.value)
        assertEquals("4 - (a + b)", f.notasjon)
        assertEquals("4 - (-2 + 1)", f.innhold)
    }

    @Test
    fun paranteser_negativeVerdier2() {
        val a = Formel.variable("a", -2)
        val b = Formel.variable("b", 1)
        val f = FormelBuilder.create<Int>()
            .name("hello")
            .expression(-4 - (a - b))
            .build()

        assertEquals(-1, f.value)
        assertEquals("-4 - (a - b)", f.notasjon)
        assertEquals("-4 - (-2 - 1)", f.innhold)
    }

    @Test
    fun paranteser_positiveVerdier() {
        val a = Formel.variable("a", 2)
        val b = Formel.variable("b", 1)
        val f = FormelBuilder.create<Int>()
            .name("hello")
            .expression(4 - (a + b))
            .build()

        assertEquals(1, f.value)
        assertEquals("4 - (a + b)", f.notasjon)
        assertEquals("4 - (2 + 1)", f.innhold)
    }

    @Test
    fun paranteser_negativeVerdier_reversed() {
        val a = Formel.variable("a", -2)
        val b = Formel.variable("b", 1)
        val f = FormelBuilder.create<Int>()
            .name("hello")
            .expression((a + b) - 4)
            .build()

        assertEquals(-5, f.value)
        assertEquals("a + b - 4", f.notasjon)
        assertEquals("-2 + 1 - 4", f.innhold)
    }

    @Test
    fun paranteser_positiveVerdier_reversed() {
        val a = Formel.variable("a", 2)
        val b = Formel.variable("b", 1)
        val f = FormelBuilder.create<Int>()
            .name("hello")
            .expression(4 - (a + b))
            .build()

        assertEquals(1, f.value)
        assertEquals("4 - (a + b)", f.notasjon)
        assertEquals("4 - (2 + 1)", f.innhold)
    }

    @Test
    fun funksjon_avrund() {
        val YPT = Formel.variable("YPT", 7.33)

        val formel1 = FormelBuilder.create<Int>()
            .name("avrund YPT")
            .expression(avrund(YPT))
            .build()
        assertEquals(7, formel1.value)
        assertEquals("avrund( YPT )", formel1.notasjon)
        assertEquals("avrund( 7.33 )", formel1.innhold)

        val SPT = Formel.variable("SPT", 4.4)

        val formel2 = FormelBuilder.create<Int>()
            .name("avrund SPT")
            .expression(avrund(avrund(SPT) * 0.4))
            .build()
        assertEquals(2, formel2.value)
        assertEquals("avrund( avrund( SPT ) * 0.4 )", formel2.notasjon)
        assertEquals("avrund( avrund( 4.4 ) * 0.4 )", formel2.innhold)
    }

    @Test
    fun copyFormelWithSubFormel() {
        val G = Formel.constant(200000)

        val tpF92 = FormelBuilder.create<Double>()
            .name("tp_f92")
            .expression(0.5 * G)
            .build()
        val tpE91 = FormelBuilder.create<Int>()
            .name("tp_e91")
            .expression(1 * G)
            .build()
        val tp = FormelBuilder.create<Double>()
            .name("tp")
            .expression(tpF92 + tpE91)
            .build()
        assertEquals(2, tp.subFormelList.size)

        val tpPlus = FormelBuilder.create<Double>()
            .name("tpPlus")
            .expression(tp + 1)
            .build()

        assertEquals(300001.0, tpPlus.value)
        assertEquals("tp + 1", tpPlus.notasjon)
        assertEquals("300000.0 + 1", tpPlus.innhold)
        assertEquals(1, tpPlus.subFormelList.size)
        assertEquals(2, tpPlus.subFormelList.first().subFormelList.size)
    }

    @Test
    fun conflictingVars() {
        val G = Formel.variable("G", 100000)
        val alsoGbutDifferent = Formel.variable("G", 200000)

        assertThrows<IllegalArgumentException> {
            FormelBuilder.create<Int>()
                .name("double G is bad")
                .expression(G + 1 + alsoGbutDifferent)
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
        val G1 = Formel.variable("G", 95000)
        val G2 = Formel.variable("G", 95001)
        val SPT = Formel.variable("SPT", 4.23)
        val påF92 = Formel.variable("PÅ_F92", 25)
        val påE91 = Formel.variable("PÅ_E91", 15)

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
        val G1 = Formel.variable("G", 95000)
        val G2 = Formel.variable("G", 95001)
        val SPT = Formel.variable("SPT", 4.23)
        val påF92 = Formel.variable("PÅ_F92", 25)
        val påE91 = Formel.variable("PÅ_E91", 15)

        val tpF92 = FormelBuilder.create<Int>().name("tp_f92").expression(avrund(0.45 * G1 * SPT * påF92 / 40)).build()
        val tpE91 = FormelBuilder.create<Int>().name("tp_e91").expression(avrund(0.45 * G2 * SPT * påE91 / 40)).build()

        assertDoesNotThrow {
            tpF92 + tpE91
        }
    }

    @Test
    fun conflictWhenUnlocked() {
        val G1 = Formel.variable("G", 95000)
        val G2 = Formel.variable("G", 95001)
        val SPT = Formel.variable("SPT", 4.23)
        val påF92 = Formel.variable("PÅ_F92", 25)
        val påE91 = Formel.variable("PÅ_E91", 15)

        val tpF92 = FormelBuilder.create<Int>().name("tp_f92").expression(avrund(0.45 * G1 * SPT * påF92 / 40)).unlocked().build()
        val tpE91 = FormelBuilder.create<Int>().name("tp_e91").expression(avrund(0.45 * G2 * SPT * påE91 / 40)).unlocked().build()

        assertThrows<IllegalArgumentException> {
            tpF92 + tpE91
        }.also {
            assertEquals("Variable conflict: 'G' with value 95000 would be reassigned to value 95001", it.message)
        }
    }

    @Test
    fun conflictingFormulas() {
        val G = Formel.variable("G", 40000)
        val tpF92 = FormelBuilder.create<Double>()
            .name("duplikatnavn")
            .expression(0.5 * G)
            .build()
        val tpE91 = FormelBuilder.create<Int>()
            .name("duplikatnavn")
            .expression(2 * G)
            .build()

        assertThrows<IllegalArgumentException> {
            FormelBuilder.create<Double>()
                .name("sum")
                .expression(tpF92 + tpE91)
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
        val G = Formel.variable("G", 95000)

        val tpE91 = FormelBuilder.create<Int>()
            .name("tp")
            .expression(2 * G)
            .build()

        assertThrows<IllegalArgumentException> {
            tpE91.toBuilder().name("G").build()
        }.also {
            assertEquals(
                "Circular reference detected: Formula name 'G' cannot contain variables with the same name.",
                it.message
            )
        }
    }

    @Test
    fun conflictingVars_renameToExistingFormel() {
        val G = Formel.variable("G", 95000)
        val tpF92 = FormelBuilder.create<Double>()
            .name("tp_f92")
            .expression(0.5 * G)
            .build()
        val tpE91 = FormelBuilder.create<Int>()
            .name("tp_e91")
            .expression(2 * G)
            .build()

        val copy = tpE91.toBuilder().name("tp_f92").build()

        assertThrows<IllegalArgumentException> {
            FormelBuilder.create<Double>()
                .name("sum")
                //  .rename(tp_e91, "tp_f92")
                .expression(tpF92 + copy)
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
        val G = Formel.variable("G", 40000)
        val tpF92 = FormelBuilder.create<Double>()
            .name("duplikatnavn")
            .expression(0.5 * G)
            .build()
        val tpE91 = FormelBuilder.create<Int>()
            .name("duplikatnavn")
            .expression(2 * G)
            .build()

        val copy = tpE91.toBuilder().name("e91").build()
        assertDoesNotThrow {
            FormelBuilder.create<Double>()
                .name("sum")
                .expression(tpF92 + copy)
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
        val G = Formel.variable("G", 100000)
        val alsoG = Formel.variable("G", 200000)

        val tpF92 = 0.5 * G
        val tpE91 = 1 * alsoG
        val tp = tpF92 + tpE91

        assertEquals("tp_f92 + 1 * G", tp.notasjon)
        assertEquals("100000 + 1 * 100000", tp.innhold)
    }

    @Test
    fun shouldShowSeperateFormulas() {
        val G = Formel.variable("G", 95000)
        val SPT = Formel.variable("SPT", 4.23)
        val påF92 = Formel.variable("PÅ_F92", 25)
        val påE91 = Formel.variable("PÅ_E91", 15)

        val tpF92 = FormelBuilder.create<Int>()
            .name("tp_f92")
            .expression(avrund(0.45 * G * SPT * påF92 / 40))
            .build()
        val tpE91 = FormelBuilder.create<Int>()
            .name("tp_e91")
            .expression(avrund(0.45 * G * SPT * påE91 / 40))
            .build()

        val sum = tpF92 + tpE91

        assertEquals(
            (0.45 * 95000 * 4.23 * 25 / 40).roundToInt() + (0.45 * 95000 * 4.23 * 15 / 40).roundToInt(),
            sum.value
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
            FormelBuilder.create<Int>()
                .name("a")
                .expression(Formel.variable("a", 1) + 1)
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
        val a = FormelBuilder.create<Int>()
            .name("a")
            .expression(Formel.variable("x", 10) + 5)
            .build()
        val sum = FormelBuilder.create<Int>()
            .name("sum")
            .expression(a + a)
            .build()
        assertEquals(1, sum.subFormelList.size, "Should have one subformula in formula 'sum' ")
        assertTrue(sum.namedVarMap.isEmpty(), "namedVarMap should be empty.")
    }

    @Test
    fun anonymousFormelAllowed() {
        assertDoesNotThrow {
            FormelBuilder.create<Int>()
                .expression(Formel.variable("a", 1))
                .build().also {
                    assertTrue(it.name.startsWith("anonymous"))
                }
        }
    }

    @Test
    fun revurderYtelse1967_SIR250725_2() {
        val G = Formel.variable("G", 79216)
        var OPT = Formel.variable("OPT", 2.47)
        var PÅ = Formel.variable("PÅ", 16)
        var SPT = Formel.variable("SPT", 2.47)
        var OÅ = Formel.variable("OÅ", 20)

        val tpBrukerUtenPTBrutto = FormelBuilder.create<Int>()
            .name("bruker_utenPT")
            .expression(avrund(0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * 1 / 12))
            .build()
        assertEquals(5870, tpBrukerUtenPTBrutto.value)
        assertEquals(
            "avrund( 0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * 1 / 12 )",
            tpBrukerUtenPTBrutto.notasjon
        )
        assertEquals(
            "avrund( 0.45 * 79216 * (2.47 * 16 / 20 + (2.47 - 2.47) * 16 / 40) * 1 / 12 )",
            tpBrukerUtenPTBrutto.innhold
        )

        OPT = Formel.variable("OPT", 4.0)
        PÅ = Formel.variable("PÅ", 17)
        SPT = Formel.variable("SPT", 6.46)
        OÅ = Formel.variable("OÅ", 20)
        val tpPst = Formel.variable("tp_pst", 0.55)
        val UFG = Formel.variable("UFG", 100)

        val tpAvdodBrutto = FormelBuilder.create<Int>()
            .name("avdød")
            .expression(avrund(0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * UFG / 100 * 1 / 12 * tpPst))
            .build()
        assertEquals(7263, tpAvdodBrutto.value)
        assertEquals(
            "avrund( 0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * UFG / 100 * 1 / 12 * tp_pst )",
            tpAvdodBrutto.notasjon
        )
        assertEquals(
            "avrund( 0.45 * 79216 * (4.0 * 17 / 20 + (6.46 - 4.0) * 17 / 40) * 100 / 100 * 1 / 12 * 0.55 )",
            tpAvdodBrutto.innhold
        )

        val tpSammenstillBrutto = FormelBuilder.create<Int>()
            .name("brutto")
            .expression(avrund(tpBrukerUtenPTBrutto * 0.55 + tpAvdodBrutto))
            .build()

        assertEquals(10492, tpSammenstillBrutto.value)
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
        val to = Formel.constant(2)
        val tre = Formel.constant(3)

        /**
         * anonyme formler uten kjennskap til kontekst
         */
        val anonF1 = FormelBuilder.create<Int>()
            .expression(to * tre)
            .build()
        val anonF2 = FormelBuilder.create<Int>()
            .expression(tre + to)
            .build()

        /**
         * Høyere nivå kjenner kontekst og navngir formlene.
         */
        val copyAnonF1 = anonF1.emne("poengtillegg")
        val copyAnonF2 = anonF2.emne("avdod")
        val tpSumBrutto = FormelBuilder.create<Double>()
            .name("tpSum")
            .expression(copyAnonF1 * 0.5 + copyAnonF2)
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
        val a = FormelBuilder.create<Int>()
            .name("a")
            .expression(Formel.variable("x", 10) + 5)
            .build()
        val sum = FormelBuilder.create<Int>()
            .name("sum")
            .expression(a + a)
            .build()

        val sumCopy = sum.copy()
        assertEquals(1, sumCopy.subFormelList.size, "Should have one subformula in formula 'sum' after copying")
        assertTrue(sumCopy.namedVarMap.isEmpty(), "Should have no vars in formula 'sum' after copying")
    }

    @Test
    fun gamleResultatMåIkkeOverskrive() {
        val orginal = FormelBuilder.create<Double>()
            .name("hundre")
            .expression(Formel.variable("bpa", 100.0))
            .build()

        assertEquals(100.0, orginal.value)

        val tohundreOgTjue = FormelBuilder.create<Double>()
            .name("tohundreOgTjue")
            .expression(orginal + 120)
            .build()

        assertEquals(220.0, tohundreOgTjue.value)

        orginal.value // kall til orginal resultat skal ikke overskrive resultatet i feltet (ja dette har skjedd).

        assertEquals(220.0, tohundreOgTjue.value)
    }

    @Test
    fun beregnYtelse_BERYP_04_forhoyelseUtenNyttUft() {
        val G = Formel.variable("G", 75000)
        val påF92 = Formel.variable("PÅ_F92", 0)
        val påE91 = Formel.variable("PÅ_E91", 40)
        val SPT = Formel.variable("SPT", 1.6)
        val UFG = Formel.variable("UFG", 30)
        val YUG = Formel.variable("YUG", 30)
        val YPT = Formel.variable("YPT", 5.08)
        val påYskE91 = Formel.variable("PÅ_YSK_E91", 40)
        val påYsk = Formel.variable("PÅ_YSK", 40)
        val påYskF92 = Formel.variable("PÅ_YSK_F92", 0)

        /**
         * Uføre andelen
         */
        val e91 = FormelBuilder.create<Double>()
            .name("e91")
            .expression(0.42 * G * SPT * påE91 / 40 * UFG / 100)
            .build()
        val f92 = FormelBuilder.create<Double>()
            .name("f92")
            .expression(0.45 * G * SPT * påF92 / 40 * UFG / 100)
            .build()
        val uføreTpSumBrutto = FormelBuilder.create<Int>()
            .name("uføre")
            .expression(avrund((f92 + e91) * 1 / 12))
            .build()
        assertEquals(1260, uføreTpSumBrutto.value)
        assertEquals(2, uføreTpSumBrutto.subFormelList.size)
        assertEquals("avrund( (f92 + e91) * 1 / 12 )", uføreTpSumBrutto.notasjon)
        assertEquals("avrund( (0.0 + 15120.0) * 1 / 12 )", uføreTpSumBrutto.innhold)

        /**
         * Restandel Uføre
         */
        val upF92 = FormelBuilder.create<Double>()
            .name("up_f92")
            .expression(0.45 * G * SPT * påF92 / 40 * (UFG - YUG) / 100)
            .build()
        val upE91 = FormelBuilder.create<Double>()
            .name("up_e91")
            .expression(0.42 * G * SPT * påE91 / 40 * (UFG - YUG) / 100)
            .build()

        /**
         * Andel yrkesskade
         */
        val ypF92 = FormelBuilder.create<Double>()
            .name("yp_f92")
            .expression(0.45 * G * YPT * påYskF92 / påYsk * YUG / 100)
            .build()
        val ypE91 = FormelBuilder.create<Double>()
            .name("yp_e91")
            .expression(0.42 * G * YPT * påYskE91 / påYsk * YUG / 100)
            .build()

        /**
         * Sammenstill restandel uføre og andel yrkesskade
         */
        val yrkeBerTpSumBrutto = FormelBuilder.create<Int>()
            .name("ysk")
            .expression(avrund((ypF92 + ypE91) * 1 / 12) + avrund((upF92 + upE91) * 1 / 12))
            .build()
        assertEquals(4001, yrkeBerTpSumBrutto.value)
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
        val samberTpsumBrutto = FormelBuilder.create<Int>()
            .name("sum")
            .expression(uføreTpSumBrutto + yrkeBerTpSumBrutto)
            .build()
        assertEquals(2, samberTpsumBrutto.subFormelList.size)
        assertEquals("uføre + ysk", samberTpsumBrutto.notasjon)
        assertEquals("1260 + 4001", samberTpsumBrutto.innhold)
    }

    @Test
    fun toHtml() {
        val femti = Formel.constant(50)
        val formel = FormelBuilder.create<Double>()
            .prefix("TP")
            .name("eksportforbud")
            .postfix("bruttoPerAr")
            .expression(femti * 2.0)
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
        val bpa = Formel.variable("bpa", 140452.77285)
        val bpaUtb = FormelBuilder.create<Double>()
            .name("avkortet")
            .expression(afpAvrundNetto(bpa, Formel.variable("utbetalingsprosent", 57)) * 12.0)
            .build()

        assertEquals(80064.0, bpaUtb.value)
        assertEquals(140452.77285, bpa.value)
    }

    @Test
    fun `skal returnere høyeste verdi ved bruk av kMax med to Formler som input`() {
        val G = Formel.variable("G", 2000)
        val f = 100 + G

        val kMax = max(G, f)
        assertEquals(2100, kMax.value)
    }

    @Test
    fun `skal returnere høyeste verdi ved bruk av kMax med Number og Formlel som input`() {
        val G = Formel.variable("G", 2000)

        val kMax = max(3000, G)
        assertEquals(3000, kMax.value)
    }

    @Test
    fun `skal returnere lavest verdi ved bruk av kMax med to Formler som input`() {
        val G = Formel.variable("G", 2000)
        val f = 100 + G

        val kMax = min(G, f)
        assertEquals(2000, kMax.value)
    }

    @Test
    fun `skal returnere lavest verdi ved bruk av kMax med Number og Formlel som input`() {
        val G = Formel.variable("G", 2000)

        val kMax = min(3000, G)
        assertEquals(2000, kMax.value)
    }

    @Test
    fun `skal kunne utvide formel`() {
        val gp = Formel.variable("Grunnpensjon", 200.0)
        val tp = Formel.variable("Tilleggspensjon", 300.0)

        var sum = FormelBuilder.create<Int>().expression(avrund(gp + tp)).build()

        sum += Formel.variable("Minstenivåtillegg", 500)

        assertEquals(1000, sum.value)
    }

    @Test
    fun `skal feile ved ustøttet type`() {
        assertThrows<IllegalArgumentException> {
            FormelBuilder.create<Float>()
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

        tp.apKap19MedGJR = FormelBuilder.create<Int>()
            .expression(Formel.variable("MedGJR", 34000))
            .build()

        tp.apKap19UtenGJR = FormelBuilder.create<Int>()
            .expression(Formel.variable("MedGJR", 28000))
            .build()

        val uttaksgrad = Formel.variable("uttaksgrad", 50)

        tp.referansebelop = FormelBuilder.create<Int>()
            .expression(avrund((tp.apKap19MedGJR - tp.apKap19UtenGJR) * 100 / uttaksgrad))
            .build()

        assertEquals(34000, tp.apKap19MedGJR.value)
        assertEquals(28000, tp.apKap19UtenGJR.value)
        assertEquals(12000, tp.referansebelop.value)
    }
}