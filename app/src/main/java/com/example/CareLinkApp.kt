package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.repository.CareLinkRepository

class CareLinkApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: CareLinkRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "carelink_db"
        )
        .fallbackToDestructiveMigration() // safe for rapid prototyping prototypes
        .build()

        repository = CareLinkRepository(database.careLinkDao())
    }
}
