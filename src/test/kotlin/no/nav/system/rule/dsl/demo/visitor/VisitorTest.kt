package no.nav.system.rule.dsl.demo.visitor

import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.demo.domain.Boperiode
import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.demo.domain.Request
import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.demo.ruleservice.BeregnAlderspensjonService
import no.nav.system.rule.dsl.demo.ruleset.PersonenErFlyktningRS
import no.nav.system.rule.dsl.enums.RuleComponentType.REGELSETT
import no.nav.system.rule.dsl.resource.root
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.visitor.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VisitorTest {
    private val serviceRequest = Request(
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
    private val service = BeregnAlderspensjonService(serviceRequest).also { it.run() }


    @Test
    fun `find test`() {
        val result = service.find { arc -> arc.type() == REGELSETT }
        assertEquals(3, result.size)
    }

    @Test
    fun `debug visitor test`() {
        assertEquals(
            """
regeltjeneste: BeregnAlderspensjonService
  regelflyt: BeregnAlderspensjonFlyt
    regelsett: PersonenErFlyktningRS
      regel: JA PersonenErFlyktningRS.SettRelevantTrygdetid_kap19
        JA 'Kapittel 20' (false) er lik 'false'
      regel: NEI PersonenErFlyktningRS.SettRelevantTrygdetid_kap20
        NEI 'Kapittel 20' (false) må være lik 'true'
      regel: NEI PersonenErFlyktningRS.AngittFlyktning_HarFlyktningFlaggetSatt
        NEI 'Angitt flyktning' (false) må være lik 'true'
      regel: NEI PersonenErFlyktningRS.AngittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning
      regel: NEI PersonenErFlyktningRS.AngittFlyktning_HarUnntakFraForutgaendeTTTypeFlyktning
      regel: NEI PersonenErFlyktningRS.Overgangsregel_AP
        NEI 'Fødselsdato' (1974-03-03) må være mindre eller lik '1959'
        NEI 'Faktisk trygdetid før 2021' (0) må være større eller lik '20'
      regel: NEI PersonenErFlyktningRS.Overgangsregel_AP_tidligereUT
        NEI 'Fødselsdato' (1974-03-03) må være mindre eller lik '1959'
        NEI 'virkningstidspunkt' (1990-05-01) må være etter eller lik 'Fødselsdato67m' (2041-04-01)
        NEI 'Faktisk trygdetid før 2021' (0) må være større eller lik '20'
        NEI 'Uføretrygd før 2021' (false) må være lik 'true'
      regel: NEI PersonenErFlyktningRS.Overgangsregel_AP_tidligereGJP
        NEI 'Fødselsdato' (1974-03-03) må være mindre eller lik '1959'
        NEI 'virkningstidspunkt' (1990-05-01) må være etter eller lik 'Fødselsdato67m' (2041-04-01)
        NEI 'Faktisk trygdetid før 2021' (0) må være større eller lik '20'
        NEI 'Gjenlevendepensjon før 2021' (false) må være lik 'true'
      regel: NEI PersonenErFlyktningRS.Overgangsregel_GJR_tidligereUT_GJT
      regel: NEI PersonenErFlyktningRS.Overgangsregel_GJR_tidligereGJR
      regel: JA PersonenErFlyktningRS.AnvendtFlyktning_ikkeRelevant
        JA 'Regelreferanse' (AngittFlyktning) ingen [regel: NEI PersonenErFlyktningRS.AngittFlyktning_HarFlyktningFlaggetSatt]
          regel: NEI PersonenErFlyktningRS.AngittFlyktning_HarFlyktningFlaggetSatt
            NEI 'Angitt flyktning' (false) må være lik 'true'
    regelsett: BeregnFaktiskTrygdetidRS
      regel: JA BeregnFaktiskTrygdetidRS.BoPeriodeStartFør16år.1
      regel: NEI BeregnFaktiskTrygdetidRS.BoPeriodeStartFør16år.2
      regel: NEI BeregnFaktiskTrygdetidRS.BoPeriodeStartFom16år.1
      regel: JA BeregnFaktiskTrygdetidRS.BoPeriodeStartFom16år.2
      regel: JA BeregnFaktiskTrygdetidRS.SettFireFemtedelskrav
      regel: NEI BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid
        NEI 'virkningstidspunkt' (1990-05-01) må være etter eller lik '1991-01-01'
        JA 'faktisk trygdetid i måneder' (224) er mindre enn 'firefemtedelskrav' (480)
      regel: JA BeregnFaktiskTrygdetidRS.FastsettTrygdetid_ikkeFlyktning
        JA 'Anvendt flyktning' (IKKE_RELEVANT) er ulik 'OPPFYLT'
      regel: NEI BeregnFaktiskTrygdetidRS.FastsettTrygdetid_Flyktning
        NEI 'Anvendt flyktning' (IKKE_RELEVANT) må være lik 'OPPFYLT'
      regel: JA BeregnFaktiskTrygdetidRS.ReturnRegel
    forgrening: BeregnAlderspensjonFlyt.Sivilstand gift?
      gren: JA BeregnAlderspensjonFlyt.Sivilstand gift?/gren 0
      gren: NEI BeregnAlderspensjonFlyt.Sivilstand gift?/gren 1
    regelsett: BeregnGrunnpensjonRS
      regel: NEI BeregnGrunnpensjonRS.FullTrygdetid
      regel: JA BeregnGrunnpensjonRS.RedusertTrygdetid
                      """.trimIndent(), service.debug()
        )
    }

    @Test
    fun `XML debug visitor test`() {
        assertEquals(
            """
<BeregnAlderspensjonService>
  <BeregnAlderspensjonFlyt>
    <PersonenErFlyktningRS>
      <SettRelevantTrygdetid_kap19 fired="true">
        <par_subsumsjon fired="true">JA 'Kapittel 20' (false) er lik 'false'</par_subsumsjon>
      </SettRelevantTrygdetid_kap19>
      <SettRelevantTrygdetid_kap20 fired="false">
        <par_subsumsjon fired="false">NEI 'Kapittel 20' (false) må være lik 'true'</par_subsumsjon>
      </SettRelevantTrygdetid_kap20>
      <AngittFlyktning_HarFlyktningFlaggetSatt fired="false" comment="Flyktningerflagget er angitt av saksbehandler.">
        <par_subsumsjon fired="false">NEI 'Angitt flyktning' (false) må være lik 'true'</par_subsumsjon>
      </AngittFlyktning_HarFlyktningFlaggetSatt>
      <AngittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning fired="false"></AngittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning>
      <AngittFlyktning_HarUnntakFraForutgaendeTTTypeFlyktning fired="false"></AngittFlyktning_HarUnntakFraForutgaendeTTTypeFlyktning>
      <Overgangsregel_AP fired="false">
        <par_subsumsjon fired="false">NEI 'Fødselsdato' (1974-03-03) må være mindre eller lik '1959'</par_subsumsjon>
        <par_subsumsjon fired="false">NEI 'Faktisk trygdetid før 2021' (0) må være større eller lik '20'</par_subsumsjon>
      </Overgangsregel_AP>
      <Overgangsregel_AP_tidligereUT fired="false">
        <par_subsumsjon fired="false">NEI 'Fødselsdato' (1974-03-03) må være mindre eller lik '1959'</par_subsumsjon>
        <par_subsumsjon fired="false">NEI 'virkningstidspunkt' (1990-05-01) må være etter eller lik 'Fødselsdato67m' (2041-04-01)</par_subsumsjon>
        <par_subsumsjon fired="false">NEI 'Faktisk trygdetid før 2021' (0) må være større eller lik '20'</par_subsumsjon>
        <par_subsumsjon fired="false">NEI 'Uføretrygd før 2021' (false) må være lik 'true'</par_subsumsjon>
      </Overgangsregel_AP_tidligereUT>
      <Overgangsregel_AP_tidligereGJP fired="false">
        <par_subsumsjon fired="false">NEI 'Fødselsdato' (1974-03-03) må være mindre eller lik '1959'</par_subsumsjon>
        <par_subsumsjon fired="false">NEI 'virkningstidspunkt' (1990-05-01) må være etter eller lik 'Fødselsdato67m' (2041-04-01)</par_subsumsjon>
        <par_subsumsjon fired="false">NEI 'Faktisk trygdetid før 2021' (0) må være større eller lik '20'</par_subsumsjon>
        <par_subsumsjon fired="false">NEI 'Gjenlevendepensjon før 2021' (false) må være lik 'true'</par_subsumsjon>
      </Overgangsregel_AP_tidligereGJP>
      <Overgangsregel_GJR_tidligereUT_GJT fired="false"></Overgangsregel_GJR_tidligereUT_GJT>
      <Overgangsregel_GJR_tidligereGJR fired="false"></Overgangsregel_GJR_tidligereGJR>
      <AnvendtFlyktning_ikkeRelevant fired="true">
        <liste_subsumsjon fired="true">JA 'Regelreferanse' (AngittFlyktning) ingen [regel: NEI PersonenErFlyktningRS.AngittFlyktning_HarFlyktningFlaggetSatt]</liste_subsumsjon>
          <AngittFlyktning_HarFlyktningFlaggetSatt fired="false" comment="Flyktningerflagget er angitt av saksbehandler.">
            <par_subsumsjon fired="false">NEI 'Angitt flyktning' (false) må være lik 'true'</par_subsumsjon>
          </AngittFlyktning_HarFlyktningFlaggetSatt>
      </AnvendtFlyktning_ikkeRelevant>
    </PersonenErFlyktningRS>
    <BeregnFaktiskTrygdetidRS>
      <BoPeriodeStartFør16år.1 fired="true"></BoPeriodeStartFør16år.1>
      <BoPeriodeStartFør16år.2 fired="false"></BoPeriodeStartFør16år.2>
      <BoPeriodeStartFom16år.1 fired="false"></BoPeriodeStartFom16år.1>
      <BoPeriodeStartFom16år.2 fired="true"></BoPeriodeStartFom16år.2>
      <SettFireFemtedelskrav fired="true"></SettFireFemtedelskrav>
      <Skal_ha_redusert_fremtidig_trygdetid fired="false" comment="Dersom faktisk trygdetid i Norge er mindre enn 4/5 av opptjeningstiden skal den framtidige trygdetiden være redusert.">
        <par_subsumsjon fired="false">NEI 'virkningstidspunkt' (1990-05-01) må være etter eller lik '1991-01-01'</par_subsumsjon>
        <par_subsumsjon fired="true">JA 'faktisk trygdetid i måneder' (224) er mindre enn 'firefemtedelskrav' (480)</par_subsumsjon>
      </Skal_ha_redusert_fremtidig_trygdetid>
      <FastsettTrygdetid_ikkeFlyktning fired="true">
        <par_subsumsjon fired="true">JA 'Anvendt flyktning' (IKKE_RELEVANT) er ulik 'OPPFYLT'</par_subsumsjon>
      </FastsettTrygdetid_ikkeFlyktning>
      <FastsettTrygdetid_Flyktning fired="false">
        <par_subsumsjon fired="false">NEI 'Anvendt flyktning' (IKKE_RELEVANT) må være lik 'OPPFYLT'</par_subsumsjon>
      </FastsettTrygdetid_Flyktning>
      <ReturnRegel fired="true"></ReturnRegel>
    </BeregnFaktiskTrygdetidRS>
    <BeregnAlderspensjonFlyt.Sivilstand gift?>
      <BeregnAlderspensjonFlyt.Sivilstand gift?/gren 0 fired="true"></BeregnAlderspensjonFlyt.Sivilstand gift?/gren 0>
      <BeregnAlderspensjonFlyt.Sivilstand gift?/gren 1 fired="false"></BeregnAlderspensjonFlyt.Sivilstand gift?/gren 1>
    </BeregnAlderspensjonFlyt.Sivilstand gift?>
    <BeregnGrunnpensjonRS>
      <FullTrygdetid fired="false"></FullTrygdetid>
      <RedusertTrygdetid fired="true"></RedusertTrygdetid>
    </BeregnGrunnpensjonRS>
  </BeregnAlderspensjonFlyt>
</BeregnAlderspensjonService>""".trimIndent(), service.xmlDebug()
        )
    }

    @Test
    fun `trace visitor, one`() {
        val ruleVisitor = ArcTraceVisitor { r ->
            r.name() == "PersonenErFlyktningRS.SettRelevantTrygdetid_kap20"
        }.apply {
            service.accept(this)
        }

        assertEquals(service, (ruleVisitor.result.first() as Rule<*>).root())
        assertEquals(
            """
            regeltjeneste: BeregnAlderspensjonService
              regelflyt: BeregnAlderspensjonFlyt
                regelsett: PersonenErFlyktningRS
                  regel: NEI PersonenErFlyktningRS.SettRelevantTrygdetid_kap20
        """.trimIndent(), ruleVisitor.trace()
        )
    }

    @Test
    fun `trace visitor, all`() {
        assertEquals(service.debug(), service.trace())
    }

    @Test
    fun `trace visitor, some`() {
        val searchTrygdetidVisitor =
            ArcTraceVisitor { r ->
                r.name().endsWith("AnvendtFlyktning_ikkeRelevant") || r.name().endsWith("FastsettTrygdetid_ikkeFlyktning")
            }.apply {
                service.accept(this)
            }

        assertEquals(
            """
            regeltjeneste: BeregnAlderspensjonService
              regelflyt: BeregnAlderspensjonFlyt
                regelsett: PersonenErFlyktningRS
                  regel: JA PersonenErFlyktningRS.AnvendtFlyktning_ikkeRelevant
                regelsett: BeregnFaktiskTrygdetidRS
                  regel: JA BeregnFaktiskTrygdetidRS.FastsettTrygdetid_ikkeFlyktning
        """.trimIndent(), searchTrygdetidVisitor.trace()
        )
    }

    @Test
    fun `trace visitor, qualified branches`() {
        val searchFlyktningVisitor =
            ArcTraceVisitor(
                qualifier = { arc -> arc.name() != PersonenErFlyktningRS::class.java.simpleName },
                target = { r -> r.name().contains("Flyktning") }
            ).apply {
                service.accept(this)
            }

        assertEquals(
            """
            regeltjeneste: BeregnAlderspensjonService
              regelflyt: BeregnAlderspensjonFlyt
                regelsett: BeregnFaktiskTrygdetidRS
                  regel: JA BeregnFaktiskTrygdetidRS.FastsettTrygdetid_ikkeFlyktning
                  regel: NEI BeregnFaktiskTrygdetidRS.FastsettTrygdetid_Flyktning
            """.trimIndent(), searchFlyktningVisitor.trace()
        )
    }

    @Test
    fun `trace visitor, by type`() {
        assertEquals(
            """
            regeltjeneste: BeregnAlderspensjonService
              regelflyt: BeregnAlderspensjonFlyt
                regelsett: PersonenErFlyktningRS
                regelsett: BeregnFaktiskTrygdetidRS
                regelsett: BeregnGrunnpensjonRS
            """.trimIndent(), service.trace(targetType = REGELSETT)
        )
    }
}