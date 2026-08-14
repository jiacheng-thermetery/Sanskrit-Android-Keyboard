package com.thermetery.sanskritkeyboards.layouts

import com.thermetery.sanskritkeyboards.core.KeyDefinition
import com.thermetery.sanskritkeyboards.core.KeyboardLayout
import com.thermetery.sanskritkeyboards.core.KeyboardMode

/**
 * QWERTY layout shared by HK→IAST and HK→Devanāgarī keyboards.
 * No long-press alternates — diacritics are produced by the transliterator
 * (e.g. typing `A` produces `ā` or `आ` depending on the keyboard).
 */
object HkLayout : KeyboardLayout {

    override fun layout(mode: KeyboardMode, shifted: Boolean): List<List<KeyDefinition>> =
        when (mode) {
            KeyboardMode.LETTERS -> if (shifted) lettersUpper else lettersLower
            KeyboardMode.NUMBERS -> numbers
        }

    private val lettersLower: List<List<KeyDefinition>> = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").map { ch(it) },
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").map { ch(it) },
        listOf(shiftKey) + listOf("z", "x", "c", "v", "b", "n", "m").map { ch(it) } + listOf(backspaceKey),
        bottomRow("123"),
    )

    private val lettersUpper: List<List<KeyDefinition>> = listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P").map { ch(it) },
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L").map { ch(it) },
        listOf(shiftKey) + listOf("Z", "X", "C", "V", "B", "N", "M").map { ch(it) } + listOf(backspaceKey),
        bottomRow("123"),
    )

    private val numbers: List<List<KeyDefinition>> = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map { ch(it) },
        listOf("-", "/", ":", ";", "(", ")", "$", "&", "@", "\"").map { ch(it) },
        listOf(
            ch("|"),
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
