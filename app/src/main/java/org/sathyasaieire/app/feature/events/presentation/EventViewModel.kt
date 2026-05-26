package org.sathyasaieire.app.feature.events.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sathyasaieire.app.domain.model.Event
import org.sathyasaieire.app.feature.alarm.ReminderOffset
import org.sathyasaieire.app.feature.alarm.ReminderScheduler
import org.sathyasaieire.app.feature.events.data.EventRepository
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
) : ViewModel() {

    val upcomingEvents: StateFlow<List<Event>> = repository.getUpcoming()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pastEvents: StateFlow<List<Event>> = repository.getPast()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refresh().onFailure { _error.value = it.message }
            _isRefreshing.value = false
        }
    }

    fun clearError() { _error.value = null }
}

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EventRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    private val eventId: String = checkNotNull(savedStateHandle["eventId"])

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    init {
        viewModelScope.launch {
            _event.value = repository.getById(eventId)
        }
    }

    fun scheduleReminder(offset: ReminderOffset) {
        val e = _event.value ?: return
        reminderScheduler.schedule(e, offset)
    }
}
