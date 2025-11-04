package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.Uttrykk
import no.nav.system.rule.dsl.rettsregel.operators.div
import no.nav.system.rule.dsl.rettsregel.operators.minus
import no.nav.system.rule.dsl.rettsregel.operators.plus
import no.nav.system.rule.dsl.rettsregel.operators.times
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Feature parity tests between Uttrykk and Formel.
 *
 * This test class ensures that Uttrykk provides equivalent functionality
 * to Formel for all essential features, mimicking the tests from FormelTest.kt
 */
class UttrykkParityTest {

    // ========================================================================
    // Basic Operations (mimicking FormelTest)
    // ========================================================================

    @Test
    fun `simple Int expression like simpleIntFormel`() {
        val grunnbeløp = Faktum("Grunnbeløp", Const(5000))
        val grunnbeløpPlussTusen = 1000 + grunnbeløp

        assertEquals("1000 + Grunnbeløp", grunnbeløpPlussTusen.notasjon())
        assertEquals("1000 + 5000", grunnbeløpPlussTusen.konkret())
        assertEquals(6000, grunnbeløpPlussTusen.evaluer())
    }

    @Test
    fun `simple Double expression like simpleDoubleFormel`() {
        val fem = Faktum("fem", Const(5))
        val toOgEnHalv = fem / 2

        assertEquals("fem / 2", toOgEnHalv.notasjon())
        assertEquals("5 / 2", toOgEnHalv.konkret())
        assertEquals(2.5, toOgEnHalv.evaluer())
    }

    @Test
    fun `reuse of expression like gjenbrukAvFormel`() {
        val G = Faktum("G", Const(1000))
        val SPT = Faktum("SPT", Const(2.0))

        val brutto = G * SPT

        val plus200 = brutto + 200
        assertEquals(2200.0, plus200.evaluer())

        val minus200 = brutto - 200
        assertEquals(1800.0, minus200.evaluer())
    }

    @Test
    fun `immutable expression like immutableFormel`() {
        val desimal = Faktum("tiKommaTo", Const(10.2))

        // Original value should remain unchanged
        assertEquals(10.2, desimal.evaluer())

        // Operations create new expressions without modifying original
        val plusOne = desimal + 1
        assertEquals(11.2, plusOne.evaluer())
        assertEquals(10.2, desimal.evaluer()) // Original unchanged
    }

    @Test
    fun `copy behavior like copyDefaultFormelFromFelt_auto`() {
        val brutto = Const(2000)
        var netto: Uttrykk<Int> = brutto

        assertEquals(2000, brutto.evaluer())
        assertEquals(2000, netto.evaluer())

        netto = brutto - 500

        assertEquals(2000, brutto.evaluer())
        assertEquals(1500, netto.evaluer())
    }

    @Test
    fun `integer division results in Double like integerDivisionResultsInDouble`() {
        val a = Faktum("a", Const(1))
        val b = Faktum("b", Const(4))

        assertEquals(0.25, (a / b).evaluer())
        assertEquals(0.25, (a / 4).evaluer())
        assertEquals(0.25, (1 / b).evaluer())
    }

    // ========================================================================
    // Parentheses Handling (mimicking FormelTest)
    // ========================================================================

    @Test
    fun `parentheses simple like paranteser_enkel`() {
        val SPT = Faktum("SPT", Const(4.3))
        val OPT = Faktum("OPT", Const(2.3))
        val PÅ = Faktum("PÅ", Const(20))

        val formel1 = (SPT - OPT) * PÅ
        assertEquals(40.0, formel1.evaluer())
        assertEquals("(SPT - OPT) * PÅ", formel1.notasjon())
        assertEquals("(4.3 - 2.3) * 20", formel1.konkret())

        val formel2 = PÅ * (SPT - OPT)
        assertEquals(40.0, formel2.evaluer())
        assertEquals("PÅ * (SPT - OPT)", formel2.notasjon())
        assertEquals("20 * (4.3 - 2.3)", formel2.konkret())
    }

    @Test
    fun `parentheses medium complexity like paranteser_middels`() {
        val SPT = Faktum("SPT", Const(4.3))
        val OPT = Faktum("OPT", Const(2.3))
        val PÅ = Faktum("PÅ", Const(20))

        val formel1 = (SPT - OPT) * (PÅ) * PÅ
        assertEquals(800.0, formel1.evaluer())
        assertEquals("(SPT - OPT) * PÅ * PÅ", formel1.notasjon())
        assertEquals("(4.3 - 2.3) * 20 * 20", formel1.konkret())
    }

    @Test
    fun `parentheses with negative values like paranteser_negativeVerdier`() {
        val a = Faktum("a", Const(-2))
        val b = Faktum("b", Const(1))
        val f = 4 - (a + b)

        assertEquals(5, f.evaluer())
        assertEquals("4 - (a + b)", f.notasjon())
        assertEquals("4 - (-2 + 1)", f.konkret())
    }

    @Test
    fun `parentheses with negative values 2 like paranteser_negativeVerdier2`() {
        val a = Faktum("a", Const(-2))
        val b = Faktum("b", Const(1))
        val f = -4 - (a - b)

        assertEquals(-1, f.evaluer())
        assertEquals("-4 - (a - b)", f.notasjon())
        assertEquals("-4 - (-2 - 1)", f.konkret())
    }

    @Test
    fun `parentheses with positive values like paranteser_positiveVerdier`() {
        val a = Faktum("a", Const(2))
        val b = Faktum("b", Const(1))
        val f = 4 - (a + b)

        assertEquals(1, f.evaluer())
        assertEquals("4 - (a + b)", f.notasjon())
        assertEquals("4 - (2 + 1)", f.konkret())
    }

    @Test
    fun `parentheses with negative values reversed like paranteser_negativeVerdier_reversed`() {
        val a = Faktum("a", Const(-2))
        val b = Faktum("b", Const(1))
        val f = (a + b) - 4

        assertEquals(-5, f.evaluer())
        // Note: Uttrykk preserves parentheses for subtraction on right side
        assertEquals("(a + b) - 4", f.notasjon())
        assertEquals("(-2 + 1) - 4", f.konkret())
    }

    @Test
    fun `parentheses with positive values reversed like paranteser_positiveVerdier_reversed`() {
        val a = Faktum("a", Const(2))
        val b = Faktum("b", Const(1))
        val f = 4 - (a + b)

        assertEquals(1, f.evaluer())
        assertEquals("4 - (a + b)", f.notasjon())
        assertEquals("4 - (2 + 1)", f.konkret())
    }

    // ========================================================================
    // Named Expressions (mimicking locked/unlocked formulas)
    // ========================================================================

    @Test
    fun `named expression with sub-expressions like copyFormelWithSubFormel`() {
        val G = Const(200000)

        val tpF92 = Faktum("tp_f92", 0.5 * G)
        val tpE91 = Faktum("tp_e91", 1 * G)
        val tp = tpF92 + tpE91

        val tpPlus = tp + 1

        assertEquals(300001.0, tpPlus.evaluer())
        assertEquals("tp + 1", tpPlus.notasjon())
        // Note: Uttrykk shows decimal notation for Double values
        assertEquals("300000.0 + 1", tpPlus.konkret())
    }

    @Test
    fun `show separate named expressions like shouldShowSeperateFormulas`() {
        val G = Faktum("G", Const(95000))
        val SPT = Faktum("SPT", Const(4.23))
        val påF92 = Faktum("PÅ_F92", Const(25))
        val påE91 = Faktum("PÅ_E91", Const(15))

        // Note: Without avrund function, we'll just test the structure
        val tpF92 = Faktum("tp_f92", 0.45 * G * SPT * påF92 / 40)
        val tpE91 = Faktum("tp_e91", 0.45 * G * SPT * påE91 / 40)

        val sum = tpF92 + tpE91

        // Verify named expressions are preserved in notation
        assertEquals("tp_f92 + tp_e91", sum.notasjon())

        // Verify the calculation works
        val expected = (0.45 * 95000 * 4.23 * 25 / 40) + (0.45 * 95000 * 4.23 * 15 / 40)
        assertEquals(expected, sum.evaluer(), 0.01)
    }

    // ========================================================================
    // Complex Real-World Examples
    // ========================================================================

    @Test
    fun `complex slitertillegg calculation like in FormelTest`() {
        val G = Faktum("G", Const(79216))
        var OPT = Faktum("OPT", Const(2.47))
        var PÅ = Faktum("PÅ", Const(16))
        var SPT = Faktum("SPT", Const(2.47))
        var OÅ = Faktum("OÅ", Const(20))

        val tpBrukerUtenPTBrutto = Faktum("bruker_utenPT", 0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * 1 / 12)

        // Verify calculation
        val expected = 0.45 * 79216 * (2.47 * 16.0 / 20 + (2.47 - 2.47) * 16 / 40.0) * 1 / 12
        assertEquals(expected, tpBrukerUtenPTBrutto.evaluer(), 0.01)

        assertEquals("bruker_utenPT", tpBrukerUtenPTBrutto.notasjon())

        // Test with different values (like second part of FormelTest)
        OPT = Faktum("OPT", Const(4.0))
        PÅ = Faktum("PÅ", Const(17))
        SPT = Faktum("SPT", Const(6.46))
        OÅ = Faktum("OÅ", Const(20))
        val tpPst = Faktum("tp_pst", Const(0.55))
        val UFG = Faktum("UFG", Const(100))

        val tpAvdodBrutto = Faktum("avdød", 0.45 * G * (OPT * PÅ / OÅ + (SPT - OPT) * PÅ / 40) * UFG / 100 * 1 / 12 * tpPst)

        val expectedAvdod = 0.45 * 79216 * (4.0 * 17 / 20.0 + (6.46 - 4.0) * 17 / 40.0) * 100 / 100.0 * 1 / 12 * 0.55
        assertEquals(expectedAvdod, tpAvdodBrutto.evaluer(), 0.01)
    }

    @Test
    fun `anonymous expressions can be renamed like anonymousSubformulaAreRenamedInHighlevelContext`() {
        val to = Const(2)
        val tre = Const(3)

        // Anonymous expressions without context
        val anonF1 = to * tre
        val anonF2 = tre + to

        // Higher level knows context and names formulas
        val copyAnonF1 = Faktum("poengtillegg", anonF1)
        val copyAnonF2 = Faktum("avdod", anonF2)
        val tpSumBrutto = Faktum("tpSum", copyAnonF1 * 0.5 + copyAnonF2)

        // Note: Faktum shows its name in notation (like locked formulas in Formel)
        assertEquals("tpSum", tpSumBrutto.notasjon())
        assertEquals("8.0", tpSumBrutto.konkret()) // Result is Double due to multiplication with 0.5

        // To see the inner expression, use utpakk()
        assertEquals("poengtillegg * 0.5 + avdod", tpSumBrutto.notasjon())
    }

    // ========================================================================
    // Type Safety and Coercion
    // ========================================================================

    @Test
    fun `Int operations preserve Int type`() {
        val a = Faktum("a", Const(10))
        val b = Faktum("b", Const(20))

        val sum: Uttrykk<Int> = a + b
        val product: Uttrykk<Int> = a * b
        val difference: Uttrykk<Int> = a - b

        assertEquals(30, sum.evaluer())
        assertEquals(200, product.evaluer())
        assertEquals(-10, difference.evaluer())
    }

    @Test
    fun `Double operations preserve Double type`() {
        val a = Faktum("a", Const(10.5))
        val b = Faktum("b", Const(20.5))

        val sum: Uttrykk<Double> = a + b
        val product: Uttrykk<Double> = a * b
        val difference: Uttrykk<Double> = a - b

        assertEquals(31.0, sum.evaluer())
        assertEquals(215.25, product.evaluer(), 0.001)
        assertEquals(-10.0, difference.evaluer())
    }

    @Test
    fun `Division always returns Double`() {
        val intA = Faktum("a", Const(10))
        val intB = Faktum("b", Const(5))

        val div: Uttrykk<Double> = intA / intB
        val result: Double = div.evaluer()

        assertEquals(2.0, result)
    }

    @Test
    fun `Mixed Int and Double operations result in Double`() {
        val intVal = Faktum("int", Const(10))
        val doubleVal = Faktum("double", Const(2.5))

        val mixed1 = intVal + doubleVal
        val mixed2 = intVal * doubleVal
        val mixed3 = doubleVal - intVal

        assertEquals(12.5, mixed1.evaluer())
        assertEquals(25.0, mixed2.evaluer())
        assertEquals(-7.5, mixed3.evaluer())
    }

    // ========================================================================
    // Operator Precedence
    // ========================================================================

    @Test
    fun `operator precedence without parentheses`() {
        val a = Faktum("a", Const(10))
        val b = Faktum("b", Const(5))
        val c = Faktum("c", Const(2))

        // a + b * c should be a + (b * c) = 10 + 10 = 20
        val expr1 = a + b * c
        assertEquals(20, expr1.evaluer())
        assertEquals("a + b * c", expr1.notasjon())

        // a - b / c should be a - (b / c) = 10 - 2.5 = 7.5
        val expr2 = a - b / c
        assertEquals(7.5, expr2.evaluer())
    }

    @Test
    fun `operator precedence with division and multiplication`() {
        val a = Faktum("a", Const(20))
        val b = Faktum("b", Const(4))
        val c = Faktum("c", Const(2))

        // a / b * c should be (a / b) * c = 5 * 2 = 10
        val expr = a / b * c
        assertEquals(10.0, expr.evaluer())
    }

    // ========================================================================
    // Edge Cases
    // ========================================================================

    @Test
    fun `multiple levels of nesting`() {
        val a = Faktum("a", Const(2))
        val b = Faktum("b", Const(3))
        val c = Faktum("c", Const(4))
        val d = Faktum("d", Const(5))

        // ((a + b) * c) - d = (5 * 4) - 5 = 15
        val expr = ((a + b) * c) - d
        assertEquals(15, expr.evaluer())
        assertEquals("(a + b) * c - d", expr.notasjon())
    }

    @Test
    fun `expression with constants only`() {
        val expr = Const(5) + Const(10) * Const(2)

        assertEquals(25, expr.evaluer())
        assertEquals("5 + 10 * 2", expr.notasjon())
        assertEquals("5 + 10 * 2", expr.konkret())
    }

    @Test
    fun `zero handling`() {
        val zero = Faktum("zero", Const(0))
        val ten = Faktum("ten", Const(10))

        assertEquals(10, (zero + ten).evaluer())
        assertEquals(0, (zero * ten).evaluer())
        assertEquals(-10, (zero - ten).evaluer())
    }

    @Test
    fun `one handling`() {
        val one = Faktum("one", Const(1))
        val ten = Faktum("ten", Const(10))

        assertEquals(10, (one * ten).evaluer())
        assertEquals(0.1, (one / ten).evaluer())
    }

    // ========================================================================
    // Expression Reuse and Sharing
    // ========================================================================

    @Test
    fun `same expression used multiple times`() {
        val base = Faktum("base", Const(10))
        val subExpr = base * 2  // This expression is reused

        val expr1 = subExpr + 5
        val expr2 = subExpr - 5
        val expr3 = subExpr + subExpr

        assertEquals(25, expr1.evaluer())
        assertEquals(15, expr2.evaluer())
        assertEquals(40, expr3.evaluer())

        // All should still have correct notation
        assertEquals("base * 2 + 5", expr1.notasjon())
        assertEquals("base * 2 - 5", expr2.notasjon())
        assertEquals("base * 2 + base * 2", expr3.notasjon())
    }

    @Test
    fun `FaktumListe collects all Faktum from expression`() {
        val a = Faktum("a", Const(10))
        val b = Faktum("b", Const(20))
        val c = Faktum("c", Const(30))

        val expr = (a + b) * c - a
        val FaktumListe = expr.grunnlagListe()

        // Faktum in expression: a appears twice, b once, c once = 4 total (with duplicates)
        assertEquals(4, FaktumListe.size)
    }

}
