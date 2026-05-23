package com.example.mobilefinalproject.models

enum class DeliveryStatus(val label: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");


    override fun toString(): String = label
}

// Helper factory + formatter for API status values (e.g. "in_progress")
fun String.toStatusLabel(): String {
    // Try to map known API values to enum labels first
    DeliveryStatus.values().forEach { ds ->
        // compare against common API keys (lowercase, underscores)
        val key = when (ds) {
            DeliveryStatus.IN_PROGRESS -> "in_progress"
            else -> ds.name.lowercase()
        }
        if (this.equals(key, ignoreCase = true)) return ds.label
    }

    // Fallback: convert snake_case or plain lowercase into Title Case
    return this.replace('_', ' ').split(' ').joinToString(" ") {
        it.lowercase().replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
    }
}
