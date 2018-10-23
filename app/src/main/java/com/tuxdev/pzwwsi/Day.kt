package com.tuxdev.pzwwsi

data class Day(
        val day : String
) {
    // block number, list of lectures if there are more in the same block but on different meetings
    val plan = HashMap<Int,ArrayList<Lecture>>()
}
