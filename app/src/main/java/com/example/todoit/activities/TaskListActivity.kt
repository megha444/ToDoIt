package com.example.todoit.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoit.R
import com.example.todoit.adapters.TaskListItemsAdapter
import com.example.todoit.firebase.FirestoreClass
import com.example.todoit.model.Board
import com.example.todoit.model.Card
import com.example.todoit.model.Task
import com.example.todoit.model.User
import com.example.todoit.utils.Constants

@Suppress("DEPRECATION")
class TaskListActivity : BaseActivity() {

    private lateinit var toolbar_task_list_activity: Toolbar
    private lateinit var rv_task_list : RecyclerView

    private lateinit var mBoardDetails: Board

    private lateinit var mBoardDocumentId : String

    lateinit var mAssignedMembersDetailList : ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if(intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        rv_task_list= findViewById(R.id.rv_task_list)

        toolbar_task_list_activity=findViewById(R.id.toolbar_task_list_activity)
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getBoardDetails(this, mBoardDocumentId)
    }

    private fun setUpActionBar() {

        setSupportActionBar(toolbar_task_list_activity)

        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.title= mBoardDetails.name
        }

        toolbar_task_list_activity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu : Menu?): Boolean{
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members -> {

                val intent= Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAIL_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this, mBoardDocumentId)
        }else{
            Log.e("Cancelled", "cancelled")
        }
    }


    fun boardDetails(board: Board){
        mBoardDetails = board
        hideProgressDialog()
        setUpActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersDetails(this@TaskListActivity, mBoardDetails.assignedTo)

    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, mBoardDetails.documentId)

    }

    fun createTaskList(taskListName : String){
        val task = Task(taskListName, FirestoreClass().getCurrentUserID())

        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun updateTaskList(position : Int, listName: String, model: Task){
        val task = Task(listName, model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }


    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }


    fun addCardToTaskList(position: Int, cardName: String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUsersList : ArrayList<String> = ArrayList()

        cardAssignedUsersList.add(FirestoreClass().getCurrentUserID())
        val card = Card(cardName, FirestoreClass().getCurrentUserID(), cardAssignedUsersList)

        val cardList = mBoardDetails.taskList[position].cards

        cardList.add(card)

        val task = Task(mBoardDetails.taskList[position].title,
        mBoardDetails.taskList[position].createdBy,
        cardList)

        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)

    }

    fun cardDetails(taskListPosition : Int, cardPosition : Int){
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)
        startActivityForResult(intent, CARD_DETAIL_REQUEST_CODE)
    }

    fun boardMembersDetailList(list : ArrayList<User>){
        mAssignedMembersDetailList = list
        hideProgressDialog()
        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager= LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rv_task_list.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, mBoardDetails.taskList)
        rv_task_list.adapter= adapter
    }

    fun updateCardsOnTaskList(taskListPosition : Int, cards : ArrayList<Card>){
        mBoardDetails.taskList.removeAt((mBoardDetails.taskList.size - 1))
        mBoardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    companion object{
        const val MEMBERS_REQUEST_CODE : Int = 13
        const val CARD_DETAIL_REQUEST_CODE : Int =14
    }
}