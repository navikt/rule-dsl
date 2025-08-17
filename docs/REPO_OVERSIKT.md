# Repository-oversikt: rule-dsl

Denne filen gir en kortfattet oversikt over repoet, hvordan det bygges, og hvor du finner de viktigste delene. Den er ment både som innføring og som oppslagsverk.

## Formål
rule-dsl er et Kotlin-basert rammeverk for å definere, kjøre og forklare regler via en liten, norsk DSL. Målet er å skille de funksjonelle reglene fra teknisk kode slik at domenereglene blir mer tilgjengelige.

## Nøkkelinfo
- Byggsystem: Maven (Jar-bibliotek)
- Språk/versjoner: Kotlin 2.1.0, Java 21, JUnit Jupiter 5.11.3
- Artefakt: no.nav.system:rule.dsl
- Nåværende versjon (pom.xml): 1.6.4
- Aktiv branch: feature/slitertillegg

## Katalogstruktur (utdrag)
- README.md – Hoveddokumentasjon og eksempler på DSL
- CLAUDE.md – Hurtigveiledning for kodeassistenter (kommandoer, arkitektur, DSL)
- pom.xml – Maven-oppsett (Kotlin, test, publisering)
- maven-settings.xml – Oppsett for publisering til GitHub Packages (bruker GITHUB_TOKEN)
- .github/workflows/bygg-og-pusher-image.yml – GitHub Actions-workflow for deploy
- src/test/kotlin/… – Demokode og tester som demonstrerer DSL og bruksområder
- src/main/doc/ – Bilder (logo, ikon, eksempel)
- target/ – Ferdigbygde artefakter og test-rapporter fra forrige bygg

Merk: Kildekoden til selve biblioteket forventes under src/main/kotlin, men finnes ikke i denne arbeidskopien. target/ inneholder derimot et ferdig bygget jar og sources-jar (f.eks. rule.dsl-1.6.0.jar). For videre utvikling bør kildekoden hentes (se «Manglende hovedkildekode»).

## Arkitektur og begreper
Kjernen består av trestruktur av «rule components» som arver fra AbstractRuleComponent. De viktigste konseptene (med forventet plassering) er:
- AbstractRuleService – Inngang til en regeltjeneste
- AbstractRuleflow – Flytlogikk med forgreninger (Decision/Branch)
- AbstractRuleset – Samling av relaterte regler
- Rule – En enkelt regel
- Predicate – Tekniske betingelser (f.eks. null-sjekker)
- Subsumtion/Faktum – Funksjonelle uttrykk og navngitte verdier som sammenlignes
- Pattern – Hjelp for regler over lister (oppretter én regel per element)

Se README.md og testkoden under src/test/kotlin for konkrete eksempler.

## DSL (norsk)
- regel("…") { … } – Definerer en regel
- HVIS { … } / OG { … } – Betingelser (teknisk/ funksjonell)
- SÅ { … } / ELLERS { … } – Tiltak/alternativ tiltak
- RETURNER(x) – Returnerer verdi fra et ruleset
- forgrening("…") { gren { betingelse { … }; flyt { … } } } – Flytstyring i ruleflow

Eksempler finnes i README.md og i tests: src/test/kotlin/no/nav/system/rule/dsl/demo/ruleset/*

## Bygg og kjøring
Typiske Maven-kommandoer:
- mvn compile – Kompilerer (forventer at src/main/kotlin finnes)
- mvn test – Kjører tester (krever kompilert hovedkode, eller tilgjengelige klasser på classpath)
- mvn package – Bygger jar
- mvn install – Legger i lokal maven-repo
- mvn -B --settings maven-settings.xml deploy – Publiserer til GitHub Packages (krever GITHUB_TOKEN)

Advarsel: mvn clean vil slette target/-innholdet. Siden hovedkildekoden mangler i denne arbeidskopien, vil et nytt bygg sannsynligvis feile til kildekoden er på plass.

## CI/CD
- Workflow: .github/workflows/bygg-og-pusher-image.yml
  - Trigges manuelt (workflow_dispatch)
  - Setter opp Java 21 og bruker maven-settings.xml
  - Publiserer til GitHub Packages med GITHUB_TOKEN

## Avhengigheter (fra pom.xml)
- org.jetbrains.kotlin:kotlin-stdlib: 2.1.0
- org.jetbrains.kotlin:kotlin-compiler: 2.1.0
- org.junit.jupiter:junit-jupiter (test): 5.11.3
- Maven plugins: kotlin-maven-plugin, maven-source-plugin, maven-surefire-plugin (3.5.2)

## Tester og demo
Under src/test/kotlin finnes domeneobjekter, rulesets, ruleflows og services for demo og tester, blant annet:
- demo/domain: Person, Trygdetid, Inntekt, m.m.
- demo/ruleset: Eksempler som BeregnFaktiskTrygdetidRS, PersonenErFlyktningRS, VilkårsprøvingSlitertillegg*
- demo/ruleflow: BeregnAlderspensjonFlyt
- demo/ruleservice: AbstractDemoRuleService, BeregnAlderspensjonService*
- demo/inspection: InspectionTest – demonstrerer debug(), xmlDebug(), trace(), find()

Tidligere testresultater ligger i target/surefire-reports/.

## Manglende hovedkildekode
I denne arbeidskopien mangler src/main/kotlin (selve bibliotekets implementasjon). Forslag for å få den på plass lokalt:
- Sjekk branch: git branch -vv (per nå: feature/slitertillegg). Kildekoden kan ligge på en annen branch (f.eks. main).
- Hent oppdateringer: git fetch --all og bytt eventuelt branch: git checkout main
- Sjekk om Git LFS brukes for kildefiler (ikke åpenbart her). Kjør git lfs ls-files for å verifisere dersom LFS er i bruk.
- Når src/main/kotlin er tilgjengelig, kan du kjøre mvn clean test for en «ren» byggesyklus.

## Publisering
Repoet er satt opp for publisering til GitHub Packages:
- DistributionManagement peker til https://maven.pkg.github.com/navikt/rule-dsl
- maven-settings.xml forventer GITHUB_TOKEN i miljøet

## Nyttige pekere
- README.md – Primær introduksjon med DSL-eksempler
- CLAUDE.md – Oppsummering av arkitektur, kommandoer og teststrategi
- target/ – Inneholder tidligere bygg (jar/sources-jar) og testrapporter

Har du spørsmål? Internt: Slack #pensjon-regler. Eksternt: Opprett issue på GitHub.
