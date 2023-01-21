package com.artyom.gpstracker_hdbh.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Toast
import com.artyom.gpstracker_hdbh.R
import com.artyom.gpstracker_hdbh.databinding.SaveDialogBinding
import com.artyom.gpstracker_hdbh.db.TrackItem

object DialogManager {

    /*Создаёт и показывает диалоговое окно для того
    * что бы пользователь мог включить GPS*/
    fun showLocEnableDialog(context: Context, listener: DialogManager.Listener){

        /*создать стандартный диалог с стандартной разметкой*/
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

    fun showSaveDialog(context: Context, item: TrackItem?, listener: DialogManager.Listener){

        /*диалог сохранения*/
        val builder = AlertDialog.Builder(context)
        val binding = SaveDialogBinding.inflate(LayoutInflater.from(context),null, false)
        builder.setView(binding.root)
        val dialog = builder.create()

        binding.apply {


            val time = "${item?.time} s"
            val speed = "${item?.velocity} km/h"
            val distance = "${item?.distance} km"

            /*Текстовые поля*/
            tvTime.text = time
            tvSpeed.text = speed
            tvDistance.text = distance

            bSave.setOnClickListener{
                listener.onClick()
                dialog.dismiss()
            }

            bCancel.setOnClickListener {

                dialog.dismiss()
            }
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()


    }

    interface Listener{
        fun onClick()
    }
}