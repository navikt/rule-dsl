1. Regelflyt må ha betingelser som tar i mot DomainPredicate. Dette muliggjør sporing av HVORFOR blokken til alle
   underliggende ARC.
 
2. FORDI sporingen algoritmen:
   Når faktum produseres og FORDI forklaringen skal lages: spørr root() om trace til this (ARC). Alle ARC på veien ned
   this THIS, må avgi forklaring.
3. Forklaring: HVA-forklaringen burde kun være navn og verdi. Formel/Uttrykk er Hvordan.
    Ta også stilling til om vi skal bruke FORDI eller HVORDAN.

4. Vurder om all sporing skal skrues av og på. Vi bør ikke levere ut en stor forklaring dersom vi kjører i
   ytelseskritisk batch kontekst. Bare spor ved forspørsel. Og granulering: ikke spor betyr lag sporing men ikke lever den ut, eller hva? Er det dyrt å bygge sporingen underveis selv om vi ikke skal levere den? 

5. Kombiner Faktum og Formel. Dvs Faktum<T : Number> fungerer som Formel<Number>. Interface Verdi utgår. 
6. Uttrykk bør erstatte Formelrammeverket, men med følgende utvidelser: a) Uttrykk må få støtte for custom uttrykk som "min, "max", "avrund", "avrundMedToDesimal". Og kanskje b) Uttrykk bør ha lazy eval på resultatet. Eller cachet resultat. Rekursiv evaluering må ved enhver operasjon "høres dyrt ut".
8. DONE: Bør gjøre en overhaling av AbstractResourceAccessor. Denne blir nødvendig for å støtte trace mekanismen og burde integreres med AbstractRuleComponent slik at alle ARC har tilgang til root().

9. LØST?: Neste "crux" er å få etablert en god måte å (hardanger-)sømløst produsere Faktum i SÅ delen av regel uten å måtte
   kalle opp sporingsmekanismer i konstruktøren av Faktum.

* Mulige løsninger: nå FAG()-metode som har nødvendig context i input: FAG(init: (fordi: ?) -> Unit). Slik at det det blir "lett" (men ikke helautomatisk) å levere et ferdig faktum.
Se faktum-trace-options.md.

10. Dersom vi skal gjøre større endringer på hvordan regelsett og regler fungerer, vurder om vi kan løse de to store svakhetene med dagens løsning: 1) en not null sjekk i et predikat er opplyser ikke påfølgende predikater, eller SÅ-blokken. 2) regel-chaining er i dag gjort med string. Dette kunne vært bedre med val regelNavn = regel(..). 3) Dagens løsning har ingen muligheter til å skrive ut en "passivt" regelgraf (må ha data for å kjøre). Det hadde vært interessant å se om vi kunne fått ut en statisk tilstandstre av regler/flyter etc.





## 2025-01-18: Phase 1-3 Complete - Reference System, Tree Visualization, and Perspectives

**Phase 1: Reference Traceability System ✅**
- Created simple `Reference(id, url)` data class
- Added `references: List<Reference>` to both AbstractRuleComponent and Faktum
- Implemented fluent `.ref()` API (only on Faktum to enforce explicit naming)
- Removed deprecated `rvsId` parameter from Faktum
- Files: reference/Reference.kt, reference/ReferenceExtensions.kt, tests

**Phase 2: Tree Visualization ✅**
- Implemented UttrykksTreePrinter with box-drawing characters
- Deduplication tracking: first occurrence shows full subtree, subsequent show `[N]` reference
- Extension function `Uttrykk<*>.printTree()` for easy usage
- Files: inspections/UttrykksTreePrinter.kt, tests

**Phase 3: Siloed Architecture & Perspectives ✅**
- Established clean separation: Execution (ARC) → Tracing (ExecutionTrace) → Perspectives (viewing)
- ExecutionTrace already perfect (no changes needed!)
- Implemented 4 perspective functions as extensions on ExecutionTrace:
  - `toFullString()` - complete audit trail
  - `toFunctionalString()` - decisions only (uses pathForHvorfor())
  - `toUttrykksTree(faktum)` - integrates UttrykksTreePrinter from Phase 2
  - `toFaktumExplanation(faktum)` - uses existing forklar() method
- Extension function pattern enables custom perspectives in client repos
- Faktum.hvorfor already wired via sporing() methods (no changes needed!)
- Files: perspectives/Perspectives.kt, tests
- Kept forklar() (not deprecated) for gradual migration

**Key Architectural Achievement:**
- Clean siloed architecture with single ExecutionTrace
- Multiple perspectives from same execution data
- Backward compatible (all existing tests passing)
- Extensible (client repos can add custom perspectives)
- References from Phase 1 ready to be displayed in perspectives
- Tree visualization from Phase 2 integrated as UttrykksTreePerspective

**See:** src/main/doc/ProgressEvaluation.md for detailed documentation

DEL 2
1. [????] Akkumulering ble ikke like enkelt. Tidligere:

```kotlin
//Ny:
 svar.faktiskTrygdetidIMåneder = Faktum(svar.faktiskTrygdetidIMåneder.navn, svar.faktiskTrygdetidIMåneder.evaluer() + økning)
//Gammel:
 svar.faktiskTrygdetidIMåneder.value += økning

// Denne er Unit i kotlin, ellers hadde dette vært enkelt.
 operator fun <T : Number> Faktum<T>.plusAssign(value: T) : Unit 
```

2. [????] arc.faktum(...) i SÅ-blokken er ikke greit. Det er svært lett å blande Faktum konstruktøren med faktum funsjonen. Denne er nå kallt sporing(...) men synes fortsatt det er litt mangelfullt.

3. [DONE] Uklart om Const skal være internal eller ikke. Hvis vi vil ha den eksponert, kan vi gjeninnføre navngi funksjon som gjør Const til Faktum. Bør være avklart nå at Const forblir internal.

4. [DONE] Forklaring er nå basert på at rammeverket produserer String. Dette er greit mens vi utvikler mekansimene, men må byttes ut. Flere veier til mål men vurder:
a) rådata er AST (Abstract Syntax Tree) av Uttrykk, og Uttrykk må skille seg ut fra Forklaring. Forklaring må være et eget objekt som bruker av rammeverket selv kan bestemme. Vi leverer noen enkle forslag på Forklaring med diverse String utgaver. I pensjon-regler har vi ytterligere en AST til GuiModel transformasjon som gjør at vi kan vise forklaringer i GUI.
b) som a, men vi transformerer AST til et ForklaringsNode objekt som intermediary før man transformerer videre til hva nå man trenger.
c) noe annet?

5. Er vi tjent med å lage et Uttrykk til som kun har navn og en set-funksjon som tar i mot uttrykket og konverterer til Faktum? Det kan gjøre domene klassene i stand til å diktiere NAVN, men ikke verdien. Noe sånnt som "UinitsialisertFaktum". evaluer på denne kaster exception. Klassen kan kanskje også støtte akkumulering.
--> Finner ingen praktisk løsning på dette. Det endrer opp med klønete casting eller halvfabrikater av Faktum som det ikke er opplagt om er initialisert eller ikke. Det hele blir enklere om vi har utviklerpattern på å initialisere Faktum med NaN verdier for Number typer. Andre typer har ingen løsning. Vi får ta opp tema på nytt om dette blir et smertepunkt.

6. [DONE] DomainPredicate bør heller hete SporingsPredikat eller SporbartPredikat. Evt. TrackingPredicate/Trackable.

7. Dersom man benytter et Faktum<Boolean> i som regelpredikat må man nå si "erLik true". Tidligere kunne man bare sende inn Faktum<Boolean> uten sammenligning mot boolean. Hvis vi ønsker det tilbake kan vi vurdere å gjeninnføre fun OG(faktumFunction: () -> Faktum<Boolean>) {

```kotlin
    fun OG(faktumFunction: () -> Faktum<Boolean>) {
    predicateFunctionList.add {
        TrackablePredicate(
            uttrykk = faktumFunction.invoke() erLik true
        )
    }
}
```
Eller en annen løsning.


8. Burde også MathOperator ha en val evaluator -> Number, istedenfor å bruke when (operator) { add, sub, etc }? Kanskje gjør dette det lettere å lage custom operatorer som "min", "max", "avrund", "avrundMedToDesimal" etc ?

9. Caching må innføres i Uttrykk evalueringsmekanisme. Hver Uttrykk bør cache sitt evaluerte resultat slik at rekursive kall ikke påvirker ytelse.

10. Hadde vært kjekt å kunne negere Faktum<Boolean>. Dvs skrive "!innvilget" istedenfor "innvilget erLik false".
Hadde en forventning om at dette skulle føre til at Faktum("bool", true) ble det samme som !Faktum("bool", false), men fant ingen god løsning på dette. 

11. Dobbeltsjekk at det ikke er fjere steder med unødvendig dobbel funksjonswrapping: "{ ogFunksjon() }".

12. Reference -> Henvisning. .ref() -> .henvis

13. Det vil kunne oppstå tilsynelatende unødvendige HVORDAN-forklaringer når et faktum etableres på bakgrunn av et annet faktum uten noen transformasjon. F.eks: sporing("slitertillegg", fulltSlitertillegg) (ingen øvrige faktorer er i bruk).