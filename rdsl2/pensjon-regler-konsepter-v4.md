# Rule-DSL v2.0

## 1. Bakgrunn

Dagens Rule-DSL v1.x er utviklet for å være funksjonelt kompatibel med tidligere Blaze Advisor-løsning.
Migrering fra Blaze til egenutviklet regelplattform ble gjort mulig av Rule-DSL v1.0. Koden er nå ren Kotlin,
men inneholder flere legacy-konsepter som er arvet fra Blaze og som legger føringer for hvordan regelkoden
utformes:

- a) er unødvendig seremoniell: regelService → regelFlyt → regelsett → regel
- b) begrenser utviklers uttrykksfrihet når alt må passe inn i DSL-språket
- c) øker terskelen for nye utviklere som skal forstå regelkoden
- d) generelt dårlig utnyttelse av Kotlin som programmeringsspråk
- e) regelkoden er ikke tilrettelagt for sporing. Forsøk på å bygge inn sporing har ikke ført frem.

Dette utgjør motivasjonen for å bygge ny regelplattform i Rule-DSL v2.0.

## 2. Løsning

Regelkode skrives som vanlige Kotlin-funksjoner og benytter DSLen kun når faglig beslutning tas.

Sporing bygges inn i selve svarene. Hver beregnet verdi er et `Faktum` som vet hvordan den ble regnet ut
og hvilken regel som ga den. Det finnes ingen egen sporingslogg. Forklaringen er grafen av fakta.

Målet er Kotlins fleksibilitet og uttrykkskraft kombinert med en stram DSL for faglige beslutninger.

| Utfordring (kap. 1) | Løsning |
|---|---|
| a) seremoniell struktur | Funksjoner i stedet for klasser. Ingen tjeneste/flyt/regelsett-hierarki. |
| b) begrenset uttrykksfrihet | Vanlig Kotlin overalt. DSL bare for regler. |
| c) høy terskel | Få begreper: `Uttrykk`, `Verdi`, `Faktum`, `Regel`, `kjør`. |
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
fun vilkårsprøvAlderspensjon(alder: Verdi<Int>): Faktum<VilkårEnum> = kjør {
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
Formel + Faktum                                →  Uttrykk + Verdi / Faktum
```

## 4. Lag 1 – Uttrykk

Et uttrykk er en utregning som husker hvordan den ble gjort. `G * sats` gir verdien `78289.2`, og kan i
tillegg vises som `G * sats` og som `118620 * 0.66`. Verdien regnes ut når uttrykket lages, og uttrykket endres
ikke etterpå. Det som vises, er derfor alltid det som faktisk ble regnet ("single source of truth").

Beregninger (`G * sats`) og sammenligninger (`trygdetid erMindreEnn 40`) er begge uttrykk. Den eneste
forskjellen er typen på verdien.

```kotlin
interface Uttrykk<out T : Any> {
    val verdi: T
    fun notasjon(): String              // "sats * trygdetid / fullTrygdetid"
    fun konkret(): String               // "2.9 * 20 / 40"
    fun grunnlag(): List<Verdi<*>>      // navngitte ledd benyttet i dette uttrykk
}
```

| | notasjon | konkret |
|---|---|---|
| Predikat | forventetInntekt er større enn 2G | 380000 er større enn 264000 |
| Formel | sats * G * trygdetid / 40 | 0.9 * 132000 * 34 / 40 |

- Operatorer for matematikk og for logikk og sammenligning bygger nye uttrykk.
- Sammenligninger tar både `Uttrykk<T>` og ren `T` på høyre eller venstre side.
- Tilrettelegger for de fleste operatorer (lik, ulik, større, mindre, etc etc) for de mest brukte datatyper (Number og LocalDate)
- Laget kjenner ikke til regler eller sporing.

### Verdi

En `Verdi` er et navngitt uttrykk, for eksempel inndata, konstanter og satser. Den er en grense i
uttrykkstreet: i andre uttrykk vises den bare med navn og verdi, og `grunnlag()` stopper ved den.

```kotlin
open class Verdi<out T : Any>(val navn: String, override val verdi: T) : Uttrykk<T> {
    override fun notasjon() = navn                 // "G"
    override fun konkret() = verdi.vis()           // "118620"
    override fun grunnlag() = listOf(this)
}
```

- `Verdi` hører hjemme i lag 1. `grunnlag()` returnerer `Verdi`, og uttrykk kan ikke brukes eller
  testes uten navngitte verdier. Den kjenner ikke til regler.
- Lag 2 utvider `Verdi` med `Faktum` og `Regel`.
- Konstruktøren er offentlig, og brukeren kan i prinsippet arve fra `Verdi`. Det koster lite: en egen
  subklasse vises og forklares som en vanlig `Verdi`.
- Likhet er identitet.

### Operatorer

#### Logiske

For å spore operandene i et logisk uttrykk innføres det egne infix-operatorer som returnerer `Uttrykk<Boolean>`.

| Kategori | Operatorer |
|---|---|
| Numerisk | `erMindreEnn`, `erMindreEllerLik`, `erStørreEnn`, `erStørreEllerLik` |
| Likhet | `erLik`, `erUlik` |
| Dato | `erFør`, `erFørEllerLik`, `erEtter`, `erEtterEllerLik` |
| Liste | `erBlant`, `erIkkeBlant` |

```kotlin
trygdetid erMindreEnn 40
fødselsdato erFør LocalDate.of(1960, 1, 1)
ytelseType erBlant listOf(YtelseEnum.AP, YtelseEnum.GJR)
```

#### Matematiske

For å spore operandene i et matematisk uttrykk overstyres infix-operatorer som returnerer `Uttrykk<Number>`.

Operatorer: `+`, `-`, `*`, `/`.

Funksjoner: `floor()`, `ceil()`, `abs()`, `min()`, `max()`, samt egne funksjoner som `avrund2desimal()`.

```kotlin
val beløp = avrund2desimal(G * sats / 12)
// beløp.notasjon() -> avrund2desimal(G * sats / 12)
// beløp.konkret() -> avrund2desimal(118620 * 0.25 / 12)
```

## 5. Lag 2 – Regel

### Faktum og Regel

Lag 2 utvider `Verdi` fra lag 1 med to typer:

```
Uttrykk<T>                                                  (lag 1)
├── Konstant, Infiks, Funksjon, …                           (lag 1)
└── Verdi<T>                 navn og verdi                  (lag 1)
    ├── Faktum<T>            + uttrykk, regel               (lag 2)
    └── Regel                + betingelser                  (lag 2, Verdi<Boolean>)
```

```kotlin
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

- **Faktum** er en verdi som en regel har fastsatt. Det lages med `faktum(...)` i en `SÅ`- eller
  `RETURNER`-blokk, og har alltid en regel.
- **Regel** er en beslutning. Den lages med `regel(...)`, og er et `Uttrykk<Boolean>` som kan brukes som
  betingelse i andre regler.
- Begge er en `Verdi`. I andre uttrykk vises de derfor bare med navn og verdi, for eksempel
  «trygdetid (20)» eller «AFP-MELLOM (false)». Forklaringen innover ligger i `uttrykk` og `betingelser`.
- Konstruktørene er internal, så bare rammeverket lager dem. Brukeren lager bare `Verdi`.
- Hvordan en regel knyttes til regelen den ble evaluert under (forutsetningen), er ikke avklart. Se
  åpne spørsmål.

### Regel

En regel er en navngitt blokk med betingelser (`HVIS` / `OG`) og et resultat (`SÅ` eller `RETURNER`).
`regel(...)` returnerer en `Regel`. DSL-byggeren med `HVIS`/`OG`/`SÅ`/`RETURNER` heter `RegelBuilder`.

```kotlin
regel("FastsettTrygdetid") {
    HVIS { flyktningUtfall != null }                  // Boolean: teknisk guard, spores ikke
    OG { flyktningUtfall erLik UtfallType.OPPFYLT }   // Uttrykk<Boolean>: fagbetingelse, spores
    SÅ { trygdetid = faktum("trygdetid", 40) }
}
```

- Typen på betingelsen bestemmer hvordan den behandles, ikke om den står i `HVIS` eller `OG`.
    - `Uttrykk<Boolean>` er en fagbetingelse. Den spores og vises i forklaringen. Det gjelder også
      `Verdi<Boolean>`, `Faktum<Boolean>` og `Regel`, siden alle er `Uttrykk<Boolean>`.
    - `Boolean` er en teknisk guard. Den spores ikke. Er den usann, treffer ikke regelen, og
      betingelsene etter den evalueres ikke.
- `HVIS` og `OG` er like. `OG` finnes for lesbarhetens skyld.
- En regel uten betingelser treffer alltid.
- En regel har enten `SÅ` eller `RETURNER`, ikke begge.

#### Regler som verdier

Fordi en `Regel` er et `Uttrykk<Boolean>`, kan den brukes som betingelse i andre regler. Det trengs ingen
egen `HVIS(regel)`. Dette erstatter `"regelA".harTruffet()` fra v1.

```kotlin
val afpMellom = regel("AFP-MELLOM") { ... }

regel("AFP-ORDINÆR") {
    HVIS { afpMellom erLik false }
    SÅ { ... }
}
```

### kjør og RETURNER

`kjør<T> { }` gir en blokk som returnerer en verdi av typen `T`. Inne i blokken kan regler bruke
`RETURNER { T }` i stedet for `SÅ`.

```kotlin
context(_: RegelKontekst)
fun beregn(beløp: Verdi<Int>, G: Verdi<Int>): Faktum<Int> = kjør {
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

context(k: RegelKontekst) fun regel(navn: String, definisjon: RegelBuilder.() -> Unit): Regel
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
    fun på(dato: LocalDate): Verdi<Int> = ...          // krasjer hvis det ikke finnes en sats for datoen
}

context(k: RegelKontekst)
fun grunnbeløp(dato: LocalDate): Verdi<Int> = k.ressurs<GrunnbeløpSatser>().på(dato)
```

- Tilgangsfunksjoner returnerer `Verdi`, slik at verdien er navngitt i forklaringen. En sats er ikke
  fastsatt av en regel, så den er ikke et `Faktum`.
- Ressurser skrives ikke defensivt. Mangler en ressurs eller en verdi i den, krasjer kallet med en
  melding som navngir ressursen.
- Det finnes ingen `NoOp`-varianter og ingen valgfrie ressurser.
- Rammeverket legger ikke til rette for noen bestemt ressurstype og varsler ikke ressurser om regler
  eller fakta.

### Sporing

Sporing er alltid på. Uttrykkstreet bygges uansett, fordi det er selve utregningen. Det er derfor lite å
spare på å slå sporing av, og det ville krevd en egen kodevei. Det finnes ingen `Tracer`, `NoOpTracer`,
`traced`-blokk eller `debugTree()`.

`Sporing` er en ressurs som holder regelen som eier den aktive `SÅ`- eller `RETURNER`-blokken.
`RegelKontekst` legger den alltid i kartet, og brukeren gjør det aldri.

```kotlin
class Sporing internal constructor(val regel: Regel?) : Ressurs
```

- `regel(...)` lager en `Regel` med betingelsene.
- `SÅ` og `RETURNER` kjører kroppen i en ny `RegelKontekst`. Kartet er det samme, bortsett fra at
  `Sporing` er byttet ut med `Sporing(regel = denne regelen)`. Den nye konteksten overstyrer den
  ytre, også gjennom vanlige funksjonskall. Det finnes ingen push/pop og ingen mutabel tilstand.
- `faktum(...)` lager et faktum med `uttrykk` og `regel = sporing.regel`. Det er altså den nærmeste
  regelen som slo til, også om faktumet lages i en funksjon kalt fra `SÅ`.
- Utenfor enhver regel er `sporing.regel` null. Hva `faktum(...)` skal gjøre da, henger sammen med
  det åpne spørsmålet om forutsetning.

### Regler som ikke treffer

Regler som ikke treffer, spores ikke. Dette er et grunnleggende valg for å holde kompleksiteten nede.

- Det finnes ingen logg eller graf over evaluerte regler, bare `Faktum`-grafen.
- En regel som ikke traff, er en `Regel` med verdi `false`. Den blir bare en del av en
  forklaring hvis en annen regel bruker den som betingelse, for eksempel `afpMellom erLik false`.
- En regel som ikke traff, har ingen `SÅ`- eller `RETURNER`-blokk som har kjørt. Den har derfor ingen
  fakta som peker på den.

### Forklaring

Forklaringen går bakover fra et resultat og nøster i én graf: `uttrykk`, `grunnlag()`, `regel` og
`betingelser`.

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
| Betingelser | `faktum.regel.betingelser` |
| Forutsetning | ikke avklart, se åpne spørsmål |

Hvert `Faktum` i `grunnlag()` kan forklares på samme måte. En ren `Verdi` er inndata og har ingen
forklaring utover navn og verdi.

## 6. Lag 3 – Orkestrering

Lag 3 er ikke avklart. Lag 2 alene dekker det v1 brukte regelflyter til, siden både regler og flyter i
praksis gjør kode betinget: beslutninger tas med `regel`, og resten er vanlig Kotlin.

Hvis laget innføres, gjelder disse føringene:

- En `Regeltjeneste` konstrueres med en `RegelKontekst`. Det er inngangen til rammeverket.
- Beslutninger i flyten er regler fra lag 2. Fakta som lages i `SÅ`, får beslutningen som regel
  automatisk. Hvordan regler under beslutningen knyttes til den, avhenger av det åpne spørsmålet om
  forutsetning.
- Laget inneholder ingen egen graf. Hvorfor noe kjørte, ligger i `Faktum.regel`.

Vurderinger:

- Uten et eget flytbegrep blir det trolig vanskeligere å generere flytdiagram.
- Hvis flyter er en god måte å organisere kode på, kan vi innføre en lettere variant av `Regelflyt`.
- Skal en forgrening kunne returnere en verdi av typen `T`, tilsvarende `kjør`?

## 7. Eksempel: slitertillegg

```kotlin
context(_: RegelKontekst)
fun beregnSlitertillegg(
    antallMnd: Verdi<Int>,      // 18
    trygdetid: Verdi<Int>,      // 30
    G: Verdi<Int>,              // 118620
): Faktum<Double> = kjør {
        val fullTrygdetid = Verdi("fullTrygdetid", 40)
        lateinit var fulltSlitertillegg: Faktum<Double>
        lateinit var justeringsFaktor: Faktum<Double>

        regel("FULLT-SLITERTILLEGG") {
            SÅ { fulltSlitertillegg = faktum("fulltSlitertillegg", avrund2(G * 0.25 / 12)) }
        }

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
   Det er uavklart hvordan referansen lagres i modellen (`Regel`) og vises i forklaringen.
4. **Lag 3 – orkestrering.** Se kapittel 6.
5. **Mekanismen bak `RETURNER`.** Planen er at `kjør` holder resultatet i en `lateinit var`. Dette må
   undersøkes nærmere:
    - Hvordan hindres de neste reglene i å evaluere når resultatet er satt? For eksempel ved at `regel`
      sjekker om `kjør` allerede har et resultat.
    - Vanlig Kotlin-kode mellom reglene kjører fortsatt etter et treff. Er det akseptabelt, eller må
      brukeren selv passe på det?
    - Hva skjer hvis `RETURNER` treffer i en nøstet regel inne i en `SÅ`-blokk?
6. **Forutsetning.** En regel som evalueres i en `SÅ`-blokk, er evaluert under regelen som eier
   blokken. Det er forutsetningen, og den gir Forutsetning-linjen i forklaringen. Hvordan den skal
   modelleres, er ikke avklart:
    - `Regel.forutsetning: Regel?` krever null for regler på øverste nivå.
    - En standardverdi `ALLTID` kan ikke selv være en `Regel`, fordi den da trenger en forutsetning
      under konstruksjon (verifisert: `NullPointerException` når klassen lastes).
    - `forutsetning: Verdi<Boolean> = ALLTID`, med `ALLTID` som en ren `Verdi<Boolean>`, unngår både
      null og sykel, men gir svakere typing. Nøsting krever `is Regel`.

   Valget avgjør også hva `faktum(...)` utenfor en regel skal gi.

## Vedlegg: Avklaringer fra v3 og DESIGN.md

| Tema | v3 | DESIGN.md | v4 |
|---|---|---|---|
| Sporing | `traced`-blokk, `Tracer`, `NoOpTracer`, `debugTree()`, regler som ikke traff vises | Alltid på, bare `Faktum`-grafen | DESIGN.md |
| RETURNER | `RETURNER { }` i regel | Bare `SÅ` + `lateinit var` | `kjør<T> { }` + `RETURNER { T }`, `lateinit var` bak kulissene, runtime-feil uten treff. Mekanismen er åpen. |
| Regelflyt | Utgår | Lag 3 med `Regeltjeneste`/`Regelflyt` | Åpent spørsmål |
| Forklaringsformat | HVA / HVORFOR / HVORDAN | Beregning / Betingelser / Forutsetning | DESIGN.md |
| Inndata | `Verdi`, `Grunnlag` | `Verdi(navn, verdi)` | `Verdi`. `Faktum` og `Regel` arver fra `Verdi`. |
| Kontekst | `RuleContext`, `ResourceAccessor`-extensions | `RegelKontekst`, `Ressurs`, context-funksjoner | DESIGN.md |
| Utledet faktum | `sporing(navn, uttrykk)` | `faktum(navn, uttrykk)` | DESIGN.md |
| HVIS / OG | `HVIS` teknisk, `OG` faglig | Typen bestemmer | Typen bestemmer |
| Regelkjeding | Vurdering | `regel(...)` returnerer `Regel` | `val regelA = regel(...)` |
| Forutsetning | – | Oppfølgingspunkt | Åpent spørsmål |

## Vedlegg: Mulige forbedringer

1. **Forenkle `erLik true`.** `afpMellom erLik true` vises i dag som «AFP-MELLOM (true) er lik true».
   En egen overload for `Boolean` kan returnere venstresiden direkte, slik at det vises som
   «AFP-MELLOM (true)»:
   ```kotlin
   infix fun Uttrykk<Boolean>.erLik(høyre: Boolean): Uttrykk<Boolean> =
       if (høyre) this else Infiks(this, "=", Konstant(høyre), ...)
   ```
    - Kotlin velger den mest spesifikke overloaden, så den generiske `erLik` for andre typer påvirkes
      ikke (verifisert).
    - Verdien og `grunnlag()` blir de samme. Bare uttrykkstreet blir kortere.
    - `erLik false` kan ikke forenkles på samme måte uten `ikke`, og vises fortsatt som
      «AFP-MELLOM (false) er lik false».
    - Alternativt kan forenklingen gjøres ved formatering i stedet for i modellen. Da beholder treet
      det brukeren skrev, og forenklingen blir en del av det åpne spørsmålet om formatering.
