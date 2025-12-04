package no.nav.system.ruledsl.core.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NoReturnValueRSTest {

    /**
     * Regelsett med type Int uten returnverdi er ugyldig.
     */
    class NoReturnValueRS : AbstractRuleset<Int>() {
        override fun create() {}
    }

    @Test
    fun `test hvor førstRegel kjører`() {
        assertThrows<ClassCastException> {
            NoReturnValueRS().test() + 1
        }
    }
}
