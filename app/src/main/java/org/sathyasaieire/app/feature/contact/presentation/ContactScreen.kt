package org.sathyasaieire.app.feature.contact.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.sathyasaieire.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    onBack: () -> Unit,
    viewModel: ContactViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val form by viewModel.form.collectAsState()
    val context = LocalContext.current
    val adminEmail = stringResource(R.string.admin_email)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Admin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is ContactUiState.Submitting -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            is ContactUiState.Success -> {
                SuccessView(
                    subject = state.subject,
                    message = state.message,
                    adminEmail = adminEmail,
                    onSendEmail = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(adminEmail))
                            putExtra(Intent.EXTRA_SUBJECT, "[Sai Ireland App] ${state.subject}")
                            putExtra(Intent.EXTRA_TEXT, state.message)
                        }
                        runCatching { context.startActivity(intent) }
                    },
                    onSendAnother = viewModel::reset,
                    contentPadding = padding,
                )
            }

            is ContactUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Text("⚠️", style = MaterialTheme.typography.displaySmall)
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        OutlinedButton(onClick = viewModel::reset) { Text("Try again") }
                    }
                }
            }

            is ContactUiState.Form -> {
                ContactForm(
                    form = form,
                    onSubjectChange = viewModel::onSubjectChange,
                    onMessageChange = viewModel::onMessageChange,
                    onSend = viewModel::send,
                    contentPadding = padding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactForm(
    form: ContactFormState,
    onSubjectChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
) {
    var subjectExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        Text(text = "✉️", style = MaterialTheme.typography.displaySmall)
        Text(
            text = "Send a message",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Your message will be logged and the admin will follow up with you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = subjectExpanded,
            onExpandedChange = { subjectExpanded = it },
        ) {
            OutlinedTextField(
                value = form.subject,
                onValueChange = {},
                readOnly = true,
                label = { Text("Subject") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(subjectExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = subjectExpanded,
                onDismissRequest = { subjectExpanded = false },
            ) {
                CONTACT_SUBJECTS.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject) },
                        onClick = { onSubjectChange(subject); subjectExpanded = false },
                    )
                }
            }
        }

        OutlinedTextField(
            value = form.message,
            onValueChange = onMessageChange,
            label = { Text("Message *") },
            placeholder = { Text("How can we help you?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 10,
        )

        Button(
            onClick = onSend,
            enabled = form.message.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Send Message")
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SuccessView(
    subject: String,
    message: String,
    adminEmail: String,
    onSendEmail: () -> Unit,
    onSendAnother: () -> Unit,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(32.dp))

        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(8.dp)
                .height(64.dp)
                .fillMaxWidth(0.2f),
        )
        Text(
            text = "Message sent!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Thank you for reaching out. The admin will get back to you soon.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "You can also send a direct email to $adminEmail if you need a faster response.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedButton(onClick = onSendEmail, modifier = Modifier.fillMaxWidth()) {
            Text("Open Email App")
        }

        Button(onClick = onSendAnother, modifier = Modifier.fillMaxWidth()) {
            Text("Send Another Message")
        }
    }
}
