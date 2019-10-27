package per.goweii.heartview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt


/**
 * @author CuiZhen
 * @date 2019/10/26
 * QQ: 302833254
 * E-mail: goweii@163.com
 * GitHub: https://github.com/goweii
 */
class HeartView : View {

    private object Holder {
        const val pathWidth = 322F
        const val pathHeight = 284F
        const val ratio: Float = pathWidth / pathHeight
        val heartPath: Path = Path().apply {
            moveTo(0F, -115F)
            cubicTo(-50F, -165F, -161F, -141F, -161F, -44F)
            cubicTo(-161F, 59F, -20F, 141F, 0F, 141F)
            cubicTo(20F, 141F, 161F, 59F, 161F, -44F)
            cubicTo(161F, -141F, 50F, -165F, 0F, -115F)
            close()
        }
        val xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
    }

    enum class SizeBasic(val value: Int) {
        None(0),
        Width(1),
        Height(2);
    }

    enum class DrawMode {
        COLOR, COLOR_EDGE, GRADIENT, COLOR_STROKE
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
        isAntiAlias = true
    }

    private var sizeBasic: SizeBasic = SizeBasic.None
    private var color: Int = Color.RED
    private var edgeColor: Int = Color.RED
    private var centerX = 0F
    private var centerY = 0F
    private var radiusPercent = 1F
    private var strokeColor: Int = Color.TRANSPARENT
    private var strokeWidth: Float = 0F

    private var heartWidth = 0F
    private var heartHeight = 0F
    private var canvasTransX: Float = 0F
    private var canvasTransY: Float = 0F
    private var canvasScale: Float = 1F

    private var solidShader: Shader? = null

    private var measured = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.HeartView, defStyleAttr, 0).run {
            sizeBasic = when (getInt(R.styleable.HeartView_heart_sizeBasic, sizeBasic.value)) {
                SizeBasic.Width.value -> SizeBasic.Width
                SizeBasic.Height.value -> SizeBasic.Height
                else -> SizeBasic.None
            }
            color = getColor(R.styleable.HeartView_heart_color, color)
            edgeColor = getColor(R.styleable.HeartView_heart_edgeColor, edgeColor)
            centerX = getFloat(R.styleable.HeartView_heart_centerX, centerX)
            centerY = getFloat(R.styleable.HeartView_heart_centerY, centerY)
            radiusPercent = getFloat(R.styleable.HeartView_heart_radius, radiusPercent)
            strokeColor = getColor(R.styleable.HeartView_heart_strokeColor, strokeColor)
            strokeWidth = getDimension(R.styleable.HeartView_heart_strokeWidth, strokeWidth)
            recycle()
        }
    }

    fun setColorRes(colorRes: Int) {
        setColor(context.resources.getColor(colorRes))
    }

    fun setColor(colorInt: Int) {
        if (color != colorInt) {
            color = colorInt
            refresh()
        }
    }

    fun setEdgeColorRes(colorRes: Int) {
        setEdgeColor(context.resources.getColor(colorRes))
    }

    fun setEdgeColor(colorInt: Int) {
        if (edgeColor != colorInt) {
            edgeColor = colorInt
            refresh()
        }
    }

    fun setCenterX(centerX: Float) {
        setCenter(centerX, centerY)
    }

    fun setRadiusPercent(radiusPercent: Float) {
        if (this.radiusPercent != radiusPercent) {
            this.radiusPercent = radiusPercent
            refresh()
        }
    }

    fun setCenterY(centerY: Float) {
        setCenter(centerX, centerY)
    }

    fun setCenter(centerX: Float, centerY: Float) {
        var changed = false
        if (this.centerX != centerX) {
            this.centerX = centerX
            changed = true
        }
        if (this.centerY != centerY) {
            this.centerY = centerY
            changed = true
        }
        if (changed) {
            refresh()
        }
    }

    fun setStrokeColorRes(colorRes: Int) {
        setStrokeColor(context.resources.getColor(colorRes))
    }

    fun setStrokeColor(colorInt: Int) {
        if (strokeColor != colorInt) {
            strokeColor = colorInt
            invalidate()
        }
    }

    fun setStrokeWidthDp(width: Float) {
        setStrokeWidth(width, TypedValue.COMPLEX_UNIT_DIP)
    }

    fun setStrokeWidth(width: Float, unit: Int = TypedValue.COMPLEX_UNIT_PX) {
        TypedValue.applyDimension(unit, width, context.resources.displayMetrics).let {
            if (strokeWidth != it) {
                strokeWidth = it
                refresh()
            }
        }
    }

    fun setPaddingDp(padding: Float) {
        setPadding(padding, TypedValue.COMPLEX_UNIT_DIP)
    }

    fun setPadding(padding: Float, unit: Int = TypedValue.COMPLEX_UNIT_PX) {
        TypedValue.applyDimension(unit, padding, context.resources.displayMetrics).roundToInt()
            .let { setPadding(it, it, it, it) }
        refresh()
    }

    fun setPadding(l: Float, t: Float, r: Float, b: Float, unit: Int = TypedValue.COMPLEX_UNIT_PX) {
        context.resources.displayMetrics.run {
            val left = TypedValue.applyDimension(unit, l, this).roundToInt()
            val top = TypedValue.applyDimension(unit, t, this).roundToInt()
            val right = TypedValue.applyDimension(unit, r, this).roundToInt()
            val bottom = TypedValue.applyDimension(unit, b, this).roundToInt()
            setPadding(left, top, right, bottom)
        }
        refresh()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureWidth: Int
        val measureHeight: Int
        when (sizeBasic) {
            SizeBasic.None -> {
                measureWidth = getSize(widthMeasureSpec, suggestedMinimumWidth)
                measureHeight = getSize(heightMeasureSpec, suggestedMinimumHeight)
            }
            SizeBasic.Width -> {
                measureWidth = getSize(widthMeasureSpec, suggestedMinimumWidth)
                measureHeight = (measureWidth / Holder.ratio).toInt()
            }
            SizeBasic.Height -> {
                measureHeight = getSize(heightMeasureSpec, suggestedMinimumHeight)
                measureWidth = (measureHeight * Holder.ratio).toInt()
            }
        }
        setMeasuredDimension(measureWidth, measureHeight)
        if (!measured) {
            measured = true
        }
    }

    private fun getSize(measureSpec: Int, suggestedSize: Int): Int {
        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.UNSPECIFIED -> suggestedSize
            MeasureSpec.AT_MOST -> suggestedSize
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(measureSpec)
            else -> suggestedSize
        }
    }

    override fun getSuggestedMinimumWidth(): Int {
        return getSuggestedMinimumSize().roundToInt() + paddingLeft + paddingRight
    }

    override fun getSuggestedMinimumHeight(): Int {
        return (getSuggestedMinimumSize() * Holder.pathHeight / Holder.pathWidth)
            .roundToInt() + paddingTop + paddingBottom
    }

    private fun getSuggestedMinimumSize(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            24F,
            context.resources.displayMetrics
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        refresh(false)
    }

    private fun refresh(invalidate: Boolean = true) {
        if (!measured) {
            return
        }
        heartWidth = (measuredWidth - paddingLeft - paddingRight).toFloat()
        heartHeight = (measuredHeight - paddingTop - paddingBottom).toFloat()
        canvasTransX = (heartWidth / 2F) + paddingLeft
        canvasTransY = (heartHeight / 2F) + paddingTop
        val sx = (heartWidth - strokeWidth * 2F) / Holder.pathWidth
        val sy = (heartHeight - strokeWidth * 2F) / Holder.pathHeight
        canvasScale = if (abs(sx) < abs(sy)) {
            sx
        } else {
            sy
        }
        if (canvasScale <= 0F) {
            solidShader = null
           return
        }
        solidShader = if (color == edgeColor) {
            null
        } else {
            val solidHalfX = heartWidth / 2F - strokeWidth
            val solidHalfY = heartHeight / 2F - strokeWidth
            val solidCenterX = solidHalfX * centerX
            val solidCenterY = solidHalfY * centerY
            val solidRadiusX = abs(solidCenterX) + solidHalfX
            val solidRadiusY = abs(solidCenterY) + solidHalfY
            val solidRadius = sqrt(solidRadiusX * solidRadiusX + solidRadiusY * solidRadiusY)
            val radius = solidRadius * radiusPercent / canvasScale
            if (radius <= 0F) {
                null
            } else {
                RadialGradient(
                    solidCenterX / canvasScale,
                    solidCenterY / canvasScale,
                    radius,
                    color, edgeColor, Shader.TileMode.CLAMP
                )
            }
        }
        if (invalidate) {
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.run {
            save()
            translate(canvasTransX, canvasTransY)
            val saveUnScale = save()
            scale(canvasScale, canvasScale)
            drawPath(Holder.heartPath, paint.apply {
                style = Paint.Style.FILL
                color = this@HeartView.color
                shader = solidShader
                xfermode = null
            })
            restoreToCount(saveUnScale)
            val paintStroke = this@HeartView.strokeWidth / canvasScale * 2
            if (paintStroke != 0F) {
                val alphaSave = saveLayerAlpha(
                    -canvasTransX, -canvasTransY, canvasTransX, canvasTransY,
                    Color.alpha(this@HeartView.strokeColor), Canvas.ALL_SAVE_FLAG
                )
                scale(canvasScale, canvasScale)
                drawPath(Holder.heartPath, paint.apply {
                    style = Paint.Style.FILL_AND_STROKE
                    setARGB(
                        255,
                        Color.red(this@HeartView.strokeColor),
                        Color.green(this@HeartView.strokeColor),
                        Color.blue(this@HeartView.strokeColor)
                    )
                    strokeWidth = paintStroke
                    shader = null
                    xfermode = null
                })
                drawPath(Holder.heartPath, paint.apply {
                    style = Paint.Style.FILL
                    setARGB(
                        255,
                        Color.red(this@HeartView.strokeColor),
                        Color.green(this@HeartView.strokeColor),
                        Color.blue(this@HeartView.strokeColor)
                    )
                    shader = null
                    xfermode = Holder.xfermode
                })
                restoreToCount(alphaSave)
            }
            restore()
        }
    }
}