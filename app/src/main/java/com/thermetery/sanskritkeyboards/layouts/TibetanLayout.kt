package com.thermetery.sanskritkeyboards.layouts

import com.thermetery.sanskritkeyboards.core.KeyDefinition
import com.thermetery.sanskritkeyboards.core.KeyKind
import com.thermetery.sanskritkeyboards.core.KeyboardLayout
import com.thermetery.sanskritkeyboards.core.KeyboardMode
import com.thermetery.sanskritkeyboards.translit.TibetanScript

/**
 * Direct Tibetan layout, matching the arrangement on the reference keyboard:
 * the letters sit on QWERTY positions phonetically rather than in alphabetical
 * order, so `k` is ཀ, `g` is ག, `m` is མ, and `e u i o` carry the four vowel
 * signs.
 *
 * Both layers are the mapping as supplied. The unshifted layer covers 21 of the
 * thirty gsal byed; shift recovers the other nine (ཝ ཐ ཕ ཨ ཤ ཁ ཞ ཆ ཚ) and adds
 * the Sanskrit retroflexes, the subjoined ྲ ྱ ྭ ླ and the marks ཿ ཾ. A few
 * keys — ྄, ཇ, བ — are the same on both layers.
 *
 * Long-pressing a consonant additionally gives its **subjoined** form, which is
 * how a stack is built by hand: tap ས, then hold ཀ and choose ྐ for སྐ. Those
 * alternates are derived from the base letters rather than listed, so the two
 * cannot drift apart.
 */
object TibetanLayout : KeyboardLayout {

    override fun layout(mode: KeyboardMode, shifted: Boolean): List<List<KeyDefinition>> =
        when (mode) {
            KeyboardMode.LETTERS -> if (shifted) lettersUpper else lettersLower
            KeyboardMode.NUMBERS -> numbers
        }

    /** A consonant key carrying its own subjoined form as the long-press. */
    private fun cons(wylie: String): KeyDefinition {
        val base = TibetanScript.consonants[wylie] ?: wylie
        val sub = TibetanScript.subjoined(wylie)
        return ch(base, if (sub != null) listOf(sub) else emptyList())
    }

    // MARK: - Unshifted, as supplied
    //
    //   q ྄   w ཉ   e ེ   r ར   t ཏ   y ཡ   u ུ   i ི   o ོ   p པ
    //   a འ   s ས   d ད   f ང   g ག   h ཧ   j ཇ   k ཀ   l ལ
    //   z ཟ   x ཛ   c ཅ   v ཙ   b བ   n ན   m མ

    private val lettersLower: List<List<KeyDefinition>> = listOf(
        listOf(
            ch("྄"),        // halanta / srog med
            cons("ny"),      // ཉ
            ch("ེ"),        // e
            cons("r"),       // ར
            cons("t"),       // ཏ
            cons("y"),       // ཡ
            ch("ུ"),        // u
            ch("ི"),        // i
            ch("ོ"),        // o
            cons("p"),       // པ
        ),
        listOf(
            cons("'"),       // འ
            cons("s"),       // ས
            cons("d"),       // ད
            cons("ng"),      // ང
            cons("g"),       // ག
            cons("h"),       // ཧ
            cons("j"),       // ཇ
            // Supplied as ཇ, which duplicated `j` and left ཀ off the keyboard
            // entirely — read as ka.
            cons("k"),       // ཀ
            cons("l"),       // ལ
        ),
        listOf(
            shiftKey,
            cons("z"),       // ཟ
            cons("dz"),      // ཛ
            cons("c"),       // ཅ
            cons("ts"),      // ཙ
            cons("b"),       // བ
            cons("n"),       // ན
            cons("m"),       // མ
            backspaceKey,
        ),
        tibetanBottomRow("༡༢༣"),
    )

    // MARK: - Shifted, as supplied
    //
    // Recovers the nine consonants absent from the unshifted layer
    // (ཝ ཐ ཕ ཨ ཤ ཁ ཞ ཆ ཚ) and adds the Sanskrit retroflexes ཊ ཋ ཌ ཎ ཥ,
    // the subjoined ྲ ྱ ྭ ླ and the marks ཿ ཾ. Together with the unshifted
    // layer that is all thirty gsal byed.
    //
    //   Q ྄   W ཝ   E ཻ   R ྲ   T ཐ   Y ྱ   U ྭ   I ྀ   O ཽ   P ཕ
    //   A ཨ   S ཤ   D ཌ   F ཋ   G ཊ   H ཿ   J ཇ   K ཁ   L ླ
    //   Z ཞ   X ཥ   C ཆ   V ཚ   B བ   N ཎ   M ཾ

    private val lettersUpper: List<List<KeyDefinition>> = listOf(
        listOf(
            ch("྄"),        // halanta, as unshifted
            cons("w"),       // ཝ
            ch("ཻ"),        // ai
            ch("ྲ"),        // ra btags
            cons("th"),      // ཐ
            ch("ྱ"),        // ya btags
            ch("ྭ"),        // wa zur
            ch("ྀ"),        // reversed gigu
            ch("ཽ"),        // au
            cons("ph"),      // ཕ
        ),
        listOf(
            cons("a"),       // ཨ
            cons("sh"),      // ཤ
            cons("D"),       // ཌ
            cons("Th"),      // ཋ
            cons("T"),       // ཊ
            ch("ཿ"),        // visarga
            cons("j"),       // ཇ, as unshifted
            cons("kh"),      // ཁ
            ch("ླ"),        // la btags
        ),
        listOf(
            shiftKey,
            cons("zh"),      // ཞ
            cons("Sh"),      // ཥ
            cons("ch"),      // ཆ
            cons("tsh"),     // ཚ
            cons("b"),       // བ, as unshifted
            cons("N"),       // ཎ
            ch("ཾ"),        // anusvara
            backspaceKey,
        ),
        tibetanBottomRow("༡༢༣"),
    )

    // MARK: - Numbers and symbols, as supplied
    //
    //   ༡༢༣༤༥༦༧༨༩༠
    //   - / : ; ༼ ༽ $ ༕ @ “
    //   ࿂ ༜ ༴ ། ༄

    private val numbers: List<List<KeyDefinition>> = listOf(
        listOf(
            ch("༡", listOf("1")), ch("༢", listOf("2")), ch("༣", listOf("3")),
            ch("༤", listOf("4")), ch("༥", listOf("5")), ch("༦", listOf("6")),
            ch("༧", listOf("7")), ch("༨", listOf("8")), ch("༩", listOf("9")),
            ch("༠", listOf("0")),
        ),
        listOf(
            ch("-"), ch("/"), ch(":"), ch(";"),
            ch("༼"), ch("༽"),
            ch("$"), ch("༕"), ch("@"),
            ch("“", listOf("“", "”")),
        ),
        listOf(
            ch("࿂"),
            ch("༜"),
            ch("༴"),
            ch("།", listOf("།", "༎")),
            ch("༄", listOf("༄", "༅", "༆")),
            backspaceKey,
        ),
        tibetanBottomRow("ཨ་བ"),
    )
}

/**
 * A dedicated tsheg key sits between the space bar and return, as on the
 * reference keyboard — on a direct Tibetan layout the tsheg is a letter you
 * reach for constantly, so the space bar stays a space.
 */
internal fun tibetanBottomRow(modeLabel: String): List<KeyDefinition> = listOf(
    KeyDefinition(
        kind = KeyKind.MODE_SWITCH, primary = modeLabel,
        widthUnits = 1.5f, displayLabel = modeLabel
    ),
    KeyDefinition(
        kind = KeyKind.NEXT_KEYBOARD, primary = "globe",
        widthUnits = 1.0f, displayLabel = "🌐"
    ),
    KeyDefinition(
        kind = KeyKind.SPACE, primary = " ",
        widthUnits = 3.0f, displayLabel = "space"
    ),
    // tsheg, with the shads on long-press
    ch("་", listOf("།", "༎")).copy(widthUnits = 1.0f),
    // Sanskrit-mode toggle: relaxes the native stacking rules for loanwords
    // like པདྨ. The sentinel is swallowed by the preprocessor, and the key
    // stays lit while the mode is on.
    KeyDefinition(
        kind = KeyKind.CHARACTER, primary = TibetanScript.SANSKRIT_MODE_TOGGLE,
        widthUnits = 1.0f, displayLabel = "ཀྵ"
    ),
    KeyDefinition(
        kind = KeyKind.RETURN, primary = "\n",
        widthUnits = 2.5f, displayLabel = "return"
    ),
)
