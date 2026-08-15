package com.thermetery.sanskritkeyboards

import com.thermetery.sanskritkeyboards.core.KeyKind
import com.thermetery.sanskritkeyboards.core.KeyboardMode
import com.thermetery.sanskritkeyboards.layouts.TibetanLayout
import com.thermetery.sanskritkeyboards.translit.TibetanScript
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the direct Tibetan layout to the supplied key mapping. A layout is data,
 * and data drifts silently — a wrong glyph here would look like a plausible
 * keyboard and simply type the wrong letter.
 */
class TibetanLayoutTest {

    /** The character each QWERTY position types, unshifted. */
    private val suppliedMapping: List<Pair<String, String>> = listOf(
        "q" to "྄", "w" to "ཉ", "e" to "ེ", "r" to "ར", "t" to "ཏ",
        "y" to "ཡ", "u" to "ུ", "i" to "ི", "o" to "ོ", "p" to "པ",
        "a" to "འ", "s" to "ས", "d" to "ད", "f" to "ང", "g" to "ག",
        "h" to "ཧ", "j" to "ཇ", "k" to "ཀ", "l" to "ལ",
        "z" to "ཟ", "x" to "ཛ", "c" to "ཅ", "v" to "ཙ",
        "b" to "བ", "n" to "ན", "m" to "མ",
    )

    /** The character each QWERTY position types with shift held. */
    private val suppliedShiftedMapping: List<Pair<String, String>> = listOf(
        "Q" to "྄", "W" to "ཝ", "E" to "ཻ", "R" to "ྲ", "T" to "ཐ",
        "Y" to "ྱ", "U" to "ྭ", "I" to "ྀ", "O" to "ཽ", "P" to "ཕ",
        "A" to "ཨ", "S" to "ཤ", "D" to "ཌ", "F" to "ཋ", "G" to "ཊ",
        "H" to "ཿ", "J" to "ཇ", "K" to "ཁ", "L" to "ླ",
        "Z" to "ཞ", "X" to "ཥ", "C" to "ཆ", "V" to "ཚ",
        "B" to "བ", "N" to "ཎ", "M" to "ཾ",
    )

    /** Character keys of a letters layer, in reading order. */
    private fun characterKeys(shifted: Boolean): List<String> =
        TibetanLayout.layout(KeyboardMode.LETTERS, shifted)
            .dropLast(1)   // the bottom row is controls
            .flatten()
            .filter { it.kind == KeyKind.CHARACTER }
            .map { it.primary }

    private fun unshiftedCharacterKeys(): List<String> = characterKeys(shifted = false)

    @Test
    fun theUnshiftedLayerMatchesTheSuppliedMapping() {
        assertEquals(
            "Tibetan unshifted layer drifted from the supplied mapping",
            suppliedMapping.map { it.second }, characterKeys(shifted = false)
        )
    }

    @Test
    fun theShiftedLayerMatchesTheSuppliedMapping() {
        assertEquals(
            "Tibetan shifted layer drifted from the supplied mapping",
            suppliedShiftedMapping.map { it.second }, characterKeys(shifted = true)
        )
    }

    /**
     * Shift is where the aspirates live, and each sits above its unaspirated
     * partner. That pairing is also what confirms K must be ཀ: K-shift is ཁ.
     */
    @Test
    fun shiftGivesTheAspiratedPartner() {
        val base = suppliedMapping.toMap()
        val shift = suppliedShiftedMapping.toMap()
        for ((lower, upper) in listOf("k" to "K", "c" to "C", "t" to "T", "p" to "P", "v" to "V")) {
            val plain = base[lower]!!
            val aspirated = shift[upper]!!
            assertTrue(
                "$lower/$upper should be an unaspirated/aspirated pair, got $plain/$aspirated",
                plain in TibetanScript.consonants.values &&
                    aspirated in TibetanScript.consonants.values
            )
        }
        assertEquals("ཀ", base["k"])
        assertEquals("ཁ", shift["K"])
    }

    @Test
    fun theRowsFollowTheQwertySkeleton() {
        val rows = TibetanLayout.layout(KeyboardMode.LETTERS, shifted = false)
        assertEquals("four rows: 10 / 9 / shift+7+backspace / bottom", 4, rows.size)
        assertEquals(10, rows[0].size)
        assertEquals(9, rows[1].size)
        assertEquals(9, rows[2].size)   // shift + 7 letters + backspace
        assertEquals(KeyKind.SHIFT, rows[2].first().kind)
        assertEquals(KeyKind.BACKSPACE, rows[2].last().kind)
    }

    /**
     * ka is the commonest letter in Tibetan. The supplied table listed ཇ twice
     * and omitted ཀ, so this guards the correction.
     */
    @Test
    fun kaIsPresentAndNotDuplicatedWithJa() {
        val keys = unshiftedCharacterKeys()
        assertTrue("ka missing from the keyboard", "ཀ" in keys)
        assertEquals("ja appears more than once", 1, keys.count { it == "ཇ" })
    }

    @Test
    fun allThirtyConsonantsAreReachableAcrossBothLayers() {
        val reachable = (
            TibetanLayout.layout(KeyboardMode.LETTERS, shifted = false) +
                TibetanLayout.layout(KeyboardMode.LETTERS, shifted = true)
            ).flatten()
            .filter { it.kind == KeyKind.CHARACTER }
            .flatMap { listOf(it.primary) + it.alternates }
            .toSet()

        // The thirty gsal byed, excluding the Sanskrit-only additions.
        val core = listOf(
            "k", "kh", "g", "ng", "c", "ch", "j", "ny", "t", "th",
            "d", "n", "p", "ph", "b", "m", "ts", "tsh", "dz", "w",
            "zh", "z", "'", "y", "r", "l", "sh", "s", "h", "a",
        )
        val missing = core.mapNotNull { w ->
            TibetanScript.consonants[w]?.takeIf { it !in reachable }?.let { "$w ($it)" }
        }
        assertTrue("not reachable from any layer: $missing", missing.isEmpty())
    }

    @Test
    fun everyConsonantOffersItsSubjoinedFormOnLongPress() {
        val keys = TibetanLayout.layout(KeyboardMode.LETTERS, shifted = false)
            .flatten()
            .filter { it.kind == KeyKind.CHARACTER }

        // Pick a few whose subjoined form is needed constantly for stacks.
        for (wylie in listOf("k", "g", "y", "r", "l", "s", "h")) {
            val base = TibetanScript.consonants[wylie]!!
            val sub = TibetanScript.subjoined(wylie)!!
            val key = keys.firstOrNull { it.primary == base }
            assertTrue("$wylie ($base) is not on the unshifted layer", key != null)
            assertTrue(
                "$base does not offer subjoined $sub on long-press",
                sub in key!!.alternates
            )
        }
    }

    @Test
    fun theBottomRowHasADedicatedTshegBesideTheSpaceBar() {
        val bottom = TibetanLayout.layout(KeyboardMode.LETTERS, shifted = false).last()
        val space = bottom.first { it.kind == KeyKind.SPACE }
        assertEquals("the direct layout's space bar types a space", " ", space.primary)
        assertTrue(
            "no tsheg key in the bottom row",
            bottom.any { it.kind == KeyKind.CHARACTER && it.primary == "་" }
        )
    }

    @Test
    fun theSymbolsLayerMatchesTheSuppliedRows() {
        val rows = TibetanLayout.layout(KeyboardMode.NUMBERS, shifted = false)
        fun chars(row: Int) = rows[row].filter { it.kind == KeyKind.CHARACTER }.map { it.primary }

        assertEquals(
            listOf("༡", "༢", "༣", "༤", "༥", "༦", "༧", "༨", "༩", "༠"),
            chars(0)
        )
        assertEquals(
            listOf("-", "/", ":", ";", "༼", "༽", "$", "༕", "@", "“"),
            chars(1)
        )
        assertEquals(
            listOf("࿂", "༜", "༴", "།", "༄"),
            chars(2)
        )
    }
}
