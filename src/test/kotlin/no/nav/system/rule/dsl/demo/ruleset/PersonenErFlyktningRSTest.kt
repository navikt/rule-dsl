package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.treevisitor.visitor.debug
import org.junit.jupiter.api.Test

class PersonenErFlyktningRSTest {

    @Test
    fun testErIkkeFlyktning() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1980, 1, 1)),
            flyktning = Faktum("Flyktning", false),
        )

        val flyktningstatus = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel20", false),
            Faktum("Virkningstidspunkt", localDate(2020, 1, 1))
        ).test().get()

        println(flyktningstatus.debug())

//        Assertions.assertFalse(flyktningstatus)

    }
}