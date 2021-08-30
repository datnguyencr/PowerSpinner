package com.skydoves.powerspinner

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import com.skydoves.powerspinner.databinding.ItemSpinnerBinding

class ItemPowerSpinnerView : LinearLayoutCompat {
    lateinit var binding: ItemSpinnerBinding
    var onItemSelected: ((Option) -> Unit)? = null
    var normalTextColor: Int = Color.parseColor("#1C1C1C")
    var selectTextColor: Int = Color.parseColor("#1C1C1C")
    var normalBackgroundColor: Drawable = resources.getDrawable(R.drawable.normal_background_color)
    var selectBackgroundColor: Drawable = resources.getDrawable(R.drawable.selected_background_color)

    constructor(context: Context) : super(context) {
        setLayout(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setLayout(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
            context,
            attrs,
            defStyle
    ) {
        setLayout(context, attrs)
    }

    private fun setLayout(context: Context, attrs: AttributeSet?) {
        binding = ItemSpinnerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun bind(item: Option) {
        binding.vLayout.setOnClickListener {
            onItemSelected?.invoke(item)
        }
        binding.itemDefaultText.text = item.name
        binding.itemDefaultText.setTextColor(
                if (isSelected) selectTextColor else normalTextColor
        )
        binding.vLayout.background = if (item.selected) selectBackgroundColor else normalBackgroundColor

    }
}
