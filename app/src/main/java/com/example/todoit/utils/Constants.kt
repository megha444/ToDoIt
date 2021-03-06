package com.example.todoit.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
object Constants {
    const val USERS: String="Users"
    const val NAME: String="name"
    const val IMAGE: String="image"
    const val MOBILE: String="mobile"

    const val READ_STORAGE_PERMISSION_CODE= 1
    const val PICK_IMAGE_REQUEST_CODE=2

    const val BOARDS: String = "Boards"
    const val ASSIGNED_TO: String = "assignedTo"
    const val DOCUMENT_ID: String = "documentId"
    const val TASK_LIST: String = "taskList"

    const val BOARD_DETAIL : String = "board_detail"
    const val ID: String = "id"

    const val EMAIL : String = "email"

    const val TASK_LIST_ITEM_POSITION : String = "task_list_item_position"
    const val CARD_ITEM_POSITION : String = "card_item_position"

    const val BOARD_MEMBERS_LIST : String = "booard_members_list"

    const val SELECT : String = "select"
    const val UNSELECT : String = "unselect"

    const val ToDoItPreferences = "Todoit_prefs"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"

    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAAZzBEcZU:APA91bEBMX1N3aT81BBng7jDgM5wXSBLLFZCjdKC-twciNCnfGR68RBq1PNiLVBv4uaazC2Vra5uhaj4qFb1gy620iDJdXNah9lFS5Wh-uKDwlFDipLKblKdUmRI1jtF6yUJzBhp4Jb8"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"


    fun showImageChooser(activity: Activity){
        var galleryIntent= Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String?{
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}