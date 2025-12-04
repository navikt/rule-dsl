# Nye operatorer i Uttrykk DSL

Denne dokumentasjonen beskriver nye operatorer som er lagt til i Uttrykk-systemet:
- `erBlant` og `erIkkeBlant` - Liste-sammenligninger
- `erEtter`, `erEtterEllerLik`, `erFør`, `erFørEllerLik` - Dato-sammenligninger
- `toGrunnlag()` - Konvertering fra Faktum til Grunnlag

## Oversikt

`erBlant` og `erIkkeBlant` operatorene lar deg sjekke om en verdi finnes i (eller ikke finnes i) en liste av verdier. Dette er nyttig for å verifisere at et enum eller en annen verdi er blant en liste av aksepterte verdier.

## Syntaks

```kotlin
// Grunnleggende syntaks
verdi erBlant liste
verdi erIkkeBlant liste

// Med Const og Grunnlag
Const(enVerdi) erBlant Grunnlag("listeName", Const(enListe))
Const(enVerdi) erBlant listOf(verdi1, verdi2, verdi3)
Grunnlag("verdiNavn", Const(enVerdi)) erBlant listOf(verdi1, verdi2, verdi3)
```

## Eksempler

### Eksempel 1: Enum-verdier

```kotlin
import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum.*
import no.nav.system.rule.dsl.forklaring.*

val aktuelleUnntakstyper = Grunnlag(
    "aktuelleUnntakstyper",
    Const(listOf(FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP))
)

val unntakType = Const(FLYKT_ALDER)
val uttrykk = unntakType erBlant aktuelleUnntakstyper

// Evaluering
println(uttrykk.evaluer())  // true

// Notasjon (symbolsk)
println(uttrykk.notasjon())  // "FLYKT_ALDER ER BLANT aktuelleUnntakstyper"

// Konkret (med verdier)
println(uttrykk.konkret())  // "FLYKT_ALDER ER BLANT [FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP]"
```

### Eksempel 2: Heltall

```kotlin
val tall = Grunnlag("tall", Const(5))
val uttrykk = tall erBlant listOf(1, 2, 3, 4, 5)

println(uttrykk.evaluer())  // true
println(uttrykk.notasjon())  // "tall ER BLANT [1, 2, 3, 4, 5]"
```

### Eksempel 3: I betinget uttrykk

```kotlin
val unntakType = Grunnlag("unntakType", Const(FLYKT_ALDER))
val aktuelleUnntakstyper = Grunnlag(
    "aktuelleUnntakstyper",
    Const(listOf(FLYKT_ALDER, FLYKT_BARNEP))
)

val resultat = (unntakType erBlant aktuelleUnntakstyper)
    .så { Const("GODKJENT") }
    .ellers { Const("AVVIST") }

println(resultat.evaluer())  // "GODKJENT"
```

### Eksempel 4: I logisk OG uttrykk

```kotlin
val unntakType = Grunnlag("unntakType", Const(FLYKT_ALDER))
val aktuelleUnntakstyper = Grunnlag(
    "aktuelleUnntakstyper",
    Const(listOf(FLYKT_ALDER, FLYKT_BARNEP))
)
val harUnntak = Grunnlag("harUnntak", Const(true))

val uttrykk = harUnntak og (unntakType erBlant aktuelleUnntakstyper)

println(uttrykk.evaluer())  // true
```

### Eksempel 5: Med forklaring

```kotlin
val unntakType = Grunnlag("unntakType", Const(FLYKT_ALDER))
val aktuelleUnntakstyper = Grunnlag(
    "aktuelleUnntakstyper",
    Const(listOf(FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP))
)

val uttrykk = unntakType erBlant aktuelleUnntakstyper
val forklaring = uttrykk.forklar("erFlyktning")

// Forklaringen inneholder:
// - Symbolsk notasjon: "unntakType ER BLANT aktuelleUnntakstyper"
// - Konkret notasjon: "FLYKT_ALDER ER BLANT [FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP]"
// - Resultat: true
```

## Brukstilfelle fra PersonErFlyktning.kt

```kotlin
val aktuelleUnntakstyper = Grunnlag(
    "aktuelleUnntakstyper",
    Const(listOf(FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP))
)

val angittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning: ErBlant<UnntakEnum>? =
    unntakFraForutgaendeMedlemskap?.let { unntakFraForutgaendeMedlemskap ->
       unntakFraForutgaendeMedlemskap.unntak.value.ifTrue {
           Const(unntakFraForutgaendeMedlemskap.unntakType.value) erBlant aktuelleUnntakstyper()
       }
    }
```

## Tekniske detaljer

### Implementerte klasser

- `ErBlant<T : Any>` - Data class for "er blant" sammenligning
- `ErIkkeBlant<T : Any>` - Data class for "er ikke blant" sammenligning

### Metoder

Begge klassene implementerer `Uttrykk<Boolean>` og støtter:
- `evaluer()`: Boolean - Evaluerer om verdien er i listen
- `notasjon()`: String - Returnerer symbolsk notasjon
- `konkret()`: String - Returnerer konkret notasjon med verdier
- `grunnlagListe()`: List<Grunnlag<out Any>> - Returnerer alle grunnlag brukt
- `dybde()`: Int - Returnerer dybde av uttrykkstre

### Støtte i UttrykkForklaring

Både `ErBlant` og `ErIkkeBlant` er fullt integrert i forklaringssystemet og støtter:
- `finnNavngitteUttrykk()` - Traversering av navngitte underuttrykk
- `finnKonstanteGrunnlag()` - Finne konstante verdier
- `treVisning()` - ASCII-tre visualisering
- `visit()` - Pattern matching visitor
- `forenkel()` - Forenkle konstante uttrykk
- `erstatt()` - Erstatte variabler
- `finnRvsIdFor()` - Finne RVS-ID for navngitte uttrykk

## Dato-sammenlignings-operatorer

### Oversikt

Dato-operatorene gir et mer naturlig språk for å sammenligne datoer enn de generiske `>`, `<`, `≥`, `≤` operatorene.

### Tilgjengelige operatorer

- `erEtter` - Sjekker om en dato er etter en annen (>)
- `erEtterEllerLik` - Sjekker om en dato er etter eller lik en annen (≥)
- `erFør` - Sjekker om en dato er før en annen (<)
- `erFørEllerLik` - Sjekker om en dato er før eller lik en annen (≤)

### Eksempler

**Grunnleggende bruk:**
```kotlin
val fødselsdato = Grunnlag("fødselsdato", Const(LocalDate.of(1957, 5, 17)))
val aldersgrense = Grunnlag("aldersgrense", Const(LocalDate.of(1960, 1, 1)))

val erFødtFørGrense = fødselsdato erFør aldersgrense
// Evaluerer til true
// Notasjon: "fødselsdato < aldersgrense"
```

**Med direkte LocalDate-verdi:**
```kotlin
val virk = Grunnlag("virk", Const(LocalDate.of(2024, 7, 1)))
val dato67m = Grunnlag("dato67m", Const(LocalDate.of(2024, 6, 1)))

val resultat = virk erEtterEllerLik dato67m
// Evaluerer til true
// Notasjon: "virk ≥ dato67m"
```

**I betinget uttrykk:**
```kotlin
val idag = Grunnlag("idag", Const(LocalDate.of(2021, 6, 15)))
val frist = Grunnlag("frist", Const(LocalDate.of(2021, 12, 31)))

val status = (idag erFørEllerLik frist)
    .så { Const("INNENFOR_FRIST") }
    .ellers { Const("UTGÅTT") }

println(status.evaluer())  // "INNENFOR_FRIST"
```

**I logisk sammensatt uttrykk:**
```kotlin
val fødselsdato = Grunnlag("fødselsdato", Const(LocalDate.of(1957, 5, 17)))
val virk = Grunnlag("virk", Const(LocalDate.of(2024, 7, 1)))

val oppfyllerKrav =
    (fødselsdato erFør LocalDate.of(1960, 1, 1)) og
    (virk erEtterEllerLik LocalDate.of(2024, 1, 1))

println(oppfyllerKrav.evaluer())  // true
```

**Med navngi og id:**
```kotlin
val dato67m = Grunnlag("dato67m", Const(LocalDate.of(2024, 6, 1)))
val virk = Grunnlag("virk", Const(LocalDate.of(2024, 7, 1)))

val sammenligning = (virk erEtterEllerLik dato67m)
    .navngi("virkErEtterEllerLikDato67m")
    .id("VIRK_ETTER_ELLER_LIK_DATO_67M")
```

## Testing

Se følgende testfiler for komplette testpakker:
- `UttrykkErBlantTest.kt` - Liste-sammenligninger (erBlant, erIkkeBlant)
- `UttrykkDatoTest.kt` - Dato-sammenligninger (erEtter, erFør, etc.)
- `FaktumToGrunnlagTest.kt` - Faktum til Grunnlag konvertering
