package com.thermetery.sanskritkeyboards.translit

/**
 * Live Velthuis → IAST transliterator.
 *
 * Greedy longest-match substitution over a case-folded Latin buffer. Velthuis
 * treats case as cosmetic — the encoding lives in the prefix characters
 * `.` `"` `~`, not in capitals.
 */
object VelthuisToIast : Transliterator {

    /**
     * Velthuis → IAST substitution rules. Everything not listed passes
     * through unchanged.
     */
    private val rules: Map<String, String> = mapOf(
        // Long vowels
        "aa" to "ā",
        "ii" to "ī",
        "uu" to "ū",
        // Vocalic r/l
        ".r" to "ṛ",
        ".rr" to "ṝ",
        ".l" to "ḷ",
        ".ll" to "ḹ",
        // Nasals
        "\"n" to "ṅ",
        "~n" to "ñ",
        ".n" to "ṇ",
        // Retroflex stops
        ".t" to "ṭ",
        ".d" to "ḍ",
        // Sibilants
        "\"s" to "ś",
        ".s" to "ṣ",
        // Anusvāra / visarga
        ".m" to "ṃ",
        ".h" to "ḥ",
    )

    private val maxRuleLen: Int = rules.keys.maxOf { it.length }

    override fun transliterate(input: String): String =
        substituteGreedily(input, rules, maxRuleLen)

    override fun acceptsIntoBuffer(input: String): Boolean = isVelthuisInput(input)

    override fun normalizeForBuffer(input: String): String = input.lowercase()
}

/**
 * Whether the entire input belongs to the buffer. Velthuis-extending
 * characters are ASCII letters plus the three prefix marks `.` `"` `~`.
 * Anything else (space, digit, return, other punctuation) commits.
 *
 * Multi-character inputs are allowed — long-press popovers commit whole
 * Velthuis bigrams like `.r` or `~n` as a single insert.
 */
internal fun isVelthuisInput(s: String): Boolean {
    if (s.isEmpty()) return false
    for (c in s) {
        val isPrefix = c == '.' || c == '"' || c == '~'
        if (!isAsciiLetter(c) && !isPrefix) return false
    }
    return true
}
