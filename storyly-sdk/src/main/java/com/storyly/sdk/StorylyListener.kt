package com.storyly.sdk

import com.storyly.sdk.model.Story
import com.storyly.sdk.model.StoryItem

/**
 * Optional hooks into story playback. Every method has a default no-op, so
 * implementations override only what they need.
 */
public interface StorylyListener {
    public fun onStoryOpened(story: Story) {}
    public fun onStoryClosed(story: Story) {}
    public fun onActionClicked(story: Story, item: StoryItem) {}
    public fun onLoadFailed(error: Throwable) {}
}
