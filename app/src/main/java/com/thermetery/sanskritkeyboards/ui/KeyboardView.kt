package com.thermetery.sanskritkeyboards.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.view.View
import android.view.ViewGroup
import com.thermetery.sanskritkeyboards.core.KeyAction
import com.thermetery.sanskritkeyboards.core.KeyboardLayout
import com.thermetery.sanskritkeyboards.core.KeyboardMode
import com.thermetery.sanskritkeyboards.core.KeyKind
import com.thermetery.sanskritkeyboards.core.ShiftState

interface KeyboardViewDelegate {
    fun onInsertText(view: KeyboardView, text: String)
    fun onDeleteBackward(view: KeyboardView)
    fun onAdvanceToNextInputMode(view: KeyboardView)
    fun onShowInputMethodPicker(view: KeyboardView)
}

/**
 * The keyboard surface: builds the key views for the current mode/shift state
 * and lays them out.
 *
 * Geometry is a direct port of the iOS `KeyboardView` — letter rows share one
 * unit width so every letter key is the same size, while the bottom row scales
 * independently so space and return can be wider.
 */
@SuppressLint("ViewConstructor")
class KeyboardView(
    context: Context,
    private val keyboardLayout: KeyboardLayout,
    private val reservesPopoverHeadroom: Boolean = false,
) : ViewGroup(context), KeyButtonDelegate {

    var delegate: KeyboardViewDelegate? = null

    private var mode: KeyboardMode = KeyboardMode.LETTERS
    private var shiftState: ShiftState = ShiftState.OFF

    private var keyButtons: List<List<KeyButton>> = emptyList()
    private var popover: PopoverView? = null
    private var popoverOriginKey: KeyButton? = null

    private val density = resources.displayMetrics.density
    private val keySpacing = 6f * density
    private val rowSpacing = 9f * density
    private val edgeInset = 4f * density
    private val topInset: Float get() = if (reservesPopoverHeadroom) 86f * density else 8f * density
    private val bottomInset = 6f * density

    init {
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(Theme.keyboardBackground(Theme.isDark(context)))
        rebuildKeys()
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration?) {
        super.onConfigurationChanged(newConfig)
        setBackgroundColor(Theme.keyboardBackground(Theme.isDark(context)))
        invalidate()
    }

    // MARK: - Key construction

    private fun rebuildKeys() {
        removeAllViews()
        popover = null
        popoverOriginKey = null

        val layout = keyboardLayout.layout(mode, shiftState != ShiftState.OFF)
        keyButtons = layout.map { row ->
            row.map { def ->
                KeyButton(context, def).also { button ->
                    button.delegate = this
                    if (def.kind == KeyKind.SHIFT) {
                        button.isActive = shiftState != ShiftState.OFF
                    }
                    addView(button)
                }
            }
        }
        requestLayout()
    }

    // MARK: - Layout

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (keyButtons.isEmpty()) return
        val boundsWidth = (r - l).toFloat()
        val boundsHeight = (b - t).toFloat()

        val availableWidth = boundsWidth - 2 * edgeInset
        val availableHeight = boundsHeight - topInset - bottomInset
        val totalRows = keyButtons.size
        val totalRowSpacing = rowSpacing * (totalRows - 1)
        val rowHeight = (availableHeight - totalRowSpacing) / totalRows

        // Letter rows share a unit width so all letters look the same size.
        // The bottom row (mode/space/return) scales independently to fill the
        // width, matching iOS where space and return are wider than letters.
        val firstRow = keyButtons[0]
        val firstRowSpacings = keySpacing * maxOf(firstRow.size - 1, 0)
        val firstRowUnits = firstRow.fold(0f) { acc, k -> acc + k.widthUnits }
        if (firstRowUnits <= 0f) return
        val letterUnitWidth = (availableWidth - firstRowSpacings) / firstRowUnits

        val lastRowIdx = keyButtons.size - 1
        for ((rowIdx, row) in keyButtons.withIndex()) {
            val totalUnits = row.fold(0f) { acc, k -> acc + k.widthUnits }
            val totalSpacings = keySpacing * maxOf(row.size - 1, 0)
            val unitWidth = if (rowIdx == lastRowIdx) {
                (availableWidth - totalSpacings) / totalUnits
            } else {
                letterUnitWidth
            }
            val rowWidth = totalUnits * unitWidth + totalSpacings
            val leadingX = edgeInset + maxOf(0f, (availableWidth - rowWidth) / 2f)
            val y = topInset + rowIdx * (rowHeight + rowSpacing)

            var x = leadingX
            for (key in row) {
                val w = key.widthUnits * unitWidth
                key.layout(
                    x.toInt(), y.toInt(),
                    (x + w).toInt(), (y + rowHeight).toInt()
                )
                x += w + keySpacing
            }
        }

        // Re-anchor a live popover (e.g. rotation mid-press — rare but cheap).
        val originKey = popoverOriginKey
        val pop = popover
        if (originKey != null && pop != null) {
            val anchorRight = originKey.centerX() > boundsWidth / 2
            layoutPopover(pop, originKey, pop.alternates.size, anchorRight, boundsWidth)
        }
    }

    /**
     * Height the input view should occupy, in px. The iOS controllers pin this
     * with a height constraint; on Android the IME window sizes itself to the
     * input view, so we report it from [onMeasure].
     */
    var desiredHeightPx: Int = 0
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = if (desiredHeightPx > 0) desiredHeightPx else MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w, h)

        // Children are positioned explicitly in onLayout; give them a spec so
        // any internal measurement they do is sane.
        for (i in 0 until childCount) {
            getChildAt(i).measure(
                MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST)
            )
        }
    }

    private fun View.centerX(): Float = (left + right) / 2f

    // MARK: - State changes

    private fun toggleShift() {
        shiftState = when (shiftState) {
            ShiftState.OFF -> ShiftState.ON
            ShiftState.ON -> ShiftState.OFF
            ShiftState.CAPS_LOCK -> ShiftState.OFF
        }
        rebuildKeys()
    }

    private fun toggleMode() {
        mode = if (mode == KeyboardMode.LETTERS) KeyboardMode.NUMBERS else KeyboardMode.LETTERS
        // Reset shift when switching out of letters.
        if (mode == KeyboardMode.NUMBERS) shiftState = ShiftState.OFF
        rebuildKeys()
    }

    private fun handleInsertion(text: String) {
        delegate?.onInsertText(this, text)
        if (shiftState == ShiftState.ON) {
            shiftState = ShiftState.OFF
            rebuildKeys()
        }
    }

    // MARK: - Popover

    private fun layoutPopover(
        pop: PopoverView,
        key: KeyButton,
        alternateCount: Int,
        anchorRight: Boolean,
        boundsWidth: Float,
    ) {
        val keyWidth = (key.right - key.left).toFloat()
        val keyHeight = (key.bottom - key.top).toFloat()
        val popWidth = keyWidth * alternateCount
        val popHeight = keyHeight * 1.35f

        var x = if (anchorRight) key.right - popWidth else key.left.toFloat()
        val maxX = boundsWidth - popWidth - edgeInset
        val minX = edgeInset
        if (maxX >= minX) {
            x = x.coerceIn(minX, maxX)
        }
        val y = key.top - popHeight - 4f * density

        pop.layout(
            x.toInt(), y.toInt(),
            (x + popWidth).toInt(), (y + popHeight).toInt()
        )
    }

    private fun presentPopoverInternal(key: KeyButton, alternates: List<String>) {
        dismissPopoverInternal(commit = false)
        // Mirror visually for right-side keys so the primary stays under the finger.
        val anchorRight = key.centerX() > width / 2f
        val displayAlts = if (anchorRight) alternates.reversed() else alternates
        if (displayAlts.isEmpty()) return

        val keyWidth = (key.right - key.left).toFloat()
        val popWidth = keyWidth * displayAlts.size
        var popX = if (anchorRight) key.right - popWidth else key.left.toFloat()
        val maxX = width - popWidth - edgeInset
        if (maxX >= edgeInset) popX = popX.coerceIn(edgeInset, maxX)

        val segWidth = popWidth / displayAlts.size
        val initialIdx = if (segWidth > 0f) {
            ((key.centerX() - popX) / segWidth).toInt().coerceIn(0, displayAlts.size - 1)
        } else {
            0
        }

        val view = PopoverView(context, displayAlts, initialIdx)
        addView(view)
        view.measure(
            MeasureSpec.makeMeasureSpec(popWidth.toInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec((key.bottom - key.top), MeasureSpec.AT_MOST)
        )
        layoutPopover(view, key, displayAlts.size, anchorRight, width.toFloat())
        view.bringToFront()
        popover = view
        popoverOriginKey = key
    }

    private fun dismissPopoverInternal(commit: Boolean): String? {
        val pop = popover ?: return null
        val selected = if (commit) pop.selectedAlternate else null
        removeView(pop)
        popover = null
        popoverOriginKey = null
        return selected
    }

    // MARK: - KeyButtonDelegate

    override fun onKeyAction(button: KeyButton, action: KeyAction) {
        when (action) {
            is KeyAction.Insert -> handleInsertion(action.text)
            KeyAction.Backspace -> delegate?.onDeleteBackward(this)
            KeyAction.Return -> handleInsertion("\n")
            // Not hard-coded to " ": Tibetan's space bar inserts a tsheg.
            KeyAction.Space -> handleInsertion(button.definition.primary)
            KeyAction.NextKeyboard -> delegate?.onAdvanceToNextInputMode(this)
            KeyAction.ShowInputMethodPicker -> delegate?.onShowInputMethodPicker(this)
            KeyAction.Shift -> toggleShift()
            KeyAction.ModeSwitch -> toggleMode()
        }
    }

    override fun presentPopover(button: KeyButton, alternates: List<String>) {
        presentPopoverInternal(button, alternates)
    }

    override fun updatePopoverHighlight(button: KeyButton, pointInKeyboardView: PointF) {
        popover?.updateHighlight(pointInKeyboardView)
    }

    override fun dismissPopover(button: KeyButton, commit: Boolean): String? =
        dismissPopoverInternal(commit)
}
