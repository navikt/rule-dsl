package no.nav.system.rule.dsl.formel

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FormelExtensionsTest {

    @Test
    fun `named extension creates copy with new name`() {
        val original = Formel.variable("original", 100)
        val renamed = original.named("newName")
        
        assertEquals("newName", renamed.emne)
        assertEquals("original", original.emne)
        assertEquals(100, renamed.value)
        assertNotSame(original, renamed)
    }

    @Test
    fun `locked extension creates locked copy`() {
        val unlocked = Formel.variable("test", 50).copy(locked = false)
        val locked = unlocked.locked()
        
        assertTrue(locked.locked)
        assertFalse(unlocked.locked)
        assertEquals(50, locked.value)
        assertNotSame(unlocked, locked)
    }

    @Test
    fun `unlocked extension creates unlocked copy`() {
        val locked = Formel.variable("test", 75).copy(locked = true)
        val unlocked = locked.unlocked()
        
        assertFalse(unlocked.locked)
        assertTrue(locked.locked)
        assertEquals(75, unlocked.value)
        assertNotSame(locked, unlocked)
    }

    @Test
    fun `withPrefix extension creates copy with prefix`() {
        val original = Formel.variable("test", 200)
        val withPrefix = original.withPrefix("TP")
        
        assertEquals("TP", withPrefix.prefix)
        assertEquals("", original.prefix)
        assertEquals(200, withPrefix.value)
        assertNotSame(original, withPrefix)
    }

    @Test
    fun `withPostfix extension creates copy with postfix`() {
        val original = Formel.variable("test", 300)
        val withPostfix = original.withPostfix("brutto")
        
        assertEquals("brutto", withPostfix.postfix)
        assertEquals("", original.postfix)
        assertEquals(300, withPostfix.value)
        assertNotSame(original, withPostfix)
    }

    @Test
    fun `plusAssign operator throws UnsupportedOperationException`() {
        val formula = Formel.variable("test", 100)
        
        val exception = assertThrows<UnsupportedOperationException> {
            formula += 50
        }
        
        assertEquals("Formulas are immutable. Use 'formula = formula + value' instead of 'formula += value'", exception.message)
    }

    @Test
    fun `minusAssign operator throws UnsupportedOperationException`() {
        val formula = Formel.variable("test", 100)
        
        val exception = assertThrows<UnsupportedOperationException> {
            formula -= 25
        }
        
        assertEquals("Formulas are immutable. Use 'formula = formula - value' instead of 'formula -= value'", exception.message)
    }

    @Test
    fun `timesAssign operator throws UnsupportedOperationException`() {
        val formula = Formel.variable("test", 100)
        
        val exception = assertThrows<UnsupportedOperationException> {
            formula *= 2
        }
        
        assertEquals("Formulas are immutable. Use 'formula = formula * value' instead of 'formula *= value'", exception.message)
    }

    @Test
    fun `divAssign operator throws UnsupportedOperationException`() {
        val formula = Formel.variable("test", 100)
        
        val exception = assertThrows<UnsupportedOperationException> {
            formula /= 4
        }
        
        assertEquals("Formulas are immutable. Use 'formula = formula / value' instead of 'formula /= value'", exception.message)
    }

    @Test
    fun `chaining extensions works correctly`() {
        val original = Formel.variable("original", 123)
        val chained = original
            .named("renamed")
            .withPrefix("TP")
            .withPostfix("brutto")
            .locked()
        
        assertEquals("renamed", chained.emne)
        assertEquals("TP", chained.prefix)
        assertEquals("brutto", chained.postfix)
        assertTrue(chained.locked)
        assertEquals(123, chained.value)
        
        // Original should remain unchanged
        assertEquals("original", original.emne)
        assertEquals("", original.prefix)
        assertEquals("", original.postfix)
        assertFalse(original.locked)
    }

    @Test
    fun `extensions work with Int and Double formulas`() {
        val intFormula = Formel.variable("intTest", 42)
        val doubleFormula = Formel.variable("doubleTest", 42.5)
        
        val renamedInt = intFormula.named("newInt")
        val renamedDouble = doubleFormula.named("newDouble")
        
        assertEquals("newInt", renamedInt.emne)
        assertEquals("newDouble", renamedDouble.emne)
        assertEquals(42, renamedInt.value)
        assertEquals(42.5, renamedDouble.value)
    }
}