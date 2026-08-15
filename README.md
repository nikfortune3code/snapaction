# SnapAction ⚡📷 (Android Application)

> **Native Android Application Powered by AI Vision for Instant Screenshot Actionability**  
> SnapAction bridges the gap between unorganized screenshots on your Android phone and actionable workflows. Simply pick or capture any screenshot (event flyers, recipes, bills, notes), and SnapAction automatically parses, categorizes, and converts it into rich, structured action cards inside your native Android app.

---

## ✨ Android Application Features

- 📸 **Multi-Modal Image Capture & Ingestion**: Pick screenshots directly from your Android gallery or capture live photos using the device camera.
- 🧠 **AI Vision & Intent Categorization**: Automatically categorizes extracted items into 4 dedicated tabs:
  - 📅 **Reminders**: Event titles, start/end dates, location, and description. Sync directly to Google Calendar (`ACTION_INSERT`) or export `.ics` calendar files. Includes manual event creation.
  - 🛒 **Groceries**: Itemized grocery lists and dish recipe ingredient checklists with interactive checkmarks and instant clipboard export.
  - 🧾 **Expenses**: Merchant tracking, total amounts due, currency, due dates, and interactive paid/unpaid status toggling.
  - 🔖 **Bookmarks**: Headlines, summaries, and key takeaways from saved screenshot notes.
- ✏️ **Human-in-the-Loop Verification**: Review, edit, and save extracted action data against the original screenshot reference.
- 🌓 **Dark / Light Theme Support**: Modern, responsive dark-mode UI with fluid Material 3 Compose micro-animations.

---

## 🛠️ Technology Stack

SnapAction is built exclusively as a **Native Android Application** backed by a Node.js AI Vision backend service:

### Native Android App (`/app`)
- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow
- **Image Loading**: Coil Compose (`io.coil-kt:coil-compose`)
- **JSON Serialization**: Kotlinx Serialization (`kotlinx-serialization-json`)
- **Calendar Integration**: Android Calendar Contract (`ACTION_INSERT`)
- **Target SDK**: Android 15 (API Level 35) | **Min SDK**: API Level 26 (Android 8.0)

### Backend AI Vision Service (`/server`)
- **Runtime**: Node.js + Express
- **File Upload & Validation**: Multer (Memory Storage, JPEG/PNG/WebP format validation, 5MB size limit)
- **AI SDK**: `@google/genai` (Google Gen AI SDK with `responseSchema` / structured output mode)
- **Endpoint**: `POST /api/analyze-image`

---

## 🚀 Building & Running the Android Application

### 1. Run via Gradle CLI

```powershell
# Build Debug APK
.\gradlew.bat assembleDebug

# Install & Run Debug APK on connected Android device/emulator
.\gradlew.bat installDebug
```

### 2. Run via Android Studio

1. Open the project root directory in **Android Studio** (Koala or newer).
2. Sync Gradle configuration (`app/build.gradle.kts`).
3. Select your target Android device/emulator and press **Run (Shift + F10)**.

### 3. Optional: Start Backend AI Server

```powershell
# Set your Gemini API key in .env or shell environment
$env:GEMINI_API_KEY="your_actual_gemini_api_key_here"

# Start the Express server on http://localhost:3001
npm run server
```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
