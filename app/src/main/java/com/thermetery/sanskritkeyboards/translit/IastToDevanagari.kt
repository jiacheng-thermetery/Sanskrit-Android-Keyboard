package com.thermetery.sanskritkeyboards.translit

import java.text.Normalizer

/**
 * Live IAST → Devanāgarī transliterator.
 *
 * Input alphabet is IAST with precomposed diacritic letters —
 * `ā ī ū ṛ ṝ ḷ ḹ ṅ ñ ṭ ḍ ṇ ś ṣ ṃ ḥ` — matching what the IAST popover keyboard
 * emits. The buffer is case-folded (Devanāgarī has no case).
 */
object IastToDevanagari : DevanagariScheme() {

    override val independentVowels: Map<String, String> = mapOf(
        "a" to "अ", "ā" to "आ",
        "i" to "इ", "ī" to "ई",
        "u" to "उ", "ū" to "ऊ",
        "ṛ" to "ऋ", "ṝ" to "ॠ",
        "ḷ" to "ऌ", "ḹ" to "ॡ",
        "e" to "ए", "ai" to "ऐ",
        "o" to "ओ", "au" to "औ",
    )

    override val vowelSigns: Map<String, String> = mapOf(
        "a" to "", "ā" to "ा",
        "i" to "ि", "ī" to "ी",
        "u" to "ु", "ū" to "ू",
        "ṛ" to "ृ", "ṝ" to "ॄ",
        "ḷ" to "ॢ", "ḹ" to "ॣ",
        "e" to "े", "ai" to "ै",
        "o" to "ो", "au" to "ौ",
    )

    override val consonants: Map<String, String> = mapOf(
        "k" to "क", "kh" to "ख", "g" to "ग", "gh" to "घ", "ṅ" to "ङ",
        "c" to "च", "ch" to "छ", "j" to "ज", "jh" to "झ", "ñ" to "ञ",
        "ṭ" to "ट", "ṭh" to "ठ", "ḍ" to "ड", "ḍh" to "ढ", "ṇ" to "ण",
        "t" to "त", "th" to "थ", "d" to "द", "dh" to "ध", "n" to "न",
        "p" to "प", "ph" to "फ", "b" to "ब", "bh" to "भ", "m" to "म",
        "y" to "य", "r" to "र", "l" to "ल", "v" to "व",
        "ś" to "श", "ṣ" to "ष", "s" to "स", "h" to "ह",
    )

    /**
     * Anusvāra and visarga. Both `ṃ` and `ṁ` are accepted as anusvāra — the
     * dot-above form is common in older scholarly transcriptions.
     */
    override val modifiers: Map<String, String> = mapOf(
        "ṃ" to "ं",
        "ṁ" to "ं",
        "ḥ" to "ः",
    )

    /**
     * Characters we treat as belonging to an IAST word — anything else
     * (space, punctuation, digits, return) commits the pending buffer.
     */
    private val iastLowerLetters: Set<Char> =
        ("abcdefghijklmnopqrstuvwxyz" + "āīūṛṝḷḹṅñṭḍṇśṣṃṁḥ").toSet()

    override fun acceptsIntoBuffer(input: String): Boolean {
        val s = normalizeForBuffer(input)
        return s.length == 1 && s[0] in iastLowerLetters
    }

    /**
     * Case-fold, and normalize to NFC so a decomposed `a` + combining macron
     * is treated as the single letter `ā` — matching how Swift's grapheme-wise
     * `Character` comparison behaves on the iOS side.
     */
    override fun normalizeForBuffer(input: String): String =
        Normalizer.normalize(input.lowercase(), Normalizer.Form.NFC)
}
