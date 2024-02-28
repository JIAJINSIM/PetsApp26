package com.example.petsapp26
import com.google.firebase.firestore.GeoPoint

data class VetClinic(
    var id: String = "",
    val name: String = "",
    val services: List<String> = listOf(),
    val rating: Double = 0.0,
    val location: Location = Location()
)

data class Location(
    val address: String = "",
    val area: String = "",
    val geo: GeoPoint = GeoPoint(0.0, 0.0)
)