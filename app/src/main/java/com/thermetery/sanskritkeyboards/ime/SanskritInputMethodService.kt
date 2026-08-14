package com.thermetery.sanskritkeyboards.ime

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.thermetery.sanskritkeyboards.core.KeyboardLayout
import com.thermetery.sanskritkeyboards.translit.InputResult
import com.thermetery.sanskritkeyboards.translit.TransliterationSession
import com.thermetery.sanskritkeyboards.translit.Transliterator
import com.thermetery.sanskritkeyboards.ui.KeyboardView
import com.thermetery.sanskritkeyboards.ui.KeyboardViewDelegate

/**
 * Shared behaviour for all six keyboards — the Android counterpart of the iOS
 * `KeyboardViewController`s.
 *
 * Subclasses supply a layout and, for the five transliterating keyboards, a
 * scheme. The plain IAST keyboard supplies no scheme and commits text directly.
 */
abstract class SanskritInputMethodService : InputMethodService(), KeyboardViewDelegate {

    /** Which key layout this keyboard presents. */
    protected abstract val keyboardLayout: KeyboardLayout

    /** Null for the plain IAST keyboard — it types its characters literally. */
    protected open val scheme: Transliterator? = null

    /**
     * The IAST keyboard leans on long-press popovers for every diacritic, so it
     * reserves headroom at the top for the popover strip.
     */
    protected open val reservesPopoverHeadroom: Boolean = false

    protected open val portraitHeightDp: Float get() = if (reservesPopoverHeadroom) 346f else 260f
    protected open val landscapeHeightDp: Float get() = if (reservesPopoverHeadroom) 275f else 200f

    private var session: TransliterationSession? = null
    private var keyboardView: KeyboardView? = null

    override fun onCreate() {
        super.onCreate()
        session = scheme?.let { TransliterationSession(it) }
    }

    override fun onCreateInputView(): View {
        val view = KeyboardView(this, keyboardLayout, reservesPopoverHeadroom)
        view.delegate = this
        view.desiredHeightPx = targetHeightPx()
        keyboardView = view
        return view
    }

    private fun targetHeightPx(): Int {
        val metrics = resources.displayMetrics
        val isPortrait = metrics.heightPixels >= metrics.widthPixels
        val dp = if (isPortrait) portraitHeightDp else landscapeHeightDp
        return (dp * metrics.density).toInt()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        keyboardView?.desiredHeightPx = targetHeightPx()
        session?.reset()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        currentInputConnection?.finishComposingText()
        session?.reset()
    }

    /**
     * Cursor moves and selection changes reset the buffer, matching the iOS
     * `selectionWillChange` behaviour — so tapping to a different position
     * mid-word starts the next keystroke fresh.
     *
     * Our own `setComposingText` calls also land here; those leave the cursor
     * collapsed at the end of the composing region, which is how we tell them
     * apart from a real cursor move.
     */
    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int,
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )
        val s = session ?: return
        if (s.pendingInput.isEmpty()) return

        val selfInflicted =
            candidatesStart >= 0 && newSelStart == newSelEnd && newSelStart == candidatesEnd
        if (!selfInflicted) {
            currentInputConnection?.finishComposingText()
            s.reset()
        }
    }

    // MARK: - KeyboardViewDelegate

    override fun onInsertText(view: KeyboardView, text: String) {
        val ic = currentInputConnection ?: return
        if (text == "\n") {
            handleReturn()
            return
        }
        val s = session
        if (s == null) {
            ic.commitText(text, 1)
            return
        }
        when (val result = s.process(text)) {
            is InputResult.Compose -> ic.setComposingText(result.text, 1)
            is InputResult.Commit -> {
                ic.finishComposingText()
                ic.commitText(result.text, 1)
            }
        }
    }

    override fun onDeleteBackward(view: KeyboardView) {
        val ic = currentInputConnection ?: return
        val result = session?.processBackspace()
        if (result is InputResult.Compose) {
            ic.setComposingText(result.text, 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    override fun onAdvanceToNextInputMode(view: KeyboardView) {
        session?.let {
            currentInputConnection?.finishComposingText()
            it.reset()
        }
        advanceToNextInputMode()
    }

    // MARK: - Helpers

    /**
     * The iOS return key always inserts a newline. On Android a single-line
     * field advertises an action (Search, Send, Done, …) instead, and typing a
     * newline there does nothing — so honour the action when the editor asks
     * for one, and insert a newline otherwise.
     */
    private fun handleReturn() {
        val ic = currentInputConnection ?: return
        session?.let {
            ic.finishComposingText()
            it.reset()
        }
        val editorInfo = currentInputEditorInfo
        val options = editorInfo?.imeOptions ?: 0
        val action = options and EditorInfo.IME_MASK_ACTION
        val suppressed = (options and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        if (action != EditorInfo.IME_ACTION_NONE && !suppressed) {
            ic.performEditorAction(action)
        } else {
            ic.commitText("\n", 1)
        }
    }

    /** Switch to the next enabled input method — the 🌐 globe key. */
    private fun advanceToNextInputMode() {
        val imm = getSystemService(InputMethodManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (switchToNextInputMethod(false)) return
        } else {
            @Suppress("DEPRECATION")
            val token = window?.window?.attributes?.token
            @Suppress("DEPRECATION")
            if (token != null && imm.switchToNextInputMethod(token, false)) return
        }
        // Nothing else enabled to rotate to — offer the picker instead.
        imm.showInputMethodPicker()
    }
}
