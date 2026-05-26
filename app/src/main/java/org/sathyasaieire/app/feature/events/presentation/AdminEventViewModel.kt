package org.sathyasaieire.app.feature.events.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sathyasaieire.app.domain.model.Event
import org.sathyasaieire.app.domain.model.EventCategory
import org.sathyasaieire.app.domain.model.RecurrenceType
import org.sathyasaieire.app.feature.events.data.EventRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

data class AdminEventState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dateTimeInput: String = "",
    val timezone: String = "Europe/Dublin",
    val location: String = "",
    val mapsLink: String = "",
    val coverImageUrl: String = "",
    val category: EventCategory = EventCategory.OTHER,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val isSaving: Boolean = false,
    val savedOk: Boolean = false,
    val deletedOk: Boolean = false,
    val error: String? = null,
) {
    val isNew: Boolean get() = id.isEmpty()
}

@HiltViewModel
class AdminEventViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EventRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {

    private val eventId: String? = savedStateHandle["eventId"]

    private val _state = MutableStateFlow(AdminEventState(id = eventId ?: ""))
    val state: StateFlow<AdminEventState> = _state.asStateFlow()

    private val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    init {
        if (eventId != null) {
            viewModelScope.launch {
                val event = repository.getById(eventId) ?: return@launch
                val localDt = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(event.dateTimeMs),
                    ZoneId.of(event.timezone),
                )
                _state.update {
                    it.copy(
                        id = event.id,
                        title = event.title,
                        description = event.description,
                        dateTimeInput = localDt.format(fmt),
                        timezone = event.timezone,
                        location = event.location,
                        mapsLink = event.mapsLink ?: "",
                        coverImageUrl = event.coverImageUrl ?: "",
                        category = event.category,
                        recurrence = event.recurrence,
                    )
                }
            }
        }
    }

    fun onTitleChange(v: String) = _state.update { it.copy(title = v) }
    fun onDescriptionChange(v: String) = _state.update { it.copy(description = v) }
    fun onDateTimeChange(v: String) = _state.update { it.copy(dateTimeInput = v) }
    fun onLocationChange(v: String) = _state.update { it.copy(location = v) }
    fun onMapsLinkChange(v: String) = _state.update { it.copy(mapsLink = v) }
    fun onCoverImageUrlChange(v: String) = _state.update { it.copy(coverImageUrl = v) }
    fun onCategoryChange(v: EventCategory) = _state.update { it.copy(category = v) }
    fun onRecurrenceChange(v: RecurrenceType) = _state.update { it.copy(recurrence = v) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun save() {
        val s = _state.value
        val dateTimeMs = try {
            LocalDateTime.parse(s.dateTimeInput.trim(), fmt)
                .atZone(ZoneId.of(s.timezone))
                .toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            _state.update { it.copy(error = "Invalid date/time — use dd/MM/yyyy HH:mm") }
            return
        }
        val uid = auth.currentUser?.uid ?: run {
            _state.update { it.copy(error = "Not signed in") }; return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val event = Event(
                id = s.id,
                title = s.title.trim(),
                description = s.description.trim(),
                dateTimeMs = dateTimeMs,
                timezone = s.timezone,
                location = s.location.trim(),
                mapsLink = s.mapsLink.trim().ifBlank { null },
                coverImageUrl = s.coverImageUrl.trim().ifBlank { null },
                category = s.category,
                recurrence = s.recurrence,
                createdBy = uid,
                createdAt = System.currentTimeMillis(),
            )
            repository.save(event).fold(
                onSuccess = { _state.update { it.copy(isSaving = false, savedOk = true) } },
                onFailure = { e -> _state.update { it.copy(isSaving = false, error = e.message) } },
            )
        }
    }

    fun delete() {
        val id = _state.value.id.ifEmpty { return }
        viewModelScope.launch {
            repository.delete(id).fold(
                onSuccess = { _state.update { it.copy(deletedOk = true) } },
                onFailure = { e -> _state.update { it.copy(error = e.message) } },
            )
        }
    }
}
