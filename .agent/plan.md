# Project Plan

Colouring Book app for Android. The app should allow users to choose from various line art images and color them using a palette. It should follow Material Design 3 guidelines, use Jetpack Compose, and have a vibrant, energetic color scheme. Include a feature to save the colored images. Also create a readme file explaining how to run the project locally.

## Project Brief

# Project Brief: Colouring Book

## Features
*   **Line Art Gallery**: A curated selection of high-quality line art images (animals, nature, patterns) for users to browse and select.
*   **Interactive Coloring Canvas**: A responsive canvas featuring a "tap-to-fill" coloring mechanism, allowing users to easily apply colors to different sections of the line art.
*   **Vibrant Material 3 Palette**: A dynamic and energetic set of color swatches built using Material Design 3 principles to ensure a cohesive and modern look.
*   **Artwork Export**: A feature allowing users to save their completed artwork to the device's local storage or gallery for sharing.

## High-Level Tech Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (with Material Design 3)
*   **Concurrency**: Kotlin Coroutines & Flow for asynchronous operations.
*   **Image Loading**: Coil for efficient loading of line art assets.
*   **Code Generation**: KSP (Kotlin Symbol Processing) for modern, high-performance code generation.

---

# README.md

# Colouring Book Android App

A vibrant and energetic colouring book application designed for creative expression, built with Jetpack Compose and Material Design 3.

## How to Run the Project Locally

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer is recommended.
- **JDK**: Version 17 or 21.
- **Android SDK**: Minimum SDK 29 (Android 10), Target SDK 36.

### Steps to Run
1.  **Clone the Repository**:
    ```bash
    git clone <repository-url>
    ```
2.  **Open in Android Studio**:
    - Launch Android Studio.
    - Select **File > Open** and navigate to the project's root folder (`ColouringBook`).
3.  **Sync Gradle**:
    - Wait for the project to load. Android Studio should automatically start a Gradle sync. If not, click the "Sync Project with Gradle Files" icon in the top toolbar.
4.  **Connect a Device**:
    - Connect a physical Android device via USB or start an Android Emulator (API 29+).
5.  **Build and Run**:
    - Select the `app` configuration in the toolbar.
    - Click the **Run** button (green play icon) or press `Shift + F10`.

## Key Features
- **Gallery**: Browse a variety of line art templates.
- **Coloring**: Tap to fill regions with vibrant colors.
- **M3 UI**: Modern interface with full edge-to-edge support and energetic color schemes.
- **Save**: Export your finished work to your device.

## Implementation Steps

### Task_1_Setup_Theme_Gallery: Set up Material 3 theme with vibrant colors, edge-to-edge display, Gallery screen to browse line art, and README file.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - App launches with vibrant M3 theme
  - Edge-to-edge implemented
  - Gallery screen displays line art images using Coil
  - README.md created with setup instructions
- **StartTime:** 2026-04-11 16:58:08 CEST

### Task_2_Coloring_Canvas: Implement the Coloring Canvas screen with a tap-to-fill mechanism on the selected line art image.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Canvas screen displays selected line art
  - Tap-to-fill algorithm correctly identifies and colors regions
  - Navigation from Gallery to Canvas works

### Task_3_Palette_and_Saving: Integrate a vibrant Material 3 color palette and implement functionality to save/export the colored artwork.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Vibrant color palette allows user selection
  - Artwork can be saved to the device's gallery/storage
  - UI follows M3 guidelines and is responsive

### Task_4_Final_Polish_Verify: Create an adaptive app icon and perform a final run to verify application stability and requirement alignment.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Adaptive app icon implemented
  - Project builds and runs successfully
  - App does not crash
  - All requirements from the project brief are met

