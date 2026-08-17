package com.thermetery.sanskritkeyboards.translit

/**
 * A stateful filter sitting between a key press and the editor, for keyboards
 * with a modifier key that changes what the *next* key produces.
 */
interface KeyPreprocessor {

    /**
     * Transform one key insert. Returning null swallows it — which is what a
     * modifier key does, since pressing it types nothing by itself.
     */
    fun process(input: String): String?

    /** The key that should render as latched right now, if any. */
    val latchedKey: String?

    fun reset()
}

/**
 * The btags (་འདོགས) modifier on the direct Tibetan keyboard.
 *
 * Tibetan stacks are vertical: the top of the stack keeps its base form and
 * everything below it takes a subjoined form. This key is how you say "put the
 * next letter *under* the last one" — press it, then the consonant, and the
 * consonant comes out subjoined:
 *
 *     ལ  ྄  ཧ   →  ལྷ
 *     ས  ྄  ཀ  ུ  →  སྐུ
 *
 * It is a modifier rather than a character. Pressing it inserts nothing, and
 * pressing it twice disarms it — the same one-shot behaviour as shift. It is
 * emphatically not a virama: Tibetan has no vowel-killer, and inserting the
 * mark literally would leave a stray glyph in the text rather than stack
 * anything.
 */
class TibetanStacker : KeyPreprocessor {

    var isArmed: Boolean = false
        private set

    override val latchedKey: String?
        get() = if (isArmed) TibetanScript.BTAGS else null

    override fun process(input: String): String? {
        if (input == TibetanScript.BTAGS) {
            // One-shot, and pressing it again cancels.
            isArmed = !isArmed
            return null
        }

        if (!isArmed) return input
        isArmed = false

        // Only consonants have a subjoined form. Anything else — a vowel sign,
        // punctuation, a tsheg — passes through untouched rather than being
        // silently dropped.
        return TibetanScript.subjoinedForm(input) ?: input
    }

    override fun reset() {
        isArmed = false
    }
}
