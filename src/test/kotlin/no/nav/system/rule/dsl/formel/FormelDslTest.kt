package no.nav.system.rule.dsl.formel

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FormelDslTest {

    @Test
    fun `variable DSL function creates named variable formula`() {
        val grunnbeløp = variable("grunnbeløp", 118620)
        
        assertEquals("grunnbeløp", grunnbeløp.emne)
        assertEquals(118620, grunnbeløp.resultat())
        assertEquals("grunnbeløp", grunnbeløp.notasjon)
        assertEquals("118620", grunnbeløp.innhold)
    }

    @Test
    fun `variable DSL function works with Double values`() {
        val rate = variable("rate", 2.45)
        
        assertEquals("rate", rate.emne)
        assertEquals(2.45, rate.resultat())
        assertEquals("rate", rate.notasjon)
        assertEquals("2.45", rate.innhold)
    }

    @Test
    fun `constant DSL function creates constant formula`() {
        val constant42 = constant(42)
        
        assertTrue(constant42.emne.startsWith("anonymous"))
        assertEquals(42, constant42.resultat())
        assertEquals("42", constant42.notasjon)
        assertEquals("42", constant42.innhold)
    }

    @Test
    fun `constant DSL function works with Double values`() {
        val pi = constant(3.14159)
        
        assertTrue(pi.emne.startsWith("anonymous"))
        assertEquals(3.14159, pi.resultat())
        assertEquals("3.14159", pi.notasjon)
        assertEquals("3.14159", pi.innhold)
    }

    @Test
    fun `named formula DSL creates complex formula with name`() {
        val grunnbeløp = variable("G", 100000)
        val sats = variable("sats", 0.45)
        
        val bruttobeløp = formula<Double>("bruttobeløp") {
            prefix("TP")
            postfix("årlig")
            expression(grunnbeløp * sats)
            locked()
        }
        
        assertEquals("bruttobeløp", bruttobeløp.emne)
        assertEquals("TP", bruttobeløp.prefix)
        assertEquals("årlig", bruttobeløp.postfix)
        assertTrue(bruttobeløp.locked)
        assertEquals(45000.0, bruttobeløp.resultat())
        assertEquals("G * sats", bruttobeløp.notasjon)
        assertEquals("100000 * 0.45", bruttobeløp.innhold)
    }

    @Test
    fun `named formula DSL works with Int type`() {
        val base = variable("base", 1000)
        val multiplier = variable("multiplier", 5)
        
        val total = formula<Int>("total") {
            expression(base * multiplier)
            unlocked()
        }
        
        assertEquals("total", total.emne)
        assertFalse(total.locked)
        assertEquals(5000, total.resultat())
        assertEquals("base * multiplier", total.notasjon)
        assertEquals("1000 * 5", total.innhold)
    }

    @Test
    fun `anonymous formula DSL creates complex formula without name`() {
        val a = variable("a", 10)
        val b = variable("b", 20)
        
        val sum = formula<Int> {
            expression(a + b)
            prefix("CALC")
            unlocked()
        }
        
        assertTrue(sum.emne.startsWith("anonymous"))
        assertEquals("CALC", sum.prefix)
        assertFalse(sum.locked)
        assertEquals(30, sum.resultat())
        assertEquals("a + b", sum.notasjon)
        assertEquals("10 + 20", sum.innhold)
    }

    @Test
    fun `anonymous formula DSL works with Double type`() {
        val x = variable("x", 5.5)
        val y = variable("y", 2.0)
        
        val division = formula<Double> {
            expression(x / y)
            postfix("result")
        }
        
        assertTrue(division.emne.startsWith("anonymous"))
        assertEquals("result", division.postfix)
        assertTrue(division.locked) // Default for builder formulas
        assertEquals(2.75, division.resultat())
        assertEquals("x / y", division.notasjon)
        assertEquals("5.5 / 2.0", division.innhold)
    }

    @Test
    fun `DSL functions equivalent to factory methods`() {
        // Variable DSL vs factory method
        val dslVariable = variable("test", 123)
        val factoryVariable = Formel.variable("test", 123)
        
        assertEquals(dslVariable.emne, factoryVariable.emne)
        assertEquals(dslVariable.resultat(), factoryVariable.resultat())
        assertEquals(dslVariable.notasjon, factoryVariable.notasjon)
        
        // Constant DSL vs factory method
        val dslConstant = constant(456)
        val factoryConstant = Formel.constant(456)
        
        assertEquals(dslConstant.resultat(), factoryConstant.resultat())
        assertEquals(dslConstant.notasjon, factoryConstant.notasjon)
    }

    @Test
    fun `complex DSL example from documentation`() {
        val grunnbeløp = variable("grunnbeløp", 118620)
        val sats = variable("sats", 0.45)
        
        val complex = formula<Double>("total") {
            prefix("TP")
            postfix("årlig") 
            expression(grunnbeløp * sats)
            locked()
        }
        
        assertEquals("total", complex.emne)
        assertEquals("TP", complex.prefix)
        assertEquals("årlig", complex.postfix)
        assertTrue(complex.locked)
        assertEquals(53379.0, complex.resultat())
        assertEquals("grunnbeløp * sats", complex.notasjon)
        assertEquals("118620 * 0.45", complex.innhold)
    }

    @Test
    fun `DSL formula with complex expression using avrund function`() {
        val base = variable("base", 1234.56)
        
        val rounded = formula<Int>("rounded") {
            expression(avrund(base))
        }
        
        assertEquals("rounded", rounded.emne)
        assertEquals(1235, rounded.resultat())
        assertEquals("avrund( base )", rounded.notasjon)
        assertEquals("avrund( 1234.56 )", rounded.innhold)
    }

    @Test
    fun `chaining DSL with extension functions`() {
        val original = variable("original", 100)
        
        val modified = original
            .named("newName")
            .withPrefix("CALC")
            .locked()
        
        assertEquals("newName", modified.emne)
        assertEquals("CALC", modified.prefix)
        assertTrue(modified.locked)
        assertEquals(100, modified.resultat())
        
        // Original should be unchanged
        assertEquals("original", original.emne)
        assertEquals("", original.prefix)
        assertFalse(original.locked)
    }
}