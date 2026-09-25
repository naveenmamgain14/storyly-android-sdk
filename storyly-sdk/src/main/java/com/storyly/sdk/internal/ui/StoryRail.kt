package com.storyly.sdk.internal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.storyly.sdk.model.Story

@Composable
internal fun StoryRail(
    stories: List<Story>,
    seen: Set<String>,
    imageLoader: ImageLoader,
    onStoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        // Keyed so recomposition reuses circles instead of rebuilding the row.
        items(items = stories, key = { it.id }) { story ->
            StoryCircle(
                story = story,
                isSeen = story.id in seen,
                imageLoader = imageLoader,
                onClick = { onStoryClick(stories.indexOfFirst { it.id == story.id }) },
            )
        }
    }
}

@Composable
private fun StoryCircle(
    story: Story,
    isSeen: Boolean,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(StorylyTokens.itemWidth)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(StorylyTokens.circleSize)
                .background(
                    brush = if (isSeen) StorylyTokens.seenRing else StorylyTokens.unseenRing,
                    shape = CircleShape,
                )
                .padding(StorylyTokens.ringWidth),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(StorylyTokens.surface)
                    .padding(2.dp),
            ) {
                val thumbnail = story.thumbnailUrl
                if (thumbnail != null) {
                    AsyncImage(
                        model = thumbnail,
                        imageLoader = imageLoader,
                        contentDescription = story.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(StorylyTokens.accent),
                    ) {
                        BasicText(
                            text = story.title.take(1).uppercase(),
                            style = StorylyTokens.avatarInitial,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        BasicText(
            text = story.title,
            style = StorylyTokens.label.copy(textAlign = TextAlign.Center),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun StoryRailSkeleton(count: Int = 4, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(count) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(StorylyTokens.itemWidth),
            ) {
                Box(
                    Modifier
                        .size(StorylyTokens.circleSize)
                        .clip(CircleShape)
                        .background(StorylyTokens.placeholder)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(StorylyTokens.placeholder)
                )
            }
        }
    }
}

@Composable
internal fun StoryRailError(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = message,
            style = StorylyTokens.message,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            BasicText(text = "Retry", style = StorylyTokens.action)
        }
    }
}
