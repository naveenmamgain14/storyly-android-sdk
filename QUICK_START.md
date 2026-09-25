# Quick Start - No Backend Needed!

## 🎯 Immediate Testing (No Backend)

**Just enter any API key to see the UI!**

### Use this test key:
```
demo-key-123
```

Since the backend isn't running yet, you'll see:
- ✅ Configuration screen works
- ✅ Story viewer UI displays
- ✅ "No stories available" (expected - backend not running)
- ✅ App doesn't crash

This lets you see the **UI and integration** without setting up the backend first!

---

## 🚀 Full Setup (With Backend & Real Stories)

### Step 1: Install PostgreSQL

**Option A - Using Homebrew (Recommended):**
```bash
# Install PostgreSQL
brew install postgresql@16

# Start PostgreSQL
brew services start postgresql@16

# Create database
createdb storyly
```

**Option B - Download PostgreSQL:**
- Download from: https://postgresapp.com/
- Open PostgreSQL app
- Create database "storyly"

### Step 2: Setup Backend

```bash
cd /Users/E2289/Documents/claudecode/storyly/backend

# Already have .env file ✅

# Install dependencies (if not done)
npm install

# Generate Prisma client
npm run prisma:generate

# Run database migrations
npm run prisma:migrate

# Start backend server
npm run dev
```

You should see:
```
🚀 Storyly Backend running on port 3000
```

### Step 3: Create Real API Key

Run this in a **new terminal**:

```bash
cd /Users/E2289/Documents/claudecode/storyly/backend

# Run the API key generator
node -e "console.log('sk_' + require('crypto').randomBytes(32).toString('hex'))"
```

This generates a key like:
```
sk_a1b2c3d4e5f6...
```

### Step 4: Add API Key to Database Manually

```bash
cd /Users/E2289/Documents/claudecode/storyly/backend

# Open Prisma Studio (database GUI)
npm run prisma:studio
```

In Prisma Studio:
1. Go to **"ApiKey"** table
2. Click **"Add record"**
3. Fill in:
   - **key**: `sk_a1b2c3d4...` (the key you generated)
   - **appName**: `Storyly Android App`
   - **appId**: `com.example.storyly`
   - **platform**: `ANDROID`
   - **isActive**: `true`
   - **createdById**: (leave empty for now)
4. Click **"Save 1 change"**

### Step 5: Use Real API Key in App

Now enter the **real API key** in your Android app!

### Step 6: Create Test Stories

In Prisma Studio or dashboard, create stories to see them in the app!

---

## ✅ What Works Right Now

**Without backend:**
- ✅ App launches
- ✅ Configuration screen
- ✅ UI components display
- ❌ No stories loaded (backend not running)

**With backend running:**
- ✅ Everything above, plus:
- ✅ Stories load from database
- ✅ Analytics tracking works
- ✅ Real API key validation

---

## 🎯 Recommended Path

### For Testing UI Only (Now):
```
1. Enter "demo-key-123" in the app
2. Click "Start Demo"
3. See the UI (no stories yet)
```

### For Full Functionality (When Ready):
```
1. Install PostgreSQL
2. Run migrations
3. Create API key
4. Add stories
5. Test in app
```

---

## 🐛 Troubleshooting

### "No stories available"
- ✅ This is **normal** without backend
- ✅ UI is working correctly
- ✅ Just means no backend/data yet

### Want to test with mock data?
Let me know and I can add mock stories to the app for offline testing!

---

## 📝 Summary

**Current Status:**
- ✅ Android app: WORKING
- ✅ SDK integrated: YES  
- ⚠️ Backend: NOT STARTED YET
- ⚠️ Database: NOT RUNNING YET

**Next Step:**
Use `demo-key-123` to test the UI right now, or follow Step 1-6 above to set up the full backend.
