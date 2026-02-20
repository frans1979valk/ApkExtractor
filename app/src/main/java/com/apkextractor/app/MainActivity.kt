package com.apkextractor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.apkextractor.app.ui.navigation.ApkExtractorNavHost
import com.apkextractor.app.ui.theme.ApkExtractorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApkExtractorTheme {
                ApkExtractorNavHost()
            }
        }
    }
}
