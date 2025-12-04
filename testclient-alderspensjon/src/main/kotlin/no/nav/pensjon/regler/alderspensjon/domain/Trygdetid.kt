package no.nav.pensjon.regler.alderspensjon.domain

import no.nav.pensjon.regler.alderspensjon.domain.koder.UtfallType
import no.nav.system.ruledsl.core.rettsregel.Faktum

data class Trygdetid(
    var år: Int = 0,
    var faktiskTrygdetidIMåneder: Faktum<Long> = Faktum("faktisk trygdetid i måneder", 0),
    var firefemtedelskrav: Faktum<Long> = Faktum("firefemtedelskrav", 0),
    var redusertFremtidigTrygdetid: Faktum<UtfallType> = Faktum("Redusert fremtidig trygdetid",UtfallType.IKKE_RELEVANT),
    var tt_fa_F2021: Faktum<Int> = Faktum("Faktisk trygdetid før 2021", 0),
)