package com.thermetery.sanskritkeyboards.translit

/**
 * Live Harvard-Kyoto → Devanāgarī transliterator.
 *
 * Greedy longest-match tokenization feeding the shared [DevanagariScheme]
 * syllable composer. Case is significant in HK, so the buffer is not folded.
 */
object HkToDevanagari : DevanagariScheme() {

    override val independentVowels: Map<String, String> = mapOf(
        "a" to "अ", "A" to "आ",
        "i" to "इ", "I" to "ई",
        "u" to "उ", "U" to "ऊ",
        "R" to "ऋ", "RR" to "ॠ",
        "lR" to "ऌ", "lRR" to "ॡ",
        "e" to "ए", "ai" to "ऐ",
        "o" to "ओ", "au" to "औ",
    )

    override val vowelSigns: Map<String, String> = mapOf(
        "a" to "", "A" to "ा",
        "i" to "ि", "I" to "ी",
        "u" to "ु", "U" to "ू",
        "R" to "ृ", "RR" to "ॄ",
        "lR" to "ॢ", "lRR" to "ॣ",
        "e" to "े", "ai" to "ै",
        "o" to "ो", "au" to "ौ",
    )

    override val consonants: Map<String, String> = mapOf(
        "k" to "क", "kh" to "ख", "g" to "ग", "gh" to "घ", "G" to "ङ",
        "c" to "च", "ch" to "छ", "j" to "ज", "jh" to "झ", "J" to "ञ",
        "T" to "ट", "Th" to "ठ", "D" to "ड", "Dh" to "ढ", "N" to "ण",
        "t" to "त", "th" to "थ", "d" to "द", "dh" to "ध", "n" to "न",
        "p" to "प", "ph" to "फ", "b" to "ब", "bh" to "भ", "m" to "म",
        "y" to "य", "r" to "र", "l" to "ल", "v" to "व",
        "z" to "श", "S" to "ष", "s" to "स", "h" to "ह",
    )

    override val modifiers: Map<String, String> = mapOf(
        "M" to "ं",   // anusvāra
        "H" to "ः",   // visarga
    )

    override fun acceptsIntoBuffer(input: String): Boolean =
        input.length == 1 && isAsciiLetter(input[0])
}
