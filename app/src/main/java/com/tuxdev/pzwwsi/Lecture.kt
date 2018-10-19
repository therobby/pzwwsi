package com.tuxdev.pzwwsi


data class Lecture (
        private val data : String,
        val room : Int
){
    val name = data.takeWhile { it != '-' }
    val meet = data.takeLastWhile { it != '(' }.replace(")","")
    val prof = data.dropWhile { it != '-' }.dropLastWhile { it != '(' }.dropWhile { !it.isLetter() }.dropLastWhile { !it.isLetter() }


    /*init {
        println(name)
        println(meet)
        println(prof)
    }*/
}