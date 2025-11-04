package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum
import no.nav.system.rule.dsl.rettsregel.Faktum

data class Unntak(var unntak: Faktum<Boolean>, val unntakType: Faktum<UnntakEnum>)