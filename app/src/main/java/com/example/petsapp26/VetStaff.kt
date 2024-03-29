package com.example.petsapp26

data class VetStaff(
    val name: String = "",
    val experience: String = "",
    val qualification: String = "",
    val specialisation: String = "",
    val uid: String = ""
) {
    constructor() : this("", "", "", "")
}