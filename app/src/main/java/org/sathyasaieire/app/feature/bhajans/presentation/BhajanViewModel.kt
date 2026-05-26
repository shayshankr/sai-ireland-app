package org.sathyasaieire.app.feature.bhajans.presentation

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
import org.sathyasaieire.app.domain.model.Bhajan
import org.sathyasaieire.app.feature.bhajans.data.BhajanRepository
import javax.inject.Inject

@HiltViewModel
class BhajanListViewModel @Inject constructor(
    repository: BhajanRepository,
) : ViewModel() {
    val bhajans: StateFlow<List<Bhajan>> = repository.getBhajans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun onQueryChange(q: String) { _query.value = q }

    fun filtered(bhajans: List<Bhajan>, query: String): List<Bhajan> {
        if (query.isBlank()) return bhajans
        val q = query.trim().lowercase()
        return bhajans.filter {
            it.title.lowercase().contains(q) ||
                it.language.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
        }
    }
}

@HiltViewModel
class BhajanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BhajanRepository,
) : ViewModel() {

    private val bhajanId: String = checkNotNull(savedStateHandle["bhajanId"])

    private val _bhajan = MutableStateFlow<Bhajan?>(null)
    val bhajan: StateFlow<Bhajan?> = _bhajan.asStateFlow()

    init {
        viewModelScope.launch { _bhajan.value = repository.getById(bhajanId) }
    }
}
