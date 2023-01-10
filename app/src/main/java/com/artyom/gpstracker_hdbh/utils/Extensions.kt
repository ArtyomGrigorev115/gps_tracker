package com.artyom.gpstracker_hdbh.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.artyom.gpstracker_hdbh.R

/*Файл с функциями-расширения*/

/*Функция расширения  класса  Fragments для переключения между фрагментами*/
fun Fragment.openFragment(f: Fragment){

    /*Заменяем активный фрагмент на новый*/
    (activity as AppCompatActivity).supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        .replace(R.id.placeHolder, f)
        .commit()
}


/*Функция расширения  класса  AppCompatActivity для переключения между фрагментами*/
fun AppCompatActivity.openFragment(f: Fragment){
    Log.d("MyLog","Fragment name: ${f.javaClass}")
    Log.d("MyLog","Фрагменты в списке: ${supportFragmentManager.fragments.size}")

    /*Проверка фрагментов, что бы запретить вызов одного и того же фрагмента бесконечное кол-во раз*/
    if(supportFragmentManager.fragments.isNotEmpty()){
        /*Если названия фрагментов совпадают, то никакие фрагменты не открываем и выходим из метода*/
        if(supportFragmentManager.fragments[0].javaClass == f.javaClass){
            return
        }

    }

    /*Заменяем активный фрагмент на новый*/
    supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        .replace(R.id.placeHolder, f)
        .commit()
}

/*Отладочная функция расширения Фрагмента
* позазывает тост с сообщением*/
fun Fragment.showToast(text: String){
    Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show()
}

/*Отладочная функция расширения Активности
* позазывает тост с сообщением*/
fun AppCompatActivity.showToast(text: String){
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}