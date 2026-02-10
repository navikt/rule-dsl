package no.nav.pensjon.regler.alderspensjon.domain

import no.nav.pensjon.regler.alderspensjon.domain.koder.UtfallType
import no.nav.system.ruledsl.core.expression.Verdi

data class Trygdetid(
    var år: Int = 0,
    var faktiskTrygdetidIMåneder: Verdi<Long> = Verdi("faktisk trygdetid i måneder", 0L),
    var firefemtedelskrav: Verdi<Long> = Verdi("firefemtedelskrav", 0L),
    var redusertFremtidigTrygdetid: Verdi<UtfallType> = Verdi("Redusert fremtidig trygdetid", UtfallType.IKKE_RELEVANT),
    var tt_fa_F2021: Verdi<Int> = Verdi("Faktisk trygdetid før 2021", 0),
)
