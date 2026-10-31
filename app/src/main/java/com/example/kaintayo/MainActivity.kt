
package com.example.kaintayo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.lifecycle.lifecycleScope
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.example.kaintayo.data.catalog.FilipinoCatalogSeeder
import com.example.kaintayo.data.local.KainTayoDatabaseProvider
import com.example.kaintayo.feature.setup.DashboardScreen
import com.example.kaintayo.ui.theme.KainTayoTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Seed the local Filipino catalog in the background.
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val database = KainTayoDatabaseProvider
                        .getDatabase(applicationContext)

                    FilipinoCatalogSeeder(
                        database.ingredientDao(),
                        database.recipeDao()
                    ).seed()
                }

                Log.i(TAG, "Filipino catalog seeding completed.")
            } catch (exception: Exception) {
                if (exception is CancellationException) {
                    throw exception
                }

                Log.e(TAG, "Filipino catalog seeding failed.", exception)
            }
        }

        // Display the dashboard without waiting for database seeding.
        setContent {
            KainTayoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    DashboardScreen()
                }
            }
        }
    }

    private companion object {
        const val TAG = "KainTayo"
    }
}
