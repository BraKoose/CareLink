package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import com.example.ui.CareLinkViewModel
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

    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showEditAliasDialog by remember { mutableStateOf(false) }

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
                    onEditAliasClick = { showEditAliasDialog = true }
                )
            }
        }

        // --- 2. TODAY'S DAILY PLAN (DEAD SIMPLE COMBINED CHECKLIST) ---
        item {
            Text(
                text = "Today's Checklist 🗓️",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Pill Log Card
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
                                text = "Daily Medication Log",
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
                            text = "Log today's PrEP or antiretroviral intake to keep up your protective streak!",
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
                            text = if (wasTaken) "Beautiful work! Your health is your power. 💪 Keep going!"
                            else "It's completely okay. Adherence is a journey — tomorrow is a fresh start! ❤️",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = if (wasTaken) MaterialTheme.colorScheme.secondary else Color.Gray
                        )
                    }
                }
            }
        }

        // Other custom reminders / clinic pickups
        if (reminders.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Upcoming Reminders",
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
                                    text = reminder.title,
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

        // --- 3. QUICK SUPPORT SPACES (LARGE, BEAUTIFUL CLICKABLE GRID) ---
        item {
            Text(
                text = "Your Support Spaces 🌟",
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
                            text = "AI Coach",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ask private health questions 100% securely.",
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
                            text = "Wellness Shop",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Order self-test kits & medications anonymously.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // --- 4. HEALTH ASSESSMENT ACCENT BANNER ---
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
                            text = "Let's Check Your Wellness! 🎯",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Take our 1-minute, private, supportive quiz to understand your health needs.",
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
                            Text("Start Health Quiz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("✨", fontSize = 38.sp)
                }
            }
        }

        // --- 5. DISCREET DELIVERIES TRACKER (ONLY SHOWN IF ACTIVE ORDER EXISTS) ---
        if (orders.isNotEmpty()) {
            item {
                Text(
                    text = "Discreet Deliveries",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(orders) { order ->
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
                                    text = order.itemName,
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

        // --- 6. ON-DEVICE SECURITY FOOTNOTE ---
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
                    text = "CareLink Secure Network: Zero logging, zero GPS tracking.",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }
    }

    // --- REMINDER ADD DIALOG ---
    if (showAddReminderDialog) {
        var title by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var timeStr by remember { mutableStateOf("08:00 AM") }
        var isClinicPickup by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = { Text(if (isClinicPickup) "Add Clinic Pickup" else "Add Daily Pill Reminder") },
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
                        label = { Text(if (isClinicPickup) "Pickup Location" else "Reminder Title") },
                        placeholder = { Text(if (isClinicPickup) "e.g. Korle Bu Pharmacy" else "e.g. Daily PrEP") },
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

    // --- ALIAS EDIT DIALOG ---
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
}

@Composable
fun WelcomeHeaderCard(
    user: AnonymizedUser,
    onEditAliasClick: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            text = "Confidential ID: ${user.hashedId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
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
                            text = "Encrypted",
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
