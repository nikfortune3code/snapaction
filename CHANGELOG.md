# Changelog

All notable changes to the **SnapAction** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.3.0] - 2026-08-15

### 🚀 Added
- **Navigation Restructuring**:
  - **`Reminders` Tab**: Reverted 'All Feeds' to Reminders feed with start/end dates, location, event details, Google Calendar sync, `.ics` file downloads, and manual reminder entry modal.
  - **`Expenses` Tab**: Renamed 'Bills' tab to Expenses, featuring merchant tracking, total amounts, due dates, and interactive paid/unpaid toggles.
  - **`Groceries` Tab**: Merged food dish recipe ingredient checklists directly into the Groceries tab alongside standard pantry items.
  - **`Bookmarks` Tab**: Replaced 'Dishes' tab with Bookmarks to store headlines, summaries, and key takeaways from saved screenshot notes.
- **Node.js / Express Backend Service**:
  - Created Express server (`server/index.js`) listening on port 3001 with endpoint `POST /api/analyze-image`.
  - Configured `multer` memory storage with 5MB file size limits and MIME format validation (`image/jpeg`, `image/png`, `image/webp`).
  - Integrated `@google/genai` with `responseSchema` / structured output mode to guarantee valid JSON responses.
  - Implemented robust error handling middleware and JSON fallback parsing.

---

## [1.2.0] - 2026-08-15

### 🚀 Added
- **5-Category LLM Intent Classification**: Standardized image classification across 5 strict categories (`BILL_RECEIPT`, `GROCERY_LIST`, `FOOD_DISH`, `PACKAGED_ITEM`, `OTHER`).
- **Strict JSON Schema Enforcement**: Updated Gemini Vision LLM system prompt and parser to strictly require structured output.
- **Cross-Platform Model Alignment**: Updated Kotlin data models in Android (`SnapActionModels.kt`) and TypeScript interfaces in Web (`src/App.tsx`).

---

## [1.1.0] - 2026-08-15

### 🚀 Added
- **Direct Camera Capture Support**: Integrated HTML5 camera capture (`capture="environment"`).
- **Dual Upload Controls**: Rendered distinct **"Select Screenshot"** and **"Take Photo"** buttons.
- **Project Documentation**: Created `README.md` containing architecture, feature breakdowns, and technology stacks.
- **Remote Synchronization**: Synced project codebase and documentation directly with GitHub repository (`nikfortune3code/snapaction`).

### 🐛 Fixed
- **Screenshot Selection Failure**: Fixed file picker re-selection issue by clearing `fileInputRef.current.value`.
- **Jetpack Compose Launcher Conflict**: Fixed duplicate activity launcher invocation in `UploadHub.kt`.
- **Expanded File Formats**: Added explicit support for `.png`, `.jpg`, `.jpeg`, `.webp`, and `.bmp`.

---

## [1.0.0] - 2026-08-15

### 🎉 Initial Release
- Multi-intent AI vision engine (Events, Groceries, Expenses, Bookmarks).
- Sync to Google Calendar and `.ics` file exports.
- Interactive grocery checklist with clipboard export.
- Expense tracker with unpaid total calculations.
- Human-in-the-loop verification and action card editing modal.
- Dual web prototype (React + Vite + TailwindCSS) and native Android app (Kotlin + Jetpack Compose).
