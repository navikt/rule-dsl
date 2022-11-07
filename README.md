# rule-dsl ![nav.no logo](src/main/doc/NavLogoRod.svg)<img height="20" src="src\main\doc\Kotlin_Icon.svg" width="30"/>

## Purpose
Provides a lightweight framework to create, run and explain rules. Any application that needs rules coded in a structured manner may use this.
The goal is to isolate functional rules from the technical code and thus make the rules more accessible to non-technical personnel.

Inspired by [dp-quiz](https://github.com/navikt/dp-quiz).

## Documentation
### Components
A set of classes inheriting **[AbstractRuleComponent](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleComponent.kt)** make up the core of the framework:
* **[AbstractRuleService](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleset.kt)** Entrypoint for the service.
* **[AbstractRuleflow](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleflow.kt)** Organizes the flowlogic.
    * **[Branch](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleflow.kt)** DSL syntax conditions and flowlogic.
    * **[Decision](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleflow.kt)** DSL syntax for a group of branches. 
* **[AbstractRuleset](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleset.kt)** Wraps a set of rules that relate to a single topic.
* **[Rule](src/main/kotlin/no/nav/system/rule/dsl/Rule.kt)** A single functional decision.
* **[Fact](src/main/kotlin/no/nav/system/rule/dsl/rettsregel/Fact.kt)** A name-value pair used by Subsumsjons.
* **[Predicate](src/main/kotlin/no/nav/system/rule/dsl/Predicate.kt)** Wraps the boolean expression function for technical expressions (null checks and similar).
    * **[AbstractSubsumtion](src/main/kotlin/no/nav/system/rule/dsl/rettsregel/Subsumtion.kt)** Base class for functional expressions.
      * [PairSubsumsjon](src/main/kotlin/no/nav/system/rule/dsl/rettsregel/Subsumtion.kt) Compares two [Fact](src/main/kotlin/no/nav/system/rule/dsl/rettsregel/Fact.kt)s using [PairComparator](src/main/kotlin/no/nav/system/rule/dsl/enums/Comparator.kt).
      * [ListSubsumtion](src/main/kotlin/no/nav/system/rule/dsl/rettsregel/Subsumtion.kt) Compares a [Fact](src/main/kotlin/no/nav/system/rule/dsl/rettsregel/Fact.kt)'s relationship to a list of [AbstractRuleComponent](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleComponent.kt).

### Treestructure
All [AbstractRuleComponent](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleComponent.kt) are organized in a visitor-accepting tree using children and parent. By using this tree it is possible to track which context a common rulecomponent was executed in.
```kotlin
regeltjeneste: BeregnAlderspensjonService
  regelflyt: BeregnAlderspensjonFlyt
    regelsett: BeregnFaktiskTrygdetidRS
      regel: JA BeregnFaktiskTrygdetidRS.SettFireFemtedelskrav
      regel: NEI BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid
        NEI 'virkningstidspunkt' (1990-05-01) må være etter eller lik '1991-01-01'
        JA 'faktisk trygdetid i måneder' (224) er mindre enn 'firefemtedelskrav' (480)
```
See [VisitorTest](src/test/kotlin/no/nav/system/rule/dsl/demo/visitor/VisitorTest.kt) for complete example.

### Resource
[AbstractRuleComponents](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleComponent.kt) have a resourceMap containing [AbstractResource](src/main/kotlin/no/nav/system/rule/dsl/AbstractResource.kt) instantiated per service call. These objects typically contain resources like rates ("satser"), loggers and other global assets. See [AbstractDemoRuleService](src/test/kotlin/no/nav/system/rule/dsl/demo/ruleservice/AbstractDemoRuleService.kt) for demonstration.

### DSL
A kotlin "mini-DSL", inspired by Kotlins [type-safe builders](https://kotlinlang.org/docs/type-safe-builders.html), provides a simple syntax for creating rules and describing logic flow control in ruleflows. The _domain_ in the DSL is generic _ruledevelopment_ and not specific to NAV.

All DSL syntax is in norwegian.

In [AbstractRuleflow](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleflow.kt):
```kotlin
forgrening("Sivilstand gift?") {
    gren {
      /**
       *  Et boolsk uttrykk betinger eksekveringen av påfølgende flyt-blokk.
       */
        betingelse { parameter.input.person.erGift }
        flyt {
            grunnpensjonSats = 0.90
        }
    }
    gren { } // andre gren
}
```
A standard rule in [AbstractRuleset](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleset.kt) with technical predicate and functional Subsumtion. A Functional Subsumtion is any expression that produces an object of type [AbstractSubsumsjon](src/main/kotlin/no/nav/system/rule/dsl/rettsregel/Subsumtion.kt). See custom [Operators](src/main/kotlin/no/nav/system/rule/dsl/rettsregel/Operators.kt).
```kotlin
regel("RedusertTrygdetid") {
  HVIS { trygdetid != null }         // technical predicate
  OG { trygdetid erMindreEnn 40 }    // functional subsumtion
  SÅ {
      netto = grunnbeløp * sats * trygdetid / 40.0
  }
}
```

A rule with else-statement and a value return:
```kotlin
regel("Trygdetid") {
  HVIS { anvendtFlyktning erLik OPPFYLT }
  SÅ {                  // action-statement runs if all predicates are true
    RETURNER( 40 )      // returns a value and stops further evaluation of the ruleset.
  }
  ELLERS {              // else-statement runs if one or more predicates are false
    RETURNER( faktiskTrygdetid )
  }
}
```

### Pattern
Optional feature for writing rules on lists. A [Pattern](src/main/kotlin/no/nav/system/rule/dsl/pattern/Pattern.kt) object wraps a List and ensures a Rule instance is created for each item in the list.
```kotlin
val norskeBoperioder = boperiodeListe.createPattern { it.land == LandEnum.NOR }

regel("BoPeriodeStartFør16år", norskeBoperioder) { boperiode ->
  HVIS { boperiode.fom < dato16år }
  SÅ {
    svar.faktiskTrygdetidIMåneder += ChronoUnit.MONTHS.between(dato16år, boperiode.tom)
  }
}
```

### Visualization
A rudimentary plugin for Intellij is in development for ruleflow visualization.

<!--suppress HtmlUnknownTarget -->
<img src="src\main\doc\ruleflow example.png" alt=""/>

Repo: [pensjon-regler-editor](https://github.com/navikt/pensjon-regler-editor)

### Usage
Maven:
```xml
<dependency>
  <groupId>no.nav.system</groupId>
  <artifactId>rule.dsl</artifactId>
  <version>1.4.2</version>
</dependency>
```

## Contact
External: Raise issues on GitHub

Internal: On slack [#pensjon-regler](https://nav-it.slack.com/archives/CDWRP7S4B)
