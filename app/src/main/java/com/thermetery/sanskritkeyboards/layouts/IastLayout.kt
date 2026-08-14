package com.thermetery.sanskritkeyboards.layouts

import com.thermetery.sanskritkeyboards.core.KeyDefinition
import com.thermetery.sanskritkeyboards.core.KeyboardLayout
import com.thermetery.sanskritkeyboards.core.KeyboardMode

/**
 * IAST popover layout — QWERTY with the Sanskrit diacritics on long-press.
 * Used by the plain IAST keyboard and by IAST → Devanāgarī (only the output
 * script differs).
 */
object IastLayout : KeyboardLayout {

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
            ch("r", listOf("ṛ", "ṝ")),
            ch("t", listOf("ṭ")),
            ch("y"),
            ch("u", listOf("ū")),
            ch("i", listOf("ī")),
            ch("o"),
            ch("p"),
        ),
        listOf(
            ch("a", listOf("ā")),
            ch("s", listOf("ś", "ṣ")),
            ch("d", listOf("ḍ")),
            ch("f"),
            ch("g"),
            ch("h", listOf("ḥ")),
            ch("j"),
            ch("k"),
            ch("l", listOf("ḷ", "ḹ")),
        ),
        listOf(
            shiftKey,
            ch("z"),
            ch("x"),
            ch("c"),
            ch("v"),
            ch("b"),
            ch("n", listOf("ñ", "ṅ", "ṇ")),
            ch("m", listOf("ṃ", "ṁ")),
            backspaceKey,
        ),
        bottomRow("123"),
    )

    // MARK: - Letters (uppercase)

    private val lettersUpper: List<List<KeyDefinition>> = listOf(
        listOf(
            ch("Q"),
            ch("W"),
            ch("E"),
            ch("R", listOf("Ṛ", "Ṝ")),
            ch("T", listOf("Ṭ")),
            ch("Y"),
            ch("U", listOf("Ū")),
            ch("I", listOf("Ī")),
            ch("O"),
            ch("P"),
        ),
        listOf(
            ch("A", listOf("Ā")),
            ch("S", listOf("Ś", "Ṣ")),
            ch("D", listOf("Ḍ")),
            ch("F"),
            ch("G"),
            ch("H", listOf("Ḥ")),
            ch("J"),
            ch("K"),
            ch("L", listOf("Ḷ", "Ḹ")),
        ),
        listOf(
            shiftKey,
            ch("Z"),
            ch("X"),
            ch("C"),
            ch("V"),
            ch("B"),
            ch("N", listOf("Ñ", "Ṅ", "Ṇ")),
            ch("M", listOf("Ṃ", "Ṁ")),
            backspaceKey,
        ),
        bottomRow("123"),
    )

    // MARK: - Numbers

    private val numbers: List<List<KeyDefinition>> = listOf(
        listOf(
            ch("1"), ch("2"), ch("3"), ch("4"), ch("5"),
            ch("6"), ch("7"), ch("8"), ch("9"), ch("0"),
        ),
        listOf(
            ch("-"), ch("/"), ch(":"), ch(";"), ch("("),
            ch(")"), ch("$"), ch("&"), ch("@"), ch("\""),
        ),
        listOf(
            ch("|", listOf("।", "॥")),
            ch("."),
            ch(","),
            ch("?"),
            ch("!"),
            ch("'"),
            ch("*"),
            backspaceKey,
        ),
        bottomRow("ABC"),
    )
}
