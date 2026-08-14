package com.thermetery.sanskritkeyboards.translit

/**
 * A live, buffered transliteration scheme.
 *
 * The model is the one used by the iOS keyboards (and by macOS's Tibetan-Wylie
 * input): every keystroke is appended to a pending Latin buffer and the *whole*
 * buffer is re-transliterated from scratch. There is no incremental state to
 * get out of sync — [transliterate] is a pure function of the buffer.
 */
interface Transliterator {

    /** Re-render the entire pending buffer. Pure. */
    fun transliterate(input: String): String

    /**
     * Whether [input] extends the pending buffer. Anything else (space,
     * digits, punctuation, return) commits the buffer and passes through.
     */
    fun acceptsIntoBuffer(input: String): Boolean

    /** Normalization applied as input is appended to the buffer. */
    fun normalizeForBuffer(input: String): String = input
}

/**
 * What the input method should do with the editor after a keystroke.
 *
 * The iOS version has no composing-text API, so it tracks how many Unicode
 * scalars it previously inserted and issues that many `deleteBackward()` calls
 * before re-inserting. Android has a first-class composing region, which is
 * exactly the right primitive for this: [Compose] replaces the composing text
 * wholesale, so no delete-counting is needed and the editor keeps correct
 * cursor/selection state throughout.
 */
sealed class InputResult {
    /** Replace the composing region with [text] (may be empty to clear it). */
    data class Compose(val text: String) : InputResult()

    /** Finish any composing region, then commit [text] literally. */
    data class Commit(val text: String) : InputResult()
}

/**
 * Holds the pending buffer for one editing session and turns keystrokes into
 * [InputResult]s. One instance per input method service.
 */
class TransliterationSession(private val scheme: Transliterator) {

    var pendingInput: String = ""
        private set

    /** Handle one insert (a key tap, or a whole popover-committed bigram). */
    fun process(input: String): InputResult {
        if (input.isEmpty() || !scheme.acceptsIntoBuffer(input)) {
            pendingInput = ""
            return InputResult.Commit(input)
        }
        pendingInput += scheme.normalizeForBuffer(input)
        return InputResult.Compose(scheme.transliterate(pendingInput))
    }

    /**
     * Handle a backspace. Returns null when there is no pending buffer, which
     * means the caller should perform a normal delete on the editor.
     *
     * Backspace removes one *input* character at a time and re-renders, so
     * deleting through `kRSNa` walks back `कृष्ण` → `कृष्` → `कृ` → `क्` …
     */
    fun processBackspace(): InputResult? {
        if (pendingInput.isEmpty()) return null
        pendingInput = pendingInput.dropLast(1)
        return InputResult.Compose(scheme.transliterate(pendingInput))
    }

    fun reset() {
        pendingInput = ""
    }
}

/**
 * Greedy longest-match tokenizer shared by every scheme: at each position take
 * the longest token in [tokens] that matches, else pass the character through.
 */
internal fun tokenize(s: String, tokens: Set<String>, maxTokenLen: Int): List<String> {
    val out = ArrayList<String>()
    var i = 0
    while (i < s.length) {
        var matched = false
        val upper = minOf(maxTokenLen, s.length - i)
        for (length in upper downTo 1) {
            val candidate = s.substring(i, i + length)
            if (candidate in tokens) {
                out.add(candidate)
                i += length
                matched = true
                break
            }
        }
        if (!matched) {
            out.add(s[i].toString())
            i += 1
        }
    }
    return out
}

/**
 * Greedy longest-match substitution — the flat form used by the two
 * Latin-output schemes (HK → IAST, Velthuis → IAST). Unlisted characters pass
 * through unchanged.
 */
internal fun substituteGreedily(s: String, rules: Map<String, String>, maxRuleLen: Int): String {
    val out = StringBuilder()
    var i = 0
    while (i < s.length) {
        var matched = false
        val upper = minOf(maxRuleLen, s.length - i)
        for (length in upper downTo 1) {
            val candidate = s.substring(i, i + length)
            val replacement = rules[candidate]
            if (replacement != null) {
                out.append(replacement)
                i += length
                matched = true
                break
            }
        }
        if (!matched) {
            out.append(s[i])
            i += 1
        }
    }
    return out.toString()
}

/** True when [s] is a single ASCII letter — the HK buffer alphabet. */
internal fun isAsciiLetter(c: Char): Boolean =
    (c in 'A'..'Z') || (c in 'a'..'z')
