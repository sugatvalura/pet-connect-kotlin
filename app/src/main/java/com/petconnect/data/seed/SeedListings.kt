package com.petconnect.data.seed

import com.petconnect.domain.model.DemoOffsetKm
import com.petconnect.domain.model.Listing
import com.petconnect.domain.model.ListingKind
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.model.PetGender
import com.petconnect.domain.model.PetLocation
import com.petconnect.domain.model.PetSpecies
import com.petconnect.domain.model.VaccinationStatus

object SeedListings {
    private val DELHI = PetLocation(28.6139, 77.209, "New Delhi")

    val all: List<Listing> = listOf(
        Listing(
            id = "listing-1", ownerId = OwnerId("owner-fatima"), kind = ListingKind.ADOPT,
            title = "Three rescue kittens looking for homes",
            photos = listOf(photo("photo-1537151625747-768eb6cf92b2"), photo("photo-1526336024174-e58f5cdd8e13")),
            species = PetSpecies.CAT, breed = "Indian Shorthair", gender = PetGender.FEMALE,
            dateOfBirth = "2026-06-20", location = DELHI, price = null,
            description = "Rescued at three weeks, hand-raised and litter trained. Dewormed and first shots done. Home visit required before adoption.",
            vaccination = VaccinationStatus.PARTIAL, createdAt = "2026-08-28T09:00:00Z",
            offsetKm = DemoOffsetKm(2.9, 1.2),
        ),
        Listing(
            id = "listing-2", ownerId = OwnerId("owner-rohan"), kind = ListingKind.SELL,
            title = "KCI registered Beagle puppies",
            photos = listOf(photo("photo-1598133894008-61f7fdb8cc3a"), photo("photo-1505628346881-b72b27e84530")),
            species = PetSpecies.DOG, breed = "Beagle", gender = PetGender.MALE,
            dateOfBirth = "2026-07-01", location = DELHI, price = 25_000,
            description = "Home-bred litter, both parents on site and health screened. Vaccination card and pedigree papers included.",
            vaccination = VaccinationStatus.VACCINATED, createdAt = "2026-09-01T09:00:00Z",
            offsetKm = DemoOffsetKm(-1.6, -2.8),
        ),
        Listing(
            id = "listing-3", ownerId = OwnerId("owner-priya"), kind = ListingKind.ADOPT,
            title = "Gentle 4-year-old indie needs a quiet home",
            photos = listOf(photo("photo-1548199973-03cce0bbc87b")),
            species = PetSpecies.DOG, breed = "Indie", gender = PetGender.MALE,
            dateOfBirth = "2022-05-10", location = DELHI, price = null,
            description = "Our landlord will not allow pets any more and we are heartbroken. He is neutered, vaccinated, and wonderful with children.",
            vaccination = VaccinationStatus.VACCINATED, createdAt = "2026-09-04T09:00:00Z",
            offsetKm = DemoOffsetKm(3.7, -4.1),
        ),
    )
}
