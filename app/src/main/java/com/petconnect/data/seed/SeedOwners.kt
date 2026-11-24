package com.petconnect.data.seed

import com.petconnect.domain.model.Owner
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.usecase.DemoAccount

internal fun photo(id: String, w: Int = 900) =
    "https://images.unsplash.com/$id?auto=format&fit=crop&w=$w&q=70"

/** Demo owners. Read-only; a real backend replaces this file. */
object SeedOwners {
    private fun owner(id: String, name: String, avatar: String, city: String, verified: Boolean, bio: String, joined: String) =
        Owner(OwnerId(id), name, photo(avatar, 300), city, verified, bio, joined)

    val all: List<Owner> = listOf(
        owner(DemoAccount.OWNER_ID.value, "Sugat", "photo-1500648767791-00dcc994a43e", "Delhi", true,
            "Weekend hiker, full-time dog person.", "2025-02-11T09:00:00Z"),
        owner("owner-neha", "Neha", "photo-1494790108377-be9c29b29330", "Hauz Khas, Delhi", true,
            "Golden retriever mum. Always up for a park meetup.", "2025-01-04T09:00:00Z"),
        owner("owner-arjun", "Arjun", "photo-1507003211169-0a1dd7228f2d", "Saket, Delhi", false,
            "Two indies and a very loud parrot.", "2025-03-22T09:00:00Z"),
        owner("owner-fatima", "Fatima", "photo-1438761681033-6461ffad8d80", "Vasant Kunj, Delhi", true,
            "Cat behaviourist. Foster home for rescues.", "2024-11-30T09:00:00Z"),
        owner("owner-rohan", "Rohan", "photo-1472099645785-5658abf4ff4e", "Green Park, Delhi", true,
            "Beagle chaos coordinator.", "2025-05-18T09:00:00Z"),
        owner("owner-priya", "Priya", "photo-1534528741775-53994a69daeb", "Malviya Nagar, Delhi", false,
            "Adopted, never shopped. 🐾", "2025-06-02T09:00:00Z"),
        owner("owner-kabir", "Kabir", "photo-1500648767791-00dcc994a43e", "Dwarka, Delhi", true,
            "Two dogs, full calendar of park runs.", "2025-04-12T09:00:00Z"),
        owner("owner-anjali", "Anjali", "photo-1544005313-94ddf0286df2", "Greater Kailash, Delhi", true,
            "Rescue cat lady. Will show you photos.", "2025-02-26T09:00:00Z"),
    )
}
