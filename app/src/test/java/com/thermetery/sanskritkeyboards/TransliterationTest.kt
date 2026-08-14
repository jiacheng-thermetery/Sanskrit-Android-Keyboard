package com.thermetery.sanskritkeyboards

import com.thermetery.sanskritkeyboards.translit.HkToDevanagari
import com.thermetery.sanskritkeyboards.translit.HkToIast
import com.thermetery.sanskritkeyboards.translit.IastToDevanagari
import com.thermetery.sanskritkeyboards.translit.InputResult
import com.thermetery.sanskritkeyboards.translit.TransliterationSession
import com.thermetery.sanskritkeyboards.translit.Transliterator
import com.thermetery.sanskritkeyboards.translit.VelthuisToDevanagari
import com.thermetery.sanskritkeyboards.translit.VelthuisToIast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The worked examples from the README, plus the live-buffer behaviours the iOS
 * keyboards document (re-render on every keystroke, backspace by input
 * character, commit on a non-buffer character).
 */
class TransliterationTest {

    // MARK: - HK → IAST

    @Test
    fun hkToIast_readmeExamples() {
        assertEquals("saṃskṛtam", HkToIast.transliterate("saMskRtam"))
        assertEquals("kṛṣṇa", HkToIast.transliterate("kRSNa"))
        assertEquals("śrī", HkToIast.transliterate("zrI"))
        assertEquals("namaste", HkToIast.transliterate("namaste"))
        assertEquals("dharma", HkToIast.transliterate("dharma"))
    }

    @Test
    fun hkToIast_greedyLongestMatchWins() {
        // RR must beat R+R, and lRR must beat lR+R.
        assertEquals("ṛ", HkToIast.transliterate("R"))
        assertEquals("ṝ", HkToIast.transliterate("RR"))
        assertEquals("ḷ", HkToIast.transliterate("lR"))
        assertEquals("ḹ", HkToIast.transliterate("lRR"))
    }

    // MARK: - HK → Devanāgarī

    @Test
    fun hkToDevanagari_readmeExamples() {
        assertEquals("नमस्ते", HkToDevanagari.transliterate("namaste"))
        assertEquals("संस्कृतम्", HkToDevanagari.transliterate("saMskRtam"))
        assertEquals("श्री", HkToDevanagari.transliterate("zrI"))
        assertEquals("कृष्ण", HkToDevanagari.transliterate("kRSNa"))
        assertEquals("धर्म", HkToDevanagari.transliterate("dharma"))
    }

    @Test
    fun hkToDevanagari_independentVsDependentVowels() {
        // Word-initial vowels are independent; after a consonant they are signs.
        assertEquals("अ", HkToDevanagari.transliterate("a"))
        assertEquals("आ", HkToDevanagari.transliterate("A"))
        assertEquals("क", HkToDevanagari.transliterate("ka"))     // implicit short-a
        assertEquals("का", HkToDevanagari.transliterate("kA"))
        assertEquals("क्", HkToDevanagari.transliterate("k"))      // bare consonant + virama
    }

    // MARK: - IAST → Devanāgarī

    @Test
    fun iastToDevanagari_readmeExamples() {
        assertEquals("नमस्ते", IastToDevanagari.transliterate("namaste"))
        assertEquals("संस्कृतम्", IastToDevanagari.transliterate("saṃskṛtam"))
        assertEquals("श्री", IastToDevanagari.transliterate("śrī"))
        assertEquals("कृष्ण", IastToDevanagari.transliterate("kṛṣṇa"))
        assertEquals("धर्म", IastToDevanagari.transliterate("dharma"))
    }

    @Test
    fun iastToDevanagari_acceptsBothAnusvaraForms() {
        assertEquals(
            IastToDevanagari.transliterate("saṃskṛtam"),
            IastToDevanagari.transliterate("saṁskṛtam")
        )
    }

    // MARK: - Velthuis → IAST

    @Test
    fun velthuisToIast_readmeExamples() {
        assertEquals("saṃskṛtam", VelthuisToIast.transliterate("sa.msk.rtam"))
        assertEquals("śrī", VelthuisToIast.transliterate("\"srii"))
        assertEquals("kṛṣṇa", VelthuisToIast.transliterate("k.r.s.na"))
        assertEquals("jñāna", VelthuisToIast.transliterate("j~naana"))
        assertEquals("namaste", VelthuisToIast.transliterate("namaste"))
    }

    // MARK: - Velthuis → Devanāgarī

    @Test
    fun velthuisToDevanagari_readmeExamples() {
        assertEquals("नमस्ते", VelthuisToDevanagari.transliterate("namaste"))
        assertEquals("संस्कृतम्", VelthuisToDevanagari.transliterate("sa.msk.rtam"))
        assertEquals("श्री", VelthuisToDevanagari.transliterate("\"srii"))
        assertEquals("कृष्ण", VelthuisToDevanagari.transliterate("k.r.s.na"))
        assertEquals("ज्ञान", VelthuisToDevanagari.transliterate("j~naana"))
        assertEquals("धर्म", VelthuisToDevanagari.transliterate("dharma"))
    }

    // MARK: - Cross-scheme agreement

    @Test
    fun allThreeDevanagariSchemesAgree() {
        val words = listOf(
            Triple("namaste", "namaste", "namaste"),
            Triple("saMskRtam", "saṃskṛtam", "sa.msk.rtam"),
            Triple("kRSNa", "kṛṣṇa", "k.r.s.na"),
            Triple("zrI", "śrī", "\"srii"),
            Triple("dharma", "dharma", "dharma"),
        )
        for ((hk, iast, velthuis) in words) {
            val viaHk = HkToDevanagari.transliterate(hk)
            val viaIast = IastToDevanagari.transliterate(iast)
            val viaVelthuis = VelthuisToDevanagari.transliterate(velthuis)
            assertEquals("HK vs IAST for $hk", viaHk, viaIast)
            assertEquals("HK vs Velthuis for $hk", viaHk, viaVelthuis)
        }
    }

    // MARK: - Live session behaviour

    /** Feed a string one character at a time, returning what is on screen. */
    private fun typeOut(scheme: Transliterator, input: String): String {
        val session = TransliterationSession(scheme)
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
    fun typingKeystrokeByKeystrokeMatchesWholeBuffer() {
        assertEquals("कृष्ण", typeOut(HkToDevanagari, "kRSNa"))
        assertEquals("संस्कृतम्", typeOut(HkToDevanagari, "saMskRtam"))
        assertEquals("saṃskṛtam", typeOut(HkToIast, "saMskRtam"))
        assertEquals("कृष्ण", typeOut(VelthuisToDevanagari, "k.r.s.na"))
        assertEquals("कृष्ण", typeOut(IastToDevanagari, "kṛṣṇa"))
    }

    @Test
    fun typingRThenAnotherRUpgradesToLongVocalicR() {
        val session = TransliterationSession(HkToIast)
        assertEquals(InputResult.Compose("ṛ"), session.process("R"))
        assertEquals(InputResult.Compose("ṝ"), session.process("R"))
    }

    @Test
    fun spaceCommitsAndResetsTheBuffer() {
        val session = TransliterationSession(HkToDevanagari)
        session.process("k")
        session.process("a")
        assertEquals("ka", session.pendingInput)

        val result = session.process(" ")
        assertEquals(InputResult.Commit(" "), result)
        assertEquals("", session.pendingInput)
    }

    @Test
    fun backspaceRemovesOneInputCharacterAndReRenders() {
        val session = TransliterationSession(HkToDevanagari)
        for (c in "kRSNa") session.process(c.toString())
        assertEquals("kRSNa", session.pendingInput)

        assertEquals(InputResult.Compose("कृष्ण्"), session.processBackspace())
        assertEquals(InputResult.Compose("कृष्"), session.processBackspace())
        assertEquals(InputResult.Compose("कृ"), session.processBackspace())
        assertEquals(InputResult.Compose("क्"), session.processBackspace())
        assertEquals(InputResult.Compose(""), session.processBackspace())
        // Buffer empty — the service falls through to a normal editor delete.
        assertNull(session.processBackspace())
    }

    @Test
    fun velthuisPrefixMarksExtendTheBufferRatherThanCommitting() {
        val session = TransliterationSession(VelthuisToDevanagari)
        assertEquals(InputResult.Compose("क्"), session.process("k"))

        // A bare `.` is letter-like here, so it extends the buffer instead of
        // committing it. It has no reading of its own yet, so it renders as a
        // literal dot until the next keystroke completes the bigram.
        assertEquals(InputResult.Compose("क्."), session.process("."))
        assertEquals("k.", session.pendingInput)

        // `.` + `r` now reads as vocalic ṛ, and the dot disappears.
        assertEquals(InputResult.Compose("कृ"), session.process("r"))
    }

    @Test
    fun popoverBigramsArriveAsOneInsert() {
        // Long-pressing `n` on the Velthuis layout commits "~n" in one go.
        val session = TransliterationSession(VelthuisToDevanagari)
        session.process("j")
        session.process("~n")
        session.process("aa")
        session.process("n")
        session.process("a")
        assertEquals("ज्ञान", VelthuisToDevanagari.transliterate(session.pendingInput))
    }

    @Test
    fun digitsAndPunctuationCommitTheBuffer() {
        val session = TransliterationSession(HkToIast)
        session.process("k")
        assertEquals(InputResult.Commit("1"), session.process("1"))
        assertEquals("", session.pendingInput)
        assertEquals(InputResult.Commit("\n"), session.process("\n"))
    }

    @Test
    fun iastSchemeIsCaseFolded() {
        // Proper nouns typed with a capital still transliterate.
        assertEquals(
            IastToDevanagari.transliterate("rāma"),
            IastToDevanagari.transliterate("Rāma".lowercase())
        )
        val session = TransliterationSession(IastToDevanagari)
        session.process("R")
        session.process("ā")
        session.process("m")
        session.process("a")
        assertEquals("राम", IastToDevanagari.transliterate(session.pendingInput))
    }
}
