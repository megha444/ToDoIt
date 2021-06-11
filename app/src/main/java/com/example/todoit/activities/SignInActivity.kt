package com.example.todoit.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.todoit.R
import com.example.todoit.firebase.FirestoreClass
import com.example.todoit.model.User
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var et_email: EditText
    private lateinit var et_password: EditText
    private lateinit var btn_signin: Button
    private lateinit var toolbar_sigin: Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        toolbar_sigin=findViewById(R.id.toolbar_sign_in_activity)
        setupActionBar()

        et_email=findViewById(R.id.et_email_sign_in)
        et_password=findViewById(R.id.et_password_sign_in)
        btn_signin=findViewById(R.id.btn_sign_in)

        auth= FirebaseAuth.getInstance()

        btn_signin.setOnClickListener { signInUser() }
    }

    private fun signInUser(){
        val email : String= et_email.text.toString().trim{it <= ' '}
        val password : String= et_password.text.toString().trim{it <= ' '}

        if(validateUser(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this){
                task ->
                hideProgressDialog()
                    if(task.isSuccessful){
                    FirestoreClass().loadUserData(this)

                    /*Log.d("SignIn", "signInWithEmail: success")
                val user= auth.currentUser
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
*/
                }else{
                Log.w("SignIn", "signInWithEmail: failed", task.exception)
                Toast.makeText(this,"Sign in failed", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_sigin)

        val actionBar= supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        }
        toolbar_sigin.setNavigationOnClickListener { onBackPressed() }
    }

    fun signInSuccess(user: User){
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun validateUser(email: String, password: String): Boolean{

        return when{
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter email address")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            }
            else->{true}
        }
    }

}