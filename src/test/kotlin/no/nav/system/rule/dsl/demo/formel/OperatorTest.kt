package no.nav.system.rule.dsl.demo.formel

import no.nav.system.rule.dsl.formel.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OperatorTest {

    private val intFormel: Formel<Int> = Formel.variable("int", 11)
    private val dblFormel: Formel<Double> = Formel.variable("dbl", 22.2)

    @Test
    fun plusOperators_Normal() {
        // Direct operator usage
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

        // Assertions
        assertEquals(12, intPlusIntFormel.value)
        assertEquals(12, intFormelPlusInt.value)
        assertEquals(24.4, dblPlusDblFormel.value)
        assertEquals(24.4, dblFormelPlusDbl.value)

        assertEquals(23.2, intPlusDblFormel.value)
        assertEquals(23.2, dblFormelPlusInt.value)
        assertEquals(13.2, dblPlusIntFormel.value)
        assertEquals(13.2, intFormelPlusDbl.value)

        assertEquals(22, intFormelPlusIntFormel.value)
        assertEquals(33.2, intFormelPlusDblFormel.value)
        assertEquals(33.2, dblFormelPlusIntFormel.value)
        assertEquals(44.4, dblFormelPlusDblFormel.value)
    }

    @Test
    fun plusOperators_DSL() {
        // DSL formula usage
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

        // Assertions
        assertEquals(12, dslIntPlusIntFormel.value)
        assertEquals(12, dslIntFormelPlusInt.value)
        assertEquals(24.4, dslDblPlusDblFormel.value)
        assertEquals(24.4, dslDblFormelPlusDbl.value)

        assertEquals(23.2, dslIntPlusDblFormel.value)
        assertEquals(23.2, dslDblFormelPlusInt.value)
        assertEquals(13.2, dslDblPlusIntFormel.value)
        assertEquals(13.2, dslIntFormelPlusDbl.value)

        assertEquals(22, dslIntFormelPlusIntFormel.value)
        assertEquals(33.2, dslIntFormelPlusDblFormel.value)
        assertEquals(33.2, dslDblFormelPlusIntFormel.value)
        assertEquals(44.4, dslDblFormelPlusDblFormel.value)
    }

    @Test
    fun plusOperators_Builder() {
        // FormelBuilder usage
        val builderIntPlusIntFormel = FormelBuilder.create<Int>()
            .name("intPlusIntFormel")
            .expression(1 + intFormel)
            .build()
        val builderIntFormelPlusInt = FormelBuilder.create<Int>()
            .name("intFormelPlusInt")
            .expression(intFormel + 1)
            .build()
        val builderDblPlusDblFormel = FormelBuilder.create<Double>()
            .name("dblPlusDblFormel")
            .expression(2.2 + dblFormel)
            .build()
        val builderDblFormelPlusDbl = FormelBuilder.create<Double>()
            .name("dblFormelPlusDbl")
            .expression(dblFormel + 2.2)
            .build()

        val builderIntPlusDblFormel = FormelBuilder.create<Double>()
            .name("intPlusDblFormel")
            .expression(1 + dblFormel)
            .build()
        val builderDblFormelPlusInt = FormelBuilder.create<Double>()
            .name("dblFormelPlusInt")
            .expression(dblFormel + 1)
            .build()
        val builderDblPlusIntFormel = FormelBuilder.create<Double>()
            .name("dblPlusIntFormel")
            .expression(2.2 + intFormel)
            .build()
        val builderIntFormelPlusDbl = FormelBuilder.create<Double>()
            .name("intFormelPlusDbl")
            .expression(intFormel + 2.2)
            .build()

        val builderIntFormelPlusIntFormel = FormelBuilder.create<Int>()
            .name("intFormelPlusIntFormel")
            .expression(intFormel + intFormel)
            .build()
        val builderIntFormelPlusDblFormel = FormelBuilder.create<Double>()
            .name("intFormelPlusDblFormel")
            .expression(intFormel + dblFormel)
            .build()
        val builderDblFormelPlusIntFormel = FormelBuilder.create<Double>()
            .name("dblFormelPlusIntFormel")
            .expression(dblFormel + intFormel)
            .build()
        val builderDblFormelPlusDblFormel = FormelBuilder.create<Double>()
            .name("dblFormelPlusDblFormel")
            .expression(dblFormel + dblFormel)
            .build()

        // Assertions
        assertEquals(12, builderIntPlusIntFormel.value)
        assertEquals(12, builderIntFormelPlusInt.value)
        assertEquals(24.4, builderDblPlusDblFormel.value)
        assertEquals(24.4, builderDblFormelPlusDbl.value)

        assertEquals(23.2, builderIntPlusDblFormel.value)
        assertEquals(23.2, builderDblFormelPlusInt.value)
        assertEquals(13.2, builderDblPlusIntFormel.value)
        assertEquals(13.2, builderIntFormelPlusDbl.value)

        assertEquals(22, builderIntFormelPlusIntFormel.value)
        assertEquals(33.2, builderIntFormelPlusDblFormel.value)
        assertEquals(33.2, builderDblFormelPlusIntFormel.value)
        assertEquals(44.4, builderDblFormelPlusDblFormel.value)
    }

    @Test
    fun minusOperators_Normal() {
        // Direct operator usage
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

        // Assertions
        assertEquals(-10, intMinusIntFormel.value)
        assertEquals(10, intFormelMinusInt.value)
        assertEquals(-20.0, dblMinusDblFormel.value)
        assertEquals(20.0, dblFormelMinusDbl.value)

        assertEquals(-21.2, intMinusDblFormel.value)
        assertEquals(21.2, dblFormelMinusInt.value)
        assertEquals(-8.8, dblMinusIntFormel.value)
        assertEquals(8.8, intFormelMinusDbl.value)

        assertEquals(0, intFormelMinusIntFormel.value)
        assertEquals(-11.2, intFormelMinusDblFormel.value)
        assertEquals(11.2, dblFormelMinusIntFormel.value)
        assertEquals(0.0, dblFormelMinusDblFormel.value)
    }

    @Test
    fun minusOperators_DSL() {
        // DSL formula usage
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

        // Assertions
        assertEquals(-10, dslIntMinusIntFormel.value)
        assertEquals(10, dslIntFormelMinusInt.value)
        assertEquals(-20.0, dslDblMinusDblFormel.value)
        assertEquals(20.0, dslDblFormelMinusDbl.value)

        assertEquals(-21.2, dslIntMinusDblFormel.value)
        assertEquals(21.2, dslDblFormelMinusInt.value)
        assertEquals(-8.8, dslDblMinusIntFormel.value)
        assertEquals(8.8, dslIntFormelMinusDbl.value)

        assertEquals(0, dslIntFormelMinusIntFormel.value)
        assertEquals(-11.2, dslIntFormelMinusDblFormel.value)
        assertEquals(11.2, dslDblFormelMinusIntFormel.value)
        assertEquals(0.0, dslDblFormelMinusDblFormel.value)
    }

    @Test
    fun minusOperators_Builder() {
        // FormelBuilder usage
        val builderIntMinusIntFormel = FormelBuilder.create<Int>()
            .name("intMinusIntFormel")
            .expression(1 - intFormel)
            .build()
        val builderIntFormelMinusInt = FormelBuilder.create<Int>()
            .name("intFormelMinusInt")
            .expression(intFormel - 1)
            .build()
        val builderDblMinusDblFormel = FormelBuilder.create<Double>()
            .name("dblMinusDblFormel")
            .expression(2.2 - dblFormel)
            .build()
        val builderDblFormelMinusDbl = FormelBuilder.create<Double>()
            .name("dblFormelMinusDbl")
            .expression(dblFormel - 2.2)
            .build()

        val builderIntMinusDblFormel = FormelBuilder.create<Double>()
            .name("intMinusDblFormel")
            .expression(1 - dblFormel)
            .build()
        val builderDblFormelMinusInt = FormelBuilder.create<Double>()
            .name("dblFormelMinusInt")
            .expression(dblFormel - 1)
            .build()
        val builderDblMinusIntFormel = FormelBuilder.create<Double>()
            .name("dblMinusIntFormel")
            .expression(2.2 - intFormel)
            .build()
        val builderIntFormelMinusDbl = FormelBuilder.create<Double>()
            .name("intFormelMinusDbl")
            .expression(intFormel - 2.2)
            .build()

        val builderIntFormelMinusIntFormel = FormelBuilder.create<Int>()
            .name("intFormelMinusIntFormel")
            .expression(intFormel - intFormel)
            .build()
        val builderIntFormelMinusDblFormel = FormelBuilder.create<Double>()
            .name("intFormelMinusDblFormel")
            .expression(intFormel - dblFormel)
            .build()
        val builderDblFormelMinusIntFormel = FormelBuilder.create<Double>()
            .name("dblFormelMinusIntFormel")
            .expression(dblFormel - intFormel)
            .build()
        val builderDblFormelMinusDblFormel = FormelBuilder.create<Double>()
            .name("dblFormelMinusDblFormel")
            .expression(dblFormel - dblFormel)
            .build()

        // Assertions
        assertEquals(-10, builderIntMinusIntFormel.value)
        assertEquals(10, builderIntFormelMinusInt.value)
        assertEquals(-20.0, builderDblMinusDblFormel.value)
        assertEquals(20.0, builderDblFormelMinusDbl.value)

        assertEquals(-21.2, builderIntMinusDblFormel.value)
        assertEquals(21.2, builderDblFormelMinusInt.value)
        assertEquals(-8.8, builderDblMinusIntFormel.value)
        assertEquals(8.8, builderIntFormelMinusDbl.value)

        assertEquals(0, builderIntFormelMinusIntFormel.value)
        assertEquals(-11.2, builderIntFormelMinusDblFormel.value)
        assertEquals(11.2, builderDblFormelMinusIntFormel.value)
        assertEquals(0.0, builderDblFormelMinusDblFormel.value)
    }

    @Test
    fun timesOperators_Normal() {
        // Direct operator usage
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

        // Assertions
        assertEquals(22, intTimesIntFormel.value)
        assertEquals(22, intFormelTimesInt.value)
        assertEquals(48.84, dblTimesDblFormel.value)
        assertEquals(48.84, dblFormelTimesDbl.value)

        assertEquals(44.4, intTimesDblFormel.value)
        assertEquals(44.4, dblFormelTimesInt.value)
        assertEquals(24.20, dblTimesIntFormel.value, 0.01)
        assertEquals(24.20, intFormelTimesDbl.value, 0.01)

        assertEquals(121, intFormelTimesIntFormel.value)
        assertEquals(244.2, intFormelTimesDblFormel.value)
        assertEquals(244.2, dblFormelTimesIntFormel.value)
        assertEquals(492.84, dblFormelTimesDblFormel.value)
    }

    @Test
    fun timesOperators_DSL() {
        // DSL formula usage
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

        // Assertions
        assertEquals(22, dslIntTimesIntFormel.value)
        assertEquals(22, dslIntFormelTimesInt.value)
        assertEquals(48.84, dslDblTimesDblFormel.value)
        assertEquals(48.84, dslDblFormelTimesDbl.value)

        assertEquals(44.4, dslIntTimesDblFormel.value)
        assertEquals(44.4, dslDblFormelTimesInt.value)
        assertEquals(24.20, dslDblTimesIntFormel.value, 0.0001)
        assertEquals(24.20, dslIntFormelTimesDbl.value, 0.0001)

        assertEquals(121, dslIntFormelTimesIntFormel.value)
        assertEquals(244.2, dslIntFormelTimesDblFormel.value)
        assertEquals(244.2, dslDblFormelTimesIntFormel.value)
        assertEquals(492.84, dslDblFormelTimesDblFormel.value)
    }

    @Test
    fun timesOperators_Builder() {
        // FormelBuilder usage
        val builderIntTimesIntFormel = FormelBuilder.create<Int>()
            .name("intTimesIntFormel")
            .expression(2 * intFormel)
            .build()
        val builderIntFormelTimesInt = FormelBuilder.create<Int>()
            .name("intFormelTimesInt")
            .expression(intFormel * 2)
            .build()
        val builderDblTimesDblFormel = FormelBuilder.create<Double>()
            .name("dblTimesDblFormel")
            .expression(2.2 * dblFormel)
            .build()
        val builderDblFormelTimesDbl = FormelBuilder.create<Double>()
            .name("dblFormelTimesDbl")
            .expression(dblFormel * 2.2)
            .build()

        val builderIntTimesDblFormel = FormelBuilder.create<Double>()
            .name("intTimesDblFormel")
            .expression(2 * dblFormel)
            .build()
        val builderDblFormelTimesInt = FormelBuilder.create<Double>()
            .name("dblFormelTimesInt")
            .expression(dblFormel * 2)
            .build()
        val builderDblTimesIntFormel = FormelBuilder.create<Double>()
            .name("dblTimesIntFormel")
            .expression(2.2 * intFormel)
            .build()
        val builderIntFormelTimesDbl = FormelBuilder.create<Double>()
            .name("intFormelTimesDbl")
            .expression(intFormel * 2.2)
            .build()

        val builderIntFormelTimesIntFormel = FormelBuilder.create<Int>()
            .name("intFormelTimesIntFormel")
            .expression(intFormel * intFormel)
            .build()
        val builderIntFormelTimesDblFormel = FormelBuilder.create<Double>()
            .name("intFormelTimesDblFormel")
            .expression(intFormel * dblFormel)
            .build()
        val builderDblFormelTimesIntFormel = FormelBuilder.create<Double>()
            .name("dblFormelTimesIntFormel")
            .expression(dblFormel * intFormel)
            .build()
        val builderDblFormelTimesDblFormel = FormelBuilder.create<Double>()
            .name("dblFormelTimesDblFormel")
            .expression(dblFormel * dblFormel)
            .build()

        // Assertions
        assertEquals(22, builderIntTimesIntFormel.value)
        assertEquals(22, builderIntFormelTimesInt.value)
        assertEquals(48.84, builderDblTimesDblFormel.value)
        assertEquals(48.84, builderDblFormelTimesDbl.value)

        assertEquals(44.4, builderIntTimesDblFormel.value)
        assertEquals(44.4, builderDblFormelTimesInt.value)
        assertEquals(24.20, builderDblTimesIntFormel.value, 0.0001)
        assertEquals(24.20, builderIntFormelTimesDbl.value, 0.0001)

        assertEquals(121, builderIntFormelTimesIntFormel.value)
        assertEquals(244.2, builderIntFormelTimesDblFormel.value)
        assertEquals(244.2, builderDblFormelTimesIntFormel.value)
        assertEquals(492.84, builderDblFormelTimesDblFormel.value)
    }

    @Test
    fun divisionOperators_Normal() {
        // Direct operator usage
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

        // Assertions
        assertEquals(1.8181, intDividedByIntFormel.value, 0.0001)
        assertEquals(5.5, intFormelDividedByInt.value)
        assertEquals(2.1801, dblDividedByDblFormel.value, 0.0001)
        assertEquals(10.0909, dblFormelDividedByDbl.value, 0.0001)

        assertEquals(0.9909, intDividedByDblFormel.value, 0.0001)
        assertEquals(11.1, dblFormelDividedByInt.value)
        assertEquals(4.0, dblDividedByIntFormel.value)
        assertEquals(5.0, intFormelDividedByDbl.value)

        assertEquals(1.0, intFormelDividedByIntFormel.value)
        assertEquals(0.4954, intFormelDividedByDblFormel.value, 0.0001)
        assertEquals(2.0181, dblFormelDividedByIntFormel.value, 0.0001)
        assertEquals(1.0, dblFormelDividedByDblFormel.value)
    }

    @Test
    fun divisionOperators_DSL() {
        // DSL formula usage
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

        // Assertions
        assertEquals(1.8181, dslIntDividedByIntFormel.value, 0.0001)
        assertEquals(5.5, dslIntFormelDividedByInt.value)
        assertEquals(2.1801, dslDblDividedByDblFormel.value, 0.0001)
        assertEquals(10.0909, dslDblFormelDividedByDbl.value, 0.0001)

        assertEquals(0.9909, dslIntDividedByDblFormel.value, 0.0001)
        assertEquals(11.1, dslDblFormelDividedByInt.value)
        assertEquals(4.0, dslDblDividedByIntFormel.value)
        assertEquals(5.0, dslIntFormelDividedByDbl.value)

        assertEquals(1.0, dslIntFormelDividedByIntFormel.value)
        assertEquals(0.4954, dslIntFormelDividedByDblFormel.value, 0.0001)
        assertEquals(2.0181, dslDblFormelDividedByIntFormel.value, 0.0001)
        assertEquals(1.0, dslDblFormelDividedByDblFormel.value)
    }

    @Test
    fun divisionOperators_Builder() {
        // FormelBuilder usage
        val builderIntDividedByIntFormel = FormelBuilder.create<Double>()
            .name("intDividedByIntFormel")
            .expression(20 / intFormel)
            .build()
        val builderIntFormelDividedByInt = FormelBuilder.create<Double>()
            .name("intFormelDividedByInt")
            .expression(intFormel / 2)
            .build()
        val builderDblDividedByDblFormel = FormelBuilder.create<Double>()
            .name("dblDividedByDblFormel")
            .expression(48.4 / dblFormel)
            .build()
        val builderDblFormelDividedByDbl = FormelBuilder.create<Double>()
            .name("dblFormelDividedByDbl")
            .expression(dblFormel / 2.2)
            .build()

        val builderIntDividedByDblFormel = FormelBuilder.create<Double>()
            .name("intDividedByDblFormel")
            .expression(22 / dblFormel)
            .build()
        val builderDblFormelDividedByInt = FormelBuilder.create<Double>()
            .name("dblFormelDividedByInt")
            .expression(dblFormel / 2)
            .build()
        val builderDblDividedByIntFormel = FormelBuilder.create<Double>()
            .name("dblDividedByIntFormel")
            .expression(44.0 / intFormel)
            .build()
        val builderIntFormelDividedByDbl = FormelBuilder.create<Double>()
            .name("intFormelDividedByDbl")
            .expression(intFormel / 2.2)
            .build()

        val builderIntFormelDividedByIntFormel = FormelBuilder.create<Double>()
            .name("intFormelDividedByIntFormel")
            .expression(intFormel / intFormel)
            .build()
        val builderIntFormelDividedByDblFormel = FormelBuilder.create<Double>()
            .name("intFormelDividedByDblFormel")
            .expression(intFormel / dblFormel)
            .build()
        val builderDblFormelDividedByIntFormel = FormelBuilder.create<Double>()
            .name("dblFormelDividedByIntFormel")
            .expression(dblFormel / intFormel)
            .build()
        val builderDblFormelDividedByDblFormel = FormelBuilder.create<Double>()
            .name("dblFormelDividedByDblFormel")
            .expression(dblFormel / dblFormel)
            .build()

        // Assertions
        assertEquals(1.8181, builderIntDividedByIntFormel.value, 0.0001)
        assertEquals(5.5, builderIntFormelDividedByInt.value)
        assertEquals(2.1801, builderDblDividedByDblFormel.value, 0.0001)
        assertEquals(10.0909, builderDblFormelDividedByDbl.value, 0.0001)

        assertEquals(0.9909, builderIntDividedByDblFormel.value, 0.0001)
        assertEquals(11.1, builderDblFormelDividedByInt.value)
        assertEquals(4.0, builderDblDividedByIntFormel.value)
        assertEquals(5.0, builderIntFormelDividedByDbl.value)

        assertEquals(1.0, builderIntFormelDividedByIntFormel.value)
        assertEquals(0.4954, builderIntFormelDividedByDblFormel.value, 0.0001)
        assertEquals(2.0181, builderDblFormelDividedByIntFormel.value, 0.0001)
        assertEquals(1.0, builderDblFormelDividedByDblFormel.value)
    }


}