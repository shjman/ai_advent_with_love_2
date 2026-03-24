package com.yahorshymanchyk.ai_advent_with_love_2.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val stubChats = listOf(
    "How do I reverse a string in Kotlin?",
    "Explain coroutines vs threads",
    "Best practices for REST API design",
    "What is dependency injection?",
    "Difference between val and var",
    "How does garbage collection work?",
    "Jetpack Compose vs XML layouts",
    "What is a monad?",
)

@Composable
fun ChatsScreen(paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(stubChats) { name ->
            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}
