package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TableView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BriefingNotice
import com.example.data.model.DailyReportCategory
import com.example.data.model.DailyReportStatus
import com.example.data.model.DailyWorkReport
import com.example.data.model.EiSeedDefaults
import com.example.data.model.EiSortHelpers
import com.example.data.model.ScmJob
import com.example.data.model.ScmJobCategory
import com.example.data.model.ScmJobStatus
import com.example.data.model.TeamMember
import com.example.data.model.WorkOrder
import com.example.data.model.WorkOrderCategory
import com.example.data.model.WorkOrderStatus
import com.example.data.model.WorkPositionStatus
import com.example.ui.theme.CobaltBright
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
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
import com.example.ui.theme.RoseSurface
import com.example.ui.theme.SinercoTeal
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.viewmodel.ActivitySubTab

@Composable
fun MonitoringTabContent(
    selectedSubTab: ActivitySubTab,
    onSelectSubTab: (ActivitySubTab) -> Unit,
    workOrders: List<WorkOrder>,
    selectedFilter: WorkOrderCategory?,
    onSelectFilter: (WorkOrderCategory?) -> Unit,
    onCycleStatus: (WorkOrder) -> Unit,
    onOpenPhotoSheet: (WorkOrder) -> Unit,
    onEditWorkOrder: (WorkOrder) -> Unit,
    onOpenNewWorkOrder: (WorkOrderCategory) -> Unit,
    dailyWorkReports: List<DailyWorkReport>,
    selectedDailyCategoryFilter: DailyReportCategory?,
    onSelectDailyCategoryFilter: (DailyReportCategory?) -> Unit,
    selectedDailyTechFilter: String?,
    onSelectDailyTechFilter: (String?) -> Unit,
    onCycleDailyReportStatus: (DailyWorkReport) -> Unit,
    onOpenDailyReportPhotoSheet: (DailyWorkReport) -> Unit,
    onEditDailyReport: (DailyWorkReport) -> Unit,
    onOpenNewDailyReport: (DailyReportCategory) -> Unit,
    scmJobs: List<ScmJob>,
    selectedScmFilter: ScmJobCategory?,
    onSelectScmFilter: (ScmJobCategory?) -> Unit,
    onCycleScmStatus: (ScmJob) -> Unit,
    onOpenScmPhotoSheet: (ScmJob) -> Unit,
    onEditScmJob: (ScmJob) -> Unit,
    onOpenNewScmJob: (ScmJobCategory) -> Unit,
    briefings: List<BriefingNotice> = emptyList(),
    onEditBriefing: (BriefingNotice) -> Unit = {},
    onAddBriefing: () -> Unit = {},
    teamMembers: List<TeamMember> = emptyList(),
    picSearchQuery: String = "",
    onPicSearchQueryChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var briefingsExpanded by remember { mutableStateOf(true) }
    val trimmedPicQuery = picSearchQuery.trim()
    val allPicChips = remember(teamMembers, workOrders, dailyWorkReports, scmJobs) {
        (
            teamMembers.map { EiSeedDefaults.sanitizePersonNameOnly(it.name) } +
                workOrders.map { EiSeedDefaults.sanitizePersonNameOnly(it.technician) } +
                dailyWorkReports.map { EiSeedDefaults.sanitizePersonNameOnly(it.technicianName) } +
                scmJobs.map { EiSeedDefaults.sanitizePersonNameOnly(it.picName) }
            )
            .filter { it.isNotBlank() }
            .distinct()
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val (bannerTitle, bannerSubtitle, bannerAction) = when (selectedSubTab) {
                ActivitySubTab.EI_WORK -> Triple(
                    "Aktivitas EII",
                    "Ketuk Edit atau status untuk ubah pekerjaan EII (PM, CM, Persiapan, Instalasi, dll)",
                    "+ Buat WO"
                )
                ActivitySubTab.DAILY_REPORT -> Triple(
                    "Daily Work Report",
                    "Laporan harian kegiatan personil: Waktu, Aktivitas, Hasil, Kendala & Foto",
                    "+ Isi Harian"
                )
                ActivitySubTab.SCM_LOGISTICS -> Triple(
                    "Aktivitas SCM & Logistik",
                    "List job Purchase Request, pengiriman barang, mob/demob, plan, forecast & meeting",
                    "+ Job SCM"
                )
            }
            SectionTopBanner(
                title = bannerTitle,
                subtitle = bannerSubtitle,
                actionLabel = bannerAction,
                onActionClick = {
                    when (selectedSubTab) {
                        ActivitySubTab.EI_WORK -> onOpenNewWorkOrder(selectedFilter ?: WorkOrderCategory.PM)
                        ActivitySubTab.DAILY_REPORT -> onOpenNewDailyReport(
                            selectedDailyCategoryFilter ?: DailyReportCategory.DAILY_INSPECTION
                        )
                        ActivitySubTab.SCM_LOGISTICS -> onOpenNewScmJob(
                            selectedScmFilter ?: ScmJobCategory.PURCHASE_REQUEST
                        )
                    }
                }
            )
        }

        item {
            Surface(
                color = SlateSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActivitySubTabPill(
                        label = "EII (${workOrders.size})",
                        selected = selectedSubTab == ActivitySubTab.EI_WORK,
                        onClick = { onSelectSubTab(ActivitySubTab.EI_WORK) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subtab_ei_work")
                    )
                    ActivitySubTabPill(
                        label = "Harian (${dailyWorkReports.size})",
                        selected = selectedSubTab == ActivitySubTab.DAILY_REPORT,
                        onClick = { onSelectSubTab(ActivitySubTab.DAILY_REPORT) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subtab_daily_report")
                    )
                    ActivitySubTabPill(
                        label = "SCM (${scmJobs.size})",
                        selected = selectedSubTab == ActivitySubTab.SCM_LOGISTICS,
                        onClick = { onSelectSubTab(ActivitySubTab.SCM_LOGISTICS) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subtab_scm_logistics")
                    )
                }
            }
        }

        item {
            SavedBriefingsActivityPanel(
                briefings = briefings,
                expanded = briefingsExpanded,
                onToggleExpand = { briefingsExpanded = !briefingsExpanded },
                onAddBriefing = onAddBriefing,
                onEditBriefing = onEditBriefing,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = picSearchQuery,
                    onValueChange = { onPicSearchQueryChange(it.take(80)) },
                    placeholder = { Text("Cari nama PIC (EII, Harian, SCM)...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Cari nama PIC",
                            tint = CobaltPrimary
                        )
                    },
                    trailingIcon = {
                        if (picSearchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onPicSearchQueryChange("") },
                                modifier = Modifier.testTag("activity_pic_search_clear")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Hapus filter PIC"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_pic_search_input")
                )

                if (allPicChips.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = trimmedPicQuery.isEmpty(),
                                onClick = {
                                    onPicSearchQueryChange("")
                                    onSelectDailyTechFilter(null)
                                },
                                label = { Text("Semua PIC") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CobaltPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("activity_pic_chip_all")
                            )
                        }
                        items(allPicChips) { picName ->
                            val isSelected = trimmedPicQuery.equals(picName, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onPicSearchQueryChange(if (isSelected) "" else picName)
                                },
                                label = { Text(picName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CobaltPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("activity_pic_chip_$picName")
                            )
                        }
                    }
                }
            }
        }

        when (selectedSubTab) {
            ActivitySubTab.EI_WORK -> {
                val sortedWorkOrders = EiSortHelpers.sortWorkOrders(workOrders)
                val picMatchedWorkOrders = if (trimmedPicQuery.isEmpty()) {
                    sortedWorkOrders
                } else {
                    sortedWorkOrders.filter { wo ->
                        val cleanPic = EiSeedDefaults.sanitizePersonNameOnly(wo.technician)
                        cleanPic.contains(trimmedPicQuery, ignoreCase = true) ||
                            wo.technician.contains(trimmedPicQuery, ignoreCase = true)
                    }
                }
                val filtered = if (selectedFilter == null) {
                    picMatchedWorkOrders
                } else {
                    picMatchedWorkOrders.filter { it.category == selectedFilter.label }
                }

                val openCount = picMatchedWorkOrders.count { it.status == WorkOrderStatus.OPEN.label }
                val progressCount = picMatchedWorkOrders.count { it.status == WorkOrderStatus.IN_PROGRESS.label }
                val doneCount = picMatchedWorkOrders.count { it.status == WorkOrderStatus.DONE.label }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniStatPill("Open", openCount.toString(), Color(0xFF4B6488), SlateSurfaceVariant, Modifier.weight(1f))
                        MiniStatPill("Progress", progressCount.toString(), CobaltPrimary, CobaltLight, Modifier.weight(1f))
                        MiniStatPill("Done", doneCount.toString(), EmeraldSuccess, EmeraldSurface, Modifier.weight(1f))
                    }
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == null,
                                onClick = { onSelectFilter(null) },
                                label = { Text("Semua (${workOrders.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CobaltPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_all")
                            )
                        }
                        items(WorkOrderCategory.entries) { category ->
                            val count = workOrders.count { it.category == category.label }
                            FilterChip(
                                selected = selectedFilter == category,
                                onClick = { onSelectFilter(category) },
                                label = { Text("${category.shortLabel} ($count)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CobaltPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_${category.name}")
                            )
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Belum ada log pekerjaan",
                            subtitle = "Tambahkan laporan Preventive, Corrective, atau Breakdown baru."
                        )
                    }
                } else {
                    items(filtered, key = { it.id.ifEmpty { it.woCode } }) { wo ->
                        WorkOrderCard(
                            workOrder = wo,
                            allowStatusCycle = true,
                            onCycleStatus = { onCycleStatus(wo) },
                            onOpenPhotoSheet = { onOpenPhotoSheet(wo) },
                            onEditWorkOrder = { onEditWorkOrder(wo) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }

            ActivitySubTab.DAILY_REPORT -> {
                val technicianNames = dailyWorkReports
                    .map { EiSeedDefaults.sanitizePersonNameOnly(it.technicianName) }
                    .filter { it.isNotBlank() }
                    .distinct()

                val picMatchedDaily = if (trimmedPicQuery.isEmpty()) {
                    dailyWorkReports
                } else {
                    dailyWorkReports.filter { report ->
                        val cleanTech = EiSeedDefaults.sanitizePersonNameOnly(report.technicianName)
                        cleanTech.contains(trimmedPicQuery, ignoreCase = true) ||
                            report.technicianName.contains(trimmedPicQuery, ignoreCase = true)
                    }
                }

                val filteredDaily = EiSortHelpers.sortDailyWorkReports(
                    picMatchedDaily.filter { report ->
                        val matchesCat = selectedDailyCategoryFilter == null ||
                            report.category.equals(selectedDailyCategoryFilter.label, ignoreCase = true)
                        val cleanTech = EiSeedDefaults.sanitizePersonNameOnly(report.technicianName)
                        val matchesTech = selectedDailyTechFilter == null ||
                            cleanTech.equals(selectedDailyTechFilter, ignoreCase = true)
                        matchesCat && matchesTech
                    }
                )

                val openDaily = picMatchedDaily.count { it.status == DailyReportStatus.OPEN.label }
                val processDaily = picMatchedDaily.count { it.status == DailyReportStatus.PROSES.label }
                val doneDaily = picMatchedDaily.count { it.status == DailyReportStatus.SELESAI.label }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniStatPill("Open", openDaily.toString(), Color(0xFF4B6488), SlateSurfaceVariant, Modifier.weight(1f))
                        MiniStatPill("Proses", processDaily.toString(), CobaltPrimary, CobaltLight, Modifier.weight(1f))
                        MiniStatPill("Selesai", doneDaily.toString(), EmeraldSuccess, EmeraldSurface, Modifier.weight(1f))
                    }
                }

                if (technicianNames.isNotEmpty()) {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedDailyTechFilter == null,
                                    onClick = { onSelectDailyTechFilter(null) },
                                    label = { Text("Semua Teknisi") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CobaltPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("daily_tech_filter_all")
                                )
                            }
                            items(technicianNames) { techName ->
                                val count = dailyWorkReports.count {
                                    EiSeedDefaults.sanitizePersonNameOnly(it.technicianName).equals(techName, ignoreCase = true)
                                }
                                FilterChip(
                                    selected = selectedDailyTechFilter.equals(techName, ignoreCase = true),
                                    onClick = { onSelectDailyTechFilter(techName) },
                                    label = { Text("$techName ($count)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CobaltPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("daily_tech_filter_$techName")
                                )
                            }
                        }
                    }
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDailyCategoryFilter == null,
                                onClick = { onSelectDailyCategoryFilter(null) },
                                label = { Text("Semua (${dailyWorkReports.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CobaltPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("daily_filter_all")
                            )
                        }
                        items(DailyReportCategory.entries) { category ->
                            val count = dailyWorkReports.count { it.category == category.label }
                            FilterChip(
                                selected = selectedDailyCategoryFilter == category,
                                onClick = { onSelectDailyCategoryFilter(category) },
                                label = { Text("${category.shortLabel} ($count)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CobaltPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("daily_filter_${category.name}")
                            )
                        }
                    }
                }

                item {
                    DailyWorkReportTimelineTable(
                        reports = filteredDaily,
                        onRowClick = { onEditDailyReport(it) },
                        onAddClick = {
                            onOpenNewDailyReport(
                                selectedDailyCategoryFilter ?: DailyReportCategory.DAILY_INSPECTION
                            )
                        },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                if (filteredDaily.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Belum ada Daily Work Report",
                            subtitle = "Ketuk + Isi Harian untuk mencatat kegiatan harian teknisi beserta foto."
                        )
                    }
                } else {
                    items(filteredDaily, key = { it.id.ifEmpty { it.reportCode } }) { report ->
                        DailyWorkReportCard(
                            report = report,
                            onCycleStatus = { onCycleDailyReportStatus(report) },
                            onOpenPhotoSheet = { onOpenDailyReportPhotoSheet(report) },
                            onEditReport = { onEditDailyReport(report) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }

            ActivitySubTab.SCM_LOGISTICS -> {
                val sortedScm = EiSortHelpers.sortScmJobs(scmJobs)
                val picMatchedScm = if (trimmedPicQuery.isEmpty()) {
                    sortedScm
                } else {
                    sortedScm.filter { scm ->
                        val cleanPic = EiSeedDefaults.sanitizePersonNameOnly(scm.picName)
                        cleanPic.contains(trimmedPicQuery, ignoreCase = true) ||
                            scm.picName.contains(trimmedPicQuery, ignoreCase = true)
                    }
                }
                val filteredScm = if (selectedScmFilter == null) {
                    picMatchedScm
                } else {
                    picMatchedScm.filter { it.category == selectedScmFilter.label }
                }

                val openScm = picMatchedScm.count { it.status == ScmJobStatus.OPEN.label }
                val processScm = picMatchedScm.count { it.status == ScmJobStatus.PROSES.label }
                val doneScm = picMatchedScm.count { it.status == ScmJobStatus.SELESAI.label }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniStatPill("Open", openScm.toString(), Color(0xFF4B6488), SlateSurfaceVariant, Modifier.weight(1f))
                        MiniStatPill("Proses", processScm.toString(), CobaltPrimary, CobaltLight, Modifier.weight(1f))
                        MiniStatPill("Selesai", doneScm.toString(), EmeraldSuccess, EmeraldSurface, Modifier.weight(1f))
                    }
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedScmFilter == null,
                                onClick = { onSelectScmFilter(null) },
                                label = { Text("Semua (${scmJobs.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CobaltPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("scm_filter_all")
                            )
                        }
                        items(ScmJobCategory.entries) { category ->
                            val count = scmJobs.count { it.category == category.label }
                            FilterChip(
                                selected = selectedScmFilter == category,
                                onClick = { onSelectScmFilter(category) },
                                label = { Text("${category.shortLabel} ($count)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CobaltPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("scm_filter_${category.name}")
                            )
                        }
                    }
                }

                if (filteredScm.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Belum ada job SCM",
                            subtitle = "Tambahkan daftar Purchase Request, pengiriman barang, atau penerimaan material."
                        )
                    }
                } else {
                    items(filteredScm, key = { it.id.ifEmpty { it.jobCode } }) { scm ->
                        ScmJobCard(
                            scmJob = scm,
                            onCycleStatus = { onCycleScmStatus(scm) },
                            onOpenPhotoSheet = { onOpenScmPhotoSheet(scm) },
                            onEditScmJob = { onEditScmJob(scm) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivitySubTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (selected) CobaltPrimary else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReportsTabContent(
    workOrderCount: Int,
    scmJobCount: Int,
    dailyReportCount: Int = 0,
    briefingCount: Int = 0,
    onShareExcel: () -> Unit,
    onSaveExcel: () -> Unit,
    onSharePdf: () -> Unit,
    onSavePdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionTopBanner(
                title = "Export Laporan Departemen",
                subtitle = "Rekap $workOrderCount EII, $scmJobCount SCM, $dailyReportCount Harian & $briefingCount Info Briefing",
                actionLabel = null,
                onActionClick = {}
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
                border = BorderStroke(1.dp, EmeraldBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldSuccess),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TableView,
                                contentDescription = "Excel",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Laporan Excel (.xlsx)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sheet 1: EII | Sheet 2: SCM | Sheet 3: Daily Work Report (+Hasil) | Sheet 4: Info & Briefing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onSaveExcel,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_excel_button")
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan .xlsx")
                        }
                        OutlinedButton(
                            onClick = onShareExcel,
                            border = BorderStroke(1.dp, EmeraldSuccess),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("share_excel_button")
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bagikan", color = EmeraldSuccess)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleSurface),
                border = BorderStroke(1.dp, PurpleBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PurplePdf),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PictureAsPdf,
                                contentDescription = "PDF",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Laporan PDF (.pdf)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Rekap EII, SCM & Logistik, Daily Work Report (Hasil), serta Info & Briefing Team",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onSavePdf,
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePdf),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_pdf_button")
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan .pdf")
                        }
                        OutlinedButton(
                            onClick = onSharePdf,
                            border = BorderStroke(1.dp, PurplePdf),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("share_pdf_button")
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, tint = PurplePdf, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bagikan", color = PurplePdf)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamRosterTabContent(
    teamMembers: List<TeamMember>,
    onOpenWorkStatus: () -> Unit,
    onOpenWaReminder: () -> Unit = {},
    onQuickChatMember: (TeamMember) -> Unit = {},
    onAddMember: () -> Unit,
    onEditMember: (TeamMember) -> Unit,
    onAddCustomJobTitle: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddJobDialog by remember { mutableStateOf(false) }
    var newJobTitleDraft by remember { mutableStateOf("") }

    if (showAddJobDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddJobDialog = false
                newJobTitleDraft = ""
            },
            title = { Text("Tambah Jabatan Manual") },
            text = {
                Column {
                    Text(
                        text = "Jabatan yang ditambahkan otomatis masuk ke dropdown saat menambah atau mengedit anggota team.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newJobTitleDraft,
                        onValueChange = { newJobTitleDraft = it.take(80) },
                        label = { Text("Nama Jabatan") },
                        placeholder = { Text("Contoh: Lead Instrument / QA/QC") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("team_dialog_job_title_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newJobTitleDraft.trim().take(80)
                        if (clean.isNotEmpty()) {
                            onAddCustomJobTitle(clean)
                            showAddJobDialog = false
                            newJobTitleDraft = ""
                        }
                    },
                    enabled = newJobTitleDraft.isNotBlank(),
                    modifier = Modifier.testTag("team_dialog_job_title_save")
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddJobDialog = false
                        newJobTitleDraft = ""
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionTopBanner(
                title = "SCM dan EII Team",
                subtitle = "${teamMembers.size} Anggota • Ketuk nama untuk edit, isi nomor WA, atau tambah personel",
                actionLabel = "+ Anggota",
                onActionClick = onAddMember
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Daftar Personel (${teamMembers.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedButton(
                            onClick = { showAddJobDialog = true },
                            border = BorderStroke(1.dp, CobaltContainer),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("team_add_job_title_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Tambah Jabatan Manual",
                                tint = CobaltPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Jabatan",
                                style = MaterialTheme.typography.labelSmall,
                                color = CobaltPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = onOpenWaReminder,
                            border = BorderStroke(1.dp, EmeraldBorder),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("team_wa_reminder_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AlarmOn,
                                contentDescription = "Reminder WA 16:00",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WA 16:00",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = onOpenWorkStatus,
                            border = BorderStroke(1.dp, SlateBorder),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("team_work_status_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Posisi", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        items(teamMembers, key = { it.id.ifEmpty { "${it.name}_${it.sortOrder}" } }) { member ->
            val roleCode = EiSeedDefaults.badgeCodeForRole(member.role)
            val badgeCircleBg = when (roleCode) {
                "SM" -> IndigoAccent
                "AM", "MGR", "SPV" -> CobaltPrimary
                "ADM" -> EmeraldSuccess
                else -> Color(0xFF2B5B9E)
            }
            val statusEnum = WorkPositionStatus.fromLabel(member.status)
            val (badgeBg, badgeFg, _) = workStatusVisualStyle(statusEnum)
            val hasWhatsapp = EiSeedDefaults.formatWhatsappWaId(member.whatsappNumber).isNotEmpty()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onEditMember(member) }
                    .testTag("team_member_card_${member.id.ifEmpty { member.name }}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.93f)),
                border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.9f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(badgeCircleBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = roleCode,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = member.role,
                                style = MaterialTheme.typography.labelMedium,
                                color = CobaltPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (member.specialty.isNotBlank() && member.specialty != member.role) {
                                Text(
                                    text = member.specialty,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (hasWhatsapp) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Phone,
                                        contentDescription = "Nomor WhatsApp",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = member.whatsappNumber,
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                        color = EmeraldSuccess,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (member.workNote.isNotBlank()) {
                                Text(
                                    text = "Tujuan: ${member.workNote}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = badgeFg,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (hasWhatsapp) {
                                Surface(
                                    color = EmeraldSurface,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, EmeraldBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { onQuickChatMember(member) }
                                        .testTag("team_wa_${member.id.ifEmpty { member.name }}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Send,
                                            contentDescription = "Kirim WhatsApp ${member.name}",
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "WA",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldSuccess
                                        )
                                    }
                                }
                            }
                            Surface(
                                color = CobaltLight,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onEditMember(member) }
                                    .testTag("team_edit_${member.id.ifEmpty { member.name }}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit ${member.name}",
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
                            Surface(color = badgeBg, shape = RoundedCornerShape(50)) {
                                Text(
                                    text = statusEnum.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = badgeFg,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (member.lastTime != "-") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = member.lastTime,
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedBriefingsActivityPanel(
    briefings: List<BriefingNotice>,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddBriefing: () -> Unit,
    onEditBriefing: (BriefingNotice) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_briefings_panel"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = IndigoSurface.copy(alpha = 0.72f)),
        border = BorderStroke(1.dp, CobaltContainer)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CobaltPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Campaign,
                            contentDescription = "Info & Briefing Team",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Info & Briefing Team (${briefings.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tersimpan di cloud & tampil di export PDF / Excel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onAddBriefing,
                        color = CobaltPrimary,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("activity_add_briefing_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Tambah briefing",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Info",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (expanded) "Tutup daftar briefing" else "Lihat daftar briefing",
                        tint = CobaltPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                if (briefings.isEmpty()) {
                    Text(
                        text = "Belum ada info & briefing tersimpan. Ketuk + Info untuk menambahkan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        briefings.sortedBy { it.sortOrder }.forEach { notice ->
                            val badgeBg = if (notice.isSafetyAlert) RoseSurface else CobaltLight
                            val badgeFg = if (notice.isSafetyAlert) RoseAlert else CobaltPrimary
                            Surface(
                                onClick = { onEditBriefing(notice) },
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.85f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("activity_briefing_item_${notice.id.ifEmpty { notice.title }}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(color = badgeBg, shape = RoundedCornerShape(6.dp)) {
                                            Text(
                                                text = notice.badge,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = badgeFg,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                        Text(
                                            text = notice.timeTag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = notice.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = notice.summary,
                                        style = MaterialTheme.typography.bodySmall,
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

@Composable
private fun SectionTopBanner(
    title: String,
    subtitle: String,
    actionLabel: String?,
    onActionClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.linearGradient(listOf(CobaltPrimary, CobaltBright, SinercoTeal)))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFDBEAFE)
                )
            }
            if (actionLabel != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = onActionClick,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (actionLabel.startsWith("+")) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = actionLabel.removePrefix("+ "),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatPill(
    label: String,
    value: String,
    fg: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = fg
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = fg
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
