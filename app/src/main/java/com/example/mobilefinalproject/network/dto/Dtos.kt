package com.example.mobilefinalproject.network.dto

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String,
    val role: String   // "customer" | "driver"
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String = "bearer",
    @SerializedName("expires_in") val expiresIn: Int
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class CustomerRegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String,
    val phone: String? = null
)

data class DriverRegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String,
    val phone: String? = null,
    @SerializedName("license_number") val licenseNumber: String,
    @SerializedName("vehicle_type") val vehicleType: String,
    @SerializedName("vehicle_plate") val vehiclePlate: String,
    @SerializedName("vehicle_capacity_kg") val vehicleCapacityKg: Double? = null
)

data class GoogleIdTokenRequest(
    @SerializedName("id_token") val idToken: String,
    val role: String = "customer"
)

// ── Users ─────────────────────────────────────────────────────────────────────

data class UserMe(
    val id: Int,
    @SerializedName("full_name") val fullName: String,
    val role: String,
    @SerializedName("profile_image_url") val profileImageUrl: String?,
    @SerializedName("created_at") val createdAt: String,
    val email: String,
    val phone: String?,
    @SerializedName("is_active") val isActive: Boolean
)

data class UserPublic(
    val id: Int,
    @SerializedName("full_name") val fullName: String,
    val role: String,
    @SerializedName("profile_image_url") val profileImageUrl: String?,
    @SerializedName("created_at") val createdAt: String
)

data class UserUpdateRequest(
    @SerializedName("full_name") val fullName: String? = null,
    val phone: String? = null,
    @SerializedName("profile_image_url") val profileImageUrl: String? = null
)

// ── Drivers ───────────────────────────────────────────────────────────────────

data class DriverProfile(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("license_number") val licenseNumber: String,
    @SerializedName("vehicle_type") val vehicleType: String,
    @SerializedName("vehicle_plate") val vehiclePlate: String,
    @SerializedName("vehicle_capacity_kg") val vehicleCapacityKg: Double?,
    val status: String,   // "offline" | "available" | "busy"
    @SerializedName("current_lat") val currentLat: Double?,
    @SerializedName("current_lng") val currentLng: Double?,
    @SerializedName("last_location_at") val lastLocationAt: String?,
    val rating: Double
)

data class DriverProfileUpdateRequest(
    @SerializedName("vehicle_type") val vehicleType: String? = null,
    @SerializedName("vehicle_plate") val vehiclePlate: String? = null,
    @SerializedName("vehicle_capacity_kg") val vehicleCapacityKg: Double? = null
)

data class DriverStatusUpdateRequest(
    val status: String  // "offline" | "available" | "busy"
)

data class DriverLocationUpdateRequest(
    val lat: Double,
    val lng: Double
)

// ── Orders ────────────────────────────────────────────────────────────────────

data class OrderRead(
    val id: Int,
    @SerializedName("customer_id") val customerId: Int,
    @SerializedName("driver_id") val driverId: Int?,
    val status: String,
    @SerializedName("pickup_address") val pickupAddress: String,
    @SerializedName("pickup_lat") val pickupLat: Double,
    @SerializedName("pickup_lng") val pickupLng: Double,
    @SerializedName("dropoff_address") val dropoffAddress: String,
    @SerializedName("dropoff_lat") val dropoffLat: Double,
    @SerializedName("dropoff_lng") val dropoffLng: Double,
    @SerializedName("cargo_description") val cargoDescription: String?,
    @SerializedName("cargo_weight_kg") val cargoWeightKg: Double?,
    val notes: String?,
    @SerializedName("price_cents") val priceCents: Int,
    val currency: String,
    @SerializedName("accepted_at") val acceptedAt: String?,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("picked_up_at") val pickedUpAt: String?,
    @SerializedName("completed_at") val completedAt: String?,
    @SerializedName("cancelled_at") val cancelledAt: String?,
    @SerializedName("cancellation_reason") val cancellationReason: String?,
    @SerializedName("cargo_image_url") val cargoImageUrl: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class OrderCreateRequest(
    @SerializedName("pickup_address") val pickupAddress: String,
    @SerializedName("pickup_lat") val pickupLat: Double,
    @SerializedName("pickup_lng") val pickupLng: Double,
    @SerializedName("dropoff_address") val dropoffAddress: String,
    @SerializedName("dropoff_lat") val dropoffLat: Double,
    @SerializedName("dropoff_lng") val dropoffLng: Double,
    @SerializedName("cargo_description") val cargoDescription: String? = null,
    @SerializedName("cargo_weight_kg") val cargoWeightKg: Double? = null,
    val notes: String? = null,
    @SerializedName("price_cents") val priceCents: Int,
    val currency: String = "USD"
)

data class OrderUpdateRequest(
    @SerializedName("pickup_address") val pickupAddress: String? = null,
    @SerializedName("pickup_lat") val pickupLat: Double? = null,
    @SerializedName("pickup_lng") val pickupLng: Double? = null,
    @SerializedName("dropoff_address") val dropoffAddress: String? = null,
    @SerializedName("dropoff_lat") val dropoffLat: Double? = null,
    @SerializedName("dropoff_lng") val dropoffLng: Double? = null,
    @SerializedName("cargo_description") val cargoDescription: String? = null,
    @SerializedName("cargo_weight_kg") val cargoWeightKg: Double? = null,
    val notes: String? = null,
    @SerializedName("price_cents") val priceCents: Int? = null,
    val currency: String? = null,
    @SerializedName("cargo_image_url") val cargoImageUrl: String? = null
)

data class OrderCancelRequest(
    val reason: String? = null
)

data class PageResponse<T>(
    val items: List<T>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

// ── Ratings ───────────────────────────────────────────────────────────────────

data class RatingCreate(
    val score: Int,
    val comment: String? = null
)

data class RatingRead(
    val id: Int,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("driver_id") val driverId: Int,
    @SerializedName("customer_id") val customerId: Int,
    val score: Int,
    val comment: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("driver_response") val driverResponse: String?,
    @SerializedName("driver_responded_at") val driverRespondedAt: String?
)

data class RatingResponseCreate(
    val response: String
)

// ── AI Price ──────────────────────────────────────────────────────────────────

data class AiPriceRequest(
    val message: String
)

data class AiPriceResponse(
    val result: String
)

// ── Chat ──────────────────────────────────────────────────────────────────────

data class MessageSender(
    val id: Int,
    @SerializedName("full_name") val fullName: String
)

data class ChatMessage(
    val id: Int,
    @SerializedName("conversation_id") val conversationId: Int,
    @SerializedName("sender_id") val senderId: Int,
    val sender: MessageSender,
    val body: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_read") val isRead: Boolean = false
)

data class ConversationSummary(
    val id: Int,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("last_message") val lastMessage: ChatMessage?,
    @SerializedName("unread_count") val unreadCount: Int = 0,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class ConversationDetail(
    val id: Int,
    @SerializedName("order_id") val orderId: Int,
    val messages: List<ChatMessage>,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class SendMessageRequest(
    val body: String
)
