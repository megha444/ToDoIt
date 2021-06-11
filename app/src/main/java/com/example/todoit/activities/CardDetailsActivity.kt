package com.example.todoit.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoit.R
import com.example.todoit.adapters.CardListItemsAdapter
import com.example.todoit.adapters.CardMemberListItemsAdapter
import com.example.todoit.adapters.LabelColorListItemsAdapter
import com.example.todoit.dialogs.LabelColorListDialog
import com.example.todoit.dialogs.MembersListDialog
import com.example.todoit.firebase.FirestoreClass
import com.example.todoit.model.*
import com.example.todoit.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var toolbar_card_details_activity : Toolbar
    private lateinit var mBoardDetails : Board
    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedDueDateMilliSeconds : Long = 0

    private lateinit var et_name_card_details : EditText
    private lateinit var btn_update_card_details : Button
    private lateinit var tv_select_label_color : TextView
    private lateinit var tv_select_due_date : TextView

    private var mSelectedColor : String = ""

    private lateinit var mMembersDetailList : ArrayList<User>

    private lateinit var tv_select_members : TextView
    private lateinit var rv_selected_members_list : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()

        toolbar_card_details_activity=findViewById(R.id.toolbar_card_details_activity)
        setUpActionBar()

        et_name_card_details = findViewById(R.id.et_name_card_details)
        et_name_card_details.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)

        et_name_card_details.setSelection(et_name_card_details.text.toString().length)

        tv_select_label_color= findViewById(R.id.tv_select_label_color)

        tv_select_members= findViewById(R.id.tv_select_members)
        rv_selected_members_list= findViewById(R.id.rv_selected_members_list)
        tv_select_due_date = findViewById(R.id.tv_select_due_date)

        btn_update_card_details = findViewById(R.id.btn_update_card_details)
        btn_update_card_details.setOnClickListener {
            if(et_name_card_details.text.toString().isNotEmpty()){
                updateCardDetails()
            }else{
                Toast.makeText(this, "Enter a card name", Toast.LENGTH_SHORT).show()
            }
        }

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        tv_select_label_color.setOnClickListener {
            labelColorListDialog()
        }

        tv_select_members.setOnClickListener {
            membersListDialog()
        }

        mSelectedDueDateMilliSeconds=mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate
        if(mSelectedDueDateMilliSeconds > 0){
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val date = sdf.format(Date(mSelectedDueDateMilliSeconds))

            tv_select_due_date.text= date
        }

        tv_select_due_date.setOnClickListener {
            showDatePicker()
        }

        setUpSelectedMembersList()
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }

        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }

        if(intent.hasExtra(Constants.CARD_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(Constants.CARD_ITEM_POSITION, -1)
        }

        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun setUpActionBar(){
        setSupportActionBar(toolbar_card_details_activity)

        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.title= mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
        }

        toolbar_card_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card -> alertDialogForDelete(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        }

        return super.onOptionsItemSelected(item)
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails(){
        val card = Card(
            et_name_card_details.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
            )

        val tasklist : ArrayList<Task> = mBoardDetails.taskList
        tasklist.removeAt(tasklist.size - 1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    private fun deleteCard(){
        val cardList : ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards

        cardList.removeAt(mCardPosition)

        val taskList : ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition].cards = cardList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    private fun alertDialogForDelete(cardName : String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setMessage(resources.getString(R.string.confirmation_message_to_delete_card, cardName))

        builder.setPositiveButton(resources.getString(R.string.yes)){
                dialogInterface, which ->
            dialogInterface.dismiss()
            deleteCard()
        }

        builder.setNegativeButton(resources.getString(R.string.no)){
                dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }


    private fun colorsList(): ArrayList<String>{
        val colorsList : ArrayList<String> = ArrayList()
        colorsList.add("#781233")
        colorsList.add("#212243")
        colorsList.add("#af313b")
        colorsList.add("#ce21df")
        colorsList.add("#887def")
        colorsList.add("#f12143")
        colorsList.add("#abcdef")
        colorsList.add("#895672")
        colorsList.add("#878fd1")
        return colorsList
    }

    private fun setColor(){
        tv_select_label_color.text= ""
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun labelColorListDialog(){
        val colorsList : ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor
        ){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun membersListDialog(){
        var cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo
        if(cardAssignedMembersList.size > 0){
            for(i in mMembersDetailList.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailList[i].id == j){
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        }else {
            for (i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            this,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)
        ){
            override fun onItemSelected(user: User, action: String) {
                if(action == Constants.SELECT){
                    if(!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(user.id)){

                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(user.id)
                    }
                }else{
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(user.id)

                    for(i in mMembersDetailList.indices){
                        if(mMembersDetailList[i].id == user.id){
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setUpSelectedMembersList()
            }
        }

        listDialog.show()
    }

    private fun setUpSelectedMembersList(){
        val carsAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo


        val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()

            for(i in mMembersDetailList.indices){
                for(j in carsAssignedMembersList){
                    if(mMembersDetailList[i].id == j){
                       val selectedMember = SelectedMembers(mMembersDetailList[i].id, mMembersDetailList[i].image)

                        selectedMembersList.add(selectedMember)
                    }
                }
            }

        if(selectedMembersList.size > 0){
            selectedMembersList.add(SelectedMembers("",""))
            tv_select_members.visibility= View.GONE

            rv_selected_members_list.visibility = View.VISIBLE
            rv_selected_members_list.layoutManager = GridLayoutManager(this, 6)
            val adapter = CardMemberListItemsAdapter(this, selectedMembersList, true)

            rv_selected_members_list.adapter = adapter
            adapter.setOnClickListener(
                object : CardMemberListItemsAdapter.OnClickListener{
                    override fun onClick() {
                        membersListDialog()
                    }
                }
            )
        }else{
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility = View.GONE
        }

        }

    private fun showDatePicker(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val sDayOfMonth = if(dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear = if((month + 1)<10) "0${month+1}" else "${month+1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"

                tv_select_due_date.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                val theDate = sdf.parse(selectedDate)

                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )

        dpd.show()
    }

}


















