package no.nav.pensjon.regler.alderspensjon.ruleset

import no.nav.pensjon.regler.alderspensjon.domain.Boperiode
import no.nav.pensjon.regler.alderspensjon.domain.Trygdetid
import no.nav.pensjon.regler.alderspensjon.domain.koder.LandEnum
import no.nav.pensjon.regler.alderspensjon.domain.koder.UtfallType
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.Verdi
import no.nav.system.ruledsl.core.expression.boolean.erLik
import no.nav.system.ruledsl.core.expression.boolean.erMindreEnn
import no.nav.system.ruledsl.core.expression.boolean.erUlik
import no.nav.system.ruledsl.core.trace.RuleContext
import no.nav.system.ruledsl.core.trace.traced
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Demo regelsett for trygdetidsberegning.
 *
 * NOTE: This is a partial port. The following features from core are NOT available in core-trace:
 * - Pattern (createPattern for list-based rules)
 * - ELLERS (else branch)
 * - kommentar() (rule documentation)
 */
context(ruleContext: RuleContext)
fun beregnFaktiskTrygdetid(
    fødselsdato: Verdi<LocalDate>,
    virkningstidspunkt: Verdi<LocalDate>,
    boperiodeListe: List<Boperiode>,
    flyktningUtfall: Faktum<UtfallType>,
): Faktum<Trygdetid> = traced {

    val norskeBoperioder = boperiodeListe.filter { it.land == LandEnum.NOR }
    val dato16år = fødselsdato.value.plusYears(16)
    val dato1991 = Verdi("januar 1991", LocalDate.of(1991, 1, 1))
    val svar = Trygdetid()

    // NOTE: In core, this was done with Pattern for per-item rule evaluation and tracing.
    // Here we must manually iterate - losing individual rule traces per boperiode.
    norskeBoperioder.forEach { boperiode ->
        if (boperiode.fom < dato16år) {
            val økning = ChronoUnit.MONTHS.between(dato16år, boperiode.tom)
            svar.faktiskTrygdetidIMåneder = Verdi(
                svar.faktiskTrygdetidIMåneder.name,
                svar.faktiskTrygdetidIMåneder.value + økning
            )
        } else {
            val økning = ChronoUnit.MONTHS.between(boperiode.fom, boperiode.tom)
            svar.faktiskTrygdetidIMåneder = Verdi(
                svar.faktiskTrygdetidIMåneder.name,
                svar.faktiskTrygdetidIMåneder.value + økning
            )
        }
    }

    regel("SettFireFemtedelskrav") {
        HVIS { true }
        SÅ {
            svar.firefemtedelskrav = Verdi("firefemtedelskrav", 480L)
        }
    }

    /**
     * Rettsregel med sporing på predikatnivå (subsumsjoner).
     *
     * NOTE: ELLERS is not available in core-trace. Using separate rule instead.
     */
    regel("Skal ha redusert fremtidig trygdetid") {
        HVIS { virkningstidspunkt.value >= dato1991.value }
        OG { svar.faktiskTrygdetidIMåneder erMindreEnn svar.firefemtedelskrav }
        SÅ {
            svar.redusertFremtidigTrygdetid = Verdi(svar.redusertFremtidigTrygdetid.name, UtfallType.OPPFYLT)
        }
    }

    regel("Skal ikke ha redusert fremtidig trygdetid") {
        HVIS { svar.redusertFremtidigTrygdetid.value == UtfallType.IKKE_RELEVANT }
        SÅ {
            svar.redusertFremtidigTrygdetid = Verdi(svar.redusertFremtidigTrygdetid.name, UtfallType.IKKE_OPPFYLT)
        }
    }

    regel("FastsettTrygdetid_ikkeFlyktning") {
        HVIS { flyktningUtfall erUlik UtfallType.OPPFYLT }
        SÅ {
            val beregnetÅr = (svar.faktiskTrygdetidIMåneder.value / 12.0).roundToInt()
            svar.år = minOf(beregnetÅr, 40)  // Cap at 40
        }
    }

    regel("FastsettTrygdetid_Flyktning") {
        HVIS { flyktningUtfall erLik UtfallType.OPPFYLT }
        SÅ {
            svar.år = 40
        }
    }

    regel("ReturnRegel") {
        HVIS { true }
        RETURNER {
            faktum("trygdetid", svar)
        }
    }
}
