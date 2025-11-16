# Forge overview

Forge is a Kotlin Multiplatform framework designed to streamline the development of applications targeting Android, iOS, Desktop (JVM), and Web (JS/Wasm).

It is built around Clean Architecture and focuses on:

- Clear separation of concerns (Domain / Data / Presentation)
- Testability and modularity
- Platform‑agnostic abstractions (navigation, logging, networking, persistence, etc.)
- A clear separation between **API contracts** and **implementation details** for critical infrastructure such as networking and persistence
- APIs that are fully usable from **`commonMain`**, with platform‑specific details handled inside Forge modules

## Third‑party dependency model

Forge does not try to completely eliminate direct third‑party dependencies in all modules. Some modules (for example, paging or view model integrations) may depend directly on external libraries where it is pragmatic.

However, for key cross‑cutting concerns like **networking** and **data storage**, Forge deliberately separates **pure API modules** from **implementation modules**:

1. **API modules (no third‑party dependencies)**  
   For networking and DataStore, Forge provides API modules that:
    - Contain only Kotlin interfaces, models, and contracts.
    - Do **not** depend on any third‑party library.
    - Are safe to use as stable building blocks in any project, regardless of which underlying libraries you choose.

2. **Implementation modules (may use third‑party libraries)**  
   For each of these APIs, there is at least one implementation module that:
    - Depends on the corresponding API module.
    - Uses a specific third‑party library (for example, a Ktor implementation for networking, or a settings library for DataStore).
    - Can be replaced or swapped out without affecting modules that only depend on the API.

3. **Only API dependencies between Forge modules**  
   When one Forge module needs functionality from another (for example, a domain module using networking or persistence), it:
    - **Depends only on that module’s API**.
    - **Never depends directly on a concrete implementation module.**

   This ensures that:
    - Domain and presentation code stay completely agnostic of concrete libraries.
    - Implementation choices can be changed, replaced, or customized without touching dependent modules.
    - Testing is simplified, because test doubles can be plugged in at the API level.

Other Forge modules may bind more tightly to external libraries where it makes sense for ergonomics or integration (ViewModels for example)

## Project structure

The repository is organized into independent modules:

- `datastore` – API and implementations for multiplatform key–value storage.
- `event` – event utilities (domain/application events).
- `logger` – logging abstractions and implementations.
- `navigation` – API and implementations for navigation events and dispatching.
- `network` – API and implementations of a HTTP-Client.
- `outcome` – success/error/progress wrapper primitives.
- `paging` – paging utilities and abstractions.
- `usecase` – base classes and helpers for use case‑oriented business logic.
- `viewmodel` – stateful view model abstractions for unidirectional data flow.

You can use Forge as a whole, or pick individual modules (for example only `usecase` + `viewmodel`) and integrate them into an existing architecture.

## Key ideas

### Clean Architecture: Domain, Data, Presentation & UI

Forge explicitly encourages structuring your code according to Clean Architecture principles into three main layers:

1. **Domain layer**
    - Contains core business logic and rules.
    - Implemented as use cases, domain models, and pure abstractions.
    - Depends only on stable contracts (for example, networking or persistence APIs), never on concrete implementations.

2. **Data layer**
    - Implements the abstractions used by the domain layer.
    - Provides repositories, network and persistence implementations, caching, and integration with external systems.
    - Adapts third‑party libraries (for example, HTTP clients, settings libraries) behind the API modules.

3. **Presentation layer**
    - Contains stateful view models, navigation abstractions, and UI‑facing events.
    - Orchestrates use cases, maps domain models to UI state, and drives navigation.
    - Depends on domain and API contracts, not on concrete data implementations.
    - Platform‑specific views and components (Compose UIs, SwiftUI, Android Views, web UI, etc.).
    - Renders the current state and forwards user interactions as intents to the presentation layer.
    - Contains no business logic; it is a pure consumer of presentation state.

This separation makes it easy to reason about responsibilities, replace implementations, and share most of the code across platforms.

### Multiplatform architecture

### Use case–driven design

Use cases in Forge encapsulate a single unit of work (for example, “load user profile” or “update settings”) and:

- Expose a clear input/output contract.
- Emit Outcome values to model success, error, or progress.
- Are easy to test, as they are pure Kotlin classes with injected dependencies.

### State‑focused view models

View models keep UI state in a single source of truth and expose it as reactive streams. UI components observe this state and send user intents back to the view model, which:

- Interprets intents.
- Executes relevant use cases.
- Reduces the state accordingly.
- Optionally triggers navigation events.

This results in a predictable, debuggable unidirectional data flow.


## 2. APIs vs implementations

For **networking**, **navigation** and **DataStore**, Forge follows a strict pattern:

- An **API module**:
  - Lives in shared code.
  - Contains interfaces and models only.
  - Has **no third‑party dependencies**.
  - Is safe to use in Domain, Data, and Presentation layers in `commonMain`.

- One or more **implementation modules**:
  - Provide concrete implementations of the API using third‑party libraries (for example, HTTP client or settings library).

Forge itself only depends across modules via these **API surfaces**. The same rule is recommended for your code:

- **Domain modules** depend only on Forge APIs and your own abstractions.
- **Data modules** depend only on Forge APIs and your own abstractions.
- **No Domain or Data module should depend directly on a Forge implementation module.**

Best practice is to:

- Treat Forge implementation modules as **infrastructure details**.
- Reference them only from your **app / composition / infrastructure modules** (for example, the Android app module, iOS app, or backend host), where you wire concrete implementations into DI or manual wiring.

This ensures that:

- Domain, Data, and Presentation code stay independent of concrete libraries.
- You can swap implementations (for example, changing your HTTP client or DataStore backend) without touching Domain or Data modules.
- The entire Forge API surface is usable from `commonMain`.

## 3. Structure your app with Clean Architecture

Forge encourages the following layering:

1. **Domain layer (commonMain)**
   - Use cases and domain models.
   - Depends only on Forge API modules (for example, networking and DataStore APIs) and your own domain abstractions.
   - Contains no platform code and no third‑party dependencies beyond what you explicitly choose.

2. **Data layer (commonMain + platform source sets)**
   - Implements the contracts defined in Forge API modules and your own repository interfaces.
   - Provides repositories and composition logic that rely on injected abstractions.
   - **Should not depend directly on Forge implementation modules.**
   - Instead, receives concrete implementations via constructor/D.I. from app or infrastructure modules.

3. **Presentation layer (commonMain)**
   - View models, navigation abstractions, and UI events.
   - Uses Forge’s `:viewmodel`, `:navigation`, `:event`, `:usecase`, and `:outcome` modules.
   - Coordinates use cases and maps domain data to UI state.
   - Exposes state flows and events to the UI.
   - Depends only on APIs (Forge and your own), never on concrete implementations.
   - Platform‑specific UI (Compose, SwiftUI, web UI, etc.) or Compose Multiplatform.
   - Collects state and events from view models.
   - Sends user interactions back as intents.
   - Contains no business logic.

4. **infrastructure layer (platform source sets / app modules)**
   - Infrastructure / app modules that:
     - Pull in Forge implementation modules.
     - Bind API interfaces to implementations via DI or manual wiring.
