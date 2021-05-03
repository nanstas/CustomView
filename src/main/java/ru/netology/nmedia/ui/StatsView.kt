package ru.netology.nmedia.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import kotlinx.coroutines.delay
import okhttp3.internal.wait
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        private const val INITIAL_ANGLE = -90F
    }

    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    private var valueSum = 0F
    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            colors = listOf(
                    getColor(
                            R.styleable.StatsView_color1,
                            randomColor()
                    ),
                    getColor(
                            R.styleable.StatsView_color2,
                            randomColor()
                    ),
                    getColor(
                            R.styleable.StatsView_color3,
                            randomColor()
                    ),
                    getColor(
                            R.styleable.StatsView_color4,
                            randomColor()
                    ),
                    getColor(
                            R.styleable.StatsView_color5,
                            randomColor()
                    ),
                    getColor(
                            R.styleable.StatsView_color6,
                            randomColor()
                    ),
                    getColor(
                            R.styleable.StatsView_color7,
                            randomColor()
                    ),
                    getColor(
                            R.styleable.StatsView_color8,
                            randomColor()
                    ),
                    getColor(
                            R.styleable.StatsView_color9,
                            randomColor()
                    ),
                    getColor(
                            R.styleable.StatsView_color10,
                            randomColor()
                    )
            )
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            valueSum = value.sum()
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
                center.x - radius, center.y - radius,
                center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty() || progress == 0F) {
            return
        }
        var startFrom = INITIAL_ANGLE
        val maxAngle = 360 * progress

        for ((index, datum) in data.withIndex()) {
            val angle = if (valueSum > 100F) {
                360F * (datum / valueSum)
            } else {
                datum * 3.6F
            }

            if (startFrom - INITIAL_ANGLE + angle > maxAngle) {
                drawData(
                        index = index,
                        canvas = canvas,
                        startFrom = startFrom,
                        sweepAngle = maxAngle - startFrom + INITIAL_ANGLE
                )
                return
            }
            drawData(
                    index = index,
                    canvas = canvas,
                    startFrom = startFrom,
                    sweepAngle = angle
            )
            startFrom += angle
        }

        canvas.drawText(
                "%.2f%%".format(100F),
                center.x,
                center.y + textPaint.textSize / 4,
                textPaint,
        )
    }

    private fun drawData(
            index: Int,
            canvas: Canvas,
            startFrom: Float,
            sweepAngle: Float,
    ) {
        paint.color = colors.getOrNull(index) ?: randomColor()
        canvas.drawArc(oval, startFrom, sweepAngle, false, paint)
        paint.color = colors[0]
        canvas.drawArc(oval, INITIAL_ANGLE, 1F, false, paint)
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 4000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}