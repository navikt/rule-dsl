package no.nav.system.rule.dsl.demo.ruleset

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class PatternRSTest {
    @Test
    fun `skal returnere korrekt sum`() {
        val list1 = mutableListOf(2, 4, 6, 8, 10)
        val list2 = mutableListOf(1, 3, 5, 7, 9)
        val sum = PatternRS(list1, list2).test().get()

        assertEquals(110, sum)
    }
}