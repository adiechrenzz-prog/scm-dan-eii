package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.DailyReportCategory
import com.example.data.model.DailyReportStatus
import com.example.data.model.EiActivitySummaryHelpers
import com.example.data.model.EiSeedDefaults
import com.example.data.model.EiSortHelpers
import com.example.data.model.ScmJobCategory
import com.example.data.model.ScmJobStatus
import com.example.data.model.WorkOrderCategory
import com.example.data.model.WorkOrderStatus
import com.example.data.model.WorkPositionStatus
import com.example.ui.components.BriefingEditSheet
import com.example.ui.components.BriefingSliderSection
import com.example.ui.components.DailyWorkReportCard
import com.example.ui.components.DailyWorkReportInputSheet
import com.example.ui.components.DailyWorkReportPhotoSheet
import com.example.ui.components.DashboardHeaderWithCards
import com.example.ui.components.MonitoringTabContent
import com.example.ui.components.OperationalMenuGrid
import com.example.ui.components.ReportsTabContent
import com.example.ui.components.ScmJobCard
import com.example.ui.components.ScmJobInputSheet
import com.example.ui.components.ScmJobPhotoSheet
import com.example.ui.components.SinercoWatermarkBackdrop
import com.example.ui.components.TeamMemberEditSheet
import com.example.ui.components.TeamRosterTabContent
import com.example.ui.components.WhatsAppReminderSheet
import com.example.ui.components.WorkOrderCard
import com.example.ui.components.WorkOrderInputSheet
import com.example.ui.components.WorkOrderPhotoSheet
import com.example.ui.components.WorkPositionsTabContent
import com.example.ui.components.WorkStatusSheet
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateTextMuted
import com.example.ui.viewmodel.ActiveModal
import com.example.ui.viewmodel.ActivitySubTab
import com.example.ui.viewmodel.BottomTab
import com.example.ui.viewmodel.EiDashboardData
import com.example.ui.viewmodel.EiTeamViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.CustomJobTitleStore
import com.example.util.ReportExporter
import com.example.util.WhatsAppReminderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun EiTeamScreen(
    viewModel: EiTeamViewModel,
    userEmail: String = "",
    defaultAccountName: String = "",
    onSignOutRequested: () -> Unit
) {
    val context = LocalContext.current
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedActivitySubTab by viewModel.selectedActivitySubTab.collectAsStateWithLifecycle()
    val activeModal by viewModel.activeModal.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val selectedScmFilter by viewModel.selectedScmFilter.collectAsStateWithLifecycle()
    val selectedDailyCategoryFilter by viewModel.selectedDailyCategoryFilter.collectAsStateWithLifecycle()
    val selectedDailyTechFilter by viewModel.selectedDailyTechFilter.collectAsStateWithLifecycle()
    val picSearchQuery by viewModel.picSearchQuery.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var pendingExportFile by remember { mutableStateOf<File?>(null) }
    var customJobRefreshTick by remember { mutableIntStateOf(0) }

    val saveExcelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(ReportExporter.excelMimeType())
    ) { uri ->
        val file = pendingExportFile
        pendingExportFile = null
        if (uri != null && file != null) {
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    ReportExporter.writeFileToUri(context, file, uri)
                }
                result.onSuccess {
                    viewModel.showToast("Laporan Excel (.xlsx) berhasil disimpan!")
                }.onFailure { err ->
                    viewModel.showToast(err.localizedMessage ?: "Gagal menyimpan file Excel.")
                }
            }
        }
    }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(ReportExporter.pdfMimeType())
    ) { uri ->
        val file = pendingExportFile
        pendingExportFile = null
        if (uri != null && file != null) {
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    ReportExporter.writeFileToUri(context, file, uri)
                }
                result.onSuccess {
                    viewModel.showToast("Laporan PDF (.pdf) berhasil disimpan!")
                }.onFailure { err ->
                    viewModel.showToast(err.localizedMessage ?: "Gagal menyimpan file PDF.")
                }
            }
        }
    }

    LaunchedEffect(toastMessage) {
        val msg = toastMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeToast()
        }
    }

    BackHandler(enabled = activeModal != ActiveModal.None || selectedTab != BottomTab.BERANDA) {
        if (activeModal != ActiveModal.None) {
            viewModel.closeModal()
        } else if (selectedTab != BottomTab.BERANDA) {
            viewModel.selectTab(BottomTab.BERANDA)
        }
    }

    fun triggerExcelExport(data: EiDashboardData, shareDirectly: Boolean) {
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                ReportExporter.exportExcel(
                    context = context,
                    workOrders = data.workOrders,
                    scmJobs = data.scmJobs,
                    dailyWorkReports = data.dailyWorkReports,
                    briefings = data.briefings
                )
            }
            result.onSuccess { file ->
                if (shareDirectly) {
                    ReportExporter.shareFile(
                        context = context,
                        file = file,
                        mimeType = ReportExporter.excelMimeType(),
                        chooserTitle = "Bagikan Laporan Excel SCM & EII Team"
                    )
                } else {
                    pendingExportFile = file
                    saveExcelLauncher.launch(file.name)
                }
            }.onFailure { err ->
                Toast.makeText(
                    context,
                    err.localizedMessage ?: "Gagal membuat file Excel",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun triggerPdfExport(data: EiDashboardData, shareDirectly: Boolean) {
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                ReportExporter.exportPdf(
                    context = context,
                    workOrders = data.workOrders,
                    scmJobs = data.scmJobs,
                    dailyWorkReports = data.dailyWorkReports,
                    briefings = data.briefings
                )
            }
            result.onSuccess { file ->
                if (shareDirectly) {
                    ReportExporter.shareFile(
                        context = context,
                        file = file,
                        mimeType = ReportExporter.pdfMimeType(),
                        chooserTitle = "Bagikan Laporan PDF SCM & EII Team"
                    )
                } else {
                    pendingExportFile = file
                    savePdfLauncher.launch(file.name)
                }
            }.onFailure { err ->
                Toast.makeText(
                    context,
                    err.localizedMessage ?: "Gagal membuat file PDF",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SinercoWatermarkBackdrop()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                EiBottomNavigationBar(
                    selectedTab = selectedTab,
                    onSelectTab = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                ) {
                when (val state = dashboardState) {
                    UiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = CobaltPrimary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Menyinkronkan data SCM dan EII Team...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is UiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Gagal memuat data tim",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(onClick = { showSignOutConfirm = true }) {
                                    Text("Ganti Akun")
                                }
                            }
                        }
                    }

                    is UiState.Success -> {
                        val data = state.data
                        val profile = data.profile
                        val currentUserName = defaultAccountName
                            .takeIf { it.isNotBlank() && it != EiSeedDefaults.SUPERVISOR_NAME }
                            ?: profile?.displayName?.takeIf { it.isNotBlank() }
                            ?: EiSeedDefaults.SUPERVISOR_NAME
                        val cleanUserName = EiSeedDefaults.sanitizePersonNameOnly(currentUserName)
                        val matchingMember = data.teamMembers.firstOrNull {
                            EiSeedDefaults.sanitizePersonNameOnly(it.name).equals(cleanUserName, ignoreCase = true)
                        }
                        val resolvedJobTitle = matchingMember?.role?.takeIf { it.isNotBlank() }
                            ?: profile?.jobTitle?.takeIf { it.isNotBlank() && it != EiSeedDefaults.DEFAULT_JOB_TITLE }
                            ?: "SCM & EII Team"

                        val activeEiiCount = data.workOrders.count { it.status != WorkOrderStatus.DONE.label }
                        val activeDailyCount = data.dailyWorkReports.count { it.status != DailyReportStatus.SELESAI.label }
                        val activeScmCount = data.scmJobs.count { it.status != ScmJobStatus.SELESAI.label }
                        val activeWoCount = activeEiiCount + activeDailyCount + activeScmCount

                        val latestLogForUser = data.workStatusLogs.firstOrNull {
                            EiSeedDefaults.sanitizePersonNameOnly(it.personName).equals(cleanUserName, ignoreCase = true)
                        }
                        val currentWorkStatus = WorkPositionStatus.fromLabel(
                            latestLogForUser?.workStatus ?: matchingMember?.status
                        )
                        val currentWorkNote = latestLogForUser?.destinationNote
                            ?: matchingMember?.workNote
                            ?: ""

                        val mergedJobTitleOptions = remember(data.teamMembers, customJobRefreshTick) {
                            CustomJobTitleStore.mergedJobTitles(context, data.teamMembers)
                        }

                        val sortedRecentEii = remember(data.workOrders) {
                            EiSortHelpers.sortWorkOrders(data.workOrders).take(3)
                        }
                        val sortedRecentDaily = remember(data.dailyWorkReports) {
                            EiSortHelpers.sortDailyWorkReports(data.dailyWorkReports).take(3)
                        }
                        val sortedRecentScm = remember(data.scmJobs) {
                            EiSortHelpers.sortScmJobs(data.scmJobs).take(3)
                        }

                        when (selectedTab) {
                            BottomTab.BERANDA -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 110.dp)
                                ) {
                                    item {
                                        DashboardHeaderWithCards(
                                            profileDisplayName = currentUserName,
                                            profileJobTitle = resolvedJobTitle,
                                            currentWorkStatus = currentWorkStatus,
                                            currentWorkNote = currentWorkNote,
                                            activeWoCount = activeWoCount,
                                            onNotificationClick = {
                                                val noteInfo = if (currentWorkNote.isNotBlank()) " ($currentWorkNote)" else ""
                                                viewModel.showToast(
                                                    "Posisi: ${currentWorkStatus.label}$noteInfo • $activeWoCount Aktif ($activeEiiCount EII, $activeDailyCount Harian, $activeScmCount SCM)"
                                                )
                                            },
                                            onWhatsAppReminderClick = {
                                                viewModel.openModal(ActiveModal.WhatsAppReminder)
                                            },
                                            onSignOutClick = { showSignOutConfirm = true },
                                            onWorkStatusCardClick = {
                                                viewModel.openModal(ActiveModal.WorkStatus())
                                            },
                                            onWorkOrderCardClick = {
                                                viewModel.selectTab(BottomTab.AKTIVITAS)
                                            }
                                        )
                                    }

                                    item { Spacer(modifier = Modifier.height(50.dp)) }

                                    item {
                                        val picTickerSummaries = remember(
                                            data.workOrders,
                                            data.dailyWorkReports,
                                            data.scmJobs
                                        ) {
                                            EiActivitySummaryHelpers.buildPicTickerSummaries(
                                                workOrders = data.workOrders,
                                                dailyWorkReports = data.dailyWorkReports,
                                                scmJobs = data.scmJobs
                                            )
                                        }
                                        val picTickerText = remember(picTickerSummaries) {
                                            EiActivitySummaryHelpers.formatPicTickerText(picTickerSummaries)
                                        }
                                        PicActivityMarqueeBanner(
                                            tickerText = picTickerText,
                                            picCount = picTickerSummaries.size,
                                            onClick = { viewModel.selectTab(BottomTab.AKTIVITAS) },
                                            modifier = Modifier.padding(horizontal = 20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                    }

                                    item {
                                        OperationalMenuGrid(
                                            totalPersonnelCount = data.teamMembers.size.coerceAtLeast(7),
                                            dailyReportCount = data.dailyWorkReports.size,
                                            onOpenDailyReportTab = {
                                                viewModel.selectActivitySubTab(ActivitySubTab.DAILY_REPORT)
                                                viewModel.selectTab(BottomTab.AKTIVITAS)
                                            },
                                            onOpenDailyReportInput = {
                                                viewModel.openModal(ActiveModal.DailyWorkReportInput())
                                            },
                                            onOpenWaReminder = {
                                                viewModel.openModal(ActiveModal.WhatsAppReminder)
                                            },
                                            onOpenPm = {
                                                viewModel.openModal(ActiveModal.WorkOrderInput(WorkOrderCategory.PM))
                                            },
                                            onOpenCm = {
                                                viewModel.openModal(ActiveModal.WorkOrderInput(WorkOrderCategory.CM))
                                            },
                                            onOpenBreakdown = {
                                                viewModel.openModal(ActiveModal.WorkOrderInput(WorkOrderCategory.BREAKDOWN))
                                            },
                                            onOpenMonitoring = {
                                                viewModel.selectTab(BottomTab.AKTIVITAS)
                                            },
                                            onOpenWorkStatus = {
                                                viewModel.openModal(ActiveModal.WorkStatus())
                                            },
                                            onOpenTeamRoster = {
                                                viewModel.selectTab(BottomTab.TIM)
                                            },
                                            onExportExcel = { triggerExcelExport(data, shareDirectly = true) },
                                            onExportPdf = { triggerPdfExport(data, shareDirectly = true) },
                                            modifier = Modifier.padding(horizontal = 20.dp)
                                        )
                                    }

                                    item { Spacer(modifier = Modifier.height(22.dp)) }

                                    item {
                                        BriefingSliderSection(
                                            briefings = data.briefings,
                                            onEditBriefing = { notice ->
                                                viewModel.openModal(ActiveModal.BriefingEdit(notice))
                                            },
                                            onAddBriefing = {
                                                viewModel.openModal(ActiveModal.BriefingEdit(null))
                                            },
                                            onViewAllClick = { viewModel.selectTab(BottomTab.AKTIVITAS) }
                                        )
                                    }

                                    item { Spacer(modifier = Modifier.height(20.dp)) }

                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(R.string.recent_wo_title),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = "${data.workOrders.size + data.dailyWorkReports.size + data.scmJobs.size} Total Log",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // 1. EII Terbaru (3 items)
                                    item {
                                        HomeRecentCategoryHeader(
                                            title = "1. Aktivitas EII Terbaru",
                                            countLabel = "${data.workOrders.size} WO",
                                            badgeBg = CobaltLight,
                                            badgeFg = CobaltPrimary,
                                            testTag = "home_recent_eii_header",
                                            onSeeAll = {
                                                viewModel.selectActivitySubTab(ActivitySubTab.EI_WORK)
                                                viewModel.selectTab(BottomTab.AKTIVITAS)
                                            }
                                        )
                                    }
                                    if (sortedRecentEii.isEmpty()) {
                                        item { HomeRecentEmptyHint("Belum ada pekerjaan EII tercatat.") }
                                    } else {
                                        items(
                                            items = sortedRecentEii,
                                            key = { "home_eii_${it.id.ifEmpty { it.woCode }}" }
                                        ) { wo ->
                                            WorkOrderCard(
                                                workOrder = wo,
                                                allowStatusCycle = true,
                                                onCycleStatus = { viewModel.cycleWorkOrderStatus(wo) },
                                                onOpenPhotoSheet = {
                                                    viewModel.openModal(ActiveModal.WorkOrderPhoto(wo.id))
                                                },
                                                onEditWorkOrder = {
                                                    viewModel.openModal(ActiveModal.WorkOrderEdit(wo.id))
                                                },
                                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                                            )
                                        }
                                    }

                                    item { Spacer(modifier = Modifier.height(10.dp)) }

                                    // 2. Harian (Daily Work Report) Terbaru (3 items)
                                    item {
                                        HomeRecentCategoryHeader(
                                            title = "2. Daily Work Report / Harian Terbaru",
                                            countLabel = "${data.dailyWorkReports.size} DWR",
                                            badgeBg = EmeraldSurface,
                                            badgeFg = EmeraldSuccess,
                                            testTag = "home_recent_daily_header",
                                            onSeeAll = {
                                                viewModel.selectActivitySubTab(ActivitySubTab.DAILY_REPORT)
                                                viewModel.selectTab(BottomTab.AKTIVITAS)
                                            }
                                        )
                                    }
                                    if (sortedRecentDaily.isEmpty()) {
                                        item { HomeRecentEmptyHint("Belum ada Daily Work Report tercatat.") }
                                    } else {
                                        items(
                                            items = sortedRecentDaily,
                                            key = { "home_daily_${it.id.ifEmpty { it.reportCode }}" }
                                        ) { rep ->
                                            DailyWorkReportCard(
                                                report = rep,
                                                onCycleStatus = { viewModel.cycleDailyWorkReportStatus(rep) },
                                                onOpenPhotoSheet = {
                                                    viewModel.openModal(ActiveModal.DailyWorkReportPhoto(rep.id))
                                                },
                                                onEditReport = {
                                                    viewModel.openModal(ActiveModal.DailyWorkReportEdit(rep.id))
                                                },
                                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                                            )
                                        }
                                    }

                                    item { Spacer(modifier = Modifier.height(10.dp)) }

                                    // 3. SCM Terbaru (3 items)
                                    item {
                                        HomeRecentCategoryHeader(
                                            title = "3. Aktivitas SCM Terbaru",
                                            countLabel = "${data.scmJobs.size} SCM",
                                            badgeBg = CobaltLight,
                                            badgeFg = CobaltPrimary,
                                            testTag = "home_recent_scm_header",
                                            onSeeAll = {
                                                viewModel.selectActivitySubTab(ActivitySubTab.SCM_LOGISTICS)
                                                viewModel.selectTab(BottomTab.AKTIVITAS)
                                            }
                                        )
                                    }
                                    if (sortedRecentScm.isEmpty()) {
                                        item { HomeRecentEmptyHint("Belum ada job SCM tercatat.") }
                                    } else {
                                        items(
                                            items = sortedRecentScm,
                                            key = { "home_scm_${it.id.ifEmpty { it.jobCode }}" }
                                        ) { scm ->
                                            ScmJobCard(
                                                scmJob = scm,
                                                onCycleStatus = { viewModel.cycleScmJobStatus(scm) },
                                                onOpenPhotoSheet = {
                                                    viewModel.openModal(ActiveModal.ScmJobPhoto(scm.id))
                                                },
                                                onEditScmJob = {
                                                    viewModel.openModal(ActiveModal.ScmJobEdit(scm.id))
                                                },
                                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            BottomTab.AKTIVITAS -> {
                                MonitoringTabContent(
                                    selectedSubTab = selectedActivitySubTab,
                                    onSelectSubTab = { viewModel.selectActivitySubTab(it) },
                                    workOrders = data.workOrders,
                                    selectedFilter = selectedFilter,
                                    onSelectFilter = { viewModel.setMonitoringFilter(it) },
                                    onCycleStatus = { viewModel.cycleWorkOrderStatus(it) },
                                    onOpenPhotoSheet = { wo ->
                                        viewModel.openModal(ActiveModal.WorkOrderPhoto(wo.id))
                                    },
                                    onEditWorkOrder = { wo ->
                                        viewModel.openModal(ActiveModal.WorkOrderEdit(wo.id))
                                    },
                                    onOpenNewWorkOrder = { category ->
                                        viewModel.openModal(ActiveModal.WorkOrderInput(category))
                                    },
                                    dailyWorkReports = data.dailyWorkReports,
                                    selectedDailyCategoryFilter = selectedDailyCategoryFilter,
                                    onSelectDailyCategoryFilter = { viewModel.setDailyCategoryFilter(it) },
                                    selectedDailyTechFilter = selectedDailyTechFilter,
                                    onSelectDailyTechFilter = { viewModel.setDailyTechFilter(it) },
                                    onCycleDailyReportStatus = { viewModel.cycleDailyWorkReportStatus(it) },
                                    onOpenDailyReportPhotoSheet = { rep ->
                                        viewModel.openModal(ActiveModal.DailyWorkReportPhoto(rep.id))
                                    },
                                    onEditDailyReport = { rep ->
                                        viewModel.openModal(ActiveModal.DailyWorkReportEdit(rep.id))
                                    },
                                    onOpenNewDailyReport = { category ->
                                        viewModel.openModal(ActiveModal.DailyWorkReportInput(category))
                                    },
                                    scmJobs = data.scmJobs,
                                    selectedScmFilter = selectedScmFilter,
                                    onSelectScmFilter = { viewModel.setScmFilter(it) },
                                    onCycleScmStatus = { viewModel.cycleScmJobStatus(it) },
                                    onOpenScmPhotoSheet = { scm ->
                                        viewModel.openModal(ActiveModal.ScmJobPhoto(scm.id))
                                    },
                                    onEditScmJob = { scm ->
                                        viewModel.openModal(ActiveModal.ScmJobEdit(scm.id))
                                    },
                                    onOpenNewScmJob = { category ->
                                        viewModel.openModal(ActiveModal.ScmJobInput(category))
                                    },
                                    briefings = data.briefings,
                                    onEditBriefing = { notice ->
                                        viewModel.openModal(ActiveModal.BriefingEdit(notice))
                                    },
                                    onAddBriefing = {
                                        viewModel.openModal(ActiveModal.BriefingEdit(null))
                                    },
                                    teamMembers = data.teamMembers,
                                    picSearchQuery = picSearchQuery,
                                    onPicSearchQueryChange = { viewModel.setPicSearchQuery(it) }
                                )
                            }

                            BottomTab.POSISI -> {
                                WorkPositionsTabContent(
                                    teamMembers = data.teamMembers,
                                    workStatusLogs = data.workStatusLogs,
                                    onUpdateStatusClick = { personName ->
                                        viewModel.openModal(ActiveModal.WorkStatus(personName))
                                    }
                                )
                            }

                            BottomTab.LAPORAN -> {
                                ReportsTabContent(
                                    workOrderCount = data.workOrders.size,
                                    scmJobCount = data.scmJobs.size,
                                    dailyReportCount = data.dailyWorkReports.size,
                                    briefingCount = data.briefings.size,
                                    onShareExcel = { triggerExcelExport(data, shareDirectly = true) },
                                    onSaveExcel = { triggerExcelExport(data, shareDirectly = false) },
                                    onSharePdf = { triggerPdfExport(data, shareDirectly = true) },
                                    onSavePdf = { triggerPdfExport(data, shareDirectly = false) }
                                )
                            }

                            BottomTab.TIM -> {
                                TeamRosterTabContent(
                                    teamMembers = data.teamMembers,
                                    onOpenWorkStatus = {
                                        viewModel.openModal(ActiveModal.WorkStatus())
                                    },
                                    onOpenWaReminder = {
                                        viewModel.openModal(ActiveModal.WhatsAppReminder)
                                    },
                                    onQuickChatMember = { member ->
                                        val (senderName, senderNumber) = WhatsAppReminderManager.resolveSender(
                                            context = context,
                                            currentUserName = cleanUserName,
                                            teamMembers = data.teamMembers
                                        )
                                        val summary = WhatsAppReminderManager.buildActivitySummaryMessage(
                                            senderNumber = senderNumber,
                                            workOrders = data.workOrders,
                                            dailyWorkReports = data.dailyWorkReports,
                                            scmJobs = data.scmJobs,
                                            briefings = data.briefings,
                                            senderName = senderName
                                        )
                                        val ok = WhatsAppReminderManager.openWhatsAppForMember(
                                            context = context,
                                            targetPhone = member.whatsappNumber,
                                            message = summary
                                        )
                                        if (!ok) {
                                            viewModel.showToast("Pastikan WhatsApp terpasang & nomor WA valid.")
                                        }
                                    },
                                    onAddMember = {
                                        viewModel.openModal(ActiveModal.TeamMemberEdit(null))
                                    },
                                    onEditMember = { member ->
                                        viewModel.openModal(ActiveModal.TeamMemberEdit(member))
                                    },
                                    onAddCustomJobTitle = { newTitle ->
                                        CustomJobTitleStore.addCustomTitle(context, newTitle)
                                        customJobRefreshTick++
                                        viewModel.showToast("Jabatan \"$newTitle\" ditambahkan.")
                                    }
                                )
                            }
                        }

                        // Modals
                        when (val modal = activeModal) {
                            ActiveModal.None -> Unit

                            is ActiveModal.WorkStatus -> {
                                WorkStatusSheet(
                                    teamMembers = data.teamMembers,
                                    workStatusLogs = data.workStatusLogs,
                                    defaultPersonName = cleanUserName.ifBlank { EiSeedDefaults.SUPERVISOR_NAME },
                                    initialPersonName = modal.initialPersonName,
                                    isSubmitting = isSubmitting,
                                    onDismiss = { viewModel.closeModal() },
                                    onSubmitStatus = { name, workStatus, destinationNote ->
                                        viewModel.submitWorkStatus(
                                            personName = name,
                                            workStatus = workStatus,
                                            destinationNote = destinationNote,
                                            teamMembers = data.teamMembers
                                        )
                                    }
                                )
                            }

                            is ActiveModal.WorkOrderInput -> {
                                val maxWoCode = data.workOrders.maxOfOrNull { EiSortHelpers.parseCodeNumber(it.woCode) } ?: 0
                                WorkOrderInputSheet(
                                    category = modal.category,
                                    teamMembers = data.teamMembers,
                                    isSubmitting = isSubmitting,
                                    onDismiss = { viewModel.closeModal() },
                                    onSubmit = { selectedCat, tag, tech, desc, status, beforeUris, afterUris ->
                                        viewModel.submitWorkOrder(
                                            category = selectedCat,
                                            equipmentTag = tag,
                                            technician = tech,
                                            description = desc,
                                            status = status,
                                            currentWorkOrderCount = maxOf(data.workOrders.size, maxWoCode),
                                            beforePhotoUris = beforeUris,
                                            afterPhotoUris = afterUris
                                        )
                                    }
                                )
                            }

                            is ActiveModal.WorkOrderEdit -> {
                                val targetWo = data.workOrders.firstOrNull { it.id == modal.workOrderId }
                                if (targetWo != null) {
                                    WorkOrderInputSheet(
                                        category = WorkOrderCategory.fromLabel(targetWo.category),
                                        teamMembers = data.teamMembers,
                                        existingWorkOrder = targetWo,
                                        isSubmitting = isSubmitting,
                                        onDismiss = { viewModel.closeModal() },
                                        onSubmit = { selectedCat, tag, tech, desc, status, beforeUris, afterUris ->
                                            viewModel.updateWorkOrder(
                                                existingWorkOrder = targetWo,
                                                category = selectedCat,
                                                equipmentTag = tag,
                                                technician = tech,
                                                description = desc,
                                                status = status,
                                                beforePhotoUris = beforeUris,
                                                afterPhotoUris = afterUris
                                            )
                                        }
                                    )
                                } else {
                                    LaunchedEffect(modal.workOrderId) {
                                        viewModel.closeModal()
                                    }
                                }
                            }

                            is ActiveModal.WorkOrderPhoto -> {
                                val targetWo = data.workOrders.firstOrNull { it.id == modal.workOrderId }
                                if (targetWo != null) {
                                    WorkOrderPhotoSheet(
                                        workOrder = targetWo,
                                        isSubmitting = isSubmitting,
                                        onDismiss = { viewModel.closeModal() },
                                        onPhotosUpdated = { isBefore, uris ->
                                            viewModel.updateWorkOrderPhotos(targetWo, isBefore, uris)
                                        },
                                        onCycleStatus = {
                                            viewModel.cycleWorkOrderStatus(targetWo)
                                        },
                                        onEditWorkOrder = {
                                            viewModel.openModal(ActiveModal.WorkOrderEdit(targetWo.id))
                                        }
                                    )
                                } else {
                                    LaunchedEffect(modal.workOrderId) {
                                        viewModel.closeModal()
                                    }
                                }
                            }

                            is ActiveModal.BriefingEdit -> {
                                val existingNotice = modal.briefing
                                BriefingEditSheet(
                                    existingBriefing = existingNotice,
                                    isSubmitting = isSubmitting,
                                    onDismiss = { viewModel.closeModal() },
                                    onSave = { badge, timeTag, title, summary, isSafety ->
                                        viewModel.saveBriefing(
                                            existingId = existingNotice?.id?.ifBlank { null },
                                            badge = badge,
                                            timeTag = timeTag,
                                            title = title,
                                            summary = summary,
                                            isSafetyAlert = isSafety,
                                            currentBriefings = data.briefings
                                        )
                                    },
                                    onDelete = existingNotice?.id?.takeIf { it.isNotBlank() }?.let { id ->
                                        { viewModel.deleteBriefing(id) }
                                    }
                                )
                            }

                            is ActiveModal.ScmJobInput -> {
                                val maxScmCode = data.scmJobs.maxOfOrNull { EiSortHelpers.parseCodeNumber(it.jobCode) } ?: 0
                                ScmJobInputSheet(
                                    initialCategory = modal.category,
                                    defaultPicName = cleanUserName,
                                    teamMembers = data.teamMembers,
                                    isSubmitting = isSubmitting,
                                    onDismiss = { viewModel.closeModal() },
                                    onSubmit = { cat, title, refNo, pic, dest, desc, status, beforeUris, afterUris ->
                                        viewModel.submitScmJob(
                                            category = cat,
                                            title = title,
                                            referenceNumber = refNo,
                                            picName = pic,
                                            destinationOrVendor = dest,
                                            description = desc,
                                            status = status,
                                            currentScmJobCount = maxOf(data.scmJobs.size, maxScmCode),
                                            beforePhotoUris = beforeUris,
                                            afterPhotoUris = afterUris
                                        )
                                    }
                                )
                            }

                            is ActiveModal.ScmJobEdit -> {
                                val targetScm = data.scmJobs.firstOrNull { it.id == modal.scmJobId }
                                if (targetScm != null) {
                                    ScmJobInputSheet(
                                        initialCategory = ScmJobCategory.fromLabel(targetScm.category),
                                        defaultPicName = cleanUserName,
                                        teamMembers = data.teamMembers,
                                        existingScmJob = targetScm,
                                        isSubmitting = isSubmitting,
                                        onDismiss = { viewModel.closeModal() },
                                        onSubmit = { cat, title, refNo, pic, dest, desc, status, beforeUris, afterUris ->
                                            viewModel.updateScmJob(
                                                existingScmJob = targetScm,
                                                category = cat,
                                                title = title,
                                                referenceNumber = refNo,
                                                picName = pic,
                                                destinationOrVendor = dest,
                                                description = desc,
                                                status = status,
                                                beforePhotoUris = beforeUris,
                                                afterPhotoUris = afterUris
                                            )
                                        },
                                        onDelete = targetScm.id.takeIf { it.isNotBlank() }?.let { id ->
                                            { viewModel.deleteScmJob(id) }
                                        }
                                    )
                                } else {
                                    LaunchedEffect(modal.scmJobId) {
                                        viewModel.closeModal()
                                    }
                                }
                            }

                            is ActiveModal.ScmJobPhoto -> {
                                val targetScm = data.scmJobs.firstOrNull { it.id == modal.scmJobId }
                                if (targetScm != null) {
                                    ScmJobPhotoSheet(
                                        scmJob = targetScm,
                                        isSubmitting = isSubmitting,
                                        onDismiss = { viewModel.closeModal() },
                                        onPhotosUpdated = { isBefore, uris ->
                                            viewModel.updateScmJobPhotos(targetScm, isBefore, uris)
                                        },
                                        onCycleStatus = {
                                            viewModel.cycleScmJobStatus(targetScm)
                                        },
                                        onEditScmJob = {
                                            viewModel.openModal(ActiveModal.ScmJobEdit(targetScm.id))
                                        }
                                    )
                                } else {
                                    LaunchedEffect(modal.scmJobId) {
                                        viewModel.closeModal()
                                    }
                                }
                            }

                            is ActiveModal.TeamMemberEdit -> {
                                val existingMember = modal.member
                                TeamMemberEditSheet(
                                    existingMember = existingMember,
                                    jobTitleOptions = mergedJobTitleOptions,
                                    isSubmitting = isSubmitting,
                                    onDismiss = { viewModel.closeModal() },
                                    onAddCustomJobTitle = { newTitle ->
                                        CustomJobTitleStore.addCustomTitle(context, newTitle)
                                        customJobRefreshTick++
                                    },
                                    onSave = { name, role, specialty, whatsappNumber ->
                                        CustomJobTitleStore.addCustomTitle(context, role)
                                        customJobRefreshTick++
                                        viewModel.saveTeamMember(
                                            existingId = existingMember?.id?.ifBlank { null },
                                            name = name,
                                            role = role,
                                            specialty = specialty,
                                            whatsappNumber = whatsappNumber,
                                            currentTeam = data.teamMembers
                                        )
                                    },
                                    onDelete = existingMember?.id?.takeIf { it.isNotBlank() }?.let { id ->
                                        { viewModel.deleteTeamMember(id) }
                                    }
                                )
                            }

                            ActiveModal.WhatsAppReminder -> {
                                WhatsAppReminderSheet(
                                    currentUserName = cleanUserName,
                                    teamMembers = data.teamMembers,
                                    workOrders = data.workOrders,
                                    dailyWorkReports = data.dailyWorkReports,
                                    scmJobs = data.scmJobs,
                                    briefings = data.briefings,
                                    onDismiss = { viewModel.closeModal() },
                                    onEditMember = { member ->
                                        viewModel.openModal(ActiveModal.TeamMemberEdit(member))
                                    },
                                    onToast = { msg -> viewModel.showToast(msg) }
                                )
                            }

                            is ActiveModal.DailyWorkReportInput -> {
                                val maxDwrCode = data.dailyWorkReports.maxOfOrNull { EiSortHelpers.parseCodeNumber(it.reportCode) } ?: 0
                                DailyWorkReportInputSheet(
                                    initialCategory = modal.category,
                                    teamMembers = data.teamMembers,
                                    defaultTechnicianName = cleanUserName,
                                    isSubmitting = isSubmitting,
                                    onDismiss = { viewModel.closeModal() },
                                    onSubmit = { workDate, techName, cat, equip, loc, act, startT, endT, res, stat, issues, photos ->
                                        viewModel.submitDailyWorkReport(
                                            workDate = workDate,
                                            technicianName = techName,
                                            category = cat,
                                            equipmentName = equip,
                                            workLocation = loc,
                                            activityDescription = act,
                                            startTime = startT,
                                            endTime = endT,
                                            workResult = res,
                                            status = stat,
                                            issuesFound = issues,
                                            currentReportCount = maxOf(data.dailyWorkReports.size, maxDwrCode),
                                            photoUris = photos
                                        )
                                    }
                                )
                            }

                            is ActiveModal.DailyWorkReportEdit -> {
                                val targetDaily = data.dailyWorkReports.firstOrNull { it.id == modal.reportId }
                                if (targetDaily != null) {
                                    DailyWorkReportInputSheet(
                                        initialCategory = DailyReportCategory.fromLabel(targetDaily.category),
                                        teamMembers = data.teamMembers,
                                        defaultTechnicianName = cleanUserName,
                                        existingReport = targetDaily,
                                        isSubmitting = isSubmitting,
                                        onDismiss = { viewModel.closeModal() },
                                        onSubmit = { workDate, techName, cat, equip, loc, act, startT, endT, res, stat, issues, photos ->
                                            viewModel.updateDailyWorkReport(
                                                existingReport = targetDaily,
                                                workDate = workDate,
                                                technicianName = techName,
                                                category = cat,
                                                equipmentName = equip,
                                                workLocation = loc,
                                                activityDescription = act,
                                                startTime = startT,
                                                endTime = endT,
                                                workResult = res,
                                                status = stat,
                                                issuesFound = issues,
                                                photoUris = photos
                                            )
                                        },
                                        onDelete = targetDaily.id.takeIf { it.isNotBlank() }?.let { id ->
                                            { viewModel.deleteDailyWorkReport(id) }
                                        }
                                    )
                                } else {
                                    LaunchedEffect(modal.reportId) {
                                        viewModel.closeModal()
                                    }
                                }
                            }

                            is ActiveModal.DailyWorkReportPhoto -> {
                                val targetDaily = data.dailyWorkReports.firstOrNull { it.id == modal.reportId }
                                if (targetDaily != null) {
                                    DailyWorkReportPhotoSheet(
                                        report = targetDaily,
                                        isSubmitting = isSubmitting,
                                        onDismiss = { viewModel.closeModal() },
                                        onPhotosUpdated = { uris ->
                                            viewModel.updateDailyWorkReportPhotos(targetDaily.id, uris)
                                        },
                                        onCycleStatus = {
                                            viewModel.cycleDailyWorkReportStatus(targetDaily)
                                        },
                                        onEditReport = {
                                            viewModel.openModal(ActiveModal.DailyWorkReportEdit(targetDaily.id))
                                        }
                                    )
                                } else {
                                    LaunchedEffect(modal.reportId) {
                                        viewModel.closeModal()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Keluar dari Akun?") },
            text = { Text("Data SCM dan EII Team tetap tersimpan aman di Firebase cloud.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        onSignOutRequested()
                    }
                ) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
    }
}

@Composable
private fun PicActivityMarqueeBanner(
    tickerText: String,
    picCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = CobaltLight.copy(alpha = 0.85f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CobaltPrimary.copy(alpha = 0.30f)),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("home_pic_marquee_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = CobaltPrimary,
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = "Notifikasi berjalan aktivitas PIC",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (picCount > 0) "PIC ($picCount)" else "PIC",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = tickerText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        velocity = 36.dp
                    )
            )
        }
    }
}

@Composable
private fun HomeRecentCategoryHeader(
    title: String,
    countLabel: String,
    badgeBg: Color,
    badgeFg: Color,
    testTag: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Surface(
                color = badgeBg,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = countLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeFg,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        TextButton(onClick = onSeeAll) {
            Text(
                text = "Lihat Semua",
                style = MaterialTheme.typography.labelMedium,
                color = CobaltPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HomeRecentEmptyHint(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.85f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SlateBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun EiBottomNavigationBar(
    selectedTab: BottomTab,
    onSelectTab: (BottomTab) -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 10.dp,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder.copy(alpha = 0.8f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Rounded.Home,
                    label = stringResource(R.string.nav_home),
                    selected = selectedTab == BottomTab.BERANDA,
                    testTag = "nav_beranda",
                    onClick = { onSelectTab(BottomTab.BERANDA) },
                    modifier = Modifier.weight(1f)
                )

                BottomNavItem(
                    icon = Icons.Rounded.Assessment,
                    label = stringResource(R.string.nav_activity),
                    selected = selectedTab == BottomTab.AKTIVITAS,
                    testTag = "nav_aktivitas",
                    onClick = { onSelectTab(BottomTab.AKTIVITAS) },
                    modifier = Modifier.weight(1f)
                )

                CenterPositionNavItem(
                    label = stringResource(R.string.nav_positions),
                    selected = selectedTab == BottomTab.POSISI,
                    onClick = { onSelectTab(BottomTab.POSISI) },
                    modifier = Modifier.weight(1f)
                )

                BottomNavItem(
                    icon = Icons.Rounded.Description,
                    label = stringResource(R.string.nav_reports),
                    selected = selectedTab == BottomTab.LAPORAN,
                    testTag = "nav_laporan",
                    onClick = { onSelectTab(BottomTab.LAPORAN) },
                    modifier = Modifier.weight(1f)
                )

                BottomNavItem(
                    icon = Icons.Rounded.Groups,
                    label = stringResource(R.string.nav_team),
                    selected = selectedTab == BottomTab.TIM,
                    testTag = "nav_tim",
                    onClick = { onSelectTab(BottomTab.TIM) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CenterPositionNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (selected) CobaltPrimary else SlateTextMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp)
            .testTag("nav_posisi")
    ) {
        Surface(
            color = if (selected) CobaltPrimary else CobaltLight,
            shape = CircleShape,
            border = if (selected) null else BorderStroke(1.dp, CobaltPrimary.copy(alpha = 0.25f)),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = label,
                    tint = if (selected) androidx.compose.ui.graphics.Color.White else CobaltPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = tint
        )
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (selected) CobaltPrimary else SlateTextMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = tint
        )
    }
}
