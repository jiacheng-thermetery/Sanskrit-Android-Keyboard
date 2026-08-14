package com.thermetery.sanskritkeyboards.translit

/** Devanāgarī virama (्) — the vowel-killer that forms conjuncts. */
internal const val VIRAMA = "्"

/**
 * The syllable composer shared by every Devanāgarī-output scheme.
 *
 * The iOS project repeats this algorithm once per keyboard (HK, IAST and
 * Velthuis each carry their own copy). Here it lives once and the schemes
 * supply only their token tables — the composition rules are identical:
 *
 *   - a bare consonant is held pending and shown with virama until a vowel
 *     arrives
 *   - a vowel attaches its dependent sign to the pending consonant
 *     (short `a` attaches nothing — it is the implicit vowel)
 *   - with no consonant pending, a vowel renders in its independent form
 *   - another consonant flushes the pending one with virama (conjunct)
 *   - anusvāra / visarga flush the pending consonant with its implicit `a`,
 *     then attach
 *   - end-of-buffer flushes any pending consonant with virama
 */
abstract class DevanagariScheme : Transliterator {

    /** Vowels in independent form, used when no consonant is pending. */
    protected abstract val independentVowels: Map<String, String>

    /**
     * Vowels as dependent signs, used after a consonant. Short `a` maps to the
     * empty string — Devanāgarī treats it as the consonant's implicit vowel.
     */
    protected abstract val vowelSigns: Map<String, String>

    protected abstract val consonants: Map<String, String>

    /** Anusvāra (ं) and visarga (ः). */
    protected abstract val modifiers: Map<String, String>

    private val allTokens: Set<String> by lazy {
        independentVowels.keys + consonants.keys + modifiers.keys
    }

    private val maxTokenLen: Int by lazy {
        allTokens.maxOfOrNull { it.length } ?: 0
    }

    override fun transliterate(input: String): String {
        val tokens = tokenize(input, allTokens, maxTokenLen)
        val out = StringBuilder()
        // The pending consonant is held as its already-rendered Devanāgarī
        // character, so flushing needs no second lookup.
        var pending: String? = null

        for (token in tokens) {
            val cons = consonants[token]
            val sign = vowelSigns[token]
            val mod = modifiers[token]
            when {
                cons != null -> {
                    pending?.let { out.append(it).append(VIRAMA) }
                    pending = cons
                }

                sign != null -> {
                    val prev = pending
                    if (prev != null) {
                        out.append(prev).append(sign)
                        pending = null
                    } else {
                        out.append(independentVowels[token] ?: token)
                    }
                }

                mod != null -> {
                    pending?.let {
                        out.append(it)   // implicit short-a, then attach modifier
                        pending = null
                    }
                    out.append(mod)
                }

                else -> {
                    // Unknown token (e.g. an embedded digit) — flush with virama
                    // and pass it through untouched.
                    pending?.let {
                        out.append(it).append(VIRAMA)
                        pending = null
                    }
                    out.append(token)
                }
            }
        }
        pending?.let { out.append(it).append(VIRAMA) }
        return out.toString()
    }
}
