package com.igalata.bubblepicker

import com.igalata.bubblepicker.model.PickerItem

/**
 * Created by irinagalata on 3/6/17.
 */
interface BubblePickerHandler {
    fun onBubbleSelected(item: PickerItem)

    fun onBubbleDeselected(item: PickerItem)

    fun onBubbleRemoved(item: PickerItem)
}

interface BubblePickerListener: BubblePickerHandler {
    override fun onBubbleRemoved(item: PickerItem) {}
}