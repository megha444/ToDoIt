package com.example.todoit.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.todoit.R
import com.example.todoit.firebase.FirestoreClass
import com.example.todoit.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignupActivity : BaseActivity() {

    private lateinit var toolbar_signup: Toolbar
    private lateinit var et_name: EditText
    private lateinit var et_email:EditText
    private lateinit var et_password: EditText
    private lateinit var btn_signup:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        toolbar_signup=findViewById(R.id.toolbar_sign_up_activity)
        setupActionBar()

        et_email=findViewById(R.id.et_email)
        et_name=findViewById(R.id.et_name)
        et_password=findViewById(R.id.et_password)
        btn_signup=findViewById(R.id.btn_sign_up)


        btn_signup.setOnClickListener { registerUser() }

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_signup)

        val actionBar= supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        }
        toolbar_signup.setNavigationOnClickListener { onBackPressed() }
    }

    private fun registerUser(){
        val name= et_name.text.toString().trim{it <= ' '}
        val email= et_email.text.toString().trim{it <= ' '}
        val password= et_password.text.toString().trim{it <= ' '}

        if(validateForm(name, email, password))
        {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!

                        val user = User(firebaseUser.uid, name, registeredEmail)

                        FirestoreClass().registerUser(this, user)
                    } else
                    {
                        Toast.makeText(this,"Registeration failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


    fun userRegisteredSuccess(){
        Toast.makeText(this, "You have successfully registered", Toast.LENGTH_LONG).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }


    private fun validateForm(name: String, email: String, password: String): Boolean{

        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }
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