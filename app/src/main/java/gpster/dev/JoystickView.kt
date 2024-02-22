package gpster.dev

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

class JoystickView(
    context: Context, attrs: AttributeSet
) : View(context, attrs) {
    private val innerCircle : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerCircle : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokeCircle : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var innerRadius : Float = 0f
    private var outerRadius : Float = 0f

    private var centerX : Float = 0f
    private var centerY : Float = 0f
    private var curX : Float = 0f
    private var curY : Float = 0f

    init {
        innerCircle.color = ContextCompat.getColor(context, R.color.blue)
        outerCircle.color = ContextCompat.getColor(context, R.color.gray0)
        strokeCircle.color = ContextCompat.getColor(context, R.color.gray1)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        outerRadius = Math.min(width, height) / 3f
        innerRadius = outerRadius / 2f

        centerX = width / 2f
        centerY = height / 2f
        curX = centerX
        curY = centerY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(centerX, centerY, outerRadius + 4f, strokeCircle)
        canvas.drawCircle(centerX, centerY, outerRadius, outerCircle)
        canvas.drawCircle(curX, curY, innerRadius, innerCircle)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                updateInnerCirclePosition(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                resetInnerCirclePosition()
                invalidate()
            }
        }
        return true
    }

    private fun updateInnerCirclePosition(x: Float, y: Float) {
        val distance = calculateDistance(x, y, centerX, centerY)
        if (distance <= outerRadius - innerRadius) {
            curX = x
            curY = y
        } else {
            // Calculate inner circle position at the edge of the outer circle
            val angle = Math.atan2((y - centerY).toDouble(), (x - centerX).toDouble())
            curX = (centerX + outerRadius * Math.cos(angle)).toFloat()
            curY = (centerY + outerRadius * Math.sin(angle)).toFloat()
        }
    }

    private fun resetInnerCirclePosition() {
        curX = centerX
        curY = centerY
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.sqrt(Math.pow((x1 - x2).toDouble(), 2.0) + Math.pow((y1 - y2).toDouble(), 2.0)).toFloat()
    }
}