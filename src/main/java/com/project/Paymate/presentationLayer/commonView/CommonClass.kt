package com.project.Paymate.presentationLayer.commonView

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.project.Paymate.R

class CommonClass(private val context: Context) {
   val p by lazy {
      Dialog(context).apply {
         setContentView(R.layout.progress)
         setCancelable(false)
         window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      }
   }
}