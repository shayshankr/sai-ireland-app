package org.sathyasaieire.app.feature.thoughtofday.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sathyasaieire.app.feature.home.data.HomeRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AdminThoughtState(
    val date: LocalDate = LocalDate.now(),
    val text: String = "",
    val attribution: String = "Sri Sathya Sai Baba",
    val source: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AdminThoughtOfDayViewModel @Inject constructor(
    private val repository: HomeRepository,
) : ViewModel() {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _state = MutableStateFlow(AdminThoughtState())
    val state: StateFlow<AdminThoughtState> = _state.asStateFlow()

    init {
        loadForDate(LocalDate.now())
    }

    fun onPreviousDay() = onDateChanged(_state.value.date.minusDays(1))
    fun onNextDay() = onDateChanged(_state.value.date.plusDays(1))

    fun onDateChanged(date: LocalDate) {
        _state.value = _state.value.copy(date = date, saved = false, error = null)
        loadForDate(date)
    }

    fun onTextChanged(text: String) { _state.value = _state.value.copy(text = text) }
    fun onAttributionChanged(attribution: String) { _state.value = _state.value.copy(attribution = attribution) }
    fun onSourceChanged(source: String) { _state.value = _state.value.copy(source = source) }

    private fun loadForDate(date: LocalDate) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val thought = repository.getThoughtForDate(date.format(formatter))
            _state.value = _state.value.copy(
                text = thought?.text ?: "",
                attribution = thought?.attribution ?: "Sri Sathya Sai Baba",
                source = thought?.source ?: "",
                isLoading = false,
            )
        }
    }

    fun save() {
        val s = _state.value
        if (s.text.isBlank()) {
            _state.value = s.copy(error = "Thought text cannot be empty")
            return
        }
        viewModelScope.launch {
            _state.value = s.copy(isSaving = true, error = null)
            repository.saveThought(
                date = s.date.format(formatter),
                text = s.text.trim(),
                attribution = s.attribution.trim().ifBlank { "Sri Sathya Sai Baba" },
                source = s.source.trim(),
            ).fold(
                onSuccess = { _state.value = _state.value.copy(isSaving = false, saved = true) },
                onFailure = { e -> _state.value = _state.value.copy(isSaving = false, error = e.message ?: "Save failed") },
            )
        }
    }

    fun clearSaved() { _state.value = _state.value.copy(saved = false) }
    fun clearError() { _state.value = _state.value.copy(error = null) }
}
