package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private lateinit var valueAnimator: ValueAnimator

    private var progressPercentage = 0f
    private var buttonText = resources.getString(R.string.download)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Smooth out edges of what is drawn without affecting shape.
        isAntiAlias = true
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)

        typeface = Typeface.create("", Typeface.BOLD)
    }


    // position variable which will be used to draw label and indicator circle position
    private val pointPosition: PointF = PointF(0.0f, 0.0f)
    var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Clicked -> {
                buttonText = resources.getString(R.string.button_loading)
                animateProgress()
            }
            ButtonState.Loading -> {
                buttonText = resources.getString(R.string.button_loading)
                animateProgress()
            }
            ButtonState.Completed -> {
                speedUpProgress()
            }
        }
    }



    private fun animateProgress(
        duration: Long = 3000,
        currentPercentage: Float = 0f,
    ) {
        valueAnimator = ValueAnimator.ofFloat(currentPercentage, 100f).apply {
            this.duration = duration
            addUpdateListener {
                val percentage = animatedValue as Float
                progressPercentage = percentage
                repeatMode = ValueAnimator.REVERSE
                invalidate()
            }
            doOnEnd {
                if (buttonState != ButtonState.Loading) {
                    endProgress()
                } else {
                    reverse()
                }
            }
        }
        valueAnimator.start()
    }

    private fun speedUpProgress() {
        valueAnimator.removeAllListeners()
        valueAnimator.cancel()
        animateProgress(duration = 500, currentPercentage = progressPercentage)
    }

    private fun endProgress() {
        progressPercentage = 0f
        buttonText = resources.getString(R.string.download)
        invalidate()
    }

    private var buttonProgressColor = 0
    private var buttonCircleColor = 0
    private var buttonTextColor = 0

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonProgressColor = getColor(R.styleable.LoadingButton_progress_color, 0)
            buttonCircleColor = getColor(R.styleable.LoadingButton_circle_color, 0)
            buttonTextColor = getColor(R.styleable.LoadingButton_text_color, 0)
        }
    }


    private fun PointF.computeXYForText() {
        x = width / 2f
        y = (height + paint.textSize) / 2f
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawRectProgress(canvas)
        drawCircleProgress(canvas)
        drawText(canvas)
    }

    private fun drawCircleProgress(canvas: Canvas) {
        val textBound = Rect()
        paint.getTextBounds(buttonText, 0, buttonText.length, textBound)
        val arcDiameter = height / 3f
        paint.color = buttonCircleColor
        val left = (width + textBound.width()) / 2f
        val top = (heightSize - arcDiameter) / 2
        var rectangle = RectF(left, top, left + arcDiameter, top + arcDiameter)

        // whole Circle is 360 Degree  so -> 360/100 Changing it to percent-> 3.6 * progress
        canvas.drawArc(rectangle, 0f, 3.6f * progressPercentage, true, paint)

    }

    private fun drawRectProgress(canvas: Canvas) {
        val rect = RectF(0f, 0f, width / 100f * progressPercentage, height.toFloat())
        paint.color = buttonProgressColor
        canvas.drawRect(rect, paint)
    }

    private fun drawText(canvas: Canvas) {
        pointPosition.computeXYForText()
        paint.color = buttonTextColor
        canvas.drawText(buttonText, pointPosition.x, pointPosition.y, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}