package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.GalleryRepository
import com.example.ui.GalleryApp
import com.example.ui.GalleryViewModel
import com.example.ui.GalleryViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable premium modern Edge-To-Edge drawing of system bars
        enableEdgeToEdge()

        // 1. Initialize local persistent database (Room)
        val database = AppDatabase.getDatabase(applicationContext)

        // 2. Coordinate database connections and sidecars in the repository pattern
        val repository = GalleryRepository(
            context = applicationContext,
            connectionDao = database.networkConnectionDao(),
            assetDao = database.mediaAssetDao(),
            cloudDao = database.cloudSyncDao()
        )

        // 3. Instantiate our ViewModel using the ViewModelProvider Factory
        val factory = GalleryViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[GalleryViewModel::class.java]

        // 4. Mount our composable on the screen
        setContent {
            MyApplicationTheme {
                GalleryApp(viewModel = viewModel)
            }
        }
    }
}
