package no.nav.system.rule.dsl.rettsregel.helper

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.rettsregel.Faktum

fun Boolean.svarord() = if (this) "JA" else "NEI"

fun AbstractRuleComponent.isLeafFaktum(): Boolean =
    this is Faktum<*> && this.children.all { c -> c.children.isEmpty() }