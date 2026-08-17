package com.thermetery.sanskritkeyboards

import android.app.Activity
import android.graphics.PointF
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import com.thermetery.sanskritkeyboards.core.KeyboardLayout
import com.thermetery.sanskritkeyboards.layouts.HkLayout
import com.thermetery.sanskritkeyboards.layouts.IastLayout
import com.thermetery.sanskritkeyboards.layouts.VelthuisLayout
import com.thermetery.sanskritkeyboards.ui.KeyButton
import com.thermetery.sanskritkeyboards.ui.KeyboardView
import com.thermetery.sanskritkeyboards.ui.KeyboardViewDelegate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Exercises the view layer on the JVM. These catch the failures a pure logic
 * test cannot — keys laid out off-screen, popovers anchored out of bounds, a
 * tap that never reaches the delegate.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KeyboardViewTest {

    private val width = 1080
    private val height = 900

    private class RecordingDelegate : KeyboardViewDelegate {
        val inserted = mutableListOf<String>()
        val spaceBars = mutableListOf<String>()
        var deletes = 0
        var advances = 0
        var pickerRequests = 0
        override fun onInsertText(view: KeyboardView, text: String) { inserted += text }
        override fun onSpaceBar(view: KeyboardView, primary: String) { spaceBars += primary }
        override fun onDeleteBackward(view: KeyboardView) { deletes++ }
        override fun onAdvanceToNextInputMode(view: KeyboardView) { advances++ }
        override fun onShowInputMethodPicker(view: KeyboardView) { pickerRequests++ }
    }

    private fun build(
        layout: KeyboardLayout,
        headroom: Boolean = false,
    ): Pair<KeyboardView, RecordingDelegate> {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val view = KeyboardView(activity, layout, headroom)
        val delegate = RecordingDelegate()
        view.delegate = delegate
        view.desiredHeightPx = height
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, width, height)
        return view to delegate
    }

    private fun keys(view: KeyboardView): List<KeyButton> =
        (0 until view.childCount).mapNotNull { view.getChildAt(it) as? KeyButton }

    private fun tap(key: KeyButton) {
        val cx = key.width / 2f
        val cy = key.height / 2f
        key.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, cx, cy, 0))
        key.onTouchEvent(MotionEvent.obtain(0, 10, MotionEvent.ACTION_UP, cx, cy, 0))
    }

    @Test
    fun everyLayoutBuildsAndLaysOutInsideItsBounds() {
        for ((name, layout, headroom) in listOf(
            Triple("IAST", IastLayout, true),
            Triple("HK", HkLayout, false),
            Triple("Velthuis", VelthuisLayout, true),
            Triple("Wylie", com.thermetery.sanskritkeyboards.layouts.WylieLayout, false),
            // Five rows rather than four — the one most likely to overflow.
            Triple("Tibetan", com.thermetery.sanskritkeyboards.layouts.TibetanLayout, true),
        )) {
            val (view, _) = build(layout, headroom)
            val all = keys(view)
            assertTrue("$name produced no keys", all.isNotEmpty())
            for (k in all) {
                assertTrue("$name key '${k.definition.label}' has no width", k.width > 0)
                assertTrue("$name key '${k.definition.label}' has no height", k.height > 0)
                assertTrue("$name key '${k.definition.label}' off left edge", k.left >= 0)
                assertTrue(
                    "$name key '${k.definition.label}' off right edge (${k.right} > $width)",
                    k.right <= width
                )
                assertTrue("$name key '${k.definition.label}' off bottom", k.bottom <= height)
            }
        }
    }

    @Test
    fun keysDoNotOverlapWithinARow() {
        val (view, _) = build(IastLayout, headroom = true)
        val rows = keys(view).groupBy { it.top }
        for ((_, row) in rows) {
            val sorted = row.sortedBy { it.left }
            for (i in 0 until sorted.size - 1) {
                assertTrue(
                    "keys '${sorted[i].definition.label}' and '${sorted[i + 1].definition.label}' overlap",
                    sorted[i].right <= sorted[i + 1].left
                )
            }
        }
    }

    @Test
    fun tappingALetterKeyReachesTheDelegate() {
        val (view, delegate) = build(HkLayout)
        val q = keys(view).first { it.definition.primary == "q" }
        tap(q)
        assertEquals(listOf("q"), delegate.inserted)
    }

    @Test
    fun spaceReturnGlobeAndBackspaceAllRoute() {
        val (view, delegate) = build(HkLayout)
        val all = keys(view)
        tap(all.first { it.definition.kind == com.thermetery.sanskritkeyboards.core.KeyKind.SPACE })
        tap(all.first { it.definition.kind == com.thermetery.sanskritkeyboards.core.KeyKind.RETURN })
        tap(all.first { it.definition.kind == com.thermetery.sanskritkeyboards.core.KeyKind.NEXT_KEYBOARD })
        tap(all.first { it.definition.kind == com.thermetery.sanskritkeyboards.core.KeyKind.BACKSPACE })
        assertEquals(listOf("\n"), delegate.inserted)
        // The space bar reports through its own channel, carrying its primary.
        assertEquals(listOf(" "), delegate.spaceBars)
        assertEquals(1, delegate.advances)
        // Backspace fires on touch-down, so exactly one delete for one tap.
        assertEquals(1, delegate.deletes)
    }

    @Test
    fun shiftSwapsTheLayoutToUppercaseAndFallsBackAfterOneLetter() {
        val (view, delegate) = build(HkLayout)
        tap(keys(view).first { it.definition.kind == com.thermetery.sanskritkeyboards.core.KeyKind.SHIFT })

        val upperQ = keys(view).firstOrNull { it.definition.primary == "Q" }
        assertTrue("shift did not switch to uppercase", upperQ != null)

        tap(upperQ!!)
        assertEquals(listOf("Q"), delegate.inserted)
        // Shift is one-shot: back to lowercase for the next key.
        assertTrue(
            "shift did not reset after one letter",
            keys(view).any { it.definition.primary == "q" }
        )
    }

    @Test
    fun modeSwitchTogglesToDigitsAndBack() {
        val (view, _) = build(HkLayout)
        tap(keys(view).first { it.definition.kind == com.thermetery.sanskritkeyboards.core.KeyKind.MODE_SWITCH })
        assertTrue("123 did not reveal digits", keys(view).any { it.definition.primary == "1" })
        tap(keys(view).first { it.definition.kind == com.thermetery.sanskritkeyboards.core.KeyKind.MODE_SWITCH })
        assertTrue("ABC did not restore letters", keys(view).any { it.definition.primary == "q" })
    }

    /** Press and hold without releasing, letting the long-press timer fire. */
    private fun pressAndHold(key: KeyButton) {
        key.onTouchEvent(
            MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, key.width / 2f, key.height / 2f, 0)
        )
        shadowOf(Looper.getMainLooper()).idleFor(
            java.time.Duration.ofMillis(500)
        )
    }

    /**
     * The package ships six input methods, so a globe tap normally rotates
     * within them. Long-pressing must open the system picker — otherwise there
     * is no way to reach a keyboard outside this package.
     */
    @Test
    fun longPressingTheGlobeOpensTheInputMethodPicker() {
        val (view, delegate) = build(HkLayout)
        val globe = keys(view)
            .first { it.definition.kind == com.thermetery.sanskritkeyboards.core.KeyKind.NEXT_KEYBOARD }

        pressAndHold(globe)
        assertEquals(1, delegate.pickerRequests)

        // Releasing after the long press must NOT also rotate the keyboard.
        globe.onTouchEvent(
            MotionEvent.obtain(0, 30, MotionEvent.ACTION_UP, globe.width / 2f, globe.height / 2f, 0)
        )
        assertEquals("release after long-press also switched keyboards", 0, delegate.advances)
    }

    @Test
    fun aQuickGlobeTapStillRotatesToTheNextInputMethod() {
        val (view, delegate) = build(HkLayout)
        val globe = keys(view)
            .first { it.definition.kind == com.thermetery.sanskritkeyboards.core.KeyKind.NEXT_KEYBOARD }
        tap(globe)
        assertEquals(1, delegate.advances)
        assertEquals("a quick tap should not open the picker", 0, delegate.pickerRequests)
    }

    @Test
    fun longPressPresentsAPopoverInsideTheKeyboardBounds() {
        val (view, _) = build(IastLayout, headroom = true)
        // `n` carries three alternates (ñ ṅ ṇ) — the widest popover on the layout.
        val n = keys(view).first { it.definition.primary == "n" }
        val before = view.childCount
        pressAndHold(n)

        assertEquals("popover was not added", before + 1, view.childCount)
        val popover = view.getChildAt(view.childCount - 1)
        assertTrue("popover has no width", popover.width > 0)
        assertTrue("popover off left edge", popover.left >= 0)
        assertTrue("popover off right edge", popover.right <= width)
        assertTrue("popover pushed above the keyboard", popover.top >= 0)
        assertTrue("popover overlaps its own key", popover.bottom <= n.top)
    }

    @Test
    fun popoverOnTheRightmostAlternateKeyStaysInBounds() {
        // `m` sits far right and has alternates; its popover must be clamped.
        val (view, _) = build(IastLayout, headroom = true)
        val m = keys(view).first { it.definition.primary == "m" }
        pressAndHold(m)
        val popover = view.getChildAt(view.childCount - 1)
        assertTrue("popover off right edge (${popover.right} > $width)", popover.right <= width)
        assertTrue("popover off left edge", popover.left >= 0)
    }

    @Test
    fun draggingAcrossThePopoverCommitsTheHighlightedAlternate() {
        val (view, delegate) = build(IastLayout, headroom = true)
        val s = keys(view).first { it.definition.primary == "s" }
        pressAndHold(s)

        // Popover reads [s, ś, ṣ]; drag onto the last segment and release.
        val popover = view.getChildAt(view.childCount - 1)
        val segWidth = popover.width / 3f
        val targetXInKeyboard = popover.left + segWidth * 2.5f
        val dx = targetXInKeyboard - s.left

        s.onTouchEvent(
            MotionEvent.obtain(0, 20, MotionEvent.ACTION_MOVE, dx, s.height / 2f, 0)
        )
        s.onTouchEvent(
            MotionEvent.obtain(0, 30, MotionEvent.ACTION_UP, dx, s.height / 2f, 0)
        )

        assertEquals(listOf("ṣ"), delegate.inserted)
        assertFalse("popover was not dismissed", view.getChildAt(view.childCount - 1) is
            com.thermetery.sanskritkeyboards.ui.PopoverView)
    }

    @Test
    fun releasingWithoutDraggingCommitsThePrimaryCharacter() {
        val (view, delegate) = build(IastLayout, headroom = true)
        val a = keys(view).first { it.definition.primary == "a" }
        pressAndHold(a)
        a.onTouchEvent(
            MotionEvent.obtain(0, 30, MotionEvent.ACTION_UP, a.width / 2f, a.height / 2f, 0)
        )
        // Finger never moved, so the segment under it — the primary — wins.
        assertEquals(listOf("a"), delegate.inserted)
    }

    @Test
    fun popoverHighlightTracksTheFingerAcrossSegments() {
        val (view, _) = build(VelthuisLayout, headroom = true)
        val r = keys(view).first { it.definition.primary == "r" }
        pressAndHold(r)
        val popover = view.getChildAt(view.childCount - 1)
            as com.thermetery.sanskritkeyboards.ui.PopoverView

        // Velthuis `r` reads [r, .r, .rr].
        assertEquals(listOf("r", ".r", ".rr"), popover.alternates)
        val segWidth = popover.width / 3f
        popover.updateHighlight(PointF(popover.left + segWidth * 1.5f, 0f))
        assertEquals(".r", popover.selectedAlternate)
        popover.updateHighlight(PointF(popover.left + segWidth * 2.5f, 0f))
        assertEquals(".rr", popover.selectedAlternate)
    }

    @Test
    fun latchingMarksExactlyTheLatchedKeysActive() {
        val (view, _) = build(
            com.thermetery.sanskritkeyboards.layouts.TibetanLayout, headroom = true
        )
        val toggle = com.thermetery.sanskritkeyboards.translit.TibetanScript.SANSKRIT_MODE_TOGGLE
        val btags = com.thermetery.sanskritkeyboards.translit.TibetanScript.BTAGS

        view.setLatchedKeys(setOf(toggle))
        assertTrue(
            "the Sanskrit-mode key did not latch",
            keys(view).first { it.definition.primary == toggle }.isActive
        )
        assertFalse(
            "an unrelated key latched",
            keys(view).first { it.definition.primary == btags }.isActive
        )

        // Both a mode and an armed modifier can be lit at once.
        view.setLatchedKeys(setOf(toggle, btags))
        assertTrue(keys(view).first { it.definition.primary == btags }.isActive)

        view.setLatchedKeys(emptySet())
        assertFalse(keys(view).first { it.definition.primary == toggle }.isActive)
    }

    @Test
    fun landscapeGeometryStaysInBounds() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val view = KeyboardView(activity, IastLayout, true)
        view.desiredHeightPx = 500
        val wide = 2000
        view.measure(
            View.MeasureSpec.makeMeasureSpec(wide, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, wide, 500)
        for (k in keys(view)) {
            assertTrue("key '${k.definition.label}' off right edge", k.right <= wide)
            assertTrue("key '${k.definition.label}' has no height", k.height > 0)
        }
    }
}
