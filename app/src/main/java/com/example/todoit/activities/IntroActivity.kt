package com.example.todoit.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.todoit.R

class IntroActivity : BaseActivity() {

    private lateinit var btn_sign_up:Button
    private lateinit var btn_sign_in: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        btn_sign_up=findViewById(R.id.btn_sign_up_intro)
        btn_sign_in=findViewById(R.id.btn_sign_in_intro)


        btn_sign_up.setOnClickListener{
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        btn_sign_in.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
}