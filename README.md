# SnapAction ⚡📷

> **AI Vision Engine for Instant Screenshot Actionability**  
> SnapAction bridges the gap between unorganized screenshots on your phone/device and actionable workflows. Simply drop, pick, or take a picture of any screenshot (event flyers, recipes, bills, notes), and SnapAction automatically parses, categorizes, and converts it into rich, structured action cards.

---

## ✨ Features

- 📸 **Multi-Modal Upload Hub**: Pick screenshots from gallery, drag-and-drop image files, or take live photos directly with your camera.
- 🧠 **AI Vision & Categorization**: Automatically detects intent across multiple categories:
  - 📅 **Events**: Event titles, start/end dates, location, and description. Sync directly to Google Calendar or export `.ics` calendar files.
  - 🛒 **Groceries**: Dish names and itemized ingredient checklists with interactive checkmarks and instant clipboard export.
  - 🧾 **Expenses**: Biller vendor, total due amounts, currency, due dates, and payment tracking status.
  - 🔖 **Bookmarks**: Headlines, summaries, and key takeaways from saved screenshot notes.
- ✏️ **Human-in-the-Loop Verification**: Review, edit, and save extracted action data against the original screenshot reference.
- 🌓 **Dark / Light Theme Support**: Modern, responsive dark-mode UI with fluid micro-animations.

---

## 🛠️ Technology Stack

SnapAction features both a web application prototype and a native Android codebase:

### Web Application (React Prototype)
- **Framework**: React 18 + Vite
- **Language**: TypeScript
- **Styling**: Tailwind CSS + Lucide React Icons
- **Build Tool**: Vite

### Backend Service (Node.js / Express API)
- **Runtime**: Node.js + Express
- **File Upload & Validation**: Multer (Memory Storage, JPEG/PNG/WebP format check, 5MB size limit)
- **AI SDK**: `@google/genai` (Google Gen AI SDK with `responseSchema` / structured output mode)
- **Endpoint**: `POST /api/analyze-image`

### Native Android App (`/app`)
- **UI Framework**: Jetpack Compose + Material 3
- **Language**: Kotlin
- **Architecture**: MVVM with Kotlin Coroutines & Flow
- **Image Loading**: Coil Compose

---

## 🚀 Getting Started

### Web Application

1. **Clone the repository**:
   ```bash
   git clone https://github.com/nikfortune3code/snapaction.git
   cd snapaction
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Start the local development server**:
   ```bash
   npm run dev
   ```

4. **Build for production**:
   ```bash
   npm run build
   ```

### Android Project

1. Open the project directory in **Android Studio** (Koala or newer).
2. Sync Gradle dependencies (`app/build.gradle.kts`).
3. Run on an Android Emulator or connected device (Target SDK 35, Min SDK 26).

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
