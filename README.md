# Forge

Forge is a Kotlin Multiplatform framework designed to streamline the development of applications targeting Android, iOS, Desktop, and Web. It provides a solid foundation based on clean architecture principles, promoting modularity, testability, and maintainability.
Forge is built to match the architecture of all my projects.

## Modules

The project is structured into the following modules:

-   **:examples:composeApp**: A sample application demonstrating the framework's usage. It's a Compose Multiplatform app targeting Android, iOS, Desktop, and Web.
-   **:viewmodel**: A Kotlin Multiplatform library that contains the implementation of a state aware base ViewModel.
-   **:usecase**: This library holds the base implementation of UseCase and OutcomeUseCase for more complex use cases (Handling of Success, Failure & Progressing).
-   **:navigation**: Platform-agnostic API for navigation.
-   **:outcome**: Provides a wrapper for representing the result of an operation, typically a success or a failure.
-   **:logger**: A logging library for structured logging across all platforms, using the `kermit` library as default. Also exposes an interface to create your own logger.
-   **:event**: For handling and dispatching events throughout the application.
-   **:paging**: A library for implementing pagination, built on top of `androidx.paging.common`. It exposes a factory to create a map any kind of UseCase to a PagingSource.
-   **:datastore:api**: Defines a platform-agnostic interface (`DataStore`) for simple key-value persistence, using typed keys (`DataEntry`).
-   **:datastore:settings**: The default implementation of `:datastore:api`. It's built on the `multiplatform-settings` library, which wraps `androidx.datastore` on Android/JVM/iOS and `LocalStorage` on Web.
-   **:network:api**: This group of modules defines the API for the networking layer.
    -   **:network:api:client**: Defines the contract for the network client.
    -   **:network:api:request**: Contains the data models for API requests.
    -   **:network:api:response**: Contains the data models for API responses.
    -   **:network:api:session**: Manages session-related data, like authentication tokens.
-   **:network:ktor**: An implementation of the `:network:api:client` using the Ktor networking library.

---

## DataStore Usage

The `:datastore:api` modules provide a simple, type-safe, and asynchronous API for key-value persistence across all platforms.

* **`DataStore`**: The core interface providing `get`, `set`, `delete`, and `observe` (Flow-based) methods.
* **`DataEntry<T>`**: A typed key that encapsulates the storage **key (String)**, a **default value**, and the **type information** (either primitive or a `KSerializer`).

The default implementation (`:datastore:multiplatform-settings`) uses [multiplatform-settings](`https://github.com/russhwolf/multiplatform-settings`), which leverages `androidx.datastore` on Android, iOS, JVM and `LocalStorage` on Web (The module contains a FlowSettingsWrapper for Web because the lib does not include it by default. This made it way easier to use in common code).

### 1. Defining Your Keys

It's best practice to define your `DataEntry` keys as objects or in a companion object for easy reuse.

```kotlin
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.skash.forge.datastore.api.DataEntry

object AppSettings {
    val USERNAME = DataEntry.string(
        key = "prefs_username",
        defaultValue = "Guest"
    )

    val LAUNCH_COUNT = DataEntry.int(
        key = "prefs_launch_count",
        defaultValue = 0
    )

    val IS_ONBOARDING_COMPLETE = DataEntry.boolean(
        key = "prefs_onboarding_complete",
        defaultValue = false
    )

    // You can also store custom serializable data classes
    @Serializable
    data class UserPreferences(val theme: String = "dark", val notifications: Boolean = true)

    val USER_PREFERENCES = DataEntry.serializable(
        key = "prefs_user_preferences",
        defaultValue = UserPreferences(),
        serializer = UserPreferences.serializer(),
        json = Json { ignoreUnknownKeys = true }
    )
}
```

### 2. Using the DataStore (with UseCases and ViewModels)

While you can call the DataStore directly, it's highly recommended to wrap all data operations within UseCases (from the :usecase module). This promotes clean architecture, separates concerns, and makes your business logic testable.

Note: You should typically create a single instance of your DataStore implementation (e.g., MultiplatformSettingsDataStore()) and provide it to your UseCases via dependency injection.

## Example 1: Observing data with UseCase

```kotlin
import com.skash.forge.datastore.DataStore
import com.skash.forge.usecase.UseCase
import kotlinx.coroutines.flow.Flow

/**
* Observes the current username from the DataStore.
  */
  class ObserveUsernameUseCase(
    private val dataStore: DataStore
  ) : UseCase<Unit, String>() {

    override fun execute(params: Unit): Flow<String> { 
        return dataStore.observe(AppSettings.USERNAME)
    } 
  }

```

## Example 2: Setting data with OutcomeUseCase

This is perfect for one-shot suspend operations like set or get. OutcomeUseCase automatically wraps the result in Success, Failure, or Progressing.

```kotlin
import com.skash.forge.datastore.DataStore
import com.skash.forge.outcome.Outcome
import com.skash.forge.usecase.OutcomeUseCase
import kotlinx.coroutines.flow.FlowCollector

/**
 * Saves a new username to the DataStore.
 */
class SetUsernameUseCase(
    private val dataStore: DataStore
) : OutcomeUseCase<String, Unit, Throwable>() { // Params: String, Success: Unit, Error: Throwable

    override suspend fun FlowCollector<Outcome<Unit, Throwable>>.execute(params: String) {
        // emitCatching will handle exceptions and emit Failure automatically
        emitCatching(errorMapper = { it }) {
            dataStore.set(AppSettings.USERNAME, params)
            // No need to return anything for a 'set' operation
        }
    }
}
```

## ViewModel Example

Your ViewModel (from the :viewmodel module) can then inject these UseCases. It collects the flows and exposes state to the UI, while handling actions.

```kotlin
import com.skash.forge.viewmodel.StateViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyScreenViewModel(
    observeUsernameUseCase: ObserveUsernameUseCase,
    private val setUsernameUseCase: SetUsernameUseCase
) : StateViewModel() { // normally takes arguments we simplified it here

    // 1. Observe data and expose it as StateFlow to the UI
    val usernameState: StateFlow<String> = observeUsernameUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings.USERNAME.defaultValue
        )

    // 2. Create functions to handle UI events 
    // we would implement this using intents and never expose functions like this. Its just an easy example
    fun onUsernameChanged(newUsername: String) {
        viewModelScope.launch {
            // The OutcomeUseCase flow emits Progressing, then Success or Failure
            setUsernameUseCase(newUsername).collect { outcome ->
                // In a real app, you would handle the outcome
                // e.g., show a loading spinner, display an error, etc.
                when (outcome) {
                    is Outcome.Success -> println("Username saved!")
                    is Outcome.Failure -> println("Error saving username: ${outcome.error}")
                    is Outcome.Progressing -> println("Saving...")
                }
            }
        }
    }
}
```

## How to Build and Run

To build and run the project, you can use the following Gradle commands from the root directory.

### Android

To build and install the app on a connected Android device or emulator:

```sh
./gradlew :examples:composeApp:installDebug
```

### Desktop (JVM)

To run the desktop application:

```sh
./gradlew :examples:composeApp:run
```

### iOS

To build and run on an iOS simulator or device, open the project in Xcode:

1.  Open `iosApp/iosApp.xcworkspace` in Xcode.
2.  Select the `iosApp` scheme and a target device.
3.  Click the "Run" button.

### Web (JavaScript)

To run the web application in a browser with hot reload:

```sh
./gradlew :examples:composeApp:jsBrowserDevelopmentRun
```

### Web (Wasm)

To run the web application in a browser with hot reload:

```sh
./gradlew :examples:composeApp:wasmJsBrowserDevelopmentRun
```

## Key Dependencies

The project relies on several key libraries and technologies:
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html): For sharing code across different platforms.
- [Jetpack Compose](https://developer.android.com/compose): For building declarative user interfaces.
- [Ktor](https://github.com/ktorio/ktor): For the default HttpClient implementation.
- [Kermit](https://github.com/touchlab/Kermit): For default logging.
- [multiplatform-settings](https://github.com/russhwolf/multiplatform-settings): For the default DataStore implementation.