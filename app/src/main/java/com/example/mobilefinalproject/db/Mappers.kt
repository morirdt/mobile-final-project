package com.example.mobilefinalproject.db

import com.example.mobilefinalproject.db.entity.ChatMessageEntity
import com.example.mobilefinalproject.db.entity.ConversationEntity
import com.example.mobilefinalproject.db.entity.DriverProfileEntity
import com.example.mobilefinalproject.db.entity.OrderEntity
import com.example.mobilefinalproject.db.entity.UserEntity
import com.example.mobilefinalproject.network.dto.ChatMessage
import com.example.mobilefinalproject.network.dto.ConversationDetail
import com.example.mobilefinalproject.network.dto.ConversationSummary
import com.example.mobilefinalproject.network.dto.DriverProfile
import com.example.mobilefinalproject.network.dto.MessageSender
import com.example.mobilefinalproject.network.dto.OrderRead
import com.example.mobilefinalproject.network.dto.UserMe

// ── OrderRead ↔ OrderEntity ────────────────────────────────────────────────

fun OrderRead.toEntity(listType: String) = OrderEntity(
    orderId = id,
    listType = listType,
    customerId = customerId,
    driverId = driverId,
    status = status,
    pickupAddress = pickupAddress,
    pickupLat = pickupLat,
    pickupLng = pickupLng,
    dropoffAddress = dropoffAddress,
    dropoffLat = dropoffLat,
    dropoffLng = dropoffLng,
    cargoDescription = cargoDescription,
    cargoWeightKg = cargoWeightKg,
    notes = notes,
    priceCents = priceCents,
    currency = currency,
    acceptedAt = acceptedAt,
    startedAt = startedAt,
    pickedUpAt = pickedUpAt,
    completedAt = completedAt,
    cancelledAt = cancelledAt,
    cancellationReason = cancellationReason,
    cargoImageUrl = cargoImageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun OrderEntity.toDto() = OrderRead(
    id = orderId,
    customerId = customerId,
    driverId = driverId,
    status = status,
    pickupAddress = pickupAddress,
    pickupLat = pickupLat,
    pickupLng = pickupLng,
    dropoffAddress = dropoffAddress,
    dropoffLat = dropoffLat,
    dropoffLng = dropoffLng,
    cargoDescription = cargoDescription,
    cargoWeightKg = cargoWeightKg,
    notes = notes,
    priceCents = priceCents,
    currency = currency,
    acceptedAt = acceptedAt,
    startedAt = startedAt,
    pickedUpAt = pickedUpAt,
    completedAt = completedAt,
    cancelledAt = cancelledAt,
    cancellationReason = cancellationReason,
    cargoImageUrl = cargoImageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ── UserMe ↔ UserEntity ────────────────────────────────────────────────────

fun UserMe.toEntity() = UserEntity(
    id = id,
    fullName = fullName,
    role = role,
    profileImageUrl = profileImageUrl,
    createdAt = createdAt,
    email = email,
    phone = phone,
    isActive = isActive
)

fun UserEntity.toDto() = UserMe(
    id = id,
    fullName = fullName,
    role = role,
    profileImageUrl = profileImageUrl,
    createdAt = createdAt,
    email = email,
    phone = phone,
    isActive = isActive
)

// ── DriverProfile ↔ DriverProfileEntity ───────────────────────────────────

fun DriverProfile.toEntity() = DriverProfileEntity(
    id = id,
    userId = userId,
    licenseNumber = licenseNumber,
    vehicleType = vehicleType,
    vehiclePlate = vehiclePlate,
    vehicleCapacityKg = vehicleCapacityKg,
    status = status,
    currentLat = currentLat,
    currentLng = currentLng,
    lastLocationAt = lastLocationAt,
    rating = rating
)

fun DriverProfileEntity.toDto() = DriverProfile(
    id = id,
    userId = userId,
    licenseNumber = licenseNumber,
    vehicleType = vehicleType,
    vehiclePlate = vehiclePlate,
    vehicleCapacityKg = vehicleCapacityKg,
    status = status,
    currentLat = currentLat,
    currentLng = currentLng,
    lastLocationAt = lastLocationAt,
    rating = rating
)

// ── ConversationSummary ↔ ConversationEntity ───────────────────────────────

fun ConversationSummary.toEntity() = ConversationEntity(
    id = id,
    orderId = orderId,
    lastMessageBody = lastMessage?.body,
    lastMessageSenderId = lastMessage?.senderId,
    lastMessageSenderName = lastMessage?.sender?.fullName,
    lastMessageCreatedAt = lastMessage?.createdAt,
    unreadCount = unreadCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ConversationEntity.toSummaryDto() = ConversationSummary(
    id = id,
    orderId = orderId,
    lastMessage = if (lastMessageBody != null && lastMessageSenderId != null) {
        ChatMessage(
            id = 0,
            conversationId = id,
            senderId = lastMessageSenderId,
            sender = MessageSender(
                id = lastMessageSenderId,
                fullName = lastMessageSenderName.orEmpty()
            ),
            body = lastMessageBody,
            createdAt = lastMessageCreatedAt.orEmpty(),
            isRead = true
        )
    } else null,
    unreadCount = unreadCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ── ChatMessage ↔ ChatMessageEntity ───────────────────────────────────────

fun ChatMessage.toEntity(convId: Int) = ChatMessageEntity(
    id = id,
    conversationId = convId,
    senderId = senderId,
    senderName = sender.fullName,
    body = body,
    createdAt = createdAt,
    isRead = isRead
)

fun ChatMessageEntity.toDto() = ChatMessage(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    sender = MessageSender(id = senderId, fullName = senderName),
    body = body,
    createdAt = createdAt,
    isRead = isRead
)

// ── ConversationDetail helpers ─────────────────────────────────────────────

fun ConversationDetail.messageEntities() =
    messages.map { it.toEntity(id) }

