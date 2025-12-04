# Regelsporing System - Oversikt

Dette dokumentet oppsummerer det nye regelsporing-systemet for `rule-dsl`.

## Hva er laget?

Et komplett forklaringssystem som lar `Faktum` og `Formel` generere strukturerte forklaringer som besvarer:
- **HVA** - Hva er resultatet? (konkrete verdier)
- **HVORFOR** - Hvorfor ble regelen aktivert? (betingelser/subsumsjoner)
- **HVORDAN** - Hvordan ble resultatet beregnet? (formeldekomponering)

## Nye filer

### Core forklaring API
1. **`src/main/kotlin/no/nav/system/rule/dsl/forklaring/Forklaring.kt`**
   - Definierer forklaringsstrukturene:
     - `HvaForklaring` - viser navn, symbolsk uttrykk, konkret uttrykk og resultat
     - `HvorforForklaring` - viser subsumsjoner og årsaker
     - `HvordanForklaring` - viser beregning med subformler
     - `KomplettForklaring` - kombinerer alle typer
     - `SubsumsjonForklaring` - representerer en enkelt betingelse

2. **`src/main/kotlin/no/nav/system/rule/dsl/forklaring/FormelForklaring.kt`**
   - Extension functions på `Formel<T>`:
     - `forklar()` - genererer komplett forklaring
     - `forklarHva()` - viser resultatet
     - `forklarHvordan()` - dekomponerer formelen
     - `forklarKompakt()` - 3-linjers tekstutskrift
     - `forklarDetaljert()` - full tekst med subformler
     - `forklarHTML()` - HTML output
     - `strukturTre()` - hierarkisk visning
     - `variabelOversikt()` - liste alle variabler

3. **`src/main/kotlin/no/nav/system/rule/dsl/forklaring/FaktumForklaring.kt`**
   - Extension functions på `Faktum<T>`:
     - `forklar()` - enkel tekstforklaring
     - `forklarHva()` - viser verdien
   - Extension functions på subsumsjoner:
     - `forklar()` - genererer FORDI forklaring
     - `forklarFordi()` - kombinerer flere subsumsjoner

### Dokumentasjon og eksempler

4. **`src/main/kotlin/no/nav/system/rule/dsl/forklaring/README.md`**
   - Komplett dokumentasjon av API-et
   - Brukseksempler
   - Output formater
   - Fremtidige utvidelser

5. **`src/test/kotlin/no/nav/system/rule/dsl/demo/forklaring/ForklaringDemo.kt`**
   - Kjørbar demo som viser alle funksjoner
   - Demonstrerer enkle og komplekse scenarier
   - Viser både tekst og HTML output

6. **`src/test/kotlin/no/nav/system/rule/dsl/demo/forklaring/ForklaringTest.kt`**
   - 10 enhetstester som verifiserer funksjonalitet
   - Alle tester passerer ✅

## Hvordan det brukes

### Eksempel 1: Enkel formelforklaring

```kotlin
val G = Formel.variable("G", 110000)
val beregning = FormelBuilder.create<Double>()
    .name("fulltSlitertillegg")
    .expression(0.25 * G / 12)
    .build()

println(beregning.forklarKompakt())
```

**Output:**
```
fulltSlitertillegg = 0.25 * G / 12
fulltSlitertillegg = 0.25 * 110000 / 12
fulltSlitertillegg = 2291.67
```

### Eksempel 2: Subsumsjonsforklaring

```kotlin
val antallMåneder = Faktum("antallMånederEtterNedreAldersgrense", 24)
val grense = Faktum("MND_36", 36)
val betingelse = antallMåneder erMindreEnn grense

val forklaring = betingelse.forklar()
println(forklaring.toText())
```

**Output:**
```
FORDI
    'antallMånederEtterNedreAldersgrense' (24) er mindre enn 'MND_36' (36)
```

### Eksempel 3: Komplett forklaring i regel

```kotlin
regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
    HVIS { antallMåneder erMindreEnn MND_36 }
    SÅ {
        justeringsFaktor = FormelBuilder.create<Double>()
            .name("justeringsFaktor")
            .expression((MND_36 - antallMåneder) / MND_36)
            .build()

        // Generer komplett forklaring
        val forklaring = KomplettForklaring(
            hva = justeringsFaktor.forklarHva(),
            hvorfor = (antallMåneder erMindreEnn MND_36).forklar(),
            hvordan = justeringsFaktor.forklarHvordan(),
            referanser = listOf("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT")
        )

        // Forklaringen kan nå lagres eller logges
        println(forklaring.toText())
    }
}
```

**Output:**
```
justeringsFaktor = (MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
justeringsFaktor = (36 - 24) / 36
justeringsFaktor = 0.33

REFERANSE
    SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT

FORDI
    'antallMånederEtterNedreAldersgrense' (24) er mindre enn 'MND_36' (36)

HVORDAN
    justeringsFaktor = (MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
    justeringsFaktor = (36 - 24) / 36
    justeringsFaktor = 0.33

    MND_36 = 36
    antallMånederEtterNedreAldersgrense = 24
```

## Output formater

Systemet støtter to output formater:

### 1. Tekst (toText())
Menneskelesbar tekst med indentering for hierarki.

### 2. HTML (toHTML())
HTML med CSS-klasser for styling:
- `hva-navn`, `hva-symbolsk`, `hva-konkret`, `hva-resultat`
- `hvorfor`, `subsumtion`, `oppfylt`, `ikke-oppfylt`
- `hvordan`
- `komplett-forklaring`, `referanser`, `referanse`

## Integrasjon med eksisterende kode

Forklaringssystemet er designet som non-intrusive extension functions:
- **Ingen endringer** i eksisterende `Formel`, `Faktum` eller `AbstractSubsumtion` klasser
- Kan brukes når det trengs, ikke påkrevd
- Kompatibelt med eksisterende `debug()`, `trace()` og `xmlDebug()` funksjoner

## Testing

- ✅ Alle tester passerer (10/10)
- ✅ Koden kompilerer uten feil
- ✅ Demo kjører og viser forventet output

## Fremtidige forbedringer

Systemet er designet for å vokse. Potensielle utvidelser:
1. Automatisk lagring av forklaringer i regelkomponent-treet
2. Direkte integrasjon med `Rule` og `AbstractRuleset`
3. Bedre håndtering av `Verdi`-objekter i subsumsjoner (krever endringer i `PairSubsumtion`)
4. JSON og XML output formater
5. Interaktiv HTML med expand/collapse
6. Kobling til lovhjemler og juridiske referanser
7. Språkstøtte (norsk/engelsk)
8. Performance optimalisering for store formeltrær

## Tilpasning til KDOC-eksempelet

Systemet er direkte inspirert av KDOC-kommentarene i `BeregnSlitertilleggRSVårFaktumVersjon.kt` (linjer 51-103) og produserer nøyaktig den strukturen som er beskrevet der:

```kotlin
/**
 * Forklaring:
 *      justeringsFaktor = (MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
 *      justeringsFaktor = (36 - 24)  / 36
 *      justeringsFaktor = 0.33
 *      FORDI
 *          antallMånederEtterNedreAldersgrense er mindre enn MND_36
 *          24 er mindre enn 36
 *
 *          FORDI
 *              antallMånederEtterNedreAldersgrense = 24
 */
```

Denne strukturen kan nå genereres programmatisk i stedet for å skrives manuelt i KDOC.

## Neste steg

1. Vurder om forklaringer skal lagres automatisk i regelkomponent-treet
2. Vurder integrasjon med eksisterende inspeksjonssystem
3. Test med reelle regelkompleks fra produksjon
4. Utvikle CSS stylesheet for HTML output
5. Vurder om `PairSubsumtion` og `ListSubsumtion` skal utvides til å holde referanser til `Verdi`-objekter for bedre forklaring
