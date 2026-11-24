package com.petconnect.domain

import com.petconnect.domain.model.AvailabilityWindow
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.model.Pet
import com.petconnect.domain.model.PetGender
import com.petconnect.domain.model.PetId
import com.petconnect.domain.model.PetLocation
import com.petconnect.domain.model.PetSpecies
import com.petconnect.domain.model.VaccinationStatus
import com.petconnect.domain.util.Dates
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/** Frozen "today" so age-based tests never rot. */
val fixedDates = Dates(Clock.fixed(Instant.parse("2026-11-19T10:00:00Z"), ZoneOffset.UTC))

val ME = OwnerId("me")
val THEM = OwnerId("them")

fun pet(
    id: String = "p1",
    owner: OwnerId = THEM,
    species: PetSpecies = PetSpecies.DOG,
    breed: String = "Labrador",
    gender: PetGender = PetGender.FEMALE,
    dob: String = "2023-11-19",
    vaccination: VaccinationStatus = VaccinationStatus.VACCINATED,
    availableForBreeding: Boolean = true,
    availability: AvailabilityWindow = AvailabilityWindow.Off,
) = Pet(
    id = PetId(id),
    ownerId = owner,
    name = "Pet $id",
    photos = emptyList(),
    species = species,
    breed = breed,
    gender = gender,
    dateOfBirth = dob,
    location = PetLocation(28.61, 77.20, "Delhi"),
    bio = "",
    vaccination = vaccination,
    isSpayedOrNeutered = false,
    availableForBreeding = availableForBreeding,
    availability = availability,
    createdAt = "2026-01-01T00:00:00Z",
)
