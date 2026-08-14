package com.thermetery.sanskritkeyboards.layouts

import com.thermetery.sanskritkeyboards.core.KeyDefinition
import com.thermetery.sanskritkeyboards.core.KeyboardLayout
import com.thermetery.sanskritkeyboards.core.KeyboardMode

/**
 * QWERTY layout shared by Velthuis → IAST and Velthuis → Devanāgarī.
 *
 * Long-press alternates expose the Velthuis bigrams (e.g. `.r` `.rr`, `~n`,
 * `"s`) as one-tap inserts. You can also type the prefix characters
 * `.` `"` `~` manually from the 123 layer — the transliterator does greedy
 * longest-match either way.
 */
object VelthuisLayout : KeyboardLayout {

    override fun layout(mode: KeyboardMode, shifted: Boolean): List<List<KeyDefinition>> =
        when (mode) {
            KeyboardMode.LETTERS -> if (shifted) lettersUpper else lettersLower
            KeyboardMode.NUMBERS -> numbers
        }

    // MARK: - Letters (lowercase)

    private val lettersLower: List<List<KeyDefinition>> = listOf(
        listOf(
            ch("q"),
            ch("w"),
            ch("e"),
            ch("r", listOf(".r", ".rr")),
            ch("t", listOf(".t")),
            ch("y"),
            ch("u", listOf("uu")),
            ch("i", listOf("ii")),
            ch("o"),
            ch("p"),
        ),
        listOf(
            ch("a", listOf("aa")),
            ch("s", listOf(".s", "\"s")),
            ch("d", listOf(".d")),
            ch("f"),
            ch("g"),
            ch("h", listOf(".h")),
            ch("j"),
            ch("k"),
            ch("l", listOf(".l", ".ll")),
        ),
        listOf(
            shiftKey,
            ch("z"),
            ch("x"),
            ch("c"),
            ch("v"),
            ch("b"),
            ch("n", listOf(".n", "~n", "\"n")),
            ch("m", listOf(".m")),
            backspaceKey,
        ),
        bottomRow("123"),
    )

    // MARK: - Letters (uppercase)
    //
    // Velthuis is case-insensitive for the encoded letters — uppercase only
    // affects the rendered Latin character (e.g. proper-noun "Rāma"). The
    // popover alts stay lowercase since that's the canonical Velthuis form.

    private val lettersUpper: List<List<KeyDefinition>> = listOf(
        listOf(
            ch("Q"),
            ch("W"),
            ch("E"),
            ch("R", listOf(".r", ".rr")),
            ch("T", listOf(".t")),
            ch("Y"),
            ch("U", listOf("uu")),
            ch("I", listOf("ii")),
            ch("O"),
            ch("P"),
        ),
        listOf(
            ch("A", listOf("aa")),
            ch("S", listOf(".s", "\"s")),
            ch("D", listOf(".d")),
            ch("F"),
            ch("G"),
            ch("H", listOf(".h")),
            ch("J"),
            ch("K"),
            ch("L", listOf(".l", ".ll")),
        ),
        listOf(
            shiftKey,
            ch("Z"),
            ch("X"),
            ch("C"),
            ch("V"),
            ch("B"),
            ch("N", listOf(".n", "~n", "\"n")),
            ch("M", listOf(".m")),
            backspaceKey,
        ),
        bottomRow("123"),
    )

    // MARK: - Numbers / symbols
    //
    // Surface `.` `"` `~` here so Velthuis can be typed manually when long-press
    // isn't desired (or for sequences the popover doesn't include).

    private val numbers: List<List<KeyDefinition>> = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map { ch(it) },
        listOf("-", "/", ":", ";", "(", ")", "$", "&", "@", "\"").map { ch(it) },
        listOf(
            ch("."),
            ch("~"),
            ch(","),
            ch("?"),
            ch("!"),
            ch("'"),
            ch("|", listOf("।", "॥")),
            backspaceKey,
        ),
        bottomRow("ABC"),
    )
}
