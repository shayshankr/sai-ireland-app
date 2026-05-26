package org.sathyasaieire.app.feature.more.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Announcement
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class MoreItem(
    val icon: ImageVector,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val route: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit,
    isAdmin: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val items = buildList {
        add(MoreItem(Icons.Outlined.VolunteerActivism, "💬", "WhatsApp Group", "Request to join our community group", "whatsapp_join"))
        add(MoreItem(Icons.Outlined.LibraryMusic, "🎵", "Bhajan Library", "Browse and play bhajans", "bhajans"))
        add(MoreItem(Icons.Outlined.Timer, "🧘", "Meditation Timer", "Guided meditation timer", "timer"))
        add(MoreItem(Icons.Outlined.Photo, "🖼", "Gallery", "Photos from our events and satsangs", "gallery"))
        add(MoreItem(Icons.Outlined.VolunteerActivism, "💚", "Seva Sign-up", "Register for seva opportunities", "seva"))
        add(MoreItem(Icons.Outlined.ContactMail, "✉️", "Contact Admin", "Send a message to the admin", "contact"))
        add(MoreItem(Icons.Outlined.Person, "👤", "Profile", "Manage your account", "profile"))
        add(MoreItem(Icons.Outlined.Settings, "⚙️", "Settings", "App preferences and notifications", "settings"))
        if (isAdmin) {
            add(MoreItem(Icons.Outlined.Announcement, "📢", "Manage Announcements", "Create, edit, or hide announcements", "admin_announcements"))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        val padding = PaddingValues(
            top = innerPadding.calculateTopPadding() + contentPadding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding() + contentPadding.calculateBottomPadding(),
            start = contentPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
            end = contentPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
        )
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items) { item ->
                MoreRow(item = item, onClick = { onNavigate(item.route) })
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
            }
        }
    }
}

@Composable
private fun MoreRow(item: MoreItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = item.emoji, style = MaterialTheme.typography.titleLarge)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
