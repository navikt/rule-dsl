package no.nav.system.rule.dsl.demo.visitor

import no.nav.system.rule.dsl.demo.domain.Boperiode
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.demo.domain.Request
import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.demo.ruleservice.BeregnAlderspensjonService
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.treevisitor.visitor.debug
import no.nav.system.rule.dsl.treevisitor.visitor.xmlDebug
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VisitorTest {

    @Test
    fun `debug visitor test`() {
        val params = Request(
            virkningstidspunkt = localDate(1990, 5, 1),
            person = Person(
                id = 1,
                fødselsdato = Faktum("Fødselsdato", localDate(1974, 3, 3)),
                erGift = true,
                boperioder = listOf(
                    Boperiode(fom = localDate(1990, 1, 1), tom = localDate(2003, 12, 31), LandEnum.NOR),
                    Boperiode(fom = localDate(2004, 1, 1), tom = localDate(2010, 12, 31), LandEnum.SWE),
                    Boperiode(fom = localDate(2011, 1, 1), tom = localDate(2015, 12, 31), LandEnum.NOR),
                    Boperiode(fom = localDate(2016, 1, 1), tom = localDate(2020, 12, 31), LandEnum.SWE)
                )
            )
        )

        val service = BeregnAlderspensjonService(params).also { it.run() }
        val txt = service.debug()

        assertEquals("""
            ruleservice: BeregnAlderspensjonService
              ruleflow: BeregnAlderspensjonFlyt
                ruleset: BeregnFaktiskTrygdetidRS
                  rule: JA BeregnFaktiskTrygdetidRS.BoPeriodeStartFør16år.1 
                  rule: NEI BeregnFaktiskTrygdetidRS.BoPeriodeStartFør16år.2 
                  rule: NEI BeregnFaktiskTrygdetidRS.BoPeriodeStartFom16år.1 
                  rule: JA BeregnFaktiskTrygdetidRS.BoPeriodeStartFom16år.2 
                  rule: JA BeregnFaktiskTrygdetidRS.SettFireFemtedelskrav 
                  rule: NEI BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid  utfallType: IKKE_OPPFYLT
                    subsumsjon: NEI 'virkningstidspunkt' (1990-05-01) må være fom '1991-01-01'
                    subsumsjon: JA 'faktisk trygdetid i måneder' (224) er mindre enn 'firefemtedelskrav' (480)
                  rule: JA BeregnFaktiskTrygdetidRS.FastsettTrygdetid 
                  rule: JA BeregnFaktiskTrygdetidRS.ReturnRegel 
                decision: BeregnAlderspensjonFlyt.Sivilstand gift?
                  branch: JA BeregnAlderspensjonFlyt.Sivilstand gift?/branch 0
                  branch: NEI BeregnAlderspensjonFlyt.Sivilstand gift?/branch 1
                ruleset: BeregnGrunnpensjonRS
                  rule: NEI BeregnGrunnpensjonRS.FullTrygdetid 
                  rule: JA BeregnGrunnpensjonRS.RedusertTrygdetid""".trimIndent(), txt)
    }
    @Test
    fun `XML debug visitor test`() {
        val params = Request(
            virkningstidspunkt = localDate(1990, 5, 1),
            person = Person(
                id = 1,
                fødselsdato = Faktum("Fødselsdato", localDate(1974, 3, 3)),
                erGift = true,
                boperioder = listOf(
                    Boperiode(fom = localDate(1990, 1, 1), tom = localDate(2003, 12, 31), LandEnum.NOR),
                    Boperiode(fom = localDate(2004, 1, 1), tom = localDate(2010, 12, 31), LandEnum.SWE),
                    Boperiode(fom = localDate(2011, 1, 1), tom = localDate(2015, 12, 31), LandEnum.NOR),
                    Boperiode(fom = localDate(2016, 1, 1), tom = localDate(2020, 12, 31), LandEnum.SWE)
                )
            )
        )

        val service = BeregnAlderspensjonService(params).also { it.run() }
        val xml = service.xmlDebug()

        assertEquals("""
            <BeregnAlderspensjonService>
              <BeregnAlderspensjonFlyt>
                <BeregnFaktiskTrygdetidRS>
                  </BoPeriodeStartFør16år.1 fired=true>
                  </BoPeriodeStartFør16år.2 fired=false>
                  </BoPeriodeStartFom16år.1 fired=false>
                  </BoPeriodeStartFom16år.2 fired=true>
                  </SettFireFemtedelskrav fired=true>
                  </Skal_ha_redusert_fremtidig_trygdetid fired=false comment="Dersom faktisk trygdetid i Norge er mindre enn 4/5 av opptjeningstiden skal den framtidige trygdetiden være redusert.">
                    <subsumsjon fired=false>NEI 'virkningstidspunkt' (1990-05-01) må være fom '1991-01-01'</predicate>
                    <subsumsjon fired=true>JA 'faktisk trygdetid i måneder' (224) er mindre enn 'firefemtedelskrav' (480)</predicate>
                  </FastsettTrygdetid fired=true>
                  </ReturnRegel fired=true>
                </BeregnFaktiskTrygdetidRS>
                <BeregnAlderspensjonFlyt.Sivilstand gift?>
                  <BeregnAlderspensjonFlyt.Sivilstand gift?/branch 0 fired=true>
                  </BeregnAlderspensjonFlyt.Sivilstand gift?/branch 0>
                  <BeregnAlderspensjonFlyt.Sivilstand gift?/branch 1 fired=false>
                  </BeregnAlderspensjonFlyt.Sivilstand gift?/branch 1>
                </BeregnAlderspensjonFlyt.Sivilstand gift?>
                <BeregnGrunnpensjonRS>
                  </FullTrygdetid fired=false>
                  </RedusertTrygdetid fired=true>
                </BeregnGrunnpensjonRS>
              </BeregnAlderspensjonFlyt>
            </BeregnAlderspensjonService>""".trimIndent(), xml)

    }
}