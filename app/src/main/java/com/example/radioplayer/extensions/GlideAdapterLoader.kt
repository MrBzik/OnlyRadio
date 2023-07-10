package com.example.radioplayer.extensions

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.radioplayer.ui.animations.fadeOut

fun RequestManager.loadImage(
    uri: String,
    tvPlaceholder: TextView,
    ivItemImage: ImageView,
    alpha : Float,
    position: Int,
    updatedHolderPos : () -> Int
){
    load(uri)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {

                return true
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {

                if(dataSource?.name == "REMOTE"){

                    tvPlaceholder.fadeOut(300, alpha, position){ pos ->
                        if(pos != updatedHolderPos()) {
                            tvPlaceholder.alpha = alpha
                        }
                    }
                }
                else {

                    tvPlaceholder.alpha = 0f
                }
                ivItemImage.visibility = View.VISIBLE
                return false
            }
        })
        .transition(DrawableTransitionOptions.withCrossFade())
//                    .apply(RequestOptions().override(65, 65))
        .into(ivItemImage)
}