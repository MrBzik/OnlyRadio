package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.LayoutParams
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.radioplayer.R
import com.example.radioplayer.databinding.DialogPickTagBinding
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.slideAnim
import com.google.android.material.card.MaterialCardView

abstract class BaseDialog<VB : ViewBinding> (
    private val requireContext : Context,
    private val bindingInflater : (inflater : LayoutInflater) -> VB
    ) : AppCompatDialog(requireContext){

    var _bind : VB? = null

    val bind : VB
        get() = _bind!!

    override fun onCreate(savedInstanceState: Bundle?) {

        _bind = bindingInflater(layoutInflater)
        super.onCreate(savedInstanceState)

//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(bind.root)

        setupMainWindow()



        bind.root.slideAnim(400, 0, R.anim.scale_up)

    }

    val dp8 = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        8f,
        requireContext.resources.displayMetrics
    ).toInt()

    fun adjustDialogHeight(view : ConstraintLayout){

        val bottomNanViewShift = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f,
            requireContext.resources.displayMetrics
        ).toInt()

        view.minHeight = MainActivity.flHeight + bottomNanViewShift

    }


    fun removeDim(){
        window?.setDimAmount(0f)
    }

    fun removeTopPadding(){

        bind.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(dp8,0,dp8, 0)
        }


    }

     fun setupMainWindow(){


             window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
             window?.setGravity(Gravity.TOP)


//        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.navigationBarColor = ContextCompat.getColor(requireContext, R.color.main_background)

         window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

//         window?.setDimAmount(0.5f)



         if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
             window?.setDimAmount(0.15f)
         }



         bind.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//            setMargins(dp8,topMargin,dp8, bottomMargin)
            setMargins(dp8,dp8*2,dp8, 0)
        }
    }

}