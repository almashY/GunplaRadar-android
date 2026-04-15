package com.example.gunplaradar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gunplaradar.ui.navigation.MainNavGraph
import com.example.gunplaradar.ui.theme.GunplaRadarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GunplaRadarTheme {
                MainNavGraph()
            }
        }
    }
}
