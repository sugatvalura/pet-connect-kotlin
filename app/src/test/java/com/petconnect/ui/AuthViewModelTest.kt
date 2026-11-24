package com.petconnect.ui

import com.petconnect.domain.fixedDates
import com.petconnect.domain.usecase.FormValidator
import com.petconnect.testing.FakeAuthRepository
import com.petconnect.testing.MainDispatcherRule
import com.petconnect.ui.auth.AuthViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Testing a "form" ViewModel: user intents in → state out.
 * No Android, no emulator — runs on the plain JVM in milliseconds.
 */
class AuthViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private val vm = AuthViewModel(auth, FormValidator(fixedDates))

    @Test fun `invalid input shows errors and never calls signIn`() {
        vm.onEmail("not-an-email")
        vm.onPassword("12")

        vm.submit(requireName = true)

        val errors = vm.state.value.errors
        assertEquals(setOf("email", "password", "name"), errors.keys)
        assertTrue("repository must not be touched", auth.signInCalls.isEmpty())
    }

    @Test fun `typing in a field clears only that field's error`() {
        vm.submit(requireName = true) // everything empty → 3 errors

        vm.onEmail("a@b.co")

        assertEquals(setOf("password", "name"), vm.state.value.errors.keys)
    }

    @Test fun `valid login signs in and finishes submitting`() {
        vm.onEmail("me@pets.com")
        vm.onPassword("secret")

        vm.submit(requireName = false)

        // UnconfinedTestDispatcher runs the launched coroutine immediately.
        assertEquals(listOf("me@pets.com" to null), auth.signInCalls)
        assertFalse(vm.state.value.submitting)
        assertTrue(vm.state.value.errors.isEmpty())
    }

    @Test fun `signup passes the name through`() {
        vm.onName("Asha")
        vm.onEmail("asha@pets.com")
        vm.onPassword("secret")

        vm.submit(requireName = true)

        assertEquals(listOf("asha@pets.com" to "Asha"), auth.signInCalls)
    }
}
