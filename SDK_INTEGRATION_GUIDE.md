# Storyly SDK Integration Guide

Complete guide to integrate Storyly (Instagram/Snapchat-style stories) into your Android app.

---

## 📦 What's Included

1. **Android SDK** - Drop-in Compose component for displaying stories
2. **Backend API** - Node.js/TypeScript server for managing stories
3. **Web Dashboard** - React dashboard for uploading and managing stories

---

## 🚀 Quick Start (5 minutes)

### Step 1: Add the SDK to your app

#### Option A: Local Module (Recommended for development)

1. **Copy the SDK module** to your project:
   ```bash
   cp -r /Users/E2289/AndroidStudioProjects/storyly/storyly-sdk ./storyly-sdk
   ```

2. **Add to `settings.gradle.kts`**:
   ```kotlin
   include(":storyly-sdk")
   ```

3. **Add dependency in `app/build.gradle.kts`**:
   ```kotlin
   dependencies {
       implementation(project(":storyly-sdk"))
       
       // Required dependencies
       implementation("io.coil-kt:coil-compose:2.5.0")
   }
   ```

#### Option B: Maven/JitPack (Production)

```kotlin
// Coming soon - publish to Maven Central or JitPack
```

### Step 2: Add StorylyView to your app

```kotlin
import com.storyly.sdk.ui.StorylyView

@Composable
fun MyScreen() {
    StorylyView(
        apiKey = "YOUR_API_KEY"
    )
}
```

That's it! Stories will appear at the top of your screen.

---

## 📱 Complete Integration Example

### MainActivity.kt
```kotlin
package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.storyly.sdk.ui.StorylyView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column {
                        // Add stories at the top
                        StorylyView(
                            apiKey = "sk_storyly_android_app_2026"
                        )
                        
                        // Rest of your app content
                        Text("Your app content here")
                    }
                }
            }
        }
    }
}
```

---

## 🎨 Features

### ✅ What the SDK Provides

- ✅ **Horizontal scrolling stories** like Instagram
- ✅ **Auto-loading** from backend
- ✅ **Seen/Unseen indicators** (gradient rings)
- ✅ **Tap to open** full-screen viewer
- ✅ **Swipe left/right** to navigate stories
- ✅ **Swipe down** to close
- ✅ **Progress indicators** for multiple items
- ✅ **No visual tap effects** (clean interaction)
- ✅ **Automatic state management**

### 🎯 User Experience

```
UNSEEN STORIES:
  Colorful gradient ring (red → pink → purple)

SEEN STORIES:
  Gray ring

INTERACTIONS:
  • Tap → Opens full-screen
  • Swipe left → Next image/story
  • Swipe right → Previous image/story
  • Swipe down → Close viewer
```

---

## 🔧 Backend Setup

### 1. Start the Backend

```bash
cd /Users/E2289/Documents/claudecode/storyly/backend

# Install dependencies
npm install

# Start server
npm run dev
```

Backend runs on: **http://localhost:3000**

### 2. Start the Dashboard

```bash
cd /Users/E2289/Documents/claudecode/storyly/dashboard

# Install dependencies
npm install

# Start dashboard
npm run dev
```

Dashboard runs on: **http://localhost:5173**

---

## 🎬 Creating Stories

### Via Dashboard (Easiest)

1. Open: **http://localhost:5173/stories**
2. Click **"+ Create Story"**
3. Fill in:
   - Title (e.g., "Summer Sale")
   - Description (optional)
   - Media Type: Image or Video
   - Upload file
4. Click **"Publish Story"**
5. ✅ Story appears in the app instantly!

### Via API

```bash
# 1. Upload media
curl -X POST http://localhost:3000/api/v1/media/upload \
  -H "X-API-Key: sk_storyly_android_app_2026" \
  -F "file=@image.jpg"

# Response: { "success": true, "data": { "id": "media-123", ... } }

# 2. Create story
curl -X POST http://localhost:3000/api/v1/stories \
  -H "X-API-Key: sk_storyly_android_app_2026" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Story",
    "description": "Story description",
    "status": "PUBLISHED",
    "items": [{
      "mediaId": "media-123",
      "type": "IMAGE",
      "duration": 5
    }]
  }'
```

---

## 🔑 API Key Management

### Get Your API Key

API keys are in the database:

```bash
cd /Users/E2289/Documents/claudecode/storyly/backend
psql -U E2289 -d storyly

SELECT key, name FROM api_keys WHERE is_active = true;
```

Default API key: `sk_storyly_android_app_2026`

### Create New API Key

```sql
INSERT INTO api_keys (key, name, type, is_active, created_by)
VALUES ('sk_your_custom_key', 'My App', 'SDK', true, 'user-123');
```

---

## 🎨 Customization

### Change Story Circle Size

Edit `StorylyView.kt`:

```kotlin
// Line ~135
.size(70.dp)  // Change to 80.dp for larger circles
```

### Change Gradient Colors

```kotlin
// Line ~142 - Unseen stories
colors = listOf(
    Color(0xFFFB5C5C),  // Red
    Color(0xFFD93F77),  // Pink
    Color(0xFFC135C6)   // Purple
)
```

### Change Backend URL

For production, update the URL in `StorylyView.kt`:

```kotlin
// Line ~300
val url = java.net.URL("https://your-backend.com/api/v1/sdk/stories")
```

---

## 📊 Backend API Endpoints

### Stories

```
GET    /api/v1/sdk/stories           # Get all stories (for SDK)
GET    /api/v1/stories                # Get all stories (for dashboard)
POST   /api/v1/stories                # Create story
PUT    /api/v1/stories/:id            # Update story
DELETE /api/v1/stories/:id            # Delete story
POST   /api/v1/stories/:id/publish    # Publish story
```

### Media

```
POST   /api/v1/media/upload           # Upload image/video
GET    /api/v1/media                  # Get all media
DELETE /api/v1/media/:id              # Delete media
```

### Headers

All requests require:
```
X-API-Key: sk_storyly_android_app_2026
Content-Type: application/json
```

---

## 🗄️ Database

### PostgreSQL Setup

```bash
# Start PostgreSQL
brew services start postgresql@16

# Connect
psql -U E2289 -d storyly

# View stories
SELECT id, title, status FROM stories;

# View media
SELECT id, filename, cdn_url FROM media;
```

### Connection String
```
postgresql://E2289@localhost:5432/storyly
```

---

## 🔧 Troubleshooting

### Stories not loading

1. **Check backend is running:**
   ```bash
   curl http://localhost:3000/health
   ```

2. **Check API key:**
   ```bash
   curl -H "X-API-Key: sk_storyly_android_app_2026" \
        http://localhost:3000/api/v1/sdk/stories
   ```

3. **Check database:**
   ```bash
   psql -U E2289 -d storyly -c "SELECT COUNT(*) FROM stories;"
   ```

### Images not showing

1. **Emulator URL:** Make sure the SDK uses `10.0.2.2` not `localhost`
2. **Check images exist:**
   ```bash
   ls -la /Users/E2289/Documents/claudecode/storyly/backend/uploads/
   ```

### App crashes

1. **Check Gradle sync** in Android Studio
2. **Clean build:**
   ```bash
   ./gradlew clean assembleDebug
   ```

---

## 📦 Production Deployment

### 1. Backend

Deploy to any Node.js host (Heroku, Vercel, Railway):

```bash
# Update production URL in SDK
# Update CORS settings in backend
# Set environment variables
```

### 2. Database

Use managed PostgreSQL (Supabase, Railway, Heroku Postgres)

### 3. Media Storage

Use cloud storage (AWS S3, Cloudinary) instead of local uploads

---

## 🎯 Example Apps

### Simple Integration
```kotlin
StorylyView(apiKey = "YOUR_KEY")
```

### Custom Placement
```kotlin
Column {
    TopAppBar(title = { Text("My App") })
    StorylyView(apiKey = "YOUR_KEY")
    LazyColumn {
        // Your content
    }
}
```

### With Error Handling
```kotlin
// Coming soon: onError callback
StorylyView(
    apiKey = "YOUR_KEY",
    onError = { error ->
        // Handle error
    }
)
```

---

## 📝 Summary

**What you get:**
- ✅ Complete Instagram-style stories
- ✅ Auto-loading from backend
- ✅ Seen/unseen tracking
- ✅ Full-screen viewer with swipe
- ✅ Web dashboard for management
- ✅ REST API for integration

**Integration steps:**
1. Add SDK dependency
2. Add `StorylyView` composable
3. Run backend + dashboard
4. Create stories via dashboard
5. Done! ✨

---

## 🆘 Support

- **Backend:** http://localhost:3000
- **Dashboard:** http://localhost:5173
- **Database:** PostgreSQL on port 5432
- **API Key:** `sk_storyly_android_app_2026`

For questions or issues, check the troubleshooting section above.
