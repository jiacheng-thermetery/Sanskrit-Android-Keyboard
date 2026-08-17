package com.thermetery.sanskritkeyboards.translit

/**
 * Live Wylie → Tibetan transliterator.
 *
 * Wylie writes every syllable with an explicit vowel (`a` included), which is
 * what makes parsing tractable: the vowel splits a syllable into the stack that
 * precedes it and the suffixes that follow. `bsgrubs` is unambiguous once you
 * see the `u` — b·s·g·r before it, b·s after.
 *
 * Where a reading is genuinely ambiguous the orthographic tables decide it. In
 * `bkra`, `b` cannot cap `k` (no such superscript) but `r` can hang beneath it,
 * so `b` must be the prefix: བཀྲ. In `sgra`, `s` *can* cap `g`, so it does: སྒྲ.
 *
 * The buffer holds one syllable, because the space key emits a tsheg (་) which
 * is not a Wylie letter and therefore commits.
 */
object WylieToTibetan : Transliterator {

    private val tokens: Set<String> =
        TibetanScript.consonants.keys + TibetanScript.vowels.keys

    private val maxTokenLen: Int = tokens.maxOf { it.length }

    private fun isVowel(token: String): Boolean = token in TibetanScript.vowels

    override fun transliterate(input: String): String {
        val groups = groupExplicitStacks(tokenize(input, tokens, maxTokenLen))
        val out = StringBuilder()
        val plain = ArrayList<String>()
        var i = 0

        fun flushPlain() {
            if (plain.isNotEmpty()) {
                out.append(renderPlain(plain))
                plain.clear()
            }
        }

        while (i < groups.size) {
            val g = groups[i]
            if (g.size > 1) {
                // An explicit stack: first letter on top in base form, the
                // rest subjoined beneath, outside the native syllable rules.
                flushPlain()
                out.append(TibetanScript.consonants[g[0]] ?: g[0])
                for (sub in g.drop(1)) out.append(TibetanScript.subjoined(sub) ?: sub)
                // A vowel directly after the stack attaches to it.
                val next = groups.getOrNull(i + 1)
                if (next != null && next.size == 1 && next[0] in TibetanScript.vowels) {
                    out.append(TibetanScript.vowels[next[0]] ?: "")
                    i++
                }
            } else {
                plain.add(g[0])
            }
            i++
        }
        flushPlain()
        return out.toString()
    }

    /**
     * EWTS writes non-native stacks with `+`: `pad+ma` is པ, then ད with མ
     * subjoined — པདྨ. Native words never need it, so a plus simply glues its
     * neighbours into one explicit stack and everything else flows through the
     * ordinary syllable parser.
     */
    private fun groupExplicitStacks(ts: List<String>): List<List<String>> {
        val groups = ArrayList<MutableList<String>>()
        var i = 0
        while (i < ts.size) {
            val t = ts[i]
            val prev = groups.lastOrNull()
            if (t == "+" && prev != null &&
                prev.all { it in TibetanScript.consonants } &&
                i + 1 < ts.size && ts[i + 1] in TibetanScript.consonants
            ) {
                prev.add(ts[i + 1])
                i += 2
            } else {
                groups.add(mutableListOf(t))
                i += 1
            }
        }
        return groups
    }

    /** The implicit-orthography parser: everything without a `+`. */
    private fun renderPlain(ts: List<String>): String {
        val out = StringBuilder()
        var i = 0

        while (i < ts.size) {
            // The onset runs up to the first vowel.
            val onsetStart = i
            while (i < ts.size && !isVowel(ts[i])) i++
            val onset = ts.subList(onsetStart, i).toList()

            if (i >= ts.size) {
                // No vowel yet — the user is mid-syllable. Render the stack so
                // far so the live update still shows something sensible.
                if (onset.isNotEmpty()) out.append(buildSyllable(onset, null, emptyList()).render())
                break
            }

            val vowel = ts[i]
            i++

            // Consonants between this vowel and the next one.
            val runStart = i
            while (i < ts.size && !isVowel(ts[i])) i++
            val run = ts.subList(runStart, i).toList()

            // A syllable takes at most a suffix and a post-suffix. If another
            // vowel follows, at least one consonant must be left to carry it.
            val moreFollows = i < ts.size
            val codaCount =
                if (moreFollows) minOf(2, maxOf(0, run.size - 1)) else minOf(2, run.size)
            val coda = run.subList(0, codaCount)

            out.append(buildSyllable(onset, vowel, coda).render())

            // Whatever is left of the run begins the next syllable.
            i = runStart + codaCount
        }
        return out.toString()
    }

    /** Assemble one syllable from its onset, vowel and coda. */
    internal fun buildSyllable(
        onset: List<String>,
        vowel: String?,
        coda: List<String>,
    ): TibetanSyllable {
        val stack = parseOnset(onset)
        return TibetanSyllable(
            prefix = stack.prefix,
            superscript = stack.superscript,
            root = stack.root,
            subscripts = stack.subscripts,
            // `a` is inherent, so it marks nothing.
            vowel = vowel?.takeIf { it != "a" },
            suffix = coda.getOrNull(0),
            postSuffix = coda.getOrNull(1),
        )
    }

    private data class Stack(
        val prefix: String? = null,
        val superscript: String? = null,
        val root: String,
        val subscripts: List<String> = emptyList(),
    )

    /**
     * Work out which consonant is the root and what hangs off it.
     *
     * The orders below are not arbitrary: a reading that the superscript and
     * subscript tables permit always beats one that needs a prefix, because a
     * prefix is what is left when nothing may stack.
     */
    private fun parseOnset(cs: List<String>): Stack = when (cs.size) {
        // A vowel with no consonant rides the carrier letter ཨ.
        0 -> Stack(root = "a")

        1 -> Stack(root = cs[0])

        2 -> {
            val (a, b) = cs
            when {
                TibetanScript.canSuperscribe(a, b) -> Stack(superscript = a, root = b)
                TibetanScript.canSubscribe(b, a) -> Stack(root = a, subscripts = listOf(b))
                a in TibetanScript.prefixes -> Stack(prefix = a, root = b)
                else -> Stack(root = a, subscripts = listOf(b))
            }
        }

        3 -> {
            val (a, b, c) = cs
            when {
                a in TibetanScript.prefixes && TibetanScript.canSuperscribe(b, c) ->
                    Stack(prefix = a, superscript = b, root = c)

                TibetanScript.canSuperscribe(a, b) && TibetanScript.canSubscribe(c, b) ->
                    Stack(superscript = a, root = b, subscripts = listOf(c))

                // Two subscripts on one root, as in grwa (གྲྭ). This has to be
                // tried before the prefix reading, or `g` would be taken as a
                // prefix on `rwa` and give གརྭ.
                TibetanScript.canSubscribe(b, a) && TibetanScript.canSubscribe(c, a) ->
                    Stack(root = a, subscripts = listOf(b, c))

                a in TibetanScript.prefixes && TibetanScript.canSubscribe(c, b) ->
                    Stack(prefix = a, root = b, subscripts = listOf(c))

                else -> Stack(root = a, subscripts = listOf(b, c))
            }
        }

        // Four is the maximum: prefix + superscript + root + subscript.
        else -> {
            val a = cs[0]
            val b = cs[1]
            val c = cs[2]
            when {
                a in TibetanScript.prefixes && TibetanScript.canSuperscribe(b, c) ->
                    Stack(prefix = a, superscript = b, root = c, subscripts = cs.drop(3))

                TibetanScript.canSuperscribe(a, b) ->
                    Stack(superscript = a, root = b, subscripts = cs.drop(2))

                else -> Stack(prefix = a, root = b, subscripts = cs.drop(2))
            }
        }
    }

    /**
     * Wylie letters, the a-chung apostrophe and the EWTS `+` extend the
     * buffer. The tsheg from the space key is not a Wylie letter, so it
     * commits — which is exactly the syllable boundary Tibetan wants.
     */
    override fun acceptsIntoBuffer(input: String): Boolean {
        if (input.isEmpty()) return false
        return input.all { isAsciiLetter(it) || it == '\'' || it == '+' }
    }

    /** Case is significant: `T`/`Sh` are the Sanskrit retroflexes, not `t`/`sh`. */
    override fun normalizeForBuffer(input: String): String = input
}
