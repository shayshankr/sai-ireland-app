package org.sathyasaieire.app.feature.polls.presentation

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
import org.sathyasaieire.app.domain.model.Poll
import org.sathyasaieire.app.domain.model.PollWithResults
import org.sathyasaieire.app.feature.polls.data.PollRepository
import javax.inject.Inject

@HiltViewModel
class PollsListViewModel @Inject constructor(
    repository: PollRepository,
) : ViewModel() {
    val polls: StateFlow<List<Poll>> = repository.getPolls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@HiltViewModel
class PollDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PollRepository,
) : ViewModel() {

    private val pollId: String = checkNotNull(savedStateHandle["pollId"])

    private val _result = MutableStateFlow<PollWithResults?>(null)
    val result: StateFlow<PollWithResults?> = _result.asStateFlow()

    private val _isVoting = MutableStateFlow(false)
    val isVoting: StateFlow<Boolean> = _isVoting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            repository.getPollWithResults(pollId).fold(
                onSuccess = { _result.value = it },
                onFailure = { _error.value = it.message },
            )
        }
    }

    fun vote(optionIndex: Int) {
        viewModelScope.launch {
            _isVoting.value = true
            repository.vote(pollId, optionIndex).fold(
                onSuccess = { load() },
                onFailure = { _error.value = it.message },
            )
            _isVoting.value = false
        }
    }

    fun closePoll() {
        viewModelScope.launch {
            repository.closePoll(pollId).fold(
                onSuccess = { load() },
                onFailure = { _error.value = it.message },
            )
        }
    }

    fun clearError() { _error.value = null }
}
