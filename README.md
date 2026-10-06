# Storyly

Instagram-style stories for Android — image, animated GIF and video, with both a
Compose and a View-based entry point.

```kotlin
StorylyView(
    config = StorylyConfig(
        apiKey = "your-api-key",
        backendUrl = "https://stories.example.com",
    )
)
```

That renders a tappable rail of story circles and a full-screen viewer with timed
playback, progress bars, tap navigation and hold-to-pause.

## Install

```kotlin
// settings.gradle.kts — mavenCentral() is usually already there
dependencyResolutionManagement {
    repositories { mavenCentral() }
}

// build.gradle.kts
dependencies {
    implementation("io.github.naveenmamgain14:storyly:0.1.3")
}
```

> **Do not use 0.1.0.** It crashes when a story is opened on Compose 1.7 or
> newer. Maven Central cannot remove a published version, so it remains
> downloadable — use 0.1.3 or later.

Requires **minSdk 24**. The rail is a Compose component; `StorylyRailView` wraps it
for apps that are not on Compose.

## You need a backend

This library is only the client. It expects an HTTP endpoint that returns your
stories, authenticated with an `X-API-Key` header. Point `backendUrl` at the host
and the SDK calls `GET {backendUrl}/api/v1/sdk/stories`.

The response it reads:

```json
{
  "success": true,
  "data": [
    {
      "id": "story-1",
      "title": "Flu Season Essentials",
      "description": "optional",
      "thumbnail_url": "https://cdn.example.com/thumb.jpg",
      "items": [
        {
          "id": "item-1",
          "type": "image",
          "media_url": "https://cdn.example.com/slide.jpg",
          "duration": 5,
          "action_url": "https://example.com/offer",
          "action_text": "Book now"
        }
      ]
    }
  ]
}
```

Notes on the fields:

- `type` is `"image"` or `"video"`. GIFs are detected from a `.gif` URL, so they
  can be sent as `"image"`.
- `duration` is seconds, clamped to 1–60. Ignored for video, which plays to its end.
- `thumbnail_url` is optional; the first item's `media_url` is used if absent.
- Items without a usable `media_url` are skipped rather than rendered blank.
- `action_url` / `action_text` are optional. A button appears only when
  `action_text` is set, and tapping it calls your listener — the SDK does not
  navigate for you.

## Compose

```kotlin
@Composable
fun Home() {
    StorylyView(
        config = StorylyConfig(apiKey = BuildConfig.STORYLY_KEY, backendUrl = "https://stories.example.com"),
        listener = object : StorylyListener {
            override fun onActionClicked(story: Story, item: StoryItem) {
                item.actionUrl?.let { openInBrowser(it) }
            }
        },
    )
}
```

## XML / Views

```xml
<com.storyly.sdk.StorylyRailView
    android:id="@+id/storyly"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
findViewById<StorylyRailView>(R.id.storyly).apply {
    config = StorylyConfig(apiKey = "…", backendUrl = "https://stories.example.com")
    listener = myListener
}
```

Setting `config` starts the load; setting it again reloads. The view must sit in a
normal Activity or Fragment layout so it has a lifecycle owner.

## Configuration

| Parameter | Default | Purpose |
|---|---|---|
| `apiKey` | required | Sent as `X-API-Key` |
| `backendUrl` | required | Host serving your stories. No default — each app points at its own |
| `diskCacheBytes` | 64 MB | On-disk media cache budget |
| `analyticsEnabled` | `true` | Set `false` to send no usage events at all |
| `userId` | `null` | Your own user identifier, attached to events |

## Listener

Every method has a default no-op, so override only what you need.

```kotlin
interface StorylyListener {
    fun onStoryOpened(story: Story) {}
    fun onStoryClosed(story: Story) {}
    fun onActionClicked(story: Story, item: StoryItem) {}
    fun onLoadFailed(error: Throwable) {}
}
```

## Behaviour

**Playback.** Each slide shows for its own duration, then advances. Finishing a
story moves to the next; finishing the last one closes the viewer. Video plays to
its end and drives the progress bar from real playback position.

**Gestures.** Tap the left third to go back, anywhere else to go forward. Press and
hold to pause, release to resume from where it stopped. Swipe down to dismiss.
Swipe left/right to move between stories.

**States.** While loading, the rail shows placeholder circles. On failure it shows a
short reason and a Retry button rather than an empty space — `"No internet
connection"`, `"Connection timed out"`, `"Invalid API key"`. When there are
genuinely no stories, the rail renders nothing.

**Seen stories** dim their ring for the current session. This is in-memory only and
is not persisted.

## Analytics

When enabled, the SDK posts to `{backendUrl}/api/v1/sdk/analytics/batch`:

```json
{ "events": [ {
  "event_type": "VIEW",
  "story_id": "story-1",
  "story_item_id": "item-1",
  "user_id": null,
  "device_id": "random-uuid",
  "session_id": "random-uuid"
} ] }
```

`IMPRESSION` when a story appears in the rail, `VIEW` when it is shown in the
viewer, `CLICK` on an action button, `COMPLETE` when all its slides finish, and
`DISMISS` when the viewer is closed.

Events are buffered and flushed on a 4-second window or a 20-event batch, so a
burst of taps is not a burst of requests. Delivery is best-effort: failures are
swallowed and never surface to your UI.

`device_id` is a random UUID generated on first run and kept in the SDK's own
`SharedPreferences`. No hardware or advertising identifiers are read. Set
`analyticsEnabled = false` and nothing is sent.

## What it brings in

Compose UI and Foundation, Coil (+ GIF decoder), Media3 ExoPlayer, OkHttp and
kotlinx-serialization. There is deliberately **no Material dependency** — the SDK
draws its own styling, so it will not pull a design system into your app or read
your theme.

Consumer ProGuard rules ship with the artifact; nothing to add for release builds.

## Sample

The `app` module in this repository is a working demo. To run it, put your own
credentials in `local.properties` (which is gitignored):

```properties
storyly.apiKey=your-api-key
storyly.backendUrl=https://stories.example.com
```

## License

Apache 2.0 — see [LICENSE](LICENSE).
