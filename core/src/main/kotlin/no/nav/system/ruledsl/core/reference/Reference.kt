package no.nav.system.ruledsl.core.reference

import java.io.Serializable

/**
 * Reference to external documentation, legal sources, or other resources.
 * Can be attached to both AbstractRuleComponent (rules, rulesets) and Faktum (formulas).
 *
 * @property id Identifier for this reference (e.g., "FTL-20-18", "CONF-SLITER")
 * @property url URL to the referenced resource (e.g., lovdata.no, confluence)
 */
data class Reference(
    val id: String,
    val url: String
) : Serializable
