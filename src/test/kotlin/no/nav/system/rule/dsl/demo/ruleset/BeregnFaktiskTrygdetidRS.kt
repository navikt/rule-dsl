package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.demo.domain.Boperiode
import no.nav.system.rule.dsl.demo.domain.Trygdetid
import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.enums.UtfallType
import no.nav.system.rule.dsl.enums.UtfallType.*
import no.nav.system.rule.dsl.pattern.createPattern
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.erEtterEllerLik
import no.nav.system.rule.dsl.rettsregel.erMindreEnn
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Demo regelsett viser bruk av Pattern og regelsporing.
 */
class BeregnFaktiskTrygdetidRS(
    fødselsdato: Faktum<LocalDate>,
    private val virkningstidspunkt: Faktum<LocalDate>,
    private val boperiodeListe: List<Boperiode>,
    private val flyktningUtfall: Faktum<UtfallType>,
) : AbstractRuleset<Trygdetid>() {

    /**
     * Nytt Pattern [norskeBoperioder] opprettes på bakgrunn av liste [boperiodeListe] med et filter på land.
     */
    private val norskeBoperioder = boperiodeListe.createPattern { it.land == LandEnum.NOR }
    private val dato16år = fødselsdato.verdi.plusYears(16)
    private val dato1991 = Faktum(localDate(1991, 1, 1))
    private val svar = Trygdetid()

    @OptIn(DslDomainPredicate::class)
    override fun create() {

        /**
         * En regel for hvert innslag i pattern [norskeBoperioder] vil bli opprettet, evaluert og evt. kjørt.
         */
        regel("BoPeriodeStartFør16år", norskeBoperioder) { boperiode ->
            HVIS { boperiode.fom < dato16år }
            SÅ {
                svar.faktiskTrygdetidIMåneder.verdi += ChronoUnit.MONTHS.between(dato16år, boperiode.tom)
            }
        }

        /**
         * En regel for hvert innslag i pattern [norskeBoperioder] vil bli opprettet, evaluert og evt. kjørt.
         */
        regel("BoPeriodeStartFom16år", norskeBoperioder) { boperiode ->
            HVIS { boperiode.fom >= dato16år }
            SÅ {
                svar.faktiskTrygdetidIMåneder.verdi += ChronoUnit.MONTHS.between(boperiode.fom, boperiode.tom)
            }
        }

        regel("SettFireFemtedelskrav") {
            HVIS { true }
            SÅ {
                svar.firefemtedelskrav = Faktum("firefemtedelskrav", 480)
            }
        }

        /**
         * Rettsregel med sporing på predikatnivå (subsumsjoner).
         */
        regel("Skal ha redusert fremtidig trygdetid") {
            HVIS { virkningstidspunkt erEtterEllerLik dato1991 }
            OG { svar.faktiskTrygdetidIMåneder erMindreEnn svar.firefemtedelskrav }
            SÅ {
                svar.redusertFremtidigTrygdetid.verdi = OPPFYLT
            }
            ELLERS {
                svar.redusertFremtidigTrygdetid.verdi = IKKE_OPPFYLT
            }
            kommentar(
                """Dersom faktisk trygdetid i Norge er mindre enn 4/5 av
                     opptjeningstiden skal den framtidige trygdetiden være redusert."""
            )
        }

        regel("FastsettTrygdetid_ikkeFlyktning") {
            HVIS { flyktningUtfall.verdi != OPPFYLT }
            SÅ {
                svar.år = (svar.faktiskTrygdetidIMåneder.verdi / 12.0).roundToInt()
            }
        }
        regel("FastsettTrygdetid_Flyktning") {
            HVIS { flyktningUtfall.verdi == OPPFYLT }
            SÅ {
                svar.år = 40
            }
        }

        regel("ReturnRegel") {
            HVIS { true }
            SÅ {
                RETURNER(svar)
            }
        }
    }
}