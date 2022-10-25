package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.enums.UtfallType
import no.nav.system.rule.dsl.rettsregel.Faktum

data class Trygdetid(
    var år: Int = 0,
    var faktiskTrygdetidIMåneder: Faktum<Long> = Faktum("faktisk trygdetid i måneder", 0),
    var firefemtedelskrav: Faktum<Long> = Faktum("firefemtedelskrav", 0),
    var redusertFremtidigTrygdetid: Faktum<UtfallType> = Faktum("Redusert fremtidig trygdetid"),
    var tt_fa_F2021: Faktum<Int> = Faktum("Faktisk trygdetid før 2021", 0),
)