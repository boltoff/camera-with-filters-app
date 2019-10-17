package ru.boltoff.camera_with_filters_app.helper.extension

import android.app.Activity
import android.app.AlertDialog
import androidx.core.content.ContextCompat
import ru.boltoff.camera_with_filters_app.R

fun Activity.showPermissionAlertDialog(
    onPositiveButtonClick: () -> Unit = {}
) {
    val alertDialog = AlertDialog.Builder(this)
        .setTitle(R.string.permissions_dialog_title)
        .setMessage(R.string.permissions_dialog_text)
        .setPositiveButton(R.string.permissions_dialog_button_positive) { dialog, _ ->
            onPositiveButtonClick()
            dialog.dismiss()
        }
        .setNegativeButton(R.string.permissions_dialog_button_negative) { _, _ ->
            finish()
        }
        .create()
    with(alertDialog) {
        setOnShowListener {
            val color = ContextCompat.getColor(this@showPermissionAlertDialog, R.color.black)
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color)
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color)
        }
        setCancelable(false)
        show()
    }
}