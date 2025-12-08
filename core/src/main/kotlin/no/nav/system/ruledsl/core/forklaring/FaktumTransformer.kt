package no.nav.system.ruledsl.core.forklaring

import no.nav.system.ruledsl.core.model.Faktum

/**
 * Generic transformer interface for converting Faktum explanations to different output types.
 *
 * The ARC tree contains the complete execution trace. Transformers walk this tree to produce
 * various output formats:
 * - String (text, HTML, markdown, XML)
 * - Structured data (JSON, sections, custom models)
 * - Other representations
 *
 * @param T The output type produced by this transformer
 */
interface FaktumTransformer<T> {
    /**
     * Transform a Faktum and its explanation context into the desired output type.
     *
     * @param faktum The Faktum to transform
     * @param filter Which ARC components to include in the transformation
     * @return The transformed representation
     */
    fun transform(faktum: Faktum<*>, filter: Filter): T
}
