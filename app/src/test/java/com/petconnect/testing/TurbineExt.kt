package com.petconnect.testing

import app.cash.turbine.ReceiveTurbine

/**
 * Skips intermediate emissions (e.g. the initial Loading state) and returns
 * the first item matching [predicate]. Keeps tests independent of exactly how
 * many times `combine` happens to emit while upstream flows settle.
 */
suspend fun <T> ReceiveTurbine<T>.awaitItemMatching(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
