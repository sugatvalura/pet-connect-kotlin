# Kotlin From 0% — Learn Through PetConnect

> **How to use this file:** Do not try to memorise all of Kotlin in one day.
> Read one small section, open the named project file, then say the answer out
> loud in your own words. In an interview, simple and correct is much better
> than complicated words you do not understand.

---

## 0. Your first 30-second introduction

Say this slowly and naturally:

> “PetConnect is a native Android app written in Kotlin. Pet owners can create
> pet profiles, discover nearby pets, match, chat, and create listings. The UI
> uses Jetpack Compose. I use MVVM: Compose screens send user actions to a
> ViewModel, and ViewModels expose UI state using StateFlow. Business rules are
> in a domain layer, and repository interfaces keep the app independent from
> its storage. Hilt provides dependencies, Room stores chat messages, and
> coroutines and Flow handle asynchronous data.”

You do **not** need to say every technology unless asked. Understand the flow
first:

```text
User taps “Save pet”
        ↓
NewPetScreen calls viewModel.save()
        ↓
NewPetViewModel validates data and creates NewPet
        ↓
PetRepository saves it (the interface hides where)
        ↓
StateFlow changes
        ↓
Compose redraws the screen
```

---

# Part 1 — Kotlin vocabulary from zero

## 1. What is Kotlin?

Kotlin is a modern programming language used for Android. Android officially
recommends it. Kotlin code is compiled to JVM bytecode, so it can run on
Android just like Java.

A `.kt` file is a Kotlin file.

Example from this project:

```kotlin
package com.petconnect.ui.pet
```

- `package` is the logical address/namespace of the code.
- It helps avoid name conflicts. You can have two classes named `User` in
  different packages.
- The folder normally matches the package: `ui/pet` maps to
  `com.petconnect.ui.pet`.

## 2. Variables: `val` and `var` — very common question

```kotlin
val petName = "Bruno"      // cannot point to another value later
var selectedPhoto = "a.jpg" // can be changed later
selectedPhoto = "b.jpg"
```

**Memorise:**

> “`val` is read-only and `var` is mutable. I prefer `val` by default because
> immutable data is safer and easier to reason about. I use `var` only when the
> variable must change.”

Important: `val` means the **reference** cannot change. An object referenced by
it might still be mutable. In this app, we prefer immutable `data class` state
and create a copied value instead of changing it directly.

## 3. Types

```kotlin
val name: String = "Bruno"
val age: Int = 3
val distance: Double = 2.5
val isVaccinated: Boolean = true
```

Kotlin often infers a type, so this is also valid:

```kotlin
val name = "Bruno" // Kotlin infers String
```

Use an explicit type when it makes a public API clearer:

```kotlin
val form: StateFlow<NewPetForm> = _form.asStateFlow()
```

Read it as: “`form` is a StateFlow which contains a `NewPetForm`.”

## 4. Null safety: `?`, `?.`, `?:`, `!!`

Kotlin separates “a value definitely exists” from “a value may not exist.”

```kotlin
val city: String = "Delhi"       // never null
val photoUri: String? = null      // may be null
```

Useful operators:

```kotlin
photoUri?.length                 // call length only if photoUri is not null
val label = photoUri ?: "No photo" // use right value if left is null
```

Avoid this unless you are 100% certain:

```kotlin
photoUri!!.length // crashes if photoUri is null
```

Real project example — open `ui/pet/NewPetViewModel.kt`:

```kotlin
val stored = images.import(uri)
photos = if (stored != null) f.photos + stored else f.photos
```

`images.import` may fail and return `null`, so the app adds a photo only when
`stored != null`.

**Memorise:**

> “Kotlin null safety moves many NullPointerException problems from runtime to
> compile time. `String` cannot be null; `String?` can. I use safe calls `?.`
> and the Elvis operator `?:` instead of unsafe `!!`.”

## 5. Functions: `fun`

A function is a named piece of work.

```kotlin
fun greet(name: String): String {
    return "Hello, $name"
}
```

- `fun` starts a function.
- `name: String` is an input parameter.
- `: String` after `)` means the function returns a `String`.
- `return` sends the result back.

Short functions can omit braces and `return`:

```kotlin
fun square(number: Int) = number * number
```

From `NewPetViewModel.kt`:

```kotlin
fun removePhoto(uri: String) = _form.update { it.copy(photos = it.photos - uri) }
```

Read it as: “When `removePhoto` receives a URI, update the form with a copy
whose photo list excludes that URI.”

## 6. Classes and objects

A class is a blueprint. An object is a real instance made from it.

```kotlin
class Pet(val name: String, val breed: String)

val bruno = Pet("Bruno", "Golden Retriever")
```

`bruno` is an object; `Pet` is its class.

## 7. `data class` — extremely important

Open `ui/pet/NewPetViewModel.kt` and find `data class NewPetForm`.

```kotlin
data class NewPetForm(
    val name: String = "",
    val breed: String = "",
    val saving: Boolean = false,
)
```

A `data class` represents data/state. Kotlin automatically gives it useful
features such as `equals`, `hashCode`, `toString`, destructuring, and `copy`.

The most useful feature here is `copy`:

```kotlin
val emptyForm = NewPetForm()
val nameAdded = emptyForm.copy(name = "Bruno")
```

`emptyForm` stays unchanged. `nameAdded` is a new object.

**Why does this matter?** State that does not change behind your back is easier
to test, debug, and display correctly in Compose.

**Memorise:**

> “I use data classes for UI state and domain models. `copy` supports immutable
> state updates: I create a changed copy instead of mutating the old state.”

## 8. Collections: `List`, `Set`, `Map`

```kotlin
val photos: List<String> = listOf("a.jpg", "b.jpg")
val blockedOwnerIds: Set<String> = setOf("owner-1", "owner-2")
val errors: Map<String, String> = mapOf("name" to "Name is required")
```

- `List`: ordered values; duplicates are allowed.
- `Set`: unique values; good for blocked IDs.
- `Map`: key → value; good for form field → error message.

Common operations used throughout this project:

```kotlin
val dogs = pets.filter { it.species == PetSpecies.DOG }
val names = pets.map { it.name }
val firstDog = pets.firstOrNull { it.species == PetSpecies.DOG }
val hasPhotos = photos.isNotEmpty()
```

The `{ it... }` part is a **lambda** (a small function passed as an argument).
`it` means the current item when there is only one argument.

## 9. Conditions: `if` and `when`

```kotlin
if (errors.isNotEmpty()) {
    return
}
```

Kotlin’s `if` can return a value:

```kotlin
val message = if (saved) "Saved" else "Not saved"
```

`when` is Kotlin’s clean switch-like expression:

```kotlin
when (message.kind) {
    MessageKind.TEXT -> showText()
    MessageKind.IMAGE -> showImage()
    MessageKind.LOCATION -> showMap()
}
```

## 10. Enum and sealed types

An `enum class` is a fixed list of named values. Example: `PetSpecies.DOG`,
`PetSpecies.CAT`.

A `sealed interface` is a fixed family of related types. Open
`ui/chat/ChatViewModel.kt`:

```kotlin
sealed interface ChatUiState {
    data object Loading : ChatUiState
    data object Unavailable : ChatUiState
    data class Ready(...) : ChatUiState
}
```

This says chat UI can be only Loading, Unavailable, or Ready. It makes the UI
handle every state explicitly.

---

# Part 2 — How this project is organised

Start from `app/src/main/java/com/petconnect/`.

```text
com.petconnect/
├── ui/             Screens, ViewModels, navigation, reusable UI components
├── domain/         App rules: models, use cases, repository interfaces
├── data/           Actual storage/device/network implementations
├── di/             Hilt dependency injection wiring
└── MainActivity.kt Android app entry point
```

## `ui/` — what the user sees

Examples:

- `ui/pet/NewPetScreen.kt`: Compose function that draws the Add Pet screen.
- `ui/pet/NewPetViewModel.kt`: screen state and actions such as `save()`.
- `ui/components/`: reusable visual pieces like `Field`, `PrimaryButton`.

A screen should be “dumb”: draw state and forward clicks. Do not put database
queries or complex business rules in a screen.

## `domain/` — app meaning and business rules

Examples:

- `domain/model/Pet.kt`: pet-related data classes.
- `domain/repository/Repositories.kt`: contracts such as `PetRepository`.
- `domain/usecase/FormValidator.kt`: validation rules.
- `domain/usecase/CompatibilityCalculator.kt`: matching score rules.

The domain layer should not import Android UI, Room, or DataStore. That makes
it easy to unit test.

## `data/` — how data is actually obtained/saved

Examples:

- `data/repository/RoomChatRepository.kt`: stores chats using Room.
- `data/db/ChatDao.kt`: SQL query definitions for chat rows.
- `data/local/JsonStore.kt`: small preference-style persisted data.
- `data/repository/DeviceLocationRepository.kt`: device location implementation.

## `di/` — dependency injection

`di/AppModule.kt` tells Hilt which real implementation to provide.

Example idea:

```kotlin
@Binds
abstract fun chat(impl: RoomChatRepository): ChatRepository
```

Read this as: “Whenever code asks for the `ChatRepository` interface, give it a
`RoomChatRepository`.”

This is powerful because a ViewModel knows only the interface. Later, you can
replace Room with a server-backed repository without rewriting the ViewModel.

---

# Part 3 — Follow one real feature: Add a Pet

Open these two files side-by-side:

1. `ui/pet/NewPetScreen.kt`
2. `ui/pet/NewPetViewModel.kt`

## Step 1: Compose screen starts

```kotlin
@Composable
fun NewPetScreen(onDone: () -> Unit, viewModel: NewPetViewModel = hiltViewModel())
```

- `@Composable` means this function describes UI.
- `onDone: () -> Unit` is a function parameter: “call this when finished.”
- `hiltViewModel()` asks Hilt to create/provide the ViewModel.
- `Unit` means “returns nothing useful,” like Java `void`.

## Step 2: screen observes state

```kotlin
val f by viewModel.form.collectAsStateWithLifecycle()
```

- `form` is a `StateFlow<NewPetForm>` from the ViewModel.
- `collectAsStateWithLifecycle()` observes it safely only while the screen is active.
- `by` lets us write `f.name` instead of `f.value.name`.
- When `form` changes, Compose automatically redraws the affected UI.

## Step 3: user types a name

```kotlin
Field("Pet name", f.name, { v ->
    viewModel.edit("name") { it.copy(name = v) }
})
```

The user types text. `Field` calls the lambda with the new value (`v`). The
screen forwards it to the ViewModel. The ViewModel does the update:

```kotlin
fun edit(field: String? = null, transform: (NewPetForm) -> NewPetForm) =
    _form.update { transform(it).copy(errors = if (field == null) it.errors else it.errors - field) }
```

Do not panic: read it in small pieces.

1. `_form` is private mutable state.
2. `.update { ... }` receives the old form as `it`.
3. `transform(it)` runs the requested edit, e.g. `it.copy(name = v)`.
4. `.copy(errors = ...)` returns a new form and removes that field’s old error.
5. Public `form` emits the new state.
6. Compose redraws the screen with the new name.

## Step 4: user clicks Save

```kotlin
PrimaryButton("Save pet", viewModel::save, loading = f.saving)
```

`viewModel::save` is a **function reference**. It means “pass the `save`
function itself; call it when the button is clicked.”

In the ViewModel:

```kotlin
fun save() {
    val f = _form.value
    val errors = validator.pet(f.name, f.breed, f.dateOfBirth)
    if (errors.isNotEmpty()) {
        _form.update { it.copy(errors = errors) }
        return
    }
    viewModelScope.launch {
        // async save work happens here
    }
}
```

The ViewModel first validates. If invalid, it stores errors and stops using
`return`. If valid, it starts a coroutine for potentially slow work.

## Step 5: create the domain object and save

```kotlin
val created = pets.createPet(NewPet(...))
```

- `NewPet` is a domain data class: it carries valid input for creating a pet.
- `pets` has type `PetRepository`, an interface.
- The actual repository saves it. The ViewModel does not know or care whether
  that data is local, Room, DataStore, or a future API.

This is **Dependency Inversion** in a real project.

---

# Part 4 — Android concepts you must know

## 1. MVVM

MVVM means Model–View–ViewModel.

- **Model:** app data and rules, such as `Pet`, `NewPet`, use cases, repositories.
- **View:** Compose screen, such as `NewPetScreen`.
- **ViewModel:** holds UI state and handles screen actions, such as
  `NewPetViewModel`.

**Memorise:**

> “The View renders state and sends events. The ViewModel owns screen state and
> calls domain/repository code. This separates UI from logic and makes the
> ViewModel easy to test.”

## 2. StateFlow and Flow

A `Flow<T>` is a stream of values over time.

```kotlin
val messages: Flow<List<Message>>
```

A `StateFlow<T>` is a Flow that always has a current value.

```kotlin
private val _form = MutableStateFlow(NewPetForm()) // private writable flow
val form: StateFlow<NewPetForm> = _form.asStateFlow() // public read-only flow
```

**Memorise:**

> “I expose immutable StateFlow to the UI and keep MutableStateFlow private.
> The ViewModel updates it; Compose observes it and redraws automatically.”

## 3. Coroutines, `suspend`, and `launch`

A coroutine is a lightweight task for asynchronous work, for example database,
network, image copying, or location access.

```kotlin
viewModelScope.launch {
    val stored = images.import(uri)
}
```

- `viewModelScope` is cancelled automatically when its ViewModel is cleared.
- `launch` starts a coroutine and does not return a result to its caller.
- A `suspend fun` can pause without blocking the UI thread.

**Memorise:**

> “I use coroutines so slow work does not freeze the main thread. I use
> `viewModelScope` because Android cancels it when the ViewModel is destroyed.”

## 4. Compose

Compose is Android’s declarative UI toolkit.

Instead of saying “find this text view and change its text,” we describe UI for
the current state:

```kotlin
if (f.saving) {
    // show loading UI
} else {
    // show Save button
}
```

When state changes, Compose recomposes: it runs relevant composable functions
again and updates the screen efficiently.

**Memorise:**

> “Compose is declarative. The UI is a function of state. When StateFlow emits
> a new value, Compose recomposes the affected UI.”

## 5. Repository pattern

A repository is the boundary between app logic and data sources.

```kotlin
interface ChatRepository {
    fun messages(matchId: String): Flow<List<Message>>
    suspend fun send(...): Message
}
```

`RoomChatRepository` implements it. Use cases and ViewModels depend on the
interface, not Room directly.

## 6. Hilt / dependency injection

Dependency injection means objects receive what they need instead of creating
those dependencies themselves.

Bad:

```kotlin
class MyViewModel : ViewModel() {
    val repository = RoomChatRepository(...) // tightly coupled
}
```

Good:

```kotlin
class MyViewModel @Inject constructor(
    private val repository: ChatRepository,
) : ViewModel()
```

Hilt constructs and supplies the implementation.

---

# Part 5 — Interview questions that should be on your tongue

## Kotlin basics

### What is the difference between `val` and `var`?

> “`val` is read-only and `var` can be reassigned. I choose `val` by default
> because immutable code is safer.”

### What is a `data class`?

> “It is a class mainly for holding data. Kotlin generates equals, hashCode,
> toString, component functions, and copy. I use it for UI state like
> `NewPetForm` and models like `Pet`.”

### What is null safety in Kotlin?

> “A normal type such as `String` cannot be null. `String?` can be null. Kotlin
> makes me handle nullable values with `?.`, `?:`, or an explicit null check,
> preventing many NullPointerExceptions.”

### What are `let`, `apply`, `also`, `run`, and `with`?

Keep this simple:

> “They are scope functions. `let` is useful for nullable values or transforming
> a result; `apply` configures an object and returns it; `also` performs a side
> effect and returns the original object; `run` computes a result in an object
> context; `with` is like `run` but is not an extension.”

Example from this project style:

```kotlin
stored?.let { photo -> f.photos + photo }
```

### What is a lambda?

> “A lambda is a function without a name that can be passed to another
> function. In `pets.filter { it.species == DOG }`, the lambda decides whether
> each pet should remain.”

### What is the difference between `List` and `MutableList`?

> “`List` exposes read operations. `MutableList` also allows add, remove, and
> update. I prefer immutable `List` in UI state.”

## Coroutines and Flow

### What is a coroutine?

> “A coroutine is a lightweight asynchronous task. It can suspend without
> blocking the main thread, so the app stays responsive during IO work.”

### `launch` versus `async`?

> “`launch` is for work with no returned result. `async` returns a `Deferred`
> result which I receive with `await()`. I use `async` only when parallel
> results are actually needed.”

### What does `suspend` mean?

> “It marks a function that can pause and resume without blocking the thread.
> It must be called from another suspend function or a coroutine.”

### Flow versus StateFlow?

> “Flow is a cold stream: it starts producing when collected. StateFlow is a
> hot state holder: it always has a current value and sends it immediately to a
> new collector. I use StateFlow for screen state.”

### Why `viewModelScope`?

> “It is tied to the ViewModel lifecycle, so child coroutines are cancelled
> automatically when the ViewModel goes away. This prevents leaked work.”

## Android architecture

### Why use MVVM?

> “MVVM keeps UI rendering separate from state and logic. Compose screens stay
> simple, while ViewModels can be unit tested without a device.”

### Why use a repository?

> “It hides data-source details behind a domain interface. That gives one source
> of truth and lets us change the storage implementation without changing UI or
> business logic.”

### Why Room instead of DataStore for chat?

> “DataStore is ideal for small preferences. Chat is a growing collection that
> needs indexed queries, ordering, and observation. Room handles rows and SQL
> efficiently, and DAO queries can return Flow.”

### Explain Clean Architecture in this project.

> “The UI layer contains Compose screens and ViewModels. The domain layer owns
> models, use cases, and repository interfaces. The data layer implements those
> interfaces using Room, DataStore, device services, or network. Dependencies
> point inward: domain does not depend on UI or data.”

## SOLID questions

### SRP: Single Responsibility Principle

> “A class should have one reason to change. In PetConnect, `ChatDao` owns SQL,
> `RoomChatRepository` owns data access behavior, and `ChatViewModel` owns UI
> state. They do not mix those responsibilities.”

### OCP: Open/Closed Principle

> “Software should be open for extension but closed for modification. The
> compatibility calculator uses independent scoring rules. I can add a rule
> without rewriting the calculator.”

### LSP: Liskov Substitution Principle

> “Any implementation of an interface should work wherever that interface is
> expected. For example, a fake `ChatRepository` in tests can substitute the
> Room implementation because it obeys the same contract.”

### ISP: Interface Segregation Principle

> “Clients should not depend on methods they do not need. The project has small
> repository interfaces such as `ChatRepository` and `ImageRepository`, rather
> than one huge repository.”

### DIP: Dependency Inversion Principle

> “High-level code depends on abstractions, not details. ViewModels depend on
> `ChatRepository`, and Hilt supplies `RoomChatRepository`. We changed chat
> storage without changing the ViewModels.”

---

# Part 6 — Questions they may ask by opening your code

## “Explain this line”

```kotlin
val f by viewModel.form.collectAsStateWithLifecycle()
```

Answer:

> “It observes a StateFlow from the ViewModel as Compose state while respecting
> the Android lifecycle. `by` unwraps the State value, so I can use `f.name`.
> A new emission triggers recomposition.”

## “Why is `_form` private but `form` public?”

```kotlin
private val _form = MutableStateFlow(NewPetForm())
val form: StateFlow<NewPetForm> = _form.asStateFlow()
```

Answer:

> “Only the ViewModel should mutate screen state. The UI can observe public
> read-only StateFlow, but cannot call `.value = ...` on it. This protects the
> single direction of data flow.”

## “Why call `copy`?”

```kotlin
_form.update { it.copy(saving = true) }
```

Answer:

> “`NewPetForm` is immutable. Copy returns a new form with only `saving`
> changed. Immutable state makes updates predictable and Compose-friendly.”

## “What does `runCatching` do?”

Open `NewPetViewModel.save()`.

> “It runs a block and captures an exception as a Result rather than letting the
> coroutine crash. `onSuccess` updates the success state; `onFailure` shows a
> friendly error. In a production app I would also map technical exceptions to
> explicit domain error types.”

---

# Part 7 — One-day interview rescue plan

## First 45 minutes

1. Read Parts 1 and 4 of this file.
2. Memorise: `val` vs `var`, data class, null safety, coroutine, StateFlow,
   MVVM, repository.
3. Open `NewPetScreen.kt` and `NewPetViewModel.kt`; follow Part 3.

## Next 45 minutes

1. Read `docs/01-architecture-and-solid.md`.
2. Say the 30-second project pitch aloud five times.
3. Explain SOLID using the short answers above.

## Next 30 minutes

1. Run a learning test:
   ```bash
   ./gradlew :app:testDebugUnitTest --tests "*KotlinBasicsTest*"
   ```
2. Open `app/src/test/java/com/petconnect/learn/KotlinBasicsTest.kt`.
3. Read one test, predict the result, then run it.

## Night before the interview

Do **not** try to learn every API. Be ready to explain:

- one feature end-to-end: **Add a Pet**;
- one architecture decision: **Room repository behind an interface**;
- one testing decision: **fake repositories + ViewModel tests**;
- all five SOLID one-line answers;
- Kotlin basics in Part 5.

---

# Final quick cheat sheet

```text
val             read-only reference
var             reassignable reference
data class      data holder; has copy(), equals(), etc.
String?         may be null
?.              safely call only if non-null
?:              fallback when null (Elvis operator)
fun             function
suspend         function may pause without blocking
launch          start coroutine, no result expected
Flow            stream of values over time
StateFlow       Flow with a current value
ViewModel       owns UI state and screen actions
Compose         declarative UI: UI is a function of state
Repository      hides storage/network implementation
Hilt            creates and injects dependencies
Room            SQLite abstraction for structured local data
```

You are not expected to be perfect. If you do not know an answer, say:

> “I have not used that deeply yet, but my understanding is ___; I would verify
> the exact API in documentation before implementing it.”

That is much better than guessing.
