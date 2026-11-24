# Part 3 — Low-Level Design (LLD) + 7-Day Practice Plan

---

## 1. How to answer ANY LLD question (the framework)

Say these steps out loud — interviewers grade the *process*, not just the answer.

1. **Clarify requirements** (2–3 min): functional ("users can swipe, match, chat")
   and non-functional ("offline? scale? real-time?"). Write them down.
2. **Identify entities (nouns):** Pet, Owner, Match, Message…
3. **Identify behaviours (verbs):** swipe, match, send, block…
4. **Define relationships:** Owner 1—* Pet, Match *—2 Pet, Match 1—* Message.
5. **Draw the layers / classes:** interfaces first, then implementations.
6. **Apply SOLID & patterns** — and *name* them.
7. **Handle edge cases:** nulls, concurrency, errors, empty states.
8. **Discuss trade-offs & extension:** "if we needed X, I'd add Y without changing Z".

---

## 2. Worked example: "Design the matching feature of a pet dating app"

*(This is literally your project — the best possible answer.)*

**Requirements:** a user swipes on pets; when both pets' owners are interested,
create a match, notify, and open chat. Skips are remembered so pets don't repeat.

**Entities** (`domain/model/`):
```kotlin
@JvmInline value class PetId(val value: String)
data class Pet(val id: PetId, val ownerId: OwnerId, val species: PetSpecies, ...)
enum class InterestDecision { INTERESTED, SKIPPED }
data class Interest(val fromPetId: PetId, val toPetId: PetId, val decision: InterestDecision, ...)
data class Match(val id: String, val petIds: List<PetId>, ...)
```

**Contracts** (`domain/repository/Repositories.kt`):
```kotlin
interface MatchRepository {
    val matches: Flow<List<Match>>
    suspend fun recordInterest(from: PetId, to: PetId, decision: InterestDecision): Match?
}
interface NotificationRepository { suspend fun push(kind, title, body, route) }
```

**Business logic** (`domain/usecase/DecideOnPetUseCase.kt`):
```kotlin
sealed interface DecisionResult {           // every outcome is explicit
    data object NoActivePet : DecisionResult
    data object Recorded : DecisionResult
    data class Matched(val match: Match, ...) : DecisionResult
}
```

**Talking points:**
- *Why a sealed result instead of `Match?`* — three outcomes, not two; the UI's
  `when` must handle each (compiler-enforced).
- *Mutual check* — `recordInterest` returns a `Match` only if the reverse
  interest exists. On a real backend this must be **atomic on the server**
  (a transaction or unique constraint on the sorted pet-id pair) to avoid two
  simultaneous swipes creating two matches. ← senior-level point.
- *Ranking* — `CompatibilityCalculator` with pluggable `CompatibilityRule`
  strategies (Open/Closed).
- *Privacy* — `AvailabilityPrivacyPolicy` is the single gate for who sees dates.
- *Testing* — `DecideOnPetUseCaseTest` with fakes.

**Class diagram (draw this on the whiteboard):**
```
 DiscoverViewModel ──▶ DecideOnPetUseCase ──▶ «interface» MatchRepository
                                 │                      ▲
                                 └──▶ «interface»       │ implements
                                      NotificationRepo  LocalMatchRepository
 NearbyViewModel ──▶ CompatibilityCalculator ──▶ «interface» CompatibilityRule
                                                  ▲   ▲   ▲   ▲   ▲
                                         Breed Age Gender Distance Health
```

---

## 3. Classic LLD questions — skeleton answers

Practise each on paper for 20 minutes, then compare with the skeleton.

### Parking Lot
- Entities: `ParkingLot`, `Floor`, `Spot(type)`, `Vehicle(type)`, `Ticket`, `Payment`.
- `sealed class Vehicle { Bike, Car, Truck }`; `enum SpotType { SMALL, MEDIUM, LARGE }`.
- **Strategy:** `SpotAllocationStrategy` (nearest-first, floor-wise).
- **Strategy:** `PricingStrategy` (hourly, flat, weekend).
- Concurrency: two cars grabbing one spot → lock/atomic `tryReserve()`.

### Elevator system
- `Elevator(state: Idle/MovingUp/MovingDown, currentFloor)`, `Request(floor, direction)`.
- **State pattern** for elevator state; **Strategy** for scheduling (SCAN/nearest).
- Controller dispatches requests to the best elevator.

### Image loader / cache (Android favourite)
- `interface Cache { get(key); put(key, bitmap) }` → `MemoryLruCache`, `DiskCache`.
- **Chain of responsibility:** memory → disk → network (just like
  `RemoteFirstAssistantRepository`, but in the other direction).
- `LruCache` = `LinkedHashMap(accessOrder = true)` + evict eldest.
- Cancel requests when the view is recycled (coroutine Job per target).

### Chat app (you built one!)
- `Message(id, matchId, senderId, kind: TEXT/IMAGE/LOCATION, body, createdAt)` —
  see `MessageKind` and `MessageBubble.kt` (`when` over kind).
- Offline-first: write locally with status `PENDING`, sync, then mark `SENT`.
- Real-time: WebSocket → `Flow<Message>`; repository merges remote + local.
- Pagination: Paging 3 + Room `PagingSource`.

### Notification system
- **Observer:** components subscribe to a `Flow<AppNotification>`.
- **Strategy/Factory** per channel: `PushSender`, `EmailSender`, `InAppSender`.
- User preferences filter — like `SettingsRepository` alerts in this app.

### Rate limiter / Undo (Stack) / LRU cache — be ready to code these in Kotlin.

```kotlin
class LruCache<K, V>(private val capacity: Int) {
    private val map = object : LinkedHashMap<K, V>(capacity, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>) = size > capacity
    }
    operator fun get(key: K): V? = map[key]
    operator fun set(key: K, value: V) { map[key] = value }
}
```

---

## 4. Questions they will ask about THIS project — practise answering

1. Walk me through what happens when the user swipes right. *(`SwipeCard` gesture → `DiscoverViewModel.onDecision` → `DecideOnPetUseCase` → repo → `DecisionResult.Matched` → celebration dialog.)*
2. Why is `CompatibilityCalculator` built from rules? *(OCP, SRP, testability.)*
3. How would you add a backend? *(New `Remote*Repository` implementing the same interfaces; rebind in `AppModule`; ViewModels untouched.)*
4. How do you handle configuration changes? *(ViewModel + StateFlow + `WhileSubscribed(5000)`.)*
5. How is location privacy handled? *(`Geo.approximate` blurs to ~1 km; `AvailabilityPrivacyPolicy`.)*
6. What would you improve? *(Be honest! See section 6.)*
7. How did you test it? *(Domain unit tests with fakes and a fixed `Clock`.)*
8. Why `PetId` instead of `String`? *(Value class → can't swap ids by accident.)*

---

## 5. 7-day plan

| Day | Learn | Do in this repo |
|---|---|---|
| 1 | Kotlin basics | Read & run `KotlinBasicsTest`; break each assertion once |
| 2 | Coroutines & Flow | Run `CoroutinesFlowTest`; then read `NearbyViewModel` and explain every `combine` |
| 3 | Architecture & MVVM | Draw the layer diagram from memory; trace one feature end-to-end |
| 4 | SOLID | For each letter, point to a file without looking at notes |
| 5 | LLD | Do Parking Lot + Image Loader on paper, 30 min each |
| 6 | Compose & Android | Read `PetDetailScreen.kt`; explain every `remember`/`LaunchedEffect` |
| 7 | Mock interview | Answer section 4 out loud, record yourself, re-listen |

---

## 6. Practice exercises (do them yourself — this is what makes it *yours*)

Each is small and touches a real concept. Do them in order.

1. **OCP** — Add a `NeuteredRule` to `Rules.kt` (prefer non-neutered pairs for
   breeding). Register it in `CompatibilityCalculator`. Write a test. Do the
   weights still sum to 1? Fix them.
2. **Sealed classes** — Add `MessageKind.VOICE`. Watch the compiler point out
   every `when` you must update. That's the benefit, felt first-hand.
3. **Flow** — Add a search box to Nearby: a `MutableStateFlow<String>` in
   `NearbyViewModel`, `debounce(300)`, and `combine` it into the feed.
4. **Testing** — Write `NearbyViewModel` tests using fakes (copy the fake style
   from `DecideOnPetUseCaseTest`). Use `Dispatchers.setMain(StandardTestDispatcher())`.
5. **Room** — Move chat messages from DataStore to Room. Only the data layer
   should change. If a ViewModel changes, your boundaries leaked.
6. **Error handling** — Introduce `sealed interface Result<out T> { Success; Failure }`
   for `PetRepository.createPet` and show an error state on failure.

Being able to say *"I did exercise 5 and no ViewModel changed"* is a very
strong interview moment.

---

## 7. Honest limitations (say them before they find them)

- Persistence is DataStore + JSON, not Room — fine for a demo, not for large chat history.
- Matching reciprocity is simulated (`simulateReciprocalInterest`) because there's no backend.
- ViewModels and Compose UI aren't unit/UI tested yet — domain is.
- No pagination from a server; the feed paginates in memory.

Naming your own trade-offs signals seniority more than pretending there are none.
