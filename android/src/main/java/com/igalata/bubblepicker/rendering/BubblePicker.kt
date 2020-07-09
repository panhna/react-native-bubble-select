package com.igalata.bubblepicker.rendering

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.os.Handler
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import com.igalata.bubblepicker.BubblePickerListener
//import com.igalata.bubblepicker.R
import com.igalata.bubblepicker.adapter.BubblePickerAdapter
import com.igalata.bubblepicker.model.Color
import com.igalata.bubblepicker.model.PickerItem
import com.reactnativebubbleselect.R

/**
 * Created by irinagalata on 1/19/17.
 */
class BubblePicker : GLSurfaceView {

    @ColorInt var background: Int = 0
        set(value) {
            field = value
            renderer.backgroundColor = Color(value)
        }
    @Deprecated(level = DeprecationLevel.WARNING,
            message = "Use BubblePickerAdapter for the view setup instead")
    var items: ArrayList<PickerItem>? = null
        set(value) {
            field = value
            renderer.items = value ?: ArrayList()
        }
    var adapter: BubblePickerAdapter? = null
        set(value) {
            field = value
            if (value != null) {
                renderer.items = ArrayList((0..value.totalCount - 1)
                        .map { value.getItem(it) }.toList())
            }
        }
    var maxSelectedCount: Int? = null
        set(value) {
            renderer.maxSelectedCount = value
        }
    var listener: BubblePickerListener? = null
        set(value) {
            renderer.listener = value
        }
    var bubbleSize = 50
        set(value) {
            if (value in 1..100) {
                renderer.bubbleSize = value
            }
        }
    val selectedItems: List<PickerItem?>
        get() = renderer.selectedItems

    var centerImmediately = false
        set(value) {
            field = value
            renderer.centerImmediately = value
        }

    private val renderer = PickerRenderer(this)
    private var startX = 0f
    private var startY = 0f
    private var previousX = 0f
    private var previousY = 0f

    private var longPressedFlag = false
    private val longPressedHandler = Handler()
    private val longPressed = Runnable {
        kotlin.run {
            longPressedFlag = true;
            onLongPress();
        }
    }
    private val longPressedDuration: Long = 500

    private val gestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(event: MotionEvent) {
            renderer.remove(event.x, event.y)
        }

        override fun onDown(event: MotionEvent): Boolean {
            startX = event.x
            startY = event.y
            previousX = event.x
            previousY = event.y
            return true;
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            if (isClick(event) && !longPressedFlag) {
                renderer.resize(event.x, event.y)
            }
            renderer.release()
            return true;
        }
    });

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setZOrderOnTop(true)
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.RGBA_8888)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        attrs?.let { retrieveAttrubutes(attrs) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                longPressedHandler.removeCallbacks(longPressed);
                if (isSwipe(event)) {
                    renderer.swipe(previousX - event.x, previousY - event.y)
                    previousX = event.x
                    previousY = event.y
                } else {
                    release()
                }
            }
            else -> gestureDetector.onTouchEvent(event);
        }

        return true;
    }

    fun addedItem(position: Int) {
        val mAdapter = adapter ?: return
        val item = mAdapter.getItem(position)
        renderer.addItem(item);
    }

    private fun onLongPress() {
        print("LONG PRESS")
    }

    private fun release() = postDelayed({ renderer.release() }, 0)

    private fun isClick(event: MotionEvent) = Math.abs(event.x - startX) < 20 && Math.abs(event.y - startY) < 20

    private fun isSwipe(event: MotionEvent) = Math.abs(event.x - previousX) > 20 && Math.abs(event.y - previousY) > 20

    private fun retrieveAttrubutes(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.BubblePicker)

        if (array.hasValue(R.styleable.BubblePicker_maxSelectedCount)) {
            maxSelectedCount = array.getInt(R.styleable.BubblePicker_maxSelectedCount, -1)
        }

        if (array.hasValue(R.styleable.BubblePicker_backgroundColor)) {
            background = array.getColor(R.styleable.BubblePicker_backgroundColor, -1)
        }

        array.recycle()
    }
}
