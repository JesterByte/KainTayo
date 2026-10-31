package com.example.kaintayo.data.local

import android.content.Context
import androidx.room.Room

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
                .build()
                .also { INSTANCE = it }
        }
    }
}