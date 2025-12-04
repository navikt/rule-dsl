package no.nav.system.ruledsl.core.rettsregel


import no.nav.system.ruledsl.core.rettsregel.operators.div
import no.nav.system.ruledsl.core.rettsregel.operators.minus
import no.nav.system.ruledsl.core.rettsregel.operators.plus
import no.nav.system.ruledsl.core.rettsregel.operators.times
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Comprehensive operator tests for Uttrykk, mimicking OperatorTest.kt for Formel.
 *
 * Tests all combinations of operators with:
 * - Uttrykk + Uttrykk
 * - Uttrykk + Number
 * - Number + Uttrykk
 * - Faktum + Faktum
 * - Faktum + Number
 * - Number + Faktum
 * - Faktum + Uttrykk
 * - Uttrykk + Faktum
 *
 * For both Int and Double types.
 */
class UttrykkOperatorTest {

    private val intFaktum: Faktum<Int> = Faktum("int", Const(11))
    private val dblFaktum: Faktum<Double> = Faktum("dbl", Const(22.2))

    // ========================================================================
    // Plus Operators
    // ========================================================================

    @Test
    fun `plus operators with Uttrykk`() {
        val intUttrykk = intFaktum
        val dblUttrykk = dblFaktum

        // Uttrykk + Number
        val intUttrykkPlusInt: Uttrykk<Int> = intUttrykk + 1
        assertEquals(12, intUttrykkPlusInt.verdi)

        val dblUttrykkPlusDbl: Uttrykk<Double> = dblUttrykk + 2.2
        assertEquals(24.4, dblUttrykkPlusDbl.verdi)

        // Number + Uttrykk
        val intPlusIntUttrykk: Uttrykk<Int> = 1 + intUttrykk
        assertEquals(12, intPlusIntUttrykk.verdi)

        val dblPlusDblUttrykk: Uttrykk<Double> = 2.2 + dblUttrykk
        assertEquals(24.4, dblPlusDblUttrykk.verdi)

        // Mixed types
        val intUttrykkPlusDbl: Uttrykk<Double> = intUttrykk + 2.2
        assertEquals(13.2, intUttrykkPlusDbl.verdi)

        val dblUttrykkPlusInt: Uttrykk<Double> = dblUttrykk + 1
        assertEquals(23.2, dblUttrykkPlusInt.verdi)

        val intPlusDblUttrykk: Uttrykk<Double> = 1 + dblUttrykk
        assertEquals(23.2, intPlusDblUttrykk.verdi)

        val dblPlusIntUttrykk: Uttrykk<Double> = 2.2 + intUttrykk
        assertEquals(13.2, dblPlusIntUttrykk.verdi)

        // Uttrykk + Uttrykk
        val intUttrykkPlusIntUttrykk: Uttrykk<Int> = intUttrykk + intUttrykk
        assertEquals(22, intUttrykkPlusIntUttrykk.verdi)

        val dblUttrykkPlusDblUttrykk: Uttrykk<Double> = dblUttrykk + dblUttrykk
        assertEquals(44.4, dblUttrykkPlusDblUttrykk.verdi)

        val intUttrykkPlusDblUttrykk: Uttrykk<Double> = intUttrykk + dblUttrykk
        assertEquals(33.2, intUttrykkPlusDblUttrykk.verdi)

        val dblUttrykkPlusIntUttrykk: Uttrykk<Double> = dblUttrykk + intUttrykk
        assertEquals(33.2, dblUttrykkPlusIntUttrykk.verdi)
    }

    @Test
    fun `plus operators with Faktum`() {
        // Faktum + Number
        val intFaktumPlusInt: Uttrykk<Int> = intFaktum + 1
        assertEquals(12, intFaktumPlusInt.verdi)

        val dblFaktumPlusDbl: Uttrykk<Double> = dblFaktum + 2.2
        assertEquals(24.4, dblFaktumPlusDbl.verdi)

        // Number + Faktum
        val intPlusIntFaktum: Uttrykk<Int> = 1 + intFaktum
        assertEquals(12, intPlusIntFaktum.verdi)

        val dblPlusDblFaktum: Uttrykk<Double> = 2.2 + dblFaktum
        assertEquals(24.4, dblPlusDblFaktum.verdi)

        // Mixed types
        val intFaktumPlusDbl: Uttrykk<Double> = intFaktum + 2.2
        assertEquals(13.2, intFaktumPlusDbl.verdi)

        val dblFaktumPlusInt: Uttrykk<Double> = dblFaktum + 1
        assertEquals(23.2, dblFaktumPlusInt.verdi)

        val intPlusDblFaktum: Uttrykk<Double> = 1 + dblFaktum
        assertEquals(23.2, intPlusDblFaktum.verdi)

        val dblPlusIntFaktum: Uttrykk<Double> = 2.2 + intFaktum
        assertEquals(13.2, dblPlusIntFaktum.verdi)

        // Faktum + Faktum
        val intFaktumPlusIntFaktum: Uttrykk<Int> = intFaktum + intFaktum
        assertEquals(22, intFaktumPlusIntFaktum.verdi)

        val dblFaktumPlusDblFaktum: Uttrykk<Double> = dblFaktum + dblFaktum
        assertEquals(44.4, dblFaktumPlusDblFaktum.verdi)

        val intFaktumPlusDblFaktum: Uttrykk<Double> = intFaktum + dblFaktum
        assertEquals(33.2, intFaktumPlusDblFaktum.verdi)

        val dblFaktumPlusIntFaktum: Uttrykk<Double> = dblFaktum + intFaktum
        assertEquals(33.2, dblFaktumPlusIntFaktum.verdi)
    }

    @Test
    fun `plus operators mixed Faktum and Uttrykk`() {
        val intUttrykk = intFaktum
        val dblUttrykk = dblFaktum

        // Faktum + Uttrykk
        val intFaktumPlusIntUttrykk: Uttrykk<Int> = intFaktum + intUttrykk
        assertEquals(22, intFaktumPlusIntUttrykk.verdi)

        val dblFaktumPlusDblUttrykk: Uttrykk<Double> = dblFaktum + dblUttrykk
        assertEquals(44.4, dblFaktumPlusDblUttrykk.verdi)

        // Uttrykk + Faktum
        val intUttrykkPlusIntFaktum: Uttrykk<Int> = intUttrykk + intFaktum
        assertEquals(22, intUttrykkPlusIntFaktum.verdi)

        val dblUttrykkPlusDblFaktum: Uttrykk<Double> = dblUttrykk + dblFaktum
        assertEquals(44.4, dblUttrykkPlusDblFaktum.verdi)
    }

    // ========================================================================
    // Minus Operators
    // ========================================================================

    @Test
    fun `minus operators with Uttrykk`() {
        val intUttrykk = intFaktum
        val dblUttrykk = dblFaktum

        // Uttrykk - Number
        val intUttrykkMinusInt: Uttrykk<Int> = intUttrykk - 1
        assertEquals(10, intUttrykkMinusInt.verdi)

        val dblUttrykkMinusDbl: Uttrykk<Double> = dblUttrykk - 2.2
        assertEquals(20.0, dblUttrykkMinusDbl.verdi)

        // Number - Uttrykk
        val intMinusIntUttrykk: Uttrykk<Int> = 1 - intUttrykk
        assertEquals(-10, intMinusIntUttrykk.verdi)

        val dblMinusDblUttrykk: Uttrykk<Double> = 2.2 - dblUttrykk
        assertEquals(-20.0, dblMinusDblUttrykk.verdi)

        // Mixed types
        val intUttrykkMinusDbl: Uttrykk<Double> = intUttrykk - 2.2
        assertEquals(8.8, intUttrykkMinusDbl.verdi)

        val dblUttrykkMinusInt: Uttrykk<Double> = dblUttrykk - 1
        assertEquals(21.2, dblUttrykkMinusInt.verdi)

        val intMinusDblUttrykk: Uttrykk<Double> = 1 - dblUttrykk
        assertEquals(-21.2, intMinusDblUttrykk.verdi)

        val dblMinusIntUttrykk: Uttrykk<Double> = 2.2 - intUttrykk
        assertEquals(-8.8, dblMinusIntUttrykk.verdi)

        // Uttrykk - Uttrykk
        val intUttrykkMinusIntUttrykk: Uttrykk<Int> = intUttrykk - intUttrykk
        assertEquals(0, intUttrykkMinusIntUttrykk.verdi)

        val dblUttrykkMinusDblUttrykk: Uttrykk<Double> = dblUttrykk - dblUttrykk
        assertEquals(0.0, dblUttrykkMinusDblUttrykk.verdi)

        val intUttrykkMinusDblUttrykk: Uttrykk<Double> = intUttrykk - dblUttrykk
        assertEquals(-11.2, intUttrykkMinusDblUttrykk.verdi)

        val dblUttrykkMinusIntUttrykk: Uttrykk<Double> = dblUttrykk - intUttrykk
        assertEquals(11.2, dblUttrykkMinusIntUttrykk.verdi)
    }

    @Test
    fun `minus operators with Faktum`() {
        // Faktum - Number
        val intFaktumMinusInt: Uttrykk<Int> = intFaktum - 1
        assertEquals(10, intFaktumMinusInt.verdi)

        val dblFaktumMinusDbl: Uttrykk<Double> = dblFaktum - 2.2
        assertEquals(20.0, dblFaktumMinusDbl.verdi)

        // Number - Faktum
        val intMinusIntFaktum: Uttrykk<Int> = 1 - intFaktum
        assertEquals(-10, intMinusIntFaktum.verdi)

        val dblMinusDblFaktum: Uttrykk<Double> = 2.2 - dblFaktum
        assertEquals(-20.0, dblMinusDblFaktum.verdi)

        // Mixed types
        val intFaktumMinusDbl: Uttrykk<Double> = intFaktum - 2.2
        assertEquals(8.8, intFaktumMinusDbl.verdi)

        val dblFaktumMinusInt: Uttrykk<Double> = dblFaktum - 1
        assertEquals(21.2, dblFaktumMinusInt.verdi)

        val intMinusDblFaktum: Uttrykk<Double> = 1 - dblFaktum
        assertEquals(-21.2, intMinusDblFaktum.verdi)

        val dblMinusIntFaktum: Uttrykk<Double> = 2.2 - intFaktum
        assertEquals(-8.8, dblMinusIntFaktum.verdi)

        // Faktum - Faktum
        val intFaktumMinusIntFaktum: Uttrykk<Int> = intFaktum - intFaktum
        assertEquals(0, intFaktumMinusIntFaktum.verdi)

        val dblFaktumMinusDblFaktum: Uttrykk<Double> = dblFaktum - dblFaktum
        assertEquals(0.0, dblFaktumMinusDblFaktum.verdi)

        val intFaktumMinusDblFaktum: Uttrykk<Double> = intFaktum - dblFaktum
        assertEquals(-11.2, intFaktumMinusDblFaktum.verdi)

        val dblFaktumMinusIntFaktum: Uttrykk<Double> = dblFaktum - intFaktum
        assertEquals(11.2, dblFaktumMinusIntFaktum.verdi)
    }

    // ========================================================================
    // Times Operators
    // ========================================================================

    @Test
    fun `times operators with Uttrykk`() {
        val intUttrykk = intFaktum
        val dblUttrykk = dblFaktum

        // Uttrykk * Number
        val intUttrykkTimesInt: Uttrykk<Int> = intUttrykk * 2
        assertEquals(22, intUttrykkTimesInt.verdi)

        val dblUttrykkTimesDbl: Uttrykk<Double> = dblUttrykk * 2.2
        assertEquals(48.84, dblUttrykkTimesDbl.verdi)

        // Number * Uttrykk
        val intTimesIntUttrykk: Uttrykk<Int> = 2 * intUttrykk
        assertEquals(22, intTimesIntUttrykk.verdi)

        val dblTimesDblUttrykk: Uttrykk<Double> = 2.2 * dblUttrykk
        assertEquals(48.84, dblTimesDblUttrykk.verdi)

        val intUttrykkTimesDbl: Uttrykk<Double> = intUttrykk * 2.2
        assertEquals(24.20, intUttrykkTimesDbl.verdi, 0.001)

        val dblUttrykkTimesInt: Uttrykk<Double> = dblUttrykk * 2
        assertEquals(44.4, dblUttrykkTimesInt.verdi)

        val intTimesDblUttrykk: Uttrykk<Double> = 2 * dblUttrykk
        assertEquals(44.4, intTimesDblUttrykk.verdi)

        val dblTimesIntUttrykk: Uttrykk<Double> = 2.2 * intUttrykk
        assertEquals(24.20, dblTimesIntUttrykk.verdi, 0.001)

        // Uttrykk * Uttrykk
        val intUttrykkTimesIntUttrykk: Uttrykk<Int> = intUttrykk * intUttrykk
        assertEquals(121, intUttrykkTimesIntUttrykk.verdi)

        val dblUttrykkTimesDblUttrykk: Uttrykk<Double> = dblUttrykk * dblUttrykk
        assertEquals(492.84, dblUttrykkTimesDblUttrykk.verdi)

        val intUttrykkTimesDblUttrykk: Uttrykk<Double> = intUttrykk * dblUttrykk
        assertEquals(244.2, intUttrykkTimesDblUttrykk.verdi)

        val dblUttrykkTimesIntUttrykk: Uttrykk<Double> = dblUttrykk * intUttrykk
        assertEquals(244.2, dblUttrykkTimesIntUttrykk.verdi)
    }

    @Test
    fun `times operators with Faktum`() {
        // Faktum * Number
        val intFaktumTimesInt: Uttrykk<Int> = intFaktum * 2
        assertEquals(22, intFaktumTimesInt.verdi)

        val dblFaktumTimesDbl: Uttrykk<Double> = dblFaktum * 2.2
        assertEquals(48.84, dblFaktumTimesDbl.verdi)

        // Number * Faktum
        val intTimesIntFaktum: Uttrykk<Int> = 2 * intFaktum
        assertEquals(22, intTimesIntFaktum.verdi)

        val dblTimesDblFaktum: Uttrykk<Double> = 2.2 * dblFaktum
        assertEquals(48.84, dblTimesDblFaktum.verdi)

        // Mixed types (Int * Double coerced to Int truncates to 24)
        val intFaktumTimesDbl: Uttrykk<Double> = intFaktum * 2.2
        assertEquals(24.20, intFaktumTimesDbl.verdi, 0.001)

        val dblFaktumTimesInt: Uttrykk<Double> = dblFaktum * 2
        assertEquals(44.4, dblFaktumTimesInt.verdi)

        val intTimesDblFaktum: Uttrykk<Double> = 2 * dblFaktum
        assertEquals(44.4, intTimesDblFaktum.verdi)

        val dblTimesIntFaktum: Uttrykk<Double> = 2.2 * intFaktum
        assertEquals(24.20, dblTimesIntFaktum.verdi, 0.001)

        // Faktum * Faktum
        val intFaktumTimesIntFaktum: Uttrykk<Int> = intFaktum * intFaktum
        assertEquals(121, intFaktumTimesIntFaktum.verdi)

        val dblFaktumTimesDblFaktum: Uttrykk<Double> = dblFaktum * dblFaktum
        assertEquals(492.84, dblFaktumTimesDblFaktum.verdi)

        val intFaktumTimesDblFaktum: Uttrykk<Double> = intFaktum * dblFaktum
        assertEquals(244.2, intFaktumTimesDblFaktum.verdi)

        val dblFaktumTimesIntFaktum: Uttrykk<Double> = dblFaktum * intFaktum
        assertEquals(244.2, dblFaktumTimesIntFaktum.verdi)
    }

    // ========================================================================
    // Division Operators (always returns Double)
    // ========================================================================

    @Test
    fun `division operators with Uttrykk`() {
        val intUttrykk = intFaktum
        val dblUttrykk = dblFaktum

        // Uttrykk / Number
        val intUttrykkDivInt: Uttrykk<Double> = intUttrykk / 2
        assertEquals(5.5, intUttrykkDivInt.verdi)

        val dblUttrykkDivDbl: Uttrykk<Double> = dblUttrykk / 2.2
        assertEquals(10.0909, dblUttrykkDivDbl.verdi, 0.0001)

        // Number / Uttrykk
        val intDivIntUttrykk: Uttrykk<Double> = 20 / intUttrykk
        assertEquals(1.8181, intDivIntUttrykk.verdi, 0.0001)

        val dblDivDblUttrykk: Uttrykk<Double> = 48.4 / dblUttrykk
        assertEquals(2.1801, dblDivDblUttrykk.verdi, 0.0001)

        // Mixed types
        val intUttrykkDivDbl: Uttrykk<Double> = intUttrykk / 2.2
        assertEquals(5.0, intUttrykkDivDbl.verdi)

        val dblUttrykkDivInt: Uttrykk<Double> = dblUttrykk / 2
        assertEquals(11.1, dblUttrykkDivInt.verdi)

        val intDivDblUttrykk: Uttrykk<Double> = 22 / dblUttrykk
        assertEquals(0.9909, intDivDblUttrykk.verdi, 0.0001)

        val dblDivIntUttrykk: Uttrykk<Double> = 44.0 / intUttrykk
        assertEquals(4.0, dblDivIntUttrykk.verdi)

        // Uttrykk / Uttrykk
        val intUttrykkDivIntUttrykk: Uttrykk<Double> = intUttrykk / intUttrykk
        assertEquals(1.0, intUttrykkDivIntUttrykk.verdi)

        val dblUttrykkDivDblUttrykk: Uttrykk<Double> = dblUttrykk / dblUttrykk
        assertEquals(1.0, dblUttrykkDivDblUttrykk.verdi)

        val intUttrykkDivDblUttrykk: Uttrykk<Double> = intUttrykk / dblUttrykk
        assertEquals(0.4954, intUttrykkDivDblUttrykk.verdi, 0.0001)

        val dblUttrykkDivIntUttrykk: Uttrykk<Double> = dblUttrykk / intUttrykk
        assertEquals(2.0181, dblUttrykkDivIntUttrykk.verdi, 0.0001)
    }

    @Test
    fun `division operators with Faktum`() {
        // Faktum / Number
        val intFaktumDivInt: Uttrykk<Double> = intFaktum / 2
        assertEquals(5.5, intFaktumDivInt.verdi)

        val dblFaktumDivDbl: Uttrykk<Double> = dblFaktum / 2.2
        assertEquals(10.0909, dblFaktumDivDbl.verdi, 0.0001)

        // Number / Faktum
        val intDivIntFaktum: Uttrykk<Double> = 20 / intFaktum
        assertEquals(1.8181, intDivIntFaktum.verdi, 0.0001)

        val dblDivDblFaktum: Uttrykk<Double> = 48.4 / dblFaktum
        assertEquals(2.1801, dblDivDblFaktum.verdi, 0.0001)

        // Mixed types
        val intFaktumDivDbl: Uttrykk<Double> = intFaktum / 2.2
        assertEquals(5.0, intFaktumDivDbl.verdi)

        val dblFaktumDivInt: Uttrykk<Double> = dblFaktum / 2
        assertEquals(11.1, dblFaktumDivInt.verdi)

        val intDivDblFaktum: Uttrykk<Double> = 22 / dblFaktum
        assertEquals(0.9909, intDivDblFaktum.verdi, 0.0001)

        val dblDivIntFaktum: Uttrykk<Double> = 44.0 / intFaktum
        assertEquals(4.0, dblDivIntFaktum.verdi)

        // Faktum / Faktum
        val intFaktumDivIntFaktum: Uttrykk<Double> = intFaktum / intFaktum
        assertEquals(1.0, intFaktumDivIntFaktum.verdi)

        val dblFaktumDivDblFaktum: Uttrykk<Double> = dblFaktum / dblFaktum
        assertEquals(1.0, dblFaktumDivDblFaktum.verdi)

        val intFaktumDivDblFaktum: Uttrykk<Double> = intFaktum / dblFaktum
        assertEquals(0.4954, intFaktumDivDblFaktum.verdi, 0.0001)

        val dblFaktumDivIntFaktum: Uttrykk<Double> = dblFaktum / intFaktum
        assertEquals(2.0181, dblFaktumDivIntFaktum.verdi, 0.0001)
    }

    // ========================================================================
    // Notation and Concrete String Tests
    // ========================================================================

    @Test
    fun `notation shows variable names`() {
        val a = Faktum("alpha", Const(10))
        val b = Faktum("beta", Const(20))

        assertEquals("alpha + beta", (a + b).notasjon())
        assertEquals("alpha - beta", (a - b).notasjon())
        assertEquals("alpha * beta", (a * b).notasjon())
        assertEquals("alpha / beta", (a / b).notasjon())
    }

    @Test
    fun `konkret shows values`() {
        val a = Faktum("alpha", Const(10))
        val b = Faktum("beta", Const(20))

        assertEquals("10 + 20", (a + b).konkret())
        assertEquals("10 - 20", (a - b).konkret())
        assertEquals("10 * 20", (a * b).konkret())
        assertEquals("10 / 20", (a / b).konkret())
    }

    @Test
    fun `notation with constants shows values`() {
        val a = Faktum("alpha", Const(10))

        assertEquals("alpha + 5", (a + 5).notasjon())
        assertEquals("5 + alpha", (5 + a).notasjon())
        assertEquals("alpha * 2", (a * 2).notasjon())
        assertEquals("2 * alpha", (2 * a).notasjon())
    }

    @Test
    fun `complex expression notation`() {
        val G = Faktum("G", Const(100000))
        val sats = Faktum("sats", Const(0.45))
        val måneder = Faktum("måneder", Const(12))

        val expr = sats * G / måneder

        assertEquals("sats * G / måneder", expr.notasjon())
        assertEquals("0.45 * 100000 / 12", expr.konkret())
        assertEquals(3750.0, expr.verdi)
    }

    // ========================================================================
    // Operator Precedence and Parentheses Tests
    // ========================================================================

    @Test
    fun `operator precedence - multiplication before addition`() {
        val a = Faktum("a", Const(2))
        val b = Faktum("b", Const(3))
        val c = Faktum("c", Const(4))

        // 2 + 3 * 4 = 2 + 12 = 14 (not 20)
        val expr = a + b * c
        assertEquals(14, expr.verdi)
        assertEquals("a + b * c", expr.notasjon())
        assertEquals("2 + 3 * 4", expr.konkret())
    }

    @Test
    fun `operator precedence - division before subtraction`() {
        val a = Faktum("a", Const(20))
        val b = Faktum("b", Const(8))
        val c = Faktum("c", Const(2))

        // 20 - 8 / 2 = 20 - 4 = 16 (not 6)
        val expr = a - b / c
        assertEquals(16.0, expr.verdi)
        assertEquals("a - b / c", expr.notasjon())
        assertEquals("20 - 8 / 2", expr.konkret())
    }

    @Test
    fun `parentheses override precedence - addition before multiplication`() {
        val a = Faktum("a", Const(2))
        val b = Faktum("b", Const(3))
        val c = Faktum("c", Const(4))

        // (2 + 3) * 4 = 5 * 4 = 20 (not 14)
        val expr = (a + b) * c
        assertEquals(20, expr.verdi)
        assertEquals("(a + b) * c", expr.notasjon())
        assertEquals("(2 + 3) * 4", expr.konkret())
    }

    @Test
    fun `parentheses override precedence - subtraction before division`() {
        val a = Faktum("a", Const(20))
        val b = Faktum("b", Const(8))
        val c = Faktum("c", Const(2))

        // (20 - 8) / 2 = 12 / 2 = 6 (not 16)
        val expr = (a - b) / c
        assertEquals(6.0, expr.verdi)
        assertEquals("(a - b) / c", expr.notasjon())
        assertEquals("(20 - 8) / 2", expr.konkret())
    }

    @Test
    fun `left-to-right associativity for subtraction`() {
        val a = Faktum("a", Const(10))
        val b = Faktum("b", Const(3))
        val c = Faktum("c", Const(2))

        // 10 - 3 - 2 = (10 - 3) - 2 = 7 - 2 = 5
        // NOT 10 - (3 - 2) = 10 - 1 = 9
        val expr = a - b - c
        assertEquals(5, expr.verdi)
        assertEquals("a - b - c", expr.notasjon())
        assertEquals("10 - 3 - 2", expr.konkret())
    }

    @Test
    fun `left-to-right associativity for division`() {
        val a = Faktum("a", Const(20))
        val b = Faktum("b", Const(4))
        val c = Faktum("c", Const(2))

        // 20 / 4 / 2 = (20 / 4) / 2 = 5 / 2 = 2.5
        // NOT 20 / (4 / 2) = 20 / 2 = 10
        val expr = a / b / c
        assertEquals(2.5, expr.verdi)
        assertEquals("a / b / c", expr.notasjon())
        assertEquals("20 / 4 / 2", expr.konkret())
    }

    @Test
    fun `nested parentheses with addition and multiplication`() {
        val a = Faktum("a", Const(1))
        val b = Faktum("b", Const(2))
        val c = Faktum("c", Const(3))
        val d = Faktum("d", Const(4))

        // (1 + 2) * (3 + 4) = 3 * 7 = 21
        val expr = (a + b) * (c + d)
        assertEquals(21, expr.verdi)
        assertEquals("(a + b) * (c + d)", expr.notasjon())
        assertEquals("(1 + 2) * (3 + 4)", expr.konkret())
    }

    @Test
    fun `parentheses on right side of subtraction`() {
        val a = Faktum("a", Const(10))
        val b = Faktum("b", Const(2))
        val c = Faktum("c", Const(3))

        // 10 - (2 + 3) = 10 - 5 = 5
        // NOT 10 - 2 + 3 = 11
        val expr = a - (b + c)
        assertEquals(5, expr.verdi)
        assertEquals("a - (b + c)", expr.notasjon())
        assertEquals("10 - (2 + 3)", expr.konkret())
    }

    @Test
    fun `parentheses on right side of division`() {
        val a = Faktum("a", Const(20))
        val b = Faktum("b", Const(2))
        val c = Faktum("c", Const(3))

        // 20 / (2 + 3) = 20 / 5 = 4
        // NOT 20 / 2 + 3 = 13
        val expr = a / (b + c)
        assertEquals(4.0, expr.verdi)
        assertEquals("a / (b + c)", expr.notasjon())
        assertEquals("20 / (2 + 3)", expr.konkret())
    }

    @Test
    fun `mixed precedence - multiplication and division left to right`() {
        val a = Faktum("a", Const(12))
        val b = Faktum("b", Const(3))
        val c = Faktum("c", Const(2))

        // 12 * 3 / 2 = (12 * 3) / 2 = 36 / 2 = 18
        val expr = a * b / c
        assertEquals(18.0, expr.verdi)
        assertEquals("a * b / c", expr.notasjon())
        assertEquals("12 * 3 / 2", expr.konkret())
    }

    @Test
    fun `mixed precedence - division and multiplication left to right`() {
        val a = Faktum("a", Const(12))
        val b = Faktum("b", Const(3))
        val c = Faktum("c", Const(2))

        // 12 / 3 * 2 = (12 / 3) * 2 = 4 * 2 = 8
        val expr = a / b * c
        assertEquals(8.0, expr.verdi)
        assertEquals("a / b * c", expr.notasjon())
        assertEquals("12 / 3 * 2", expr.konkret())
    }

    @Test
    fun `complex expression with multiple operators`() {
        val a = Faktum("a", Const(5))
        val b = Faktum("b", Const(3))
        val c = Faktum("c", Const(2))
        val d = Faktum("d", Const(4))

        // 5 + 3 * 2 - 4 = 5 + 6 - 4 = 7
        val expr = a + b * c - d
        assertEquals(7, expr.verdi)
        assertEquals("a + b * c - d", expr.notasjon())
        assertEquals("5 + 3 * 2 - 4", expr.konkret())
    }

    @Test
    fun `no unnecessary parentheses for same precedence operations`() {
        val a = Faktum("a", Const(10))
        val b = Faktum("b", Const(5))
        val c = Faktum("c", Const(3))

        // a + b - c should not add parentheses (same precedence, left-to-right)
        val expr = a + b - c
        assertEquals(12, expr.verdi)
        assertEquals("a + b - c", expr.notasjon())
        assertEquals("10 + 5 - 3", expr.konkret())
    }

    @Test
    fun `multiplication with subtraction on left requires parentheses`() {
        val a = Faktum("a", Const(5))
        val b = Faktum("b", Const(3))
        val c = Faktum("c", Const(2))

        // (5 - 3) * 2 = 2 * 2 = 4
        val expr = (a - b) * c
        assertEquals(4, expr.verdi)
        assertEquals("(a - b) * c", expr.notasjon())
        assertEquals("(5 - 3) * 2", expr.konkret())
    }

    @Test
    fun `multiplication with subtraction on right requires parentheses`() {
        val a = Faktum("a", Const(2))
        val b = Faktum("b", Const(5))
        val c = Faktum("c", Const(3))

        // 2 * (5 - 3) = 2 * 2 = 4
        val expr = a * (b - c)
        assertEquals(4, expr.verdi)
        assertEquals("a * (b - c)", expr.notasjon())
        assertEquals("2 * (5 - 3)", expr.konkret())
    }

    @Test
    fun `division with addition on right requires parentheses`() {
        val a = Faktum("a", Const(12))
        val b = Faktum("b", Const(2))
        val c = Faktum("c", Const(4))

        // 12 / (2 + 4) = 12 / 6 = 2
        val expr = a / (b + c)
        assertEquals(2.0, expr.verdi)
        assertEquals("a / (b + c)", expr.notasjon())
        assertEquals("12 / (2 + 4)", expr.konkret())
    }
}
