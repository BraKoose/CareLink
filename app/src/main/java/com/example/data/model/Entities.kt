package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anonymized_users")
data class AnonymizedUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val aliasName: String, // e.g. "Warrior-Alpha" or hashed identifier
    val hashedId: String,  // anonymized SHA-256 identifier
    val currentRiskScore: Int = -1, // -1 means not calculated yet
    val riskAssessmentLevel: String = "Unknown", // e.g. "Low", "Medium", "High"
    val quizCompletedAt: Long = 0
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medType: String, // e.g. "ARV" or "PrEP"
    val dateMillis: Long, // normalized to start of day
    val taken: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medication_reminders")
data class MedicationReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String, // e.g., "Daily Dose - PrEP", "Clinic Refill Pickup"
    val timeString: String, // e.g., "08:30 AM" or "July 15, 2026"
    val type: String, // "DAILY_DOSE" or "CLINIC_PICKUP"
    val notes: String = "", // e.g. "Korle Bu Pharmacy"
    val isCompleted: Boolean = false
)

@Entity(tableName = "delivery_orders")
data class DeliveryOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String,
    val priceGhs: Double,
    val quantity: Int = 1,
    val isPrescriptionRequired: Boolean,
    val prescriptionPhotoPath: String? = null, // Path or indicator of upload
    val trackingStatus: String, // "Pending Verification", "Dispatched", "In Transit", "Delivered"
    val orderDateMillis: Long = System.currentTimeMillis()
)
