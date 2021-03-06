package csiw.android

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView


/**
 * Created by PatelMilan on 23/12/2017.
 * Email : patelmilan2692@gmail.com
 * Mo : 8306306809
 */

class ImageViews @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : android.support.v7.widget.AppCompatImageView(context, attrs, defStyleAttr) {
    var orientation = ORIENTATION_NONE
        private set

    // Enable panorama effect or not
    var isPanoramaModeEnabled: Boolean = false
        private set

    // If true, the image scroll left(top) when the device clockwise rotate along y-axis(x-axis).
    private var mInvertScrollDirection: Boolean = false

    // Image's width and height
    private var mDrawableWidth: Int = 0
    private var mDrawableHeight: Int = 0

    // View's width and height
    private var mWidth: Int = 0
    private var mHeight: Int = 0

    // Image's offset from initial state(center in the view).
    private var mMaxOffset: Float = 0.toFloat()

    // The scroll progress.
    private var mProgress: Float = 0.toFloat()

    // Show scroll bar or not
    private var isScrollbarEnabled: Boolean = false

    // The paint to draw scrollbar
    private var mScrollbarPaint: Paint? = null

    // Observe scroll state
    private var mOnPanoramaScrollListener: OnPanoramaScrollListener? = null

    var isInvertScrollDirection: Boolean
        get() = mInvertScrollDirection
        set(invert) {
            if (mInvertScrollDirection != invert) {
                mInvertScrollDirection = invert
            }
        }

    init {
        super.setScaleType(ImageView.ScaleType.CENTER_CROP)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageViews)
        isPanoramaModeEnabled = typedArray.getBoolean(R.styleable.ImageViews_enablePanoramaMode, true)
        mInvertScrollDirection = typedArray.getBoolean(R.styleable.ImageViews_invertScrollDirection, false)
        isScrollbarEnabled = typedArray.getBoolean(R.styleable.ImageViews_show_scrollbar, true)
        typedArray.recycle()

        if (isScrollbarEnabled) {
            initScrollbarPaint()
        }
    }

    private fun initScrollbarPaint() {
        mScrollbarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mScrollbarPaint!!.color = Color.WHITE
        mScrollbarPaint!!.strokeWidth = dp2px(1.5f)
    }

    fun setGyroscopeObserver(observer: GyroscopeObserver?) {
        observer?.addImageViews(this)
    }

    internal fun updateProgress(progress: Float) {
        if (isPanoramaModeEnabled) {
            mProgress = if (mInvertScrollDirection) -progress else progress
            invalidate()
            if (mOnPanoramaScrollListener != null) {
                mOnPanoramaScrollListener!!.onScrolled(this, -mProgress)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        mWidth = View.MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        mHeight = View.MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom

        if (drawable != null) {
            mDrawableWidth = drawable.intrinsicWidth
            mDrawableHeight = drawable.intrinsicHeight

            if (mDrawableWidth * mHeight > mDrawableHeight * mWidth) {
                orientation = ORIENTATION_HORIZONTAL
                val imgScale = mHeight.toFloat() / mDrawableHeight.toFloat()
                mMaxOffset = Math.abs((mDrawableWidth * imgScale - mWidth) * 0.5f)
            } else if (mDrawableWidth * mHeight < mDrawableHeight * mWidth) {
                orientation = ORIENTATION_VERTICAL
                val imgScale = mWidth.toFloat() / mDrawableWidth.toFloat()
                mMaxOffset = Math.abs((mDrawableHeight * imgScale - mHeight) * 0.5f)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!isPanoramaModeEnabled || drawable == null || isInEditMode) {
            super.onDraw(canvas)
            return
        }

        // Draw image
        if (orientation == ORIENTATION_HORIZONTAL) {
            val currentOffsetX = mMaxOffset * mProgress
            canvas.save()
            canvas.translate(currentOffsetX, 0f)
            super.onDraw(canvas)
            canvas.restore()
        } else if (orientation == ORIENTATION_VERTICAL) {
            val currentOffsetY = mMaxOffset * mProgress
            canvas.save()
            canvas.translate(0f, currentOffsetY)
            super.onDraw(canvas)
            canvas.restore()
        }

        // Draw scrollbar
        if (isScrollbarEnabled) {
            when (orientation) {
                ORIENTATION_HORIZONTAL -> {
                    val barBgWidth = mWidth * 0.9f
                    val barWidth = barBgWidth * mWidth / mDrawableWidth

                    val barBgStartX = (mWidth - barBgWidth) / 2
                    val barBgEndX = barBgStartX + barBgWidth
                    val barStartX = barBgStartX + (barBgWidth - barWidth) / 2 * (1 - mProgress)
                    val barEndX = barStartX + barWidth
                    val barY = mHeight * 0.95f

                    mScrollbarPaint!!.alpha = 100
                    canvas.drawLine(barBgStartX, barY, barBgEndX, barY, mScrollbarPaint!!)
                    mScrollbarPaint!!.alpha = 255
                    canvas.drawLine(barStartX, barY, barEndX, barY, mScrollbarPaint!!)
                }
                ORIENTATION_VERTICAL -> {
                    val barBgHeight = mHeight * 0.9f
                    val barHeight = barBgHeight * mHeight / mDrawableHeight

                    val barBgStartY = (mHeight - barBgHeight) / 2
                    val barBgEndY = barBgStartY + barBgHeight
                    val barStartY = barBgStartY + (barBgHeight - barHeight) / 2 * (1 - mProgress)
                    val barEndY = barStartY + barHeight
                    val barX = mWidth * 0.95f

                    mScrollbarPaint!!.alpha = 100
                    canvas.drawLine(barX, barBgStartY, barX, barBgEndY, mScrollbarPaint!!)
                    mScrollbarPaint!!.alpha = 255
                    canvas.drawLine(barX, barStartY, barX, barEndY, mScrollbarPaint!!)
                }
            }
        }
    }

    private fun dp2px(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics)
    }

    fun setEnablePanoramaMode(enable: Boolean) {
        isPanoramaModeEnabled = enable
    }

    fun setEnableScrollbar(enable: Boolean) {
        if (isScrollbarEnabled != enable) {
            isScrollbarEnabled = enable
            if (isScrollbarEnabled) {
                initScrollbarPaint()
            } else {
                mScrollbarPaint = null
            }
        }
    }

    override fun setScaleType(scaleType: ImageView.ScaleType) {
        /*
         * Do nothing because PanoramaImageView only
         * supports {@link scaleType.CENTER_CROP}
         */
    }

    fun setOnPanoramaScrollListener(listener: OnPanoramaScrollListener) {
        mOnPanoramaScrollListener = listener
    }

    /**
     * Interface definition for a callback to be invoked when the image is scrolling
     */
    interface OnPanoramaScrollListener {
        /**
         * Call when the image is scrolling
         *
         * @param view           the panoramaImageView shows the image
         * @param offsetProgress value between (-1, 1) indicating the offset progress.
         * -1 means the image scrolls to show its left(top) bound,
         * 1 means the image scrolls to show its right(bottom) bound.
         */
        fun onScrolled(view: ImageViews, offsetProgress: Float)
    }

    companion object {

        // Image's scroll orientation
        val ORIENTATION_NONE: Byte = -1
        val ORIENTATION_HORIZONTAL: Byte = 0
        val ORIENTATION_VERTICAL: Byte = 1
    }
}