# Part 1 — Architecture & SOLID (with proof from this project)

> How to use this: read a section, open the file it points to, then explain it
> **out loud** in your own words. Interviewers want "in my project I did X because Y".

---

## 1. The 30-second project pitch (memorise this)

> "PetConnect is a native Android app in Kotlin where pet owners find nearby
> pets, swipe to match, chat, and adopt/sell. I built it with **Clean
> Architecture + MVVM**: Jetpack Compose UI, ViewModels exposing `StateFlow`,
> a pure-Kotlin domain layer with use cases, and a data layer behind repository
> interfaces. Dependencies are wired with **Hilt**, async work uses
> **coroutines and Flow**, and data is persisted with **DataStore**. The domain
> layer has no Android imports, so it's unit tested with plain JUnit and fakes."

---

## 2. The layers

```
 ┌──────────── ui/ ─────────────┐   Compose screens + ViewModels
 │ Screen ──events──▶ ViewModel │   Screen only draws state & sends events
 │        ◀──StateFlow──        │
 └──────────────┬───────────────┘
                │ calls
 ┌──────────── domain/ ─────────┐   PURE Kotlin. No Android imports.
 │ UseCases · Models · Rules    │   Business rules live here.
 │ Repository INTERFACES        │
 └──────────────▲───────────────┘
                │ implements
 ┌──────────── data/ ───────────┐   DataStore, GPS, files, network
 │ Local*Repository, JsonStore  │
 └──────────────────────────────┘
            di/AppModule.kt  ← Hilt glues interface → implementation
```

**The dependency rule:** arrows point *inward*. `domain` knows nothing about
`data` or `ui`. Proof: `domain/repository/Repositories.kt` declares interfaces;
`data/repository/*` implements them; `di/AppModule.kt` binds them with `@Binds`.

**Interview Q: "Why bother with layers?"**
1. **Testability** — domain tests run on the JVM in milliseconds (see `app/src/test`).
2. **Replaceability** — swap DataStore for Retrofit by writing new repository
   classes; no ViewModel changes.
3. **Separation** — UI bugs and business bugs live in different places.

---

## 3. MVVM / Unidirectional Data Flow

Open `ui/nearby/NearbyViewModel.kt` and `ui/nearby/NearbyScreen.kt`.

```
User taps filter → screen calls viewModel.setFilters(...)      (EVENT goes UP)
ViewModel updates flows → combine(...) builds NearbyUiState     (STATE)
Screen collects state with collectAsStateWithLifecycle()        (STATE comes DOWN)
```

Pattern used in every ViewModel:

```kotlin
private val _form = MutableStateFlow(NewPetForm())   // private, mutable
val form: StateFlow<NewPetForm> = _form.asStateFlow() // public, read-only
```

**Q: Why private Mutable + public read-only?** Encapsulation — only the
ViewModel can change state, so there's a single source of truth.

**Q: Why does the ViewModel survive rotation?** It's scoped to the
`ViewModelStoreOwner` (the nav back-stack entry), not the Activity instance.
`viewModelScope` is cancelled only when the screen is truly gone.

**Q: MVVM vs MVI?** MVVM: ViewModel exposes state + functions. MVI: one
immutable state + a single `onEvent(Intent)` entry point. This app is MVVM with
MVI-style **single immutable UiState** data classes (e.g. `NearbyUiState`).

**Q: How are one-off events (navigate back after save) handled?**
Here: a state flag (`saved = true`) observed with `LaunchedEffect(f.saved)`.
Alternative: a `Channel`/`SharedFlow` of events. Know both and the trade-off
(state flags survive rotation; channels must be consumed exactly once).

---

## 4. SOLID — each principle with a real file

### S — Single Responsibility
*"A class should have one reason to change."*

- `domain/usecase/compatibility/Rules.kt`: `BreedRule` knows only breed,
  `DistanceRule` only distance. Changing the distance formula touches one class.
- `domain/usecase/AvailabilityPrivacyPolicy.kt`: the **only** place that decides
  who may see breeding dates. Screens never re-implement the rule.
- `domain/usecase/Validation.kt`: validation lives outside the ViewModel.

**Counter-example to mention:** a 600-line ViewModel that validates, calls the
API, formats dates and navigates — four reasons to change.

### O — Open/Closed
*"Open for extension, closed for modification."*

`CompatibilityCalculator` takes a `List<CompatibilityRule>`. To add a
"Temperament" factor you write a **new** class; you don't edit the calculator.
**Proof it works:** the test `new rule plugs in without modifying the calculator`
in `CompatibilityCalculatorTest.kt`.

Before the refactor it was one class with a big function per factor — every
new factor meant editing it. Great story to tell: *"I refactored it to the
Strategy pattern for OCP."*

### L — Liskov Substitution
*"Subtypes must be usable wherever the parent is expected, without surprises."*

- Every `CompatibilityRule` obeys the same contract: score 0..1, `null` = not
  applicable. The calculator treats all rules identically.
- `FakeMatches` in `DecideOnPetUseCaseTest.kt` substitutes for the real
  `MatchRepository` and the use case can't tell the difference.

**Classic violation:** `Square extends Rectangle` — setting width on a square
also changes height, breaking code that expected a rectangle.

### I — Interface Segregation
*"Don't force clients to depend on methods they don't use."*

`Repositories.kt` has **many small interfaces** (`ImageRepository` has one
method, `SettingsRepository` two) instead of one giant `AppRepository`.
`ChatViewModel` depends on `ChatRepository`, not on listings or auth.

### D — Dependency Inversion
*"Depend on abstractions, not concretions."*

`DecideOnPetUseCase(matches: MatchRepository, notifications: NotificationRepository)`
takes **interfaces**. Hilt injects the real ones in the app; tests inject fakes.
`Dates` takes a `Clock`, so tests freeze time (`Fixtures.kt` → `fixedDates`).

**DI vs DIP (common trick question):** DIP is the *principle* (depend on
abstractions). DI is a *technique* (pass dependencies in from outside). Hilt is
a DI *framework*.

---

## 5. Design patterns you can point to

| Pattern | Where | One-line explanation |
|---|---|---|
| Repository | `domain/repository/Repositories.kt` | Hides where data comes from |
| Strategy | `usecase/compatibility/Rules.kt` | Swappable scoring algorithms |
| Use Case / Interactor | `DecideOnPetUseCase.kt` | One business action per class |
| Observer | `StateFlow` everywhere | UI reacts to state changes |
| Singleton | `@Singleton` repos, Kotlin `object` | One shared instance |
| Factory | `companion object` factories, Hilt `@Provides` | Centralised creation |
| Fallback / Chain | `RemoteFirstAssistantRepository` | Try remote, fall back to local |
| Sealed state | `PetDetailUiState`, `DecisionResult` | Make illegal states unrepresentable |

---

## 6. Hilt in 6 lines

- `@HiltAndroidApp` on `PetConnectApp` → creates the component graph.
- `@AndroidEntryPoint` on `MainActivity` → can receive injections.
- `@HiltViewModel` + `@Inject constructor(...)` → ViewModel gets dependencies.
- `@Binds` (interface → impl, abstract, cheaper) vs `@Provides` (you write the
  creation code, e.g. for DataStore/OkHttp you don't own).
- `@Singleton` → one instance for the app's lifetime.
- `SavedStateHandle` → ViewModel reads nav arguments (`toRoute<Route.Chat>()`).
