# Implementation Plan - Fix Daily Reset

The user reported that the water tracker doesn't reset every day, even when closed. The current logic only exists in `MainActivity.onCreate()`. If the app process stays alive in the background, `onCreate()` isn't called again. Furthermore, the reset logic is asynchronous and might not finish before the user interacts with the UI.

## Proposed Changes

I will centralize the reset and data loading logic in `MainApplication` and ensure it's called whenever any activity is resumed. This ensures that even if the app was in the background, it will check the date and reset if necessary.

### [MainApplication](file:///C:/Users/anous/Desktop/AndroidStudioProjects/WaterTracker/app/src/main/java/com/example/watertracker/MainApplication.kt)

- [MODIFY] Add a `syncWithDatabase(onComplete: () -> Unit = {})` function.
    - It will run in `applicationScope`.
    - It will compare the current date (using `Locale.US` for consistency) with the one in the `Updated` table.
    - If they differ, it will call `waterlogDao().resetAllCups()` and reset in-memory variables.
    - It will then load the latest counts and states from the database into `totalWater` and `cupChecked`.
    - Finally, it will call `onComplete()` on the main thread.

### [MainActivity](file:///C:/Users/anous/Desktop/AndroidStudioProjects/WaterTracker/app/src/main/java/com/example/watertracker/MainActivity.kt)

- [MODIFY] Move the reset/load logic from `onCreate` to a new `updateUI()` method.
- [MODIFY] Call `app.syncWithDatabase { updateUI() }` in `onResume()`. This covers both fresh starts and resumes from background.
- [MODIFY] Remove the redundant background logic from `onCreate`.

### [LogActivity](file:///C:/Users/anous/Desktop/AndroidStudioProjects/WaterTracker/app/src/main/java/com/example/watertracker/LogActivity.kt)

- [MODIFY] Add an `updateUI()` method to refresh the visibility of checkmarks and animation states based on `app.cupChecked`.
- [MODIFY] Call `app.syncWithDatabase { updateUI() }` in `onResume()`. This ensures that if the user opens `LogActivity` directly (if that were possible) or resumes it on a new day, the state is correct.

## Verification Plan

### Manual Verification
1. **Background Reset:**
    - Log water in `LogActivity`.
    - Go to `MainActivity`, verify plant/bar.
    - Background the app (Home button).
    - Change system date to tomorrow.
    - Open the app. Verify it resets immediately.
2. **Fresh Start Reset:**
    - Log water.
    - Force stop the app.
    - Change system date.
    - Open the app. Verify it's reset.
3. **LogActivity Refresh:**
    - Stay in `LogActivity`.
    - Background the app.
    - Change system date.
    - Resume the app. Verify checkmarks are gone.
