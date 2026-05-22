package com.example.mobilefinalproject.models

enum class DeliveryStatus(val label: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");


    override fun toString(): String = label
}