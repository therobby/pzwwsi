package com.tuxdev.pzwwsi

import java.text.SimpleDateFormat
import java.util.Calendar

data class DatePackage(
        val nr : Int,
        val dateA : String,
        val dateB : String
) {

    fun currentWeek() : Boolean {

        // dateB is not supported yet

        if(nr >= 15)
            return false    // not supported yet

        val date = if(dateA.contains(" oraz ")){
            dateA.split(" oraz ").last()
        }
        else{
            dateA.dropWhile { it != '-' }.drop(1)
        }

        val currentCalendar = Calendar.getInstance()
        val week = currentCalendar.get(Calendar.WEEK_OF_YEAR)
        val year = currentCalendar.get(Calendar.YEAR)
        val targetCalendar = Calendar.getInstance()

        targetCalendar.time = SimpleDateFormat("dd.MM.yyy").parse(date)

        val targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR)
        val targetYear = targetCalendar.get(Calendar.YEAR)
        return week == targetWeek && year == targetYear
    }
}