package com.petconnect.learn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * KOTLIN BASICS — every test answers one common interview question.
 * Run with: ./gradlew :app:testDebugUnitTest --tests "*KotlinBasicsTest*"
 * Break an assertion on purpose to see what Kotlin really does.
 */
class KotlinBasicsTest {

    // Q1: val vs var?
    // val = read-only reference (like Java final). var = reassignable.
    // NOTE: val does NOT mean the object is immutable — see the list below.
    @Test fun `val is a read-only reference, not an immutable object`() {
        val names = mutableListOf("Bruno")
        names += "Luna" // allowed: we mutate the object, not the reference
        assertEquals(2, names.size)
        // names = mutableListOf() // ❌ compile error: val cannot be reassigned
    }

    // Q2: How does Kotlin prevent NullPointerException?
    // Types are non-null by default. `String?` may be null. The compiler forces you to handle it.
    @Test fun `null safety operators`() {
        val breed: String? = null
        assertNull(breed?.length)                        // ?.  safe call → null instead of crash
        assertEquals(0, breed?.length ?: 0)              // ?:  Elvis → default value
        val known: String? = "Beagle"
        if (known != null) assertEquals(6, known.length) // smart cast: compiler knows it's non-null here
        // breed!!.length  // !! = "I promise it's not null" → throws NPE if wrong. Avoid.
    }

    // Q3: What does a data class give you for free?
    // equals(), hashCode(), toString(), copy(), componentN() (destructuring).
    data class PetDto(val name: String, val age: Int)

    @Test fun `data class equality, copy and destructuring`() {
        val a = PetDto("Bruno", 3)
        val b = PetDto("Bruno", 3)
        assertEquals(a, b)              // structural equality (==) compares fields
        assertNotSame(a, b)             // referential equality (===) — different objects
        val older = a.copy(age = 4)     // immutable update — the pattern every UiState here uses
        assertEquals(3, a.age)
        val (name, age) = older         // destructuring via component1()/component2()
        assertEquals("Bruno" to 4, name to age)
    }

    // Q4: sealed class/interface vs enum?
    // enum = fixed set of SINGLE instances. sealed = fixed set of TYPES, each can carry different data.
    // `when` over a sealed type is exhaustive → no `else` needed, compiler catches missing cases.
    sealed interface LoadState {
        data object Loading : LoadState
        data class Ready(val count: Int) : LoadState
        data class Failed(val reason: String) : LoadState
    }

    private fun render(s: LoadState): String = when (s) { // no else branch!
        LoadState.Loading -> "spinner"
        is LoadState.Ready -> "${s.count} pets" // smart cast to Ready
        is LoadState.Failed -> "error: ${s.reason}"
    }

    @Test fun `sealed types give exhaustive when`() {
        assertEquals("3 pets", render(LoadState.Ready(3)))
        assertEquals("spinner", render(LoadState.Loading))
    }

    // Q5: object vs companion object?
    // object = singleton (thread-safe, lazily created). companion = static-like members inside a class.
    object Counter { var hits = 0 }
    class Pet private constructor(val name: String) {
        companion object { fun named(n: String) = Pet(n.trim()) } // factory method
    }

    @Test fun `object is a singleton, companion is a factory`() {
        assertSame(Counter, Counter)
        assertEquals("Bruno", Pet.named("  Bruno ").name)
    }

    // Q6: Extension functions — how and why?
    // Add functions to classes you don't own. Resolved statically (no real inheritance).
    private fun String.initials(): String = split(" ").filter { it.isNotBlank() }.joinToString("") { it.first().uppercase() }

    @Test fun `extension functions`() {
        assertEquals("AS", "Asha Sharma".initials())
    }

    // Q7: Scope functions — let, run, apply, also, with?
    //  let   → it, returns lambda result   (null checks: x?.let { })
    //  apply → this, returns the object    (configure/builder)
    //  also  → it, returns the object      (side effects: logging)
    //  run   → this, returns lambda result
    //  with  → this, returns lambda result (not an extension)
    @Test fun `scope functions`() {
        val sb = StringBuilder().apply { append("Pet"); append("Connect") }
        assertEquals("PetConnect", sb.toString())
        val len = "Bruno".let { it.length }
        assertEquals(5, len)
        val log = mutableListOf<String>()
        val n = 42.also { log += "got $it" }
        assertEquals(42, n); assertEquals(listOf("got 42"), log)
    }

    // Q8: Higher-order functions & lambdas.
    // A function that takes or returns a function. `inline` avoids the lambda object allocation.
    private inline fun <T> measure(block: () -> T): Pair<T, Boolean> = block() to true

    @Test fun `higher order functions`() {
        val ages = listOf(1, 4, 7, 2)
        assertEquals(listOf(4, 7), ages.filter { it > 3 })
        assertEquals(14, ages.sum())
        assertEquals(14, ages.fold(0) { acc, x -> acc + x })
        assertEquals("ok" to true, measure { "ok" })
    }

    // Q9: List vs MutableList — and why expose read-only types?
    // Same reason ViewModels expose StateFlow but keep MutableStateFlow private: encapsulation.
    @Test fun `read-only collection views`() {
        val backing = mutableListOf(1, 2)
        val exposed: List<Int> = backing
        backing += 3
        assertEquals(3, exposed.size) // read-only VIEW, not a copy!
        assertTrue(exposed !is MutableList<*> || exposed === backing)
    }

    // Q10: lazy vs lateinit?
    // lazy  → val, computed on first access, thread-safe by default.
    // lateinit → var, non-null, assigned later (DI, test setup). Accessing early throws.
    private val expensive: Int by lazy { computeCount++; 99 }
    private var computeCount = 0
    private lateinit var injected: String

    @Test fun `lazy and lateinit`() {
        assertEquals(0, computeCount)
        expensive; expensive
        assertEquals(1, computeCount) // computed exactly once
        assertFalse(::injected.isInitialized)
        injected = "ready"
        assertTrue(::injected.isInitialized)
    }

    // Q11: Value classes — why PetId instead of String?
    // Type safety with zero runtime cost: you can't pass an OwnerId where a PetId is expected.
    @JvmInline value class PetId(val value: String)
    @JvmInline value class OwnerId(val value: String)
    private fun loadPet(id: PetId) = "pet:${id.value}"

    @Test fun `value classes prevent mixing up ids`() {
        assertEquals("pet:42", loadPet(PetId("42")))
        // loadPet(OwnerId("42")) // ❌ compile error — the whole point
    }

    // Q12: Generics & variance. `out` = producer (covariant), `in` = consumer (contravariant).
    // A consumer: it only TAKES T in, never returns it → safe to declare `in`.
    fun interface Printer<in T> { fun print(value: T): String }

    @Test fun `variance`() {
        val dogs: List<String> = listOf("Bruno")
        val anything: List<Any> = dogs // OK because List<out E> (a producer)
        assertEquals(1, anything.size)

        val printsAnything: Printer<Any> = Printer { "<$it>" }
        val printsStrings: Printer<String> = printsAnything // OK because Printer<in T>
        assertEquals("<Bruno>", printsStrings.print("Bruno"))
        // Gotcha: java.util.Comparator is a JAVA interface, so Kotlin can't see
        // its variance — `val c: Comparator<String> = Comparator<Any>{…}` won't compile.
    }
}
