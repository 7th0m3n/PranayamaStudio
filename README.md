# Pranayama Studio 🧘

A beautiful, minimal Android app for guided breathing exercises (pranayama). Built with Kotlin and Jetpack Compose.

## Features

- **4 Preset Breathing Patterns**
  - Box Breathing (4-4-4-4)
  - 4-7-8 Breathing (sleep support)
  - Alternate Nostril (Nadi Shodhana, simplified)
  - Custom (placeholder for user patterns)

- **Smooth Breath Pacer Animation**
  - Expanding/contracting circle synced to breath phases
  - Phase-based colors (blue=inhale, amber=hold, green=exhale)
  - Subtle glow effects

- **Session Tracking**
  - Today's practice minutes
  - Total sessions completed
  - Persisted with DataStore

- **Haptic Feedback**
  - Light vibration on phase transitions

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 26+ (Android 8.0+)
- Kotlin 1.9+

## Project Structure

```
app/src/main/java/com/pranayama/studio/
├── MainActivity.kt              # App entry point
├── model/
│   └── BreathPattern.kt         # Data models & presets
├── viewmodel/
│   └── SessionViewModel.kt      # Session state & timing
├── data/
│   └── StatsRepository.kt       # Stats persistence
├── ui/
│   ├── theme/
│   │   ├── Color.kt             # Color palette
│   │   ├── Type.kt              # Typography
│   │   └── Theme.kt             # Material theme
│   ├── navigation/
│   │   └── NavGraph.kt          # Navigation setup
│   ├── home/
│   │   └── HomeScreen.kt        # Pattern list & stats
│   ├── session/
│   │   └── SessionScreen.kt     # Breathing exercise UI
│   └── components/
│       └── BreathPacer.kt       # Animated pacer circle
```

## Customization Guide

### Adding New Breathing Patterns

Edit `model/BreathPattern.kt`:

```kotlin
// In PranayamaPatterns object, add:
val myNewPattern = BreathPattern(
    id = "my_pattern",
    name = "My Pattern",
    description = "Description of benefits",
    inhaleSeconds = 4,
    holdAfterInhaleSeconds = 2,
    exhaleSeconds = 6,
    holdAfterExhaleSeconds = 0,  // 0 = skip this phase
    sessionType = SessionType.TIMED,
    defaultMinutes = 5
)

// Then add to allPatterns list:
val allPatterns = listOf(
    boxBreathing,
    breathing478,
    nadiShodhana,
    myNewPattern,  // Add here
    customPattern
)
```

### Changing the Pacer Animation

Edit `ui/components/BreathPacer.kt`:

- **Colors**: Modify `getPhaseColor()` function
- **Size**: Adjust `minRadius` and `maxRadius` in the Canvas
- **Glow**: Change `glowAlpha` animation parameters
- **Add rings**: Draw additional circles in the Canvas block

### Switching Between Cycle-based and Time-based Sessions

In `BreathPattern`, set `sessionType`:

```kotlin
// Time-based: breathe for X minutes
sessionType = SessionType.TIMED,
defaultMinutes = 5

// Cycle-based: complete X breath cycles
sessionType = SessionType.CYCLES,
defaultCycles = 4
```

### Adding Sound Cues

In `viewmodel/SessionViewModel.kt`, find the `onPhaseChange()` function:

```kotlin
private fun onPhaseChange(phase: BreathPhase) {
    // Add your audio code here:
    // 1. MediaPlayer for sound effects
    // 2. TextToSpeech for voice prompts
}
```

### Changing Colors

Edit `ui/theme/Color.kt`:

```kotlin
// Main colors
val PrimaryTeal = Color(0xFF58A6FF)    // Change hex value
val SecondaryAmber = Color(0xFFD29922)

// Phase-specific colors
val InhaleColor = Color(0xFF58A6FF)
val HoldColor = Color(0xFFD29922)
val ExhaleColor = Color(0xFF7EE787)
```

### Disabling Haptic Feedback

In `SessionViewModel.kt`, comment out the body of `triggerHapticFeedback()`:

```kotlin
private fun triggerHapticFeedback() {
    // Comment out or add user preference check
}
```

## Building

1. Open project in Android Studio
2. Sync Gradle files
3. Run on device/emulator (API 26+)

## License

MIT License - feel free to use and modify!
