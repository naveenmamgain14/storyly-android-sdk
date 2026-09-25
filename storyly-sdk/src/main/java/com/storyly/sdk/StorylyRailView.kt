package com.storyly.sdk

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView

/**
 * View-system entry point for apps that are not on Compose.
 *
 * ```
 * val rail = findViewById<StorylyRailView>(R.id.storyly)
 * rail.config = StorylyConfig(apiKey = "…", backendUrl = "https://example.com")
 * ```
 *
 * Must be hosted somewhere with a `ViewTreeLifecycleOwner` — any normal
 * Activity or Fragment layout qualifies.
 */
public class StorylyRailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var configState by mutableStateOf<StorylyConfig?>(null)
    private var listenerState by mutableStateOf<StorylyListener?>(null)

    /** Setting this triggers a load; setting it again reloads. */
    public var config: StorylyConfig?
        get() = configState
        set(value) {
            configState = value
        }

    public var listener: StorylyListener?
        get() = listenerState
        set(value) {
            listenerState = value
        }

    init {
        addView(
            ComposeView(context).apply {
                setContent {
                    configState?.let { StorylyView(config = it, listener = listenerState) }
                }
            }
        )
    }
}
