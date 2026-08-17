package com.thermetery.sanskritkeyboards

import com.thermetery.sanskritkeyboards.translit.TibetanScript
import com.thermetery.sanskritkeyboards.translit.TibetanStacker
import com.thermetery.sanskritkeyboards.translit.WylieToTibetan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The ྄ key is a btags modifier, not a virama — it makes the *next* consonant
 * subjoined so a stack can be built by hand. Typing it as a literal mark would
 * leave a stray glyph in the text and stack nothing.
 */
class TibetanStackerTest {

    private fun codePoints(s: String): String =
        s.codePoints().toArray().joinToString(" ") { "U+%04X".format(it) }

    /** Feed a sequence of key presses and return what reaches the editor. */
    private fun type(vararg keys: String): String {
        val stacker = TibetanStacker()
        val out = StringBuilder()
        for (k in keys) stacker.process(k)?.let { out.append(it) }
        return out.toString()
    }

    @Test
    fun theModifierItselfTypesNothing() {
        val stacker = TibetanStacker()
        assertNull("the btags key must not insert a character", stacker.process(TibetanScript.BTAGS))
        assertTrue(stacker.isArmed)
    }

    @Test
    fun itSubjoinsTheFollowingConsonant() {
        val stacker = TibetanStacker()
        stacker.process(TibetanScript.BTAGS)
        // ཀ (U+0F40) becomes subjoined ka (U+0F90), not ka plus a mark.
        assertEquals("ྐ", stacker.process("ཀ"))
        assertFalse("the modifier is one-shot", stacker.isArmed)
    }

    @Test
    fun buildingRealStacksByHand() {
        // ལ ྄ ཧ -> ལྷ
        val lha = type("ལ", TibetanScript.BTAGS, "ཧ")
        assertEquals("expected [${codePoints("ལྷ")}] got [${codePoints(lha)}]", "ལྷ", lha)

        // ས ྄ ཀ ུ -> སྐུ
        val sku = type("ས", TibetanScript.BTAGS, "ཀ", "ུ")
        assertEquals("expected [${codePoints("སྐུ")}] got [${codePoints(sku)}]", "སྐུ", sku)

        // ར ྄ ཀ -> རྐ
        assertEquals("རྐ", type("ར", TibetanScript.BTAGS, "ཀ"))
    }

    @Test
    fun handBuiltStacksMatchWhatWylieProduces() {
        // The two routes into the same script must agree.
        assertEquals(
            WylieToTibetan.transliterate("lha"),
            type("ལ", TibetanScript.BTAGS, "ཧ")
        )
        assertEquals(
            WylieToTibetan.transliterate("sku"),
            type("ས", TibetanScript.BTAGS, "ཀ", "ུ")
        )
        assertEquals(
            WylieToTibetan.transliterate("rka"),
            type("ར", TibetanScript.BTAGS, "ཀ")
        )
        // Two subscripts: ག ྄ ར ྄ ཝ -> གྲྭ
        assertEquals(
            WylieToTibetan.transliterate("grwa"),
            type("ག", TibetanScript.BTAGS, "ར", TibetanScript.BTAGS, "ཝ")
        )
    }

    @Test
    fun pressingItTwiceDisarms() {
        val stacker = TibetanStacker()
        stacker.process(TibetanScript.BTAGS)
        assertTrue(stacker.isArmed)
        stacker.process(TibetanScript.BTAGS)
        assertFalse(stacker.isArmed)
        // Now an ordinary consonant stays in its base form.
        assertEquals("ཀ", stacker.process("ཀ"))
    }

    @Test
    fun itAffectsOnlyTheNextKey() {
        // Only the first ཀ is subjoined; the second is not.
        assertEquals("ཀྐཀ", type("ཀ", TibetanScript.BTAGS, "ཀ", "ཀ"))
    }

    @Test
    fun nonConsonantsPassThroughRatherThanBeingDropped() {
        val stacker = TibetanStacker()
        stacker.process(TibetanScript.BTAGS)
        // A vowel sign has no subjoined form — insert it rather than eat it.
        assertEquals("ི", stacker.process("ི"))
        assertFalse(stacker.isArmed)

        stacker.process(TibetanScript.BTAGS)
        assertEquals("་", stacker.process("་"))
    }

    @Test
    fun theKeyReportsItselfLatchedWhileArmed() {
        val stacker = TibetanStacker()
        assertNull(stacker.latchedKey)
        stacker.process(TibetanScript.BTAGS)
        assertEquals(TibetanScript.BTAGS, stacker.latchedKey)
        stacker.process("ཀ")
        assertNull(stacker.latchedKey)
    }

    @Test
    fun resetDisarms() {
        val stacker = TibetanStacker()
        stacker.process(TibetanScript.BTAGS)
        stacker.reset()
        assertFalse(stacker.isArmed)
        assertEquals("ཀ", stacker.process("ཀ"))
    }

    @Test
    fun everyBaseConsonantHasASubjoinedFormExactly0x50Above() {
        for ((wylie, base) in TibetanScript.consonants) {
            val sub = TibetanScript.subjoinedForm(base) ?: continue
            assertEquals(
                "$wylie: subjoined form is not 0x50 above the base",
                base.codePointAt(0) + 0x50, sub.codePointAt(0)
            )
        }
    }
}
