package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BriefingNotice
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseSurface
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant

private val PRESET_BRIEFING_BADGES = listOf(
    "SHIFT BRIEFING",
    "PREVENTIVE SCHEDULE",
    "SAFETY REMINDER",
    "URGENT INFO"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BriefingEditSheet(
    existingBriefing: BriefingNotice?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        badge: String,
        timeTag: String,
        title: String,
        summary: String,
        isSafetyAlert: Boolean
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isEditMode = existingBriefing != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var badge by remember(existingBriefing) {
        mutableStateOf(existingBriefing?.badge ?: "SHIFT BRIEFING")
    }
    var timeTag by remember(existingBriefing) {
        mutableStateOf(existingBriefing?.timeTag ?: "Hari Ini")
    }
    var title by remember(existingBriefing) {
        mutableStateOf(existingBriefing?.title ?: "")
    }
    var summary by remember(existingBriefing) {
        mutableStateOf(existingBriefing?.summary ?: "")
    }
    var isSafetyAlert by remember(existingBriefing) {
        mutableStateOf(existingBriefing?.isSafetyAlert ?: false)
    }
    var validationError by remember { mutableStateOf<String?>(null) }

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
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = if (isSafetyAlert) RoseSurface else CobaltLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isEditMode) "EDIT INFO BRIEFING" else "TAMBAH INFO BRIEFING",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSafetyAlert) RoseAlert else CobaltPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isEditMode) "Ubah Info & Briefing Shift" else "Buat Info Briefing Baru",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SlateSurfaceVariant)
                        .testTag("close_briefing_modal")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup modal briefing")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Kategori Label",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PRESET_BRIEFING_BADGES.forEach { preset ->
                    FilterChip(
                        selected = badge.equals(preset, ignoreCase = true),
                        onClick = {
                            badge = preset
                            if (preset == "SAFETY REMINDER" || preset == "URGENT INFO") {
                                isSafetyAlert = true
                            }
                        },
                        label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isSafetyAlert) RoseAlert else CobaltPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = badge,
                    onValueChange = {
                        badge = it.take(40)
                        validationError = null
                    },
                    label = { Text("Badge Label") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("briefing_badge_input")
                )
                OutlinedTextField(
                    value = timeTag,
                    onValueChange = {
                        timeTag = it.take(40)
                        validationError = null
                    },
                    label = { Text("Waktu / Shift") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("briefing_time_input")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it.take(100)
                    validationError = null
                },
                label = { Text("Judul Briefing") },
                placeholder = { Text("Contoh: Fokus Maintenance Compressor Unit 02") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("briefing_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = summary,
                onValueChange = {
                    summary = it.take(500)
                    validationError = null
                },
                label = { Text("Isi / Arahan Briefing") },
                placeholder = { Text("Tulis instruksi kerja, prioritas alat, atau pengingat safety...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("briefing_summary_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = if (isSafetyAlert) RoseSurface else SlateSurfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (isSafetyAlert) RoseAlert.copy(alpha = 0.4f) else SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = if (isSafetyAlert) RoseAlert else CobaltPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tandai Peringatan Safety / Prioritas",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Menampilkan badge warna merah pada slider briefing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isSafetyAlert,
                        onCheckedChange = { isSafetyAlert = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = RoseAlert
                        ),
                        modifier = Modifier.testTag("briefing_safety_switch")
                    )
                }
            }

            if (validationError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = validationError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = RoseAlert
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    when {
                        badge.isBlank() -> validationError = "Badge label wajib diisi."
                        timeTag.isBlank() -> validationError = "Waktu / Shift wajib diisi."
                        title.isBlank() -> validationError = "Judul briefing wajib diisi."
                        summary.isBlank() -> validationError = "Isi arahan briefing wajib diisi."
                        else -> onSave(
                            badge.trim(),
                            timeTag.trim(),
                            title.trim(),
                            summary.trim(),
                            isSafetyAlert
                        )
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CobaltPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("briefing_save_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Simpan Perubahan Briefing" else "Tambahkan Info Briefing",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isEditMode && onDelete != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDelete,
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("briefing_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = RoseAlert,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hapus Info Briefing", color = RoseAlert, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
