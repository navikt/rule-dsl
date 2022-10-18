package no.nav.system.rule.dsl.demo.domain

import EmptyRule
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.TomtUtfall
import no.nav.system.rule.dsl.Utfall
import no.nav.system.rule.dsl.rettsregel.Faktum

data class Trygdetid(
    var år: Int = 0,
    var faktiskTrygdetidIMåneder: Faktum<Long> = Faktum("faktisk trygdetid i måneder", 0),
    var firefemtedelskrav: Faktum<Long> = Faktum("firefemtedelskrav", 0),
    var redusertFremtidigTrygdetid: Utfall = TomtUtfall(),
    var tt_fa_F2021: Faktum<Int> = Faktum("Faktisk trygdetid før 2021", 0),
)