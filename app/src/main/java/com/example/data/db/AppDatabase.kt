package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.data.model.AnonymizedUser
import com.example.data.model.MedicationLog
import com.example.data.model.MedicationReminder
import com.example.data.model.DeliveryOrder
import com.example.data.model.PeriodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CareLinkDao {

    // --- Anonymized User / Profile ---
    @Query("SELECT * FROM anonymized_users LIMIT 1")
    fun getAnonymizedProfile(): Flow<AnonymizedUser?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnonymizedProfile(user: AnonymizedUser)

    // --- Medication Logging ---
    @Query("SELECT * FROM medication_logs ORDER BY dateMillis DESC")
    fun getAllMedicationLogs(): Flow<List<MedicationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationLog(log: MedicationLog)

    // --- Medication Reminders & Clinic Pickups ---
    @Query("SELECT * FROM medication_reminders ORDER BY isCompleted ASC, id DESC")
    fun getAllReminders(): Flow<List<MedicationReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MedicationReminder)

    @Query("UPDATE medication_reminders SET isCompleted = :completed WHERE id = :id")
    suspend fun updateReminderStatus(id: Int, completed: Boolean)

    @Query("DELETE FROM medication_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)

    // --- Storefront Purchases & Deliveries ---
    @Query("SELECT * FROM delivery_orders ORDER BY orderDateMillis DESC")
    fun getAllOrders(): Flow<List<DeliveryOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: DeliveryOrder)

    @Query("UPDATE delivery_orders SET trackingStatus = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Int, status: String)

    // --- Period Tracker Logs ---
    @Query("SELECT * FROM period_logs ORDER BY dateMillis DESC")
    fun getAllPeriodLogs(): Flow<List<PeriodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriodLog(log: PeriodLog)

    @Query("DELETE FROM period_logs WHERE id = :id")
    suspend fun deletePeriodLogById(id: Int)
}

@Database(
    entities = [
        AnonymizedUser::class,
        MedicationLog::class,
        MedicationReminder::class,
        DeliveryOrder::class,
        PeriodLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun careLinkDao(): CareLinkDao
}
