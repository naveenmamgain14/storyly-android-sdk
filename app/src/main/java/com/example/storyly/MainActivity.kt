package com.example.storyly

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.storyly.sdk.StorylyConfig
import com.storyly.sdk.StorylyListener
import com.storyly.sdk.StorylyView
import com.storyly.sdk.model.Story
import com.storyly.sdk.model.StoryItem

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (BuildConfig.STORYLY_API_KEY.isBlank() || BuildConfig.STORYLY_BACKEND_URL.isBlank()) {
                        SetupHint()
                    } else {
                        StorylyView(
                            config = StorylyConfig(
                                apiKey = BuildConfig.STORYLY_API_KEY,
                                backendUrl = BuildConfig.STORYLY_BACKEND_URL,
                            ),
                            listener = DemoListener,
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun SetupHint() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Add storyly.apiKey and storyly.backendUrl to local.properties, " +
                "then rebuild to see the demo.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(32.dp),
        )
    }
}

private object DemoListener : StorylyListener {
    override fun onStoryOpened(story: Story) {
        Log.d(TAG, "opened: ${story.title}")
    }

    override fun onActionClicked(story: Story, item: StoryItem) {
        Log.d(TAG, "action: ${item.actionUrl}")
    }

    override fun onLoadFailed(error: Throwable) {
        Log.w(TAG, "load failed", error)
    }

    private const val TAG = "StorylyDemo"
}
