package com.example.videostreaming

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CustomDrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var paint: Paint = Paint().apply {
        color = Color.GREEN  // Default color
        strokeWidth = 5f    // Default stroke width
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private var path: Path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                // Touch released, do any required actions
            }
            else -> return false
        }
        // Redraw the view
        invalidate()
        return true
    }

    fun setColor(color: Int) {
        paint.color = color
    }

    fun setStrokeWidth(width: Float) {
        paint.strokeWidth = width
    }

    fun clear() {
        path.reset()
        invalidate()
    }
}
