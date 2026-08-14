package com.thermetery.sanskritkeyboards.ime

import com.thermetery.sanskritkeyboards.translit.InputResult
import com.thermetery.sanskritkeyboards.translit.TransliterationSession

/**
 * What a backspace should do. Kept separate from the service — and free of any
 * Android dependency — so the precedence between "there is a selection",
 * "there is a pending transliteration buffer" and "plain delete" is testable
 * on the JVM.
 */
sealed class BackspacePlan {

    /** Re-render the composing region after shortening the pending buffer. */
    data class Compose(val text: String) : BackspacePlan()

    /** Replace the current selection with nothing. */
    data object DeleteSelection : BackspacePlan()

    /** Hand a DEL key to the editor and let it decide what one delete means. */
    data object SendDelKey : BackspacePlan()
}

/**
 * Decide what a backspace means right now.
 *
 * A selection takes precedence over everything: deleting it is what every
 * other keyboard does, and it is what the user is asking for. The pending
 * buffer is dropped in that case rather than consumed, since the text it was
 * tracking is about to disappear.
 */
fun planBackspace(hasSelection: Boolean, session: TransliterationSession?): BackspacePlan {
    if (hasSelection) {
        session?.reset()
        return BackspacePlan.DeleteSelection
    }
    val result = session?.processBackspace()
    return if (result is InputResult.Compose) {
        BackspacePlan.Compose(result.text)
    } else {
        BackspacePlan.SendDelKey
    }
}
