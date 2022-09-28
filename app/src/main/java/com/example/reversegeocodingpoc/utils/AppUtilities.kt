package com.example.reversegeocodingpoc.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.text.NumberFormat
import java.util.*
import java.util.regex.Pattern

object AppUtilities {
    fun decimalValidation(
        data: String,
        beforeDecimalLength: Int,
        afterDecimalLength: Int
    ): String {
        var beforedecimallengthformatted = beforeDecimalLength
        if (data.contains("-")) {
            beforedecimallengthformatted += 1
        }
        if (data.contains(".")) {
            val beforeDecimal = splitFromDotAcresValidation(data)[0]
            val afterDecimal = splitFromDotAcresValidation(data)[1]
            var afterDecimalFinal = ""
            if (afterDecimal.length > afterDecimalLength) {
                afterDecimalFinal = afterDecimal.substring(0, afterDecimalLength)
            }
            if (beforeDecimal.length > beforedecimallengthformatted) {

                val beforeDecimalFinal = beforeDecimal.substring(0, beforedecimallengthformatted)
                return "$beforeDecimalFinal.$afterDecimalFinal"
            }
            return if (afterDecimalFinal == "") {
                "$beforeDecimal.0"
            } else {
                "$beforeDecimal.$afterDecimalFinal"
            }
        } else {
            if (data.length > beforedecimallengthformatted) {
                return data.substring(0, beforedecimallengthformatted)
            }
        }
        return ""
    }

     fun splitFromDotAcresValidation(acresSize: String): List<String> {
        return acresSize.split(".")
    }

    fun showShortToast(view: View, message: String) {
        Toast.makeText(view.context, message, Toast.LENGTH_SHORT).show()
    }

    fun hideSoftInputKeyboard(context: Activity) {
        try {
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputManager!!.hideSoftInputFromWindow(
                context.currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun validLatLng(lat_lng: String): Boolean {
        val latLngPattern: String = when {
            lat_lng.contains(".") && lat_lng.contains("-") ->
                "[-][0-9]{1,}.[0-9]{1,}"

            lat_lng.contains("-") ->
                "[-][0-9]{1,}"

            lat_lng.contains(".") ->
                "[0-9]{1,}.[0-9]{1,}"

            else ->
                "[0-9]{1,}"
        }

        val pattern = Pattern.compile(latLngPattern)
        val matcher = pattern.matcher(lat_lng)
        return matcher.matches()
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun convertingNumbersToEnglish(number: String): Double {
        val defaultLocale = Resources.getSystem().configuration.locales
        val displayLang = defaultLocale.get(0).language
        val format = NumberFormat.getInstance(Locale(displayLang, "IND"))
        val parse: Number?
        try {
            if (number.isNotEmpty()) {
                parse = format.parse(number)
                return parse!!.toDouble()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0.0
    }
}