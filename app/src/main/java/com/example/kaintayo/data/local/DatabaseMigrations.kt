package com.example.kaintayo.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {

        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                """
                ALTER TABLE ingredients
                ADD COLUMN purchaseQuantity REAL NOT NULL DEFAULT 1.0
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE ingredients
                ADD COLUMN purchaseUnit TEXT NOT NULL DEFAULT 'PIECE'
                """.trimIndent()
            )

            db.execSQL(
                """
                UPDATE ingredients
                SET purchaseQuantity = priceQuantity,
                    purchaseUnit = priceUnit
                """.trimIndent()
            )
        }
    }
}
