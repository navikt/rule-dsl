1. Regelflyt må ha betingelser som tar i mot DomainPredicate. Dette muliggjør sporing av HVORFOR blokken til alle
   underliggende ARC.

2. FORDI sporingen algoritmen:
   Når faktum produseres og FORDI forklaringen skal lages: spørr root() om trace til this (ARC). Alle ARC på veien ned
   this THIS, må avgi forklaring.

3. Vurder om all sporing skal skrues av og på. Vi bør ikke levere ut en stor forklaring dersom vi kjører i
   ytelseskritisk batch kontekst. Bare spor ved forspørsel.

4. Kombiner Faktum og Formel. Dvs Faktum<T : Number> fungerer som Formel<Number>.
5. Neste "crux" er å få etablert en god måte å (hardanger-)sømløst produsere Faktum i SÅ delen av regel uten å måtte
   kalle opp sporingsmekanismer i konstruktøren av Faktum.

* Mulige løsninger: nå FAG()-metode som har nødvendig context i input: FAG(init: (fordi: ?) -> Unit). Slik at det det
  blir "lett" (men ikke helautomatisk) å levere et ferdig faktum:
*

```kotlin
        val BeregnSlitertillegg = rettsregel {
    HVIS { true }
    FAG { sporing ->
        param.svar = Faktum(
            hvordan = formula("slitertillegg") {
                expression(fulltSlitertillegg * justeringsFaktor * trygdetidFaktor)
            },
            hvorfor = sporing
        )
    }
}
```

evt FaktumBuilder:

```kotlin
        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
    HVIS { antallMånederEtterNedreAldersgrense erMindreEnn MND_36 }
    FAG { sporing ->
        justeringsFaktor = FaktumBuilder<Double>()
            .navn("justeringsFaktor")
            .hvorfor(sporing)
            .hvordan((MND_36 - antallMånederEtterNedreAldersgrense) / MND_36) // UTTRYKK
            .build() // hva = navn + " : " hvordan.value
    }
}
```

