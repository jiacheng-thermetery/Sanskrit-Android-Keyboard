package com.thermetery.sanskritkeyboards.layouts

import com.thermetery.sanskritkeyboards.core.KeyDefinition
import com.thermetery.sanskritkeyboards.core.KeyKind

/**
 * Shared key constructors. The geometry (width units) matches the iOS
 * layouts one-for-one so the ported keyboards lay out identically.
 */
internal fun ch(s: String, alts: List<String> = emptyList()): KeyDefinition =
    KeyDefinition(kind = KeyKind.CHARACTER, primary = s, alternates = alts)

internal val shiftKey = KeyDefinition(
    kind = KeyKind.SHIFT, primary = "shift", widthUnits = 1.5f, displayLabel = "⇧"
)

internal val backspaceKey = KeyDefinition(
    kind = KeyKind.BACKSPACE, primary = "backspace", widthUnits = 1.5f, displayLabel = "⌫"
)

/**
 * @param spacePrimary what the space bar inserts. Tibetan writes a tsheg (་)
 *   between syllables rather than a space, so its keyboards override this and
 *   offer a literal space on long-press instead.
 */
internal fun bottomRow(
    modeLabel: String,
    spacePrimary: String = " ",
    spaceLabel: String = "space",
    spaceAlternates: List<String> = emptyList(),
): List<KeyDefinition> = listOf(
    KeyDefinition(
        kind = KeyKind.MODE_SWITCH, primary = modeLabel,
        widthUnits = 1.5f, displayLabel = modeLabel
    ),
    KeyDefinition(
        kind = KeyKind.NEXT_KEYBOARD, primary = "globe",
        widthUnits = 1.0f, displayLabel = "🌐"
    ),
    KeyDefinition(
        kind = KeyKind.SPACE, primary = spacePrimary,
        alternates = spaceAlternates,
        widthUnits = 5.0f, displayLabel = spaceLabel
    ),
    KeyDefinition(
        kind = KeyKind.RETURN, primary = "\n",
        widthUnits = 2.5f, displayLabel = "return"
    ),
)
