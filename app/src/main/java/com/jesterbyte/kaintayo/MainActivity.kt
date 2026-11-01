package com.jesterbyte.kaintayo

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
import com.jesterbyte.kaintayo.data.KainTayoAppContainer
import com.jesterbyte.kaintayo.feature.setup.DashboardScreen
import com.jesterbyte.kaintayo.ui.theme.KainTayoTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: KainTayoAppContainer

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContainer = KainTayoAppContainer(applicationContext)

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    appContainer.seedCatalog()
                }

                Log.i(TAG, "Filipino catalog seeding completed.")
            } catch (exception: Exception) {
                if (exception is CancellationException) {
                    throw exception
                }

                Log.e(TAG, "Filipino catalog seeding failed.", exception)
            }
        }

        setContent {
            KainTayoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    DashboardScreen(
                        appContainer = appContainer
                    )
                }
            }
        }
    }

    private companion object {
        const val TAG = "KainTayo"
    }
}
