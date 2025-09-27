package com.example.topseriesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.topseriesapp.ui.navigation.AppNavGraph
import com.example.topseriesapp.ui.theme.TopSeriesAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TopSeriesAppTheme {
                AppNavGraph()
            }
        }
    }
}

