package com.project.Paymate.presentationLayer.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.project.Paymate.databinding.ActivitySendRequestBinding

class SendRequestActivity : AppCompatActivity() {
    private val bind by lazy { ActivitySendRequestBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)



    }
}