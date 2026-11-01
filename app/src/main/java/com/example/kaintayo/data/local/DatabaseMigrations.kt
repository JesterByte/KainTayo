
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

    val MIGRATION_2_3 = object : Migration(2, 3) {

        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pantry_stock (
                    ingredientId TEXT NOT NULL,
                    quantity REAL NOT NULL,
                    unit TEXT NOT NULL,
                    PRIMARY KEY(ingredientId),
                    FOREIGN KEY(ingredientId)
                        REFERENCES ingredients(id)
                        ON UPDATE NO ACTION
                        ON DELETE CASCADE
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_pantry_stock_ingredientId
                ON pantry_stock(ingredientId)
                """.trimIndent()
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {

        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS pantry_stock")
        }
    }
}
