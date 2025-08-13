package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.demo.domain.PensjonsgivendeInntekt
import no.nav.system.rule.dsl.demo.domain.VeietGrunnbeløp
import no.nav.system.rule.dsl.pattern.createPattern
import no.nav.system.rule.dsl.rettsregel.Faktum
import java.time.LocalDate

class VilkårsprøvingSlitertilleggRS(
    val inntektListe: List<PensjonsgivendeInntekt>,
    val veietGrunnbeløpListe: List<VeietGrunnbeløp>, // antar veiet grunnbeløp siste 3 år
    val uttaksdato: LocalDate,
    ) : AbstractRuleset<Boolean>() {

    private val siste3år = listOf(uttaksdato.minusYears(3).year, uttaksdato.minusYears(2).year, uttaksdato.minusYears(1).year)
    private val inntektListeSiste3år = inntektListe.createPattern { it.år in siste3år  }
    private var sumInntektSiste3år = 0.0

    private val faktuminntektSiste3år = Faktum("inntektListeSiste3år", inntektListe.filter { it.år in siste3år })

    private val veietGrunnbeløpListeSiste3år = veietGrunnbeløpListe.createPattern { it.år in siste3år  }
    private var sumVeietGrunnbeløpSiste3år = 0

    @OptIn(DslDomainPredicate::class)
    override fun create() {

        faktuminntSiste3år
         regel snitt

        regel("SumInntektSiste3år", inntektListeSiste3år ) { inntekt ->
            HVIS { true }
            SÅ { sumInntektSiste3år += inntekt.belop}

        }

        regel("SumVeietGrunnbeløpSiste3år", veietGrunnbeløpListeSiste3år) { veietGrunnbeløp ->
            HVIS { true }
            SÅ { sumVeietGrunnbeløpSiste3år += veietGrunnbeløp.beløp }
        }
    }
}