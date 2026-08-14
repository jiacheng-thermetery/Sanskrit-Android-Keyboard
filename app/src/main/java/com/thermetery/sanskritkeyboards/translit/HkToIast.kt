package com.thermetery.sanskritkeyboards.translit

/**
 * Live Harvard-Kyoto → IAST transliterator.
 *
 * Greedy longest-match substitution over the pending Latin buffer. Case is
 * significant in HK — capitals encode long vowels and retroflex/palatal
 * consonants — so the buffer is *not* case-folded.
 */
object HkToIast : Transliterator {

    /** Static input → output rules. Greedy: longer matches win. */
    private val rules: Map<String, String> = mapOf(
        // Long vowels
        "A" to "ā",
        "I" to "ī",
        "U" to "ū",
        // Vocalic r/l
        "R" to "ṛ",
        "RR" to "ṝ",
        "lR" to "ḷ",
        "lRR" to "ḹ",
        // Nasals
        "G" to "ṅ",
        "J" to "ñ",
        "N" to "ṇ",
        // Retroflex stops
        "T" to "ṭ",
        "D" to "ḍ",
        // Sibilants
        "z" to "ś",
        "S" to "ṣ",
        // Anusvāra / visarga
        "M" to "ṃ",
        "H" to "ḥ",
    )

    private val maxRuleLen: Int = rules.keys.maxOf { it.length }

    override fun transliterate(input: String): String =
        substituteGreedily(input, rules, maxRuleLen)

    /** Only single ASCII letters extend the buffer; anything else commits. */
    override fun acceptsIntoBuffer(input: String): Boolean =
        input.length == 1 && isAsciiLetter(input[0])
}
