package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BriefingNotice
import com.example.data.model.DailyReportCategory
import com.example.data.model.DailyReportStatus
import com.example.data.model.DailyWorkReport
import com.example.data.model.EiSeedDefaults
import com.example.data.model.ScmJob
import com.example.data.model.ScmJobCategory
import com.example.data.model.ScmJobStatus
import com.example.data.model.TeamMember
import com.example.data.model.WorkOrder
import com.example.data.model.WorkOrderCategory
import com.example.data.model.WorkOrderStatus
import com.example.data.model.WorkPositionStatus
import com.example.data.model.WorkStatusLog
import com.example.data.model.WorkspaceProfile
import com.example.data.repository.EiTeamRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class EiDashboardData(
    val profile: WorkspaceProfile?,
    val teamMembers: List<TeamMember>,
    val workOrders: List<WorkOrder>,
    val workStatusLogs: List<WorkStatusLog>,
    val briefings: List<BriefingNotice>,
    val scmJobs: List<ScmJob> = emptyList(),
    val dailyWorkReports: List<DailyWorkReport> = emptyList()
)

enum class BottomTab {
    BERANDA,
    AKTIVITAS,
    POSISI,
    LAPORAN,
    TIM
}

enum class ActivitySubTab {
    EI_WORK,
    DAILY_REPORT,
    SCM_LOGISTICS
}

sealed interface ActiveModal {
    data object None : ActiveModal
    data class WorkStatus(val initialPersonName: String? = null) : ActiveModal
    data class WorkOrderInput(val category: WorkOrderCategory) : ActiveModal
    data class WorkOrderEdit(val workOrderId: String) : ActiveModal
    data class WorkOrderPhoto(val workOrderId: String) : ActiveModal
    data class DailyWorkReportInput(val category: DailyReportCategory = DailyReportCategory.DAILY_INSPECTION) : ActiveModal
    data class DailyWorkReportEdit(val reportId: String) : ActiveModal
    data class DailyWorkReportPhoto(val reportId: String) : ActiveModal
    data class ScmJobInput(val category: ScmJobCategory) : ActiveModal
    data class ScmJobEdit(val scmJobId: String) : ActiveModal
    data class ScmJobPhoto(val scmJobId: String) : ActiveModal
    data class BriefingEdit(val briefing: BriefingNotice?) : ActiveModal
    data class TeamMemberEdit(val member: TeamMember?) : ActiveModal
    data object WhatsAppReminder : ActiveModal
}

class EiTeamViewModel(
    private val repository: EiTeamRepository,
    private val currentUserId: String,
    private val userDisplayName: String
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(BottomTab.BERANDA)
    val selectedTab: StateFlow<BottomTab> = _selectedTab.asStateFlow()

    private val _activeModal = MutableStateFlow<ActiveModal>(ActiveModal.None)
    val activeModal: StateFlow<ActiveModal> = _activeModal.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<WorkOrderCategory?>(null)
    val selectedCategoryFilter: StateFlow<WorkOrderCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedActivitySubTab = MutableStateFlow(ActivitySubTab.EI_WORK)
    val selectedActivitySubTab: StateFlow<ActivitySubTab> = _selectedActivitySubTab.asStateFlow()

    private val _selectedScmFilter = MutableStateFlow<ScmJobCategory?>(null)
    val selectedScmFilter: StateFlow<ScmJobCategory?> = _selectedScmFilter.asStateFlow()

    private val _selectedDailyCategoryFilter = MutableStateFlow<DailyReportCategory?>(null)
    val selectedDailyCategoryFilter: StateFlow<DailyReportCategory?> = _selectedDailyCategoryFilter.asStateFlow()

    private val _selectedDailyTechFilter = MutableStateFlow<String?>(null)
    val selectedDailyTechFilter: StateFlow<String?> = _selectedDailyTechFilter.asStateFlow()

    private val _picSearchQuery = MutableStateFlow("")
    val picSearchQuery: StateFlow<String> = _picSearchQuery.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val dashboardState: StateFlow<UiState<EiDashboardData>> =
        combine(
            combine(
                repository.observeProfile(currentUserId),
                repository.observeTeamMembers(currentUserId),
                repository.observeWorkOrders(currentUserId)
            ) { profile, team, workOrders -> Triple(profile, team, workOrders) },
            combine(
                repository.observeWorkStatusLogs(currentUserId),
                repository.observeBriefings(currentUserId),
                repository.observeScmJobs(currentUserId)
            ) { workStatusLogs, briefings, scmJobs -> Triple(workStatusLogs, briefings, scmJobs) },
            repository.observeDailyWorkReports(currentUserId)
        ) { first, second, dailyReports ->
            EiDashboardData(
                profile = first.first,
                teamMembers = first.second,
                workOrders = first.third,
                workStatusLogs = second.first,
                briefings = second.second,
                scmJobs = second.third,
                dailyWorkReports = dailyReports
            )
        }
            .toUiStateFlow("Gagal memuat data SCM dan EII Team")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    init {
        viewModelScope.launch {
            repository.ensureWorkspaceSeeded(
                userId = currentUserId,
                defaultDisplayName = userDisplayName
            ).onFailure { error ->
                _toastMessage.value = error.localizedMessage ?: "Gagal inisialisasi workspace"
            }
        }
    }

    fun selectTab(tab: BottomTab) {
        _selectedTab.value = tab
    }

    fun selectActivitySubTab(subTab: ActivitySubTab) {
        _selectedActivitySubTab.value = subTab
    }

    fun openModal(modal: ActiveModal) {
        _activeModal.value = modal
    }

    fun closeModal() {
        _activeModal.value = ActiveModal.None
    }

    fun setMonitoringFilter(category: WorkOrderCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setScmFilter(category: ScmJobCategory?) {
        _selectedScmFilter.value = category
    }

    fun setDailyCategoryFilter(category: DailyReportCategory?) {
        _selectedDailyCategoryFilter.value = category
    }

    fun setDailyTechFilter(techName: String?) {
        _selectedDailyTechFilter.value = techName
    }

    fun setPicSearchQuery(query: String) {
        _picSearchQuery.value = query
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun consumeToast() {
        _toastMessage.value = null
    }

    fun completeWorkerProfile(workerName: String, jobTitle: String) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.completeWorkerProfile(
                userId = currentUserId,
                workerName = workerName,
                jobTitle = jobTitle
            ).onSuccess {
                _toastMessage.value = "Profil disimpan sebagai $workerName ($jobTitle)"
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal menyimpan profil pekerja"
            }
            _isSubmitting.value = false
        }
    }

    fun saveTeamMember(
        existingId: String?,
        name: String,
        role: String,
        specialty: String,
        whatsappNumber: String,
        currentTeam: List<TeamMember>
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = if (!existingId.isNullOrBlank()) {
                repository.updateTeamMember(
                    userId = currentUserId,
                    memberId = existingId,
                    name = name,
                    role = role,
                    specialty = specialty,
                    whatsappNumber = whatsappNumber
                )
            } else {
                val nextSortOrder = (currentTeam.maxOfOrNull { it.sortOrder } ?: -1L) + 1L
                repository.addTeamMember(
                    userId = currentUserId,
                    name = name,
                    role = role,
                    specialty = specialty,
                    whatsappNumber = whatsappNumber,
                    nextSortOrder = nextSortOrder
                )
            }
            result.onSuccess {
                _activeModal.value = ActiveModal.None
                _toastMessage.value = if (!existingId.isNullOrBlank()) {
                    "Data anggota team berhasil diperbarui!"
                } else {
                    "Anggota team baru berhasil ditambahkan!"
                }
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal menyimpan anggota team"
            }
            _isSubmitting.value = false
        }
    }

    fun deleteTeamMember(memberId: String) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deleteTeamMember(currentUserId, memberId)
                .onSuccess {
                    _activeModal.value = ActiveModal.None
                    _toastMessage.value = "Anggota team dihapus."
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Gagal menghapus anggota team"
                }
            _isSubmitting.value = false
        }
    }

    fun submitWorkOrder(
        category: WorkOrderCategory,
        equipmentTag: String,
        technician: String,
        description: String,
        status: WorkOrderStatus,
        currentWorkOrderCount: Int,
        beforePhotoUris: List<String> = emptyList(),
        afterPhotoUris: List<String> = emptyList()
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.addWorkOrder(
                userId = currentUserId,
                category = category,
                equipmentTag = equipmentTag,
                technician = technician,
                description = description,
                status = status,
                nextCodeNumber = currentWorkOrderCount + 1,
                beforePhotoUris = beforePhotoUris,
                afterPhotoUris = afterPhotoUris
            ).onSuccess {
                _activeModal.value = ActiveModal.None
                _toastMessage.value = "${category.label} berhasil disimpan!"
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal menyimpan pekerjaan"
            }
            _isSubmitting.value = false
        }
    }

    fun updateWorkOrder(
        existingWorkOrder: WorkOrder,
        category: WorkOrderCategory,
        equipmentTag: String,
        technician: String,
        description: String,
        status: WorkOrderStatus,
        beforePhotoUris: List<String>,
        afterPhotoUris: List<String>
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateWorkOrder(
                userId = currentUserId,
                existingWorkOrder = existingWorkOrder,
                category = category,
                equipmentTag = equipmentTag,
                technician = technician,
                description = description,
                status = status,
                beforePhotoUris = beforePhotoUris,
                afterPhotoUris = afterPhotoUris
            ).onSuccess {
                _activeModal.value = ActiveModal.None
                _toastMessage.value = "${existingWorkOrder.woCode} berhasil diperbarui!"
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal memperbarui pekerjaan"
            }
            _isSubmitting.value = false
        }
    }

    fun updateWorkOrderPhotos(
        existingWorkOrder: WorkOrder,
        isBeforePhoto: Boolean,
        photoUris: List<String>
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateWorkOrderPhotos(
                userId = currentUserId,
                existingWorkOrder = existingWorkOrder,
                isBeforePhoto = isBeforePhoto,
                photoUris = photoUris
            ).onSuccess {
                val label = if (isBeforePhoto) "Foto Sebelum Dikerjakan" else "Foto Sesudah Dikerjakan"
                _toastMessage.value = if (photoUris.isEmpty()) {
                    "$label dihapus."
                } else {
                    "$label (${photoUris.size}/4) tersimpan di Firebase!"
                }
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal menyimpan foto pekerjaan"
            }
            _isSubmitting.value = false
        }
    }

    fun cycleWorkOrderStatus(workOrder: WorkOrder) {
        viewModelScope.launch {
            val nextStatus = WorkOrderStatus.fromLabel(workOrder.status).next()
            repository.cycleWorkOrderStatus(currentUserId, workOrder)
                .onSuccess {
                    _toastMessage.value = "Status ${workOrder.woCode} diubah menjadi ${nextStatus.label}"
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Gagal memperbarui status"
                }
        }
    }

    fun submitWorkStatus(
        personName: String,
        workStatus: WorkPositionStatus,
        destinationNote: String,
        teamMembers: List<TeamMember>
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            val sanitizedTarget = EiSeedDefaults.sanitizePersonNameOnly(personName)
            val matchingMember = teamMembers.firstOrNull {
                EiSeedDefaults.sanitizePersonNameOnly(it.name).equals(sanitizedTarget, ignoreCase = true)
            }
            repository.submitWorkStatus(
                userId = currentUserId,
                personName = sanitizedTarget.ifEmpty { personName.trim() },
                workStatus = workStatus,
                destinationNote = destinationNote,
                matchingMemberId = matchingMember?.id
            ).onSuccess {
                _activeModal.value = ActiveModal.None
                val noteSuffix = if (destinationNote.isNotBlank()) " (${destinationNote.trim()})" else ""
                _toastMessage.value = "Status posisi $personName: ${workStatus.label}$noteSuffix disimpan!"
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal menyimpan status posisi kerja"
            }
            _isSubmitting.value = false
        }
    }

    fun saveBriefing(
        existingId: String?,
        badge: String,
        timeTag: String,
        title: String,
        summary: String,
        isSafetyAlert: Boolean,
        currentBriefings: List<BriefingNotice>
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = if (!existingId.isNullOrBlank()) {
                repository.updateBriefing(
                    userId = currentUserId,
                    briefingId = existingId,
                    badge = badge,
                    timeTag = timeTag,
                    title = title,
                    summary = summary,
                    isSafetyAlert = isSafetyAlert
                )
            } else {
                val nextSortOrder = (currentBriefings.maxOfOrNull { it.sortOrder } ?: -1L) + 1L
                repository.addBriefing(
                    userId = currentUserId,
                    badge = badge,
                    timeTag = timeTag,
                    title = title,
                    summary = summary,
                    isSafetyAlert = isSafetyAlert,
                    nextSortOrder = nextSortOrder
                )
            }
            result.onSuccess {
                _activeModal.value = ActiveModal.None
                _toastMessage.value = if (!existingId.isNullOrBlank()) {
                    "Info briefing berhasil diperbarui!"
                } else {
                    "Info briefing baru berhasil ditambahkan!"
                }
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal menyimpan info briefing"
            }
            _isSubmitting.value = false
        }
    }

    fun deleteBriefing(briefingId: String) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deleteBriefing(currentUserId, briefingId)
                .onSuccess {
                    _activeModal.value = ActiveModal.None
                    _toastMessage.value = "Info briefing dihapus."
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Gagal menghapus info briefing"
                }
            _isSubmitting.value = false
        }
    }

    fun submitScmJob(
        category: ScmJobCategory,
        title: String,
        referenceNumber: String,
        picName: String,
        destinationOrVendor: String,
        description: String,
        status: ScmJobStatus,
        currentScmJobCount: Int,
        beforePhotoUris: List<String> = emptyList(),
        afterPhotoUris: List<String> = emptyList()
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.addScmJob(
                userId = currentUserId,
                category = category,
                title = title,
                referenceNumber = referenceNumber,
                picName = picName,
                destinationOrVendor = destinationOrVendor,
                description = description,
                status = status,
                nextCodeNumber = currentScmJobCount + 1,
                beforePhotoUris = beforePhotoUris,
                afterPhotoUris = afterPhotoUris
            ).onSuccess {
                _activeModal.value = ActiveModal.None
                _toastMessage.value = "${category.label} berhasil ditambahkan!"
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal menyimpan job SCM"
            }
            _isSubmitting.value = false
        }
    }

    fun updateScmJob(
        existingScmJob: ScmJob,
        category: ScmJobCategory,
        title: String,
        referenceNumber: String,
        picName: String,
        destinationOrVendor: String,
        description: String,
        status: ScmJobStatus,
        beforePhotoUris: List<String>,
        afterPhotoUris: List<String>
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateScmJob(
                userId = currentUserId,
                existingScmJob = existingScmJob,
                category = category,
                title = title,
                referenceNumber = referenceNumber,
                picName = picName,
                destinationOrVendor = destinationOrVendor,
                description = description,
                status = status,
                beforePhotoUris = beforePhotoUris,
                afterPhotoUris = afterPhotoUris
            ).onSuccess {
                _activeModal.value = ActiveModal.None
                _toastMessage.value = "${existingScmJob.jobCode} berhasil diperbarui!"
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal memperbarui job SCM"
            }
            _isSubmitting.value = false
        }
    }

    fun updateScmJobPhotos(
        existingScmJob: ScmJob,
        isBeforePhoto: Boolean,
        photoUris: List<String>
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateScmJobPhotos(
                userId = currentUserId,
                existingScmJob = existingScmJob,
                isBeforePhoto = isBeforePhoto,
                photoUris = photoUris
            ).onSuccess {
                val label = if (isBeforePhoto) "Foto Sebelum (SCM)" else "Foto Sesudah (SCM)"
                _toastMessage.value = if (photoUris.isEmpty()) {
                    "$label dihapus."
                } else {
                    "$label (${photoUris.size}/4) tersimpan di Firebase!"
                }
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal menyimpan foto SCM"
            }
            _isSubmitting.value = false
        }
    }

    fun cycleScmJobStatus(scmJob: ScmJob) {
        viewModelScope.launch {
            val nextStatus = ScmJobStatus.fromLabel(scmJob.status).next()
            repository.cycleScmJobStatus(currentUserId, scmJob)
                .onSuccess {
                    _toastMessage.value = "Status ${scmJob.jobCode} diubah menjadi ${nextStatus.label}"
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Gagal memperbarui status job SCM"
                }
        }
    }

    fun deleteScmJob(scmJobId: String) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deleteScmJob(currentUserId, scmJobId)
                .onSuccess {
                    _activeModal.value = ActiveModal.None
                    _toastMessage.value = "Job SCM dihapus."
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Gagal menghapus job SCM"
                }
            _isSubmitting.value = false
        }
    }

    fun submitDailyWorkReport(
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
        currentReportCount: Int,
        photoUris: List<String> = emptyList()
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.addDailyWorkReport(
                userId = currentUserId,
                workDate = workDate,
                technicianName = technicianName,
                category = category,
                equipmentName = equipmentName,
                workLocation = workLocation,
                activityDescription = activityDescription,
                startTime = startTime,
                endTime = endTime,
                workResult = workResult,
                status = status,
                issuesFound = issuesFound,
                nextCodeNumber = currentReportCount + 1,
                photoUris = photoUris
            ).onSuccess {
                _activeModal.value = ActiveModal.None
                _toastMessage.value = "Daily Work Report berhasil disimpan!"
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal menyimpan Daily Work Report"
            }
            _isSubmitting.value = false
        }
    }

    fun updateDailyWorkReport(
        existingReport: DailyWorkReport,
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
    ) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateDailyWorkReport(
                userId = currentUserId,
                existingReport = existingReport,
                workDate = workDate,
                technicianName = technicianName,
                category = category,
                equipmentName = equipmentName,
                workLocation = workLocation,
                activityDescription = activityDescription,
                startTime = startTime,
                endTime = endTime,
                workResult = workResult,
                status = status,
                issuesFound = issuesFound,
                photoUris = photoUris
            ).onSuccess {
                _activeModal.value = ActiveModal.None
                _toastMessage.value = "${existingReport.reportCode} berhasil diperbarui!"
            }.onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "Gagal memperbarui Daily Work Report"
            }
            _isSubmitting.value = false
        }
    }

    fun updateDailyWorkReportPhotos(reportId: String, photoUris: List<String>) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateDailyWorkReportPhotos(currentUserId, reportId, photoUris)
                .onSuccess {
                    _toastMessage.value = if (photoUris.isEmpty()) {
                        "Foto dokumentasi dihapus."
                    } else {
                        "Foto dokumentasi (${photoUris.size}/4) tersimpan di Firebase!"
                    }
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Gagal memperbarui foto dokumentasi"
                }
            _isSubmitting.value = false
        }
    }

    fun cycleDailyWorkReportStatus(report: DailyWorkReport) {
        viewModelScope.launch {
            val nextStatus = DailyReportStatus.fromLabel(report.status).next()
            repository.cycleDailyWorkReportStatus(currentUserId, report)
                .onSuccess {
                    _toastMessage.value = "Status ${report.reportCode} diubah menjadi ${nextStatus.label}"
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Gagal mengubah status"
                }
        }
    }

    fun deleteDailyWorkReport(reportId: String) {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deleteDailyWorkReport(currentUserId, reportId)
                .onSuccess {
                    _activeModal.value = ActiveModal.None
                    _toastMessage.value = "Daily Work Report dihapus."
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "Gagal menghapus Daily Work Report"
                }
            _isSubmitting.value = false
        }
    }

    private fun <T> Flow<T>.toUiStateFlow(fallbackMessage: String): Flow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing dashboard state", rootError)
                emit(UiState.Error(rootError.message ?: fallbackMessage))
            }

    private companion object {
        const val TAG = "EiTeamVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
