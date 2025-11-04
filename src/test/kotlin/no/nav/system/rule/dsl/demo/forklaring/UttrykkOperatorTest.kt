package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.Uttrykk
import no.nav.system.rule.dsl.rettsregel.operators.*

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
        assertEquals(12, intUttrykkPlusInt.evaluer())

        val dblUttrykkPlusDbl: Uttrykk<Double> = dblUttrykk + 2.2
        assertEquals(24.4, dblUttrykkPlusDbl.evaluer())

        // Number + Uttrykk
        val intPlusIntUttrykk: Uttrykk<Int> = 1 + intUttrykk
        assertEquals(12, intPlusIntUttrykk.evaluer())

        val dblPlusDblUttrykk: Uttrykk<Double> = 2.2 + dblUttrykk
        assertEquals(24.4, dblPlusDblUttrykk.evaluer())

        // Mixed types
        val intUttrykkPlusDbl: Uttrykk<Double> = intUttrykk + 2.2
        assertEquals(13.2, intUttrykkPlusDbl.evaluer())

        val dblUttrykkPlusInt: Uttrykk<Double> = dblUttrykk + 1
        assertEquals(23.2, dblUttrykkPlusInt.evaluer())

        val intPlusDblUttrykk: Uttrykk<Double> = 1 + dblUttrykk
        assertEquals(23.2, intPlusDblUttrykk.evaluer())

        val dblPlusIntUttrykk: Uttrykk<Double> = 2.2 + intUttrykk
        assertEquals(13.2, dblPlusIntUttrykk.evaluer())

        // Uttrykk + Uttrykk
        val intUttrykkPlusIntUttrykk: Uttrykk<Int> = intUttrykk + intUttrykk
        assertEquals(22, intUttrykkPlusIntUttrykk.evaluer())

        val dblUttrykkPlusDblUttrykk: Uttrykk<Double> = dblUttrykk + dblUttrykk
        assertEquals(44.4, dblUttrykkPlusDblUttrykk.evaluer())

        val intUttrykkPlusDblUttrykk: Uttrykk<Double> = intUttrykk + dblUttrykk
        assertEquals(33.2, intUttrykkPlusDblUttrykk.evaluer())

        val dblUttrykkPlusIntUttrykk: Uttrykk<Double> = dblUttrykk + intUttrykk
        assertEquals(33.2, dblUttrykkPlusIntUttrykk.evaluer())
    }

    @Test
    fun `plus operators with Faktum`() {
        // Faktum + Number
        val intFaktumPlusInt: Uttrykk<Int> = intFaktum + 1
        assertEquals(12, intFaktumPlusInt.evaluer())

        val dblFaktumPlusDbl: Uttrykk<Double> = dblFaktum + 2.2
        assertEquals(24.4, dblFaktumPlusDbl.evaluer())

        // Number + Faktum
        val intPlusIntFaktum: Uttrykk<Int> = 1 + intFaktum
        assertEquals(12, intPlusIntFaktum.evaluer())

        val dblPlusDblFaktum: Uttrykk<Double> = 2.2 + dblFaktum
        assertEquals(24.4, dblPlusDblFaktum.evaluer())

        // Mixed types
        val intFaktumPlusDbl: Uttrykk<Double> = intFaktum + 2.2
        assertEquals(13.2, intFaktumPlusDbl.evaluer())

        val dblFaktumPlusInt: Uttrykk<Double> = dblFaktum + 1
        assertEquals(23.2, dblFaktumPlusInt.evaluer())

        val intPlusDblFaktum: Uttrykk<Double> = 1 + dblFaktum
        assertEquals(23.2, intPlusDblFaktum.evaluer())

        val dblPlusIntFaktum: Uttrykk<Double> = 2.2 + intFaktum
        assertEquals(13.2, dblPlusIntFaktum.evaluer())

        // Faktum + Faktum
        val intFaktumPlusIntFaktum: Uttrykk<Int> = intFaktum + intFaktum
        assertEquals(22, intFaktumPlusIntFaktum.evaluer())

        val dblFaktumPlusDblFaktum: Uttrykk<Double> = dblFaktum + dblFaktum
        assertEquals(44.4, dblFaktumPlusDblFaktum.evaluer())

        val intFaktumPlusDblFaktum: Uttrykk<Double> = intFaktum + dblFaktum
        assertEquals(33.2, intFaktumPlusDblFaktum.evaluer())

        val dblFaktumPlusIntFaktum: Uttrykk<Double> = dblFaktum + intFaktum
        assertEquals(33.2, dblFaktumPlusIntFaktum.evaluer())
    }

    @Test
    fun `plus operators mixed Faktum and Uttrykk`() {
        val intUttrykk = intFaktum
        val dblUttrykk = dblFaktum

        // Faktum + Uttrykk
        val intFaktumPlusIntUttrykk: Uttrykk<Int> = intFaktum + intUttrykk
        assertEquals(22, intFaktumPlusIntUttrykk.evaluer())

        val dblFaktumPlusDblUttrykk: Uttrykk<Double> = dblFaktum + dblUttrykk
        assertEquals(44.4, dblFaktumPlusDblUttrykk.evaluer())

        // Uttrykk + Faktum
        val intUttrykkPlusIntFaktum: Uttrykk<Int> = intUttrykk + intFaktum
        assertEquals(22, intUttrykkPlusIntFaktum.evaluer())

        val dblUttrykkPlusDblFaktum: Uttrykk<Double> = dblUttrykk + dblFaktum
        assertEquals(44.4, dblUttrykkPlusDblFaktum.evaluer())
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
        assertEquals(10, intUttrykkMinusInt.evaluer())

        val dblUttrykkMinusDbl: Uttrykk<Double> = dblUttrykk - 2.2
        assertEquals(20.0, dblUttrykkMinusDbl.evaluer())

        // Number - Uttrykk
        val intMinusIntUttrykk: Uttrykk<Int> = 1 - intUttrykk
        assertEquals(-10, intMinusIntUttrykk.evaluer())

        val dblMinusDblUttrykk: Uttrykk<Double> = 2.2 - dblUttrykk
        assertEquals(-20.0, dblMinusDblUttrykk.evaluer())

        // Mixed types
        val intUttrykkMinusDbl: Uttrykk<Double> = intUttrykk - 2.2
        assertEquals(8.8, intUttrykkMinusDbl.evaluer())

        val dblUttrykkMinusInt: Uttrykk<Double> = dblUttrykk - 1
        assertEquals(21.2, dblUttrykkMinusInt.evaluer())

        val intMinusDblUttrykk: Uttrykk<Double> = 1 - dblUttrykk
        assertEquals(-21.2, intMinusDblUttrykk.evaluer())

        val dblMinusIntUttrykk: Uttrykk<Double> = 2.2 - intUttrykk
        assertEquals(-8.8, dblMinusIntUttrykk.evaluer())

        // Uttrykk - Uttrykk
        val intUttrykkMinusIntUttrykk: Uttrykk<Int> = intUttrykk - intUttrykk
        assertEquals(0, intUttrykkMinusIntUttrykk.evaluer())

        val dblUttrykkMinusDblUttrykk: Uttrykk<Double> = dblUttrykk - dblUttrykk
        assertEquals(0.0, dblUttrykkMinusDblUttrykk.evaluer())

        val intUttrykkMinusDblUttrykk: Uttrykk<Double> = intUttrykk - dblUttrykk
        assertEquals(-11.2, intUttrykkMinusDblUttrykk.evaluer())

        val dblUttrykkMinusIntUttrykk: Uttrykk<Double> = dblUttrykk - intUttrykk
        assertEquals(11.2, dblUttrykkMinusIntUttrykk.evaluer())
    }

    @Test
    fun `minus operators with Faktum`() {
        // Faktum - Number
        val intFaktumMinusInt: Uttrykk<Int> = intFaktum - 1
        assertEquals(10, intFaktumMinusInt.evaluer())

        val dblFaktumMinusDbl: Uttrykk<Double> = dblFaktum - 2.2
        assertEquals(20.0, dblFaktumMinusDbl.evaluer())

        // Number - Faktum
        val intMinusIntFaktum: Uttrykk<Int> = 1 - intFaktum
        assertEquals(-10, intMinusIntFaktum.evaluer())

        val dblMinusDblFaktum: Uttrykk<Double> = 2.2 - dblFaktum
        assertEquals(-20.0, dblMinusDblFaktum.evaluer())

        // Mixed types
        val intFaktumMinusDbl: Uttrykk<Double> = intFaktum - 2.2
        assertEquals(8.8, intFaktumMinusDbl.evaluer())

        val dblFaktumMinusInt: Uttrykk<Double> = dblFaktum - 1
        assertEquals(21.2, dblFaktumMinusInt.evaluer())

        val intMinusDblFaktum: Uttrykk<Double> = 1 - dblFaktum
        assertEquals(-21.2, intMinusDblFaktum.evaluer())

        val dblMinusIntFaktum: Uttrykk<Double> = 2.2 - intFaktum
        assertEquals(-8.8, dblMinusIntFaktum.evaluer())

        // Faktum - Faktum
        val intFaktumMinusIntFaktum: Uttrykk<Int> = intFaktum - intFaktum
        assertEquals(0, intFaktumMinusIntFaktum.evaluer())

        val dblFaktumMinusDblFaktum: Uttrykk<Double> = dblFaktum - dblFaktum
        assertEquals(0.0, dblFaktumMinusDblFaktum.evaluer())

        val intFaktumMinusDblFaktum: Uttrykk<Double> = intFaktum - dblFaktum
        assertEquals(-11.2, intFaktumMinusDblFaktum.evaluer())

        val dblFaktumMinusIntFaktum: Uttrykk<Double> = dblFaktum - intFaktum
        assertEquals(11.2, dblFaktumMinusIntFaktum.evaluer())
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
        assertEquals(22, intUttrykkTimesInt.evaluer())

        val dblUttrykkTimesDbl: Uttrykk<Double> = dblUttrykk * 2.2
        assertEquals(48.84, dblUttrykkTimesDbl.evaluer())

        // Number * Uttrykk
        val intTimesIntUttrykk: Uttrykk<Int> = 2 * intUttrykk
        assertEquals(22, intTimesIntUttrykk.evaluer())

        val dblTimesDblUttrykk: Uttrykk<Double> = 2.2 * dblUttrykk
        assertEquals(48.84, dblTimesDblUttrykk.evaluer())

        val intUttrykkTimesDbl: Uttrykk<Double> = intUttrykk * 2.2
        assertEquals(24.20, intUttrykkTimesDbl.evaluer(), 0.001)

        val dblUttrykkTimesInt: Uttrykk<Double> = dblUttrykk * 2
        assertEquals(44.4, dblUttrykkTimesInt.evaluer())

        val intTimesDblUttrykk: Uttrykk<Double> = 2 * dblUttrykk
        assertEquals(44.4, intTimesDblUttrykk.evaluer())

        val dblTimesIntUttrykk: Uttrykk<Double> = 2.2 * intUttrykk
        assertEquals(24.20, dblTimesIntUttrykk.evaluer(), 0.001)

        // Uttrykk * Uttrykk
        val intUttrykkTimesIntUttrykk: Uttrykk<Int> = intUttrykk * intUttrykk
        assertEquals(121, intUttrykkTimesIntUttrykk.evaluer())

        val dblUttrykkTimesDblUttrykk: Uttrykk<Double> = dblUttrykk * dblUttrykk
        assertEquals(492.84, dblUttrykkTimesDblUttrykk.evaluer())

        val intUttrykkTimesDblUttrykk: Uttrykk<Double> = intUttrykk * dblUttrykk
        assertEquals(244.2, intUttrykkTimesDblUttrykk.evaluer())

        val dblUttrykkTimesIntUttrykk: Uttrykk<Double> = dblUttrykk * intUttrykk
        assertEquals(244.2, dblUttrykkTimesIntUttrykk.evaluer())
    }

    @Test
    fun `times operators with Faktum`() {
        // Faktum * Number
        val intFaktumTimesInt: Uttrykk<Int> = intFaktum * 2
        assertEquals(22, intFaktumTimesInt.evaluer())

        val dblFaktumTimesDbl: Uttrykk<Double> = dblFaktum * 2.2
        assertEquals(48.84, dblFaktumTimesDbl.evaluer())

        // Number * Faktum
        val intTimesIntFaktum: Uttrykk<Int> = 2 * intFaktum
        assertEquals(22, intTimesIntFaktum.evaluer())

        val dblTimesDblFaktum: Uttrykk<Double> = 2.2 * dblFaktum
        assertEquals(48.84, dblTimesDblFaktum.evaluer())

        // Mixed types (Int * Double coerced to Int truncates to 24)
        val intFaktumTimesDbl: Uttrykk<Double> = intFaktum * 2.2
        assertEquals(24.20, intFaktumTimesDbl.evaluer(), 0.001)

        val dblFaktumTimesInt: Uttrykk<Double> = dblFaktum * 2
        assertEquals(44.4, dblFaktumTimesInt.evaluer())

        val intTimesDblFaktum: Uttrykk<Double> = 2 * dblFaktum
        assertEquals(44.4, intTimesDblFaktum.evaluer())

        val dblTimesIntFaktum: Uttrykk<Double> = 2.2 * intFaktum
        assertEquals(24.20, dblTimesIntFaktum.evaluer(), 0.001)

        // Faktum * Faktum
        val intFaktumTimesIntFaktum: Uttrykk<Int> = intFaktum * intFaktum
        assertEquals(121, intFaktumTimesIntFaktum.evaluer())

        val dblFaktumTimesDblFaktum: Uttrykk<Double> = dblFaktum * dblFaktum
        assertEquals(492.84, dblFaktumTimesDblFaktum.evaluer())

        val intFaktumTimesDblFaktum: Uttrykk<Double> = intFaktum * dblFaktum
        assertEquals(244.2, intFaktumTimesDblFaktum.evaluer())

        val dblFaktumTimesIntFaktum: Uttrykk<Double> = dblFaktum * intFaktum
        assertEquals(244.2, dblFaktumTimesIntFaktum.evaluer())
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
        assertEquals(5.5, intUttrykkDivInt.evaluer())

        val dblUttrykkDivDbl: Uttrykk<Double> = dblUttrykk / 2.2
        assertEquals(10.0909, dblUttrykkDivDbl.evaluer(), 0.0001)

        // Number / Uttrykk
        val intDivIntUttrykk: Uttrykk<Double> = 20 / intUttrykk
        assertEquals(1.8181, intDivIntUttrykk.evaluer(), 0.0001)

        val dblDivDblUttrykk: Uttrykk<Double> = 48.4 / dblUttrykk
        assertEquals(2.1801, dblDivDblUttrykk.evaluer(), 0.0001)

        // Mixed types
        val intUttrykkDivDbl: Uttrykk<Double> = intUttrykk / 2.2
        assertEquals(5.0, intUttrykkDivDbl.evaluer())

        val dblUttrykkDivInt: Uttrykk<Double> = dblUttrykk / 2
        assertEquals(11.1, dblUttrykkDivInt.evaluer())

        val intDivDblUttrykk: Uttrykk<Double> = 22 / dblUttrykk
        assertEquals(0.9909, intDivDblUttrykk.evaluer(), 0.0001)

        val dblDivIntUttrykk: Uttrykk<Double> = 44.0 / intUttrykk
        assertEquals(4.0, dblDivIntUttrykk.evaluer())

        // Uttrykk / Uttrykk
        val intUttrykkDivIntUttrykk: Uttrykk<Double> = intUttrykk / intUttrykk
        assertEquals(1.0, intUttrykkDivIntUttrykk.evaluer())

        val dblUttrykkDivDblUttrykk: Uttrykk<Double> = dblUttrykk / dblUttrykk
        assertEquals(1.0, dblUttrykkDivDblUttrykk.evaluer())

        val intUttrykkDivDblUttrykk: Uttrykk<Double> = intUttrykk / dblUttrykk
        assertEquals(0.4954, intUttrykkDivDblUttrykk.evaluer(), 0.0001)

        val dblUttrykkDivIntUttrykk: Uttrykk<Double> = dblUttrykk / intUttrykk
        assertEquals(2.0181, dblUttrykkDivIntUttrykk.evaluer(), 0.0001)
    }

    @Test
    fun `division operators with Faktum`() {
        // Faktum / Number
        val intFaktumDivInt: Uttrykk<Double> = intFaktum / 2
        assertEquals(5.5, intFaktumDivInt.evaluer())

        val dblFaktumDivDbl: Uttrykk<Double> = dblFaktum / 2.2
        assertEquals(10.0909, dblFaktumDivDbl.evaluer(), 0.0001)

        // Number / Faktum
        val intDivIntFaktum: Uttrykk<Double> = 20 / intFaktum
        assertEquals(1.8181, intDivIntFaktum.evaluer(), 0.0001)

        val dblDivDblFaktum: Uttrykk<Double> = 48.4 / dblFaktum
        assertEquals(2.1801, dblDivDblFaktum.evaluer(), 0.0001)

        // Mixed types
        val intFaktumDivDbl: Uttrykk<Double> = intFaktum / 2.2
        assertEquals(5.0, intFaktumDivDbl.evaluer())

        val dblFaktumDivInt: Uttrykk<Double> = dblFaktum / 2
        assertEquals(11.1, dblFaktumDivInt.evaluer())

        val intDivDblFaktum: Uttrykk<Double> = 22 / dblFaktum
        assertEquals(0.9909, intDivDblFaktum.evaluer(), 0.0001)

        val dblDivIntFaktum: Uttrykk<Double> = 44.0 / intFaktum
        assertEquals(4.0, dblDivIntFaktum.evaluer())

        // Faktum / Faktum
        val intFaktumDivIntFaktum: Uttrykk<Double> = intFaktum / intFaktum
        assertEquals(1.0, intFaktumDivIntFaktum.evaluer())

        val dblFaktumDivDblFaktum: Uttrykk<Double> = dblFaktum / dblFaktum
        assertEquals(1.0, dblFaktumDivDblFaktum.evaluer())

        val intFaktumDivDblFaktum: Uttrykk<Double> = intFaktum / dblFaktum
        assertEquals(0.4954, intFaktumDivDblFaktum.evaluer(), 0.0001)

        val dblFaktumDivIntFaktum: Uttrykk<Double> = dblFaktum / intFaktum
        assertEquals(2.0181, dblFaktumDivIntFaktum.evaluer(), 0.0001)
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
        assertEquals(3750.0, expr.evaluer())
    }
}
