package com.petconnect.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Standard UI-state sharing: keeps upstream alive 5s across config changes
 * (rotation) but stops collecting when the screen is truly gone.
 */
fun <T> Flow<T>.stateInVm(vm: ViewModel, initial: T): StateFlow<T> =
    stateIn(vm.viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)
