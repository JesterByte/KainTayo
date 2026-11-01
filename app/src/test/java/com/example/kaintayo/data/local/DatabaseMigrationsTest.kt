
package com.example.kaintayo.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DatabaseMigrationsTest {

    @Test
    fun migrationsPreserveExistingDataAndRemovePantryTable() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val callback = object : SupportSQLiteOpenHelper.Callback(1) {

            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE ingredients (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        defaultUnit TEXT NOT NULL,
                        estimatedPriceCentavos INTEGER NOT NULL,
                        priceQuantity REAL NOT NULL,
                        priceUnit TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE meal_plans (
                        date TEXT NOT NULL,
                        mealType TEXT NOT NULL,
                        recipeId TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO meal_plans
                    VALUES ('2026-10-31', 'LUNCH', 'tinolang-manok')
                    """.trimIndent()
                )
            }

            override fun onUpgrade(
                db: SupportSQLiteDatabase,
                oldVersion: Int,
                newVersion: Int
            ) {
                // Migrations are invoked explicitly in this test.
            }
        }

        val configuration = SupportSQLiteOpenHelper.Configuration
            .builder(context)
            .name(null)
            .callback(callback)
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory()
            .create(configuration)

        try {
            val db = helper.writableDatabase

            db.execSQL("PRAGMA foreign_keys = ON")

            db.execSQL(
                """
                INSERT INTO ingredients (
                    id,
                    name,
                    defaultUnit,
                    estimatedPriceCentavos,
                    priceQuantity,
                    priceUnit
                )
                VALUES (
                    'chicken',
                    'Chicken',
                    'GRAM',
                    21000,
                    1.0,
                    'KILOGRAM'
                )
                """.trimIndent()
            )

            // Upgrade version 1 to version 2.
            DatabaseMigrations.MIGRATION_1_2.migrate(db)

            // Upgrade version 2 to version 3.
            DatabaseMigrations.MIGRATION_2_3.migrate(db)

            // Confirm the version 3 migration created the Pantry table.
            db.query(
                """
                SELECT COUNT(*)
                FROM sqlite_master
                WHERE type = 'table'
                AND name = 'pantry_stock'
                """
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
            }

            // Insert existing Pantry data before upgrading to version 4.
            db.execSQL(
                """
                INSERT INTO pantry_stock (
                    ingredientId,
                    quantity,
                    unit
                )
                VALUES ('chicken', 500.0, 'GRAM')
                """.trimIndent()
            )

            // Upgrade version 3 to version 4.
            DatabaseMigrations.MIGRATION_3_4.migrate(db)

            // The Pantry table must no longer exist.
            db.query(
                """
                SELECT COUNT(*)
                FROM sqlite_master
                WHERE type = 'table'
                AND name = 'pantry_stock'
                """
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }

            // Existing ingredient data and purchase information survive.
            db.query(
                """
                SELECT
                    name,
                    estimatedPriceCentavos,
                    priceQuantity,
                    priceUnit,
                    purchaseQuantity,
                    purchaseUnit
                FROM ingredients
                WHERE id = 'chicken'
                """
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())

                assertEquals("Chicken", cursor.getString(0))
                assertEquals(21000L, cursor.getLong(1))
                assertEquals(1.0, cursor.getDouble(2), 0.0001)
                assertEquals("KILOGRAM", cursor.getString(3))
                assertEquals(1.0, cursor.getDouble(4), 0.0001)
                assertEquals("KILOGRAM", cursor.getString(5))
            }

            // Existing meal plans remain intact.
            db.query(
                "SELECT COUNT(*) FROM meal_plans"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
            }

        } finally {
            helper.close()
        }
    }
}
