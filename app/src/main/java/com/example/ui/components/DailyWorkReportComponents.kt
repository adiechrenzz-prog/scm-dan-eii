package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DailyReportCategory
import com.example.data.model.DailyReportStatus
import com.example.data.model.DailyWorkReport
import com.example.data.model.EiSeedDefaults
import com.example.data.model.EiSortHelpers
import com.example.data.model.TeamMember
import com.example.ui.theme.AmberBorder
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanSurface
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoSurface
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.PurplePdf
import com.example.ui.theme.PurpleSurface
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseBorder
import com.example.ui.theme.RoseSurface
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyWorkReportTimelineTable(
    reports: List<DailyWorkReport>,
    onRowClick: (DailyWorkReport) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedReports = remember(reports) {
        EiSortHelpers.sortDailyWorkReports(reports)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_report_timeline_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.93f)),
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tabel Daily Work Report",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ringkasan kronologis Waktu • Aktivitas • Status",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    onClick = onAddClick,
                    color = CobaltLight,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CobaltContainer),
                    modifier = Modifier.testTag("daily_table_add_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Tambah laporan harian",
                            tint = CobaltPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Isi",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CobaltPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table Header Row
            Surface(
                color = CobaltPrimary,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Waktu",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.width(90.dp)
                    )
                    Text(
                        text = "Aktivitas",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.width(64.dp)
                    )
                }
            }

            if (sortedReports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada aktivitas harian tercatat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                sortedReports.forEachIndexed { idx, item ->
                    val statusEnum = DailyReportStatus.fromLabel(item.status)
                    val (statusBg, statusFg) = dailyStatusStyle(statusEnum)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (idx % 2 == 1) SlateSurfaceVariant.copy(alpha = 0.55f) else Color.Transparent)
                            .clickable { onRowClick(item) }
                            .padding(horizontal = 10.dp, vertical = 9.dp)
                            .testTag("daily_timeline_row_${item.reportCode}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.width(94.dp).padding(end = 6.dp)) {
                            Text(
                                text = item.formattedTimeRange(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = CobaltPrimary,
                                maxLines = 1
                            )
                            if (item.workDate.isNotBlank()) {
                                Text(
                                    text = item.workDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = item.activityDescription.ifBlank { item.equipmentName },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${EiSeedDefaults.sanitizePersonNameOnly(item.technicianName)} • ${item.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            color = statusBg,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.width(64.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = statusEnum.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusFg,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    if (idx < sortedReports.lastIndex) {
                        HorizontalDivider(color = SlateBorder.copy(alpha = 0.45f))
                    }
                }
            }
        }
    }
}

@Composable
fun DailyWorkReportCard(
    report: DailyWorkReport,
    onCycleStatus: () -> Unit,
    onOpenPhotoSheet: () -> Unit,
    onEditReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = DailyReportCategory.fromLabel(report.category)
    val status = DailyReportStatus.fromLabel(report.status)
    val (catBg, catFg, catBorder) = dailyCategoryStyle(category)
    val (statusBg, statusFg) = dailyStatusStyle(status)
    val photos = report.resolvedPhotos()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenPhotoSheet)
            .testTag("daily_report_card_${report.reportCode}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.93f)),
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = catBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, catBorder)
                ) {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = catFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = CobaltLight,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, CobaltContainer),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onEditReport)
                            .testTag("daily_edit_${report.reportCode}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit laporan harian ${report.reportCode}",
                                tint = CobaltPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelSmall,
                                color = CobaltPrimary
                            )
                        }
                    }

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onCycleStatus)
                            .testTag("daily_status_${report.reportCode}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = status.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusFg
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Ubah status laporan harian",
                                tint = statusFg,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time + Equipment header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CobaltLight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = CobaltPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${report.formattedTimeRange()} WIB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = CobaltPrimary
                        )
                    }
                }
                Text(
                    text = "${report.reportCode} • ${report.workDate}",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.activityDescription,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = SlateSurfaceVariant, shape = RoundedCornerShape(6.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Build,
                            contentDescription = null,
                            tint = CobaltPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = report.equipmentName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Surface(
                    color = SlateSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = report.workLocation,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircleOutline,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Hasil: ${report.workResult}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (report.issuesFound.isNotBlank() && report.issuesFound != "-") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Rounded.ReportProblem,
                        contentDescription = null,
                        tint = AmberWarning,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Kendala: ${report.issuesFound}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            WorkOrderMiniPhotoBadge(
                label = "Foto Dokumentasi",
                photoUris = photos,
                onClick = onOpenPhotoSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_photo_chip_${report.reportCode}")
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SlateBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = EiSeedDefaults.sanitizePersonNameOnly(report.technicianName),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Foto: ${photos.size}/4",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (photos.isNotEmpty()) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyWorkReportInputSheet(
    initialCategory: DailyReportCategory,
    teamMembers: List<TeamMember>,
    defaultTechnicianName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        workDate: String,
        technicianName: String,
        category: DailyReportCategory,
        equipmentName: String,
        workLocation: String,
        activityDescription: String,
        startTime: String,
        endTime: String,
        workResult: String,
        status: DailyReportStatus,
        issuesFound: String,
        photoUris: List<String>
    ) -> Unit,
    existingReport: DailyWorkReport? = null,
    onDelete: (() -> Unit)? = null
) {
    val isEditMode = existingReport != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val todayLabel = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date())
    }

    val executorNames = remember(teamMembers, defaultTechnicianName) {
        val fromTeam = teamMembers
            .map { EiSeedDefaults.sanitizePersonNameOnly(it.name) }
            .filter { it.isNotBlank() }
        val cleanDefault = EiSeedDefaults.sanitizePersonNameOnly(defaultTechnicianName)
        (if (cleanDefault.isNotBlank()) listOf(cleanDefault) + fromTeam else fromTeam)
            .distinct()
            .ifEmpty { EiSeedDefaults.technicianOptions.map { it.first } }
    }

    // 12 fields required by user
    var workDate by remember(existingReport) {
        mutableStateOf(existingReport?.workDate?.takeIf { it.isNotBlank() } ?: todayLabel)
    }
    var technicianName by remember(existingReport, executorNames) {
        val existingTech = existingReport?.technicianName
            ?.let { EiSeedDefaults.sanitizePersonNameOnly(it) }
            .orEmpty()
        mutableStateOf(existingTech.ifBlank { executorNames.first() })
    }
    var selectedCategory by remember(existingReport, initialCategory) {
        mutableStateOf(existingReport?.let { DailyReportCategory.fromLabel(it.category) } ?: initialCategory)
    }
    var equipmentName by remember(existingReport) {
        mutableStateOf(existingReport?.equipmentName ?: "")
    }
    var workLocation by remember(existingReport) {
        mutableStateOf(existingReport?.workLocation ?: "")
    }
    var activityDescription by remember(existingReport) {
        mutableStateOf(existingReport?.activityDescription ?: "")
    }
    var startTime by remember(existingReport) {
        mutableStateOf(existingReport?.startTime?.takeIf { it.isNotBlank() } ?: "08.00")
    }
    var endTime by remember(existingReport) {
        mutableStateOf(existingReport?.endTime?.takeIf { it.isNotBlank() } ?: "10.00")
    }
    var workResult by remember(existingReport) {
        mutableStateOf(existingReport?.workResult ?: "")
    }
    var selectedStatus by remember(existingReport) {
        mutableStateOf(existingReport?.let { DailyReportStatus.fromLabel(it.status) } ?: DailyReportStatus.SELESAI)
    }
    var issuesFound by remember(existingReport) {
        mutableStateOf(existingReport?.issuesFound?.takeIf { it != "-" } ?: "")
    }
    var photoUris by remember(existingReport) {
        mutableStateOf(existingReport?.resolvedPhotos() ?: emptyList())
    }

    var techExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var timePickerTarget by remember { mutableStateOf<String?>(null) } // "start" or "end"
    var previewPhoto by remember { mutableStateOf<Pair<String, String>?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoActions = rememberMultiPhotoPickerState(
        prefix = "daily_report",
        currentCountProvider = { photoUris.size },
        onPhotosAdded = { added ->
            photoUris = (photoUris + added)
                .distinct()
                .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
        }
    )

    val (badgeBg, badgeFg, _) = dailyCategoryStyle(selectedCategory)

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
                .padding(bottom = 26.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(color = badgeBg, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = selectedCategory.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isEditMode) "Edit ${existingReport?.reportCode}" else "Daily Work Report",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Lengkapi laporan aktivitas harian teknisi (12 kolom wajib).",
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
                        .testTag("close_daily_modal")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup form laporan harian"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Tanggal pekerjaan
            OutlinedTextField(
                value = workDate,
                onValueChange = {
                    workDate = it.take(40)
                    validationError = null
                },
                label = { Text("1. Tanggal Pekerjaan") },
                placeholder = { Text("Contoh: 19 Nov 2026") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "Pilih tanggal pekerjaan",
                            tint = CobaltPrimary
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_date_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Nama teknisi
            ExposedDropdownMenuBox(
                expanded = techExpanded,
                onExpandedChange = { techExpanded = !techExpanded }
            ) {
                OutlinedTextField(
                    value = technicianName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("2. Nama Teknisi") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = techExpanded) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("daily_tech_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = techExpanded,
                    onDismissRequest = { techExpanded = false }
                ) {
                    executorNames.forEach { nameOnly ->
                        DropdownMenuItem(
                            text = { Text(nameOnly) },
                            onClick = {
                                technicianName = nameOnly
                                techExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Kategori pekerjaan (9 categories)
            Text(
                text = "3. Kategori Pekerjaan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DailyReportCategory.entries.forEach { catOption ->
                    FilterChip(
                        selected = selectedCategory == catOption,
                        onClick = { selectedCategory = catOption },
                        label = { Text(catOption.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CobaltPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("daily_category_option_${catOption.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Nama equipment
            OutlinedTextField(
                value = equipmentName,
                onValueChange = {
                    equipmentName = it.take(120)
                    validationError = null
                },
                label = { Text("4. Nama Equipment") },
                placeholder = { Text("Contoh: Pressure Transmitter PT-101 / Panel MCC") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_equipment_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Lokasi pekerjaan
            OutlinedTextField(
                value = workLocation,
                onValueChange = {
                    workLocation = it.take(120)
                    validationError = null
                },
                label = { Text("5. Lokasi Pekerjaan") },
                placeholder = { Text("Contoh: Compressor Area / Substation B / Workshop") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_location_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Deskripsi aktivitas
            OutlinedTextField(
                value = activityDescription,
                onValueChange = {
                    activityDescription = it.take(800)
                    validationError = null
                },
                label = { Text("6. Deskripsi Aktivitas") },
                placeholder = { Text("Contoh: Pemeriksaan & kalibrasi zero-span transmitter 4-20mA") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_activity_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Jam mulai & 8. Jam selesai
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = {
                        startTime = it.take(10)
                        validationError = null
                    },
                    label = { Text("7. Jam Mulai") },
                    placeholder = { Text("08.00") },
                    trailingIcon = {
                        IconButton(onClick = { timePickerTarget = "start" }) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = "Pilih jam mulai",
                                tint = CobaltPrimary
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("daily_start_time_input")
                )

                OutlinedTextField(
                    value = endTime,
                    onValueChange = {
                        endTime = it.take(10)
                        validationError = null
                    },
                    label = { Text("8. Jam Selesai") },
                    placeholder = { Text("10.00") },
                    trailingIcon = {
                        IconButton(onClick = { timePickerTarget = "end" }) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = "Pilih jam selesai",
                                tint = CobaltPrimary
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("daily_end_time_input")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 9. Hasil pekerjaan
            OutlinedTextField(
                value = workResult,
                onValueChange = {
                    workResult = it.take(600)
                    validationError = null
                },
                label = { Text("9. Hasil Pekerjaan") },
                placeholder = { Text("Contoh: Output stabil 4-20mA, fungsi normal dan siap operasi") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_result_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 10. Status pekerjaan
            Text(
                text = "10. Status Pekerjaan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DailyReportStatus.entries.forEach { statusOption ->
                    FilterChip(
                        selected = selectedStatus == statusOption,
                        onClick = { selectedStatus = statusOption },
                        label = { Text(statusOption.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CobaltPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("daily_status_option_${statusOption.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 11. Kendala yang ditemukan
            OutlinedTextField(
                value = issuesFound,
                onValueChange = { issuesFound = it.take(400) },
                label = { Text("11. Kendala yang Ditemukan (Kosongkan jika tidak ada)") },
                placeholder = { Text("Contoh: Baut terminal berkarat / Tidak ada kendala") },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_issues_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 12. Foto dokumentasi
            Text(
                text = "12. Foto Dokumentasi (Maks. 4 Foto)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            WorkOrderMultiPhotoSlot(
                title = "Foto Dokumentasi Aktivitas Harian",
                subtitle = "Foto langsung dengan kamera atau unggah dari galeri (tersimpan di Firebase)",
                photoUris = photoUris,
                testTagPrefix = "daily_input_photo",
                actions = photoActions,
                onRemovePhotoAt = { idx ->
                    if (idx in photoUris.indices) {
                        photoUris = photoUris.toMutableList().apply { removeAt(idx) }
                    }
                },
                onPreviewPhoto = { idx, uri ->
                    previewPhoto = "Foto Daily Work Report (${idx + 1}/4)" to uri
                }
            )

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
                        workDate.isBlank() -> validationError = "Tanggal pekerjaan wajib diisi."
                        technicianName.isBlank() -> validationError = "Nama teknisi wajib dipilih."
                        equipmentName.isBlank() -> validationError = "Nama equipment wajib diisi."
                        workLocation.isBlank() -> validationError = "Lokasi pekerjaan wajib diisi."
                        activityDescription.isBlank() -> validationError = "Deskripsi aktivitas wajib diisi."
                        startTime.isBlank() || endTime.isBlank() -> validationError = "Jam mulai & jam selesai wajib diisi."
                        workResult.isBlank() -> validationError = "Hasil pekerjaan wajib diisi."
                        else -> onSubmit(
                            workDate.trim(),
                            technicianName.trim(),
                            selectedCategory,
                            equipmentName.trim(),
                            workLocation.trim(),
                            activityDescription.trim(),
                            startTime.trim(),
                            endTime.trim(),
                            workResult.trim(),
                            selectedStatus,
                            issuesFound.trim().ifEmpty { "-" },
                            photoUris
                        )
                    }
                },
                enabled = !isSubmitting && !photoActions.isUploading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CobaltPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("daily_submit_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Simpan Perubahan Laporan" else "Simpan Daily Work Report",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isEditMode && onDelete != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDelete,
                    enabled = !isSubmitting,
                    border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("daily_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Hapus laporan harian",
                        tint = RoseAlert,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hapus Laporan Harian", color = RoseAlert, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            workDate = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
                                .format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val activeTimeTarget = timePickerTarget
    if (activeTimeTarget != null) {
        val initialRaw = if (activeTimeTarget == "start") startTime else endTime
        val parsed = parseHourMinute(initialRaw)
        val timePickerState = rememberTimePickerState(
            initialHour = parsed.first,
            initialMinute = parsed.second,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { timePickerTarget = null },
            title = {
                Text(if (activeTimeTarget == "start") "Pilih Jam Mulai" else "Pilih Jam Selesai")
            },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val formatted = String.format(
                            Locale.US,
                            "%02d.%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        if (activeTimeTarget == "start") {
                            startTime = formatted
                        } else {
                            endTime = formatted
                        }
                        timePickerTarget = null
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { timePickerTarget = null }) {
                    Text("Batal")
                }
            }
        )
    }

    val activePreview = previewPhoto
    if (activePreview != null) {
        FullScreenPhotoDialog(
            title = activePreview.first,
            photoUri = activePreview.second,
            onDismiss = { previewPhoto = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyWorkReportPhotoSheet(
    report: DailyWorkReport,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onPhotosUpdated: (photoUris: List<String>) -> Unit,
    onCycleStatus: () -> Unit,
    onEditReport: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var previewPhoto by remember { mutableStateOf<Pair<String, String>?>(null) }
    val photos = remember(report) { report.resolvedPhotos() }

    val photoActions = rememberMultiPhotoPickerState(
        prefix = "daily_${report.reportCode}",
        currentCountProvider = { photos.size },
        onPhotosAdded = { added ->
            val updated = (photos + added)
                .distinct()
                .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
            onPhotosUpdated(updated)
        }
    )

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
                .padding(bottom = 26.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dokumentasi Daily Report",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${report.reportCode} • ${report.formattedTimeRange()} (${report.equipmentName})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CobaltPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SlateSurfaceVariant)
                        .testTag("close_daily_photo_modal")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup dokumentasi laporan harian"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${report.category} • ${EiSeedDefaults.sanitizePersonNameOnly(report.technicianName)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = onCycleStatus,
                    enabled = !isSubmitting,
                    color = CobaltLight,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("daily_photo_sheet_cycle_status")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DailyReportStatus.fromLabel(report.status).label,
                            style = MaterialTheme.typography.labelSmall,
                            color = CobaltPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Ubah status laporan harian",
                            tint = CobaltPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            WorkOrderMultiPhotoSlot(
                title = "Foto Dokumentasi Pekerjaan Harian",
                subtitle = "Foto langsung dengan kamera atau unggah dari galeri (maks. 4 foto, terlihat semua team)",
                photoUris = photos,
                testTagPrefix = "daily_detail_photo",
                actions = photoActions,
                onRemovePhotoAt = { idx ->
                    if (idx in photos.indices) {
                        val updated = photos.toMutableList().apply { removeAt(idx) }
                        onPhotosUpdated(updated)
                    }
                },
                onPreviewPhoto = { idx, uri ->
                    previewPhoto = "Foto ${report.reportCode} #${idx + 1}" to uri
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onEditReport,
                enabled = !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CobaltPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("daily_photo_sheet_edit_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = CobaltPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit 12 Data Daily Work Report",
                    color = CobaltPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    val activePreview = previewPhoto
    if (activePreview != null) {
        FullScreenPhotoDialog(
            title = activePreview.first,
            photoUri = activePreview.second,
            onDismiss = { previewPhoto = null }
        )
    }
}

private fun dailyCategoryStyle(category: DailyReportCategory): Triple<Color, Color, Color> = when (category) {
    DailyReportCategory.DAILY_INSPECTION -> Triple(CyanSurface, CyanAccent, CyanAccent.copy(alpha = 0.35f))
    DailyReportCategory.PREVENTIVE_MAINTENANCE -> Triple(CobaltLight, CobaltPrimary, CobaltContainer)
    DailyReportCategory.CORRECTIVE_MAINTENANCE -> Triple(AmberSurface, AmberWarning, AmberBorder)
    DailyReportCategory.BREAKDOWN -> Triple(RoseSurface, RoseAlert, RoseBorder)
    DailyReportCategory.CALIBRATION -> Triple(EmeraldSurface, EmeraldSuccess, EmeraldBorder)
    DailyReportCategory.TROUBLESHOOTING -> Triple(PurpleSurface, PurplePdf, PurpleBorder)
    DailyReportCategory.INSTALLATION -> Triple(IndigoSurface, IndigoAccent, IndigoAccent.copy(alpha = 0.35f))
    DailyReportCategory.MODIFICATION -> Triple(AmberSurface, AmberWarning, AmberBorder)
    DailyReportCategory.OTHER_ACTIVITY -> Triple(SlateSurfaceVariant, Color(0xFF2B5B9E), SlateBorder)
}

private fun dailyStatusStyle(status: DailyReportStatus): Pair<Color, Color> = when (status) {
    DailyReportStatus.SELESAI -> EmeraldSurface to EmeraldSuccess
    DailyReportStatus.PROSES -> CobaltLight to CobaltPrimary
    DailyReportStatus.OPEN -> SlateSurfaceVariant to Color(0xFF4B6488)
}

private fun parseHourMinute(raw: String): Pair<Int, Int> {
    val clean = raw.trim().replace(':', '.')
    val parts = clean.split('.')
    val h = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8
    val m = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    return h to m
}
