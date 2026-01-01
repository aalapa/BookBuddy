# BookBuddy

A modern Android application built with Kotlin to help you track and manage your reading list.

## Features

- **Reading List Management**: Add books you want to read with custom rankings
- **Book Tracking**: Track book status (Not Started, In Progress, Completed)
- **Category Organization**: Organize books by categories (Fiction, Non-Fiction, Technical, Leadership, Philosophy, etc.)
- **Date Tracking**: Automatically record start and end dates when you begin and complete reading
- **Dashboard Analytics**: 
  - Total books read
  - Books read this year
  - Reading run rate (books per year projection)
  - Currently reading count
  - Books in queue count
- **Persistent Storage**: All data is stored locally using Room Database

## Architecture

- **MVVM Architecture**: Clean separation of concerns
- **Room Database**: Local data persistence
- **Material Design 3**: Modern, elegant UI
- **Navigation Component**: Seamless navigation between screens
- **LiveData & Flow**: Reactive data updates

## Project Structure

```
app/
├── src/main/java/com/bookbuddy/
│   ├── data/              # Room database entities, DAOs, and database
│   ├── repository/        # Data repository layer
│   ├── ui/
│   │   ├── adapters/      # RecyclerView adapters
│   │   ├── fragments/     # UI fragments
│   │   └── viewmodel/     # ViewModels
│   └── MainActivity.kt    # Main activity
└── src/main/res/          # Resources (layouts, strings, etc.)
```

## Setup Instructions

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run the app on an Android device or emulator (API 24+)

## Usage

1. **Add a Book**: Tap the + button on the "Books to Read" screen
2. **Set Ranking**: Enter a ranking number (1 = highest priority)
3. **Mark as In Progress**: Tap "Mark as In Progress" when you start reading
4. **Mark as Completed**: Tap "Mark as Completed" when you finish reading
5. **View Statistics**: Check the Dashboard tab for reading analytics
6. **View Completed Books**: See all completed books in the "Already Read" tab

## Requirements

- Android Studio Hedgehog or later
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Kotlin 1.9.20+

## Technologies Used

- Kotlin
- Android Jetpack:
  - Room Database
  - ViewModel & LiveData
  - Navigation Component
  - Material Design Components
- MVVM Architecture Pattern

