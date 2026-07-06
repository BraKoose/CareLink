package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnonymizedUser
import com.example.data.model.DeliveryOrder
import com.example.data.model.MedicationLog
import com.example.data.model.MedicationReminder
import com.example.data.model.PeriodLog
import com.example.ui.CareLinkViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: CareLinkViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTab: (Int) -> Unit
) {
    val profile by viewModel.anonymizedProfile.collectAsState()
    val medicationLogs by viewModel.allMedicationLogs.collectAsState()
    val reminders by viewModel.allReminders.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val periodLogs by viewModel.allPeriodLogs.collectAsState()

    val isDisguised = profile?.isDisguised ?: false

    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showEditAliasDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLogPeriodDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. WELCOME BANNER ---
        item {
            profile?.let { user ->
                WelcomeHeaderCard(
                    user = user,
                    onEditAliasClick = { showEditAliasDialog = true },
                    onPrivacySettingsClick = { showPrivacyDialog = true }
                )
            }
        }

        // --- 2. MENSTRUAL PERIOD TRACKER SECTION (DISGUISE SHIELD) ---
        // Shown prominently if disguised, or accessible as an option
        item {
            PeriodTrackerSection(
                periodLogs = periodLogs,
                isDisguised = isDisguised,
                onLogPeriodClick = { showLogPeriodDialog = true },
                onDeleteLog = { id -> viewModel.deletePeriodLog(id) }
            )
        }

        // --- 3. TODAY'S DAILY PLAN (ADAPTIVE CHECKLIST) ---
        item {
            Text(
                text = if (isDisguised) "Daily Wellness Log 🗓️" else "Today's Checklist 🗓️",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Adaptive Pill Log Card (Supplements or Medication)
        item {
            val today = System.currentTimeMillis()
            val hasLoggedToday = medicationLogs.any { log ->
                val cal1 = Calendar.getInstance().apply { timeInMillis = log.dateMillis }
                val cal2 = Calendar.getInstance().apply { timeInMillis = today }
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
            }
            val wasTaken = medicationLogs.find { log ->
                val cal1 = Calendar.getInstance().apply { timeInMillis = log.dateMillis }
                val cal2 = Calendar.getInstance().apply { timeInMillis = today }
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
            }?.taken ?: false

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (hasLoggedToday && wasTaken)
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (hasLoggedToday && wasTaken) "🎉" else "💊", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isDisguised) "Daily Supplement Log" else "Daily Medication Log",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (hasLoggedToday) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (wasTaken) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                        else Color.Red.copy(alpha = 0.1f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (wasTaken) "Taken ✓" else "Missed ✕",
                                    color = if (wasTaken) MaterialTheme.colorScheme.secondary else Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (!hasLoggedToday) {
                        Text(
                            text = if (isDisguised) {
                                "Log your daily multi-vitamin and wellness support capsules to maintain strong immune tracking!"
                            } else {
                                "Log today's PrEP or antiretroviral intake to keep up your protective streak!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.logMedicationIntake(
                                        medType = "PrEP/ARV",
                                        dateMillis = System.currentTimeMillis(),
                                        taken = true
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("log_taken_button"),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Taken", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark Taken", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.logMedicationIntake(
                                        medType = "PrEP/ARV",
                                        dateMillis = System.currentTimeMillis(),
                                        taken = false
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("log_missed_button"),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Missed", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark Missed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = if (wasTaken) {
                                if (isDisguised) "Excellent job staying consistent with your daily wellness routine! 🌸"
                                else "Beautiful work! Your health is your power. 💪 Keep going!"
                            } else {
                                "Tomorrow is a fresh start! Take deep breaths and keep up your supportive cycle. ❤️"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = if (wasTaken) MaterialTheme.colorScheme.secondary else Color.Gray
                        )
                    }
                }
            }
        }

        // Other custom reminders / clinic pickups (Adaptive nomenclature)
        if (reminders.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isDisguised) "Wellness Reminders" else "Upcoming Reminders",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Gray
                    )
                    IconButton(
                        onClick = { showAddReminderDialog = true },
                        modifier = Modifier.size(24.dp).testTag("add_reminder_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            items(reminders) { reminder ->
                val adaptiveTitle = if (isDisguised) {
                    reminder.title
                        .replace("PrEP", "Daily Vitamins")
                        .replace("ARV", "Daily Vitamin Support")
                        .replace("Medication", "Wellness Supplements")
                        .replace("Clinic Refill", "Wellness Refill Checkup")
                } else {
                    reminder.title
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = reminder.isCompleted,
                                onCheckedChange = { isChecked ->
                                    viewModel.updateReminderStatus(reminder.id, isChecked)
                                },
                                modifier = Modifier.testTag("checkbox_${reminder.id}")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = adaptiveTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (reminder.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (reminder.type == "CLINIC_PICKUP") Icons.Default.MedicalServices else Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${reminder.timeString} • ${reminder.notes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { viewModel.deleteReminder(reminder.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // --- 4. QUICK SUPPORT SPACES (LARGE, BEAUTIFUL CLICKABLE GRID) ---
        item {
            Text(
                text = if (isDisguised) "Your Wellness Spaces 🌸" else "Your Support Spaces 🌟",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AI COACH CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab(1) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💬", fontSize = 28.sp)
                        Text(
                            text = if (isDisguised) "Symptom Coach" else "AI Coach",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isDisguised) {
                                "Discuss cramps, menstrual cycle symptoms, or overall wellness safely."
                            } else {
                                "Ask private health questions 100% securely."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            lineHeight = 14.sp
                        )
                    }
                }

                // STOREFRONT CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab(3) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🛍️", fontSize = 28.sp)
                        Text(
                            text = if (isDisguised) "Essentials Shop" else "Wellness Shop",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = if (isDisguised) {
                                "Order vitamins, self-tests, and wellness essentials discreetly."
                            } else {
                                "Order self-test kits & medications anonymously."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // --- 5. HEALTH ASSESSMENT ACCENT BANNER ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDisguised) "Let's Check Your Cycle Wellness! 🎯" else "Let's Check Your Wellness! 🎯",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isDisguised) {
                                "Take our 1-minute, confidential, supportive checkup to understand your cycle indicators."
                            } else {
                                "Take our 1-minute, private, supportive quiz to understand your health needs."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onNavigateToTab(2) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(36.dp).testTag("take_risk_quiz_cta")
                        ) {
                            Text(if (isDisguised) "Start Wellness Quiz" else "Start Health Quiz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("✨", fontSize = 38.sp)
                }
            }
        }

        // --- 6. DISCREET DELIVERIES TRACKER (Adaptive order tracking) ---
        if (orders.isNotEmpty()) {
            item {
                Text(
                    text = if (isDisguised) "Discreet Shipments" else "Discreet Deliveries",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(orders) { order ->
                val adaptiveItemName = if (isDisguised) {
                    order.itemName
                        .replace("HIV Self-Test Kit", "FlowTrack Wellness Testing Strip")
                        .replace("PrEP", "Daily Multi-Vitamins")
                        .replace("ARV Refill", "Symptom Care Kit")
                        .replace("PEP", "Urgent Emergency Health Pack")
                        .replace("STI Combo Panel Rapid Test Kit", "Home pH Balance Wellness Kit")
                } else {
                    order.itemName
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = adaptiveItemName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Confidential Package #${order.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (order.trackingStatus) {
                                            "Delivered" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = order.trackingStatus,
                                    color = when (order.trackingStatus) {
                                        "Delivered" -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val progress = when (order.trackingStatus) {
                            "Order Placed" -> 0.25f
                            "Pending Verification" -> 0.4f
                            "Dispatched" -> 0.65f
                            "In Transit" -> 0.85f
                            "Delivered" -> 1.0f
                            else -> 0.5f
                        }

                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }

        // --- 7. SECURITY & CONFIDENTIALITY INDICATOR ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Security, contentDescription = "Security Info", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDisguised) {
                        "FlowTrack Private Offline Mode. Zero cloud logging."
                    } else {
                        "CareLink Secure Network: Zero logging, zero GPS tracking."
                    },
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }
    }

    // --- DIALOGS & SHIELDS ---

    // 1. ADD REMINDER DIALOG
    if (showAddReminderDialog) {
        var title by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var timeStr by remember { mutableStateOf("08:00 AM") }
        var isClinicPickup by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = { Text(if (isClinicPickup) "Add Clinic Refill" else "Add Daily Dose Reminder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = !isClinicPickup,
                            onClick = { isClinicPickup = false },
                            label = { Text("Daily Dose") }
                        )
                        FilterChip(
                            selected = isClinicPickup,
                            onClick = { isClinicPickup = true },
                            label = { Text("Clinic Refill") }
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (isClinicPickup) "Location / Name" else "Reminder Title") },
                        placeholder = { Text(if (isClinicPickup) "e.g. Korle Bu Pharmacy" else "e.g. Daily Vitamins") },
                        modifier = Modifier.fillMaxWidth().testTag("reminder_title_input")
                    )

                    OutlinedTextField(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = { Text(if (isClinicPickup) "Pickup Date" else "Dose Time") },
                        placeholder = { Text(if (isClinicPickup) "e.g. July 25, 2026" else "e.g. 08:00 AM") },
                        modifier = Modifier.fillMaxWidth().testTag("reminder_time_input")
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Confidential Notes") },
                        placeholder = { Text("e.g. Take with dinner") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addReminder(
                                title = title,
                                timeString = timeStr,
                                type = if (isClinicPickup) "CLINIC_PICKUP" else "DAILY_DOSE",
                                notes = notes
                            )
                            showAddReminderDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_reminder_button")
                ) {
                    Text("Add Reminder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. EDIT ALIAS DIALOG
    if (showEditAliasDialog) {
        var aliasInput by remember { mutableStateOf(profile?.aliasName ?: "") }
        AlertDialog(
            onDismissRequest = { showEditAliasDialog = false },
            title = { Text("Update Anonymized Alias") },
            text = {
                Column {
                    Text(
                        "CareLink enforces total privacy. Avoid using your real name. Choose a supportive, pseudonymous nickname.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = aliasInput,
                        onValueChange = { aliasInput = it },
                        label = { Text("Confidential Alias") },
                        modifier = Modifier.fillMaxWidth().testTag("alias_input_field"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (aliasInput.isNotBlank()) {
                            viewModel.updateAlias(aliasInput)
                            showEditAliasDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_alias_button")
                ) {
                    Text("Save Alias")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAliasDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. PRIVACY, THEME & ICON CUSTOMIZER DIALOG (GORGEOUS GRAPHICAL DISPLAY)
    if (showPrivacyDialog) {
        val selectedTheme = profile?.selectedTheme ?: "CLASSIC"
        val activeDisguise = profile?.isDisguised ?: false
        val selectedIcon = profile?.selectedIcon ?: "DEFAULT"

        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Privacy & App Disguise")
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)
                ) {
                    // STIGMA-RESISTANT APP DISGUISE TOGGLE
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "App Disguise Mode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Switch(
                                        checked = activeDisguise,
                                        onCheckedChange = { isChecked ->
                                            viewModel.toggleDisguiseMode(isChecked)
                                            if (isChecked && selectedTheme == "CLASSIC") {
                                                // Auto-switch to orchid theme for high-fidelity period cover look!
                                                viewModel.updateSelectedTheme("PINK_ORCHID")
                                            } else if (!isChecked && selectedTheme == "PINK_ORCHID") {
                                                viewModel.updateSelectedTheme("CLASSIC")
                                            }
                                        }
                                    )
                                }
                                Text(
                                    text = "When activated, the app completely rebrands as \"FlowTrack: Menstrual Cycle Tracker\". Icons, titles, and menus switch instantly to menstrual cycle themes to ensure zero stigma or public exposure.",
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // CENTRALIZED COLOR THEME SWITCHER
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Visual Comfort Themes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                data class CustomThemeOption(val id: String, val color: Color, val desc: String)
                                val themes = listOf(
                                    CustomThemeOption("CLASSIC", TealPrimary, "Classic Teal"),
                                    CustomThemeOption("PINK_ORCHID", PinkPrimary, "Orchid Pink"),
                                    CustomThemeOption("EMERALD", EmeraldPrimary, "Emerald Leaf"),
                                    CustomThemeOption("WARM_SUNSET", SunsetPrimary, "Warm Sunset"),
                                    CustomThemeOption("DARK_SLATE", DarkPrimary, "Dark Slate")
                                )

                                themes.forEach { themeOpt ->
                                    val name = themeOpt.id
                                    val color = themeOpt.color
                                    val desc = themeOpt.desc
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (selectedTheme == name) 3.dp else 1.dp,
                                                color = if (selectedTheme == name) MaterialTheme.colorScheme.onBackground else Color.LightGray.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                            .clickable { viewModel.updateSelectedTheme(name) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedTheme == name) {
                                            Icon(Icons.Default.Check, contentDescription = desc, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // LAUNCHER ICON REPLACEMENT (NO DEAD-END: EXPLAINING AND SIMULATING COMPONENT CHANGE)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Launcher Icon Disguise", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Choose a subtle design for your home screen icon to make the app's function completely invisible on your app tray.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                data class IconOption(val id: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)
                                val icons = listOf(
                                    IconOption("DEFAULT", Icons.Default.HealthAndSafety, "Shield"),
                                    IconOption("LOTUS", Icons.Default.Favorite, "Lotus"),
                                    IconOption("WAVE", Icons.Default.Star, "Star")
                                )

                                icons.forEach { iconOpt ->
                                    val name = iconOpt.id
                                    val icon = iconOpt.icon
                                    val label = iconOpt.label
                                    OutlinedCard(
                                        onClick = { viewModel.updateSelectedIcon(name) },
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = if (selectedIcon == name) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) else Color.Transparent
                                        ),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            width = if (selectedIcon == name) 2.dp else 1.dp
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }

                            // STIGMA-RESISTANT HOME SCREEN PREVIEW GRID (HIGH ARTISTRY MOCKUP)
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Home Screen Preview", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Fake app 1
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF3B82F6)), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                            Text("Chat", fontSize = 9.sp, color = Color.Gray)
                                        }
                                        // Fake app 2
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF10B981)), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.LocalMall, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                            Text("Shop", fontSize = 9.sp, color = Color.Gray)
                                        }
                                        // ACTIVE APLET ICON Mockup!
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            val activeAppColor = when (selectedTheme) {
                                                "PINK_ORCHID" -> PinkPrimary
                                                "EMERALD" -> EmeraldPrimary
                                                "WARM_SUNSET" -> SunsetPrimary
                                                "DARK_SLATE" -> DarkPrimary
                                                else -> TealPrimary
                                            }
                                            val activeAppIcon = when (selectedIcon) {
                                                "LOTUS" -> Icons.Default.Favorite
                                                "WAVE" -> Icons.Default.Star
                                                else -> Icons.Default.HealthAndSafety
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(activeAppColor)
                                                    .border(2.dp, Color.White, RoundedCornerShape(10.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(activeAppIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                            Text(
                                                text = if (activeDisguise) "FlowTrack" else "CareLink",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }
                                        // Fake app 3
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF59E0B)), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Photo, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                            Text("Photos", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) {
                    Text("Apply & Close")
                }
            }
        )
    }

    // 4. PERIOD FLOW LOGGING DIALOG
    if (showLogPeriodDialog) {
        var flowSelection by remember { mutableStateOf("Medium") }
        var moodSelection by remember { mutableStateOf("Calm") }
        val selectedSymptoms = remember { mutableStateListOf<String>() }
        var notesInput by remember { mutableStateOf("") }

        val symptomOptions = listOf("Cramps", "Headache", "Bloating", "Mood Swings", "Fatigue", "Nausea")
        val flowOptions = listOf("None", "Light", "Medium", "Heavy")
        val moodOptions = listOf("Happy", "Calm", "Anxious", "Moody", "Tired")

        AlertDialog(
            onDismissRequest = { showLogPeriodDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Log Period Flow & Symptoms")
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)
                ) {
                    // FLOW INTENSITY SELECTION
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Flow Intensity:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                flowOptions.forEach { flow ->
                                    FilterChip(
                                        selected = flowSelection == flow,
                                        onClick = { flowSelection = flow },
                                        label = { Text(flow, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // MOOD SELECTION
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Current Mood:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                moodOptions.forEach { m ->
                                    FilterChip(
                                        selected = moodSelection == m,
                                        onClick = { moodSelection = m },
                                        label = { Text(m, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // SYMPTOMS CHECKBOXES / CHIPS
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Symptoms:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                mainAxisSpacing = 6.dp,
                                crossAxisSpacing = 6.dp
                            ) {
                                symptomOptions.forEach { symptom ->
                                    val isSelected = selectedSymptoms.contains(symptom)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (isSelected) selectedSymptoms.remove(symptom)
                                            else selectedSymptoms.add(symptom)
                                        },
                                        label = { Text(symptom, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // SUPPORTIVE NOTES
                    item {
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Private Cycle Notes") },
                            placeholder = { Text("e.g. Cramps improved after warm bath.") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val symptomsString = if (selectedSymptoms.isEmpty()) "None" else selectedSymptoms.joinToString(", ")
                        viewModel.addPeriodLog(
                            dateMillis = System.currentTimeMillis(),
                            flowIntensity = flowSelection,
                            symptoms = symptomsString,
                            mood = moodSelection,
                            notes = notesInput
                        )
                        showLogPeriodDialog = false
                    },
                    modifier = Modifier.testTag("submit_period_log")
                ) {
                    Text("Save Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogPeriodDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WelcomeHeaderCard(
    user: AnonymizedUser,
    onEditAliasClick: () -> Unit,
    onPrivacySettingsClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👋", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hello, ${user.aliasName}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Alias",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onEditAliasClick() }
                            )
                        }
                        Text(
                            text = "Anonymized ID: ${user.hashedId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { onPrivacySettingsClick() },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                            .testTag("disguise_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Themes & Disguise",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Encrypted",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "SECURE",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// MENSTRUAL CYCLE CALCULATION AND TIMELINE INTERFACE
@Composable
fun PeriodTrackerSection(
    periodLogs: List<PeriodLog>,
    isDisguised: Boolean,
    onLogPeriodClick: () -> Unit,
    onDeleteLog: (Int) -> Unit
) {
    // Elegant menstrual tracker widget
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Cycle Tracker",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Menstrual Cycle Tracker",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = { onLogPeriodClick() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp).testTag("log_flow_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Flow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // DYNAMIC STATISTICS & MATHEMATICAL CYCLE PREDICTION
            if (periodLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Welcome to your Cycle Companion 🌸",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Log your flow periods to predict next cycle dates, determine fertile windows, and identify wellness trends anonymously.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                }
            } else {
                val latestLog = periodLogs.first()
                val diffDays = (System.currentTimeMillis() - latestLog.dateMillis) / (24 * 60 * 60 * 1000L)
                val predictedDays = (28 - (diffDays % 28)).coerceAtLeast(0)

                val statePhase = when {
                    diffDays < 4 -> "Period Phase 🩸"
                    diffDays in 12..15 -> "Ovulatory Phase (Fertile Window) ✨"
                    diffDays in 5..11 -> "Follicular Phase ⚡"
                    else -> "Luteal Phase (Pre-Menstrual) 🌙"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Predictions Circle
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (predictedDays == 0L) "Period Today" else "$predictedDays Days",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "Until next period",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Cycle Phase Info
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = statePhase,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            )
                            Text(
                                text = "Cycle Day ${diffDays + 1} • Predicted 28-day cycle",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // COLLAPSIBLE TIMELINE OF RECENT FLOW LOGS
                var isTimelineExpanded by remember { mutableStateOf(false) }

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isTimelineExpanded = !isTimelineExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View Log History (${periodLogs.size} logs)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isTimelineExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = isTimelineExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            periodLogs.take(5).forEach { log ->
                                val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(log.dateMillis))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text("🩸", fontSize = 12.sp)
                                                Text(
                                                    text = "Flow: ${log.flowIntensity}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text("•", fontSize = 12.sp, color = Color.Gray)
                                                Text(
                                                    text = "Mood: ${log.mood}",
                                                    fontSize = 11.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                            if (log.symptoms.isNotBlank() && log.symptoms != "None") {
                                                Text(
                                                    text = "Symptoms: ${log.symptoms}",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            if (log.notes.isNotBlank()) {
                                                Text(
                                                    text = "\"${log.notes}\"",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Text(
                                                text = dateStr,
                                                fontSize = 9.sp,
                                                color = Color.LightGray
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteLog(log.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete log", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// MULTI-ROW FLOW LAYOUT HELPER FOR FILTERS / SYMPTOM CHIPS
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val mainAxisSpacingPx = mainAxisSpacing.roundToPx()
        val crossAxisSpacingPx = crossAxisSpacing.roundToPx()

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        placeables.forEach { placeable ->
            if (currentRowWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + mainAxisSpacingPx
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val height = rows.sumOf { row -> row.maxOf { it.height } } + (rows.size - 1) * crossAxisSpacingPx
        val width = constraints.maxWidth

        layout(width, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOf { it.height }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + mainAxisSpacingPx
                }
                y += rowHeight + crossAxisSpacingPx
            }
        }
    }
}
