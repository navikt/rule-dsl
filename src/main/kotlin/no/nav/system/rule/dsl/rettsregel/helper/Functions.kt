package no.nav.system.rule.dsl.rettsregel.helper

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.rettsregel.PairSubsumtion

fun Boolean.svarord() = if (this) "JA" else "NEI"

fun AbstractRuleComponent.isLeafPairSubsumtion(): Boolean = this is PairSubsumtion && this.children.all { c -> c.children.isEmpty() }