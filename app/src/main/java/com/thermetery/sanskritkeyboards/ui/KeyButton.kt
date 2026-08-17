package com.thermetery.sanskritkeyboards.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import com.thermetery.sanskritkeyboards.core.KeyAction
import com.thermetery.sanskritkeyboards.core.KeyDefinition
import com.thermetery.sanskritkeyboards.core.KeyKind

interface KeyButtonDelegate {
    fun onKeyAction(button: KeyButton, action: KeyAction)
    fun presentPopover(button: KeyButton, alternates: List<String>)
    fun updatePopoverHighlight(button: KeyButton, pointInKeyboardView: PointF)
    fun dismissPopover(button: KeyButton, commit: Boolean): String?
}

/**
 * A single key. Draws itself (rounded rect + centred label) and owns its own
 * touch handling, including the long-press popover and backspace auto-repeat.
 */
@SuppressLint("ViewConstructor")
class KeyButton(
    context: Context,
    val definition: KeyDefinition,
) : View(context) {

    var delegate: KeyButtonDelegate? = null

    val widthUnits: Float get() = definition.widthUnits

    var isActive: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private var keyPressed = false
        set(value) {
            field = value
            invalidate()
        }

    private var popoverShowing = false

    private val handler = Handler(Looper.getMainLooper())
    private var alternatesRunnable: Runnable? = null
    private var backspaceRunnable: Runnable? = null
    private var pickerRunnable: Runnable? = null

    /** True once the globe long-press has opened the picker this gesture. */
    private var pickerShown = false

    private val density = resources.displayMetrics.density
    private val cornerRadius = 5f * density

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val rect = RectF()

    init {
        isHapticFeedbackEnabled = false
        textPaint.textSize = preferredTextSize()
    }

    /** Mirrors the iOS `preferredFont()` sizing. */
    private fun preferredTextSize(): Float = when (definition.kind) {
        // Slightly smaller for multi-char labels since they're already compact.
        KeyKind.CHARACTER -> if (definition.primary.length > 1) 16f * density else 22f * density
        KeyKind.SPACE, KeyKind.RETURN, KeyKind.MODE_SWITCH -> 15f * density
        KeyKind.SHIFT, KeyKind.BACKSPACE, KeyKind.NEXT_KEYBOARD -> 18f * density
    }

    private fun baseBackgroundColor(dark: Boolean): Int {
        val isLetterLike =
            definition.kind == KeyKind.CHARACTER || definition.kind == KeyKind.SPACE
        return if (isLetterLike) Theme.letterKey(dark) else Theme.specialKey(dark)
    }

    override fun onDraw(canvas: Canvas) {
        val dark = Theme.isDark(context)
        val inset = 0f
        rect.set(inset, inset, width - inset, height - inset)

        // Subtle drop shadow, matching the iOS key shadow (0.15 alpha, 1pt down).
        shadowPaint.color = android.graphics.Color.argb(38, 0, 0, 0)
        canvas.drawRoundRect(
            rect.left, rect.top + 1f * density, rect.right, rect.bottom + 1f * density,
            cornerRadius, cornerRadius, shadowPaint
        )

        // A latched character key (an armed modifier, a sticky mode like
        // Sanskrit) highlights in blue: the white "active" treatment shift
        // uses is invisible on a character key in light theme, whose resting
        // colour is already white.
        val isLatchedCharacter = isActive && definition.kind == KeyKind.CHARACTER

        backgroundPaint.color = when {
            keyPressed -> Theme.pressedKey(dark)
            isLatchedCharacter -> Theme.popoverHighlight
            isActive -> Theme.activeKey(dark)
            else -> baseBackgroundColor(dark)
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint)

        // Latched keys invert their label: white on the blue highlight,
        // black on shift's white.
        textPaint.color = when {
            isLatchedCharacter && !keyPressed -> android.graphics.Color.WHITE
            isActive && !keyPressed -> android.graphics.Color.BLACK
            else -> Theme.label(dark)
        }

        val label = definition.label
        // Shrink-to-fit, floor at 0.6× like `minimumScaleFactor` on iOS.
        var size = preferredTextSize()
        textPaint.textSize = size
        val available = width - 4f * density
        val measured = textPaint.measureText(label)
        if (measured > available && measured > 0f) {
            size = (size * available / measured).coerceAtLeast(size * 0.6f)
            textPaint.textSize = size
        }

        val fm = textPaint.fontMetrics
        val baseline = height / 2f - (fm.ascent + fm.descent) / 2f
        canvas.drawText(label, width / 2f, baseline, textPaint)
    }

    // MARK: - Touch handling

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                keyPressed = true
                if (definition.kind == KeyKind.BACKSPACE) {
                    delegate?.onKeyAction(this, KeyAction.Backspace)
                    scheduleBackspaceRepeat()
                } else if (definition.kind == KeyKind.NEXT_KEYBOARD) {
                    // Holding the globe opens the system picker, which is the
                    // only way to reach a keyboard outside this package.
                    schedulePickerTimer()
                } else if (definition.alternates.isNotEmpty()) {
                    scheduleAlternatesTimer()
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (popoverShowing) {
                    // Report in keyboard-view space: our own frame origin plus
                    // the touch offset within us.
                    val p = PointF(x + event.x, y + event.y)
                    delegate?.updatePopoverHighlight(this, p)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                cancelAlternatesTimer()
                cancelBackspaceRepeat()
                cancelPickerTimer()
                keyPressed = false

                if (pickerShown) {
                    // The picker already opened on the long press; releasing
                    // must not also switch keyboards.
                    pickerShown = false
                    return true
                }

                if (popoverShowing) {
                    popoverShowing = false
                    val alt = delegate?.dismissPopover(this, commit = true)
                    if (!alt.isNullOrEmpty()) {
                        delegate?.onKeyAction(this, KeyAction.Insert(alt))
                    }
                    return true
                }

                if (definition.kind == KeyKind.BACKSPACE) {
                    return true   // already fired on ACTION_DOWN
                }

                // Fire if the touch ended within a forgiving extension of our bounds.
                val slop = 8f * density
                val inside = event.x >= -slop && event.x <= width + slop &&
                    event.y >= -slop && event.y <= height + slop
                if (inside) fireAction()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                cancelAlternatesTimer()
                cancelBackspaceRepeat()
                cancelPickerTimer()
                pickerShown = false
                keyPressed = false
                if (popoverShowing) {
                    popoverShowing = false
                    delegate?.dismissPopover(this, commit = false)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // MARK: - Timers

    private fun scheduleAlternatesTimer() {
        cancelAlternatesTimer()
        val r = Runnable {
            popoverShowing = true
            val allAlts = listOf(definition.primary) + definition.alternates
            delegate?.presentPopover(this, allAlts)
        }
        alternatesRunnable = r
        handler.postDelayed(r, 400L)
    }

    private fun cancelAlternatesTimer() {
        alternatesRunnable?.let { handler.removeCallbacks(it) }
        alternatesRunnable = null
    }

    private fun schedulePickerTimer() {
        cancelPickerTimer()
        val r = Runnable {
            pickerShown = true
            keyPressed = false
            delegate?.onKeyAction(this, KeyAction.ShowInputMethodPicker)
        }
        pickerRunnable = r
        handler.postDelayed(r, 400L)
    }

    private fun cancelPickerTimer() {
        pickerRunnable?.let { handler.removeCallbacks(it) }
        pickerRunnable = null
    }

    private fun scheduleBackspaceRepeat() {
        cancelBackspaceRepeat()
        val repeat = object : Runnable {
            override fun run() {
                delegate?.onKeyAction(this@KeyButton, KeyAction.Backspace)
                handler.postDelayed(this, 80L)
            }
        }
        val start = Runnable { handler.post(repeat) }
        backspaceRunnable = start
        handler.postDelayed(start, 450L)
        // Keep a handle on the repeating task so cancellation stops both.
        repeatTask = repeat
    }

    private var repeatTask: Runnable? = null

    private fun cancelBackspaceRepeat() {
        backspaceRunnable?.let { handler.removeCallbacks(it) }
        repeatTask?.let { handler.removeCallbacks(it) }
        backspaceRunnable = null
        repeatTask = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAlternatesTimer()
        cancelBackspaceRepeat()
        cancelPickerTimer()
    }

    private fun fireAction() {
        val action = when (definition.kind) {
            KeyKind.CHARACTER -> KeyAction.Insert(definition.primary)
            KeyKind.SPACE -> KeyAction.Space
            KeyKind.RETURN -> KeyAction.Return
            KeyKind.SHIFT -> KeyAction.Shift
            KeyKind.NEXT_KEYBOARD -> KeyAction.NextKeyboard
            KeyKind.MODE_SWITCH -> KeyAction.ModeSwitch
            KeyKind.BACKSPACE -> KeyAction.Backspace
        }
        delegate?.onKeyAction(this, action)
    }
}
