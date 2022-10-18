package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.UtfallType.IKKE_OPPFYLT
import no.nav.system.rule.dsl.rettsregel.UtfallType.OPPFYLT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DomainPatternRSTest {

    @Test
    internal fun `skla returnere ikke_oppfylt når ingen regler treffer`() {

        val faktumListe = mutableListOf(
            Faktum("bool1", true),
            Faktum("bool2", true),
            Faktum("bool3", true),
            Faktum("bool4", false)
        )

        val utfall = DomainPatternRS(faktumListe).let {
            val x = it.test()
            println(x)
            x
        }.get()

        assertEquals(IKKE_OPPFYLT, utfall.utfallType)
    }

    @Test
    internal fun `skal returnere oppfylt når regelen 'sann' treffer`() {

        val faktumListe = mutableListOf(
            Faktum("bool1", true),
            Faktum("bool2", true),
            Faktum("bool3", true),
            Faktum("bool4", true)
        )

        val utfall = DomainPatternRS(faktumListe).test().get()

        assertEquals(OPPFYLT, utfall.utfallType)
    }

    @Test
    internal fun `skal returnere ikke_oppfylt når regelen 'usann' treffer`() {

        val faktumListe = mutableListOf(
            Faktum("bool1", false),
            Faktum("bool2", false),
            Faktum("bool3", false),
            Faktum("bool4", false)
        )

        val utfall = DomainPatternRS(faktumListe).test().get()

        assertEquals(IKKE_OPPFYLT, utfall.utfallType)
    }


}