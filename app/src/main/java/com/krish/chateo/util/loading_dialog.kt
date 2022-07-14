package com.krish.chateo.util

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.krish.chateo.R

private lateinit var dialog: Dialog
fun showLoadingDialog(context: Context, message: String) {
    dialog = Dialog(context)
    dialog.apply {
        setCancelable(false)
        setContentView(R.layout.loading_dialog)
        val loadingMessage = findViewById<TextView>(R.id.tvLoading)
        loadingMessage.text = message
        dialog.show()
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


fun dismissDialogBox() {
    dialog.dismiss()
}