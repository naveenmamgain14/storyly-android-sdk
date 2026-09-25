# Storyly - Instagram-Style Stories SDK for Android

Complete solution for adding Instagram/Snapchat-style stories to any Android app.

<img src="https://img.shields.io/badge/Platform-Android-green.svg" />
<img src="https://img.shields.io/badge/Language-Kotlin-purple.svg" />
<img src="https://img.shields.io/badge/Compose-UI-blue.svg" />

---

## ✨ Features

- 📱 **Drop-in Compose Component** - Add stories with one line of code
- 🎨 **Instagram-Style UI** - Horizontal scrolling, gradient rings, full-screen viewer
- 👁️ **Seen/Unseen Tracking** - Visual indicators for viewed stories
- 🔄 **Auto-Loading** - Stories load automatically from backend
- 👆 **Rich Interactions** - Tap to open, swipe to navigate, swipe down to close
- 📊 **Web Dashboard** - Upload and manage stories via web interface
- 🚀 **Production Ready** - Complete backend API and database

---

## 🎬 Demo

```kotlin
// That's all you need!
StorylyView(apiKey = "YOUR_API_KEY")
```

**What you get:**
- Horizontal story circles at top
- Colorful gradient rings for unseen stories
- Tap to open full-screen viewer
- Swipe between stories and items
- Swipe down to close
- Progress indicators
- Auto-marks as seen

---

## 📦 Installation

### 1. Add SDK to your project

```kotlin
// settings.gradle.kts
include(":storyly-sdk")

// app/build.gradle.kts
dependencies {
    implementation(project(":storyly-sdk"))
    implementation("io.coil-kt:coil-compose:2.5.0")
}
```

### 2. Use in your app

```kotlin
import com.storyly.sdk.ui.StorylyView

@Composable
fun MyScreen() {
    Column {
        // Add stories
        StorylyView(apiKey = "YOUR_API_KEY")
        
        // Rest of your content
    }
}
```

---

## 🎯 Quick Start

### 1. Start Backend

```bash
cd backend
npm install
npm run dev
# Runs on http://localhost:3000
```

### 2. Start Dashboard

```bash
cd dashboard
npm install
npm run dev
# Runs on http://localhost:5173
```

### 3. Create Stories

1. Open http://localhost:5173/stories
2. Click "+ Create Story"
3. Upload image/video
4. Publish
5. ✅ Appears in app instantly!

---

## 🏗️ Architecture

```
┌─────────────────┐
│  Android App    │
│  + Storyly SDK  │
└────────┬────────┘
         │
         │ API Key
         │
┌────────▼────────┐      ┌──────────────┐
│  Backend API    │◄─────┤  Dashboard   │
│  (Node.js)      │      │  (React)     │
└────────┬────────┘      └──────────────┘
         │
         │
┌────────▼────────┐
│   PostgreSQL    │
│   Database      │
└─────────────────┘
```

---

## 📱 SDK Components

### StorylyView

Main component that displays stories:

```kotlin
StorylyView(
    apiKey = "sk_your_api_key",
    modifier = Modifier.fillMaxWidth(),
    config = StorylyConfig(
        baseUrl = "http://10.0.2.2:3000"
    )
)
```

### Features

| Feature | Description |
|---------|-------------|
| **Horizontal Scroll** | Instagram-style story circles |
| **Gradient Rings** | Colorful for unseen, gray for seen |
| **Auto-Load** | Stories load on app start |
| **Full-Screen** | Tap to open viewer |
| **Swipe Navigation** | Left/right for next/prev |
| **Swipe to Close** | Swipe down to dismiss |
| **Progress Bars** | Multiple bars for multi-item stories |
| **No Ripple** | Clean tap interaction |

---

## 🔧 Backend API

### Endpoints

```
GET    /api/v1/sdk/stories           # Get stories (mobile SDK)
GET    /api/v1/stories                # Get stories (dashboard)
POST   /api/v1/stories                # Create story
POST   /api/v1/media/upload           # Upload image/video
DELETE /api/v1/stories/:id            # Delete story
POST   /api/v1/stories/:id/publish    # Publish story
```

### Authentication

All requests require API key:

```
X-API-Key: sk_your_api_key
```

---

## 🎨 Dashboard

Web interface for managing stories:

- 📤 Upload images/videos
- 📝 Add titles and descriptions
- 📊 View all stories
- 🗑️ Delete stories
- 📱 Publish/unpublish
- 🖼️ Visual thumbnails

**URL:** http://localhost:5173

---

## 💾 Database Schema

### Stories Table
```sql
- id (uuid)
- title (string)
- description (string)
- status (DRAFT | PUBLISHED)
- created_at (timestamp)
```

### Story Items Table
```sql
- id (uuid)
- story_id (uuid)
- media_id (uuid)
- type (IMAGE | VIDEO)
- duration (int)
- order (int)
```

### Media Table
```sql
- id (uuid)
- filename (string)
- cdn_url (string)
- size (bigint)
- mime_type (string)
```

---

## 🎯 Use Cases

### E-commerce
- Product launches
- Flash sales
- New arrivals

### Social Apps
- User stories
- Updates
- Announcements

### News Apps
- Breaking news
- Top stories
- Updates

### Any App
- Onboarding
- Feature highlights
- Promotions

---

## 📂 Project Structure

```
storyly/
├── storyly-sdk/               # Android SDK module
│   └── src/main/java/com/storyly/sdk/
│       └── ui/
│           └── StorylyView.kt # Main UI component
├── app/                       # Demo app
│   └── src/main/java/
│       └── MainActivity.kt    # Integration example
├── backend/                   # Node.js API
│   ├── src/
│   │   ├── controllers/       # API controllers
│   │   ├── routes/            # API routes
│   │   └── index.ts           # Server entry
│   └── prisma/
│       └── schema.prisma      # Database schema
└── dashboard/                 # React dashboard
    └── src/
        ├── pages/
        │   └── Stories.tsx    # Story management
        └── services/
            └── api.ts         # API client
```

---

## 🔑 Configuration

### Backend URL

For emulator (default):
```kotlin
baseUrl = "http://10.0.2.2:3000"
```

For physical device:
```kotlin
baseUrl = "http://YOUR_COMPUTER_IP:3000"
```

For production:
```kotlin
baseUrl = "https://api.yourapp.com"
```

---

## 🚀 Production Deployment

### 1. Backend
- Deploy to Heroku, Railway, Vercel
- Set environment variables
- Update CORS settings

### 2. Database
- Use managed PostgreSQL (Supabase, Railway)
- Update connection string

### 3. Media
- Use S3, Cloudinary for uploads
- Update media controller

### 4. SDK
- Update baseUrl in config
- Secure API keys

---

## 📊 Tech Stack

### Android SDK
- Kotlin
- Jetpack Compose
- Coil (image loading)
- Material 3

### Backend
- Node.js
- TypeScript
- Express
- Prisma ORM
- PostgreSQL

### Dashboard
- React 18
- TypeScript
- TailwindCSS
- Vite
- Axios

---

## 🎓 Integration Guide

**See:** [SDK_INTEGRATION_GUIDE.md](./SDK_INTEGRATION_GUIDE.md)

Complete guide with:
- Step-by-step setup
- Code examples
- API documentation
- Troubleshooting
- Customization

---

## 🆘 Troubleshooting

### Stories not loading
```bash
# Check backend
curl http://localhost:3000/health

# Check API key
curl -H "X-API-Key: YOUR_KEY" \
     http://localhost:3000/api/v1/sdk/stories
```

### Build errors
```bash
# Clean and rebuild
./gradlew clean assembleDebug
```

### Images not showing
- Use `10.0.2.2` for emulator
- Check uploads directory exists
- Verify CORS settings

---

## 📝 License

MIT License - use freely in your projects

---

## 🌟 Features Roadmap

- [ ] Video support
- [ ] Analytics tracking
- [ ] Deep linking
- [ ] Offline caching
- [ ] Custom themes
- [ ] Link actions
- [ ] Product tags
- [ ] Maven publication

---

## 🤝 Contributing

This is a complete, production-ready solution. Feel free to customize for your needs!

---

## 📞 Support

- **API Key:** `sk_storyly_android_app_2026`
- **Backend:** http://localhost:3000
- **Dashboard:** http://localhost:5173
- **Database:** PostgreSQL on port 5432

---

Built with ❤️ using Kotlin, Compose, Node.js, and React
