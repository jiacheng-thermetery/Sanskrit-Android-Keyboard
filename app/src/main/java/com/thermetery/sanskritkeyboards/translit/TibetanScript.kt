package com.thermetery.sanskritkeyboards.translit

/**
 * Tibetan script tables and the syllable stacking rules.
 *
 * Tibetan is not written as a flat sequence of letters. A syllable is a
 * vertical stack built around one root letter (ming gzhi):
 *
 *     [prefix] [superscript] ROOT [subscript…] [vowel] [suffix] [post-suffix]
 *      sngon    mgo can              'dogs can          rjes    yang 'jug
 *      'jug                                             'jug
 *
 * Only the stack is vertical — prefix, suffix and post-suffix sit beside it as
 * ordinary letters. In Unicode the *topmost* member of the stack carries the
 * base form and everything beneath it uses a subjoined form, so `rka` is
 * ra + subjoined ka (རྐ), not ka + something.
 *
 * Which letters may sit above or below which is fixed by orthography, not by
 * the typist, so those combinations are tabulated below.
 */
object TibetanScript {

    const val TSHEG = "་"        // ་  syllable separator
    const val SHAD = "།"         // །  phrase terminator
    const val DOUBLE_SHAD = "༎"  // ༎

    /** Root/base letter forms, keyed by Wylie. */
    val consonants: Map<String, String> = mapOf(
        "k" to "ཀ", "kh" to "ཁ", "g" to "ག", "ng" to "ང",
        "c" to "ཅ", "ch" to "ཆ", "j" to "ཇ", "ny" to "ཉ",
        "t" to "ཏ", "th" to "ཐ", "d" to "ད", "n" to "ན",
        "p" to "པ", "ph" to "ཕ", "b" to "བ", "m" to "མ",
        "ts" to "ཙ", "tsh" to "ཚ", "dz" to "ཛ", "w" to "ཝ",
        "zh" to "ཞ", "z" to "ཟ", "'" to "འ", "y" to "ཡ",
        "r" to "ར", "l" to "ལ", "sh" to "ཤ", "s" to "ས",
        "h" to "ཧ", "a" to "ཨ",
        // Retroflexes and aspirates used to write Sanskrit loanwords.
        "T" to "ཊ", "Th" to "ཋ", "D" to "ཌ", "N" to "ཎ",
        "Sh" to "ཥ", "kSh" to "ཀྵ",
    )

    /**
     * The btags key: arms the next consonant to come out subjoined, so that a
     * stack can be built by hand. It is a modifier, not a character — nothing
     * is inserted when it is pressed.
     */
    const val BTAGS = "྄"

    /** Base consonants occupy this block; their subjoined forms sit 0x50 above. */
    private val baseRange = 0x0F40..0x0F6C
    private const val SUBJOINED_OFFSET = 0x50

    /**
     * Subjoined forms sit exactly 0x50 above their base in Unicode, so they are
     * derived rather than tabulated — that keeps the two tables from drifting.
     */
    fun subjoined(wylie: String): String? {
        val base = consonants[wylie] ?: return null
        return subjoinedForm(base)
    }

    /** The subjoined form of an already-rendered base letter, or null. */
    fun subjoinedForm(base: String): String? {
        if (base.codePointCount(0, base.length) != 1) return null
        val cp = base.codePointAt(0)
        if (cp !in baseRange) return null
        return String(Character.toChars(cp + SUBJOINED_OFFSET))
    }

    /** Vowel signs. `a` is inherent in the consonant and marks nothing. */
    val vowels: Map<String, String> = mapOf(
        "a" to "",
        "i" to "ི",
        "u" to "ུ",
        "e" to "ེ",
        "o" to "ོ",
        // Long vowels, for Sanskrit loanwords.
        "A" to "ཱ",
        "I" to "ཱི",
        "U" to "ཱུ",
        "E" to "ཻ",
        "O" to "ཽ",
    )

    /** Letters that may stand before the stack. */
    val prefixes: Set<String> = setOf("g", "d", "b", "m", "'")

    /** Letters that may follow the stack. */
    val suffixes: Set<String> = setOf("g", "ng", "d", "n", "b", "m", "'", "r", "l", "s")

    /** Letters that may follow a suffix. */
    val postSuffixes: Set<String> = setOf("s", "d")

    /**
     * Superscripts and the roots each may cap — ra mgo, la mgo and sa mgo.
     * `lh` falls out of la mgo naturally, which is why `h` appears there.
     */
    val superscripts: Map<String, Set<String>> = mapOf(
        "r" to setOf("k", "g", "ng", "j", "ny", "t", "d", "n", "b", "m", "ts", "dz"),
        "l" to setOf("k", "g", "ng", "c", "j", "t", "d", "p", "b", "h"),
        "s" to setOf("k", "g", "ng", "ny", "t", "d", "n", "p", "b", "m", "ts"),
    )

    /** Subscripts and the roots each may hang from — ya/ra/la btags and wa zur. */
    val subscripts: Map<String, Set<String>> = mapOf(
        "y" to setOf("k", "kh", "g", "p", "ph", "b", "m"),
        "r" to setOf("k", "kh", "g", "t", "th", "d", "n", "p", "ph", "b", "m", "s", "h"),
        "l" to setOf("k", "g", "b", "r", "s", "z"),
        "w" to setOf(
            "k", "kh", "g", "c", "ny", "t", "d", "ts", "tsh",
            "zh", "z", "r", "l", "sh", "s", "h",
        ),
    )

    fun canSuperscribe(top: String, root: String): Boolean =
        superscripts[top]?.contains(root) == true

    fun canSubscribe(sub: String, root: String): Boolean =
        subscripts[sub]?.contains(root) == true
}

/** One parsed Tibetan syllable, ready to render. */
data class TibetanSyllable(
    val prefix: String? = null,
    val superscript: String? = null,
    val root: String,
    val subscripts: List<String> = emptyList(),
    val vowel: String? = null,
    val suffix: String? = null,
    val postSuffix: String? = null,
) {
    /**
     * Render to Unicode. The top of the stack takes a base form and everything
     * below it a subjoined form, so a superscript demotes the root.
     */
    fun render(): String {
        val out = StringBuilder()
        prefix?.let { out.append(TibetanScript.consonants[it] ?: it) }

        if (superscript != null) {
            out.append(TibetanScript.consonants[superscript] ?: superscript)
            out.append(TibetanScript.subjoined(root) ?: root)
        } else {
            out.append(TibetanScript.consonants[root] ?: root)
        }
        for (sub in subscripts) {
            out.append(TibetanScript.subjoined(sub) ?: sub)
        }

        vowel?.let { out.append(TibetanScript.vowels[it] ?: "") }
        suffix?.let { out.append(TibetanScript.consonants[it] ?: it) }
        postSuffix?.let { out.append(TibetanScript.consonants[it] ?: it) }
        return out.toString()
    }
}
