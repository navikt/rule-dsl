package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.demo.domain.koder.UtfallType
import no.nav.system.rule.dsl.rettsregel.Fact

data class Trygdetid(
    var år: Int = 0,
    var faktiskTrygdetidIMåneder: Fact<Long> = Fact("faktisk trygdetid i måneder", 0),
    var firefemtedelskrav: Fact<Long> = Fact("firefemtedelskrav", 0),
    var redusertFremtidigTrygdetid: Fact<UtfallType> = Fact("Redusert fremtidig trygdetid"),
    var tt_fa_F2021: Fact<Int> = Fact("Faktisk trygdetid før 2021", 0),
)