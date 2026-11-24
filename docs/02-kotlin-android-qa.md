# Part 2 — Kotlin, Coroutines, Compose & Android Q&A

> Each Kotlin/coroutines answer has a **runnable** test:
> `app/src/test/java/com/petconnect/learn/KotlinBasicsTest.kt` and `CoroutinesFlowTest.kt`.
> Change an assertion, run it, and watch it fail — that's how it sticks.
>
> Run: `./gradlew :app:testDebugUnitTest --tests "*KotlinBasicsTest*"`

---

## A. Kotlin basics (most asked)

| Question | Short answer | Where in the project |
|---|---|---|
| `val` vs `var` | read-only reference vs reassignable. `val` ≠ immutable object | everywhere |
| `val` vs `const val` | `const` = compile-time primitive/String, top-level or in `object` | `Weights` in `Rules.kt` |
| Null safety | `T?`, `?.`, `?:`, smart casts; avoid `!!` | `owner?.name ?: "You"` in `ProfileScreen.kt` |
| `==` vs `===` | structural (equals) vs referential | KotlinBasicsTest Q3 |
| data class | free `equals/hashCode/toString/copy/componentN` | every `UiState` |
| `copy()` | immutable update — how all state changes | `_form.update { it.copy(name = v) }` |
| sealed vs enum | sealed = fixed set of **types** with data; enum = fixed **instances** | `PetDetailUiState`, `DecisionResult` |
| exhaustive `when` | no `else` needed for sealed/enum; compiler catches new cases | `when (val s = state)` in screens |
| `object` | thread-safe lazy singleton | `AvailabilityPrivacyPolicy`, `BreedRule` |
| `companion object` | static-like members/factories | `NewPetForm.MAX_PHOTOS` |
| extension function | add functions to types you don't own; resolved statically | `List<String>.primaryPhoto()` |
| scope functions | `let`/`run`/`apply`/`also`/`with` (see table in test Q7) | `?.let { }` throughout |
| `lazy` vs `lateinit` | `val` computed once vs non-null `var` set later | KotlinBasicsTest Q10 |
| value class | type-safe wrapper with ~zero cost | `PetId`, `OwnerId` in `Pet.kt` |
| `fun interface` | SAM interface → can be created with a lambda | `CompatibilityRule` |
| higher-order fn | takes/returns a function | `edit(field) { it.copy(...) }` in form VMs |
| `inline` | copies lambda body into caller → no allocation, allows `return` from lambda, `reified` generics | KotlinBasicsTest Q8 |
| generics `out`/`in` | producer is covariant, consumer contravariant | KotlinBasicsTest Q12 |
| `internal` | visible inside the same module | — |
| `open` | classes are **final by default** in Kotlin; `open` allows inheritance | — |
| `abstract class` vs `interface` | abstract can hold state + constructor; interfaces can have default methods but no state | `AppModule` (abstract for `@Binds`) |

**Trick question:** *"Is Kotlin `List` immutable?"* → No, it's a **read-only
view**. The underlying object can be a `MutableList` that someone else mutates.
(Test: `read-only collection views`.)

**Trick question:** *"What does `data object` do?"* → A singleton with a nice
`toString()` ("Loading" instead of "Loading@1a2b"). Used in sealed UI states.

---

## B. Coroutines & Flow

| Question | Answer | Test |
|---|---|---|
| What is a coroutine? | A suspendable computation; lightweight (thousands per thread) | — |
| `suspend` | can pause without blocking the thread | Q1 |
| `launch` vs `async` | Job (fire & forget) vs Deferred (await a result) | Q2 |
| Parallel calls | `async {} + async {}` → time = max, not sum | `async runs in parallel` |
| Structured concurrency | children bound to a scope; failure cancels siblings; scope waits | Q3 |
| `SupervisorJob` | a failing child doesn't cancel siblings (viewModelScope uses one) | Q4 |
| Cancellation | cooperative — at suspension points or `isActive` checks | Q5 |
| Dispatchers | `Main` (UI), `IO` (disk/network), `Default` (CPU) | `withContext(Dispatchers.IO)` in data layer |
| `withContext` | switch dispatcher for a block, returns its result | data repositories |
| `viewModelScope` | cancelled in `onCleared()` → no leaks | every ViewModel |
| `GlobalScope` | avoid — not tied to any lifecycle → leaks | — |
| Flow cold vs hot | `flow {}` runs per collector; StateFlow/SharedFlow exist regardless | Q6, Q7 |
| StateFlow | always has a value, replays latest, drops duplicates → **UI state** | Q7 |
| SharedFlow | no initial value, configurable replay → **events** | Q7 |
| LiveData vs StateFlow | StateFlow: Kotlin-native, needs initial value, works in pure domain, rich operators. LiveData: lifecycle-aware out of the box, Android-only | — |
| `combine` | recompute when any source emits | Q8, `NearbyViewModel` |
| `stateIn` | turn a cold flow into a StateFlow owned by a scope | `stateInVm` in `ui/common/UiExtensions.kt` |
| `WhileSubscribed(5000)` | stop upstream 5s after last collector leaves → survives rotation, saves battery | `UiExtensions.kt` |
| `debounce` | wait for quiet period (search box) | Q9 |
| `flatMapLatest` | cancel previous inner flow when a new value arrives | — |
| `collectAsStateWithLifecycle` | stops collecting when app is backgrounded | every screen |

---

## C. Jetpack Compose

| Question | Answer | Where |
|---|---|---|
| What is recomposition? | Compose re-runs composables whose read state changed | — |
| `remember` | keep a value across recompositions | `remember { SnackbarHostState() }` |
| `rememberSaveable` | also survives rotation/process death (Bundle) | selected tab in `MainScreen.kt` |
| State hoisting | state lives in the caller; child gets `value` + `onChange` | `Field(value, onValueChange)` |
| Stateless vs stateful | `MyPetCard` is stateless → reusable & previewable | `ui/profile/MyPetCard.kt` |
| Side effects | `LaunchedEffect(key)` runs a coroutine when key changes | `LaunchedEffect(f.saved) { onDone() }` |
| `DisposableEffect` | setup + cleanup (listeners) | — |
| `derivedStateOf` | compute from state, recompose only when result changes | — |
| Lazy lists & `key` | `items(list, key = { it.id })` keeps item state stable & animations right | `ChatScreen.kt` |
| Modifier order | order matters: `padding().background()` ≠ `background().padding()` | everywhere |
| Stability | immutable data classes with `val`s → Compose can skip recomposition | all UiStates |
| Why no NavController in screens? | Screens get lambdas (`onBack`, `onOpenPet`) → testable & reusable | `PetConnectNavHost.kt` |
| Type-safe navigation | `@Serializable` route classes, `composable<Route.Chat>` | `ui/navigation/Routes.kt` |

---

## D. Android fundamentals

- **Activity lifecycle:** `onCreate → onStart → onResume → (running) → onPause → onStop → onDestroy`.
  Rotation destroys and recreates the Activity → that's why state lives in the ViewModel.
- **Process death:** the OS kills a backgrounded app. ViewModels die too;
  `SavedStateHandle`/`rememberSaveable` survive. Persisted data (DataStore) survives.
- **Single-Activity architecture:** one `MainActivity`, screens are composables.
- **Runtime permissions:** requested with `rememberLauncherForActivityResult(RequestMultiplePermissions())`
  in `PetConnectNavHost.kt`; app degrades gracefully (falls back to a default city).
- **Photo Picker:** `PickVisualMedia` needs **no storage permission** (`PhotoPickerRow.kt`).
- **DataStore vs SharedPreferences:** DataStore is async (coroutines/Flow), transactional,
  no UI-thread I/O. SharedPreferences can block the main thread with `commit()`.
- **Room vs DataStore:** Room = relational data with queries; DataStore = small key-value/proto.
  *Honest answer for this project:* "I used DataStore with JSON for simplicity; for
  production-scale chats I'd move messages to **Room** for paging and queries."
- **Memory leaks:** holding an Activity `Context` in a singleton/ViewModel. Use
  `@ApplicationContext` (see `AppModule.kt`).
- **ANR:** main thread blocked ~5s. Prevent with coroutines on `Dispatchers.IO`.

---

## E. Testing

- **Unit tests** (`app/src/test`): JVM only, fast. Domain is pure Kotlin → easy.
- **Fakes vs mocks:** a fake is a small working implementation (`FakeMatches`);
  a mock (MockK/Mockito) records calls. Fakes are often clearer and less brittle.
- **`runTest`:** virtual time — `delay(1000)` is instant, `currentTime` advances.
- **Deterministic time:** inject a `Clock` (`Fixtures.kt` → `fixedDates`).
- **Test pyramid:** many unit tests → some integration → few UI (Compose) tests.
