package com.project.Paymate.presentationLayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.project.Paymate.databinding.ActivityMainBinding
import com.project.Paymate.presentationLayer.commonView.Login
import com.project.Paymate.presentationLayer.user.UserMainActivity

class MainActivity : AppCompatActivity() {
   private val bind by lazy {
      ActivityMainBinding.inflate(layoutInflater)
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(bind.root)
      val type = getSharedPreferences("user", MODE_PRIVATE)
         .getString("mobile", "")
      bind.imageView.apply {
         alpha = 0f
         animate().setDuration(1000).alpha(1f).withEndAction {
               finish()

            if(type!=null){
               if(type.isNotEmpty()){
                  startActivity(Intent(this@MainActivity,UserMainActivity::class.java))
               }else{
                  startActivity(Intent(this@MainActivity, Login::class.java))
               }
            }else{
               startActivity(Intent(this@MainActivity, Login::class.java))
            }
         }.start()
      }
   }
}