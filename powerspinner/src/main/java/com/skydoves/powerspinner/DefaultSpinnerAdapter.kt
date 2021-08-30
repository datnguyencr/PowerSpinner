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

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DefaultSpinnerAdapter(
    var normalTextColor: Int,
    var selectTextColor: Int,
    var normalBackgroundColor: Drawable,
    var selectBackgroundColor: Drawable,
    var onItemSelected: ((Option) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<Option> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(
            ItemPowerSpinnerView(context = parent.context).also { view ->
                view.normalTextColor = normalTextColor
                view.selectTextColor = selectTextColor
                view.normalBackgroundColor = normalBackgroundColor
                view.selectBackgroundColor = selectBackgroundColor
                view.onItemSelected = { option ->
                    items.forEach {
                        if (it.name == option.name) {
                            it.selected = true
                            onItemSelected?.invoke(it)
                        } else {
                            it.selected = false
                        }
                    }
                    notifyDataSetChanged()
                }
            }
        ) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemView) {
            is ItemPowerSpinnerView -> {
                items[position].let {
                    (holder.itemView as ItemPowerSpinnerView).bind(
                        items[position]
                    )
                }
            }
        }
    }

    fun setItems(itemList: List<Option>) {
        this.items = itemList
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}
