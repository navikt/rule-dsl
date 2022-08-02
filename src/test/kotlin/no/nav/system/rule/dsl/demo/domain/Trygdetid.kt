package no.nav.system.rule.dsl.demo.domain

data class Trygdetid(
    var år: Int = 0,
    var faktiskTrygdetidIMåneder: Long = 0,
    var firefemtedelskrav: Long = 0,
    var redusertFremtidigTrygdetid: Boolean = false
)