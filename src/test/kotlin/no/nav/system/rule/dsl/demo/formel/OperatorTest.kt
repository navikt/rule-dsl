package no.nav.system.rule.dsl.demo.formel

import no.nav.system.rule.dsl.formel.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OperatorTest {

    private val intFormel: Formel<Int> = Formel.variable("int", 11)
    private val dblFormel: Formel<Double> = Formel.variable("dbl", 22.2)

    @Test
    fun plusOperators() {
        val intPlusIntFormel: Formel<Int> = 1 + intFormel
        val intFormelPlusInt: Formel<Int> = intFormel + 1
        val dblPlusDblFormel: Formel<Double> = 2.2 + dblFormel
        val dblFormelPlusDbl: Formel<Double> = dblFormel + 2.2

        val intPlusDblFormel: Formel<Double> = 1 + dblFormel
        val dblFormelPlusInt: Formel<Double> = dblFormel + 1
        val dblPlusIntFormel: Formel<Double> = 2.2 + intFormel
        val intFormelPlusDbl: Formel<Double> = intFormel + 2.2

        val intFormelPlusIntFormel: Formel<Int> = intFormel + intFormel
        val intFormelPlusDblFormel: Formel<Double> = intFormel + dblFormel
        val dblFormelPlusIntFormel: Formel<Double> = dblFormel + intFormel
        val dblFormelPlusDblFormel: Formel<Double> = dblFormel + dblFormel

        val dslIntPlusIntFormel = formula<Int> { expression(1 + intFormel) }
        val dslIntFormelPlusInt = formula<Int> { expression(intFormel + 1) }
        val dslDblPlusDblFormel = formula<Double> { expression(2.2 + dblFormel) }
        val dslDblFormelPlusDbl = formula<Double> { expression(dblFormel + 2.2) }

        val dslIntPlusDblFormel = formula<Double> { expression(1 + dblFormel) }
        val dslDblFormelPlusInt = formula<Double> { expression(dblFormel + 1) }
        val dslDblPlusIntFormel = formula<Double> { expression(2.2 + intFormel) }
        val dslIntFormelPlusDbl = formula<Double> { expression(intFormel + 2.2) }

        val dslIntFormelPlusIntFormel = formula<Int> { expression(intFormel + intFormel) }
        val dslIntFormelPlusDblFormel = formula<Double> { expression(intFormel + dblFormel) }
        val dslDblFormelPlusIntFormel = formula<Double> { expression(dblFormel + intFormel) }
        val dslDblFormelPlusDblFormel = formula<Double> { expression(dblFormel + dblFormel) }

        assertEquals(12, intPlusIntFormel.resultat())
        assertEquals(12, intFormelPlusInt.resultat())
        assertEquals(24.4, dblPlusDblFormel.resultat())
        assertEquals(24.4, dblFormelPlusDbl.resultat())

        assertEquals(23.2, intPlusDblFormel.resultat())
        assertEquals(23.2, dblFormelPlusInt.resultat())
        assertEquals(13.2, dblPlusIntFormel.resultat())
        assertEquals(13.2, intFormelPlusDbl.resultat())

        assertEquals(22, intFormelPlusIntFormel.resultat())
        assertEquals(33.2, intFormelPlusDblFormel.resultat())
        assertEquals(33.2, dblFormelPlusIntFormel.resultat())
        assertEquals(44.4, dblFormelPlusDblFormel.resultat())

        assertEquals(12, dslIntPlusIntFormel.resultat())
        assertEquals(12, dslIntFormelPlusInt.resultat())
        assertEquals(24.4, dslDblPlusDblFormel.resultat())
        assertEquals(24.4, dslDblFormelPlusDbl.resultat())

        assertEquals(23.2, dslIntPlusDblFormel.resultat())
        assertEquals(23.2, dslDblFormelPlusInt.resultat())
        assertEquals(13.2, dslDblPlusIntFormel.resultat())
        assertEquals(13.2, dslIntFormelPlusDbl.resultat())

        assertEquals(22, dslIntFormelPlusIntFormel.resultat())
        assertEquals(33.2, dslIntFormelPlusDblFormel.resultat())
        assertEquals(33.2, dslDblFormelPlusIntFormel.resultat())
        assertEquals(44.4, dslDblFormelPlusDblFormel.resultat())
    }

    @Test
    fun minusOperators() {
        val intMinusIntFormel: Formel<Int> = 1 - intFormel
        val intFormelMinusInt: Formel<Int> = intFormel - 1
        val dblMinusDblFormel: Formel<Double> = 2.2 - dblFormel
        val dblFormelMinusDbl: Formel<Double> = dblFormel - 2.2

        val intMinusDblFormel: Formel<Double> = 1 - dblFormel
        val dblFormelMinusInt: Formel<Double> = dblFormel - 1
        val dblMinusIntFormel: Formel<Double> = 2.2 - intFormel
        val intFormelMinusDbl: Formel<Double> = intFormel - 2.2

        val intFormelMinusIntFormel: Formel<Int> = intFormel - intFormel
        val intFormelMinusDblFormel: Formel<Double> = intFormel - dblFormel
        val dblFormelMinusIntFormel: Formel<Double> = dblFormel - intFormel
        val dblFormelMinusDblFormel: Formel<Double> = dblFormel - dblFormel

        val dslIntMinusIntFormel = formula<Int> { expression(1 - intFormel) }
        val dslIntFormelMinusInt = formula<Int> { expression(intFormel - 1) }
        val dslDblMinusDblFormel = formula<Double> { expression(2.2 - dblFormel) }
        val dslDblFormelMinusDbl = formula<Double> { expression(dblFormel - 2.2) }

        val dslIntMinusDblFormel = formula<Double> { expression(1 - dblFormel) }
        val dslDblFormelMinusInt = formula<Double> { expression(dblFormel - 1) }
        val dslDblMinusIntFormel = formula<Double> { expression(2.2 - intFormel) }
        val dslIntFormelMinusDbl = formula<Double> { expression(intFormel - 2.2) }

        val dslIntFormelMinusIntFormel = formula<Int> { expression(intFormel - intFormel) }
        val dslIntFormelMinusDblFormel = formula<Double> { expression(intFormel - dblFormel) }
        val dslDblFormelMinusIntFormel = formula<Double> { expression(dblFormel - intFormel) }
        val dslDblFormelMinusDblFormel = formula<Double> { expression(dblFormel - dblFormel) }

        // Asserts
        assertEquals(-10, intMinusIntFormel.resultat())
        assertEquals(10, intFormelMinusInt.resultat())
        assertEquals(-20.0, dblMinusDblFormel.resultat())
        assertEquals(20.0, dblFormelMinusDbl.resultat())

        assertEquals(-21.2, intMinusDblFormel.resultat())
        assertEquals(21.2, dblFormelMinusInt.resultat())
        assertEquals(-8.8, dblMinusIntFormel.resultat())
        assertEquals(8.8, intFormelMinusDbl.resultat())

        assertEquals(0, intFormelMinusIntFormel.resultat())
        assertEquals(-11.2, intFormelMinusDblFormel.resultat())
        assertEquals(11.2, dblFormelMinusIntFormel.resultat())
        assertEquals(0.0, dblFormelMinusDblFormel.resultat())

        assertEquals(-10, dslIntMinusIntFormel.resultat())
        assertEquals(10, dslIntFormelMinusInt.resultat())
        assertEquals(-20.0, dslDblMinusDblFormel.resultat())
        assertEquals(20.0, dslDblFormelMinusDbl.resultat())

        assertEquals(-21.2, dslIntMinusDblFormel.resultat())
        assertEquals(21.2, dslDblFormelMinusInt.resultat())
        assertEquals(-8.8, dslDblMinusIntFormel.resultat())
        assertEquals(8.8, dslIntFormelMinusDbl.resultat())

        assertEquals(0, dslIntFormelMinusIntFormel.resultat())
        assertEquals(-11.2, dslIntFormelMinusDblFormel.resultat())
        assertEquals(11.2, dslDblFormelMinusIntFormel.resultat())
        assertEquals(0.0, dslDblFormelMinusDblFormel.resultat())
    }

    @Test
    fun timesOperators() {
        val intTimesIntFormel: Formel<Int> = 2 * intFormel
        val intFormelTimesInt: Formel<Int> = intFormel * 2
        val dblTimesDblFormel: Formel<Double> = 2.2 * dblFormel
        val dblFormelTimesDbl: Formel<Double> = dblFormel * 2.2

        val intTimesDblFormel: Formel<Double> = 2 * dblFormel
        val dblFormelTimesInt: Formel<Double> = dblFormel * 2
        val dblTimesIntFormel: Formel<Double> = 2.2 * intFormel
        val intFormelTimesDbl: Formel<Double> = intFormel * 2.2

        val intFormelTimesIntFormel: Formel<Int> = intFormel * intFormel
        val intFormelTimesDblFormel: Formel<Double> = intFormel * dblFormel
        val dblFormelTimesIntFormel: Formel<Double> = dblFormel * intFormel
        val dblFormelTimesDblFormel: Formel<Double> = dblFormel * dblFormel

        val dslIntTimesIntFormel = formula<Int> { expression(2 * intFormel) }
        val dslIntFormelTimesInt = formula<Int> { expression(intFormel * 2) }
        val dslDblTimesDblFormel = formula<Double> { expression(2.2 * dblFormel) }
        val dslDblFormelTimesDbl = formula<Double> { expression(dblFormel * 2.2) }

        val dslIntTimesDblFormel = formula<Double> { expression(2 * dblFormel) }
        val dslDblFormelTimesInt = formula<Double> { expression(dblFormel * 2) }
        val dslDblTimesIntFormel = formula<Double> { expression(2.2 * intFormel) }
        val dslIntFormelTimesDbl = formula<Double> { expression(intFormel * 2.2) }

        val dslIntFormelTimesIntFormel = formula<Int> { expression(intFormel * intFormel) }
        val dslIntFormelTimesDblFormel = formula<Double> { expression(intFormel * dblFormel) }
        val dslDblFormelTimesIntFormel = formula<Double> { expression(dblFormel * intFormel) }
        val dslDblFormelTimesDblFormel = formula<Double> { expression(dblFormel * dblFormel) }

        // Asserts
        assertEquals(22, intTimesIntFormel.resultat())
        assertEquals(22, intFormelTimesInt.resultat())
        assertEquals(48.84, dblTimesDblFormel.resultat())
        assertEquals(48.84, dblFormelTimesDbl.resultat())

        assertEquals(44.4, intTimesDblFormel.resultat())
        assertEquals(44.4, dblFormelTimesInt.resultat())
        assertEquals(24.20, dblTimesIntFormel.resultat(), 0.01)
        assertEquals(24.20, intFormelTimesDbl.resultat(), 0.01)

        assertEquals(121, intFormelTimesIntFormel.resultat())
        assertEquals(244.2, intFormelTimesDblFormel.resultat())
        assertEquals(244.2, dblFormelTimesIntFormel.resultat())
        assertEquals(492.84, dblFormelTimesDblFormel.resultat())

        assertEquals(22, dslIntTimesIntFormel.resultat())
        assertEquals(22, dslIntFormelTimesInt.resultat())
        assertEquals(48.84, dslDblTimesDblFormel.resultat())
        assertEquals(48.84, dslDblFormelTimesDbl.resultat())

        assertEquals(44.4, dslIntTimesDblFormel.resultat())
        assertEquals(44.4, dslDblFormelTimesInt.resultat())
        assertEquals(24.20, dslDblTimesIntFormel.resultat(), 0.0001)
        assertEquals(24.20, dslIntFormelTimesDbl.resultat(), 0.0001)

        assertEquals(121, dslIntFormelTimesIntFormel.resultat())
        assertEquals(244.2, dslIntFormelTimesDblFormel.resultat())
        assertEquals(244.2, dslDblFormelTimesIntFormel.resultat())
        assertEquals(492.84, dslDblFormelTimesDblFormel.resultat())
    }

    @Test
    fun divisionOperators() {
        val intDividedByIntFormel: Formel<Double> = 20 / intFormel
        val intFormelDividedByInt: Formel<Double> = intFormel / 2
        val dblDividedByDblFormel: Formel<Double> = 48.4 / dblFormel
        val dblFormelDividedByDbl: Formel<Double> = dblFormel / 2.2

        val intDividedByDblFormel: Formel<Double> = 22 / dblFormel
        val dblFormelDividedByInt: Formel<Double> = dblFormel / 2
        val dblDividedByIntFormel: Formel<Double> = 44.0 / intFormel
        val intFormelDividedByDbl: Formel<Double> = intFormel / 2.2

        val intFormelDividedByIntFormel: Formel<Double> = intFormel / intFormel
        val intFormelDividedByDblFormel: Formel<Double> = intFormel / dblFormel
        val dblFormelDividedByIntFormel: Formel<Double> = dblFormel / intFormel
        val dblFormelDividedByDblFormel: Formel<Double> = dblFormel / dblFormel

        val dslIntDividedByIntFormel = formula<Double> { expression(20 / intFormel) }
        val dslIntFormelDividedByInt = formula<Double> { expression(intFormel / 2) }
        val dslDblDividedByDblFormel = formula<Double> { expression(48.4 / dblFormel) }
        val dslDblFormelDividedByDbl = formula<Double> { expression(dblFormel / 2.2) }

        val dslIntDividedByDblFormel = formula<Double> { expression(22 / dblFormel) }
        val dslDblFormelDividedByInt = formula<Double> { expression(dblFormel / 2) }
        val dslDblDividedByIntFormel = formula<Double> { expression(44.0 / intFormel) }
        val dslIntFormelDividedByDbl = formula<Double> { expression(intFormel / 2.2) }

        val dslIntFormelDividedByIntFormel = formula<Double> { expression(intFormel / intFormel) }
        val dslIntFormelDividedByDblFormel = formula<Double> { expression(intFormel / dblFormel) }
        val dslDblFormelDividedByIntFormel = formula<Double> { expression(dblFormel / intFormel) }
        val dslDblFormelDividedByDblFormel = formula<Double> { expression(dblFormel / dblFormel) }

        // Asserts
        assertEquals(1.8181, intDividedByIntFormel.resultat(), 0.0001)
        assertEquals(5.5, intFormelDividedByInt.resultat())
        assertEquals(2.1801, dblDividedByDblFormel.resultat(), 0.0001)
        assertEquals(10.0909, dblFormelDividedByDbl.resultat(), 0.0001)

        assertEquals(0.9909, intDividedByDblFormel.resultat(), 0.0001)
        assertEquals(11.1, dblFormelDividedByInt.resultat())
        assertEquals(4.0, dblDividedByIntFormel.resultat())
        assertEquals(5.0, intFormelDividedByDbl.resultat())

        assertEquals(1.0, intFormelDividedByIntFormel.resultat())
        assertEquals(0.4954, intFormelDividedByDblFormel.resultat(), 0.0001)
        assertEquals(2.0181, dblFormelDividedByIntFormel.resultat(), 0.0001)
        assertEquals(1.0, dblFormelDividedByDblFormel.resultat())

        assertEquals(1.8181, dslIntDividedByIntFormel.resultat(), 0.0001)
        assertEquals(5.5, dslIntFormelDividedByInt.resultat())
        assertEquals(2.1801, dslDblDividedByDblFormel.resultat(), 0.0001)
        assertEquals(10.0909, dslDblFormelDividedByDbl.resultat(), 0.0001)

        assertEquals(0.9909, dslIntDividedByDblFormel.resultat(), 0.0001)
        assertEquals(11.1, dslDblFormelDividedByInt.resultat())
        assertEquals(4.0, dslDblDividedByIntFormel.resultat())
        assertEquals(5.0, dslIntFormelDividedByDbl.resultat())

        assertEquals(1.0, dslIntFormelDividedByIntFormel.resultat())
        assertEquals(0.4954, dslIntFormelDividedByDblFormel.resultat(), 0.0001)
        assertEquals(2.0181, dslDblFormelDividedByIntFormel.resultat(), 0.0001)
        assertEquals(1.0, dslDblFormelDividedByDblFormel.resultat())
    }


}