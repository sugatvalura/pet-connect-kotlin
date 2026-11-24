# PetConnect — Interview Prep Guide

Read in order. Every concept points to real code in this repo.

0. [Kotlin From 0%](00-kotlin-from-zero.md) — beginner guide: Kotlin basics, project folders, Add Pet feature walkthrough, interview answers to memorise
1. [Architecture & SOLID](01-architecture-and-solid.md) — layers, MVVM, each SOLID letter with a file, patterns, Hilt
2. [Kotlin, Coroutines, Compose & Android Q&A](02-kotlin-android-qa.md) — the most-asked questions, with answers
3. [LLD + 7-day plan](03-lld-and-practice-plan.md) — LLD framework, worked example, classic questions, exercises
4. [Room & ViewModel testing](04-room-and-viewmodel-testing.md) — DataStore→Room swap via DIP, DAO/Flow, migrations, Turbine, fakes

## Runnable learning files

| File | What it teaches |
|---|---|
| `app/src/test/.../learn/KotlinBasicsTest.kt` | 12 Kotlin interview questions as tests |
| `app/src/test/.../learn/CoroutinesFlowTest.kt` | 12 coroutines/Flow questions as tests |
| `app/src/test/.../domain/CompatibilityCalculatorTest.kt` | Open/Closed principle, proven |
| `app/src/test/.../domain/DecideOnPetUseCaseTest.kt` | Dependency Inversion + fakes |
| `app/src/test/.../ui/AuthViewModelTest.kt` | Testing a form ViewModel (intents → state) |
| `app/src/test/.../ui/ChatsViewModelTest.kt` | Testing reactive StateFlow with Turbine |
| `app/src/test/.../data/ChatMappersTest.kt` | Room entity ↔ domain mapping |

```bash
./gradlew :app:testDebugUnitTest                              # all 64 tests
./gradlew :app:testDebugUnitTest --tests "*KotlinBasicsTest*"  # just one file
```

**Best way to learn:** change an assertion so it fails, run it, read the error,
fix it. Then explain the concept out loud as if to an interviewer.
