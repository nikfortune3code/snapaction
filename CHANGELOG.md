# Changelog

All notable changes to the **SnapAction** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.2.0] - 2026-08-15

### 🚀 Added
- **5-Category LLM Intent Classification**: Standardized image classification across 5 strict categories:
  - `BILL_RECEIPT`: Scanned invoices, store/restaurant receipts, digital payment screenshots.
  - `GROCERY_LIST`: Written grocery lists, pantry snapshots, retail grocery items.
  - `FOOD_DISH`: Cooked meals, plate photos, or food dishes (with recipe/ingredient inference).
  - `PACKAGED_ITEM`: Packaged food, beverage, or household products with labels.
  - `OTHER`: General text notes, social media snippets, and bookmarks.
- **Strict JSON Schema Enforcement**: Updated the Gemini Vision LLM system prompt and parser to strictly require a single valid JSON object containing:
  - `detected_category`
  - `confidence_score`
  - `summary_title`
  - `expense_details` (`is_expense`, `merchant`, `total_amount`, `currency`, `date`)
  - `extracted_items` (`name`, `quantity`, `estimated_price`)
  - `recipe_details` (`dish_name`, `estimated_ingredients_required`, `notes`)
- **Cross-Platform Model Alignment**: Updated Kotlin data models in Android (`SnapActionModels.kt`) and TypeScript interfaces in Web (`src/App.tsx`).

---

## [1.1.0] - 2026-08-15

### 🚀 Added
- **Direct Camera Capture Support**: Integrated HTML5 camera capture (`capture="environment"`) allowing mobile and camera-equipped devices to take live photos directly from the Upload Hub.
- **Dual Upload Controls**: Rendered distinct **"Select Screenshot"** (Gallery selection) and **"Take Photo"** (Live camera) action buttons inside the dropzone.
- **Project Documentation**: Created `README.md` containing project architecture, feature breakdowns, web/Android technology stacks, and execution instructions.
- **Remote Synchronization**: Synced project codebase and documentation directly with the remote GitHub repository (`nikfortune3code/snapaction`).

### 🐛 Fixed
- **Screenshot Selection Failure**: Fixed an issue where the file picker became unfunctional when re-selecting the same file. Explicitly cleared `fileInputRef.current.value` before triggering `.click()`.
- **Jetpack Compose Launcher Conflict**: Fixed a duplicate activity launcher invocation in `UploadHub.kt` where nested `.clickable` modifiers caused `IllegalStateException` on image pickers.
- **Expanded File Formats**: Added explicit support for `.png`, `.jpg`, `.jpeg`, `.webp`, and `.bmp` in file input accept attributes.

---

## [1.0.0] - 2026-08-15

### 🎉 Initial Release
- Multi-intent AI vision engine (Events, Groceries, Expenses, Bookmarks).
- Sync to Google Calendar and `.ics` file exports.
- Interactive grocery checklist with clipboard export.
- Expense tracker with unpaid total calculations.
- Human-in-the-loop verification and action card editing modal.
- Dual web prototype (React + Vite + TailwindCSS) and native Android app (Kotlin + Jetpack Compose).
