package com.thermetery.sanskritkeyboards.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.view.View

/**
 * The long-press alternates strip. Segments are laid out left-to-right; the
 * one under the finger is highlighted and is what gets committed on release.
 */
@SuppressLint("ViewConstructor")
class PopoverView(
    context: Context,
    val alternates: List<String>,
    initialIndex: Int,
) : View(context) {

    var highlightedIndex: Int = initialIndex.coerceIn(0, (alternates.size - 1).coerceAtLeast(0))
        private set

    private val density = resources.displayMetrics.density
    private val cornerRadius = 8f * density
    private val segmentCornerRadius = 6f * density

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Theme.popoverHighlight
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(46, 0, 0, 0)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 26f * density
    }

    private val rect = RectF()
    private val segRect = RectF()

    val selectedAlternate: String
        get() = alternates.getOrNull(highlightedIndex) ?: alternates.firstOrNull() ?: ""

    fun updateHighlight(parentSpacePoint: PointF) {
        if (alternates.isEmpty()) return
        val localX = parentSpacePoint.x - x
        val segmentWidth = width.toFloat() / alternates.size
        if (segmentWidth <= 0f) return
        val clamped = (localX / segmentWidth).toInt().coerceIn(0, alternates.size - 1)
        if (clamped != highlightedIndex) {
            highlightedIndex = clamped
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (alternates.isEmpty()) return
        val dark = Theme.isDark(context)

        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(
            rect.left, rect.top + 2f * density, rect.right, rect.bottom + 2f * density,
            cornerRadius, cornerRadius, shadowPaint
        )

        backgroundPaint.color = Theme.popoverBackground(dark)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint)

        val segmentWidth = width.toFloat() / alternates.size
        val fm = textPaint.fontMetrics
        val baseline = height / 2f - (fm.ascent + fm.descent) / 2f

        for ((i, alt) in alternates.withIndex()) {
            val left = i * segmentWidth
            val isOn = i == highlightedIndex
            if (isOn) {
                segRect.set(left + 2f, 2f, left + segmentWidth - 2f, height - 2f)
                canvas.drawRoundRect(
                    segRect, segmentCornerRadius, segmentCornerRadius, highlightPaint
                )
            }
            textPaint.color = if (isOn) Color.WHITE else Theme.label(dark)
            canvas.drawText(alt, left + segmentWidth / 2f, baseline, textPaint)
        }
    }
}
