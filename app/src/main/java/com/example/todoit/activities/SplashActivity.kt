package com.example.todoit.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.todoit.R
import com.example.todoit.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {

    private lateinit var tv_splash_name:TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        tv_splash_name=findViewById(R.id.tv_splash_name)

        val typeface = resources.getFont(R.font.secret_admirer)
        tv_splash_name.typeface=typeface

        Handler(Looper.getMainLooper()).postDelayed({

            var currentUserId = FirestoreClass().getCurrentUserID()
            if (currentUserId.isNotEmpty()) {

                startActivity(Intent(this, MainActivity::class.java))

            } else {
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 2000)
    }
}