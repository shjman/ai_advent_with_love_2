package com.yahorshymanchyk.ai_advent_with_love_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yahorshymanchyk.ai_advent_with_love_2.presentation.AppNavigation
import com.yahorshymanchyk.ai_advent_with_love_2.ui.theme.Ai_advent_with_love_2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ai_advent_with_love_2Theme {
                AppNavigation()
            }
        }
    }
}
