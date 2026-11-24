package com.petconnect.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The Room database. Bump [version] whenever an @Entity changes and add a
 * Migration (see docs/04-room.md) — otherwise existing users' data is lost.
 */
@Database(
    entities = [MessageEntity::class, BlockedOwnerEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PetConnectDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        const val NAME = "petconnect.db"
    }
}
