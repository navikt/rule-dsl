package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.demo.domain.PensjonsgivendeInntekt
import no.nav.system.rule.dsl.demo.domain.VeietGrunnbeløp
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.erMindreEllerLik
import no.nav.system.rule.dsl.rettsregel.erStørreEllerLik
import java.time.LocalDate

class VilkårsprøvingSlitertilleggRS(
    val inntektListe: List<PensjonsgivendeInntekt>,
    val veietGrunnbeløpListe: List<VeietGrunnbeløp>, // antar veiet grunnbeløp siste 3 år
    val uttaksdato: LocalDate,
) : AbstractRuleset<Faktum<Boolean>>() {

    private val ANTALL_ÅR_TILBAKE = 3
    private val G_FAKTOR_OVRE_INNTEKTSGRENSE = 7.1

    private fun sisteÅrFra(uttaksdato: LocalDate, antallÅr: Int): List<Int> =
        (1..antallÅr).map { år -> uttaksdato.minusYears(år.toLong()).year }

    private val siste3år = Faktum("siste3år", sisteÅrFra(uttaksdato, ANTALL_ÅR_TILBAKE))
    private val forrigeår = Faktum("forrigeAr", sisteÅrFra(uttaksdato, 1).first())

    val inntektSiste3år =
        Faktum("inntektListeSiste3år", inntektListe.filter { it.år in siste3år.value })

    private val veietGrunnbeløpListeSiste3år =
        Faktum("veietGrunnbeløpSiste3år", veietGrunnbeløpListe.filter { it.år in siste3år.value })

    private var vilårInntekt3årOppfylt = false
    private var vilkårInntektForrigeårOppfylt = false

    @OptIn(DslDomainPredicate::class)
    override fun create() {

//        regel("SumInntektSiste3år", inntektListeSiste3år ) { inntekt ->
//            HVIS { true }
//            SÅ { sumInntektSiste3år += inntekt.belop}
//
//        }

//        regel("SumVeietGrunnbeløpSiste3år", veietGrunnbeløpListeSiste3år) { veietGrunnbeløp ->
//            HVIS { true }
//            SÅ { sumVeietGrunnbeløpSiste3år += veietGrunnbeløp.beløp }
//        }

        regel("SLITERTILLEGG-INNGANGSVILKÅR-INNTEKT-TRE-ÅR") {

            val gjennomsnittligInntektSiste3år = Faktum(
                "gjennomsnittligInntektSiste3år",
                inntektSiste3år.value.sumOf { it.belop } / ANTALL_ÅR_TILBAKE.toDouble())

            val gjennomsnittligVeietGrunnbeløpSiste3år =
                Faktum("", veietGrunnbeløpListeSiste3år.value.sumOf { it.beløp } / ANTALL_ÅR_TILBAKE.toDouble())

            HVIS { gjennomsnittligInntektSiste3år erMindreEllerLik G_FAKTOR_OVRE_INNTEKTSGRENSE * gjennomsnittligVeietGrunnbeløpSiste3år.value }
            SÅ { vilårInntekt3årOppfylt = true }
        }

        regel("SLITERTILLEGG-INNGANGSVILKÅR-INNTEKT-FORRIGE-ÅR") {

            val inntektForrigeår = Faktum("inntektForrigeår",
                inntektSiste3år.value.first { it.år == forrigeår.value }.belop)

            val veietGrunnbeløpForrigeår = Faktum(
                "veietGrunnbeløpForrigeår",
                veietGrunnbeløpListeSiste3år.value.first { it.år == forrigeår.value }.beløp)

            HVIS { inntektForrigeår erStørreEllerLik  1 * veietGrunnbeløpForrigeår.value }
            SÅ { vilkårInntektForrigeårOppfylt = true }
        }

        regel("Test") {
            HVIS { true }
            SÅ {
                RETURNER(
                    Faktum(
                        "vilkårOppfylt",
                        "SLITERTILLEGG-INNGANGSVILKÅR-INNTEKT-FORRIGE-ÅR".harTruffet() && "SLITERTILLEGG-INNGANGSVILKÅR-INNTEKT-FORRIGE-ÅR".harTruffet()
                    )
                )}
        }
    }
}