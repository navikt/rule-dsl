package no.nav.system.rule.dsl.demo.formel

import no.nav.system.rule.dsl.formel.Builder.Companion.kmath
import no.nav.system.rule.dsl.formel.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OperatorTest {

    val intFormel: Formel<Int> = Formel("int", 11)
    val dblFormel: Formel<Double> = Formel("dbl", 22.2)

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

        val kintPlusIntFormel = kmath<Int>().formel(1 + intFormel).build()
        val kintFormelPlusInt = kmath<Int>().formel(intFormel + 1).build()
        val kdblPlusDblFormel = kmath<Double>().formel(2.2 + dblFormel).build()
        val kdblFormelPlusDbl = kmath<Double>().formel(dblFormel + 2.2).build()

        val kintPlusDblFormel = kmath<Double>().formel(1 + dblFormel).build()
        val kdblFormelPlusInt = kmath<Double>().formel(dblFormel + 1).build()
        val kdblPlusIntFormel = kmath<Double>().formel(2.2 + intFormel).build()
        val kintFormelPlusDbl = kmath<Double>().formel(intFormel + 2.2).build()

        val kintFormelPlusIntFormel = kmath<Int>().formel(intFormel + intFormel).build()
        val kintFormelPlusDblFormel = kmath<Double>().formel(intFormel + dblFormel).build()
        val kdblFormelPlusIntFormel = kmath<Double>().formel(dblFormel + intFormel).build()
        val kdblFormelPlusDblFormel = kmath<Double>().formel(dblFormel + dblFormel).build()

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

        assertEquals(12, kintPlusIntFormel.resultat())
        assertEquals(12, kintFormelPlusInt.resultat())
        assertEquals(24.4, kdblPlusDblFormel.resultat())
        assertEquals(24.4, kdblFormelPlusDbl.resultat())

        assertEquals(23.2, kintPlusDblFormel.resultat())
        assertEquals(23.2, kdblFormelPlusInt.resultat())
        assertEquals(13.2, kdblPlusIntFormel.resultat())
        assertEquals(13.2, kintFormelPlusDbl.resultat())

        assertEquals(22, kintFormelPlusIntFormel.resultat())
        assertEquals(33.2, kintFormelPlusDblFormel.resultat())
        assertEquals(33.2, kdblFormelPlusIntFormel.resultat())
        assertEquals(44.4, kdblFormelPlusDblFormel.resultat())
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

        val kintMinusIntFormel = kmath<Int>().formel(1 - intFormel).build()
        val kintFormelMinusInt = kmath<Int>().formel(intFormel - 1).build()
        val kdblMinusDblFormel = kmath<Double>().formel(2.2 - dblFormel).build()
        val kdblFormelMinusDbl = kmath<Double>().formel(dblFormel - 2.2).build()

        val kintMinusDblFormel = kmath<Double>().formel(1 - dblFormel).build()
        val kdblFormelMinusInt = kmath<Double>().formel(dblFormel - 1).build()
        val kdblMinusIntFormel = kmath<Double>().formel(2.2 - intFormel).build()
        val kintFormelMinusDbl = kmath<Double>().formel(intFormel - 2.2).build()

        val kintFormelMinusIntFormel = kmath<Int>().formel(intFormel - intFormel).build()
        val kintFormelMinusDblFormel = kmath<Double>().formel(intFormel - dblFormel).build()
        val kdblFormelMinusIntFormel = kmath<Double>().formel(dblFormel - intFormel).build()
        val kdblFormelMinusDblFormel = kmath<Double>().formel(dblFormel - dblFormel).build()

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

        assertEquals(-10, kintMinusIntFormel.resultat())
        assertEquals(10, kintFormelMinusInt.resultat())
        assertEquals(-20.0, kdblMinusDblFormel.resultat())
        assertEquals(20.0, kdblFormelMinusDbl.resultat())

        assertEquals(-21.2, kintMinusDblFormel.resultat())
        assertEquals(21.2, kdblFormelMinusInt.resultat())
        assertEquals(-8.8, kdblMinusIntFormel.resultat())
        assertEquals(8.8, kintFormelMinusDbl.resultat())

        assertEquals(0, kintFormelMinusIntFormel.resultat())
        assertEquals(-11.2, kintFormelMinusDblFormel.resultat())
        assertEquals(11.2, kdblFormelMinusIntFormel.resultat())
        assertEquals(0.0, kdblFormelMinusDblFormel.resultat())
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

        val kintTimesIntFormel = kmath<Int>().formel(2 * intFormel).build()
        val kintFormelTimesInt = kmath<Int>().formel(intFormel * 2).build()
        val kdblTimesDblFormel = kmath<Double>().formel(2.2 * dblFormel).build()
        val kdblFormelTimesDbl = kmath<Double>().formel(dblFormel * 2.2).build()

        val kintTimesDblFormel = kmath<Double>().formel(2 * dblFormel).build()
        val kdblFormelTimesInt = kmath<Double>().formel(dblFormel * 2).build()
        val kdblTimesIntFormel = kmath<Double>().formel(2.2 * intFormel).build()
        val kintFormelTimesDbl = kmath<Double>().formel(intFormel * 2.2).build()

        val kintFormelTimesIntFormel = kmath<Int>().formel(intFormel * intFormel).build()
        val kintFormelTimesDblFormel = kmath<Double>().formel(intFormel * dblFormel).build()
        val kdblFormelTimesIntFormel = kmath<Double>().formel(dblFormel * intFormel).build()
        val kdblFormelTimesDblFormel = kmath<Double>().formel(dblFormel * dblFormel).build()

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

        assertEquals(22, kintTimesIntFormel.resultat())
        assertEquals(22, kintFormelTimesInt.resultat())
        assertEquals(48.84, kdblTimesDblFormel.resultat())
        assertEquals(48.84, kdblFormelTimesDbl.resultat())

        assertEquals(44.4, kintTimesDblFormel.resultat())
        assertEquals(44.4, kdblFormelTimesInt.resultat())
        assertEquals(24.20, kdblTimesIntFormel.resultat(), 0.0001)
        assertEquals(24.20, kintFormelTimesDbl.resultat(), 0.0001)

        assertEquals(121, kintFormelTimesIntFormel.resultat())
        assertEquals(244.2, kintFormelTimesDblFormel.resultat())
        assertEquals(244.2, kdblFormelTimesIntFormel.resultat())
        assertEquals(492.84, kdblFormelTimesDblFormel.resultat())
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

        val kintDividedByIntFormel = kmath<Double>().formel(20 / intFormel).build()
        val kintFormelDividedByInt = kmath<Double>().formel(intFormel / 2).build()
        val kdblDividedByDblFormel = kmath<Double>().formel(48.4 / dblFormel).build()
        val kdblFormelDividedByDbl = kmath<Double>().formel(dblFormel / 2.2).build()

        val kintDividedByDblFormel = kmath<Double>().formel(22 / dblFormel).build()
        val kdblFormelDividedByInt = kmath<Double>().formel(dblFormel / 2).build()
        val kdblDividedByIntFormel = kmath<Double>().formel(44.0 / intFormel).build()
        val kintFormelDividedByDbl = kmath<Double>().formel(intFormel / 2.2).build()

        val kintFormelDividedByIntFormel = kmath<Double>().formel(intFormel / intFormel).build()
        val kintFormelDividedByDblFormel = kmath<Double>().formel(intFormel / dblFormel).build()
        val kdblFormelDividedByIntFormel = kmath<Double>().formel(dblFormel / intFormel).build()
        val kdblFormelDividedByDblFormel = kmath<Double>().formel(dblFormel / dblFormel).build()

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

        assertEquals(1.8181, kintDividedByIntFormel.resultat(), 0.0001)
        assertEquals(5.5, kintFormelDividedByInt.resultat())
        assertEquals(2.1801, kdblDividedByDblFormel.resultat(), 0.0001)
        assertEquals(10.0909, kdblFormelDividedByDbl.resultat(), 0.0001)

        assertEquals(0.9909, kintDividedByDblFormel.resultat(), 0.0001)
        assertEquals(11.1, kdblFormelDividedByInt.resultat())
        assertEquals(4.0, kdblDividedByIntFormel.resultat())
        assertEquals(5.0, kintFormelDividedByDbl.resultat())

        assertEquals(1.0, kintFormelDividedByIntFormel.resultat())
        assertEquals(0.4954, kintFormelDividedByDblFormel.resultat(), 0.0001)
        assertEquals(2.0181, kdblFormelDividedByIntFormel.resultat(), 0.0001)
        assertEquals(1.0, kdblFormelDividedByDblFormel.resultat())
    }


}