package com.kotlin_project.tinder

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText

class UserNameDialog(context: Context) {
    private val dialog = Dialog(context)

    private lateinit var onClickListener: ButtonClickListener

    fun setOnClickListener(listener: ButtonClickListener) {
        onClickListener = listener
    }

    fun showDialog() {
        dialog.setContentView(R.layout.user_name_dialog_layout)

        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        val userNameEditText = dialog.findViewById<EditText>(R.id.userNameEditText)
        val saveButton = dialog.findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            onClickListener.onSaveButtonClick(userNameEditText.text.toString())
            if (userNameEditText.text.isNotEmpty())
                dialog.dismiss()
        }

        dialog.show()
    }

    interface ButtonClickListener {
        fun onSaveButtonClick(userName: String)
    }
}