package com.artyom.gpstracker_hdbh.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.artyom.gpstracker_hdbh.R

object DialogManager {

    /*Создаёт и показывает диалоговое окно для того
    * что бы пользователь мог включить GPS*/
    fun showLocEnableDialog(context: Context, listener: DialogManager.Listener){

        /*создать стандартный диалог с сьандартной разметкой*/
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()


        dialog.setTitle(R.string.location_disabled)
        dialog.setMessage(context.getString(R.string.location_dialog_message))

        /*Кнопки*/
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,"Yes"){ _, _ ->
            //Toast.makeText(context, "Yes", Toast.LENGTH_SHORT).show()
            listener.onClick()
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"No"){ _, _ ->

            //Toast.makeText(context, "No", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()

    }

    interface Listener{
        fun onClick()
    }
}