# Rule-DSL v2.0

## 1. Bakgrunn

Dagens Rule-DSL v1.x er utviklet for å være funksjonelt kompatibel med tidligere Blaze Advisor-løsning.
Migrering fra Blaze til egenutviklet regelplattform ble gjort mulig av Rule-DSL v1. Koden er nå ren Kotlin,
men inneholder flere legacy-konsepter som er arvet fra Blaze og som legger føringer for hvordan regelkoden
utformes:

- a) er unødvendig seremoniell: regelService → regelFlyt → regelsett → regel
- b) begrenser utviklers uttrykksfrihet når alt må passe inn i DSL-språket
- c) øker terskelen for nye utviklere som skal forstå regelkoden
- d) generelt dårlig utnyttelse av Kotlin som programmeringsspråk
- e) regelkoden er ikke tilrettelagt for sporing. Det er gjort forsøk på å bygge sporing inn i eksisterende
  plattform uten å oppnå et godt resultat

Dette utgjør motivasjonen for å bygge ny regelplattform i Rule-DSL v2.0.

## 2. Løsning

Regelkode skrives som vanlige Kotlin-funksjoner. Klassehierarkiet (tjeneste, flyt, regelsett) erstattes av
funksjoner med en usynlig kontekst. DSL-en brukes bare der en faglig beslutning tas, og ellers skrives
ordinær Kotlin.

Sporing bygges inn i selve verdiene. Hver beregnet verdi er et `Faktum` som vet hvordan den ble regnet ut,
hvilken regel som ga den, og hvilken forutsetning regelen ble evaluert under. Det finnes ingen egen
sporingslogg. Forklaringen er grafen av fakta.

Målet er Kotlins fleksibilitet og uttrykkskraft kombinert med en stram DSL for faglige beslutninger.

| Utfordring (kap. 1) | Løsning |
|---|---|
| a) seremoniell struktur | Funksjoner i stedet for klasser. Ingen tjeneste/flyt/regelsett-hierarki. |
| b) begrenset uttrykksfrihet | Vanlig Kotlin overalt. DSL bare for regler. |
| c) høy terskel | Få begreper: `Uttrykk`, `Faktum`, `regel`, `kjør`. |
| d) dårlig utnyttelse av Kotlin | Context parameters, infix-operatorer, typede uttrykk. |
| e) mangelfull sporing | Sporing er en egenskap ved `Faktum`, ikke et eget system. |

## 3. Teknisk konsept

Rammeverket skal forklare hvilke verdier som er brukt, og hvordan og hvorfor de er brukt. Det er delt i tre
lag. Hvert lag bygger bare på lagene under, og hvert lag svarer på sitt spørsmål.

| Lag | Pakke | Svarer på |
|---|---|---|
| 1. Uttrykk | `uttrykk` | Hvordan ble verdien regnet ut? |
| 2. Regel | `regel` | Hvorfor fikk faktumet denne verdien, og under hvilken forutsetning? |
| 3. Orkestrering | `orkestrering` | Hvordan er regeltjenesten organisert? |

Grunnprinsipp: modellen skal være liten. Kompleksitet hører hjemme i DSL-en, ikke i modellen.

Et regelsett er en funksjon med `context(_: RegelKontekst)`:

```kotlin
context(_: RegelKontekst)
fun vilkårsprøvAlderspensjon(alder: Faktum<Int>): Faktum<VilkårEnum> = kjør {
    regel("VilkårOppfylt") {
        HVIS { alder erStørreEllerLik 62 }
        RETURNER { faktum("vilkårAlderspensjon", VilkårEnum.INNVILGET) }
    }
    regel("VilkårIkkeOppfylt") {
        HVIS { alder erMindreEnn 62 }
        RETURNER { faktum("vilkårAlderspensjon", VilkårEnum.AVSLAG) }
    }
}
```

Overgang fra v1:

```
class BeregnGrunnpensjonRS : AbstractRuleset   →  fun beregnGrunnpensjon(...)
ARC.run(parent) / ruleComponent-propagering    →  context(_: RegelKontekst)
"regelA".harTruffet()                          →  val regelA = regel("regelA") { ... }
Formel + Faktum                                →  Uttrykk + Faktum
```

## 4. Lag 1 – Uttrykk

Uttrykk er en uforanderlig trestruktur med en verdi. Formel og predikat har samme modell: både beregninger
og sammenligninger er `Uttrykk`, og begge har `notasjon` og `konkret`.

```kotlin
interface Uttrykk<out T : Any> {
    val verdi: T
    fun notasjon(): String              // "sats * trygdetid / fullTrygdetid"
    fun konkret(): String               // "2.9 * 20 / 40"
    fun grunnlag(): List<Faktum<*>>     // navngitte ledd brukt direkte
}
```

| | notasjon | konkret |
|---|---|---|
| Predikat | forventetInntekt er større enn 2G | 380000 er større enn 264000 |
| Formel | sats * G * trygdetid / 40 | 0.9 * 132000 * 34 / 40 |

- Operatorer for matematikk og for logikk og sammenligning bygger nye uttrykk.
- Sammenligninger tar både `Uttrykk<T>` og ren `T` på høyre eller venstre side.
- Laget kjenner ikke til regler eller sporing.

### Komparatorer

Infix-operatorer som returnerer `Uttrykk<Boolean>`.

| Kategori | Operatorer |
|---|---|
| Numerisk | `erMindreEnn`, `erMindreEllerLik`, `erStørreEnn`, `erStørreEllerLik` |
| Likhet | `erLik`, `erUlik` |
| Dato | `erFør`, `erFørEllerLik`, `erEtter`, `erEtterEllerLik` |
| Liste | `erBlant`, `erIkkeBlant` |
| Logikk | `og`, `eller`, `ikke` |

```kotlin
trygdetid erMindreEnn 40
fødselsdato erFør LocalDate.of(1960, 1, 1)
ytelseType erBlant listOf(YtelseEnum.AP, YtelseEnum.GJR)
```

### Matematiske operatorer og funksjoner

Operatorer: `+`, `-`, `*`, `/`.

Funksjoner: `floor()`, `ceil()`, `abs()`, `min()`, `max()`, samt egne funksjoner som `avrund2desimal()`.

```kotlin
val beløp = avrund2desimal(G * sats / 12)
// notasjon: avrund2desimal(G * sats / 12)
// konkret:  avrund2desimal(118620 * 0.25 / 12)
```

## 5. Lag 2 – Regel

### Faktum

Et `Faktum` er et navngitt uttrykk. Utover er det en grense: andre uttrykk ser bare navnet og verdien.
Innover bærer det sin egen forklaring.

```kotlin
class Faktum<out T : Any>(
    val navn: String,
    val uttrykk: Uttrykk<T>,         // utregningen, eller betingelsene for en regel
    val regel: Faktum<Boolean>?,     // regelen som gjorde at faktumet oppsto
) : Uttrykk<T>
```

- **Inndata** lages med `Faktum(navn, verdi)` og har ingen regel. Det finnes ikke egne begreper
  for inndata som `Verdi` eller `Grunnlag`. Se åpne spørsmål.
- **Utledet faktum** lages med `faktum(...)`. I en `SÅ`- eller `RETURNER`-blokk får det regelen som
  eier blokken, som `regel`. Utenfor en regel er det lov, men da er `regel = null`, og forklaringen viser
  bare utregningen. Brukeren må selv vite om forskjellen.
- **Regel** er et `Faktum<Boolean>` der `uttrykk` er betingelsene. `regel` er forutsetningen, altså
  regelen som regelen ble evaluert under.
- Likhet er identitet.

`Faktum.regel` har dermed én betydning: regelen som gjorde at faktumet oppsto.

### Regel

En regel er en navngitt blokk med betingelser (`HVIS` / `OG`) og et resultat (`SÅ` eller `RETURNER`).
`regel(...)` returnerer et `Faktum<Boolean>`.

```kotlin
regel("FastsettTrygdetid") {
    HVIS { flyktningUtfall != null }                  // Boolean: teknisk guard, spores ikke
    OG { flyktningUtfall erLik UtfallType.OPPFYLT }   // Uttrykk<Boolean>: fagbetingelse, spores
    SÅ { trygdetid = faktum("trygdetid", 40) }
}
```

- Typen på betingelsen bestemmer hvordan den behandles, ikke om den står i `HVIS` eller `OG`.
  - `Uttrykk<Boolean>` er en fagbetingelse. Den spores og vises i forklaringen.
  - `Boolean` er en teknisk guard. Den spores ikke. Er den usann, treffer ikke regelen, og
    betingelsene etter den evalueres ikke.
- `HVIS` og `OG` er like. `OG` finnes for lesbarhetens skyld.
- En regel uten betingelser treffer alltid.
- En regel har enten `SÅ` eller `RETURNER`, ikke begge.

#### Regler som verdier

Fordi en regel er et `Faktum<Boolean>`, kan den brukes som betingelse i andre regler. Dette erstatter
`"regelA".harTruffet()` fra v1.

```kotlin
val afpMellom = regel("AFP-MELLOM") { ... }

regel("AFP-ORDINÆR") {
    HVIS { ikke(afpMellom) }
    SÅ { ... }
}
```

### kjør og RETURNER

`kjør<T> { }` gir en blokk som returnerer en verdi av typen `T`. Inne i blokken kan regler bruke
`RETURNER { T }` i stedet for `SÅ`.

```kotlin
context(_: RegelKontekst)
fun beregn(beløp: Faktum<Int>, G: Faktum<Int>): Faktum<Int> = kjør {
    regel("InntektUnderG") {
        HVIS { beløp erMindreEnn G }
        RETURNER { faktum("uavkortet", beløp) }
    }
    regel("InntektFomG") {
        HVIS { beløp erStørreEllerLik G }
        RETURNER { faktum("avkortet", beløp - 50000) }
    }
}
```

- Bak kulissene holder `kjør` resultatet i en `lateinit var`. `RETURNER` setter den, og `kjør`
  returnerer verdien når blokken er ferdig. Brukeren skriver ikke `return`.
- Når resultatet er satt, evalueres ikke de neste reglene i blokken (first-match). Verdien fra
  `RETURNER` blir verdien til `kjør`. Hva som skjer med vanlig Kotlin-kode mellom reglene, er ikke
  avklart. Se åpne spørsmål.
- `T` trenger ikke være et `Faktum`. Er det et `Faktum` laget med `faktum(...)`, får det regelen som
  `regel`, akkurat som i `SÅ`. Andre typer bærer ingen forklaring.
- Hvis blokken kjører ferdig uten at noen regel leverer en verdi, oppstår en runtime-feil.
- `RETURNER` er bare tilgjengelig innenfor en `kjør`-blokk. `T` utledes fra den nærmeste `kjør`.
  Regler med bare `SÅ` kan også brukes i blokken.
- `kjør` er ingen tracer og lager ingen egen sporing. Den styrer bare kontrollflyten.

### Regelkontekst

`RegelKontekst` er den usynlige konteksten som følger med gjennom hele lag 2, også inn i regler og
`SÅ`-blokker. Den inneholder bare ressurskartet. Den vet ingenting om sporing.

```kotlin
class RegelKontekst(ressurser: Map<KClass<out Ressurs>, Ressurs>) {  // legger alltid til Sporing
    internal val ressurser: Map<KClass<out Ressurs>, Ressurs>         // uforanderlig
    inline fun <reified R : Ressurs> ressurs(): R                     // krasjer hvis ressursen mangler
}

context(k: RegelKontekst) fun regel(navn: String, definisjon: Regel.() -> Unit): Faktum<Boolean>
context(k: RegelKontekst) fun <T : Any> faktum(navn: String, uttrykk: Uttrykk<T>): Faktum<T>
context(k: RegelKontekst) fun <T : Any> kjør(blokk: () -> Unit): T
```

- Konteksten lages én gang, av den som kaller regeltjenesten, og kan ikke endres etterpå.
- Lag 2 lager aldri en kontekst fra bunnen av. Den eneste nye konteksten lag 2 lager, er den som
  `SÅ` og `RETURNER` lager, med `Sporing` byttet ut.
- Funksjoner som lager regler, deklarerer `context(_: RegelKontekst)`. Ingen parametre sendes manuelt.
- Utad tilbyr `RegelKontekst` bare konstruktøren og `ressurs<R>()`. Kartet er internal.

### Ressurser

Alt som skal være tilgjengelig overalt uten å sendes som parameter, ligger i ressurskartet, for eksempel
satser og konfigurasjon. Kartet settes opp én gang og kan ikke endres underveis.

Alle ressurser implementerer markørgrensesnittet `Ressurs`:

```kotlin
interface Ressurs
```

- Kompilatoren sjekker at bare ressurser havner i kartet. `k.ressurs<String>()` kompilerer ikke.
- Grensesnittet er tomt. Det har ingen livssyklusmetoder, fordi rammeverket ikke styrer ressursene.
- Klasser fra tredjepart (HTTP-klient, databaseklient, logger) pakkes inn i en klasse som implementerer
  `Ressurs`.

Den som definerer en ressurs, tilbyr også tilgangsfunksjoner for den. Funksjonene skrives som
context-funksjoner. Extension-funksjoner på `RegelKontekst` kan ikke kalles implisitt fra en
context-funksjon.

```kotlin
class GrunnbeløpSatser(...) : Ressurs {
    fun på(dato: LocalDate): Faktum<Int> = ...         // krasjer hvis det ikke finnes en sats for datoen
}

context(k: RegelKontekst)
fun grunnbeløp(dato: LocalDate): Faktum<Int> = k.ressurs<GrunnbeløpSatser>().på(dato)
```

- Tilgangsfunksjoner returnerer `Faktum`, slik at verdien er navngitt i forklaringen.
- Ressurser skrives ikke defensivt. Mangler en ressurs eller en verdi i den, krasjer kallet med en
  melding som navngir ressursen.
- Det finnes ingen `NoOp`-varianter og ingen valgfrie ressurser.
- Rammeverket legger ikke til rette for noen bestemt ressurstype og varsler ikke ressurser om regler
  eller fakta.

### Sporing

Sporing er alltid på. Uttrykkstreet bygges uansett, fordi det er selve utregningen. Det er derfor lite å
spare på å slå sporing av, og det ville krevd en egen kodevei. Det finnes ingen `Tracer`, `NoOpTracer`,
`traced`-blokk eller `debugTree()`.

`Sporing` er en ressurs som holder den aktive forutsetningen. `RegelKontekst` legger den alltid i kartet,
og brukeren gjør det aldri.

```kotlin
class Sporing internal constructor(val forutsetning: Faktum<Boolean>?) : Ressurs
```

- `regel(...)` lager et `Faktum<Boolean>` med betingelsene som `uttrykk` og
  `regel = sporing.forutsetning`.
- `SÅ` og `RETURNER` kjører kroppen i en ny `RegelKontekst`. Kartet er det samme, bortsett fra at
  `Sporing` er byttet ut med `Sporing(forutsetning = denne regelen)`. Den nye konteksten overstyrer den
  ytre, også gjennom vanlige funksjonskall. Det finnes ingen push/pop og ingen mutabel tilstand.
- `faktum(...)` lager et faktum med `uttrykk` og `regel = sporing.forutsetning`. Det er altså den
  nærmeste regelen som slo til, også om faktumet lages i en funksjon kalt fra `SÅ`.

### Regler som ikke treffer

Regler som ikke treffer, spores ikke. Dette er et grunnleggende valg for å holde kompleksiteten nede.

- Det finnes ingen logg eller graf over evaluerte regler, bare `Faktum`-grafen.
- En regel som ikke traff, er et `Faktum<Boolean>` med verdi `false`. Den blir bare en del av en
  forklaring hvis en annen regel bruker den som betingelse, for eksempel `ikke(afpMellom)`.
- En regel som ikke traff, har ingen `SÅ`- eller `RETURNER`-blokk som har kjørt. Den har derfor ingen
  fakta som peker på den.

### Forklaring

Forklaringen går bakover fra et resultat og nøster i én graf: `uttrykk`, `grunnlag()` og `regel`.

```kotlin
val forklaring = slitertillegg.forklar()
```

```
slitertillegg = 1.45
  Beregning     sats (2.9) * trygdetid (20) / full trygdetid (40)
  Betingelser   trygdetid (20) er mindre enn full trygdetid (40)
  Forutsetning  vilkårsvedtak sliterordning er lik INNVILGET
```

| Linje | Kilde |
|---|---|
| Beregning | `faktum.uttrykk` |
| Betingelser | `faktum.regel.uttrykk` |
| Forutsetning | `faktum.regel.regel` (en kjede som kan nøstes videre) |

Hvert `Faktum` i `grunnlag()` kan forklares på samme måte.

## 6. Lag 3 – Orkestrering

Lag 3 er ikke avklart. Lag 2 alene dekker det v1 brukte regelflyter til, siden både regler og flyter i
praksis gjør kode betinget: beslutninger tas med `regel`, og resten er vanlig Kotlin.

Hvis laget innføres, gjelder disse føringene:

- En `Regeltjeneste` konstrueres med en `RegelKontekst`. Det er inngangen til rammeverket.
- Beslutninger i flyten er regler fra lag 2. Det som kjøres i `SÅ`, får beslutningen som forutsetning
  automatisk.
- Laget inneholder ingen egen graf. Hvorfor noe kjørte, ligger i `Faktum.regel`.

Vurderinger:

- Uten et eget flytbegrep blir det trolig vanskeligere å generere flytdiagram.
- Hvis flyter er en god måte å organisere kode på, kan vi innføre en lettere variant av `Regelflyt`.
- Skal en forgrening kunne returnere en verdi av typen `T`, tilsvarende `kjør`?

## 7. Eksempel: slitertillegg

```kotlin
context(_: RegelKontekst)
fun beregnSlitertillegg(
    antallMnd: Faktum<Int>,     // 18
    trygdetid: Faktum<Int>,     // 30
    G: Faktum<Int>,             // 118620
): Faktum<Double> = kjør {
    val fullTrygdetid = Faktum("fullTrygdetid", 40)
    val fulltSlitertillegg = faktum("fulltSlitertillegg", avrund2(G * 0.25 / 12))
    lateinit var justeringsFaktor: Faktum<Double>

    regel("UTTAK-TIDLIG") {
        HVIS { antallMnd erMindreEnn 36 }
        SÅ { justeringsFaktor = faktum("justeringsFaktor", (36 - antallMnd) / 36) }
    }

    regel("UTTAK-SENT") {
        HVIS { antallMnd erStørreEllerLik 36 }
        SÅ { justeringsFaktor = faktum("justeringsFaktor", 0.0) }
    }

    regel("AVKORTING-TRYGDETID") {
        RETURNER {
            faktum(
                "slitertillegg",
                avrund2(fulltSlitertillegg * justeringsFaktor * trygdetid / fullTrygdetid)
            )
        }
    }
}
```

Forklaring:

```
slitertillegg = 926.72
  Beregning     avrund2(fulltSlitertillegg (2471.25) * justeringsFaktor (0.5) * trygdetid (30) / fullTrygdetid (40))

  justeringsFaktor = 0.5
    Beregning     (36 - antallMnd (18)) / 36
    Betingelser   antallMnd (18) er mindre enn 36

  fulltSlitertillegg = 2471.25
    Beregning     avrund2(G (118620) * 0.25 / 12)
```

## 8. Begrensninger

- Bare beslutninger tatt i en regel blir begrunnet. Vanlig `if`/`when` utenfor en `SÅ`-blokk er usynlig.
- Konteksten følger kallet. Bytter koden tråd eller korutine, må konteksten sendes med.
- First-match i `when` vises ikke i forklaringen. Bruk `kjør` og `RETURNER` når rekkefølgen er faglig
  viktig. Heller ikke da vises reglene som ikke traff.
- Reglene evalueres bare når de har data. Det er ikke mulig å dokumentere reglene uten data.

## 9. Åpne spørsmål

1. **Formatering og presentasjon av forklaringen.** Mottakeren skal bestemme formatet. Rammeverket
   tilbyr en håndfull formater, eller brukeren sender inn sin egen formateringsfunksjon. Modellen må
   derfor eksponere nok struktur til at en ekstern funksjon kan formatere den, ikke bare ferdige strenger
   fra `notasjon()` og `konkret()`. Eksempler på formater:
   - kompakt: `antallMåneder (15) er mindre enn 36`
   - utvidet: én rad med notasjon (`G * 2`) og én med konkrete verdier (`130000 * 2`)
   - frontend: navngitte fakta rendres som lenker man kan navigere gjennom, eller hele grafen som et tre
2. **Regler over lister og map.** I v1 brukes `Pattern`. Forslag fra v3: `regel` tar en `List` eller
   `Map` direkte, og `Pattern` blir et internt konsept.
   ```kotlin
   regel("ReguleringOpptjening", opptjeningsgrunnlagListe) { opptj ->
       HVIS { opptj.år erStørreEnn 2025 }
       SÅ { ... }
   }
   ```
   Evalueringsrekkefølgen skal følge listen, ikke rekkefølgen reglene er definert i
   ([issue #113](https://github.com/navikt/rule-dsl/issues/113)). Med regel R1 og R2 over elementene
   I1 og I2 evaluerer pattern i v1 én regel for alle elementer før neste regel slipper til:
   `R1:I1 → R1:I2 → R2:I1 → R2:I2`. Ønsket er at alle regler evalueres ferdig per element:
   `R1:I1 → R2:I1 → R1:I2 → R2:I2`.

   Eksempel fra issuet, med reglene `odd` og `even` over tallene 1–10:
   - v1-pattern: `1 odd, 3 odd, 5 odd, 7 odd, 9 odd, 2 even, 4 even, 6 even, 8 even, 10 even`
   - ønsket: `1 odd, 2 even, 3 odd, 4 even, …`

   Dette er nødvendig når en regel for et element bygger på resultatet for forrige element, for
   eksempel venteperiode per beregningsvilkårsperiode (BVP), der en periode kan arve resultatet fra
   forrige periode. Med regler som funksjonskall følger rekkefølgen naturlig av en vanlig løkke:
   ```kotlin
   bvpListe.forEach { bvp ->
       regel("FørsteBvp") { ... }
       regel("Gradsøkning") { ... }
   }
   ```
   Det er uavklart om det trengs egen DSL (`forHvert`), og hvordan regler per element navngis.
3. **Referanse til lovtolkning.** v3 foreslår `REF("BER-TT", "https://confluence.nav/...")` i regelen.
   Det er uavklart hvordan referansen lagres i modellen (`Faktum<Boolean>`) og vises i forklaringen.
4. **Lag 3 – orkestrering.** Se kapittel 6.
5. **Mekanismen bak `RETURNER`.** Planen er at `kjør` holder resultatet i en `lateinit var`. Dette må
   undersøkes nærmere:
   - Hvordan hindres de neste reglene i å evaluere når resultatet er satt? For eksempel ved at `regel`
     sjekker om `kjør` allerede har et resultat.
   - Vanlig Kotlin-kode mellom reglene kjører fortsatt etter et treff. Er det akseptabelt, eller må
     brukeren selv passe på det?
   - Hva skjer hvis `RETURNER` treffer i en nøstet regel inne i en `SÅ`-blokk?
6. **Inndata uten regel.** I dag er inndata et `Faktum(navn, verdi)` med `regel = null`. Det betyr at
   `regel = null` kan bety både «inndata» og «utledet utenfor en regel», og forklaringen kan ikke skille
   dem. Vurder om dette er uakseptabelt, og om vi eventuelt skal innføre `Verdi` for inndata og
   konstanter. Da får `Faktum` alltid en regel, mens `Verdi` er navngitt, men ikke begrunnet.

## Vedlegg: Avklaringer fra v3 og DESIGN.md

| Tema | v3 | DESIGN.md | v4 |
|---|---|---|---|
| Sporing | `traced`-blokk, `Tracer`, `NoOpTracer`, `debugTree()`, regler som ikke traff vises | Alltid på, bare `Faktum`-grafen | DESIGN.md |
| RETURNER | `RETURNER { }` i regel | Bare `SÅ` + `lateinit var` | `kjør<T> { }` + `RETURNER { T }`, `lateinit var` bak kulissene, runtime-feil uten treff. Mekanismen er åpen. |
| Regelflyt | Utgår | Lag 3 med `Regeltjeneste`/`Regelflyt` | Åpent spørsmål |
| Forklaringsformat | HVA / HVORFOR / HVORDAN | Beregning / Betingelser / Forutsetning | DESIGN.md |
| Inndata | `Verdi`, `Grunnlag` | `Faktum(navn, verdi)` | DESIGN.md, men `Verdi` er et åpent spørsmål |
| Kontekst | `RuleContext`, `ResourceAccessor`-extensions | `RegelKontekst`, `Ressurs`, context-funksjoner | DESIGN.md |
| Utledet faktum | `sporing(navn, uttrykk)` | `faktum(navn, uttrykk)` | DESIGN.md |
| HVIS / OG | `HVIS` teknisk, `OG` faglig | Typen bestemmer | Typen bestemmer |
| Regelkjeding | Vurdering | `regel(...)` returnerer `Faktum<Boolean>` | `val regelA = regel(...)` |
