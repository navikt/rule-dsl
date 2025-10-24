1. Regelflyt må ha betingelser som tar i mot DomainPredicate. Dette muliggjør sporing av HVORFOR blokken til alle
   underliggende ARC.
 
2. FORDI sporingen algoritmen:
   Når faktum produseres og FORDI forklaringen skal lages: spørr root() om trace til this (ARC). Alle ARC på veien ned
   this THIS, må avgi forklaring.
3. Forklaring: HVA-forklaringen burde kun være navn og verdi. Formel/Uttrykk er Hvordan.
    Ta også stilling til om vi skal bruke FORDI eller HVORDAN.

4. Vurder om all sporing skal skrues av og på. Vi bør ikke levere ut en stor forklaring dersom vi kjører i
   ytelseskritisk batch kontekst. Bare spor ved forspørsel.

5. Kombiner Faktum og Formel. Dvs Faktum<T : Number> fungerer som Formel<Number>.
6. Uttrykk må få støtte for custom uttrykk som "min, "max", "avrund", "avrundMedToDesimal".
7. Uttrykk bør ha lazy eval på resultatet. Eller cachet resultat. 
8. Bør gjøre en overhaling av AbstractResourceAccessor. Denne blir nødvendig for å støtte trace mekanismen og burde integreres med AbstractRuleComponent slik at alle ARC har tilgang til root().

9. Neste "crux" er å få etablert en god måte å (hardanger-)sømløst produsere Faktum i SÅ delen av regel uten å måtte
   kalle opp sporingsmekanismer i konstruktøren av Faktum.

* Mulige løsninger: nå FAG()-metode som har nødvendig context i input: FAG(init: (fordi: ?) -> Unit). Slik at det det blir "lett" (men ikke helautomatisk) å levere et ferdig faktum.
Se faktum-trace-options.md.

  

