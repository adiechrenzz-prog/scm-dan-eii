package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BriefingNotice
import com.example.data.model.DailyWorkReport
import com.example.data.model.EiSeedDefaults
import com.example.data.model.ScmJob
import com.example.data.model.TeamMember
import com.example.data.model.WorkOrder
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant
import com.example.util.WhatsAppReminderManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppReminderSheet(
    currentUserName: String = "",
    teamMembers: List<TeamMember>,
    workOrders: List<WorkOrder>,
    dailyWorkReports: List<DailyWorkReport>,
    scmJobs: List<ScmJob>,
    briefings: List<BriefingNotice>,
    onDismiss: () -> Unit,
    onEditMember: (TeamMember) -> Unit,
    onToast: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val initialSender = remember(currentUserName, teamMembers) {
        WhatsAppReminderManager.resolveSender(context, currentUserName, teamMembers)
    }
    var reminderEnabled by remember { mutableStateOf(WhatsAppReminderManager.isReminderEnabled(context)) }
    var senderName by remember { mutableStateOf(initialSender.first) }
    var senderNumber by remember { mutableStateOf(initialSender.second) }
    var senderDropdownExpanded by remember { mutableStateOf(false) }

    val senderChoices = remember(currentUserName, teamMembers) {
        val cleanLogin = EiSeedDefaults.sanitizePersonNameOnly(currentUserName)
            .ifBlank { currentUserName.trim() }
        val list = mutableListOf<Triple<String, String, String>>() // name, phone, subtitle
        if (cleanLogin.isNotBlank()) {
            val matched = teamMembers.firstOrNull {
                EiSeedDefaults.sanitizePersonNameOnly(it.name).equals(cleanLogin, ignoreCase = true)
            }
            list.add(
                Triple(
                    cleanLogin,
                    matched?.whatsappNumber.orEmpty(),
                    "User Login"
                )
            )
        }
        teamMembers.forEach { member ->
            val cleanMember = EiSeedDefaults.sanitizePersonNameOnly(member.name).ifBlank { member.name }
            if (list.none { it.first.equals(cleanMember, ignoreCase = true) }) {
                list.add(
                    Triple(
                        cleanMember,
                        member.whatsappNumber,
                        member.role
                    )
                )
            }
        }
        list.toList()
    }

    var customMessage by remember(workOrders, dailyWorkReports, scmJobs, briefings, senderName, senderNumber) {
        mutableStateOf(
            WhatsAppReminderManager.buildActivitySummaryMessage(
                senderNumber = senderNumber,
                workOrders = workOrders,
                dailyWorkReports = dailyWorkReports,
                scmJobs = scmJobs,
                briefings = briefings,
                senderName = senderName
            )
        )
    }

    val membersWithWa = remember(teamMembers) {
        teamMembers.filter { EiSeedDefaults.formatWhatsappWaId(it.whatsappNumber).isNotEmpty() }
    }
    var sequentialIndex by remember { mutableIntStateOf(0) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            reminderEnabled = true
            WhatsAppReminderManager.setReminderEnabled(context, true)
            onToast("Pengingat harian jam 16:00 WIB aktif!")
        } else {
            onToast("Izin notifikasi ditolak; aktifkan izin notifikasi agar alarm 16:00 tampil.")
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reminder WhatsApp 16:00 WIB",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Kirim rekap aktivitas EII, Harian, SCM & Briefing dari nomor pengirim.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SlateSurfaceVariant)
                        .testTag("close_wa_reminder_modal")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup reminder WhatsApp")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daily 16:00 Schedule Toggle Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (reminderEnabled) EmeraldSurface else SlateSurfaceVariant
                ),
                border = BorderStroke(
                    1.dp,
                    if (reminderEnabled) EmeraldBorder else SlateBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (reminderEnabled) EmeraldSuccess else Color(0xFF64748B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AlarmOn,
                                    contentDescription = "Alarm 16:00",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pengingat Otomatis Jam 16:00",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (reminderEnabled) {
                                        "Aktif setiap hari pukul 16:00 WIB"
                                    } else {
                                        "Nonaktif — geser untuk menjadwalkan"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (reminderEnabled) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { checked ->
                                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !WhatsAppReminderManager.hasNotificationPermission(context)
                                ) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    reminderEnabled = checked
                                    WhatsAppReminderManager.setReminderEnabled(context, checked)
                                    onToast(
                                        if (checked) {
                                            "Pengingat otomatis 16:00 WIB diaktifkan."
                                        } else {
                                            "Pengingat otomatis 16:00 WIB dimatikan."
                                        }
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldSuccess
                            ),
                            modifier = Modifier.testTag("wa_reminder_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !WhatsAppReminderManager.hasNotificationPermission(context)
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                WhatsAppReminderManager.postReminderNotification(context)
                                onToast("Notifikasi reminder 16:00 berhasil dikirim ke bilah notifikasi.")
                            }
                        },
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wa_test_notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tes Notifikasi Reminder 16:00 Sekarang",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sender Name Dropdown (Logged-in User + Registered Team Members)
            ExposedDropdownMenuBox(
                expanded = senderDropdownExpanded,
                onExpandedChange = { senderDropdownExpanded = !senderDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = senderName.ifBlank { "Pilih Pengirim dari Tab Team" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nama Pengirim (Akun Login / Team)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = CobaltPrimary
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = senderDropdownExpanded)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("wa_sender_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = senderDropdownExpanded,
                    onDismissRequest = { senderDropdownExpanded = false }
                ) {
                    senderChoices.forEach { (choiceName, choiceNumber, roleLabel) ->
                        val phoneText = choiceNumber.ifBlank { "Belum ada no. WA" }
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = choiceName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "$roleLabel • $phoneText",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (choiceNumber.isNotBlank()) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                senderName = choiceName
                                senderNumber = choiceNumber
                                WhatsAppReminderManager.setSenderSelection(context, choiceName, choiceNumber)
                                senderDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sender WhatsApp Number
            OutlinedTextField(
                value = senderNumber,
                onValueChange = { raw ->
                    val cleaned = EiSeedDefaults.normalizeWhatsappDisplay(raw)
                    senderNumber = cleaned
                    WhatsAppReminderManager.setSenderSelection(context, senderName, cleaned)
                },
                label = { Text("Nomor WhatsApp Pengirim") },
                placeholder = { Text("Isi nomor WA pengirim (opsional jika dari daftar team)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = null,
                        tint = CobaltPrimary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wa_sender_number_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Editable Summary Message
            OutlinedTextField(
                value = customMessage,
                onValueChange = { customMessage = it },
                label = { Text("Ringkasan Pesan Aktivitas Team") },
                minLines = 5,
                maxLines = 9,
                shape = RoundedCornerShape(14.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wa_summary_message_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Broadcast & Copy Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        WhatsAppReminderManager.shareBroadcastMessage(context, customMessage)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSuccess,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("wa_share_group_button")
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kirim ke Grup WA", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(customMessage))
                        onToast("Ringkasan aktivitas disalin ke clipboard.")
                    },
                    border = BorderStroke(1.dp, CobaltPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("wa_copy_message_button")
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = CobaltPrimary, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin", color = CobaltPrimary)
                }
            }

            if (membersWithWa.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                val targetMember = membersWithWa[sequentialIndex % membersWithWa.size]
                Button(
                    onClick = {
                        val opened = WhatsAppReminderManager.openWhatsAppForMember(
                            context = context,
                            targetPhone = targetMember.whatsappNumber,
                            message = customMessage
                        )
                        if (opened) {
                            sequentialIndex = (sequentialIndex + 1) % membersWithWa.size
                        } else {
                            onToast("WhatsApp tidak ditemukan di perangkat.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CobaltPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("wa_sequential_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kirim Berurutan -> ${targetMember.name} (${(sequentialIndex % membersWithWa.size) + 1}/${membersWithWa.size})",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SlateBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Daftar Nomor WhatsApp Team (${membersWithWa.size}/${teamMembers.size} terisi)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            teamMembers.forEach { member ->
                val waId = EiSeedDefaults.formatWhatsappWaId(member.whatsappNumber)
                val hasValidWa = waId.isNotEmpty()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (hasValidWa) {
                                    "WA: ${member.whatsappNumber}"
                                } else {
                                    "Nomor WA belum diisi"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (hasValidWa) EmeraldSuccess else AmberWarning
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (hasValidWa) {
                            Surface(
                                color = EmeraldSurface,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, EmeraldBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        val ok = WhatsAppReminderManager.openWhatsAppForMember(
                                            context = context,
                                            targetPhone = member.whatsappNumber,
                                            message = customMessage
                                        )
                                        if (!ok) {
                                            onToast("Gagal membuka WhatsApp untuk ${member.name}")
                                        }
                                    }
                                    .testTag("wa_send_member_${member.id.ifEmpty { member.name }}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = "Kirim WA ke ${member.name}",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Kirim WA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = AmberSurface,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onEditMember(member) }
                            ) {
                                Text(
                                    text = "+ Isi Nomor",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberWarning,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = CobaltLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = CobaltPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Setiap hari pukul 16:00 WIB aplikasi memicu notifikasi pengingat otomatis. Ketuk notifikasi untuk membuka WhatsApp dari pengirim (${WhatsAppReminderManager.formatSenderLabel(senderName, senderNumber)}) dengan ringkasan aktivitas siap kirim.",
                        style = MaterialTheme.typography.labelSmall,
                        color = CobaltPrimary
                    )
                }
            }
        }
    }
}
