package com.thermetery.sanskritkeyboards.layouts

import com.thermetery.sanskritkeyboards.core.KeyDefinition
import com.thermetery.sanskritkeyboards.core.KeyKind
import com.thermetery.sanskritkeyboards.core.KeyboardLayout
import com.thermetery.sanskritkeyboards.core.KeyboardMode
import com.thermetery.sanskritkeyboards.translit.TibetanScript

/**
 * Direct Tibetan layout, in the traditional alphabetical order of the thirty
 * consonants (ka kha ga nga / ca cha ja nya / …) — the order they are taught
 * and recited in, so it is the one a reader can find keys in without hunting.
 *
 * Long-pressing any consonant gives its **subjoined** form, which is how you
 * build a stack: tap ས, then hold ཀ and pick ྐ to get སྐ. The subjoined
 * alternates are derived from the base letters rather than listed, so the two
 * can never disagree.
 *
 * The space bar inserts a tsheg (་) because that, not a space, is what
 * separates Tibetan syllables. Hold it for a literal space.
 */
object TibetanLayout : KeyboardLayout {

    override fun layout(mode: KeyboardMode, shifted: Boolean): List<List<KeyDefinition>> =
        when (mode) {
            KeyboardMode.LETTERS -> if (shifted) lettersUpper else lettersLower
            KeyboardMode.NUMBERS -> numbers
        }

    /** A consonant key carrying its own subjoined form as the long-press. */
    private fun cons(wylie: String): KeyDefinition {
        val base = TibetanScript.consonants[wylie] ?: wylie
        val sub = TibetanScript.subjoined(wylie)
        return ch(base, if (sub != null) listOf(sub) else emptyList())
    }

    private val lettersLower: List<List<KeyDefinition>> = listOf(
        // ka kha ga nga / ca cha ja nya
        listOf("k", "kh", "g", "ng", "c", "ch", "j", "ny", "t", "th").map { cons(it) },
        // ta tha da na / pa pha ba ma / tsa tsha dza wa
        listOf("d", "n", "p", "ph", "b", "m", "ts", "tsh", "dz", "w").map { cons(it) },
        // zha za 'a ya / ra la sha sa / ha a
        listOf("zh", "z", "'", "y", "r", "l", "sh", "s", "h", "a").map { cons(it) },
        listOf(
            shiftKey,
            ch("ི"),   // i
            ch("ུ"),   // u
            ch("ེ"),   // e
            ch("ོ"),   // o
            ch("ཱ"),   // long a, for Sanskrit loanwords
            ch("ཾ"),   // anusvara
            ch("ཿ"),   // visarga
            backspaceKey,
        ),
        tibetanBottomRow("༡༢༣"),
    )

    /**
     * The shifted layer carries the letters used to write Sanskrit in Tibetan
     * script — the retroflexes and voiced aspirates that Tibetan itself has no
     * use for — plus the fixed-form and head marks.
     */
    private val lettersUpper: List<List<KeyDefinition>> = listOf(
        listOf(
            ch("ཊ", listOf("ྚ")),   // Ta
            ch("ཋ", listOf("ྛ")),   // Tha
            ch("ཌ", listOf("ྜ")),   // Da
            ch("ཎ", listOf("ྞ")),   // Na
            ch("ཥ", listOf("ྵ")),   // Sha
            ch("ྐྵ"),                    // ksha
            ch("ཛྷ"),                    // dzha
            ch("གྷ"),                    // gha
            ch("དྷ"),                    // dha
            ch("བྷ"),                    // bha
        ),
        listOf(
            ch("ཀྵ"),
            ch("ཪ"),   // fixed-form ra
            ch("ཬ"),   // fixed-form ra (subjoined context)
            ch("ྰ"),
            ch("ྵ"),
            ch("ྶ"),
            ch("ྷ"),
            ch("ྸ"),
            ch("ཱྀ"),
            ch("ྀ"),
        ),
        listOf(
            ch("ཻ"),   // ai
            ch("ཽ"),   // au
            ch("ྲྀ"),
            ch("ླྀ"),
            ch("ྃ"),   // nada
            ch("ྂ"),
            ch("༵"),
            ch("༷"),
            ch("༹"),
            ch("༾"),
        ),
        listOf(
            shiftKey,
            ch("ི"), ch("ུ"), ch("ེ"), ch("ོ"),
            ch("ཱ"), ch("ཾ"), ch("ཿ"),
            backspaceKey,
        ),
        tibetanBottomRow("༡༢༣"),
    )

    private val numbers: List<List<KeyDefinition>> = listOf(
        // Tibetan digits, with the Arabic equivalents on long-press.
        listOf(
            ch("༡", listOf("1")), ch("༢", listOf("2")), ch("༣", listOf("3")),
            ch("༤", listOf("4")), ch("༥", listOf("5")), ch("༦", listOf("6")),
            ch("༧", listOf("7")), ch("༨", listOf("8")), ch("༩", listOf("9")),
            ch("༠", listOf("0")),
        ),
        listOf(
            ch("།", listOf("༎")),   // shad, double shad
            ch("༎"),
            ch("༄", listOf("༅", "༆")),   // yig mgo head marks
            ch("༔"),
            ch("༼"), ch("༽"),
            ch("-"), ch("/"), ch("("), ch(")"),
        ),
        listOf(
            ch("་"),   // tsheg, also available on the space bar
            ch("."), ch(","), ch("?"), ch("!"),
            ch("\""), ch("'"),
            backspaceKey,
        ),
        tibetanBottomRow("ཨ་བ"),
    )
}

/**
 * A dedicated tsheg key sits between the space bar and return, matching the
 * reference layout — on a direct Tibetan keyboard the tsheg is a letter you
 * reach for constantly, and the space bar stays a space.
 */
internal fun tibetanBottomRow(modeLabel: String): List<KeyDefinition> = listOf(
    KeyDefinition(
        kind = KeyKind.MODE_SWITCH, primary = modeLabel,
        widthUnits = 1.5f, displayLabel = modeLabel
    ),
    KeyDefinition(
        kind = KeyKind.NEXT_KEYBOARD, primary = "globe",
        widthUnits = 1.0f, displayLabel = "🌐"
    ),
    KeyDefinition(
        kind = KeyKind.SPACE, primary = " ",
        widthUnits = 4.0f, displayLabel = "space"
    ),
    // tsheg, with shad and double shad on long-press
    ch("་", listOf("་", "།", "༎")).copy(widthUnits = 1.0f),
    KeyDefinition(
        kind = KeyKind.RETURN, primary = "\n",
        widthUnits = 2.5f, displayLabel = "return"
    ),
)
