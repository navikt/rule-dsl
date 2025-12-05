package no.nav.pensjon.regler.sliterordning.format

/**
 * Structured representation of an explanation section.
 *
 * Sections represent the hierarchical structure of an explanation,
 * separating the data model from the presentation.
 */
data class Section(
    val depth: Int,
    val type: SectionType,
    val content: String
)

/**
 * Types of explanation sections.
 */
enum class SectionType {
    /** What - the value being explained */
    HVA,

    /** How - the formula/calculation */
    HVORDAN,

    /** Why - the decision context */
    HVORFOR,

    /** A rule that was evaluated */
    RULE,

    /** A predicate within a rule */
    PREDICATE,

    /** A reference to external documentation */
    REFERENCE,

    /** A branch condition from the ruleflow */
    BRANCH_CONDITION
}
