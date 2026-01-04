# 🧹 Household Cleaning Planner

A native Android application built with **Kotlin** and **Firebase Firestore** designed to help users organize, track, and manage household cleaning tasks efficiently.

![Project Status](https://img.shields.io/badge/Status-Active-success)
![Platform](https://img.shields.io/badge/Platform-Android-green)
![Backend](https://img.shields.io/badge/Backend-Firebase_Firestore-orange)

## Features

* **Real-Time Synchronization:** Uses Firebase Firestore to sync tasks instantly across devices.
* **Task Management:** Add tasks with specific attributes:
    * **Title**: The name of the chore.
    * **Room**: Assign tasks to specific rooms (Kitchen, Bedroom, etc.).
    * **Priority**: High (Red), Medium (Yellow), Low (Green).
* **Smart Filtering:** Filter the task list by specific rooms or view all at once.
* **Priority Visuals:** Tasks are color-coded based on urgency.
* **Daily Reset:** A specialized feature that allows users to uncheck "Done" tasks based on their priority (e.g., reset only "High Priority" daily tasks).
* **Modern UI:** Built using `RecyclerView`, `ListAdapter` (with DiffUtil), and ViewBinding for smooth performance.

## Tech Stack

* **Language:** [Kotlin](https://kotlinlang.org/)
* **Architecture:** MVVM (Model-View-ViewModel)
* **UI Components:**
    * RecyclerView & ListAdapter
    * ViewBinding
    * Material Design Components
* **Backend:** [Google Firebase Firestore](https://firebase.google.com/docs/firestore) (NoSQL Database)
* **State Management:** LiveData & ViewModel

## Getting Started

To run this project locally, you need to connect it to your own Firebase project.

### Prerequisites
* Android Studio (Ladybug or newer recommended)
* A Google Firebase Account

### Installation Steps

1.  **Clone the Repository**
    ```bash
    git clone [https://github.com/YourUsername/HouseholdCleaningPlanner.git](https://github.com/YourUsername/HouseholdCleaningPlanner.git)
    ```

2.  **Set up Firebase**
    * Go to the [Firebase Console](https://console.firebase.google.com/).
    * Create a new project.
    * Add an Android app with the package name: `com.example.householdcleaningplanner`.
    * Download the `google-services.json` file.

3.  **Add Configuration File**
    * Move the downloaded `google-services.json` file into the `app/` directory of the project:
        ```text
        HouseholdCleaningPlanner/
          ├── app/
          │     ├── google-services.json  <-- Place here
          │     ├── src/
          │     └── build.gradle
          └── build.gradle
        ```

4.  **Configure Firestore**
    * In your Firebase Console, go to **Firestore Database**.
    * Click **Create Database**.
    * **Important:** Set your Security Rules. For development/testing, you can use:
        ```javascript
        rules_version = '2';
        service cloud.firestore {
          match /databases/{database}/documents {
            match /{document=**} {
              allow read, write: if true;
            }
          }
        }
        ```

5.  **Build and Run**
    * Open the project in Android Studio.
    * Sync Gradle files.
    * Run on an Emulator or Physical Device.

## Project Structure

```text
com.example.householdcleaningplanner
├── adapter
│   └── TaskAdapter.kt       # Handles list display and DiffUtil
├── model
│   └── Task.kt              # Data model (with @PropertyName mapping)
├── viewmodel
│   └── MainViewModel.kt     # Handles business logic & Firestore calls
└── MainActivity.kt          # UI entry point
