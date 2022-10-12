package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.treevisitor.Rettsregel
import no.nav.system.rule.dsl.treevisitor.TomRettsregel

data class Trygdetid(
    var år: Int = 0,
    var faktiskTrygdetidIMåneder: Faktum<Long> = Faktum("faktisk trygdetid i måneder", 0),
    var firefemtedelskrav: Faktum<Long> = Faktum("firefemtedelskrav", 0),
    var redusertFremtidigTrygdetid: Rettsregel = TomRettsregel(),
    var tt_fa_F2021: Faktum<Int> = Faktum("Faktisk trygdetid før 2021", 0),
)