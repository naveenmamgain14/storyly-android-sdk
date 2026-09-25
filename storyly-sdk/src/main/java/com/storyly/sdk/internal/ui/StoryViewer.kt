package com.storyly.sdk.internal.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState, stories) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            stories.getOrNull(page)?.let(onStoryShown)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(StorylyTokens.scrim)
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val story = stories.getOrNull(page) ?: return@HorizontalPager
                StoryPage(
                    story = story,
                    // Only the story actually on screen runs its timer.
                    isActive = pagerState.currentPage == page && pagerState.targetPage == page,
                    imageLoader = imageLoader,
                    onActionClick = { item -> onActionClick(story, item) },
                    onFinished = {
                        if (page < stories.lastIndex) {
                            scope.launch { pagerState.animateScrollToPage(page + 1) }
                        } else {
                            onDismiss()
                        }
                    },
                    onPrevious = {
                        if (page > 0) scope.launch { pagerState.animateScrollToPage(page - 1) }
                    },
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun StoryPage(
    story: Story,
    isActive: Boolean,
    imageLoader: ImageLoader,
    onActionClick: (StoryItem) -> Unit,
    onFinished: () -> Unit,
    onPrevious: () -> Unit,
    onDismiss: () -> Unit,
) {
    val items = story.items
    if (items.isEmpty()) return

    var index by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val progress = remember { Animatable(0f) }

    val item = items.getOrNull(index) ?: items.first()

    // Leaving the story rewinds it, so returning later starts from the top.
    LaunchedEffect(isActive) {
        if (!isActive) {
            index = 0
            progress.snapTo(0f)
        }
    }

    LaunchedEffect(index, isActive) { progress.snapTo(0f) }

    // Not keyed on `paused` resetting progress: resuming continues the remaining time.
    LaunchedEffect(index, isActive, paused, item.id) {
        if (!isActive || paused) return@LaunchedEffect
        if (item.type == StoryMediaType.VIDEO) return@LaunchedEffect

        val remainingMs = ((1f - progress.value) * item.durationSeconds * 1000).toInt()
        if (remainingMs <= 0) return@LaunchedEffect
        val result = progress.animateTo(1f, tween(remainingMs, easing = LinearEasing))
        if (result.endReason == AnimationEndReason.Finished) {
            if (index < items.lastIndex) index++ else onFinished()
        }
    }

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
        StorySlide(item = item, imageLoader = imageLoader)

        // Tap zones: left third steps back, the rest steps forward. Holding pauses.
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(index, items.size) {
                    detectTapGestures(
                        onPress = {
                            paused = true
                            tryAwaitRelease()
                            paused = false
                        },
                        onTap = { offset ->
                            if (offset.x < size.width / 3f) {
                                if (index > 0) index-- else onPrevious()
                            } else {
                                if (index < items.lastIndex) index++ else onFinished()
                            }
                        },
                    )
                }
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
        )

        Column(Modifier.align(Alignment.TopCenter)) {
            ProgressBars(
                count = items.size,
                currentIndex = index,
                currentProgress = progress.value,
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
                        text = "${index + 1}/${items.size}",
                        style = StorylyTokens.viewerCounter,
                    )
                }
                CloseButton(onClick = onDismiss)
            }
        }

        val actionText = item.actionText
        if (actionText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .clickable { onActionClick(item) }
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
private fun ProgressBars(
    count: Int,
    currentIndex: Int,
    currentProgress: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(count) { i ->
            val fraction = when {
                i < currentIndex -> 1f
                i == currentIndex -> currentProgress.coerceIn(0f, 1f)
                else -> 0f
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                if (fraction > 0f) {
                    Box(
                        Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
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
        drawLine(Color.White, Offset(0f, 0f), Offset(size.width, size.height), stroke, StrokeCap.Round)
        drawLine(Color.White, Offset(size.width, 0f), Offset(0f, size.height), stroke, StrokeCap.Round)
    }
}

private const val DISMISS_THRESHOLD_PX = 200f
private const val DISMISS_FADE_DISTANCE = 1000f
