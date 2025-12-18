package no.nav.system.ruledsl.core.helper

fun Boolean.jaNei() = if (this) "JA" else "NEI"

fun Boolean.checkmark() = if (this) "✓" else "✗"