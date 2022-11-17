package com.example.actiontest.utils

import java.text.SimpleDateFormat
import java.util.*

//Extension Function
fun Date.stringDateFormat(format: String): String {
    val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
    //return the formatted date string
    return dateFormatter.format(this)
}