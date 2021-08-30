/*
 * Designed and developed by 2019 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.powerspinner

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.*
import android.widget.PopupWindow
import androidx.annotation.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ScrollingView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.powerspinner.databinding.LayoutBodyPowerSpinnerLibraryBinding

/** A lightweight dropdown spinner, fully customizable with arrow and animations. */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class PowerSpinnerView : AppCompatTextView, LifecycleObserver {
    var onItemSelected: ((Option) -> Unit)? = null

    /** Main body view for composing the Spinner popup. */
    private val binding: LayoutBodyPowerSpinnerLibraryBinding =
        LayoutBodyPowerSpinnerLibraryBinding.inflate(LayoutInflater.from(context), null, false)

    /** PopupWindow for creating the spinner. */
    private var spinnerWindow: PopupWindow? = null

    /** Spinner is showing or not. */
    var isShowing: Boolean = false
        private set

    /** An index of the selected item. */
    var selectedIndex: Int = NO_SELECTED_INDEX
        private set

    var arrowAnimate: Boolean = true

    /** A duration of the arrow animation when show and dismiss. */
    var arrowAnimationDuration: Long = 250L

    /** A drawable of the arrow. */
    var arrowDrawable: Drawable? =
        context.contextDrawable(R.drawable.arrow_power_spinner_library)?.mutate()

    /** A duration of the debounce for showOrDismiss. */
    var debounceDuration: Long = 150L
        private set

    /** Disable changing text automatically when an item selection notified. */
    var disableChangeTextWhenNotified: Boolean = false

    var normalTextColor: Int = Color.parseColor("#1C1C1C")
    var selectTextColor: Int = Color.parseColor("#1C1C1C")

    //    var normalBackgroundColor: Int = Color.parseColor("#ffffff")
//    var selectBackgroundColor: Int = Color.parseColor("#46A355")
    var normalBackgroundColor: Drawable = resources.getDrawable(R.drawable.normal_background_color)
    var selectBackgroundColor: Drawable = resources.getDrawable(R.drawable.selected_background_color)

    /** A backing field of the previously debounce local time. */
    private var previousDebounceTime: Long = 0
    private var adapter = DefaultSpinnerAdapter(
        normalTextColor = normalTextColor,
        selectTextColor = selectTextColor,
        normalBackgroundColor = normalBackgroundColor,
        selectBackgroundColor = selectBackgroundColor,
        onItemSelected = {
            dismiss()
            onItemSelected?.invoke(it)
            invalidate()
        }
    )

    @DrawableRes
    private var _arrowResource: Int = NO_INT_VALUE

    /** A drawable resource of the arrow. */
    var arrowResource: Int
        @DrawableRes get() = _arrowResource
        set(@DrawableRes value) {
            _arrowResource = value
            updateSpinnerArrow()
        }

    private var _showArrow: Boolean = true

    /** The arrow will be shown or not on the popup. */
    var showArrow: Boolean
        get() = _showArrow
        set(value) {
            _showArrow = value
            updateSpinnerArrow()
        }

    private var _arrowGravity: SpinnerGravity = SpinnerGravity.END

    /** A gravity of the arrow. */
    var arrowGravity: SpinnerGravity
        get() = _arrowGravity
        set(value) {
            _arrowGravity = value
            updateSpinnerArrow()
        }

    @Px
    private var _arrowPadding: Int = 0

    /** A padding of the arrow. */
    var arrowPadding: Int
        @Px get() = _arrowPadding
        set(@Px value) {
            _arrowPadding = value
            updateSpinnerArrow()
        }

    @ColorInt
    private var _arrowTint: Int = Color.WHITE

    /** A tint color of the arrow. */
    var arrowTint: Int
        @ColorInt get() = _arrowTint
        set(@ColorInt value) {
            _arrowTint = value
            updateSpinnerArrow()
        }

    private var _showDivider: Boolean = false

    /** A divider between items will be shown or not. */
    var showDivider: Boolean
        get() = _showDivider
        set(value) {
            _showDivider = value
            updateSpinnerWindow()
        }

    @Px
    private var _dividerSize: Int = dp2Px(0.5f)

    /** A width size of the divider. */
    var dividerSize: Int
        @Px get() = _dividerSize
        set(@Px value) {
            _dividerSize = value
            updateSpinnerWindow()
        }

    @ColorInt
    private var _dividerColor: Int = Color.WHITE

    /** A color of the divider. */
    var dividerColor: Int
        @ColorInt get() = _dividerColor
        set(@ColorInt value) {
            _dividerColor = value
            updateSpinnerWindow()
        }

    @ColorInt
    private var _spinnerPopupBackgroundColor: Int = outRangeColor

    /** A background color of the spinner popup. */
    var spinnerPopupBackgroundColor: Int
        @ColorInt get() = _spinnerPopupBackgroundColor
        set(@ColorInt value) {
            _spinnerPopupBackgroundColor = value
            updateSpinnerWindow()
        }

    @Px
    private var _spinnerPopupElevation: Int = dp2Px(4)

    /** A elevation of the spinner popup. */
    var spinnerPopupElevation: Int
        @Px get() = _spinnerPopupElevation
        set(@Px value) {
            _spinnerPopupElevation = value
            updateSpinnerWindow()
        }

    /** A style resource for the popup animation when show and dismiss. */
    @StyleRes
    var spinnerPopupAnimationStyle: Int = NO_INT_VALUE

    /** A width size of the spinner popup. */
    var spinnerPopupWidth: Int = NO_INT_VALUE

    /** A height size of the spinner popup. */
    var spinnerPopupHeight: Int = NO_INT_VALUE

    /** The spinner popup will be dismissed when got notified an item is selected. */
    var dismissWhenNotifiedItemSelected: Boolean = true

    /** Interface definition for a callback to be invoked when touched on outside of the spinner popup. */
    var spinnerOutsideTouchListener: OnSpinnerOutsideTouchListener? = null

    /** Interface definition for a callback to be invoked when spinner popup is dismissed. */
    var onSpinnerDismissListener: OnSpinnerDismissListener? = null

    /** A collection of the spinner popup animation when show and dismiss. */
    var spinnerPopupAnimation: SpinnerAnimation = SpinnerAnimation.NORMAL

    var lifecycleOwner: LifecycleOwner? = null
        set(value) {
            field = value
            field?.lifecycle?.addObserver(this@PowerSpinnerView)
        }

    init {
        if (adapter is RecyclerView.Adapter<*>) {
            getSpinnerRecyclerView().adapter = adapter as RecyclerView.Adapter<*>
        }

        this.setOnClickListener {
            showOrDismiss()
        }
        if (this.gravity == Gravity.NO_GRAVITY) {
            this.gravity = Gravity.CENTER_VERTICAL
        }
        val viewContext = context
        if (lifecycleOwner == null && viewContext is LifecycleOwner) {
            lifecycleOwner = viewContext
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        getAttrs(attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        getAttrs(attributeSet, defStyle)
    }

    private fun getAttrs(attributeSet: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PowerSpinnerView)
        try {
            setTypeArray(typedArray)
        } finally {
            typedArray.recycle()
        }
    }

    private fun getAttrs(attributeSet: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.PowerSpinnerView,
            defStyleAttr,
            0
        )
        try {
            setTypeArray(typedArray)
        } finally {
            typedArray.recycle()
        }
    }

    private fun setTypeArray(a: TypedArray) {
        a.apply {
            if (hasValue(R.styleable.PowerSpinnerView_spinner_arrow_drawable)) {
                _arrowResource =
                    getResourceId(
                        R.styleable.PowerSpinnerView_spinner_arrow_drawable,
                        _arrowResource
                    )
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_arrow_show)) {
                _showArrow =
                    a.getBoolean(R.styleable.PowerSpinnerView_spinner_arrow_show, _showArrow)
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_arrow_gravity)) {
                _arrowGravity = when (
                    getInteger(
                        R.styleable.PowerSpinnerView_spinner_arrow_gravity,
                        _arrowGravity.value
                    )
                ) {
                    SpinnerGravity.START.value -> SpinnerGravity.START
                    SpinnerGravity.TOP.value -> SpinnerGravity.TOP
                    SpinnerGravity.END.value -> SpinnerGravity.END
                    SpinnerGravity.BOTTOM.value -> SpinnerGravity.BOTTOM
                    else -> throw IllegalArgumentException("unknown argument: spinner_arrow_gravity")
                }
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_arrow_padding)) {
                _arrowPadding =
                    getDimensionPixelSize(
                        R.styleable.PowerSpinnerView_spinner_arrow_padding,
                        _arrowPadding
                    )
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_arrow_tint)) {
                _arrowTint =
                    getColor(R.styleable.PowerSpinnerView_spinner_arrow_tint, _arrowTint)
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_arrow_animate)) {
                arrowAnimate =
                    getBoolean(R.styleable.PowerSpinnerView_spinner_arrow_animate, arrowAnimate)
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_arrow_animate_duration)) {
                arrowAnimationDuration =
                    getInteger(
                        R.styleable.PowerSpinnerView_spinner_arrow_animate_duration,
                        arrowAnimationDuration.toInt()
                    ).toLong()
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_divider_show)) {
                _showDivider =
                    getBoolean(R.styleable.PowerSpinnerView_spinner_divider_show, _showDivider)
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_divider_size)) {
                _dividerSize =
                    getDimensionPixelSize(
                        R.styleable.PowerSpinnerView_spinner_divider_size,
                        _dividerSize
                    )
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_divider_color)) {
                _dividerColor =
                    getColor(R.styleable.PowerSpinnerView_spinner_divider_color, _dividerColor)
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_popup_background)) {
                _spinnerPopupBackgroundColor =
                    getColor(
                        R.styleable.PowerSpinnerView_spinner_popup_background,
                        _spinnerPopupBackgroundColor
                    )
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_popup_animation)) {
                spinnerPopupAnimation = when (
                    getInteger(
                        R.styleable.PowerSpinnerView_spinner_popup_animation,
                        spinnerPopupAnimation.value
                    )
                ) {
                    SpinnerAnimation.DROPDOWN.value -> SpinnerAnimation.DROPDOWN
                    SpinnerAnimation.FADE.value -> SpinnerAnimation.FADE
                    SpinnerAnimation.BOUNCE.value -> SpinnerAnimation.BOUNCE
                    SpinnerAnimation.NORMAL.value -> SpinnerAnimation.NORMAL
                    else -> throw IllegalArgumentException("unknown argument: spinner_popup_animation")
                }
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_popup_animation_style)) {
                spinnerPopupAnimationStyle =
                    getResourceId(
                        R.styleable.PowerSpinnerView_spinner_popup_animation_style,
                        spinnerPopupAnimationStyle
                    )
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_popup_width)) {
                spinnerPopupWidth =
                    getDimensionPixelSize(
                        R.styleable.PowerSpinnerView_spinner_popup_width,
                        spinnerPopupWidth
                    )
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_popup_height)) {
                spinnerPopupHeight =
                    getDimensionPixelSize(
                        R.styleable.PowerSpinnerView_spinner_popup_height,
                        spinnerPopupHeight
                    )
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_popup_elevation)) {
                _spinnerPopupElevation =
                    getDimensionPixelSize(
                        R.styleable.PowerSpinnerView_spinner_popup_elevation,
                        _spinnerPopupElevation
                    )
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_dismiss_notified_select)) {
                dismissWhenNotifiedItemSelected =
                    getBoolean(
                        R.styleable.PowerSpinnerView_spinner_dismiss_notified_select,
                        dismissWhenNotifiedItemSelected
                    )
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_debounce_duration)) {
                debounceDuration =
                    getInteger(
                        R.styleable.PowerSpinnerView_spinner_debounce_duration,
                        debounceDuration.toInt()
                    )
                        .toLong()
            }

            if (hasValue(R.styleable.PowerSpinnerView_spinner_popup_focusable)) {
                setIsFocusable(
                    getBoolean(
                        R.styleable.PowerSpinnerView_spinner_popup_focusable,
                        false
                    )
                )
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        updateSpinnerWindow()
        updateSpinnerArrow()
    }

    private fun updateSpinnerWindow() {
        post {
            this.spinnerWindow = PopupWindow(
                this.binding.recyclerView,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            ).also {
                it.setBackgroundDrawable(normalBackgroundColor)
                it.apply {
                    width = this@PowerSpinnerView.width
                    isOutsideTouchable = true
                    setOnDismissListener { onSpinnerDismissListener?.onDismiss() }
                    setTouchInterceptor(
                        object : OnTouchListener {
                            @SuppressLint("ClickableViewAccessibility")
                            override fun onTouch(view: View, event: MotionEvent): Boolean {
                                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                                    spinnerOutsideTouchListener?.onSpinnerOutsideTouch(view, event)
                                    return true
                                }
                                return false
                            }
                        }
                    )
                    elevation = spinnerPopupElevation.toFloat()
                }
            }

            binding.recyclerView.apply {
                if (this@PowerSpinnerView.spinnerPopupBackgroundColor == outRangeColor) {
                    background = this@PowerSpinnerView.background
                } else {
                    setBackgroundColor(this@PowerSpinnerView.spinnerPopupBackgroundColor)
                }
                setPadding(
                    this.paddingLeft,
                    this.paddingTop,
                    this.paddingRight,
                    this.paddingBottom
                )
                if (this@PowerSpinnerView.showDivider) {
                    val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                    val shape = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        setSize(width, dividerSize)
                        setColor(dividerColor)
                    }
                    decoration.setDrawable(shape)
                    getSpinnerRecyclerView().addItemDecoration(decoration)
                }
            }
            if (this.spinnerPopupWidth != NO_INT_VALUE) {
                this.spinnerWindow?.width = this.spinnerPopupWidth
            }
            if (this.spinnerPopupHeight != NO_INT_VALUE) {
                this.spinnerWindow?.height = this.spinnerPopupHeight
            }
        }
    }

    private fun updateSpinnerArrow() {
        if (this.arrowResource != NO_INT_VALUE) {
            this.arrowDrawable = context.contextDrawable(this.arrowResource)?.mutate()
        }
        this.compoundDrawablePadding = this.arrowPadding
        updateCompoundDrawable(this.arrowDrawable)
    }

    private fun updateCompoundDrawable(drawable: Drawable?) {
        if (this.showArrow) {
            drawable?.let {
                val wrappedDrawable = DrawableCompat.wrap(it).mutate()
                DrawableCompat.setTint(wrappedDrawable, this.arrowTint)
                wrappedDrawable.invalidateSelf()
            }
            when (this.arrowGravity) {
                SpinnerGravity.START -> setCompoundDrawablesWithIntrinsicBounds(
                    drawable,
                    null,
                    null,
                    null
                )
                SpinnerGravity.TOP -> setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    drawable,
                    null,
                    null
                )
                SpinnerGravity.END -> setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    drawable,
                    null
                )
                SpinnerGravity.BOTTOM -> setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    drawable
                )
            }
        } else {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    /** gets the spinner popup's recyclerView. */
    fun getSpinnerRecyclerView(): RecyclerView = binding.recyclerView

    /** gets the spinner popup's body. */
    fun getSpinnerBodyView(): ScrollingView = binding.recyclerView

    fun setItems(itemList: List<Option>) {
        adapter.setItems(itemList)
    }

    @JvmSynthetic
    fun setOnSpinnerOutsideTouchListener(block: (View, MotionEvent) -> Unit) {
        this.spinnerOutsideTouchListener =
            OnSpinnerOutsideTouchListener { view, event -> block(view, event) }
    }

    /** sets a [OnSpinnerDismissListener] to the popup using lambda. */
    @JvmSynthetic
    fun setOnSpinnerDismissListener(block: () -> Unit) {
        this.onSpinnerDismissListener = OnSpinnerDismissListener {
            block()
        }
    }

    /** shows the spinner popup menu to the center. */
    @MainThread
    @JvmOverloads
    fun show(xOff: Int = 0, yOff: Int = 0) {
        debounceShowOrDismiss {
            if (!isShowing) {
                this.isShowing = true
                animateArrow(true)
                this.spinnerWindow?.showAsDropDown(this, xOff, yOff)
                post {
                    val spinnerWidth = if (spinnerPopupWidth != NO_INT_VALUE) {
                        spinnerPopupWidth
                    } else {
                        width
                    }
                    val spinnerHeight = if (spinnerPopupHeight != NO_INT_VALUE) {
                        spinnerPopupHeight
                    } else {
                        getSpinnerRecyclerView().height
                    }
                    this.spinnerWindow?.update(spinnerWidth, spinnerHeight)
                }
            }
        }
    }

    /** dismiss the spinner popup menu. */
    @MainThread
    fun dismiss() {
        debounceShowOrDismiss {
            if (this.isShowing) {
                animateArrow(false)
                this.spinnerWindow?.dismiss()
                this.isShowing = false
            }
        }
    }

    @MainThread
    @JvmOverloads
    fun showOrDismiss(xOff: Int = 0, yOff: Int = 0) {
        val adapter = getSpinnerRecyclerView().adapter ?: return
        if (!isShowing && adapter.itemCount > 0) {
            show(xOff, yOff)
        } else {
            dismiss()
        }
    }

    /** Disable changing text automatically when an item selection notified. */
    fun setDisableChangeTextWhenNotified(value: Boolean) = apply {
        this.disableChangeTextWhenNotified = value
    }

    fun setIsFocusable(isFocusable: Boolean) {
        this.spinnerWindow?.isFocusable = isFocusable
        this.onSpinnerDismissListener = OnSpinnerDismissListener { dismiss() }
    }

    /** debounce for showing or dismissing spinner popup. */
    private fun debounceShowOrDismiss(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - previousDebounceTime > debounceDuration) {
            this.previousDebounceTime = currentTime
            action()
        }
    }

    /** notifies to [PowerSpinnerView] of changed information from [PowerSpinnerInterface]. */
    fun notifyItemSelected(index: Int, option: Option) {
        this.selectedIndex = index
        if (!disableChangeTextWhenNotified) {
            this.text = option.name
        }
        if (this.dismissWhenNotifiedItemSelected) {
            dismiss()
        }
    }

    /** clears a selected item. */
    fun clearSelectedItem() {
        notifyItemSelected(NO_SELECTED_INDEX, Option(""))
    }

    /** animates the arrow rotation. */
    private fun animateArrow(shouldRotateUp: Boolean) {
        if (this.arrowAnimate) {
            val start = if (shouldRotateUp) 0 else 10000
            val end = if (shouldRotateUp) 10000 else 0
            ObjectAnimator.ofInt(this.arrowDrawable, "level", start, end).apply {
                duration = this@PowerSpinnerView.arrowAnimationDuration
                start()
            }
        }
    }

    /** dismiss automatically when lifecycle owner is destroyed. */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        dismiss()
    }
}
