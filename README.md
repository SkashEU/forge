# Forge

Forge is a Kotlin Multiplatform framework designed to streamline the development of applications targeting Android, iOS, Desktop, and Web. It provides a solid foundation based on clean architecture principles, promoting modularity, testability, and maintainability.
Forge is built to match the architecture of all my projects.

## Modules

The project is structured into the following modules:

- **:examples:composeApp**: A sample application demonstrating the framework's usage. It's a Compose Multiplatform app targeting Android, iOS, Desktop, and Web.
- **:viewmodel**: A Kotlin Multiplatform library that contains the implementation of a state aware base ViewModel.
- **:usecase**: This library holds the base implementation of UseCase and OutcomeUseCase for more complex use cases (Handling of Success, Failure & Progressing).
- **:navigation**: Platform-agnostic API for navigation.
- **:outcome**: Provides a wrapper for representing the result of an operation, typically a success or a failure.
- **:logger**: A logging library for structured logging across all platforms, using the `kermit` library as default. Also exposes an interface to create your own logger.
- **:event**: For handling and dispatching events throughout the application.
- **:paging**: A library for implementing pagination, built on top of `androidx.paging.common`. It exposes a factory to create a map any kind of UseCase to a PagingSource.
- **:network:api**: This group of modules defines the API for the networking layer.
  - **:network:api:client**: Defines the contract for the network client.
  - **:network:api:request**: Contains the data models for API requests.
  - **:network:api:response**: Contains the data models for API responses.
  - **:network:api:session**: Manages session-related data, like authentication tokens.
- **:network:ktor**: An implementation of the `:network:api:client` using the Ktor networking library.

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

- **Kotlin Multiplatform**: For sharing code across different platforms.
- **Jetpack Compose**: For building declarative user interfaces.
- **Ktor**: For networking.
- **Kermit**: For logging.
