# rdsl2 – design

Rammeverket skal forklare hvilke verdier som er brukt, og hvordan og hvorfor de er brukt.
Det er delt i tre lag. Hvert lag bygger bare på lagene under, og hvert lag svarer på sitt spørsmål.

| Lag | Pakke | Svarer på |
|---|---|---|
| 1. Uttrykk | `uttrykk` | Hvordan ble verdien regnet ut? |
| 2. Regel | `regel` | Hvorfor fikk faktumet denne verdien, og under hvilken forutsetning? |
| 3. Orkestrering | `orkestrering` | Hvordan er regeltjenesten organisert? |

Grunnprinsipp: modellen skal være liten. Kompleksitet hører hjemme i DSL-en, ikke i modellen.

## Lag 1 – Uttrykk

Uttrykk er en uforanderlig trestruktur med en verdi.

```kotlin
interface Uttrykk<out T : Any> {
    val verdi: T
    fun notasjon(): String              // "sats * trygdetid / fullTrygdetid"
    fun konkret(): String               // "2.9 * 20 / 40"
    fun grunnlag(): List<Verdi<*>>      // navngitte ledd brukt direkte
}
```

- Operatorer for matematikk (`+ - * /`) og sammenligning (`erMindreEnn`, `erLik`, …) bygger
  nye uttrykk.
- Sammenligninger tar både `Uttrykk<T>` og ren `T` på høyre eller venstre side.
- Laget kjenner ikke til regler eller sporing.

## Lag 2 – Regel

### Verdi, Faktum og Regel

Tre typer, med `Uttrykk` på toppen:

```
Uttrykk<T>
├── Konstant, Sum, Produkt, Sammenligning, Og, Ikke, …   (lag 1)
└── Verdi<T>                 navn og verdi
    ├── Faktum<T>            + uttrykk, regel
    └── Regel                + betingelser          (Verdi<Boolean>)
```

```kotlin
open class Verdi<out T : Any>(val navn: String, override val verdi: T) : Uttrykk<T>   // inndata

class Faktum<out T : Any> internal constructor(
    navn: String,
    val uttrykk: Uttrykk<T>,         // Beregning
    val regel: Regel,                // regelen som fastsatte faktumet
) : Verdi<T>(navn, uttrykk.verdi)

class Regel internal constructor(
    navn: String,
    val betingelser: Uttrykk<Boolean>,   // Betingelser
) : Verdi<Boolean>(navn, betingelser.verdi)
```

- **Verdi** er en navngitt verdi. Inndata lages med `Verdi(navn, verdi)`. Det er den eneste
  typen brukeren konstruerer selv.
- **Faktum** er en verdi som en regel har fastsatt. Det lages med `faktum(...)` i en
  `SÅ`-blokk, og har alltid en regel.
- **Regel** er en beslutning. Den lages med `regel(...)`, og er et `Uttrykk<Boolean>` som kan
  brukes som predikat i andre regler.
- Grensen går ved `Verdi`. I andre uttrykk vises alle tre bare med navn og verdi, for
  eksempel «trygdetid (20)» eller «AFP-MELLOM (false)». Forklaringen innover ligger i
  `Faktum` og `Regel`.
- Konstruktørene til `Faktum` og `Regel` er internal, så bare rammeverket lager dem.
  `Verdi` har offentlig konstruktør, og brukeren kan i prinsippet arve fra den. Det koster lite:
  en egen subklasse vises og forklares som en vanlig `Verdi`.
- Likhet er identitet.
- Hvordan en regel knyttes til regelen den ble evaluert under (forutsetningen), er ikke
  avklart. Se oppfølgingspunkter.

### Regelkontekst (`@Context`)

`RegelKontekst` er den usynlige konteksten som følger med gjennom hele lag 2, også inn i
regler og `SÅ`-blokker. Den inneholder bare ressurskartet. Den vet ingenting om sporing.

```kotlin
class RegelKontekst(ressurser: Map<KClass<out Ressurs>, Ressurs>) {  // legger alltid til Sporing
    internal val ressurser: Map<KClass<out Ressurs>, Ressurs>         // uforanderlig
    inline fun <reified R : Ressurs> ressurs(): R                     // krasjer hvis ressursen mangler
}

context(k: RegelKontekst) fun regel(navn: String, definisjon: RegelBuilder.() -> Unit): Regel
context(k: RegelKontekst) fun <T : Any> faktum(navn: String, uttrykk: Uttrykk<T>): Faktum<T>
```

- Konteksten lages én gang, av den som kaller regeltjenesten, og sendes inn når tjenesten
  konstrueres (se lag 3). Kartet kan ikke endres etterpå.
- Lag 2 lager aldri en kontekst fra bunnen av. Den eneste nye konteksten lag 2 lager, er
  den som `SÅ` lager, med `Sporing` byttet ut.
- Funksjoner som lager regler, deklarerer `context(_: RegelKontekst)`. Ingen parametre
  sendes manuelt.
- Utad tilbyr `RegelKontekst` bare konstruktøren og `ressurs<R>()`. Kartet er internal.
  Ressurser hentes alltid eksplisitt fra konteksten: `k.ressurs<R>()`.
- `RegelKontekst` erstatter `RegelKropp`. DSL-byggeren med `HVIS`/`OG`/`SÅ` heter
  `RegelBuilder`.

### Ressurser

Alt som skal være tilgjengelig overalt uten å sendes som parameter, ligger i ressurskartet.
Kartet settes opp én gang når regeltjenesten konstrueres. Det kan ikke endres
underveis.

Alle ressurser implementerer markørgrensesnittet `Ressurs`:

```kotlin
interface Ressurs
```

- Kompilatoren sjekker at bare ressurser havner i kartet. `k.ressurs<String>()` kompilerer
  ikke, og kartet kan ikke brukes som en sekk for vilkårlige data.
- Alle implementasjoner av `Ressurs` er tjenestens ressurser, så de er lette å finne.
- Grensesnittet er tomt. Det er ingen abstrakt klasse, fordi det ikke finnes felles
  tilstand å arve. Det har ingen livssyklusmetoder, fordi rammeverket ikke styrer
  ressursene.
- Klasser fra tredjepart (HTTP-klient, databaseklient, logger) pakkes inn i en klasse som
  implementerer `Ressurs`. Innpakningen er det naturlige stedet for tilgangsfunksjonene.

Den som definerer en ressurs, tilbyr også tilgangsfunksjoner for den. Funksjonene skrives
som context-funksjoner. Extension-funksjoner på `RegelKontekst` kan ikke kalles implisitt
fra en context-funksjon, så de virker ikke her.

```kotlin
class GrunnbeløpSatser(...) : Ressurs {
    fun på(dato: LocalDate): Faktum<Int> = ...         // krasjer hvis det ikke finnes en sats for datoen
}

context(k: RegelKontekst)
fun grunnbeløp(dato: LocalDate): Faktum<Int> = k.ressurs<GrunnbeløpSatser>().på(dato)
```

- Tilgangsfunksjoner returnerer `Faktum`, slik at verdien er navngitt i forklaringen.
- Ressurser skrives ikke defensivt. Mangler en ressurs, eller mangler en verdi i den,
  krasjer kallet med en melding som navngir ressursen. Det er en feil i oppsettet, ikke
  noe regelkoden skal håndtere.
- `faktum()` bruker samme mekanisme for `Sporing`. Rammeverket har ingen egen vei for seg
  selv.
- Det finnes ingen `NoOp`-varianter og ingen valgfrie ressurser.
- Rammeverket legger ikke til rette for noen bestemt ressurstype. En logger, en
  databaseklient eller en REST-klient er en ressurs som alle andre, og regelkoden kaller den
  selv der den trenger det. Rammeverket tilbyr bare ressurskartet overalt i regelkoden, og
  varsler ikke ressurser om regler eller fakta.

### Sporing

Sporing er alltid på. Uttrykkstreet bygges uansett, fordi det er selve utregningen. Det er
derfor lite å spare på å slå sporing av, og det ville krevd en egen kodevei.

`Sporing` er en ressurs som holder regelen som eier den aktive `SÅ`-blokken. Alt som har med
sporing å gjøre, ligger her. `RegelKontekst` legger den alltid i kartet, og brukeren gjør
det aldri.

```kotlin
class Sporing internal constructor(val regel: Regel?) : Ressurs
```

- `regel(...)` lager en `Regel` med betingelsene.
- `SÅ` kjører kroppen i en ny `RegelKontekst`. Kartet er det samme, bortsett fra at
  `Sporing` er byttet ut med `Sporing(regel = denne regelen)`. Den nye konteksten
  overstyrer den ytre, også gjennom vanlige funksjonskall. Det finnes ingen push/pop og
  ingen mutabel tilstand.
- `faktum(...)` lager et faktum med `uttrykk` og `regel = sporing.regel`. Det er altså den
  nærmeste regelen som slo til, også om faktumet lages i en funksjon kalt fra `SÅ`.
- Utenfor enhver regel er `sporing.regel` null. Hva `faktum(...)` skal gjøre da, henger
  sammen med oppfølgingspunktet om forutsetning.

### Regler som ikke treffer

Regler som ikke treffer, spores ikke. Dette er et grunnleggende valg for å holde
kompleksiteten nede.

- Det finnes ingen logg eller graf over evaluerte regler, bare `Faktum`-grafen.
- En regel som ikke traff, er en `Regel` med verdi `false`. Den blir bare en
  del av en forklaring hvis en annen regel bruker den som predikat, for eksempel
  `afpMellom erLik false`.
- En regel som ikke traff, har ingen `SÅ`-blokk som har kjørt. Den har derfor ingen fakta
  som peker på den.

### DSL

```kotlin
context(_: RegelKontekst)
fun beregnSlitertillegg(...): Faktum<Double> {
    lateinit var slitertillegg: Faktum<Double>
    regel("AVKORTING-TRYGDETID") {
        HVIS { trygdetid erMindreEnn fullTrygdetid }    // sporet predikat
        OG { x != null }                                // teknisk guard, spores ikke
        SÅ { slitertillegg = faktum("slitertillegg", sats * trygdetid / fullTrygdetid) }
    }
    return slitertillegg
}
```

### Forklaring

Forklaringen nøster i én graf: `uttrykk`, `grunnlag()`, `regel` og `betingelser`.

```
slitertillegg = 1.45
  Beregning     sats (2.9) * trygdetid (20) / full trygdetid (40)
  Betingelser   trygdetid (20) er mindre enn full trygdetid (40)
  Forutsetning  vilkårsvedtak sliterordning er lik INNVILGET
```

| Linje | Kilde |
|---|---|
| Beregning | `faktum.uttrykk` |
| Betingelser | `faktum.regel.betingelser` |
| Forutsetning | ikke avklart, se oppfølgingspunkter |

## Lag 3 – Orkestrering

Lag 3 orkestrerer flyten gjennom en regeltjeneste. Laget er foreløpig ikke innført. Når det
trengs, kommer klassene `Regeltjeneste` og `Regelflyt` med en tilhørende DSL for å styre
flyten.

- En `Regeltjeneste` konstrueres med en `RegelKontekst`. Det er inngangen til rammeverket.
- `Regelflyt` og `Regel` får `RegelKontekst` fra rammeverket. Brukeren sender den aldri
  inn selv.
- Beslutninger i flyten er regler fra lag 2. Fakta som lages i `SÅ`, får beslutningen som
  regel automatisk. Hvordan regler under beslutningen knyttes til den, avhenger av
  oppfølgingspunktet om forutsetning.
- Laget inneholder ingen egen graf. Hvorfor noe kjørte, ligger i `Faktum.regel`.

## Begrensninger

- Bare beslutninger tatt i en regel blir begrunnet. Vanlig `if`/`when` utenfor en
  `SÅ`-blokk er usynlig.
- Konteksten følger kallet. Bytter koden tråd eller korutine, må konteksten sendes med.
- First-match i `when` gjenspeiles ikke i forklaringen.

## Oppfølgingspunkter

1. Forutsetning. En regel som evalueres i en `SÅ`-blokk, er evaluert under regelen som eier
   blokken. Det er forutsetningen, og den gir Forutsetning-linjen i forklaringen. Hvordan
   den skal modelleres, er ikke avklart:
   - `Regel.forutsetning: Regel?` krever null for regler på øverste nivå.
   - En standardverdi `ALLTID` kan ikke selv være en `Regel`, fordi den da trenger en
     forutsetning under konstruksjon (verifisert: `NullPointerException` når klassen lastes).
   - `forutsetning: Verdi<Boolean> = ALLTID`, med `ALLTID` som en ren `Verdi<Boolean>`,
     unngår både null og sykel, men gir svakere typing. Nøsting krever `is Regel`.

   Valget avgjør også hva `faktum(...)` utenfor en regel skal gi.

## Åpne spørsmål

1. Formatering av forklaringen. Målet er at mottakeren bestemmer formatet: rammeverket
   tilbyr en håndfull formater, eller brukeren sender inn sin egen formateringsfunksjon.
   Det betyr at modellen må eksponere nok struktur til at en ekstern funksjon kan
   formatere den, ikke bare ferdige strenger fra `notasjon()` og `konkret()`.
