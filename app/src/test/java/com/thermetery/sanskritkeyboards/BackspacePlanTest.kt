package com.thermetery.sanskritkeyboards

import com.thermetery.sanskritkeyboards.ime.BackspacePlan
import com.thermetery.sanskritkeyboards.ime.planBackspace
import com.thermetery.sanskritkeyboards.translit.HkToDevanagari
import com.thermetery.sanskritkeyboards.translit.TransliterationSession
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Backspace has three jobs and they must be tried in the right order. Getting
 * this wrong is invisible until someone selects text on a real device, which
 * is exactly how the original bug shipped.
 */
class BackspacePlanTest {

    @Test
    fun aSelectionIsDeletedEvenWithNoPendingBuffer() {
        assertEquals(
            BackspacePlan.DeleteSelection,
            planBackspace(hasSelection = true, session = null)
        )
    }

    @Test
    fun aSelectionBeatsThePendingBuffer() {
        val session = TransliterationSession(HkToDevanagari)
        for (c in "kRSNa") session.process(c.toString())
        assertEquals("kRSNa", session.pendingInput)

        // The selected text is going away, so the buffer tracking it must be
        // dropped rather than re-rendered over the top of the deletion.
        assertEquals(
            BackspacePlan.DeleteSelection,
            planBackspace(hasSelection = true, session = session)
        )
        assertEquals("", session.pendingInput)
    }

    @Test
    fun withoutASelectionThePendingBufferShortens() {
        val session = TransliterationSession(HkToDevanagari)
        for (c in "kRSNa") session.process(c.toString())

        assertEquals(
            BackspacePlan.Compose("कृष्ण्"),
            planBackspace(hasSelection = false, session = session)
        )
    }

    @Test
    fun anEmptyBufferDefersToTheEditor() {
        val session = TransliterationSession(HkToDevanagari)
        assertEquals(
            BackspacePlan.SendDelKey,
            planBackspace(hasSelection = false, session = session)
        )
    }

    @Test
    fun theNonTransliteratingKeyboardAlwaysDefersToTheEditor() {
        // The plain IAST keyboard has no session at all.
        assertEquals(
            BackspacePlan.SendDelKey,
            planBackspace(hasSelection = false, session = null)
        )
    }

    @Test
    fun deletingPastTheStartOfTheBufferHandsOffToTheEditor() {
        val session = TransliterationSession(HkToDevanagari)
        session.process("k")
        assertEquals(
            BackspacePlan.Compose(""),
            planBackspace(hasSelection = false, session = session)
        )
        // Buffer now empty — the next backspace must delete real committed text.
        assertEquals(
            BackspacePlan.SendDelKey,
            planBackspace(hasSelection = false, session = session)
        )
    }
}
