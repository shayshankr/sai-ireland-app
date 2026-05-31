package org.sathyasaieire.app.feature.whatsapp.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.sathyasaieire.app.R
import org.sathyasaieire.app.domain.model.JoinRequest
import org.sathyasaieire.app.domain.model.JoinRequestStatus

private val IRISH_COUNTIES = listOf(
    "Carlow", "Cavan", "Clare", "Cork", "Donegal", "Dublin",
    "Galway", "Kerry", "Kildare", "Kilkenny", "Laois", "Leitrim",
    "Limerick", "Longford", "Louth", "Mayo", "Meath", "Monaghan",
    "Offaly", "Roscommon", "Sligo", "Tipperary", "Waterford",
    "Westmeath", "Wexford", "Wicklow",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppJoinScreen(
    onBack: () -> Unit,
    viewModel: WhatsAppViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val form by viewModel.form.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Group") },
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
            is WhatsAppUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            is WhatsAppUiState.Form -> {
                JoinRequestForm(
                    form = form,
                    onNameChange = viewModel::onNameChange,
                    onPhoneChange = viewModel::onPhoneChange,
                    onCountyChange = viewModel::onCountyChange,
                    onMessageChange = viewModel::onMessageChange,
                    onSubmit = viewModel::submit,
                    contentPadding = padding,
                )
            }

            is WhatsAppUiState.Status -> {
                val groupLink = androidx.compose.ui.res.stringResource(R.string.whatsapp_group_link)
                RequestStatusView(request = state.request, defaultGroupLink = groupLink, contentPadding = padding)
            }

            is WhatsAppUiState.Error -> {
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
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinRequestForm(
    form: WhatsAppFormState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCountyChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSubmit: () -> Unit,
    contentPadding: PaddingValues,
) {
    var countyExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        // Header
        Text(text = "💬", style = MaterialTheme.typography.displaySmall)
        Text(
            text = "Join our WhatsApp Group",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Stay connected with the Sathya Sai community in Ireland. " +
                "Submit your request and our admin will add you to the group.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = form.name,
            onValueChange = onNameChange,
            label = { Text("Full name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = form.phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone number (WhatsApp) *") },
            placeholder = { Text("+353 87 …") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        ExposedDropdownMenuBox(
            expanded = countyExpanded,
            onExpandedChange = { countyExpanded = it },
        ) {
            OutlinedTextField(
                value = form.county,
                onValueChange = {},
                readOnly = true,
                label = { Text("County") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(countyExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = countyExpanded,
                onDismissRequest = { countyExpanded = false },
            ) {
                IRISH_COUNTIES.forEach { county ->
                    DropdownMenuItem(
                        text = { Text(county) },
                        onClick = { onCountyChange(county); countyExpanded = false },
                    )
                }
            }
        }

        OutlinedTextField(
            value = form.message,
            onValueChange = onMessageChange,
            label = { Text("Brief introduction (optional)") },
            placeholder = { Text("How did you connect with Sai Baba?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
        )

        Button(
            onClick = onSubmit,
            enabled = !form.isSubmitting && form.name.isNotBlank() && form.phone.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
        ) {
            Text(if (form.isSubmitting) "Submitting…" else "Send Join Request")
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun RequestStatusView(
    request: JoinRequest,
    defaultGroupLink: String,
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        val (emoji, headline, bodyText) = when (request.status) {
            JoinRequestStatus.PENDING -> Triple(
                "⏳",
                "Request pending",
                "Your request is under review. Our admin will be in touch soon.",
            )
            JoinRequestStatus.APPROVED -> Triple(
                "✅",
                "Request approved!",
                "Tap the button below to join the group.",
            )
            JoinRequestStatus.REJECTED -> Triple(
                "❌",
                "Request not approved",
                "Unfortunately your request was not approved at this time. " +
                    "Please contact the admin for more information.",
            )
        }

        Text(text = emoji, style = MaterialTheme.typography.displayMedium)
        Text(text = headline, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(text = bodyText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LabeledRow("Name", request.name)
                LabeledRow("Phone", request.phone)
                if (request.county.isNotBlank()) LabeledRow("County", request.county)
                LabeledRow("Status", request.status.label)
            }
        }

        if (request.status == JoinRequestStatus.APPROVED) {
            val link = request.whatsappLink ?: defaultGroupLink
            Button(
                onClick = {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
            ) {
                Text("Open WhatsApp Group")
            }
        }
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
