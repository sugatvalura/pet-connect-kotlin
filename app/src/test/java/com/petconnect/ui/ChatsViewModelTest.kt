package com.petconnect.ui

import app.cash.turbine.test
import com.petconnect.domain.ME
import com.petconnect.domain.THEM
import com.petconnect.domain.fixedDates
import com.petconnect.domain.model.Match
import com.petconnect.domain.model.PetId
import com.petconnect.domain.pet
import com.petconnect.domain.usecase.ObserveViewerUseCase
import com.petconnect.testing.FakeAuthRepository
import com.petconnect.testing.FakeChatRepository
import com.petconnect.testing.FakeMatchRepository
import com.petconnect.testing.FakePetRepository
import com.petconnect.testing.FakePreferencesRepository
import com.petconnect.testing.MainDispatcherRule
import com.petconnect.testing.awaitItemMatching
import com.petconnect.ui.chats.ChatsViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Testing a REACTIVE ViewModel with Turbine.
 *
 * `state.test { ... }` subscribes to the StateFlow; `awaitItem()` suspends
 * until the next emission. We then mutate a fake repository and assert the
 * ViewModel re-emits — proving the whole Flow pipeline is wired correctly.
 */
class ChatsViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val myPet = pet(id = "mine", owner = ME)
    private val theirPet = pet(id = "theirs", owner = THEM)
    private val strangerPet = pet(id = "stranger", owner = THEM)

    private val pets = FakePetRepository(listOf(myPet, theirPet, strangerPet))
    private val chat = FakeChatRepository()
    private val matches = FakeMatchRepository(
        listOf(
            Match("old", listOf(myPet.id, theirPet.id), createdAt = "2026-11-01T00:00:00Z"),
            Match("new", listOf(myPet.id, strangerPet.id), createdAt = "2026-11-10T00:00:00Z"),
            // Not involving my pet → must never appear in my inbox.
            Match("other", listOf(PetId("x"), PetId("y")), createdAt = "2026-11-15T00:00:00Z"),
        ),
    )

    private fun viewModel() = ChatsViewModel(
        observeViewer = ObserveViewerUseCase(FakeAuthRepository(signedInAs = ME), pets, FakePreferencesRepository()),
        matches = matches,
        pets = pets,
        chat = chat,
        dates = fixedDates,
    )

    @Test fun `starts loading, then lists only my conversations, newest first`() = runTest {
        val vm = viewModel()
        // WhileSubscribed: nothing is collected until someone subscribes,
        // so before the UI shows up the state is the initial Loading value.
        assertTrue("initial state is Loading", vm.state.value.loading)

        vm.state.test {
            // StateFlow is conflated: with instant fakes we may never observe
            // Loading inside test{} — so wait for the state we care about.
            val loaded = awaitItemMatching { !it.loading }
            assertEquals(listOf("new", "old"), loaded.conversations.map { it.match.id })
            assertEquals(myPet, loaded.conversations.first().myPet)
        }
    }

    @Test fun `blocking an owner removes their conversations reactively`() = runTest {
        viewModel().state.test {
            awaitItemMatching { it.conversations.size == 2 }

            chat.blockOwner(THEM) // simulate the user tapping "Report & block"

            val afterBlock = awaitItem()
            assertTrue(afterBlock.conversations.isEmpty())
        }
    }

    @Test fun `a new match appears without reloading the screen`() = runTest {
        matches.matches.value = emptyList()

        viewModel().state.test {
            awaitItemMatching { !it.loading && it.conversations.isEmpty() }

            matches.matches.value = listOf(Match("fresh", listOf(myPet.id, theirPet.id), "2026-11-19T00:00:00Z"))

            assertEquals(listOf("fresh"), awaitItem().conversations.map { it.match.id })
        }
    }
}
