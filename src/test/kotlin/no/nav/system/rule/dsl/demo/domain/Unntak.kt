package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum
import no.nav.system.rule.dsl.rettsregel.Fact

data class Unntak(val unntak: Fact<Boolean>, val unntakType: Fact<UnntakEnum>)