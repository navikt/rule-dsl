package no.nav.pensjon.regler.alderspensjon.ruleset

import no.nav.pensjon.regler.alderspensjon.domain.Boperiode
import no.nav.pensjon.regler.alderspensjon.domain.koder.LandEnum
import no.nav.pensjon.regler.alderspensjon.domain.koder.UtfallType
import no.nav.system.ruledsl.core.inspections.find
import no.nav.system.ruledsl.core.model.arc.TrackablePredicate
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.model.arc.Rule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Prototype for produksjon av regelsporing på predikatnivå.
 */
class BeregnFaktiskTrygdetidRSTest {

    @Test
    fun `test fagregel 'Redusert fremtidig trygdetid' har truffet`() {
        val result = BeregnFaktiskTrygdetidRS(
            fødselsdato = Faktum("Fødselsdato", LocalDate.of(1990, 1, 1)),
            virkningstidspunkt = Faktum("virkningstidspunkt", LocalDate.of(2000, 1, 1)),
            boperiodeListe = listOf(
                Boperiode(fom = LocalDate.of(1990, 1, 1), tom = LocalDate.of(2018, 12, 31), LandEnum.NOR)
            ),
            flyktningUtfall = Faktum("Anvendt flyktning", UtfallType.IKKE_OPPFYLT)
        ).run {
            test()
            find { regel -> regel.name() == "BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid" }
        }

        assertTrue(result.isNotEmpty())
        val redFttRegel = result.first() as Rule<*>
        assertTrue(redFttRegel.fired())
        (redFttRegel.children[0] as? TrackablePredicate)?.let { pdp ->
            assertEquals("predikat: JA 'virkningstidspunkt' (2000-01-01) er etter eller lik 'januar 1991' (1991-01-01)", pdp.toString())
            assertEquals("JA 'virkningstidspunkt' er etter eller lik 'januar 1991'", pdp.notasjon())
            assertEquals("JA '2000-01-01' er etter eller lik '1991-01-01'", pdp.konkret())

        }
        (redFttRegel.children[1] as? TrackablePredicate)?.let { pdp ->
            assertEquals("predikat: JA 'faktisk trygdetid i måneder' (155) er mindre enn 'firefemtedelskrav' (480)", pdp.toString())
            assertEquals("JA 'faktisk trygdetid i måneder' er mindre enn 'firefemtedelskrav'", pdp.notasjon())
            assertEquals("JA '155' er mindre enn '480'", pdp.konkret())
        }
    }

    @Test
    fun `test fagregel 'Redusert fremtidig trygdetid' har ikke truffet`() {
        val result = BeregnFaktiskTrygdetidRS(
            fødselsdato = Faktum("Fødselsdato", LocalDate.of(1990, 1, 1)),
            virkningstidspunkt = Faktum("virkningstidspunkt", LocalDate.of(2000, 1, 1)),
            boperiodeListe = listOf(
                Boperiode(fom = LocalDate.of(1990, 1, 1), tom = LocalDate.of(2048, 12, 31), LandEnum.NOR)
            ),
            flyktningUtfall = Faktum("Anvendt flyktning", UtfallType.IKKE_OPPFYLT)
        ).run {
            test()
            find { regel -> regel.name() == "BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid" }
        }

        assertTrue(result.isNotEmpty())
        val regelSkalHaRedusertFremtidigTrygdetid = result.first() as Rule<*>
        assertFalse(regelSkalHaRedusertFremtidigTrygdetid.fired())
        (regelSkalHaRedusertFremtidigTrygdetid.children[0] as? TrackablePredicate)?.let { pdp ->
            assertEquals("predikat: JA 'virkningstidspunkt' (2000-01-01) er etter eller lik 'januar 1991' (1991-01-01)", pdp.toString())
            assertEquals("JA 'virkningstidspunkt' er etter eller lik 'januar 1991'", pdp.notasjon())
            assertEquals("JA '2000-01-01' er etter eller lik '1991-01-01'", pdp.konkret())
        }
        (regelSkalHaRedusertFremtidigTrygdetid.children[1] as? TrackablePredicate)?.let { pdp ->
            assertEquals("predikat: NEI 'faktisk trygdetid i måneder' (515) må være mindre enn 'firefemtedelskrav' (480)", pdp.toString())
            assertEquals("NEI 'faktisk trygdetid i måneder' må være mindre enn 'firefemtedelskrav'", pdp.notasjon())
            assertEquals("NEI '515' må være mindre enn '480'", pdp.konkret())
        }
    }
}