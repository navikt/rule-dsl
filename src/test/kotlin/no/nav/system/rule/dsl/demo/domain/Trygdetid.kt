package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.rettsregel.Faktum

data class Trygdetid(
    var år: Int = 0,
    var faktiskTrygdetidIMåneder: Long = 0,
    var firefemtedelskrav: Long = 0,
    var redusertFremtidigTrygdetid: Boolean = false
)

data class TrygdetidSubSum(
    var år: Int = 0,
    var faktiskTrygdetidIMåneder: Faktum<Long> = Faktum("faktiskTrygdetidIMåneder", 0),
    var faktiskTrygdetidIMånederRettsregel: Rule<*>? = null,
    var firefemtedelskrav: Faktum<Long> = Faktum("firefemtedelskrav", 0),
    var redusertFremtidigTrygdetid: Boolean = false
)