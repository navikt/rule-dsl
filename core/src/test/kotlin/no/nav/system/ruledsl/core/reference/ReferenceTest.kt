package no.nav.system.ruledsl.core.reference

import no.nav.system.ruledsl.core.forklaring.forklar
import no.nav.system.ruledsl.core.model.arc.DslDomainPredicate
import no.nav.system.ruledsl.core.operators.times
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.model.arc.AbstractRuleset
import no.nav.system.ruledsl.core.operators.erStørreEllerLik
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class ReferenceTest {

    @Test
    fun `skal opprette Reference med id og url`() {
        val ref = Reference(
            id = "FTL-20-18",
            url = "https://lovdata.no/dokument/NL/lov/1997-02-28-19/§20-18"
        )

        assertEquals("FTL-20-18", ref.id)
        assertEquals("https://lovdata.no/dokument/NL/lov/1997-02-28-19/§20-18", ref.url)
    }

    @Test
    fun `skal legge til reference via Faktum constructor`() {
        val ref = Reference("FTL-20-7", "https://lovdata.no/...")

        val faktum = Faktum(
            navn = "aldersgrense",
            verdi = 67,
            references = listOf(ref)
        )

        assertEquals(1, faktum.references.size)
        assertEquals("FTL-20-7", faktum.references[0].id)
        assertEquals(67, faktum.verdi)
    }

    @Test
    fun `skal legge til reference via fluent ref() med Reference object`() {
        val ref = Reference("FTL-20-18", "https://lovdata.no/...")

        val faktum = Faktum("slitertillegg", 2500.0)
            .ref(ref)

        assertEquals(1, faktum.references.size)
        assertEquals("FTL-20-18", faktum.references[0].id)
        assertEquals(2500.0, faktum.verdi)
    }

    @Test
    fun `skal legge til reference via fluent ref() med id og url direkte`() {
        val faktum = Faktum("aldersgrense", 67)
            .ref("FTL-20-7", "https://lovdata.no/...")

        assertEquals(1, faktum.references.size)
        assertEquals("FTL-20-7", faktum.references[0].id)
        assertEquals("https://lovdata.no/...", faktum.references[0].url)
        assertEquals(67, faktum.verdi)
    }

    @Test
    fun `skal kunne chaine flere references med fluent API`() {
        val faktum = Faktum("beregning", 1000)
            .ref("FTL-20-18", "https://lovdata.no/ftl-20-18")
            .ref("RUNDSKRIV-2024", "https://nav.no/rundskriv")
            .ref("CONF-SLITER", "https://confluence.nav.no/sliter")

        assertEquals(3, faktum.references.size)
        assertEquals("FTL-20-18", faktum.references[0].id)
        assertEquals("RUNDSKRIV-2024", faktum.references[1].id)
        assertEquals("CONF-SLITER", faktum.references[2].id)
    }

    @Test
    fun `skal kunne legge til reference på Faktum med formel`() {
        val G = Faktum("G", 110000)
        val faktor = Faktum("faktor", 0.25)

        val beregning = Faktum("fulltSlitertillegg", faktor * G)
            .ref("FTL-20-18-FORMEL", "https://lovdata.no/...")

        assertEquals(1, beregning.references.size)
        assertEquals("FTL-20-18-FORMEL", beregning.references[0].id)
        assertEquals(27500.0, beregning.verdi)
    }

    @Test
    fun `skal ha tom referanseliste som default`() {
        val faktum = Faktum("test", 42)

        assertTrue(faktum.references.isEmpty())
    }

    @Test
    fun `skal kunne kopiere Faktum og beholde eksisterende references`() {
        val faktum1 = Faktum("original", 100)
            .ref("REF-1", "https://example.com/1")

        val faktum2 = faktum1.copy(navn = "kopi")

        // Original should be unchanged
        assertEquals(1, faktum1.references.size)
        assertEquals("REF-1", faktum1.references[0].id)

        // Copy should have same references
        assertEquals(1, faktum2.references.size)
        assertEquals("REF-1", faktum2.references[0].id)

        // But different names
        assertEquals("original", faktum1.navn)
        assertEquals("kopi", faktum2.navn)
    }

    @Test
    fun `skal kunne legge til flere references på kopieret Faktum`() {
        val faktum1 = Faktum("original", 100)
            .ref("REF-1", "https://example.com/1")

        val faktum2 = faktum1
            .ref("REF-2", "https://example.com/2")

        // Original should still have only one reference
        assertEquals(1, faktum1.references.size)

        // New faktum should have both
        assertEquals(2, faktum2.references.size)
        assertEquals("REF-1", faktum2.references[0].id)
        assertEquals("REF-2", faktum2.references[1].id)
    }

    // ========================================
    // Rule Reference Tests
    // ========================================

    @OptIn(DslDomainPredicate::class)
    @Test
    fun `skal kunne legge til reference på Rule via REF DSL`() {
        class TestRuleset : AbstractRuleset<Unit>() {
            override fun create() {
                val alder = sporing("alder", 70)
                val aldersgrense = sporing("aldersgrense", 67)

                regel("VILKÅR-ALDER") {
                    REF("FTL-20-18", "https://lovdata.no/forskrift/2014-12-19-1819/§20-18")
                    HVIS { alder erStørreEllerLik aldersgrense }
                    SÅ { sporing("aldersvilkår", true) }
                }
            }
        }

        val ruleset = TestRuleset()
        ruleset.test()

        // Find the rule
        val rule = ruleset.children.first { it.name() == "TestRuleset.VILKÅR-ALDER" }

        // Verify reference was added
        assertEquals(1, rule.references.size)
        assertEquals("FTL-20-18", rule.references[0].id)
        assertEquals("https://lovdata.no/forskrift/2014-12-19-1819/§20-18", rule.references[0].url)
    }

    @OptIn(DslDomainPredicate::class)
    @Test
    fun `skal kunne legge til flere references på samme Rule`() {
        class TestRuleset : AbstractRuleset<Unit>() {
            override fun create() {
                val alder = sporing("alder", 70)
                val aldersgrense = sporing("aldersgrense", 67)

                regel("VILKÅR-ALDER") {
                    REF("FTL-20-18", "https://lovdata.no/forskrift/2014-12-19-1819/§20-18")
                    REF("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT", "https://confluence.nav.no/sliter")
                    REF("RUNDSKRIV-2024", "https://nav.no/rundskriv")
                    HVIS { alder erStørreEllerLik aldersgrense }
                    SÅ { sporing("aldersvilkår", true) }
                }
            }
        }

        val ruleset = TestRuleset()
        ruleset.test()

        // Find the rule
        val rule = ruleset.children.first { it.name() == "TestRuleset.VILKÅR-ALDER" }

        // Verify all three references were added
        assertEquals(3, rule.references.size)
        assertEquals("FTL-20-18", rule.references[0].id)
        assertEquals("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT", rule.references[1].id)
        assertEquals("RUNDSKRIV-2024", rule.references[2].id)
    }

    @OptIn(DslDomainPredicate::class)
    @Test
    fun `Rule uten REF skal ha tom referanseliste`() {
        class TestRuleset : AbstractRuleset<Unit>() {
            override fun create() {
                val alder = sporing("alder", 70)
                val aldersgrense = sporing("aldersgrense", 67)

                regel("VILKÅR-ALDER") {
                    HVIS { alder erStørreEllerLik aldersgrense }
                    SÅ { sporing("aldersvilkår", true) }
                }
            }
        }

        val ruleset = TestRuleset()
        ruleset.test()

        // Find the rule
        val rule = ruleset.children.first { it.name() == "TestRuleset.VILKÅR-ALDER" }

        // Verify no references
        assertTrue(rule.references.isEmpty())
    }

    // ========================================
    // Tracker Integration Tests
    // ========================================

    @OptIn(DslDomainPredicate::class)
    @Test
    fun `forklar skal vise REFERENCES i HVORFOR section`() {
        class TestRulesetWithReferences : AbstractRuleset<Unit>() {
            lateinit var aldersvilkår: Faktum<Boolean>

            override fun create() {
                val alder = sporing("alder", 70)
                val aldersgrense = sporing("aldersgrense", 67)

                regel("VILKÅR-ALDER") {
                    HVIS { alder erStørreEllerLik aldersgrense }
                    SÅ {
                        aldersvilkår = sporing("aldersvilkår", true)
                    }
                    REF("FTL-20-18", "https://lovdata.no/forskrift/2014-12-19-1819/§20-18")
                    REF("SLITERTILLEGG-JUSTERING", "https://confluence.nav.no/sliter")
                }
            }
        }

        val ruleset = TestRulesetWithReferences()
        ruleset.test()

        val explanation = ruleset.aldersvilkår.forklar()

        println("=== Explanation with REFERENCES ===")
        println(explanation)
        println("=== End ===")

        // Verify the explanation contains REFERENCES section
        assertTrue(explanation.contains("REFERENCES:"), "Should contain REFERENCES header")
        assertTrue(explanation.contains("FTL-20-18: https://lovdata.no/forskrift/2014-12-19-1819/§20-18"), "Should contain first reference")
        assertTrue(explanation.contains("SLITERTILLEGG-JUSTERING: https://confluence.nav.no/sliter"), "Should contain second reference")
    }

    @OptIn(DslDomainPredicate::class)
    @Test
    fun `forklar skal IKKE vise REFERENCES section når regel mangler references`() {
        class TestRulesetNoReferences : AbstractRuleset<Unit>() {
            lateinit var aldersvilkår: Faktum<Boolean>

            override fun create() {
                val alder = sporing("alder", 70)
                val aldersgrense = sporing("aldersgrense", 67)

                regel("VILKÅR-ALDER") {
                    // NO REF() calls
                    HVIS { alder erStørreEllerLik aldersgrense }
                    SÅ { aldersvilkår = sporing("aldersvilkår", true) }
                }
            }
        }

        val ruleset = TestRulesetNoReferences()
        ruleset.test()

        val explanation = ruleset.aldersvilkår.forklar()

        println("=== Explanation without REFERENCES ===")
        println(explanation)
        println("=== End ===")

        // Verify the explanation does NOT contain REFERENCES section
        assertFalse(explanation.contains("REFERENCES:"), "Should NOT contain REFERENCES header when no references exist")
    }
}
