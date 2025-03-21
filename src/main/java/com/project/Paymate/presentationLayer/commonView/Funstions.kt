package com.project.Paymate.presentationLayer.commonView

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.showToast(message:Any?)= Toast.makeText(requireContext(),"$message",Toast.LENGTH_SHORT).show()
fun Activity.showToast(message:Any?)= Toast.makeText(this,"$message",Toast.LENGTH_SHORT).show()