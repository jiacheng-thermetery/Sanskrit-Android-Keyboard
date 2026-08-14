package com.thermetery.sanskritkeyboards.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

/**
 * The iOS keyboards derive every colour from `UIColor { trait in ... }`
 * closures, so light/dark is resolved per trait collection. Android resolves
 * the same way off the night-mode configuration bit.
 */
internal object Theme {

    fun isDark(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    private fun white(level: Float): Int {
        val v = (level * 255f).toInt().coerceIn(0, 255)
        return Color.rgb(v, v, v)
    }

    /** Keyboard backdrop. */
    fun keyboardBackground(dark: Boolean): Int = if (dark) white(0.13f) else white(0.82f)

    /** Letter and space keys. */
    fun letterKey(dark: Boolean): Int = if (dark) white(0.42f) else Color.WHITE

    /** Shift, backspace, mode switch, return, globe. */
    fun specialKey(dark: Boolean): Int = if (dark) white(0.27f) else white(0.72f)

    fun pressedKey(dark: Boolean): Int = if (dark) white(0.55f) else white(0.85f)

    /** Shift key when latched on. */
    fun activeKey(dark: Boolean): Int =
        if (dark) Color.argb(230, 255, 255, 255) else Color.WHITE

    fun label(dark: Boolean): Int = if (dark) Color.WHITE else Color.BLACK

    fun popoverBackground(dark: Boolean): Int = if (dark) white(0.30f) else white(0.98f)

    /** iOS systemBlue. */
    val popoverHighlight: Int = Color.rgb(0, 122, 255)
}
