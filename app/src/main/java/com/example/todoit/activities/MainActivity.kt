package com.example.todoit.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todoit.R
import com.example.todoit.adapters.BoardItemsAdapter
import com.example.todoit.model.User
import com.example.todoit.firebase.FirestoreClass
import com.example.todoit.model.Board
import com.example.todoit.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import de.hdodenhof.circleimageview.CircleImageView

@Suppress("DEPRECATION")
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar_main_activity: Toolbar
    private lateinit var drawer_layout: DrawerLayout
    private lateinit var nav_view: NavigationView
    private lateinit var tv_username_nav: TextView
    private lateinit var nav_user_image: CircleImageView
    private lateinit var fab_create_board: FloatingActionButton

    private lateinit var rv_boards_list: RecyclerView
    private lateinit var tv_no_boards_available: TextView

    private lateinit var mUserName: String

    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSharedPreferences = this.getSharedPreferences(Constants.ToDoItPreferences, Context.MODE_PRIVATE)
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if(tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this, true)
        }else{
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this@MainActivity){
                instanceIdResult ->
                updateFcmToken(instanceIdResult.token)
            }
        }


        toolbar_main_activity=findViewById(R.id.toolbar_main_activity)
        drawer_layout=findViewById(R.id.drawer_layout)
        nav_view=findViewById(R.id.nav_view)

        val hview: View = nav_view.inflateHeaderView(R.layout.nav_header_main)
        tv_username_nav=hview.findViewById(R.id.tv_nav_username)
        nav_user_image=hview.findViewById(R.id.nav_header_user_image)
        fab_create_board=findViewById(R.id.fab_create_board)

        tv_no_boards_available=findViewById(R.id.tv_no_boards_available)
        rv_boards_list=findViewById(R.id.rv_boards_list)

        setUpActionBar()
        nav_view.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this, true)

        fab_create_board.setOnClickListener {

            val intent=Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    private fun setUpActionBar(){

        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_baseline_menu_24)

        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }


    }

    private fun toggleDrawer(){

        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            drawer_layout.openDrawer(GravityCompat.START)
        }

    }

    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{

            doubleBackToExit()
        }
    }

    fun updateNavigationUserDetails(user: User, readBoardList : Boolean){

        hideProgressDialog()
            Glide
                .with(this)
                .load(user!!.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(nav_user_image)

            tv_username_nav.text=user.name
        mUserName=user.name

        if(readBoardList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsListFromDb(this)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_my_profile-> {
            startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }

            R.id.nav_sign_out-> {

                FirebaseAuth.getInstance().signOut()
                mSharedPreferences.edit().clear().apply()
                val intent= Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode== Activity.RESULT_OK && requestCode== MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }
        else if(resultCode==Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE)
        {
            FirestoreClass().getBoardsListFromDb(this)
        }
        else{
            Log.e("Cancelled", "Main Activity load failed")
        }
    }

    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11

        const val CREATE_BOARD_REQUEST_CODE : Int =12
    }

    fun populateBoardsListToUI(boardsList : ArrayList<Board>){
        hideProgressDialog()
        if(boardsList.size > 0){
            rv_boards_list.visibility=View.VISIBLE
            tv_no_boards_available.visibility=View.GONE

            rv_boards_list.layoutManager= LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardsList)
            rv_boards_list.adapter=adapter


            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {

                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                 startActivity(intent)
                }
            })

        }else{
            rv_boards_list.visibility=View.GONE
            tv_no_boards_available.visibility=View.VISIBLE
        }
    }

    fun tokenUpdateSuccess(){
        hideProgressDialog()
        val editor : SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this, true)
    }

    private fun updateFcmToken(token : String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }


}












