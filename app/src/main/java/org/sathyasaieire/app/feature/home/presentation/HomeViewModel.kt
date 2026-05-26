package org.sathyasaieire.app.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sathyasaieire.app.domain.model.Announcement
import org.sathyasaieire.app.domain.model.Quote
import org.sathyasaieire.app.feature.home.data.HomeRepository
import javax.inject.Inject

data class HomeUiState(
    val quote: Quote? = null,
    val announcements: List<Announcement> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val quoteDeferred = async { repository.getTodayQuote() }
            val announcementsDeferred = async { repository.getActiveAnnouncements() }
            _state.value = HomeUiState(
                quote = quoteDeferred.await(),
                announcements = announcementsDeferred.await(),
                isLoading = false,
            )
        }
    }
}
