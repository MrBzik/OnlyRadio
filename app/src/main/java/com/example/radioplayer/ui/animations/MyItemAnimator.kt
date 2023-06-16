package com.example.radioplayer.ui.animations

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class MyItemAnimator : DefaultItemAnimator() {

    override fun animatePersistence(
        viewHolder: RecyclerView.ViewHolder,
        preInfo: ItemHolderInfo,
        postInfo: ItemHolderInfo
    ): Boolean {
        return false
    }



}