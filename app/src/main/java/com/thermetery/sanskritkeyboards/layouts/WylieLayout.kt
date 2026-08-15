package com.thermetery.sanskritkeyboards.layouts

import com.thermetery.sanskritkeyboards.core.KeyDefinition
import com.thermetery.sanskritkeyboards.core.KeyboardLayout
import com.thermetery.sanskritkeyboards.core.KeyboardMode

/**
 * QWERTY layout for Wylie input. Plain Latin letters — the Tibetan is produced
 * by the transliterator, so `bkra` becomes བཀྲ as you type.
 *
 * Two departures from the Sanskrit QWERTY layouts:
 *
 *  - The apostrophe sits on the home row rather than in the symbols layer.
 *    In Wylie it is a-chung, a letter (`'brug` → འབྲུག), not punctuation.
 *  - The space bar inserts a tsheg (་). Tibetan separates syllables with a
 *    tsheg, and in Wylie you separate them with a space, so the two line up.
 *    Hold it for a literal space.
 */
object WylieLayout : KeyboardLayout {

    override fun layout(mode: KeyboardMode, shifted: Boolean): List<List<KeyDefinition>> =
        when (mode) {
            KeyboardMode.LETTERS -> if (shifted) lettersUpper else lettersLower
            KeyboardMode.NUMBERS -> numbers
        }

    private val lettersLower: List<List<KeyDefinition>> = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").map { ch(it) },
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").map { ch(it) } + listOf(ch("'")),
        listOf(shiftKey) + listOf("z", "x", "c", "v", "b", "n", "m").map { ch(it) } +
            listOf(backspaceKey),
        wylieBottomRow("༡༢༣"),
    )

    /**
     * Capitals matter in Wylie: `T Th D N Sh` are the Sanskrit retroflexes,
     * distinct from `t th d n sh`.
     */
    private val lettersUpper: List<List<KeyDefinition>> = listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P").map { ch(it) },
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L").map { ch(it) } + listOf(ch("'")),
        listOf(shiftKey) + listOf("Z", "X", "C", "V", "B", "N", "M").map { ch(it) } +
            listOf(backspaceKey),
        wylieBottomRow("༡༢༣"),
    )

    private val numbers: List<List<KeyDefinition>> = listOf(
        // Tibetan digits, with the Arabic ones on long-press.
        listOf(
            ch("༡", listOf("1")), ch("༢", listOf("2")), ch("༣", listOf("3")),
            ch("༤", listOf("4")), ch("༥", listOf("5")), ch("༦", listOf("6")),
            ch("༧", listOf("7")), ch("༨", listOf("8")), ch("༩", listOf("9")),
            ch("༠", listOf("0")),
        ),
        listOf(
            ch("།", listOf("༎")),          // shad / double shad
            ch("༄", listOf("༅", "༆")),   // head marks
            ch("༔"),
            ch("༼"), ch("༽"),
            ch("-"), ch("/"), ch("("), ch(")"), ch(":"),
        ),
        listOf(
            ch("་"),   // tsheg, also on the space bar
            ch("."), ch(","), ch("?"), ch("!"), ch("\""), ch("+"),
            backspaceKey,
        ),
        wylieBottomRow("ABC"),
    )
}

/** Space inserts a tsheg; long-press gives a literal space. */
internal fun wylieBottomRow(modeLabel: String): List<KeyDefinition> = bottomRow(
    modeLabel = modeLabel,
    spacePrimary = "་",
    spaceLabel = "་",
    spaceAlternates = listOf("་", " "),
)
