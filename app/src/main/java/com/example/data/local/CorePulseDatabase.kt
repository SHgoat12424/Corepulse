package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ConnectionProfile
import com.example.data.model.DiagnosticSession

@Database(
    entities = [ConnectionProfile::class, DiagnosticSession::class],
    version = 1,
    exportSchema = false
)
abstract class CorePulseDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun diagnosticDao(): DiagnosticDao

    companion object {
        @Volatile
        private var INSTANCE: CorePulseDatabase? = null

        fun getDatabase(context: Context): CorePulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CorePulseDatabase::class.java,
                    "corepulse_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
