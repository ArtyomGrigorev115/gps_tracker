package com.artyom.gpstracker_hdbh.utils

import android.app.Activity
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