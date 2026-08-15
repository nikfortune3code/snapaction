# Changelog

All notable changes to the **SnapAction** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.8.0] - 2026-08-16

### 🧹 Refactored
- **Complete Removal of Web Prototype Code**:
  - Removed all web application files (`src/App.tsx`, `src/main.tsx`, `index.html`, `vite.config.ts`, `tsconfig.json`).
  - Purged React web dependencies from `package.json`, focusing the repository exclusively on the native **Android Application** (`/app`) and Node.js Express backend API (`/server`).

---

## [1.7.0] - 2026-08-16

### 🚀 Added
- **Receipt, Bill & Invoice Expense Structuring**:
  - Implemented automatic classification placing all receipts, store invoices, and utility bills directly into the **Expenses** tab.
  - Extracted structured fields: **Bill Heading** (Vendor/Store), **Total Amount** ($), **Expense Category** (e.g. Electric Bill, Gas Bill, Credit Card, Utilities, Retail), and **Due Date** (populated ONLY if applicable, e.g. for recurring Electric, Gas, or Credit Card bills).
  - Made `dueDate` optional (`dueDate: String?` in Kotlin) so instant store receipts without due dates omit the due date field cleanly.

---

## [1.6.0] - 2026-08-16

### 🐛 Fixed
- **Image Parsing & Tab Auto-Switching**:
  - Fixed issue where uploaded screenshots (with numeric Android content URIs like `content://media/external/images/...`) were defaulting to Groceries without showing in the active tab.
  - Implemented `preferredCategory` fallback in `AiVisionRepository.kt` to use the currently active tab when categorizing incoming images.
  - Added automatic active tab switching in `SnapViewModel.kt` so whenever a new screenshot is parsed, the app automatically switches to the parsed card's tab so the user sees it immediately.
  - Added interactive Category Tab selection chips in `EditActionSheet.kt` so users can reassign any screenshot to Reminders, Groceries, Expenses, or Bookmarks with a single tap.

---

## [1.5.0] - 2026-08-16

### 🚀 Added
- **Native Android Architecture Focus**:
  - Re-architected and updated documentation, project scripts, and application metadata to focus exclusively on native **Android Application** development (Kotlin 2.0, Jetpack Compose, Material 3, ViewModel, StateFlow, Android Calendar Integration).
  - Maintained the Node.js Express server (`/server`) for server-side Gemini Vision API structured output processing.

---

## [1.4.0] - 2026-08-16

### 🚀 Added
- **Manual Event Reminder Creation**:
  - Added a prominent **"Add Event"** button directly inside the **Reminders** tab header in Android (Compose).
  - Implemented an interactive manual event creation dialog allowing users to manually enter event reminders (Title, Start Date, Start Time, Location, Description) without requiring a screenshot upload.
- **Strict Reminders Tab Isolation**:
  - Updated filtering rules so that the **Reminders** tab strictly displays ONLY `EVENT` category items. Items from Groceries, Expenses, and Bookmarks are completely hidden from the Reminders view.

---

## [1.3.0] - 2026-08-15

### 🚀 Added
- **Navigation Restructuring**:
  - **`Reminders` Tab**: Reverted 'All Feeds' to Reminders feed with start/end dates, location, event details, Google Calendar sync, and `.ics` file downloads.
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
- **Cross-Platform Model Alignment**: Updated Kotlin data models in Android (`SnapActionModels.kt`).

---

## [1.1.0] - 2026-08-15

### 🚀 Added
- **Direct Camera Capture Support**: Integrated HTML5/Android camera capture.
- **Dual Upload Controls**: Rendered distinct **"Select Screenshot"** and **"Take Photo"** buttons.
- **Project Documentation**: Created `README.md` containing architecture, feature breakdowns, and technology stacks.
- **Remote Synchronization**: Synced project codebase and documentation directly with GitHub repository (`nikfortune3code/snapaction`).

---

## [1.0.0] - 2026-08-15

### 🎉 Initial Release
- Multi-intent AI vision engine (Events, Groceries, Expenses, Bookmarks).
- Sync to Google Calendar and `.ics` file exports.
- Interactive grocery checklist with clipboard export.
- Expense tracker with unpaid total calculations.
- Human-in-the-loop verification and action card editing modal.
- Native Android app (Kotlin + Jetpack Compose).
