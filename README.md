# Forge overview

Forge is a Kotlin Multiplatform framework designed to streamline the development of applications targeting Android, iOS, Desktop (JVM), and Web (JS/Wasm).

It is built around Clean Architecture and focuses on:

- Clear separation of concerns (Domain / Data / Presentation)
- Testability and modularity
- Platform‑agnostic abstractions (navigation, logging, networking, persistence, etc.)
- A clear separation between **API contracts** and **implementation details** for critical infrastructure such as networking and persistence
- APIs that are fully usable from **`commonMain`**, with platform‑specific details handled inside Forge modules

## Table of Contents

1. [Clean Architecture](#1-clean-architecture)
2. [Project Structure & Modules](#2-project-structure--modules)
3. [Architectural Blueprint](#3-architectural-blueprint)
4. [Feature Examples](#4-feature-examples)
   - [UseCases & Outcome](#usecases--outcome)
   - [StateViewModel & UDF](#stateviewmodel--udf)
   - [Navigation & Typed Results](#navigation--typed-results)
   - [DataStore & Type-Safe Persistence](#datastore--type-safe-persistence)
   - [Network & HttpClient](#network--httpclient)
5. [Dependency Guidelines](#5-dependency-guidelines)

### 1. Clean Architecture
Forge separates code into distinct layers—**Domain, Data, and Presentation**. This makes your business logic easy to test and allows you to share the vast majority of your code across platforms.

### B. Strict API vs. Implementation Separation
For critical infrastructure (Networking, DataStore, Navigation...), Forge radically decouples the **contract** from the **execution**.

* **API Modules:** Contain *only* interfaces and models. They have **zero** third-party dependencies. They are safe to use anywhere in your `commonMain`.
* **Implementation Modules:** Implement the API using specific libraries (e.g., Ktor, OkHttp). These are treated as interchangeable "details."

> Your Domain and Data layers must depend **only** on API modules. They never know *which* library is performing the network request or saving the data.

---

## 2. Project Structure & Modules

Forge is modular. You can adopt the entire framework or cherry-pick specific libraries to fit your existing architecture.

| Module | Description
| :--- | :--- |
| **`outcome`** | Primitives for success, error, and progress handling. 
| **`usecase`** | Base classes for business logic units of work.
| **`viewmodel`** | Stateful abstractions for Unidirectional Data Flow (UDF). 
| **`network`** | HTTP Client abstractions (**API**) and implementations.
| **`datastore`** | Key-Value storage abstractions (**API**) and implementations.
| **`navigation`** | Event-based navigation dispatching.
| **`logger`** | Logging facades and platform writers.
| **`event`** | Utilities for domain and application-wide events.
| **`paging`** | Utilities for paginated data loading. 

---

## 3. Architectural Blueprint

Forge encourages a specific layering strategy to maximize code sharing and testability.

### I. Domain Layer (`commonMain`)
* **Role:** The heart of the application. Contains business rules and logic.
* **Components:** Use Cases, Domain Models, Repository Interfaces.
* **Dependencies:** Only Forge **API** modules (e.g., `network-api`). **No** platform code.

### II. Data Layer (`commonMain` + Platform)
* **Role:** The implementation of the domain's requirements.
* **Components:** Repositories, Mappers, Caching logic.
* **Dependencies:** Forge **API** modules. It relies on implementations injected via the constructor.

### III. Presentation Layer (`commonMain`)
* **Role:** State management and UI coordination.
* **Components:** ViewModels, UI State classes, Navigation events.
* **Dependencies:** Domain layer and Forge primitives (`viewmodel`, `outcome`).

### IV. Infrastructure / App Layer (Platform specific)
* **Role:** The "Composition Root." It wires everything together.
* **Components:** Dependency Injection (DI) modules, Platform entry points (Activity, Main).
* **Dependencies:** This is the **only** place that imports Forge **Implementation** modules (e.g., `network-ktor`) to inject them into the Data layer.

---

## 4. Feature Examples

### UseCases & Outcome
Encapsulate business logic in a `UseCase` that returns an `Outcome`. This forces you to handle success and failure scenarios explicitly.

The `OutcomeUseCase` requires three generic arguments to enforce type safety across your Clean Architecture layers.

`OutcomeUseCase<Params, S, E>`

1. **`Params` (Input Parameters)**
   * **What it is:** The data required to execute the use case.
   * **Usage:** These are passed as the argument to the `execute(params)` function.
   * **Best Practice:** If you need multiple arguments, group them into a `data class` (e.g., `LoginParams`). If the use case requires no input, use `Unit`.

2. **`S` (Success Type)**
   * **What it is:** The data returned when the operation completes successfully.
   * **Usage:** This becomes the `.data` property inside `Outcome.Success<S>`.
   * **Best Practice:** This should be a Domain Model, not a raw DTO or API response object.

3. **`E` (Error Type)**
   * **What it is:** The specific type representing a failure in this business logic.
   * **Usage:** This becomes the `.error` property inside `Outcome.Error<E>`.
   * **Best Practice:** Avoid using generic `Throwable` or `Exception`. Instead, use a **sealed interface** or **enum** (e.g., `LoginError.InvalidCredentials`) to force the UI to handle specific failure scenarios explicitly.


#### 1. Basic Implementation

```kotlin
class UpdateProfileUseCase : OutcomeUseCase<ProfileParams, UserProfile, ProfileError>() {

    // 1. The 'Params' type is used here as the argument
    override suspend fun FlowCollector<Outcome<UserProfile, ProfileError>>.execute(params: ProfileParams) {
        
        if (params.name.isEmpty()) {
            // 3. The 'Error' type is emitted here
            emitFailure(ProfileError.NameEmpty) 
            return
        }

        val updatedProfile = api.update(params)

        // 2. The 'Success' type is emitted here
        emitSuccess(updatedProfile) 
    }
}
```


#### 2. Handling Exceptions safely (`emitCatching`)

Avoid try-catch blocks by using `emitCatching`. It executes the block, captures exceptions, and maps them to your specific domain error type.

```kotlin
class ReadFileUseCase(
    private val fileManager: FileManager
) : OutcomeUseCase<String, String, FileError>() {

    override suspend fun FlowCollector<Outcome<String, FileError>>.execute(fileName: String) {
        // Tries to read the file. If an IOException occurs, it maps to FileError.ReadFailed
        emitCatching(
            errorMapper = { throwable -> FileError.ReadFailed(throwable.message) },
            block = { fileManager.readText(fileName) }
        )
    }
}
```

#### 3. Integrating Network Responses (`emitFrom`)
   
When working with the Forge Network module, `emitFrom` automatically unpacks an `ApiResponse`, handling the Success/Error branching for you.

```kotlin
class FetchUserUseCase(
    private val userRepository: UserRepository
) : OutcomeUseCase<String, User, UserError>() {

    override suspend fun FlowCollector<Outcome<User, UserError>>.execute(userId: String) {
        val apiResponse = userRepository.fetchUser(userId)
        
        // Automatically emits Success(User) OR Failure(UserError)
        emitFrom(apiResponse) { apiError ->
            // Map the network error (HTTP 404, 500, etc.) to a Domain Error
            when (apiError.code) {
                404 -> UserError.NotFound
                else -> UserError.Unknown
            }
        }
    }
}
```

#### 4. Consuming the Use Case

Since `OutcomeUseCase` returns a `Flow`, it integrates naturally with `ViewModels`and especialy when using Forges `StateViewModel`.

When you dont want to use the `StateViewModel`you can consume the UseCase like this

```kotlin
// In your ViewModel
fun increaseCount(currentCount: Int) {
    viewModelScope.launch {
        increaseCounterUseCase(IncreaseCounterUseCase.Params(currentCount))
            .collect { result ->
                when (result) {
                    is Outcome.Progress -> updateState { it.copy(isLoading = true) }
                    is Outcome.Success -> updateState { it.copy(isLoading = false, message = "Done!") }
                    is Outcome.Error -> updateState { it.copy(isLoading = false, error = result.error) }
                }
            }
    }
}
```


with the `StateViewModel`you can a few helper functions to consume UseCases

```kotlin
private val posts = getDummyPostsUseCase(Unit)
        .onEachOutcome(
            onProgress = { setState(ExampleState.Loading) },
            onFailure = { sendUIEvent(UIEvent.Snackbar(it.message)) },
            onSuccess = { posts ->
                reduceStateOrCreate<ExampleState.Success>(
                    reducer = { copy(posts = posts) },
                    create = { ExampleState.Success(posts = posts) }
                )
            }
        )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )
```

Directly consuming a UseCase

```kotlin
viewModelScope.launch {
   decreaseCounterUseCase.invoke(state.count)
      .collectOutcome(
         onProgress = { setState(DetailState.Loading) },
         onSuccess = { setState(DetailState.Details(it)) },
         onFailure = { sendUIEvent(UIEvent.Snackbar(it.message)) }
      )
}
```


### StateViewModel & UDF

The `StateViewModel` is the core of the Presentation layer. It enforces a strict Unidirectional Data Flow by ensuring that the View only observes **State** and sends **Intents**. It also includes built-in support for state caching (restoration), navigation, and one-off events.

**Key Features:**
* **Type-Safe Reducers:** Update state only if the current state matches a specific type (e.g., only update a counter if the state is `Loaded`).
* **State Machine Logic:** Process specific Intents only when in specific States via `handleIntent`.
* **State Caching:** Automatically caches previous state types to allow easy restoration (e.g., returning from a detail screen).
* **Navigation & Events:** Built-in dispatchers for routing and one-off UI events (Snackbars, Toasts).
* **Navigation Results:** Callback for Type-Safe navigation results triggered by other screens (NavBackStackEntry).
 
You normaly would want to create your own abstract BaseViewModel because the `StateViewModel` requires some dependencies that are easier to manage when having a BaseViewModel.
All examples will use exactly these BaseViewmodel:

```kotlin
abstract class BaseViewModel<State : Any, Intent> : StateViewModel<State, Intent, UIEvent> {
    constructor(initialState: State, eventBus: EventBus<UIEvent>?) : super(
        initialState = initialState,
        eventBus = eventBus,
        navigationDispatcher = resolveNavigationDispatcher(),
    )

    constructor(initialState: State, useEventBus: Boolean = true) : this(
        initialState = initialState,
        eventBus = if (useEventBus) resolveEventBus() else null,
    )

    private companion object Companion : KoinComponent {
        fun resolveEventBus(): EventBus<UIEvent> = get()

        fun resolveNavigationDispatcher(): NavigationDispatcher = get()
    }
}
```

```kotlin
abstract class BaseViewModelWithNavResult<State : Any, Intent, NavResult : Any> :
    NavResultAwareStateViewModel<State, Intent, UIEvent, NavResult> {
    constructor(initialState: State, eventBus: EventBus<UIEvent>?) : super(
        initialState = initialState,
        eventBus = eventBus,
        navigationDispatcher = resolveNavigationDispatcher(),
    )

    constructor(initialState: State, useEventBus: Boolean = true) : this(
        initialState = initialState,
        eventBus = if (useEventBus) resolveEventBus() else null,
    )

    private companion object Companion : KoinComponent {
        fun resolveEventBus(): EventBus<UIEvent> = get()

        fun resolveNavigationDispatcher(): NavigationDispatcher = get()
    }
}
```

```kotlin
// 1. Define the Contract
sealed interface DetailState {
    data object Loading : DetailState
    
    // Scoped Intents: These actions are only valid when the screen is in 'Details' state
    data class Details(val count: Int) : DetailState {
        sealed interface Intent : DetailState.Intent {
            data object IncreaseCounter : Intent
            data object DecreaseCounter : Intent
            data object NavigateBack : Intent
        }
    }
    
    // Base Intent Interface
    interface Intent
}
```

#### 1. Implement the ViewModel

This example demonstrates observing a use case, handling intent-state matching, and managing navigation results.

```kotlin
class DetailViewModel(
    count: Int,
    observeCounterUseCase: ObserveCounterUseCase,
    private val increaseCounterUseCase: IncreaseCounterUseCase,
    private val decreaseCounterUseCase: DecreaseCounterUseCase
) : BaseViewModel<DetailState, DetailState.Intent>(initialState = DetailState.Details(count = count)) {

    // 1. Reactive State Management
    // We listen to a UseCase Flow and update the UI state automatically.
    private val currentCount = observeCounterUseCase(Unit)
        .onEach {
            reduceStateOrCreate<DetailState.Details>(
                reducer = { copy(count = it) },
                create = { DetailState.Details(it) }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    // 2. The Entry Point (State Machine)
    override fun executeIntent(intent: DetailState.Intent) = when (intent) {
        // use handleIntent to ensure we are in the correct state before processing
        is DetailState.Details.Intent.DecreaseCounter -> handleIntent<_, _>(
            intent = intent,
            handler = ::handleDecreaseCounter
        )

        is DetailState.Details.Intent.IncreaseCounter -> handleIntent<_, _>(
            intent = intent,
            handler = ::handleIncreaseCounter
        )

        is DetailState.Details.Intent.NavigateBack -> handleIntent<_, _>(
            intent = intent,
            handler = ::handleNavigateUp
        )
        // If we receive an Intent not valid for the current state, handleIntent
        // automatically calls handleInvalidState().
        // This functions throws per default but should get overridden in production
    }

    // 3. Intent Handlers
    private fun handleDecreaseCounter(
        state: DetailState.Details,
        intent: DetailState.Details.Intent.DecreaseCounter
    ) {
        // 'collectOutcome' is a Forge helper to handle Success/Error/Progress easily
        viewModelScope.launch {
            decreaseCounterUseCase.invoke(DecreaseCounterUseCase.DecreaseCounterParams(state.count))
                .collectOutcome(onProgress = { setState(DetailState.Loading) })
        }
    }

    private fun handleIncreaseCounter(
        state: DetailState.Details,
        intent: DetailState.Details.Intent.IncreaseCounter
    ) {
        viewModelScope.launch {
            increaseCounterUseCase.invoke(IncreaseCounterUseCase.IncreaseCounterParams(state.count))
                .collectOutcome(onProgress = { setState(DetailState.Loading) })
        }
    }
    
    // 4. Navigation with Results
    private fun handleNavigateUp(
        state: DetailState.Details,
        intent: DetailState.Details.Intent.NavigateBack
    ) = dispatchNavigationEvent(
        event = NavigationEvent.NavigateUpWithResult(
            key = NavigationResult.ExampleNavResult, // Type-safe key
            value = state.count
        )
    )
}
```

### Navigation & Typed Results
Forge decouples navigation logic from the UI. ViewModels simply "request" navigation via events, and the UI layer (Compose, SwiftUI, Fragment) observes and executes them.

Crucially, Forge provides a standardized way to pass data **back** from a screen (like `startActivityForResult` or `setFragmentResult`), which works seamlessly across platforms.
This API does not depend on any navigation library. Forge provides a implementation of the API using the Nav2 androidx libraries fork by jetbrains that supports all targeted platforms. 
I might add alternatives later. You can ofc implement the API by yourself using any other library.


### Navigation & Typed Results

#### 1. Define Global Keys
First, define a central registry of keys. By inheriting from `NavResultKey<T>`, you enforce that a specific key always carries a specific type of data (e.g., `Int`), preventing runtime casting errors.

```kotlin
// Global file: NavigationResult.kt
sealed class NavigationResult<T>(
    key: String,
) : NavResultKey<T>(key) {
    // Defined globally, reusable across modules if needed
    data object ExampleNavResult : NavigationResult<Int>("example_nav_result")
}
```

#### 2. Define the Result Contract per feature
Create a sealed interface representing the possible results a flow can return. Primitives and kotlinx serialziables are supported

```kotlin
sealed interface MainNavigationResult {
    data class CounterUpdated(val count: Int) : MainNavigationResult
}
```

#### 3. Dispatching Navigation
As seen in the `DetailViewModel` example, you dispatch events using `dispatchNavigationEvent`.

```kotlin
// In DetailViewModel
fun saveAndExit(count: Int) {
     dispatchNavigationEvent(
        event = NavigationEvent.NavigateUpWithResult(
            key = NavigationResult.ExampleNavResult, // Type-safe key
            value = state.count
      )
}
```

#### 4. Receiving the Result
The receiving ViewModel inherits from NavResultAwareStateViewModel. You specify the expected result type as a generic argument (MainNavigationResult in this case).

The onNavResultReceived function is the entry point. Using reduceStateOrCreate is highly recommended here: it handles cases where the parent screen might be in a Loading or Empty state when the result arrives, ensuring the state is correctly initialized or updated.

```kotlin

class MainViewModel(
    getDummyPostsUseCase: GetDummyPostsUseCase
) : BaseViewModelWithNavResult<ExampleState, ExampleState.Intent, MainNavigationResult>(
    initialState = ExampleState.Loading,
) {
   override fun onNavResultReceived(event: MainNavigationResult) = when (event) {
        is MainNavigationResult.ExampleEvent -> reduceStateOrCreate<ExampleState.Success>(
            reducer = { copy(count = event.count) },
            create = { ExampleState.Success(count = event.count) }
        )
    }
}
```

#### 5. Wiring it up in Compose (Nav2 Integration)
Finally, you need to bridge the platform's navigation system with your ViewModel. Forge's Nav2 implementation provides the HandleNavResults composable.

This block listens to the savedStateHandle of the current backStackEntry and maps raw results into your typed MainNavigationResult events.
This allows you to have a app wide list of events (NavigationResult) that does not depend on any feature because you dont want to build relations between feature.

```kotlin
composable<AppScreen.Main> { backStackEntry ->

    val viewModel = koinViewModel<MainViewModel>()

    HandleNavResults(
        handle = backStackEntry.savedStateHandle,
        onResult = viewModel::onNavResultReceived
    ) {
        // Register the expected results
        OnResult(
            navResult = NavigationResult.ExampleNavResult, 
            mapper = { count -> 
                // 'count' is automatically cast to Int here because ExampleNavResult is Key<Int>
                MainNavigationResult.ExampleEvent(count) 
            }
        )
    }

    MainScreen(viewModel)
}
```

### DataStore & Type-Safe Persistence

Forge provides a strongly typed, platform-agnostic API for key-value storage. Instead of using "magic strings" and casting generic objects, you define **`DataEntry`** objects that carry both the key name and the expected data type.

**Key Features:**
* **Type Safety:** Keys define their own type (`Int`, `String`, `Boolean`, or Custom Objects).
* **Reactive:** Built-in support for observing values as a `Flow`.
* **Serialization:** Native support for storing complex objects using `kotlinx.serialization`.
* **Default Values:** Every key requires a default value, eliminating null checks in your business logic.

#### 1. Defining Keys (The Schema)
Define your storage schema in a global object. Use the `DataEntry` factory methods to create typed keys.

```kotlin
@Serializable
data class UserSession(val token: String, val expiry: Long)

object AppDataEntry {
    // 1. Primitive Types
    val Count = DataEntry.int(
        key = "app_count",
        defaultValue = 0
    )

    val IsOnboarded = DataEntry.boolean(
        key = "is_onboarded",
        defaultValue = false
    )

    // 2. Complex Objects (requires kotlinx.serialization)
    val Session = DataEntry.serializable(
        key = "user_session",
        defaultValue = UserSession("", 0L),
        serializer = UserSession.serializer(),
    )
}
```

##### 2. Writing Data (UseCase)

Inject the `DataStore` interface into your UseCases. Because `AppDataEntry.Count` is defined as a `DataEntry<Int>`, the set method enforces that you pass an Int.

```kotlin
class DecreaseCounterUseCase(
    private val dataStore: DataStore
): OutcomeUseCase<Int, Unit, String>() {

    override suspend fun FlowCollector<Outcome<Unit, String>>.execute(params: Int) {
        emitSuccess(dataStore.set(AppDataEntry.Count, params - 1))
    }
}
```

##### 3. Observing Data (Reactive)

The `observe` function returns a `Flow<T>`. It automatically emits the defaultValue if the key hasn't been written to disk yet.

```kotlin
class ObserveCounterUseCase(
    private val dataStore: DataStore
) : UseCase<Unit, Int>() {

    override fun execute(params: Unit): Flow<Int> {
        // Returns Flow<Int>. Emits 0 immediately if nothing is stored.
        return dataStore.observe(AppDataEntry.Count)
    }
}
```

##### 4. Creating a DataStore

Forge comes with the `datastore:multiplatform-settings` module that implementes the `:datastore:api`. 
Forge uses the androidx DataStore backend for Android, JVM & iOS. On web its a wrapper around LocalStorage.
You should only create a `DataStore` as singleton and inject it into all the classes that need it. 
Creating multible instances will break the observing for the web targets.

You can create it like this when using the `multiplatform-settings`

This class accepts a custom kotlinx Json when you want to define your own rules.

```kotlin
MultiplatformSettingsDataStore()
```

### Network & HttpClient

Forge provides a purely abstract HTTP client. This allows your **Domain** and **Data** layers to define network interactions without depending on a specific implementation like Ktor or OkHttp.

The Network API is granular, split into multiple modules so you can include only what you need:

* **`client`**: The core `HttpClient` interface and request builder.
* **`request`**: Data classes for header, http method, route, ApiRequest. This gets used by the client.
* **`response`**: The `ApiResponse` sealed classes and functional operators.
* **`session`**: Interfaces for token management and session expiration.

#### 1. Basic Usage (Repository)
The `HttpClient` is designed to handle the request execution and the response mapping in a single atomic operation. This keeps your Repositories clean and purely functional.

```kotlin
class DummyRepositoryImpl(
    private val httpClient: HttpClient
) : DummyRepository {

    override suspend fun fetchDummyPosts(): ApiResponse<List<DummyPost>> =
        httpClient.execute<DummyPostResponse, List<DummyPost>>(
            requestBuilder = {
                // The builder supports all standard verbs: get, post, put, patch, delete
                get("https://dummyjson.com/posts")
            },
            mapper = { response -> 
                response.posts.map { it.toDummyPost() } 
            }
        )
}
```

#### 2. Building Requests & Routes
The ApiRequestBuilder provides a fluent DSL for constructing requests. All HTTP verbs (get, post, put, delete, patch) provide overloads to accept either a raw string URL or a type-safe Route object.

Using Route objects (usually sealed classes) is recommended for complex APIs to ensure type safety and centralized path management.

```kotlin
httpClient.execute<UserDto, User>(
    requestBuilder = {
        // Option A: Raw String
        // post("https://api.example.com/users")
        
        // Option B: Type-Safe Route (Recommended)
        post(UserRoutes.Create)
        
        // Type-safe body serialization
        body(CreateUserRequest(name = "John"))
        
        // Headers & Parameters
        header("Authorization", "Bearer xyz")
        parameters {
            put("sort", "desc")
        }
    },
    mapper = { it.toDomain() }
)
```

#### 3. Functional Response Handling
The `ApiResponse` is a sealed class (`Success` | `Error`). Forge provides functional operators to chain, transform, and combine responses without "Callback Hell."

`map`: Transform the success body.

`flatMap`: Chain a second API call dependent on the first.

`onSuccess`: Perform a side-effect (like logging or caching).

`zip`: Run multiple requests in parallel and combine the results.

```kotlin
class LoadDashboardUseCase(
    private val userRepository: UserRepository,
    private val walletRepository: WalletRepository,
    private val analytics: AnalyticsService
) : OutcomeUseCase<Unit, DashboardUiModel, DashboardError>() {

    override suspend fun FlowCollector<Outcome<DashboardUiModel, DashboardError>>.execute(params: Unit) {
        
        // 1. Start with the User Profile (Sequential)
        val result = userRepository.fetchProfile()
            .flatMap { profile ->
                // 2. Now that we have the profile, fetch dependent data in PARALLEL
                zip(
                  { walletRepository.fetchBalance(profile.accountId) },
                  { walletRepository.fetchTransactions(profile.accountId) }
                ) { balance, transactions ->
                    // 3. Combine all data into a Domain Model
                    DashboardDomainModel(profile, balance, transactions)
                }
            }
            .onSuccess { dashboard ->
                // 4. Side Effect: Log success without altering the data flow
                analytics.logEvent("dashboard_loaded", mapOf("user_id" to dashboard.profile.id))
            }
            .map { domainModel ->
                // 5. Transform Domain Model -> UI Model
                DashboardUiModel(
                    welcomeMessage = "Hello, ${domainModel.profile.firstName}",
                    balance = "$${domainModel.balance.amount}",
                    recentActivity = domainModel.transactions.take(5)
                )
            }

        // 6. Emit the result (The UseCase handles the Outcome wrapping automatically)
        emitFrom(result) { apiError ->
            // Map HTTP errors to Domain errors if needed
            DashboardError.LoadFailed(apiError.reason)
        }
    }
}
```

#### 4. Authentication

Forge provides a API to append and refresh bearer tokens. For this you simply need to implement the `TokenAuthenticator` interface. 

```kotlin
class TokenAuthenticatorImpl(
    private val dataStore: DataStore,
    private val sessionManagerHandler: SessionExpirationHandler,
) : TokenAuthenticator {
    override suspend fun loadTokens(authApiClient: ApiClient): AuthTokens? {
        val access = dataStore
            .get(AppDataEntry.AuthToken)
            .takeUnless { it.isNullOrBlank() } ?: return null

        val refresh = dataStore
            .get(AppDataEntry.RefreshToken)
            .takeUnless { it.isNullOrBlank() } ?: return null

        return AuthTokens(
            bearer = access,
            refresh = refresh
        )
    }

    override suspend fun refreshTokens(authApiClient: ApiClient): AuthTokens? {

        val refresh = dataStore
            .get(AppDataEntry.RefreshToken)
            .takeUnless { it.isNullOrBlank() } ?: return null

        val response = authApiClient.execute<TokenRefreshResponse, TokenRefresh>(
            requestBuilder = {
              method(HttpMethod.Get)
              route(Api.Auth.Refresh)
              header(HeaderValue.Bearer(refresh))
            },
          mapper = { it.toRefreshToken() },
        }.onSuccess { refresh ->
            dataStore.set(AppDataEntry.AuthToken, refresh.body.token)
            dataStore.set(AppDataEntry.RefreshToken, refresh.body.refresh)
        }

        when (response) {
            is ApiResponse.Error -> {
                sessionManagerHandler.onSessionExpired()
                return null
            }

            is ApiResponse.Success -> return AuthTokens(
                bearer = response.body.token,
                refresh = response.body.refresh
            )
        }

    }
}
```

Register it on the client with the `authentication` function. 
Forge will now append bearers to every request when `loadTokens` returns a Token and automaticly trigger `refreshTokens` once the token expires.

#### 4. Usage

Forge provides a implementation of the network api using ktor. You can configure it like that

```kotlin
KtorApiClient {
        authentication(get())
        defaultHeaders {
            append("X-App-Version", "1.0.0")
        }
    }
```


This returns a `HttpClientBundle`. The bundle contains the `HttpClient` and a instance of the `StateClearable`
You can ignore the `StateClearable` unless you use the authentication API with the Ktor Client. 
In this case the ktor modules provides a `KtorClientStateClearer` which clears Ktors internal `authProvider` state. 
You'd need to call this when you want to logout a user for example


## 5. Dependency Guidelines

To maintain architectural integrity, follow these strict dependency rules:

1. **Domain Modules:**
    * ✅ Depends on `network-api`, `datastore-api`, `usecase`, `outcome`.
    * ❌ **Never** depends on `network-ktor` or `datastore-multiplatformsettings`.

2. **Data Modules:**
    * ✅ Depends on Domain modules and Forge APIs.
    * ❌ **Never** depends on implementation modules. It receives the implementation instances via the constructor.

3. **App Module (The Root):**
    * ✅ Depends on everything.
    * This is where you import `network-ktor` and bind it to the `HttpClientApi` interface in your Dependency Injection graph.
