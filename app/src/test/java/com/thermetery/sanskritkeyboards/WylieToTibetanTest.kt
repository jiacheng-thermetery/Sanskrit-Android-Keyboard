package com.thermetery.sanskritkeyboards

import com.thermetery.sanskritkeyboards.translit.InputResult
import com.thermetery.sanskritkeyboards.translit.TransliterationSession
import com.thermetery.sanskritkeyboards.translit.WylieToTibetan
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tibetan stacking is where a transliterator earns its keep, so every expected
 * value here is written as explicit code points. A stack can render plausibly
 * in a font and still be encoded wrongly — using the wrong base/subjoined pair,
 * say — and only the code points expose that.
 *
 * Base letters live at U+0F40..U+0F6C and their subjoined forms exactly 0x50
 * higher, so ka is U+0F40 and subjoined ka is U+0F90.
 */
class WylieToTibetanTest {

    private fun codePoints(s: String): String =
        s.codePoints().toArray().joinToString(" ") { "U+%04X".format(it) }

    private fun assertTibetan(expected: String, wylie: String) {
        val actual = WylieToTibetan.transliterate(wylie)
        assertEquals(
            "$wylie -> expected [${codePoints(expected)}] got [${codePoints(actual)}]",
            expected, actual
        )
    }

    // MARK: - The stack: base form on top, subjoined beneath

    @Test
    fun aLoneRootIsJustTheBaseLetter() {
        assertTibetan("ཀ", "ka")                          // ཀ
        assertTibetan("ང", "nga")                         // ང
        assertTibetan("ཆོས", "chos")            // ཆོས
    }

    @Test
    fun aSuperscriptTakesTheBaseFormAndDemotesTheRoot() {
        // ra keeps its base form; ka becomes SUBJOINED ka (U+0F90).
        assertTibetan("རྐ", "rka")                   // རྐ
        assertTibetan("སྐུ", "sku")             // སྐུ
        assertTibetan("ལྷ", "lha")                   // ལྷ — falls out of la mgo
        assertTibetan("རྡོ", "rdo")             // རྡོ
        assertTibetan("རྗེ", "rje")             // རྗེ
        assertTibetan("སྙན", "snyan")           // སྙན
        assertTibetan("སྟོང", "stong")     // སྟོང
    }

    @Test
    fun aSubscriptHangsBeneathTheRoot() {
        assertTibetan("ཀྱ", "kya")                   // ཀྱ
        assertTibetan("ཕྱག", "phyag")           // ཕྱག
        assertTibetan("ཁྱེད", "khyed")     // ཁྱེད
    }

    @Test
    fun superscriptAndSubscriptStackTogether() {
        // ra over ga, ya beneath, suffix sa.
        assertTibetan("རྒྱས", "rgyas")     // རྒྱས
    }

    @Test
    fun twoSubscriptsOnOneRoot() {
        // grwa must not read `g` as a prefix on `rwa`, which would give གརྭ.
        assertTibetan("གྲྭ", "grwa")            // གྲྭ
    }

    // MARK: - Prefixes, which are the reading of last resort

    @Test
    fun aPrefixIsUsedOnlyWhenNothingMayStack() {
        // b cannot cap k, but r can hang from k — so b must be the prefix.
        assertTibetan("བཀྲ", "bkra")            // བཀྲ
        // s CAN cap g, so it does rather than falling back to a prefix.
        assertTibetan("སྒྲ", "sgra")            // སྒྲ
        assertTibetan("དགྲ", "dgra")            // དགྲ
        assertTibetan("དབང", "dbang")           // དབང
        assertTibetan("འབྲུག", "'brug") // འབྲུག
        assertTibetan("མཁྱེན", "mkhyen") // མཁྱེན
    }

    @Test
    fun theFullFourMemberStack() {
        // prefix b, superscript s, root g, subscript r, vowel u, suffix b,
        // post-suffix s.
        assertTibetan(
            "བསྒྲུབས", "bsgrubs"
        )                                                      // བསྒྲུབས
        assertTibetan("བརྒྱད", "brgyad") // བརྒྱད
    }

    // MARK: - Vowels and codas

    @Test
    fun theInherentVowelMarksNothing() {
        assertTibetan("མ", "ma")                          // མ, no vowel sign
        assertTibetan("མི", "mi")                    // མི
    }

    @Test
    fun suffixAndPostSuffixSitBesideTheStack() {
        assertTibetan("བོད", "bod")             // བོད
        assertTibetan("སངས", "sangs")           // སངས
        assertTibetan("ཐུགས", "thugs")     // ཐུགས
        assertTibetan("གཟུགས", "gzugs") // གཟུགས
    }

    @Test
    fun bkraShisBdeLegs() {
        assertTibetan("བཀྲ", "bkra")            // བཀྲ
        assertTibetan("ཤིས", "shis")            // ཤིས
        assertTibetan("བདེ", "bde")             // བདེ
        assertTibetan("ལེགས", "legs")      // ལེགས
    }

    @Test
    fun thugsRjeChe() {
        assertTibetan("ཐུགས", "thugs")     // ཐུགས
        assertTibetan("རྗེ", "rje")             // རྗེ
        assertTibetan("ཆེ", "che")                   // ཆེ
    }

    // MARK: - Live typing

    private fun typeOut(input: String): String {
        val session = TransliterationSession(WylieToTibetan)
        var shown = ""
        for (c in input) {
            when (val r = session.process(c.toString())) {
                is InputResult.Compose -> shown = r.text
                is InputResult.Commit -> shown += r.text
            }
        }
        return shown
    }

    @Test
    fun keystrokeByKeystrokeMatchesTheWholeBuffer() {
        for (w in listOf("bsgrubs", "rgyas", "khyed", "'brug", "grwa", "brgyad")) {
            assertEquals(w, WylieToTibetan.transliterate(w), typeOut(w))
        }
    }

    @Test
    fun aPartialSyllableStillRendersSomething() {
        val session = TransliterationSession(WylieToTibetan)
        session.process("b")
        session.process("s")
        // b prefix, s over g, no vowel yet.
        assertEquals(InputResult.Compose("བསྒ"), session.process("g"))
    }

    @Test
    fun theTshegCommitsTheSyllable() {
        val session = TransliterationSession(WylieToTibetan)
        for (c in "bod") session.process(c.toString())
        assertEquals("bod", session.pendingInput)

        // The space key emits a tsheg, which is not a Wylie letter.
        assertEquals(InputResult.Commit("་"), session.process("་"))
        assertEquals("", session.pendingInput)
    }

    @Test
    fun theApostropheIsALetterNotABoundary() {
        val session = TransliterationSession(WylieToTibetan)
        for (c in "'brug") session.process(c.toString())
        assertEquals("'brug", session.pendingInput)
        assertEquals("འབྲུག", WylieToTibetan.transliterate("'brug"))
    }

    @Test
    fun backspaceWalksBackOneWylieLetterAtATime() {
        val session = TransliterationSession(WylieToTibetan)
        for (c in "bkra") session.process(c.toString())

        assertEquals(InputResult.Compose("བཀྲ"), session.processBackspace())
        assertEquals("bkr", session.pendingInput)

        assertEquals(InputResult.Compose("བཀ"), session.processBackspace())
        assertEquals("bk", session.pendingInput)
    }
}
