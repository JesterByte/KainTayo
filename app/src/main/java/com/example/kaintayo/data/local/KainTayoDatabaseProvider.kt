package com.example.kaintayo.data.local

import android.content.Context
import androidx.room.Room
import com.example.kaintayo.data.local.DatabaseMigrations.MIGRATION_1_2
import com.example.kaintayo.data.local.DatabaseMigrations.MIGRATION_2_3

object KainTayoDatabaseProvider {

    @Volatile
    private var INSTANCE: KainTayoDatabase? = null

    fun getDatabase(context: Context): KainTayoDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                KainTayoDatabase::class.java,
                "kaintayo_database"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { INSTANCE = it }
        }
    }
}