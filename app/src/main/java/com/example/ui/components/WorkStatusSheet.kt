package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.EiSeedDefaults
import com.example.data.model.TeamMember
import com.example.data.model.WorkPositionStatus
import com.example.data.model.WorkStatusLog
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoSurface
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PurplePdf
import com.example.ui.theme.PurpleSurface
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseSurface
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant

fun workStatusVisualStyle(status: WorkPositionStatus): Triple<Color, Color, ImageVector> = when (status) {
    WorkPositionStatus.KANTOR -> Triple(EmeraldSurface, EmeraldSuccess, Icons.Rounded.Apartment)
    WorkPositionStatus.WORKSHOP -> Triple(CobaltLight, CobaltPrimary, Icons.Rounded.Build)
    WorkPositionStatus.LUAR_AREA_KERJA -> Triple(AmberSurface, AmberWarning, Icons.Rounded.LocationOn)
    WorkPositionStatus.DINAS_LUAR_KOTA -> Triple(IndigoSurface, IndigoAccent, Icons.Rounded.FlightTakeoff)
    WorkPositionStatus.CUTI -> Triple(PurpleSurface, PurplePdf, Icons.Rounded.EventBusy)
    WorkPositionStatus.SAKIT -> Triple(RoseSurface, RoseAlert, Icons.Rounded.LocalHospital)
    WorkPositionStatus.IZIN -> Triple(AmberSurface, AmberWarning, Icons.Rounded.WorkOutline)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkStatusSheet(
    teamMembers: List<TeamMember>,
    workStatusLogs: List<WorkStatusLog>,
    defaultPersonName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmitStatus: (
        personName: String,
        workStatus: WorkPositionStatus,
        destinationNote: String
    ) -> Unit,
    initialPersonName: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val availableNames = remember(teamMembers, defaultPersonName, initialPersonName) {
        buildList {
            val cleanInitial = initialPersonName?.let { EiSeedDefaults.sanitizePersonNameOnly(it) }.orEmpty()
            if (cleanInitial.isNotBlank()) add(cleanInitial)
            val cleanDefault = EiSeedDefaults.sanitizePersonNameOnly(defaultPersonName)
            if (cleanDefault.isNotBlank() && !any { it.equals(cleanDefault, ignoreCase = true) }) {
                add(cleanDefault)
            }
            teamMembers.map { EiSeedDefaults.sanitizePersonNameOnly(it.name) }.forEach { name ->
                if (name.isNotBlank() && !any { it.equals(name, ignoreCase = true) }) {
                    add(name)
                }
            }
            if (isEmpty()) add(EiSeedDefaults.SUPERVISOR_NAME)
        }
    }

    var selectedPerson by remember(defaultPersonName, initialPersonName, availableNames) {
        val target = initialPersonName?.takeIf { it.isNotBlank() } ?: defaultPersonName
        val cleanTarget = EiSeedDefaults.sanitizePersonNameOnly(target)
        mutableStateOf(
            availableNames.firstOrNull { it.equals(cleanTarget, ignoreCase = true) }
                ?: availableNames.first()
        )
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val matchingMember = remember(selectedPerson, teamMembers) {
        teamMembers.firstOrNull {
            EiSeedDefaults.sanitizePersonNameOnly(it.name).equals(selectedPerson, ignoreCase = true)
        }
    }
    var selectedStatus by remember(selectedPerson, matchingMember?.status) {
        mutableStateOf(WorkPositionStatus.fromLabel(matchingMember?.status))
    }
    var destinationNote by remember(selectedPerson, matchingMember?.workNote) {
        mutableStateOf(matchingMember?.workNote.orEmpty())
    }
    var showNoteError by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Status Posisi Kerja",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Laporkan posisi kerja terkini agar terpantau seluruh team",
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
                        .testTag("close_work_status_modal")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup Status Posisi Kerja"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedPerson,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nama Personel / PIC") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("work_status_person_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    availableNames.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selectedPerson = name
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Pilih Status Posisi Kerja",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                WorkPositionStatus.entries.forEach { option ->
                    val isSelected = selectedStatus == option
                    val (bgColor, fgColor, icon) = workStatusVisualStyle(option)
                    Surface(
                        color = if (isSelected) bgColor else SlateSurfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) fgColor else SlateBorder
                        ),
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedStatus = option
                                showNoteError = false
                                if (!option.requiresDestinationNote) {
                                    destinationNote = ""
                                }
                            }
                            .testTag("work_status_option_${option.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) fgColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) fgColor else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (option.requiresDestinationNote) {
                                    Text(
                                        text = "Isi tujuan / kemana",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Dipilih",
                                    tint = fgColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (selectedStatus.requiresDestinationNote) {
                Spacer(modifier = Modifier.height(14.dp))
                val noteLabel = when (selectedStatus) {
                    WorkPositionStatus.LUAR_AREA_KERJA -> "Keterangan Kemana (Lokasi Luar Area Kerja) *"
                    WorkPositionStatus.DINAS_LUAR_KOTA -> "Keterangan Kemana (Kota / Site Tujuan Dinas) *"
                    WorkPositionStatus.IZIN -> "Keterangan Kemana / Alasan Izin *"
                    else -> "Keterangan Tujuan *"
                }
                val notePlaceholder = when (selectedStatus) {
                    WorkPositionStatus.LUAR_AREA_KERJA -> "Contoh: Site Plant B / Warehouse Eksternal"
                    WorkPositionStatus.DINAS_LUAR_KOTA -> "Contoh: Surabaya / Balikpapan - Commissioning"
                    WorkPositionStatus.IZIN -> "Contoh: Izin urusan keluarga / kontrol medis"
                    else -> "Masukkan keterangan tujuan"
                }
                OutlinedTextField(
                    value = destinationNote,
                    onValueChange = {
                        destinationNote = it
                        if (it.isNotBlank()) showNoteError = false
                    },
                    label = { Text(noteLabel) },
                    placeholder = { Text(notePlaceholder) },
                    isError = showNoteError,
                    supportingText = if (showNoteError) {
                        { Text("Keterangan kemana wajib diisi untuk status ${selectedStatus.label}.") }
                    } else null,
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("work_status_note_input")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val trimmedNote = if (selectedStatus.requiresDestinationNote) {
                        destinationNote.trim()
                    } else {
                        ""
                    }
                    if (selectedStatus.requiresDestinationNote && trimmedNote.isEmpty()) {
                        showNoteError = true
                    } else {
                        onSubmitStatus(selectedPerson, selectedStatus, trimmedNote)
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
                    .testTag("submit_work_status_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Simpan Status Posisi Kerja", fontWeight = FontWeight.Bold)
                }
            }

            if (workStatusLogs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "Riwayat Status Posisi Team (${workStatusLogs.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    workStatusLogs.take(12).forEach { log ->
                        val statusEnum = WorkPositionStatus.fromLabel(log.workStatus)
                        val (badgeBg, badgeFg, icon) = workStatusVisualStyle(statusEnum)
                        Surface(
                            color = SlateSurfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(badgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = statusEnum.label,
                                            tint = badgeFg,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = log.personName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (log.destinationNote.isNotBlank()) {
                                            Text(
                                                text = "Tujuan: ${log.destinationNote}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(color = badgeBg, shape = RoundedCornerShape(50)) {
                                        Text(
                                            text = statusEnum.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeFg,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    if (log.timeLabel.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = log.timeLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMonoFamily
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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
