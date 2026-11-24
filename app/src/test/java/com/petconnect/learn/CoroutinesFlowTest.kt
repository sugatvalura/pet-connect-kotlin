package com.petconnect.learn

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * COROUTINES & FLOW — the interview questions, as runnable code.
 * `runTest` uses VIRTUAL time: delay(1000) finishes instantly, but currentTime advances.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutinesFlowTest {

    // Q1: What is a suspend function?
    // A function that can PAUSE without blocking the thread, and resume later.
    private suspend fun fetchPet(): String { delay(1000); return "Bruno" }

    // Q2: launch vs async?
    // launch → fire-and-forget, returns Job. async → returns Deferred<T>, call await() for the result.
    @Test fun `async runs in parallel - total time is the max, not the sum`() = runTest {
        val a = async { fetchPet() }
        val b = async { fetchPet() }
        assertEquals("Bruno+Bruno", "${a.await()}+${b.await()}")
        assertEquals(1000, currentTime) // parallel: 1s, not 2s
    }

    @Test fun `sequential suspend calls add up`() = runTest {
        fetchPet(); fetchPet()
        assertEquals(2000, currentTime)
    }

    // Q3: Structured concurrency?
    // Children live inside a scope. Parent waits for all children; if one FAILS, siblings are cancelled.
    // That's why the app uses viewModelScope: leaving the screen cancels all its work → no leaks.
    @Test fun `failure in one child cancels its siblings`() = runTest {
        var siblingFinished = false
        val result = runCatching {
            coroutineScope {
                launch { delay(5000); siblingFinished = true }
                launch { delay(100); error("network down") }
            }
        }
        assertTrue(result.isFailure)
        assertFalse(siblingFinished)
    }

    // Q4: SupervisorJob — when one child failing should NOT kill the others (e.g. viewModelScope uses one).
    @Test fun `supervisor isolates failures`() = runTest {
        var otherFinished = false
        val handler = CoroutineExceptionHandler { _, _ -> /* swallow for demo */ }
        val scope = kotlinx.coroutines.CoroutineScope(coroutineContext + SupervisorJob() + handler)
        scope.launch { error("boom") }
        scope.launch { delay(100); otherFinished = true }
        advanceUntilIdle()
        assertTrue(otherFinished)
    }

    // Q5: Cancellation is cooperative. Suspend points (delay, yield) check it; tight CPU loops must check isActive.
    @Test fun `cancellation is cooperative`() = runTest {
        var iterations = 0
        val job = launch {
            while (isActive) { iterations++; yield() }
        }
        repeat(3) { yield() }
        job.cancelAndJoin()
        assertTrue(iterations in 1..10)
    }

    @Test fun `timeouts`() = runTest {
        assertNull(withTimeoutOrNull(500) { fetchPet() }) // 1000ms > 500ms
    }

    // Q6: Flow — cold stream. Nothing runs until someone COLLECTS; each collector gets its own run.
    @Test fun `flow is cold`() = runTest {
        var starts = 0
        val pets = flow { starts++; emit("Bruno"); emit("Luna") }
        assertEquals(0, starts)
        assertEquals(listOf("Bruno", "Luna"), pets.toList())
        pets.toList()
        assertEquals(2, starts) // ran once per collector
    }

    @Test fun `flow operators`() = runTest {
        val result = flowOf(1, 2, 3, 4).filter { it % 2 == 0 }.map { it * 10 }.toList()
        assertEquals(listOf(20, 40), result)
    }

    // Q7: StateFlow vs SharedFlow?
    // StateFlow: HOT, always has a value, replays the LATEST to new collectors, skips duplicates. → UI state.
    // SharedFlow: HOT, no initial value, configurable replay. → one-off events (snackbar, navigation).
    @Test fun `stateflow holds the latest value and conflates duplicates`() = runTest {
        val state = MutableStateFlow(0)
        state.value = 1
        state.value = 1 // equal → not re-emitted
        assertEquals(1, state.first()) // new collector immediately gets current value
        // Atomic read-modify-write; avoid `state.value = state.value + 1` in concurrent code.
        state.update { it + 1 }
        assertEquals(2, state.value)
    }

    @Test fun `sharedflow without replay misses events emitted before collection`() = runTest {
        val events = MutableSharedFlow<String>()
        events.tryEmit("lost") // nobody listening, no buffer → dropped
        val received = mutableListOf<String>()
        val job = launch { events.collect { received += it } }
        advanceUntilIdle()
        events.emit("delivered")
        advanceUntilIdle()
        job.cancel()
        assertEquals(listOf("delivered"), received)
    }

    // Q8: combine — exactly how NearbyViewModel merges pets + location + filters into one UiState.
    @Test fun `combine recomputes when any source changes`() = runTest {
        val pets = MutableStateFlow(listOf("Bruno", "Luna", "Max"))
        val query = MutableStateFlow("")
        val visible = combine(pets, query) { list, q -> list.filter { it.contains(q, ignoreCase = true) } }

        assertEquals(3, visible.first().size)
        query.value = "lu"
        assertEquals(listOf("Luna"), visible.first())
    }

    // Q9: debounce — search boxes: wait until the user stops typing.
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    @Test fun `debounce keeps only the last value of a burst`() = runTest {
        val typed = flow {
            emit("b"); delay(100); emit("br"); delay(100); emit("bru")
            delay(600); emit("bruno")
        }
        assertEquals(listOf("bru", "bruno"), typed.debounce(300).toList())
    }
}
