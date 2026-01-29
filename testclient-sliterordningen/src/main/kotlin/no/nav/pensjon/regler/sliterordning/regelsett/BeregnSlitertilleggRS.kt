package no.nav.pensjon.regler.sliterordning.regelsett

import no.nav.pensjon.regler.sliterordning.config.AbstractDemoRuleset
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.fagdata.FagKonstanter.MND_36
import no.nav.pensjon.regler.sliterordning.functions.avrund2desimal
import no.nav.system.ruledsl.core.model.arc.DslDomainPredicate
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.operators.*
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 *
 * Regelsett for beregning av slitertillegg
 *
 * Denne versjonen legger mer vekt på Domenepredikatene, dvs reglene er spisset mot den aktuelle formel.
 *
 * https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering#
 *
 */
class BeregnSlitertilleggRS(
    innUttakstidspunkt: YearMonth,
    innPerson: Person,
    innGrunnbeløp: Int
) : AbstractDemoRuleset<Faktum<Double>>() {
    // Faktum
    private val faktiskTrygdetid = Faktum("faktiskTrygdetid", innPerson.trygdetid.faktiskTrygdetid)
    private val uttakstidspunkt = Faktum("uttakstidspunkt", innUttakstidspunkt)
    private val nedrePensjonsDato = Faktum("nedrePensjonsDato", innPerson.nedrePensjonsDato())
    private val fullTrygdetid = Faktum("fullTrygdetid", 40)

    // Formler
    private val antallMånederEtterNedrePensjonsDato = Faktum(
        "antallMånederEtterNedrePensjonsDato",
        ChronoUnit.MONTHS.between(nedrePensjonsDato.verdi, uttakstidspunkt.verdi).toInt().coerceAtMost(MND_36)
    )

    private val G = Faktum("G", innGrunnbeløp)
    private val fulltSlitertillegg = Faktum("fulltSlitertillegg", avrund2desimal(0.25 * G / 12))
    private var justeringsFaktor = Faktum("justeringsFaktor", 0.0)
    private val trygdetidFaktor = Faktum("trygdetidFaktor", faktiskTrygdetid / fullTrygdetid)

    @OptIn(DslDomainPredicate::class)
    override fun create() {

        /**
         * Uttaket er innen 36 måneder etter nedre pensjonsdato.
         */
        regel("TidligUttak") {
            HVIS { antallMånederEtterNedrePensjonsDato erMindreEnn MND_36 }
            SÅ {
                justeringsFaktor = sporing(
                    "justeringsFaktor", (MND_36 - antallMånederEtterNedrePensjonsDato) / MND_36
                )
            }
            REF("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT", "https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering")
        }

        /**
         * Uttaket er fom 36 måneder etter nedre pensjonsdato.
         */
        regel("SentUttak") {
            HVIS { antallMånederEtterNedrePensjonsDato erStørreEllerLik MND_36 }
            SÅ {
                justeringsFaktor = sporing(
                    "justeringsFaktor", 0.0
                )
            }
            REF("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT", "https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering")
        }

        /**
         * Beregn Slitertillegget
         */
        regel("Beregn") {
            HVIS { true }
            SÅ {
                RETURNER(
                    sporing("slitertillegg", avrund2desimal(fulltSlitertillegg * justeringsFaktor * trygdetidFaktor))
                )
            }
            REF(
                "SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID",
                "https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering"
            )
        }
    }
}