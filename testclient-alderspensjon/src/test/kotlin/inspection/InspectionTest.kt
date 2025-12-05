package no.nav.system.rule.dsl.demo.inspection

import no.nav.pensjon.regler.alderspensjon.domain.Boperiode
import no.nav.pensjon.regler.alderspensjon.domain.Person
import no.nav.pensjon.regler.alderspensjon.domain.Request
import no.nav.pensjon.regler.alderspensjon.domain.koder.LandEnum
import no.nav.pensjon.regler.alderspensjon.service.BeregnAlderspensjonService
import no.nav.system.ruledsl.core.enums.RuleComponentType
import no.nav.system.ruledsl.core.inspections.debug
import no.nav.system.ruledsl.core.inspections.find
import no.nav.system.ruledsl.core.inspections.xmlDebug
import no.nav.system.ruledsl.core.model.Faktum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class InspectionTest {
    private val serviceRequest = Request(
        virkningstidspunkt = LocalDate.of(1990, 5, 1),
        person = Person(
            id = 1,
            fødselsdato = Faktum("Fødselsdato", LocalDate.of(1974, 3, 3)),
            erGift = true,
            boperioder = listOf(
                Boperiode(fom = LocalDate.of(1990, 1, 1), tom = LocalDate.of(2003, 12, 31), LandEnum.NOR),
                Boperiode(fom = LocalDate.of(2004, 1, 1), tom = LocalDate.of(2010, 12, 31), LandEnum.SWE),
                Boperiode(fom = LocalDate.of(2011, 1, 1), tom = LocalDate.of(2015, 12, 31), LandEnum.NOR),
                Boperiode(fom = LocalDate.of(2016, 1, 1), tom = LocalDate.of(2020, 12, 31), LandEnum.SWE)
            )
        )
    )
    private val service = BeregnAlderspensjonService(serviceRequest).also { it.run() }


    @Test
    fun `find test`() {
        val result = service.find { arc -> arc.type() == RuleComponentType.REGELSETT }
        assertEquals(3, result.size)
    }


    @Test
    fun `debug test`() {
        assertEquals(
            """
regeltjeneste: BeregnAlderspensjonService
  regelflyt: BeregnAlderspensjonFlyt
    regelsett: PersonenErFlyktningRS
      regel: JA PersonenErFlyktningRS.SettRelevantTrygdetid_kap19
        predikat: JA 'Kapittel 20' (false) er lik 'false'
      regel: NEI PersonenErFlyktningRS.SettRelevantTrygdetid_kap20
        predikat: NEI 'Kapittel 20' (false) må være lik 'true'
      regel: NEI PersonenErFlyktningRS.AngittFlyktning_HarFlyktningFlaggetSatt
        predikat: 'Angitt flyktning' (false)
      regel: NEI PersonenErFlyktningRS.AngittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning
      regel: NEI PersonenErFlyktningRS.AngittFlyktning_HarUnntakFraForutgaendeTTTypeFlyktning
      regel: NEI PersonenErFlyktningRS.Overgangsregel_AP
        predikat: NEI 'Fødselsdato' (1974-03-03) må være mindre eller lik '1959'
        predikat: NEI 'Faktisk trygdetid før 2021' (0) må være større eller lik '20'
      regel: NEI PersonenErFlyktningRS.Overgangsregel_AP_tidligereUT
        predikat: NEI 'Fødselsdato' (1974-03-03) må være mindre eller lik '1959'
        predikat: NEI 'virkningstidspunkt' (1990-05-01) må være etter eller lik 'Fødselsdato67m' (2041-04-01)
        predikat: NEI 'Faktisk trygdetid før 2021' (0) må være større eller lik '20'
        predikat: 'Uføretrygd før 2021' (false)
      regel: NEI PersonenErFlyktningRS.Overgangsregel_AP_tidligereGJP
        predikat: NEI 'Fødselsdato' (1974-03-03) må være mindre eller lik '1959'
        predikat: NEI 'virkningstidspunkt' (1990-05-01) må være etter eller lik 'Fødselsdato67m' (2041-04-01)
        predikat: NEI 'Faktisk trygdetid før 2021' (0) må være større eller lik '20'
        predikat: 'Gjenlevendepensjon før 2021' (false)
      regel: NEI PersonenErFlyktningRS.Overgangsregel_GJR_tidligereUT_GJT
      regel: NEI PersonenErFlyktningRS.Overgangsregel_GJR_tidligereGJR
      regel: JA PersonenErFlyktningRS.AnvendtFlyktning_ikkeRelevant
        predikat: JA 'Regelreferanse' (AngittFlyktning) ingen 'uaktuelle regler' ([AngittFlyktning_HarFlyktningFlaggetSatt, AngittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning, AngittFlyktning_HarUnntakFraForutgaendeTTTypeFlyktning])
        faktum: Anvendt flyktning = IKKE_RELEVANT
    regelsett: BeregnFaktiskTrygdetidRS
      regel: JA BeregnFaktiskTrygdetidRS.BoPeriodeStartFør16år.1
      regel: NEI BeregnFaktiskTrygdetidRS.BoPeriodeStartFør16år.2
      regel: NEI BeregnFaktiskTrygdetidRS.BoPeriodeStartFom16år.1
      regel: JA BeregnFaktiskTrygdetidRS.BoPeriodeStartFom16år.2
      regel: JA BeregnFaktiskTrygdetidRS.SettFireFemtedelskrav
      regel: NEI BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid
        predikat: NEI 'virkningstidspunkt' (1990-05-01) må være etter eller lik 'januar 1991' (1991-01-01)
        predikat: JA 'faktisk trygdetid i måneder' (224) er mindre enn 'firefemtedelskrav' (480)
      regel: JA BeregnFaktiskTrygdetidRS.FastsettTrygdetid_ikkeFlyktning
        predikat: JA 'Anvendt flyktning' (IKKE_RELEVANT) er ulik 'OPPFYLT'
      regel: NEI BeregnFaktiskTrygdetidRS.FastsettTrygdetid_Flyktning
        predikat: NEI 'Anvendt flyktning' (IKKE_RELEVANT) må være lik 'OPPFYLT'
      regel: JA BeregnFaktiskTrygdetidRS.ReturnRegel
    forgrening: BeregnAlderspensjonFlyt.Sivilstand?
      gren: 'Gift' (true)
      gren: 'Ugift' (false)
    regelsett: BeregnGrunnpensjonRS
      regel: NEI BeregnGrunnpensjonRS.FullTrygdetid
      regel: JA BeregnGrunnpensjonRS.RedusertTrygdetid
                      """.trimIndent(), service.debug()
        )
    }

    @Test
    fun `XML debug test`() {
        assertEquals(
            """
<BeregnAlderspensjonService>
  <BeregnAlderspensjonFlyt>
    <PersonenErFlyktningRS>
      <SettRelevantTrygdetid_kap19 fired="true">
        <predikat fired="true">JA 'Kapittel 20' er lik 'false'</predikat>
      </SettRelevantTrygdetid_kap19>
      <SettRelevantTrygdetid_kap20 fired="false">
        <predikat fired="false">NEI 'Kapittel 20' må være lik 'true'</predikat>
      </SettRelevantTrygdetid_kap20>
      <AngittFlyktning_HarFlyktningFlaggetSatt fired="false" comment="Flyktningerflagget er angitt av saksbehandler.">
        <predikat fired="false">Angitt flyktning</predikat>
      </AngittFlyktning_HarFlyktningFlaggetSatt>
      <AngittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning fired="false"></AngittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning>
      <AngittFlyktning_HarUnntakFraForutgaendeTTTypeFlyktning fired="false"></AngittFlyktning_HarUnntakFraForutgaendeTTTypeFlyktning>
      <Overgangsregel_AP fired="false">
        <predikat fired="false">NEI 'Fødselsdato' må være mindre eller lik '1959'</predikat>
        <predikat fired="false">NEI 'Faktisk trygdetid før 2021' må være større eller lik '20'</predikat>
      </Overgangsregel_AP>
      <Overgangsregel_AP_tidligereUT fired="false">
        <predikat fired="false">NEI 'Fødselsdato' må være mindre eller lik '1959'</predikat>
        <predikat fired="false">NEI 'virkningstidspunkt' må være etter eller lik 'Fødselsdato67m'</predikat>
        <predikat fired="false">NEI 'Faktisk trygdetid før 2021' må være større eller lik '20'</predikat>
        <predikat fired="false">Uføretrygd før 2021</predikat>
      </Overgangsregel_AP_tidligereUT>
      <Overgangsregel_AP_tidligereGJP fired="false">
        <predikat fired="false">NEI 'Fødselsdato' må være mindre eller lik '1959'</predikat>
        <predikat fired="false">NEI 'virkningstidspunkt' må være etter eller lik 'Fødselsdato67m'</predikat>
        <predikat fired="false">NEI 'Faktisk trygdetid før 2021' må være større eller lik '20'</predikat>
        <predikat fired="false">Gjenlevendepensjon før 2021</predikat>
      </Overgangsregel_AP_tidligereGJP>
      <Overgangsregel_GJR_tidligereUT_GJT fired="false"></Overgangsregel_GJR_tidligereUT_GJT>
      <Overgangsregel_GJR_tidligereGJR fired="false"></Overgangsregel_GJR_tidligereGJR>
      <AnvendtFlyktning_ikkeRelevant fired="true">
        <predikat fired="true">JA 'Regelreferanse' ingen 'uaktuelle regler'</predikat>
        <Anvendt flyktning>
      </AnvendtFlyktning_ikkeRelevant>
    </PersonenErFlyktningRS>
    <BeregnFaktiskTrygdetidRS>
      <BoPeriodeStartFør16år.1 fired="true"></BoPeriodeStartFør16år.1>
      <BoPeriodeStartFør16år.2 fired="false"></BoPeriodeStartFør16år.2>
      <BoPeriodeStartFom16år.1 fired="false"></BoPeriodeStartFom16år.1>
      <BoPeriodeStartFom16år.2 fired="true"></BoPeriodeStartFom16år.2>
      <SettFireFemtedelskrav fired="true"></SettFireFemtedelskrav>
      <Skal_ha_redusert_fremtidig_trygdetid fired="false" comment="Dersom faktisk trygdetid i Norge er mindre enn 4/5 av opptjeningstiden skal den framtidige trygdetiden være redusert.">
        <predikat fired="false">NEI 'virkningstidspunkt' må være etter eller lik 'januar 1991'</predikat>
        <predikat fired="true">JA 'faktisk trygdetid i måneder' er mindre enn 'firefemtedelskrav'</predikat>
      </Skal_ha_redusert_fremtidig_trygdetid>
      <FastsettTrygdetid_ikkeFlyktning fired="true">
        <predikat fired="true">JA 'Anvendt flyktning' er ulik 'OPPFYLT'</predikat>
      </FastsettTrygdetid_ikkeFlyktning>
      <FastsettTrygdetid_Flyktning fired="false">
        <predikat fired="false">NEI 'Anvendt flyktning' må være lik 'OPPFYLT'</predikat>
      </FastsettTrygdetid_Flyktning>
      <ReturnRegel fired="true"></ReturnRegel>
    </BeregnFaktiskTrygdetidRS>
    <BeregnAlderspensjonFlyt.Sivilstand?>
      <Gift fired="true"></Gift>
      <Ugift fired="false"></Ugift>
    </BeregnAlderspensjonFlyt.Sivilstand?>
    <BeregnGrunnpensjonRS>
      <FullTrygdetid fired="false"></FullTrygdetid>
      <RedusertTrygdetid fired="true"></RedusertTrygdetid>
    </BeregnGrunnpensjonRS>
  </BeregnAlderspensjonFlyt>
</BeregnAlderspensjonService>
""".trimIndent(), service.xmlDebug()
        )
    }

}