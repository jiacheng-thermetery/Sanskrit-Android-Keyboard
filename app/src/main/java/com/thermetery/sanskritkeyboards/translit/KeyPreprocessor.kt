package com.thermetery.sanskritkeyboards.translit

/**
 * A stateful filter sitting between a key press and the editor, for keyboards
 * with modifier keys that change what the *next* key produces.
 */
interface KeyPreprocessor {

    /**
     * Transform one key insert. Returning null swallows it — which is what a
     * modifier key does, since pressing it types nothing by itself.
     */
    fun process(input: String): String?

    /** Keys that should render as latched right now. */
    val latchedKeys: Set<String>

    /**
     * Drop transient state (an armed modifier, stack context). Long-lived user
     * choices like Sanskrit mode survive — a mode toggle would be useless if a
     * backspace or field change silently cleared it.
     */
    fun reset()
}

/**
 * The btags (་འདོགས) modifier on the direct Tibetan keyboard.
 *
 * Tibetan stacks are vertical: the top of the stack keeps its base form and
 * everything below it takes a subjoined form. This key is how you say "put the
 * next letter *under* the last one" — press it, then the consonant:
 *
 *     ལ  ྄  ཧ   →  ལྷ
 *     ས  ྄  ཀ  ུ  →  སྐུ
 *
 * It is a modifier rather than a character: pressing it inserts nothing, and
 * pressing it twice disarms it, like shift. It is emphatically not a virama —
 * Tibetan has no vowel-killer; subjoining is a different mechanism entirely.
 *
 * ## Native orthography vs Sanskrit mode
 *
 * By default the stacker enforces native Tibetan orthography using the same
 * tables the Wylie engine parses with: a letter may only go beneath the
 * previous one if the result is a legal superscript pairing (ས over ཀ) or a
 * legal subscript (ྲ under ཀ). པ ྄ ད is refused — ད types in its base form —
 * because no native word stacks ད under པ; in མི་དམངས the ད and མ occupy
 * different structural slots entirely.
 *
 * The ཀྵ key toggles **Sanskrit mode**, which lifts those tables for loanwords
 * like པདྨ, where ད genuinely does cap མ. Sanskrit mode is deliberately
 * uncapped in depth: the Kālacakra ten-fold monogram stacks ha kṣa ma la va
 * ra ya, so "too deep to be real" has no safe cutoff. The mode is sticky until
 * toggled off, and the ཀྵ key stays lit while it is on.
 */
class TibetanStacker : KeyPreprocessor {

    var isArmed: Boolean = false
        private set

    var sanskritMode: Boolean = false
        private set

    /**
     * The stack being built, as Wylie letters, top first — the context the
     * native-orthography check validates against. Cleared by anything that
     * breaks the run of letters: a vowel, punctuation, a reset.
     */
    private val stack = mutableListOf<String>()

    private val wylieOf: Map<String, String> =
        TibetanScript.consonants.entries.associate { (w, g) -> g to w }

    override val latchedKeys: Set<String>
        get() = buildSet {
            if (isArmed) add(TibetanScript.BTAGS)
            if (sanskritMode) add(TibetanScript.SANSKRIT_MODE_TOGGLE)
        }

    override fun process(input: String): String? {
        when (input) {
            TibetanScript.BTAGS -> {
                // One-shot, and pressing it again cancels.
                isArmed = !isArmed
                return null
            }

            TibetanScript.SANSKRIT_MODE_TOGGLE -> {
                sanskritMode = !sanskritMode
                return null
            }
        }

        val wylie = wylieOf[input]

        if (!isArmed) {
            stack.clear()
            if (wylie != null) stack.add(wylie)
            return input
        }
        isArmed = false

        // Only consonants have subjoined forms. A vowel sign or tsheg passes
        // through untouched rather than being silently dropped.
        if (wylie == null) {
            stack.clear()
            return input
        }

        val allowed = sanskritMode || isNativeJoin(wylie)
        return if (allowed) {
            stack.add(wylie)
            TibetanScript.subjoined(wylie) ?: input
        } else {
            // Refused: the letter types in its base form, visibly unstacked,
            // and begins a stack of its own.
            stack.clear()
            stack.add(wylie)
            input
        }
    }

    /**
     * Whether native orthography lets [next] join beneath the current stack.
     * For the first join either reading works — the pair may be superscript +
     * root (ས ཀ → སྐ) or root + subscript (ཀ ྲ → ཀྲ). Deeper joins can only
     * be subscripts hanging from the letter above (ས ྒ ྲ → སྒྲ).
     */
    private fun isNativeJoin(next: String): Boolean = when {
        stack.isEmpty() -> false
        stack.size == 1 ->
            TibetanScript.canSuperscribe(stack[0], next) ||
                TibetanScript.canSubscribe(next, stack[0])
        else -> TibetanScript.canSubscribe(next, stack.last())
    }

    override fun reset() {
        isArmed = false
        stack.clear()
        // sanskritMode survives deliberately — see the interface contract.
    }
}
