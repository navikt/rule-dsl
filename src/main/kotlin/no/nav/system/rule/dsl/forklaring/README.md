# Regelsporing - Forklaring API

Dette pakket gir strukturert forklaring av regelberegninger og beslutninger.

## Konsept

Regelsporing besvarer tre hovedspørsmål:

1. **HVA** - Hva er resultatet? (konkrete verdier)
2. **HVORFOR** - Hvorfor ble regelen aktivert? (betingelser/subsumsjoner)
3. **HVORDAN** - Hvordan ble resultatet beregnet? (formeldekomponering)

## Bruk

### Formelforklaring (HVORDAN)

```kotlin
val G = Formel.variable("G", 110000)
val sats = Formel.variable("sats", 0.25)

val fulltSlitertillegg = FormelBuilder.create<Double>()
    .name("fulltSlitertillegg")
    .expression(sats * G / 12)
    .build()

// Kompakt forklaring (3 linjer)
println(fulltSlitertillegg.forklarKompakt())
// Output:
// fulltSlitertillegg = sats * G / 12
// fulltSlitertillegg = 0.25 * 110000 / 12
// fulltSlitertillegg = 2291.67

// Detaljert forklaring med subformler
println(fulltSlitertillegg.forklarDetaljert())
// Output:
// HVORDAN
//     fulltSlitertillegg = sats * G / 12
//     fulltSlitertillegg = 0.25 * 110000 / 12
//     fulltSlitertillegg = 2291.67
//
//     sats = 0.25
//     G = 110000
```

### Subsumsjonsforklaring (FORDI)

```kotlin
val antallMåneder = Faktum("antallMånederEtterNedreAldersgrense", 24)
val grense = Faktum("MND_36", 36)

val betingelse = antallMåneder erMindreEnn grense

// Generer forklaring
val forklaring = betingelse.forklar()
println(forklaring.toText())
// Output:
// FORDI
//     antallMånederEtterNedreAldersgrense er mindre enn MND_36
//     24 er mindre enn 36
```

### Komplett forklaring

```kotlin
val G = Formel.variable("G", 110000)
val antallMåneder = Formel.variable("antallMånederEtterNedreAldersgrense", 24)
val MND_36 = Formel.variable("MND_36", 36)

val betingelse = antallMåneder erMindreEnn MND_36

val justeringsFaktor = FormelBuilder.create<Double>()
    .name("justeringsFaktor")
    .expression((MND_36 - antallMåneder) / MND_36)
    .locked()
    .build()

// Kombiner alle forklaringer
val komplettForklaring = KomplettForklaring(
    hva = justeringsFaktor.forklarHva(),
    hvorfor = betingelse.forklar(),
    hvordan = justeringsFaktor.forklarHvordan(),
    referanser = listOf("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT")
)

println(komplettForklaring.toText())
// Output:
// justeringsFaktor = (MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
// justeringsFaktor = (36 - 24) / 36
// justeringsFaktor = 0.33
//
// REFERANSE
//     SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT
//
// FORDI
//     antallMånederEtterNedreAldersgrense er mindre enn MND_36
//     24 er mindre enn 36
//
// HVORDAN
//     justeringsFaktor = (MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
//     justeringsFaktor = (36 - 24) / 36
//     justeringsFaktor = 0.33
//
//     MND_36 = 36
//     antallMånederEtterNedreAldersgrense = 24
```

## Integrasjon i regelsett

```kotlin
regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
    HVIS { antallMånederEtterNedreAldersgrense erMindreEnn MND_36 }
    SÅ {
        justeringsFaktor = FormelBuilder.create<Double>()
            .name("justeringsFaktor")
            .expression((MND_36 - antallMånederEtterNedreAldersgrense) / MND_36)
            .build()

        // Generer og lagre forklaring
        val forklaring = KomplettForklaring(
            hva = justeringsFaktor.forklarHva(),
            hvorfor = (antallMånederEtterNedreAldersgrense erMindreEnn MND_36).forklar(),
            hvordan = justeringsFaktor.forklarHvordan()
        )

        // Lagre forklaring for senere visning/logging
        // lagreForklaring(forklaring)
    }
}
```

## API Oversikt

### Extension functions på Formel

| Metode | Beskrivelse | Output |
|--------|-------------|--------|
| `forklarHva()` | Viser resultatet | HvaForklaring |
| `forklarHvordan()` | Dekomponerer formelen | HvordanForklaring |
| `forklar()` | Komplett forklaring | HvordanForklaring |
| `forklarKompakt()` | 3-linjers tekst | String |
| `forklarDetaljert()` | Full tekst med subformler | String |
| `forklarHTML()` | HTML output | String |
| `strukturTre()` | Hierarkisk visning | String |
| `variabelOversikt()` | Liste alle variabler | String |

### Extension functions på Faktum

| Metode | Beskrivelse | Output |
|--------|-------------|--------|
| `forklarHva()` | Viser verdien | HvaForklaring |
| `forklar()` | Enkel tekstforklaring | String |

### Extension functions på Subsumtion

| Metode | Beskrivelse | Output |
|--------|-------------|--------|
| `forklar()` | FORDI forklaring | HvorforForklaring |

### Forklaringstyper

- **HvaForklaring** - Viser navn, symbolsk uttrykk, konkret uttrykk og resultat
- **HvorforForklaring** - Viser subsumsjoner og underliggende årsaker
- **HvordanForklaring** - Viser beregning med subformler
- **KomplettForklaring** - Kombinerer alle typer med referanser

## Output formater

Alle forklaringer støtter:
- `toText()` - Menneskelesbar tekst
- `toHTML()` - HTML med CSS-klasser for styling

HTML klasser:
- `hva-navn`, `hva-symbolsk`, `hva-konkret`, `hva-resultat`
- `hvorfor`, `subsumtion`, `oppfylt`, `ikke-oppfylt`
- `hvordan`
- `komplett-forklaring`, `referanser`, `referanse`

## Eksempler

Se:
- `ForklaringDemo.kt` - Kjørbar demo av alle funksjoner
- `ForklaringTest.kt` - Enhetstester som viser bruk
- `BeregnSlitertilleggRSVårFaktumVersjon.kt` - Eksempel på hvordan forklaringer kan dokumenteres i KDOC

## Fremtidige utvidelser

Potensielle forbedringer:
1. Automatisk lagring av forklaringer i regelkomponent-treet
2. Integrasjon med eksisterende `debug()` og `trace()` funksjoner
3. Støtte for å koble subsumsjoner direkte til faktum-objekter
4. JSON og XML output formater
5. Interaktiv HTML med expand/collapse
6. Kobling til lovhjemler og referanser
7. Språkstøtte (norsk/engelsk)
