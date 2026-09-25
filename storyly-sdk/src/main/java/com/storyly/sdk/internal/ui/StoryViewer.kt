package com.storyly.sdk.internal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import com.storyly.sdk.model.Story
import com.storyly.sdk.model.StoryItem
import com.storyly.sdk.model.StoryMediaType
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun StoryViewer(
    stories: List<Story>,
    initialStoryIndex: Int,
    imageLoader: ImageLoader,
    onStoryShown: (Story) -> Unit,
    onActionClick: (Story, StoryItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = initialStoryIndex.coerceIn(0, (stories.size - 1).coerceAtLeast(0)),
        pageCount = { stories.size },
    )

    LaunchedEffect(pagerState, stories) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            stories.getOrNull(page)?.let(onStoryShown)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(StorylyTokens.scrim)
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                stories.getOrNull(page)?.let { story ->
                    StoryPage(
                        story = story,
                        imageLoader = imageLoader,
                        onActionClick = { item -> onActionClick(story, item) },
                        onDismiss = onDismiss,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StoryPage(
    story: Story,
    imageLoader: ImageLoader,
    onActionClick: (StoryItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val items = story.items
    if (items.isEmpty()) return

    val itemPager = rememberPagerState(initialPage = 0, pageCount = { items.size })
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = dragOffset
                alpha = 1f - (dragOffset.absoluteValue / DISMISS_FADE_DISTANCE).coerceIn(0f, 0.5f)
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset > DISMISS_THRESHOLD_PX) onDismiss()
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onVerticalDrag = { _, delta ->
                        if (delta > 0 || dragOffset > 0) {
                            dragOffset = (dragOffset + delta).coerceAtLeast(0f)
                        }
                    },
                )
            }
    ) {
        HorizontalPager(state = itemPager, modifier = Modifier.fillMaxSize()) { index ->
            StorySlide(item = items[index], imageLoader = imageLoader)
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent))
                )
        )

        Column(Modifier.align(Alignment.TopCenter)) {
            ProgressBars(
                count = items.size,
                currentIndex = itemPager.currentPage,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    BasicText(
                        text = story.title,
                        style = StorylyTokens.viewerTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        text = "${itemPager.currentPage + 1}/${items.size}",
                        style = StorylyTokens.viewerCounter,
                    )
                }
                CloseButton(onClick = onDismiss)
            }
        }

        val current = items.getOrNull(itemPager.currentPage)
        val actionText = current?.actionText
        if (current != null && actionText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .clickable { onActionClick(current) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                BasicText(text = actionText, style = StorylyTokens.action)
            }
        }
    }
}

@Composable
private fun StorySlide(item: StoryItem, imageLoader: ImageLoader) {
    when (item.type) {
        StoryMediaType.IMAGE, StoryMediaType.GIF -> AsyncImage(
            model = item.url,
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        // Playback arrives with the Media3 work; until then a slide is never blank.
        StoryMediaType.VIDEO -> Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111111)),
        ) {
            BasicText(text = "Video", style = StorylyTokens.viewerCounter)
        }
    }
}

@Composable
private fun ProgressBars(count: Int, currentIndex: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(count) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                if (index <= currentIndex) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    Canvas(
        modifier = Modifier
            .size(32.dp)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        val stroke = 2.dp.toPx()
        drawLine(
            color = Color.White,
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White,
            start = Offset(size.width, 0f),
            end = Offset(0f, size.height),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

private const val DISMISS_THRESHOLD_PX = 200f
private const val DISMISS_FADE_DISTANCE = 1000f
