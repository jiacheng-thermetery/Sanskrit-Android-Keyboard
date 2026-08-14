package com.thermetery.sanskritkeyboards.translit

/**
 * Live Velthuis → Devanāgarī transliterator.
 *
 * Greedy longest-match tokenization feeding the shared [DevanagariScheme]
 * syllable composer. Input is case-folded — Velthuis treats case as cosmetic,
 * and Devanāgarī has no case anyway.
 */
object VelthuisToDevanagari : DevanagariScheme() {

    override val independentVowels: Map<String, String> = mapOf(
        "a" to "अ", "aa" to "आ",
        "i" to "इ", "ii" to "ई",
        "u" to "उ", "uu" to "ऊ",
        ".r" to "ऋ", ".rr" to "ॠ",
        ".l" to "ऌ", ".ll" to "ॡ",
        "e" to "ए", "ai" to "ऐ",
        "o" to "ओ", "au" to "औ",
    )

    override val vowelSigns: Map<String, String> = mapOf(
        "a" to "", "aa" to "ा",
        "i" to "ि", "ii" to "ी",
        "u" to "ु", "uu" to "ू",
        ".r" to "ृ", ".rr" to "ॄ",
        ".l" to "ॢ", ".ll" to "ॣ",
        "e" to "े", "ai" to "ै",
        "o" to "ो", "au" to "ौ",
    )

    override val consonants: Map<String, String> = mapOf(
        "k" to "क", "kh" to "ख", "g" to "ग", "gh" to "घ", "\"n" to "ङ",
        "c" to "च", "ch" to "छ", "j" to "ज", "jh" to "झ", "~n" to "ञ",
        ".t" to "ट", ".th" to "ठ", ".d" to "ड", ".dh" to "ढ", ".n" to "ण",
        "t" to "त", "th" to "थ", "d" to "द", "dh" to "ध", "n" to "न",
        "p" to "प", "ph" to "फ", "b" to "ब", "bh" to "भ", "m" to "म",
        "y" to "य", "r" to "र", "l" to "ल", "v" to "व",
        "\"s" to "श", ".s" to "ष", "s" to "स", "h" to "ह",
    )

    override val modifiers: Map<String, String> = mapOf(
        ".m" to "ं",   // anusvāra
        ".h" to "ः",   // visarga
    )

    override fun acceptsIntoBuffer(input: String): Boolean = isVelthuisInput(input)

    override fun normalizeForBuffer(input: String): String = input.lowercase()
}
