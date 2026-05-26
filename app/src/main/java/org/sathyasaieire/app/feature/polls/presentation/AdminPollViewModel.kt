package org.sathyasaieire.app.feature.polls.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sathyasaieire.app.feature.polls.data.PollRepository
import javax.inject.Inject

data class AdminPollState(
    val question: String = "",
    val options: List<String> = listOf("", ""),
    val isCreating: Boolean = false,
    val savedOk: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AdminPollViewModel @Inject constructor(
    private val repository: PollRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminPollState())
    val state: StateFlow<AdminPollState> = _state.asStateFlow()

    fun onQuestionChange(v: String) = _state.update { it.copy(question = v) }

    fun onOptionChange(index: Int, v: String) = _state.update {
        val opts = it.options.toMutableList().also { list -> list[index] = v }
        it.copy(options = opts)
    }

    fun addOption() = _state.update {
        if (it.options.size >= 8) it else it.copy(options = it.options + "")
    }

    fun removeOption(index: Int) = _state.update {
        if (it.options.size <= 2) it
        else it.copy(options = it.options.filterIndexed { i, _ -> i != index })
    }

    fun clearError() = _state.update { it.copy(error = null) }

    fun create() {
        val s = _state.value
        val validOptions = s.options.map { it.trim() }.filter { it.isNotBlank() }
        if (s.question.isBlank() || validOptions.size < 2) {
            _state.update { it.copy(error = "Enter a question and at least 2 non-empty options") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isCreating = true) }
            repository.createPoll(s.question.trim(), validOptions).fold(
                onSuccess = { _state.update { it.copy(isCreating = false, savedOk = true) } },
                onFailure = { e -> _state.update { it.copy(isCreating = false, error = e.message) } },
            )
        }
    }
}
