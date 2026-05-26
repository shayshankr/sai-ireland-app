package org.sathyasaieire.app.feature.announcements.presentation

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
import org.sathyasaieire.app.domain.model.Announcement
import org.sathyasaieire.app.feature.announcements.data.AnnouncementRepository
import javax.inject.Inject

@HiltViewModel
class AdminAnnouncementsListViewModel @Inject constructor(
    private val repository: AnnouncementRepository,
) : ViewModel() {

    val announcements: StateFlow<List<Announcement>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun toggleActive(announcement: Announcement) {
        viewModelScope.launch {
            runCatching { repository.setActive(announcement.id, !announcement.isActive) }
                .onFailure { _error.value = it.message }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError() { _error.value = null }
}

data class AnnouncementFormState(
    val title: String = "",
    val body: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val savedOk: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AdminAnnouncementFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AnnouncementRepository,
) : ViewModel() {

    private val announcementId: String? = savedStateHandle["announcementId"]
    val isEditing: Boolean get() = announcementId != null

    private val _state = MutableStateFlow(AnnouncementFormState())
    val state: StateFlow<AnnouncementFormState> = _state.asStateFlow()

    init {
        if (announcementId != null) {
            _state.value = _state.value.copy(isLoading = true)
            viewModelScope.launch {
                val ann = repository.getById(announcementId)
                _state.value = _state.value.copy(
                    title = ann?.title ?: "",
                    body = ann?.body ?: "",
                    isLoading = false,
                )
            }
        }
    }

    fun onTitleChange(v: String) { _state.value = _state.value.copy(title = v) }
    fun onBodyChange(v: String) { _state.value = _state.value.copy(body = v) }

    fun save() {
        val s = _state.value
        if (s.title.isBlank()) {
            _state.value = s.copy(error = "Title is required")
            return
        }
        _state.value = s.copy(isSaving = true, error = null)
        viewModelScope.launch {
            runCatching {
                if (announcementId != null) {
                    repository.update(announcementId, s.title.trim(), s.body.trim())
                } else {
                    repository.create(s.title.trim(), s.body.trim())
                }
            }.fold(
                onSuccess = { _state.value = _state.value.copy(isSaving = false, savedOk = true) },
                onFailure = { _state.value = _state.value.copy(isSaving = false, error = it.message) },
            )
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = announcementId ?: return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .fold(
                    onSuccess = { onDeleted() },
                    onFailure = { _state.value = _state.value.copy(isSaving = false, error = it.message) },
                )
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
