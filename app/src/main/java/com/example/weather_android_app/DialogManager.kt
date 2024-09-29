package com.example.weather_android_app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.widget.EditText
import androidx.core.content.ContextCompat

object DialogManager {
    fun locationSettingsDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("GPS disabled!")
        dialog.setMessage("Do you want to enable GPS?")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK"){_,_, ->
            listener.onClick(null)
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel"){_,_, ->
            dialog.dismiss()
        }
        dialog.show()
    }

    fun searchByNameDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val edName = EditText(context)
        builder.setView(edName)
        val dialog = builder.create()
        dialog.setTitle("Your city:")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK"){_,_, ->
            listener.onClick(edName.text.toString())
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel"){_,_, ->
            dialog.dismiss()
        }
        dialog.show()

        val okBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okBtn?.setTextColor(ContextCompat.getColor(context, R.color.dialog_ok))

        val cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        cancelBtn?.setTextColor(ContextCompat.getColor(context, R.color.dialog_cancel))
    }

    interface Listener{
        fun onClick(name: String?)
    }
}