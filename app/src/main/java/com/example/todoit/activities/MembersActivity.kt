package com.example.todoit.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoit.R
import com.example.todoit.adapters.MembersAdapter
import com.example.todoit.firebase.FirestoreClass
import com.example.todoit.model.Board
import com.example.todoit.model.User
import com.example.todoit.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

@Suppress("DEPRECATION")
class MembersActivity : BaseActivity() {

    private lateinit var mBoardDetails : Board
    private lateinit var toolbar_members_activity: Toolbar
    private lateinit var rv_members_list: RecyclerView

    private lateinit var mAssignedMembersList : ArrayList<User>

    private var anyChangesMade : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)


        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails= intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getAssignedMembersDetails(this, mBoardDetails.assignedTo)
        }

        toolbar_members_activity=findViewById(R.id.toolbar_members_activity)
        setUpActionBar()

        rv_members_list=findViewById(R.id.rv_members_list)


    }

    private fun setUpActionBar() {

        setSupportActionBar(toolbar_members_activity)

        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.title= resources.getString(R.string.members)
        }

        toolbar_members_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUpMembersList(list : ArrayList<User>){

        mAssignedMembersList= list

        hideProgressDialog()

        rv_members_list.layoutManager= LinearLayoutManager(this)
        rv_members_list.setHasFixedSize(true)

        val adapter = MembersAdapter(this, list)
        rv_members_list.adapter=adapter

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun memberDetails(user: User){
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToBoard(this, mBoardDetails, user)
    }

    private fun dialogSearchMember(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add_dialog).setOnClickListener {
            val email = dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()
            if(email.isNotEmpty()){
                dialog.dismiss()

                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this, email)

            }else{
                Toast.makeText(this, "Please enter email address", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.findViewById<TextView>(R.id.tv_cancel_dialog).setOnClickListener {
            dialog.dismiss()
        }
        Toast.makeText(this, "Reached here", Toast.LENGTH_SHORT).show()
        dialog.show()
    }

    fun memberAssignedSuccessCall(user : User){
        hideProgressDialog()
        mAssignedMembersList.add(user)
        anyChangesMade = true
        setUpMembersList(mAssignedMembersList)

        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()
    }

    override fun onBackPressed() {
        if(anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    private inner class SendNotificationToUserAsyncTask(val boardName : String, val token: String) : AsyncTask<Any, Void, String>(){

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(resources.getString(R.string.please_wait))
        }

        override fun doInBackground(vararg params: Any?): String {
            var result : String

            var connection : HttpURLConnection? = null

            try{
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection

                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)
                val jsonResquest = JSONObject()
                val dataObject = JSONObject()

                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the board $boardName")
                dataObject.put(Constants.FCM_KEY_MESSAGE, "You have been assigned to the board by ${mAssignedMembersList[0].name}")

                jsonResquest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonResquest.put(Constants.FCM_KEY_TO, token)

                wr.writeBytes(jsonResquest.toString())
                wr.flush()
                wr.close()

                val httpResult : Int = connection.responseCode
                if(httpResult == HttpURLConnection.HTTP_OK){
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val sb = StringBuilder()
                    var line : String?
                    try{
                        while(reader.readLine().also {line = it} != null){
                            sb.append(line + "\n")
                        }
                    }catch (e : IOException){e.printStackTrace()}

                    finally {
                        try {
                            inputStream.close()
                        }catch (e : IOException){e.printStackTrace()}
                    }
                    result = sb.toString()
                }else{
                    result = connection.responseMessage
                }
            }catch(e: SocketTimeoutException) {result = "Connection TimeOut"}
            catch (e : Exception){ result = "Error : " + e.message }
            finally {
                connection?.disconnect()
            }

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
            if (result != null) {
                Log.e("JSON response", result)
            }
        }

    }
}