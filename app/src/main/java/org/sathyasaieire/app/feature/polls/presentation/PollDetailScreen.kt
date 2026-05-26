package org.sathyasaieire.app.feature.polls.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.sathyasaieire.app.domain.model.PollWithResults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollDetailScreen(
    onBack: () -> Unit,
    isAdmin: Boolean = false,
    viewModel: PollDetailViewModel = hiltViewModel(),
) {
    val result by viewModel.result.collectAsState()
    val isVoting by viewModel.isVoting.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showCloseConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Poll") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAdmin && result?.poll?.isOpen == true) {
                        IconButton(onClick = { showCloseConfirm = true }) {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = "Close poll",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (result == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        PollContent(
            data = result!!,
            isAdmin = isAdmin,
            isVoting = isVoting,
            onVote = viewModel::vote,
            contentPadding = padding,
        )
    }

    if (showCloseConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseConfirm = false },
            title = { Text("Close poll?") },
            text = { Text("No more votes will be accepted. This cannot be undone.") },
            confirmButton = {
                Button(onClick = { viewModel.closePoll(); showCloseConfirm = false }) {
                    Text("Close Poll")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PollContent(
    data: PollWithResults,
    isAdmin: Boolean,
    isVoting: Boolean,
    onVote: (Int) -> Unit,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
) {
    val poll = data.poll
    val showResults = data.hasVoted || !poll.isOpen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Status chip
        SuggestionChip(
            onClick = {},
            label = { Text(if (poll.isOpen) "🗳 Open" else "🔒 Closed") },
        )

        Text(
            text = poll.question,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "${data.totalVotes} vote${if (data.totalVotes != 1) "s" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        HorizontalDivider()
        Spacer(Modifier.height(4.dp))

        if (showResults) {
            ResultsBars(data = data)
        } else {
            VotingOptions(
                poll = poll,
                isVoting = isVoting,
                onVote = onVote,
            )
        }
    }
}

@Composable
private fun ResultsBars(data: PollWithResults) {
    val poll = data.poll
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        poll.options.forEachIndexed { index, option ->
            val count = data.countFor(index)
            val pct = data.percentageFor(index)
            val isMyVote = data.myVoteIndex == index

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        if (isMyVote) {
                            Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isMyVote) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isMyVote) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = "$count · ${(pct * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LinearProgressIndicator(
                    progress = { pct },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (isMyVote) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun VotingOptions(
    poll: org.sathyasaieire.app.domain.model.Poll,
    isVoting: Boolean,
    onVote: (Int) -> Unit,
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        poll.options.forEachIndexed { index, option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!isVoting) Modifier.padding(vertical = 2.dp) else Modifier
                    ),
            ) {
                RadioButton(
                    selected = selectedIndex == index,
                    onClick = { if (!isVoting) selectedIndex = index },
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { if (selectedIndex >= 0) onVote(selectedIndex) },
            enabled = selectedIndex >= 0 && !isVoting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isVoting) "Submitting…" else "Cast Vote")
        }
    }
}
