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
    fun migrationPreservesIngredientDataAndAddsPurchaseInformation() {
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
                // No upgrade is needed when creating a version 1 database.
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

            DatabaseMigrations.MIGRATION_1_2.migrate(db)

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
