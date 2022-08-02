# rule-dsl ![nav.no logo](src/main/doc/NavLogoRod.svg)<img height="20" src="src\main\doc\Kotlin_Icon.svg" width="30"/>

## Purpose
Provides a lightweight framework to create, run and explain rules. Any application that needs rules coded in a structured manner may use this.
The goal is to isolate functional rules from the technical code and thus make the rules more accessible to non technical personnel. 

## Documentation
### Components
A set of classes inheriting **[AbstractRuleComponent](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleComponent.kt)** make up the core of the framework:
* **[AbstractRuleService]()** Entrypoint for the service.
* **[AbstractRuleflow](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleflow.kt)** Organizes the flowlogic.
    * **[Branch](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleflow.kt#Decision.Branch)** DSL syntax conditions and flowlogic.
    * **[Decision](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleflow.kt#)** DSL syntax for a group of branches. 
* **[AbstractRuleset](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleset.kt)** Wraps a set of rules that relate to a single topic.
* **[Rule](src/main/kotlin/no/nav/system/rule/dsl/Rule.kt)** A single functional decision.
* **[Predicate](src/main/kotlin/no/nav/system/rule/dsl/Predicate.kt)** Wraps the boolean expression and subject text.

### Treestructure
All [AbstractRuleComponent](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleComponent.kt) are organized in a visitor-accepting tree using children and parent. By using this tree it is possible to track which context a common rulecomponent was executed in.
```kotlin
ruleservice: BeregnAlderspensjonService
  ruleflow: BeregnAlderspensjonFlyt
    ruleset: BeregnFaktiskTrygdetidRS
      rule: BeregnFaktiskTrygdetidRS.SettFireFemtedelskrav fired: true
      rule: BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid fired: false
        predicate: Virkningsdato i saken, 1990-05-01, er før 1991-01-01. fired: false
        predicate: Faktisk trygdetid, 224, er lavere enn fire-femtedelskravet (480). fired: true
```
See [VisitorTest](src/test/kotlin/no/nav/pensjon/regler/demo/teknisk/visitor/VisitorTest.kt) for complete example.

### Resource
[AbstractRuleComponents](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleComponent.kt) have a resourceMap containing [AbstractResource](src/main/kotlin/no/nav/system/rule/dsl/AbstractResource.kt) instantiated per service call. These objects typically contain resources like rates ("satser"), loggers and other global assets. See [AbstractDemoRuleService](src/test/kotlin/no/nav/system/rule/dsl/demo/ruleservice/AbstractDemoRuleService.kt) for demonstration.

### DSL
A kotlin "mini-DSL", inspired by Kotlins [type-safe builders](https://kotlinlang.org/docs/type-safe-builders.html), provides a simple syntax for creating rules and describing logic flow control in ruleflows. The _domain_ in the DSL is generic _ruledevelopment_ and not specific to NAV. 

In [AbstractRuleflow](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleflow.kt):
```kotlin
decision("Sivilstand gift?") {
    branch {
      /**
       *  A boolean condition governing the
       *  execution of the [flow] block
       */
        condition { parameter.input.person.erGift }
        flow {
            grunnpensjonSats = 0.90
        }
    }
    branch { } // second branch
}
```
A standard rule in [AbstractRuleset](src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleset.kt):
```kotlin
regel("RedusertTrygdetid") {
  HVIS { trygdetid < 40 }
  SÅ {
      netto = grunnbeløp * sats * trygdetid / 40.0
  }
}
```

A rule with domain text:
```kotlin
regel("RedusertTrygdetid") {
  HVIS("Trygdetiden er [lavere enn|lik] 40") {
    trygdetid < 40
  }
  SÅ {
    netto = grunnbeløp * sats * trygdetid / 40.0
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

<img src="src\main\doc\ruleflow example.png"/>

Repo: [pensjon-regler-editor](https://github.com/navikt/pensjon-regler-editor)

### Usage
Maven:
TODO
```xml
<dependency>
  <groupId>??</groupId>
  <artifactId>kotlin-compiler</artifactId>
  <version>${kotlin.version}</version>
</dependency>
```
Gradle:
TODO

## Contact
External: Raise issues on GitHub

Internal: On slack #pensjon-regler
