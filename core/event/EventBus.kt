package com.hermes.companion.core.event

import com.hermes.companion.core.domain.AppEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Simple in-process event bus backed by SharedFlow.
 * Producers emit [AppEvent] instances; collectors receive them
 * via [events].  Buffer is unlimited to avoid dropping events
 * when collectors are briefly suspended.
 */
class EventBus {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 128)
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    /** Fire-and-forget emit. */
    fun emit(event: AppEvent) {
        scope.launch { _events.emit(event) }
    }

    /** Suspended emit — use inside a coroutine. */
    suspend fun emitSuspend(event: AppEvent) {
        _events.emit(event)
    }

    /**
     * Collect events that match [predicate].
     * Returns a handle that can be used to cancel collection.
     */
    fun collectFiltered(
        predicate: (AppEvent) -> Boolean,
        handler: suspend (AppEvent) -> Unit
    ) = scope.launch {
        events.collect { event ->
            if (predicate(event)) handler(event)
        }
    }
}