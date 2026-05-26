package org.sathyasaieire.app.feature.events.presentation

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.sathyasaieire.app.domain.model.Event
import org.sathyasaieire.app.domain.model.EventCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    onBack: () -> Unit,
    onEditEvent: ((String) -> Unit)? = null,
    viewModel: EventDetailViewModel = hiltViewModel(),
) {
    val event by viewModel.event.collectAsState()
    val context = LocalContext.current
    var showReminderDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (onEditEvent != null && event != null) {
                        IconButton(onClick = { onEditEvent(event!!.id) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit event")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (event == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val e = event!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Cover image or category banner
            if (e.coverImageUrl != null) {
                AsyncImage(
                    model = e.coverImageUrl,
                    contentDescription = e.title,
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentScale = ContentScale.Crop,
                )
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Category + recurrence
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text("${e.category.emoji} ${e.category.displayName}")
                        },
                    )
                    if (e.recurrence.name != "NONE") {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("↻ ${e.recurrence.displayName}") },
                        )
                    }
                }

                Text(
                    text = e.title,
                    style = MaterialTheme.typography.headlineSmall,
                )

                HorizontalDivider()

                // Date + time
                DetailRow(
                    icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    text = e.formatDate(),
                )
                DetailRow(
                    icon = { Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    text = e.formatTime(),
                )

                // Location
                if (e.location.isNotBlank()) {
                    DetailRow(
                        icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        text = e.location,
                    )
                }

                HorizontalDivider()

                // Description
                if (e.description.isNotBlank()) {
                    Text(
                        text = e.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                }

                // Action buttons
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, e.title)
                            putExtra(CalendarContract.Events.DESCRIPTION, e.description)
                            putExtra(CalendarContract.Events.EVENT_LOCATION, e.location)
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, e.dateTimeMs)
                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, e.dateTimeMs + 2 * 3600_000L)
                            putExtra(CalendarContract.Events.EVENT_TIMEZONE, e.timezone)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Add to Google Calendar")
                }

                if (e.location.isNotBlank() || e.mapsLink != null) {
                    OutlinedButton(
                        onClick = {
                            val uri = if (e.mapsLink != null) {
                                Uri.parse(e.mapsLink)
                            } else {
                                Uri.parse("geo:0,0?q=${Uri.encode(e.location)}")
                            }
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.DirectionsWalk, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Get Directions")
                    }
                }

                OutlinedButton(
                    onClick = { showReminderDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.AddTask, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Set Reminder")
                }

                if (showReminderDialog) {
                    ReminderDialog(
                        onDismiss = { showReminderDialog = false },
                        onConfirm = { offset ->
                            viewModel.scheduleReminder(offset)
                            showReminderDialog = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: @Composable () -> Unit, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon()
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
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
