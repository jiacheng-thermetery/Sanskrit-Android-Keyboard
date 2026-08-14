package com.thermetery.sanskritkeyboards.core

enum class KeyKind {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    NEXT_KEYBOARD,
    SPACE,
    RETURN,
    MODE_SWITCH,
}

data class KeyDefinition(
    val kind: KeyKind = KeyKind.CHARACTER,
    val primary: String,
    val alternates: List<String> = emptyList(),
    val widthUnits: Float = 1.0f,
    val displayLabel: String? = null,
) {
    val label: String get() = displayLabel ?: primary
}

sealed class KeyAction {
    data class Insert(val text: String) : KeyAction()
    data object Backspace : KeyAction()
    data object Return : KeyAction()
    data object Space : KeyAction()
    data object NextKeyboard : KeyAction()

    /** Long-press on the globe: open the system input-method picker. */
    data object ShowInputMethodPicker : KeyAction()
    data object Shift : KeyAction()
    data object ModeSwitch : KeyAction()
}

enum class KeyboardMode {
    LETTERS,
    NUMBERS,
}

enum class ShiftState {
    OFF,
    ON,
    CAPS_LOCK,
}

/** A keyboard layout: the rows of keys for each mode / shift combination. */
interface KeyboardLayout {
    fun layout(mode: KeyboardMode, shifted: Boolean): List<List<KeyDefinition>>
}
