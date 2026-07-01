package org.sathyasaieire.app.feature.events.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.sathyasaieire.app.domain.model.Event
import org.sathyasaieire.app.domain.model.EventCategory
import org.sathyasaieire.app.ui.components.MonthCalendar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    onEventClick: (String) -> Unit,
    onCreateEvent: (() -> Unit)? = null,
    isAdmin: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: EventViewModel = hiltViewModel(),
) {
    val upcoming by viewModel.upcomingEvents.collectAsState()
    val past by viewModel.pastEvents.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showCalendar by remember { mutableStateOf(true) }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar("Could not refresh — showing cached data")
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (onCreateEvent != null) {
                FloatingActionButton(
                    onClick = { onCreateEvent.invoke() },
                    modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding()),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Create event")
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Events") },
                actions = {
                    TextButton(onClick = { showCalendar = !showCalendar }) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = "Toggle calendar",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        val listBottomPadding = innerPadding.calculateBottomPadding() + contentPadding.calculateBottomPadding()
        val padding = PaddingValues(
            top = innerPadding.calculateTopPadding() + contentPadding.calculateTopPadding(),
            start = contentPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
            end = contentPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
        )
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0; selectedDate = null }) {
                    Text("Upcoming", modifier = Modifier.padding(vertical = 12.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; selectedDate = null }) {
                    Text("Past", modifier = Modifier.padding(vertical = 12.dp))
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                val events = if (selectedTab == 0) upcoming else past

                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp + listBottomPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Calendar (upcoming tab only)
                    if (selectedTab == 0) {
                        item {
                            Column {
                            AnimatedVisibility(visible = showCalendar) {
                                val eventDates = upcoming.map { event ->
                                    Instant.ofEpochMilli(event.dateTimeMs)
                                        .atZone(ZoneId.of(event.timezone))
                                        .toLocalDate()
                                }.toSet()
                                MonthCalendar(
                                    eventDates = eventDates,
                                    selectedDate = selectedDate,
                                    onDateSelected = { selectedDate = it },
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                            }
                            } // Column
                        }
                        if (selectedDate != null) {
                            item {
                                FilterChip(
                                    selected = true,
                                    onClick = { selectedDate = null },
                                    label = { Text("Showing ${selectedDate.toString()} · tap to clear") },
                                )
                            }
                        }
                    }

                    val filtered = if (selectedDate != null) {
                        events.filter { event ->
                            Instant.ofEpochMilli(event.dateTimeMs)
                                .atZone(ZoneId.of(event.timezone))
                                .toLocalDate() == selectedDate
                        }
                    } else events

                    if (filtered.isEmpty()) {
                        item { EmptyEventsState(isUpcoming = selectedTab == 0) }
                    } else {
                        items(filtered, key = { it.id }) { event ->
                            EventCard(event = event, onClick = { onEventClick(event.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            if (event.coverImageUrl != null) {
                AsyncImage(
                    model = event.coverImageUrl,
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                CategoryHeader(category = event.category)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoryBadge(category = event.category)
                    if (event.recurrence.name != "NONE") {
                        Text(
                            text = "↻ ${event.recurrence.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${event.formatDate()} · ${event.formatTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (event.location.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: EventCategory) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(MaterialTheme.shapes.medium)
            .then(Modifier.padding(0.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize()
                    .then(Modifier)
            )
            Text(text = category.emoji, style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Composable
private fun CategoryBadge(category: EventCategory) {
    androidx.compose.material3.SuggestionChip(
        onClick = {},
        label = { Text(text = "${category.emoji} ${category.displayName}", style = MaterialTheme.typography.labelSmall) },
    )
}

@Composable
private fun EmptyEventsState(isUpcoming: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Outlined.EventBusy,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (isUpcoming) "No upcoming events" else "No past events",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val EventCategory.emoji: String get() = when (this) {
    EventCategory.BHAJAN -> "🎵"
    EventCategory.SATSANG -> "🙏"
    EventCategory.SEVA -> "💚"
    EventCategory.STUDY_CIRCLE -> "📖"
    EventCategory.FESTIVAL -> "🪔"
    EventCategory.OTHER -> "📅"
}
