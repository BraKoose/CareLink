package com.example.data.repository

import com.example.data.db.CareLinkDao
import com.example.data.model.AnonymizedUser
import com.example.data.model.MedicationLog
import com.example.data.model.MedicationReminder
import com.example.data.model.DeliveryOrder
import kotlinx.coroutines.flow.Flow

class CareLinkRepository(private val dao: CareLinkDao) {

    val anonymizedProfile: Flow<AnonymizedUser?> = dao.getAnonymizedProfile()
    val allMedicationLogs: Flow<List<MedicationLog>> = dao.getAllMedicationLogs()
    val allReminders: Flow<List<MedicationReminder>> = dao.getAllReminders()
    val allOrders: Flow<List<DeliveryOrder>> = dao.getAllOrders()

    suspend fun saveProfile(user: AnonymizedUser) {
        dao.insertAnonymizedProfile(user)
    }

    suspend fun logMedication(log: MedicationLog) {
        dao.insertMedicationLog(log)
    }

    suspend fun addReminder(reminder: MedicationReminder) {
        dao.insertReminder(reminder)
    }

    suspend fun updateReminderStatus(id: Int, completed: Boolean) {
        dao.updateReminderStatus(id, completed)
    }

    suspend fun deleteReminder(id: Int) {
        dao.deleteReminderById(id)
    }

    suspend fun placeOrder(order: DeliveryOrder) {
        dao.insertOrder(order)
    }

    suspend fun updateOrderStatus(id: Int, status: String) {
        dao.updateOrderStatus(id, status)
    }
}
