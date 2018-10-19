package com.tuxdev.pzwwsi

data class Day(
        val day : String
) {
    val plan = HashMap<Int,ArrayList<Lecture>>()
}
