# prototype-regelsett

Prototype av en sporingsmodell der alle faktum har en regel. Uavhengig av `rdsl2`.

## Filer

| Fil | Innhold |
|---|---|
| `Uttrykk.kt` | `Uttrykk`, interne noder (`Konstant`, `Binær`, `Og`) og operatorer |
| `Faktum.kt` | `Faktum`, `Regel` og `DefaultRegel` |
| `Regelsett.kt` | `regelsett`, `regel`, `HVIS`/`OG`/`SÅ`, `RETURNER` |
| `Forklaring.kt` | `forklar()`: nøster nedover fra et faktum |
| `eksempel/Slitertillegg.kt` | Små eksempler: ett faktum, flere faktum (`Pair`), `regelsett<Unit>` med tre typer kobling |
| `eksempel/SlitertilleggRegelsett.kt` | Større regelsett: tidlig retur, regel uten betingelse, lokale variabler, regel som betingelse, retur |
| `SlitertilleggTest.kt`, `SlitertilleggRegelsettTest.kt` | Kjører eksemplene og skriver ut forklaringer |

## Regler

- Brukeren lager faktum på én måte: `Faktum(navn, verdi)` eller `Faktum(navn, uttrykk)`.
- Alle faktum har en regel:
  - I en `SÅ`-blokk får faktumet regelen som eier blokken. Det skjer automatisk: `RegelKropp` har
    funksjoner som heter `Faktum`, og de vinner over de vanlige `Faktum`-funksjonene.
  - Utenfor en regel får faktumet `DefaultRegel`. Det gir ingen regel-linje i forklaringen.
- En regel kjenner ikke de andre reglene i regelsettet. To regler henger bare sammen når den ene
  bruker noe den andre har laget:
  1. et faktum i en betingelse,
  2. et faktum i en utregning,
  3. selve regelen (`Faktum<Boolean>`) i en betingelse.
- `regel(...)` vurderes med en gang. Rekkefølgen i koden er rekkefølgen reglene kjøres i.
- Betingelser i `HVIS`/`OG` er av to slag:
  - Funksjonell (`Uttrykk<Boolean>`): spores. Er den usann, vurderes resten likevel, så forklaringen
    viser alle grunnene til at regelen ikke slo til.
  - Teknisk (`Boolean`, f.eks. null-sjekk): spores ikke. Er den usann, vurderes ingen flere
    betingelser i regelen, og forklaringen viser bare «teknisk betingelse ikke oppfylt».
- Første `RETURNER` avslutter regelsettet. Uten `RETURNER` må returtypen være `Unit`, ellers kjøretidsfeil.

## Kjent hull

Et faktum som lages i en hjelpefunksjon som kalles fra `SÅ`, får `DefaultRegel`, fordi
hjelpefunksjonen ikke ser `SÅ`-blokken. Det synes i forklaringen ved at regel-linjen mangler.

## Feilbruk som ikke kompilerer

```kotlin
regelsett<Unit>("y") {
    regel("A") {
        regel("NØSTET") { }                      // regel i regel
        SÅ { RETURNER(1) }                       // feil type: forventet Unit
    }
}
val z = regelsett("z") {                         // typen kan ikke utledes uten RETURNER
    regel("A") { SÅ { Faktum("x", 1) } }
}
regelsett<Faktum<Double>>("u") {
    regel("A") { SÅ { regelsett<Unit>("indre") {
        regel("B") { SÅ { RETURNER(Faktum("x", 1.0)) } }   // RETURNER til ytre regelsett fra indre
    } } }
}
```
