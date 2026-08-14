package com.thermetery.sanskritkeyboards.ime

import com.thermetery.sanskritkeyboards.core.KeyboardLayout
import com.thermetery.sanskritkeyboards.layouts.HkLayout
import com.thermetery.sanskritkeyboards.layouts.IastLayout
import com.thermetery.sanskritkeyboards.layouts.VelthuisLayout
import com.thermetery.sanskritkeyboards.translit.HkToDevanagari
import com.thermetery.sanskritkeyboards.translit.HkToIast
import com.thermetery.sanskritkeyboards.translit.IastToDevanagari
import com.thermetery.sanskritkeyboards.translit.Transliterator
import com.thermetery.sanskritkeyboards.translit.VelthuisToDevanagari
import com.thermetery.sanskritkeyboards.translit.VelthuisToIast

/** IAST — QWERTY with the diacritics on long-press. No transliteration. */
class IastKeyboardService : SanskritInputMethodService() {
    override val keyboardLayout: KeyboardLayout = IastLayout
    override val reservesPopoverHeadroom: Boolean = true
}

/** HK → IAST — type Harvard-Kyoto, see IAST. */
class HkIastKeyboardService : SanskritInputMethodService() {
    override val keyboardLayout: KeyboardLayout = HkLayout
    override val scheme: Transliterator = HkToIast
}

/** HK → Devanāgarī — type Harvard-Kyoto, see Devanāgarī. */
class HkDevanagariKeyboardService : SanskritInputMethodService() {
    override val keyboardLayout: KeyboardLayout = HkLayout
    override val scheme: Transliterator = HkToDevanagari
}

/** IAST → Devanāgarī — the IAST popover layout, Devanāgarī output. */
class IastDevanagariKeyboardService : SanskritInputMethodService() {
    override val keyboardLayout: KeyboardLayout = IastLayout
    override val scheme: Transliterator = IastToDevanagari
    override val reservesPopoverHeadroom: Boolean = true
}

/** Velthuis → IAST — type `.r "s ~n aa`, see IAST. */
class VelthuisIastKeyboardService : SanskritInputMethodService() {
    override val keyboardLayout: KeyboardLayout = VelthuisLayout
    override val scheme: Transliterator = VelthuisToIast
    override val reservesPopoverHeadroom: Boolean = true
}

/** Velthuis → Devanāgarī — same input, Devanāgarī output. */
class VelthuisDevanagariKeyboardService : SanskritInputMethodService() {
    override val keyboardLayout: KeyboardLayout = VelthuisLayout
    override val scheme: Transliterator = VelthuisToDevanagari
    override val reservesPopoverHeadroom: Boolean = true
}
