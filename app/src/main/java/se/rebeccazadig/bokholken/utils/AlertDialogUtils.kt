package se.rebeccazadig.bokholken.utils

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import se.rebeccazadig.bokholken.R

data class DialogMessages(
    val titleText: Int,
    val positiveButtonText: Int,
    val negativeButtonText: Int
)

fun showConfirmationDialog(
    context: Context,
    titleResId: Int,
    messageResId: Int,
    positiveAction: () -> Unit
) {
    AlertDialog.Builder(context)
        .setTitle(titleResId)
        .setMessage(messageResId)
        .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
            positiveAction.invoke()
        }
        .setNegativeButton(context.getString(R.string.cancel), null)
        .show()
}

fun showAlertWithEditText(
    context: Context,
    view: View,
    editTexts: List<EditText>,
    dialogMessages: DialogMessages,
    confirmCallback: () -> Unit,
    cancelCallback: (() -> Unit) ?= null,
    textEditCallback: (Editable?, Button) -> Unit
) :AlertDialog {
    val alertDialog = AlertDialog.Builder(context)
        .setTitle(dialogMessages.titleText)
        .setView(view)
        .setPositiveButton(dialogMessages.positiveButtonText, null)
        .setNegativeButton(dialogMessages.negativeButtonText) { _, _ ->
            cancelCallback?.invoke()
        }
        .create()

    alertDialog.setOnShowListener {
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.isEnabled = false

        val checkAndUpdateButtonState = {
            positiveButton.isEnabled = editTexts.all {it.text.toString().trim().isNotEmpty() }
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = checkAndUpdateButtonState()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        editTexts.forEach { editText ->
            editText.addTextChangedListener(textWatcher)
            textEditCallback(editText.text, positiveButton)
        }

        positiveButton.setOnClickListener {
            confirmCallback.invoke()
            alertDialog.dismiss()
        }

        alertDialog.setOnDismissListener {
            editTexts.forEach { editText -> editText.removeTextChangedListener(textWatcher) }
        }
    }

    alertDialog.show()
    return alertDialog
}