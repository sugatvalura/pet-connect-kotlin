package com.petconnect.data.local

import java.util.UUID
import javax.inject.Inject

/** Injectable so tests can produce deterministic ids. */
open class IdGenerator @Inject constructor() {
    open fun next(prefix: String): String = "$prefix-${UUID.randomUUID().toString().take(12)}"
}
