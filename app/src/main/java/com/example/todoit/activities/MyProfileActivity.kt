package com.example.todoit.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.todoit.R
import com.example.todoit.firebase.FirestoreClass
import com.example.todoit.model.User
import com.example.todoit.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

@Suppress("DEPRECATION")
class MyProfileActivity : BaseActivity() {

    private lateinit var toolbar_my_profile_activity: Toolbar
    private lateinit var et_profile_name: EditText
    private lateinit var et_profile_email: EditText
    private lateinit var et_profile_mobile: EditText
    private lateinit var iv_profile_user_image: CircleImageView
    private lateinit var btn_update: Button


    private var mSelectedImageFileUri: Uri? = null

    private lateinit var mUserDetails: User
    private var mProfileImageUri: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        toolbar_my_profile_activity = findViewById(R.id.toolbar_my_profile_activity)
        setUpActionBar()

        et_profile_email=findViewById(R.id.et_profile_email)
        et_profile_name=findViewById(R.id.et_profile_name)
        et_profile_mobile=findViewById(R.id.et_profile_mobile)
        iv_profile_user_image=findViewById(R.id.iv_profile_user_image)
        btn_update=findViewById(R.id.btn_update)

        FirestoreClass().loadUserData(this)

        iv_profile_user_image.setOnClickListener {

            if(ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }
            else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE)
            }
        }

        btn_update.setOnClickListener {
            if (mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode== Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                Toast.makeText(
                    this,
                    "You just denied permission storage. You can modify this in the settings",
                    Toast.LENGTH_LONG)
                    .show()
            }
        }
    }



    private fun updateUserProfileData(){
         val userHashMap = HashMap<String, Any>()
        var anychangesmade = false
        if(mProfileImageUri!!.isNotEmpty() && mProfileImageUri!= mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageUri!!
            anychangesmade=true
        }
        if(et_profile_name.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = et_profile_name.text.toString()
            anychangesmade=true
        }
        if(et_profile_mobile.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = et_profile_mobile.text.toString().toLong()
            anychangesmade=true
        }
        if(anychangesmade) {
            FirestoreClass().updateUserProfileData(this, userHashMap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK && requestCode== Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null){

            mSelectedImageFileUri=data.data

            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_profile_user_image)
            }catch (e: IOException){ e.printStackTrace() }
        }
    }

    private fun setUpActionBar() {

        setSupportActionBar(toolbar_my_profile_activity)

        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.title= resources.getString(R.string.my_profile)
        }

        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }


    fun setUserDataInUi(user: User){

        mUserDetails=user

        Glide
            .with(this)
            .load(user.image)
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profile_user_image)

        et_profile_name.setText(user.name)
        et_profile_email.setText(user.email)
        if(user.mobile != 0L){
            et_profile_mobile.setText(user.mobile.toString())
        }
    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if(mSelectedImageFileUri != null){
         val sRef: StorageReference =
             FirebaseStorage.getInstance().reference.child(
                 "User_image"
                     + System.currentTimeMillis()
                     +"."
                     +Constants.getFileExtension(this, mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot->
                Log.i("Firebase Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri-> Log.i("Downloadable image uri", uri.toString())
                    mProfileImageUri = uri.toString()

                    updateUserProfileData()

                }.addOnFailureListener {
                    exception->
                    Toast.makeText(this,
                    exception.message,
                    Toast.LENGTH_SHORT)
                        .show()

                    hideProgressDialog()
                }

            }
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

}