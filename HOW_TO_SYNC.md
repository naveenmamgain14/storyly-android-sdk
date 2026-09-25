# How to Sync Your Storyly Project

## ✅ What I Did

I've integrated the Storyly SDK into YOUR existing Android project at:
```
/Users/E2289/AndroidStudioProjects/storyly/
```

## 📂 What Was Added

1. **storyly-sdk module** - The complete SDK library
2. **MainActivity.kt** - Demo app with SDK integration
3. **Updated configurations** - Gradle files, AndroidManifest, etc.

## 🔄 How to Sync in Android Studio

### Step 1: Open Your Project
If not already open:
1. Open Android Studio
2. Click **File → Open**
3. Navigate to: `/Users/E2289/AndroidStudioProjects/storyly`
4. Click **Open**

### Step 2: Sync Project

Click one of these:
- Click the **"Sync Now"** banner at the top (if visible)
- Click **File → Sync Project with Gradle Files**
- Click the 🐘 **Elephant icon** in the toolbar

### Step 3: Wait for Gradle Build

You should see:
```
BUILD SUCCESSFUL in Xs
```

## 🛠️ If Sync Fails

### Fix 1: Invalidate Caches
1. **File → Invalidate Caches...**
2. Check **"Clear file system cache"**
3. Check **"Clear build cache"**
4. Click **"Invalidate and Restart"**

### Fix 2: Clean and Rebuild
```bash
cd /Users/E2289/AndroidStudioProjects/storyly
./gradlew clean
./gradlew build
```

Then in Android Studio: **File → Sync Project with Gradle Files**

### Fix 3: Update Gradle Wrapper
```bash
cd /Users/E2289/AndroidStudioProjects/storyly
./gradlew wrapper --gradle-version=8.2
```

### Fix 4: Check Java Version
Make sure you're using Java 17:
1. **File → Project Structure**
2. **SDK Location** → **JDK Location**
3. Should be Java 17 or higher

## 📱 Run the App

Once synced successfully:

1. Click the green **▶️ Run** button
2. Select your emulator or device
3. App will launch

## 🎯 What You'll See

Your app now has:
- **Configuration screen** - Enter API key
- **Story viewer** - Displays stories
- **Load button** - Fetches from backend
- **Status display** - Shows loading/errors

## ⚙️ Project Structure

```
storyly/
├── app/                                    # Your app
│   ├── src/main/java/com/example/storyly/
│   │   └── MainActivity.kt                 # ← Demo activity
│   └── build.gradle.kts                    # ← Updated with SDK dependency
│
├── storyly-sdk/                            # SDK module (added)
│   ├── src/main/java/com/storyly/sdk/
│   │   ├── Storyly.kt
│   │   ├── model/
│   │   ├── network/
│   │   └── ui/
│   └── build.gradle.kts
│
└── settings.gradle.kts                     # ← Updated to include SDK
```

## 🔧 Changes Made

### settings.gradle.kts
```kotlin
include(":app")
include(":storyly-sdk")  // ← Added this
```

### app/build.gradle.kts
```kotlin
dependencies {
    implementation(project(":storyly-sdk"))  // ← Added SDK
    // ... Jetpack Compose dependencies
}
```

## ✅ Verification

After sync, verify:
1. No red errors in files
2. "Build: BUILD SUCCESSFUL" in Build tab
3. Can see both modules in Project view:
   - **app**
   - **storyly-sdk**

## 🚀 Next Steps

1. ✅ Sync project
2. ✅ Start backend: `cd backend && npm run dev`
3. ✅ Run the app
4. ✅ Enter API key
5. ✅ Load stories

## 📞 Still Issues?

If sync still fails, share the error message from:
- **Build** tab (bottom)
- **Event Log** (bottom right corner)

Common errors and fixes are in the main documentation.
