# 04 — Room migration & ViewModel testing

Two upgrades, each a strong interview story.

---

## Part A — Chat moved from DataStore JSON to Room

### The story (say this in the interview)

> "Chat messages were stored as one JSON blob in DataStore. Every new message
> rewrote the whole list, and filtering one conversation meant loading all of
> them. I moved chat to Room. Because ViewModels depend on the `ChatRepository`
> **interface**, the swap was **one line in the DI module**. No ViewModel,
> use case or screen changed. I also added a one-time migration so existing
> users keep their messages."

### Files

| Layer | File | Role |
|---|---|---|
| Domain | `domain/repository/Repositories.kt` → `ChatRepository` | Contract. **Unchanged.** |
| Data | `data/db/ChatEntities.kt` | `@Entity` tables + index |
| Data | `data/db/ChatDao.kt` | SQL queries, `Flow` for observation |
| Data | `data/db/PetConnectDatabase.kt` | `@Database`, version, entity list |
| Data | `data/db/ChatMappers.kt` | Entity ↔ domain conversion |
| Data | `data/repository/RoomChatRepository.kt` | Implements `ChatRepository` with the DAO |
| DI | `di/AppModule.kt` → `DatabaseModule` + one `@Binds` line | Wiring |

### DataStore vs Room: when to use which

| | DataStore (Preferences) | Room |
|---|---|---|
| Good for | Small key-value settings (theme, active pet, filters) | Lists that grow, need queries, relations |
| Query | Load everything, filter in Kotlin | SQL `WHERE`/`ORDER BY`, uses indexes |
| Write | Rewrites the whole value | Inserts one row |
| Observe | `Flow` | `Flow` from DAO, re-emits when the table changes |
| Schema changes | Your problem | Versioned `Migration`s, schema exported to `app/schemas/` |

### Why entities ≠ domain models

`MessageEntity` has Room annotations; `Message` (domain) has none. The
**mapper** sits between them. If the DB column changes, only the entity and
mapper change. This is the **Single Responsibility Principle** at the layer level,
and it keeps the domain free of framework code.

### Likely questions

- **How does Room know to re-emit the Flow?** It tracks which tables a query
  reads. Any write to those tables re-runs the query and emits again.
- **Do I need `withContext(Dispatchers.IO)` for DAO calls?** No. Room
  `suspend` and `Flow` functions run on Room's own background executor.
- **What happens if I add a column and don't bump the version?** The app
  crashes on open, because Room's identity hash doesn't match. Bump `version` and add
  `Migration(1, 2) { db -> db.execSQL("ALTER TABLE messages ADD COLUMN ...") }`.
- **What does `fallbackToDestructiveMigration()` do?** It wipes the DB on
  version mismatch. That's fine for caches but bad for user data, so we don't use it.
- **Why the `Mutex` in `migrateLegacyOnce`?** Two screens can start collecting
  at the same time. The mutex plus a double-checked flag makes the copy run
  exactly once. This is the coroutine-friendly version of `synchronized`.
- **Why `OnConflictStrategy.IGNORE` for blocking?** Blocking twice should be
  a harmless no-op. The primary key already guarantees uniqueness.

---

## Part B — ViewModel unit tests

### Files

| File | Teaches |
|---|---|
| `test/.../testing/MainDispatcherRule.kt` | Replacing `Dispatchers.Main` on the JVM |
| `test/.../testing/Fakes.kt` | In-memory fakes backed by `MutableStateFlow` |
| `test/.../testing/TurbineExt.kt` | Skipping intermediate emissions safely |
| `test/.../ui/AuthViewModelTest.kt` | Form ViewModel: intents → state |
| `test/.../ui/ChatsViewModelTest.kt` | Reactive ViewModel with **Turbine** |
| `test/.../data/ChatMappersTest.kt` | Pure mapper tests (no Room, no device) |

### Key ideas

1. **Why `MainDispatcherRule`?** `viewModelScope` uses `Dispatchers.Main`.
   The JVM has no Android Looper, so without the rule you get
   *"Module with the Main dispatcher is missing"*.
2. **Why `UnconfinedTestDispatcher`?** Launched coroutines start immediately,
   so `vm.submit()` has finished by the next line. `StandardTestDispatcher`
   queues them until you call `advanceUntilIdle()`. Use that when you need to
   check an in-between state such as `submitting == true`.
3. **Fakes vs mocks.** A fake is a small working implementation. Tests don't
   break when internal call order changes, and they read like normal code. Use mocks
   (MockK) only to verify an interaction you can't observe any other way.
4. **Turbine.** `flow.test { awaitItem() }` collects in the background and
   lets you take emissions one at a time. It fails if unconsumed events are
   left over, which catches accidental extra emissions.
5. **StateFlow is conflated.** It holds only the latest value. With instant fakes,
   `Loading` may be replaced before Turbine sees it. That's why we check the
   initial value directly and use `awaitItemMatching { ... }` for the loaded state.
6. **`WhileSubscribed(5_000)`.** Upstream is collected only while someone
   observes. The 5 s grace period survives screen rotation without restarting DB queries.

### Exercise

Write `ChatViewModelTest`. You need a `SavedStateHandle` holding the route,
plus the fakes. Assert that `sendText()` with a blank draft sends nothing,
and that `reportAndBlock()` sets `closed = true`.
