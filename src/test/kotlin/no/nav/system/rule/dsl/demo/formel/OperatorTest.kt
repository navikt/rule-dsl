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
        assertEquals(12, builderIntPlusIntFormel.resultat())
        assertEquals(12, builderIntFormelPlusInt.resultat())
        assertEquals(24.4, builderDblPlusDblFormel.resultat())
        assertEquals(24.4, builderDblFormelPlusDbl.resultat())

        assertEquals(23.2, builderIntPlusDblFormel.resultat())
        assertEquals(23.2, builderDblFormelPlusInt.resultat())
        assertEquals(13.2, builderDblPlusIntFormel.resultat())
        assertEquals(13.2, builderIntFormelPlusDbl.resultat())

        assertEquals(22, builderIntFormelPlusIntFormel.resultat())
        assertEquals(33.2, builderIntFormelPlusDblFormel.resultat())
        assertEquals(33.2, builderDblFormelPlusIntFormel.resultat())
        assertEquals(44.4, builderDblFormelPlusDblFormel.resultat())
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
        assertEquals(-10, builderIntMinusIntFormel.resultat())
        assertEquals(10, builderIntFormelMinusInt.resultat())
        assertEquals(-20.0, builderDblMinusDblFormel.resultat())
        assertEquals(20.0, builderDblFormelMinusDbl.resultat())

        assertEquals(-21.2, builderIntMinusDblFormel.resultat())
        assertEquals(21.2, builderDblFormelMinusInt.resultat())
        assertEquals(-8.8, builderDblMinusIntFormel.resultat())
        assertEquals(8.8, builderIntFormelMinusDbl.resultat())

        assertEquals(0, builderIntFormelMinusIntFormel.resultat())
        assertEquals(-11.2, builderIntFormelMinusDblFormel.resultat())
        assertEquals(11.2, builderDblFormelMinusIntFormel.resultat())
        assertEquals(0.0, builderDblFormelMinusDblFormel.resultat())
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
        assertEquals(22, builderIntTimesIntFormel.resultat())
        assertEquals(22, builderIntFormelTimesInt.resultat())
        assertEquals(48.84, builderDblTimesDblFormel.resultat())
        assertEquals(48.84, builderDblFormelTimesDbl.resultat())

        assertEquals(44.4, builderIntTimesDblFormel.resultat())
        assertEquals(44.4, builderDblFormelTimesInt.resultat())
        assertEquals(24.20, builderDblTimesIntFormel.resultat(), 0.0001)
        assertEquals(24.20, builderIntFormelTimesDbl.resultat(), 0.0001)

        assertEquals(121, builderIntFormelTimesIntFormel.resultat())
        assertEquals(244.2, builderIntFormelTimesDblFormel.resultat())
        assertEquals(244.2, builderDblFormelTimesIntFormel.resultat())
        assertEquals(492.84, builderDblFormelTimesDblFormel.resultat())
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
        assertEquals(1.8181, builderIntDividedByIntFormel.resultat(), 0.0001)
        assertEquals(5.5, builderIntFormelDividedByInt.resultat())
        assertEquals(2.1801, builderDblDividedByDblFormel.resultat(), 0.0001)
        assertEquals(10.0909, builderDblFormelDividedByDbl.resultat(), 0.0001)

        assertEquals(0.9909, builderIntDividedByDblFormel.resultat(), 0.0001)
        assertEquals(11.1, builderDblFormelDividedByInt.resultat())
        assertEquals(4.0, builderDblDividedByIntFormel.resultat())
        assertEquals(5.0, builderIntFormelDividedByDbl.resultat())

        assertEquals(1.0, builderIntFormelDividedByIntFormel.resultat())
        assertEquals(0.4954, builderIntFormelDividedByDblFormel.resultat(), 0.0001)
        assertEquals(2.0181, builderDblFormelDividedByIntFormel.resultat(), 0.0001)
        assertEquals(1.0, builderDblFormelDividedByDblFormel.resultat())
    }


}