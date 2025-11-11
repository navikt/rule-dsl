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

4. [????] Forklaring er nå basert på at rammeverket produserer String. Dette er greit mens vi utvikler mekansimene, men må byttes ut. Flere veier til mål men vurder:
a) rådata er AST (Abstract Syntax Tree) av Uttrykk, og Uttrykk må skille seg ut fra Forklaring. Forklaring må være et eget objekt som bruker av rammeverket selv kan bestemme. Vi leverer noen enkle forslag på Forklaring med diverse String utgaver. I pensjon-regler har vi ytterligere en AST til GuiModel transformasjon som gjør at vi kan vise forklaringer i GUI.
b) som a, men vi transformerer AST til et ForklaringsNode objekt som intermediary før man transformerer videre til hva nå man trenger.
c) noe annet?

5. Er vi tjent med å lage et Uttrykk til som kun har navn og en set-funksjon som tar i mot uttrykket og konverterer til Faktum? Det kan gjøre domene klassene i stand til å diktiere NAVN, men ikke verdien. Noe sånnt som "UinitsialisertFaktum". evaluer på denne kaster exception. Klassen kan kanskje også støtte akkumulering.

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