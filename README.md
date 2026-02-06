# Nutrition Tracker

An Android app that uses AI-powered image recognition to identify food and beverages, estimate nutritional information, and track daily/weekly intake.

## Features

- **AI Food Recognition** - Snap a photo of any food or beverage and get instant nutritional analysis
- **Multiple AI Providers** - Choose between Claude (Anthropic), Gemini (Google), or Grok (xAI)
- **Camera & Gallery** - Capture directly or pick from your photo library
- **Daily Tracker** - View cumulative nutrition totals for the current day
- **Weekly Tracker** - See weekly summaries with daily breakdowns
- **Meal Categorization** - Tag entries as Breakfast, Lunch, Dinner, or Snack
- **Offline Storage** - All data stored locally with Room database

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **CameraX** for camera capture
- **Room** for local persistence
- **OkHttp** for API calls
- **Coil** for image loading
- **MVVM** architecture

## Setup

1. Clone the repository
2. Open in Android Studio (Hedgehog or later)
3. Add your API key(s) to `gradle.properties`:

```properties
CLAUDE_API_KEY=your_anthropic_api_key
GEMINI_API_KEY=your_google_ai_api_key
GROK_API_KEY=your_xai_api_key
```

Or enter the API key directly in the app at runtime.

4. Build and run on a device or emulator (API 26+)

## API Key Setup

You only need one API key for the provider you want to use:

- **Claude**: Get a key at [console.anthropic.com](https://console.anthropic.com)
- **Gemini**: Get a key at [aistudio.google.com](https://aistudio.google.com)
- **Grok**: Get a key at [console.x.ai](https://console.x.ai)

## Project Structure

```
app/src/main/java/com/nutritiontracker/
├── ai/                    # AI service layer
│   ├── AIService.kt       # Interface
│   ├── AIServiceFactory.kt
│   ├── ClaudeAIService.kt
│   ├── GeminiAIService.kt
│   ├── GrokAIService.kt
│   └── NutritionResponseParser.kt
├── data/
│   ├── db/                # Room database
│   ├── model/             # Data classes
│   └── repository/        # Data repository
├── ui/
│   ├── components/        # Reusable UI components
│   ├── navigation/        # Navigation setup
│   ├── screens/           # App screens
│   └── theme/             # Material theme
├── viewmodel/             # ViewModels
└── MainActivity.kt        # Entry point
```
