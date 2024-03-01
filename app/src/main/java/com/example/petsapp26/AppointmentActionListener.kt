package com.example.petsapp26

interface AppointmentActionListener {
    fun deleteAppointment(appointment: Appointment)
    fun onEditAppointment(appointment: Appointment)
}