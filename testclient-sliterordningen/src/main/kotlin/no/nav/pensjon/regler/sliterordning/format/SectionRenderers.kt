package no.nav.pensjon.regler.sliterordning.format

/**
 * Renderer extensions for Section lists.
 *
 * Demonstrates separation of transformation (to sections) from presentation (rendering).
 * Transform once, render many ways.
 */

/**
 * Render sections as HTML with CSS classes for styling.
 */
fun List<Section>.renderAsHtml(): String = buildString {
    appendLine("<div class=\"explanation\">")
    this@renderAsHtml.forEach { section ->
        val indentPx = section.depth * 20
        val cssClass = section.type.name.lowercase()
        appendLine("  <div class=\"$cssClass\" style=\"margin-left: ${indentPx}px\">")
        appendLine("    ${escapeHtml(section.content)}")
        appendLine("  </div>")
    }
    appendLine("</div>")
}

/**
 * Render sections as Markdown with appropriate nesting.
 */
fun List<Section>.renderAsMarkdown(): String = buildString {
    var currentSection: SectionType? = null

    this@renderAsMarkdown.forEach { section ->
        val indent = "  ".repeat(section.depth)

        // Add section headers for HVA/HVORDAN/HVORFOR
        when (section.type) {
            SectionType.HVA, SectionType.HVORDAN, SectionType.HVORFOR -> {
                if (currentSection != section.type) {
                    appendLine()
                    appendLine("$indent## ${section.type}")
                    currentSection = section.type
                }
                appendLine("$indent- ${section.content}")
            }
            SectionType.RULE -> {
                appendLine()
                appendLine("$indent**Regel:** ${section.content}")
            }
            SectionType.PREDICATE -> {
                appendLine("$indent  - Predikat: ${section.content}")
            }
            SectionType.REFERENCE -> {
                appendLine("$indent  - Ref: ${section.content}")
            }
            SectionType.BRANCH_CONDITION -> {
                appendLine("$indent  - Branch: ${section.content}")
            }
        }
    }
}

/**
 * Render sections as plain text with indentation.
 */
fun List<Section>.renderAsText(): String = buildString {
    var lastType: SectionType? = null

    this@renderAsText.forEach { section ->
        val indent = "  ".repeat(section.depth)

        // Add blank line and header for section type changes (HVA/HVORDAN/HVORFOR)
        when (section.type) {
            SectionType.HVA, SectionType.HVORDAN, SectionType.HVORFOR -> {
                if (lastType != section.type) {
                    if (lastType != null) appendLine()
                    appendLine("$indent${section.type}:")
                    lastType = section.type
                }
                appendLine("$indent  ${section.content}")
            }
            SectionType.RULE -> {
                appendLine("$indent  regel: ${section.content}")
            }
            SectionType.PREDICATE -> {
                appendLine("$indent    predikat: ${section.content}")
            }
            SectionType.REFERENCE -> {
                appendLine("$indent    ${section.content}")
            }
            SectionType.BRANCH_CONDITION -> {
                appendLine("$indent    gren betingelse: ${section.content}")
            }
        }
    }
}

/**
 * Render sections as JSON.
 */
fun List<Section>.renderAsJson(): String = buildString {
    appendLine("[")
    this@renderAsJson.forEachIndexed { index, section ->
        val comma = if (index < this@renderAsJson.size - 1) "," else ""
        appendLine("  {")
        appendLine("    \"depth\": ${section.depth},")
        appendLine("    \"type\": \"${section.type}\",")
        appendLine("    \"content\": \"${escapeJson(section.content)}\"")
        appendLine("  }$comma")
    }
    appendLine("]")
}

// Helper functions
private fun escapeHtml(text: String): String {
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}

private fun escapeJson(text: String): String {
    return text
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}
