package no.nav.system.rule.dsl.demo.ruleset

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class NoReturnValueRSTest {

    @Test
    fun `test hvor førstRegel kjører`() {
        assertThrows<ClassCastException> {
            NoReturnValueRS().test() + 1
        }
    }
}
