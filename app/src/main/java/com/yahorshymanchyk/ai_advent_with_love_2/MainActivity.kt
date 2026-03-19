package com.yahorshymanchyk.ai_advent_with_love_2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yahorshymanchyk.ai_advent_with_love_2.presentation.ClaudeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ClaudeFragment())
                .commit()
        }
    }
}
