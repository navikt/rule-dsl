package no.nav.system.rule.dsl.demo.ruleset

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Regelsett som demonstrerer rulechaining.
 */
class RuleChainingRSTest {

    @Test
    fun `test hvor førstRegel kjører`() {
        val ruleChainingRS = RuleChainingRS(true).test().get()
        assertEquals(1003, ruleChainingRS)
    }

    @Test
    fun `test hvor førsteRegel ikke kjører`() {
        val ruleChainingRS = RuleChainingRS(false).test().get()
        assertEquals(999, ruleChainingRS)
    }

}
