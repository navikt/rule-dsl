package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*
import no.nav.system.rule.dsl.rettsregel.Faktum
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

    private val intFaktum: Faktum<Int> = Faktum("int", 11)
    private val dblFaktum: Faktum<Double> = Faktum("dbl", 22.2)

    // ========================================================================
    // Plus Operators
    // ========================================================================

    @Test
    fun `plus operators with Uttrykk`() {
        val intUttrykk = Var(intFaktum)
        val dblUttrykk = Var(dblFaktum)

        // Uttrykk + Number
        val intUttrykkPlusInt: Add<Int> = intUttrykk + 1
        assertEquals(12, intUttrykkPlusInt.evaluer())

        val dblUttrykkPlusDbl: Add<Double> = dblUttrykk + 2.2
        assertEquals(24.4, dblUttrykkPlusDbl.evaluer())

        // Number + Uttrykk
        val intPlusIntUttrykk: Add<Int> = 1 + intUttrykk
        assertEquals(12, intPlusIntUttrykk.evaluer())

        val dblPlusDblUttrykk: Add<Double> = 2.2 + dblUttrykk
        assertEquals(24.4, dblPlusDblUttrykk.evaluer())

        // Mixed types
        val intUttrykkPlusDbl: Add<Int> = intUttrykk + 2.2
        assertEquals(13.2, intUttrykkPlusDbl.evaluer())

        val dblUttrykkPlusInt: Add<Double> = dblUttrykk + 1
        assertEquals(23.2, dblUttrykkPlusInt.evaluer())

        val intPlusDblUttrykk: Add<Double> = 1 + dblUttrykk
        assertEquals(23.2, intPlusDblUttrykk.evaluer())

        val dblPlusIntUttrykk: Add<Int> = 2.2 + intUttrykk
        assertEquals(13.2, dblPlusIntUttrykk.evaluer())

        // Uttrykk + Uttrykk
        val intUttrykkPlusIntUttrykk: Add<Int> = intUttrykk + intUttrykk
        assertEquals(22, intUttrykkPlusIntUttrykk.evaluer())

        val dblUttrykkPlusDblUttrykk: Add<Double> = dblUttrykk + dblUttrykk
        assertEquals(44.4, dblUttrykkPlusDblUttrykk.evaluer())

        val intUttrykkPlusDblUttrykk: Add<Int> = intUttrykk + dblUttrykk
        assertEquals(33.2, intUttrykkPlusDblUttrykk.evaluer())

        val dblUttrykkPlusIntUttrykk: Add<Double> = dblUttrykk + intUttrykk
        assertEquals(33.2, dblUttrykkPlusIntUttrykk.evaluer())
    }

    @Test
    fun `plus operators with Faktum`() {
        // Faktum + Number
        val intFaktumPlusInt: Add<Int> = intFaktum + 1
        assertEquals(12, intFaktumPlusInt.evaluer())

        val dblFaktumPlusDbl: Add<Double> = dblFaktum + 2.2
        assertEquals(24.4, dblFaktumPlusDbl.evaluer())

        // Number + Faktum
        val intPlusIntFaktum: Add<Int> = 1 + intFaktum
        assertEquals(12, intPlusIntFaktum.evaluer())

        val dblPlusDblFaktum: Add<Double> = 2.2 + dblFaktum
        assertEquals(24.4, dblPlusDblFaktum.evaluer())

        // Mixed types
        val intFaktumPlusDbl: Add<Int> = intFaktum + 2.2
        assertEquals(13.2, intFaktumPlusDbl.evaluer())

        val dblFaktumPlusInt: Add<Double> = dblFaktum + 1
        assertEquals(23.2, dblFaktumPlusInt.evaluer())

        val intPlusDblFaktum: Add<Double> = 1 + dblFaktum
        assertEquals(23.2, intPlusDblFaktum.evaluer())

        val dblPlusIntFaktum: Add<Int> = 2.2 + intFaktum
        assertEquals(13.2, dblPlusIntFaktum.evaluer())

        // Faktum + Faktum
        val intFaktumPlusIntFaktum: Add<Int> = intFaktum + intFaktum
        assertEquals(22, intFaktumPlusIntFaktum.evaluer())

        val dblFaktumPlusDblFaktum: Add<Double> = dblFaktum + dblFaktum
        assertEquals(44.4, dblFaktumPlusDblFaktum.evaluer())

        val intFaktumPlusDblFaktum: Add<Int> = intFaktum + dblFaktum
        assertEquals(33.2, intFaktumPlusDblFaktum.evaluer())

        val dblFaktumPlusIntFaktum: Add<Double> = dblFaktum + intFaktum
        assertEquals(33.2, dblFaktumPlusIntFaktum.evaluer())
    }

    @Test
    fun `plus operators mixed Faktum and Uttrykk`() {
        val intUttrykk = Var(intFaktum)
        val dblUttrykk = Var(dblFaktum)

        // Faktum + Uttrykk
        val intFaktumPlusIntUttrykk: Add<Int> = intFaktum + intUttrykk
        assertEquals(22, intFaktumPlusIntUttrykk.evaluer())

        val dblFaktumPlusDblUttrykk: Add<Double> = dblFaktum + dblUttrykk
        assertEquals(44.4, dblFaktumPlusDblUttrykk.evaluer())

        // Uttrykk + Faktum
        val intUttrykkPlusIntFaktum: Add<Int> = intUttrykk + intFaktum
        assertEquals(22, intUttrykkPlusIntFaktum.evaluer())

        val dblUttrykkPlusDblFaktum: Add<Double> = dblUttrykk + dblFaktum
        assertEquals(44.4, dblUttrykkPlusDblFaktum.evaluer())
    }

    // ========================================================================
    // Minus Operators
    // ========================================================================

    @Test
    fun `minus operators with Uttrykk`() {
        val intUttrykk = Var(intFaktum)
        val dblUttrykk = Var(dblFaktum)

        // Uttrykk - Number
        val intUttrykkMinusInt: Sub<Int> = intUttrykk - 1
        assertEquals(10, intUttrykkMinusInt.evaluer())

        val dblUttrykkMinusDbl: Sub<Double> = dblUttrykk - 2.2
        assertEquals(20.0, dblUttrykkMinusDbl.evaluer())

        // Number - Uttrykk
        val intMinusIntUttrykk: Sub<Int> = 1 - intUttrykk
        assertEquals(-10, intMinusIntUttrykk.evaluer())

        val dblMinusDblUttrykk: Sub<Double> = 2.2 - dblUttrykk
        assertEquals(-20.0, dblMinusDblUttrykk.evaluer())

        // Mixed types
        val intUttrykkMinusDbl: Sub<Int> = intUttrykk - 2.2
        assertEquals(8.8, intUttrykkMinusDbl.evaluer())

        val dblUttrykkMinusInt: Sub<Double> = dblUttrykk - 1
        assertEquals(21.2, dblUttrykkMinusInt.evaluer())

        val intMinusDblUttrykk: Sub<Double> = 1 - dblUttrykk
        assertEquals(-21.2, intMinusDblUttrykk.evaluer())

        val dblMinusIntUttrykk: Sub<Int> = 2.2 - intUttrykk
        assertEquals(-8.8, dblMinusIntUttrykk.evaluer())

        // Uttrykk - Uttrykk
        val intUttrykkMinusIntUttrykk: Sub<Int> = intUttrykk - intUttrykk
        assertEquals(0, intUttrykkMinusIntUttrykk.evaluer())

        val dblUttrykkMinusDblUttrykk: Sub<Double> = dblUttrykk - dblUttrykk
        assertEquals(0.0, dblUttrykkMinusDblUttrykk.evaluer())

        val intUttrykkMinusDblUttrykk: Sub<Int> = intUttrykk - dblUttrykk
        assertEquals(-11.2, intUttrykkMinusDblUttrykk.evaluer())

        val dblUttrykkMinusIntUttrykk: Sub<Double> = dblUttrykk - intUttrykk
        assertEquals(11.2, dblUttrykkMinusIntUttrykk.evaluer())
    }

    @Test
    fun `minus operators with Faktum`() {
        // Faktum - Number
        val intFaktumMinusInt: Sub<Int> = intFaktum - 1
        assertEquals(10, intFaktumMinusInt.evaluer())

        val dblFaktumMinusDbl: Sub<Double> = dblFaktum - 2.2
        assertEquals(20.0, dblFaktumMinusDbl.evaluer())

        // Number - Faktum
        val intMinusIntFaktum: Sub<Int> = 1 - intFaktum
        assertEquals(-10, intMinusIntFaktum.evaluer())

        val dblMinusDblFaktum: Sub<Double> = 2.2 - dblFaktum
        assertEquals(-20.0, dblMinusDblFaktum.evaluer())

        // Mixed types
        val intFaktumMinusDbl: Sub<Int> = intFaktum - 2.2
        assertEquals(8.8, intFaktumMinusDbl.evaluer())

        val dblFaktumMinusInt: Sub<Double> = dblFaktum - 1
        assertEquals(21.2, dblFaktumMinusInt.evaluer())

        val intMinusDblFaktum: Sub<Double> = 1 - dblFaktum
        assertEquals(-21.2, intMinusDblFaktum.evaluer())

        val dblMinusIntFaktum: Sub<Int> = 2.2 - intFaktum
        assertEquals(-8.8, dblMinusIntFaktum.evaluer())

        // Faktum - Faktum
        val intFaktumMinusIntFaktum: Sub<Int> = intFaktum - intFaktum
        assertEquals(0, intFaktumMinusIntFaktum.evaluer())

        val dblFaktumMinusDblFaktum: Sub<Double> = dblFaktum - dblFaktum
        assertEquals(0.0, dblFaktumMinusDblFaktum.evaluer())

        val intFaktumMinusDblFaktum: Sub<Int> = intFaktum - dblFaktum
        assertEquals(-11.2, intFaktumMinusDblFaktum.evaluer())

        val dblFaktumMinusIntFaktum: Sub<Double> = dblFaktum - intFaktum
        assertEquals(11.2, dblFaktumMinusIntFaktum.evaluer())
    }

    // ========================================================================
    // Times Operators
    // ========================================================================

    @Test
    fun `times operators with Uttrykk`() {
        val intUttrykk = Var(intFaktum)
        val dblUttrykk = Var(dblFaktum)

        // Uttrykk * Number
        val intUttrykkTimesInt: Mul<Int> = intUttrykk * 2
        assertEquals(22, intUttrykkTimesInt.evaluer())

        val dblUttrykkTimesDbl: Mul<Double> = dblUttrykk * 2.2
        assertEquals(48.84, dblUttrykkTimesDbl.evaluer())

        // Number * Uttrykk
        val intTimesIntUttrykk: Mul<Int> = 2 * intUttrykk
        assertEquals(22, intTimesIntUttrykk.evaluer())

        val dblTimesDblUttrykk: Mul<Double> = 2.2 * dblUttrykk
        assertEquals(48.84, dblTimesDblUttrykk.evaluer())

        // Mixed types (Int * Double coerced to Int truncates to 24)
        val intUttrykkTimesDbl: Mul<Int> = intUttrykk * 2.2
        assertEquals(24, intUttrykkTimesDbl.evaluer()) // 11 * 2.2 = 24.2, truncated to 24

        val dblUttrykkTimesInt: Mul<Double> = dblUttrykk * 2
        assertEquals(44.4, dblUttrykkTimesInt.evaluer())

        val intTimesDblUttrykk: Mul<Double> = 2 * dblUttrykk
        assertEquals(44.4, intTimesDblUttrykk.evaluer())

        val dblTimesIntUttrykk: Mul<Int> = 2.2 * intUttrykk
        assertEquals(24, dblTimesIntUttrykk.evaluer()) // 2.2 * 11 = 24.2, truncated to 24

        // Uttrykk * Uttrykk
        val intUttrykkTimesIntUttrykk: Mul<Int> = intUttrykk * intUttrykk
        assertEquals(121, intUttrykkTimesIntUttrykk.evaluer())

        val dblUttrykkTimesDblUttrykk: Mul<Double> = dblUttrykk * dblUttrykk
        assertEquals(492.84, dblUttrykkTimesDblUttrykk.evaluer())

        val intUttrykkTimesDblUttrykk: Mul<Int> = intUttrykk * dblUttrykk
        assertEquals(244.2, intUttrykkTimesDblUttrykk.evaluer())

        val dblUttrykkTimesIntUttrykk: Mul<Double> = dblUttrykk * intUttrykk
        assertEquals(244.2, dblUttrykkTimesIntUttrykk.evaluer())
    }

    @Test
    fun `times operators with Faktum`() {
        // Faktum * Number
        val intFaktumTimesInt: Mul<Int> = intFaktum * 2
        assertEquals(22, intFaktumTimesInt.evaluer())

        val dblFaktumTimesDbl: Mul<Double> = dblFaktum * 2.2
        assertEquals(48.84, dblFaktumTimesDbl.evaluer())

        // Number * Faktum
        val intTimesIntFaktum: Mul<Int> = 2 * intFaktum
        assertEquals(22, intTimesIntFaktum.evaluer())

        val dblTimesDblFaktum: Mul<Double> = 2.2 * dblFaktum
        assertEquals(48.84, dblTimesDblFaktum.evaluer())

        // Mixed types (Int * Double coerced to Int truncates to 24)
        val intFaktumTimesDbl: Mul<Int> = intFaktum * 2.2
        assertEquals(24, intFaktumTimesDbl.evaluer()) // 11 * 2.2 = 24.2, truncated to 24

        val dblFaktumTimesInt: Mul<Double> = dblFaktum * 2
        assertEquals(44.4, dblFaktumTimesInt.evaluer())

        val intTimesDblFaktum: Mul<Double> = 2 * dblFaktum
        assertEquals(44.4, intTimesDblFaktum.evaluer())

        val dblTimesIntFaktum: Mul<Int> = 2.2 * intFaktum
        assertEquals(24, dblTimesIntFaktum.evaluer()) // 2.2 * 11 = 24.2, truncated to 24

        // Faktum * Faktum
        val intFaktumTimesIntFaktum: Mul<Int> = intFaktum * intFaktum
        assertEquals(121, intFaktumTimesIntFaktum.evaluer())

        val dblFaktumTimesDblFaktum: Mul<Double> = dblFaktum * dblFaktum
        assertEquals(492.84, dblFaktumTimesDblFaktum.evaluer())

        val intFaktumTimesDblFaktum: Mul<Int> = intFaktum * dblFaktum
        assertEquals(244.2, intFaktumTimesDblFaktum.evaluer())

        val dblFaktumTimesIntFaktum: Mul<Double> = dblFaktum * intFaktum
        assertEquals(244.2, dblFaktumTimesIntFaktum.evaluer())
    }

    // ========================================================================
    // Division Operators (always returns Double)
    // ========================================================================

    @Test
    fun `division operators with Uttrykk`() {
        val intUttrykk = Var(intFaktum)
        val dblUttrykk = Var(dblFaktum)

        // Uttrykk / Number
        val intUttrykkDivInt: Div = intUttrykk / 2
        assertEquals(5.5, intUttrykkDivInt.evaluer())

        val dblUttrykkDivDbl: Div = dblUttrykk / 2.2
        assertEquals(10.0909, dblUttrykkDivDbl.evaluer(), 0.0001)

        // Number / Uttrykk
        val intDivIntUttrykk: Div = 20 / intUttrykk
        assertEquals(1.8181, intDivIntUttrykk.evaluer(), 0.0001)

        val dblDivDblUttrykk: Div = 48.4 / dblUttrykk
        assertEquals(2.1801, dblDivDblUttrykk.evaluer(), 0.0001)

        // Mixed types
        val intUttrykkDivDbl: Div = intUttrykk / 2.2
        assertEquals(5.0, intUttrykkDivDbl.evaluer())

        val dblUttrykkDivInt: Div = dblUttrykk / 2
        assertEquals(11.1, dblUttrykkDivInt.evaluer())

        val intDivDblUttrykk: Div = 22 / dblUttrykk
        assertEquals(0.9909, intDivDblUttrykk.evaluer(), 0.0001)

        val dblDivIntUttrykk: Div = 44.0 / intUttrykk
        assertEquals(4.0, dblDivIntUttrykk.evaluer())

        // Uttrykk / Uttrykk
        val intUttrykkDivIntUttrykk: Div = intUttrykk / intUttrykk
        assertEquals(1.0, intUttrykkDivIntUttrykk.evaluer())

        val dblUttrykkDivDblUttrykk: Div = dblUttrykk / dblUttrykk
        assertEquals(1.0, dblUttrykkDivDblUttrykk.evaluer())

        val intUttrykkDivDblUttrykk: Div = intUttrykk / dblUttrykk
        assertEquals(0.4954, intUttrykkDivDblUttrykk.evaluer(), 0.0001)

        val dblUttrykkDivIntUttrykk: Div = dblUttrykk / intUttrykk
        assertEquals(2.0181, dblUttrykkDivIntUttrykk.evaluer(), 0.0001)
    }

    @Test
    fun `division operators with Faktum`() {
        // Faktum / Number
        val intFaktumDivInt: Div = intFaktum / 2
        assertEquals(5.5, intFaktumDivInt.evaluer())

        val dblFaktumDivDbl: Div = dblFaktum / 2.2
        assertEquals(10.0909, dblFaktumDivDbl.evaluer(), 0.0001)

        // Number / Faktum
        val intDivIntFaktum: Div = 20 / intFaktum
        assertEquals(1.8181, intDivIntFaktum.evaluer(), 0.0001)

        val dblDivDblFaktum: Div = 48.4 / dblFaktum
        assertEquals(2.1801, dblDivDblFaktum.evaluer(), 0.0001)

        // Mixed types
        val intFaktumDivDbl: Div = intFaktum / 2.2
        assertEquals(5.0, intFaktumDivDbl.evaluer())

        val dblFaktumDivInt: Div = dblFaktum / 2
        assertEquals(11.1, dblFaktumDivInt.evaluer())

        val intDivDblFaktum: Div = 22 / dblFaktum
        assertEquals(0.9909, intDivDblFaktum.evaluer(), 0.0001)

        val dblDivIntFaktum: Div = 44.0 / intFaktum
        assertEquals(4.0, dblDivIntFaktum.evaluer())

        // Faktum / Faktum
        val intFaktumDivIntFaktum: Div = intFaktum / intFaktum
        assertEquals(1.0, intFaktumDivIntFaktum.evaluer())

        val dblFaktumDivDblFaktum: Div = dblFaktum / dblFaktum
        assertEquals(1.0, dblFaktumDivDblFaktum.evaluer())

        val intFaktumDivDblFaktum: Div = intFaktum / dblFaktum
        assertEquals(0.4954, intFaktumDivDblFaktum.evaluer(), 0.0001)

        val dblFaktumDivIntFaktum: Div = dblFaktum / intFaktum
        assertEquals(2.0181, dblFaktumDivIntFaktum.evaluer(), 0.0001)
    }

    // ========================================================================
    // Unary Minus
    // ========================================================================

    @Test
    fun `unary minus with Uttrykk`() {
        val intUttrykk = Var(intFaktum)
        val dblUttrykk = Var(dblFaktum)

        val negInt: Neg<Int> = -intUttrykk
        assertEquals(-11, negInt.evaluer())
        assertEquals("-int", negInt.notasjon())

        val negDbl: Neg<Double> = -dblUttrykk
        assertEquals(-22.2, negDbl.evaluer())
        assertEquals("-dbl", negDbl.notasjon())
    }

    @Test
    fun `unary minus with complex expression`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 5)

        val negSum = -(a + b)
        assertEquals(-15, negSum.evaluer())
        assertEquals("-(a + b)", negSum.notasjon())

        val negProduct = -(a * b)
        assertEquals(-50, negProduct.evaluer())
        // Note: Uttrykk doesn't add parentheses for multiplication inside negation
        assertEquals("-a * b", negProduct.notasjon())
    }

    // ========================================================================
    // Notation and Concrete String Tests
    // ========================================================================

    @Test
    fun `notation shows variable names`() {
        val a = Faktum("alpha", 10)
        val b = Faktum("beta", 20)

        assertEquals("alpha + beta", (a + b).notasjon())
        assertEquals("alpha - beta", (a - b).notasjon())
        assertEquals("alpha * beta", (a * b).notasjon())
        assertEquals("alpha / beta", (a / b).notasjon())
    }

    @Test
    fun `konkret shows values`() {
        val a = Faktum("alpha", 10)
        val b = Faktum("beta", 20)

        assertEquals("10 + 20", (a + b).konkret())
        assertEquals("10 - 20", (a - b).konkret())
        assertEquals("10 * 20", (a * b).konkret())
        assertEquals("10 / 20", (a / b).konkret())
    }

    @Test
    fun `notation with constants shows values`() {
        val a = Faktum("alpha", 10)

        assertEquals("alpha + 5", (a + 5).notasjon())
        assertEquals("5 + alpha", (5 + a).notasjon())
        assertEquals("alpha * 2", (a * 2).notasjon())
        assertEquals("2 * alpha", (2 * a).notasjon())
    }

    @Test
    fun `complex expression notation`() {
        val G = Faktum("G", 100000)
        val sats = Faktum("sats", 0.45)
        val måneder = Faktum("måneder", 12)

        val expr = sats * G / måneder

        assertEquals("sats * G / måneder", expr.notasjon())
        assertEquals("0.45 * 100000 / 12", expr.konkret())
        assertEquals(3750.0, expr.evaluer())
    }
}
