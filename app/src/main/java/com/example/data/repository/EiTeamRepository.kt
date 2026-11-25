package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.BriefingNotice
import com.example.data.model.DailyReportCategory
import com.example.data.model.DailyReportStatus
import com.example.data.model.DailyWorkReport
import com.example.data.model.EiSeedDefaults
import com.example.data.model.EiSortHelpers
import com.example.data.model.RoleMode
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EiTeamRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val sharedWorkspaceRef: DocumentReference
        get() = db.collection("workspaces").document(EiSeedDefaults.SHARED_WORKSPACE_ID)

    private fun requireUserId(expectedUserId: String): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google terlebih dahulu.")
        check(uid == expectedUserId) { "Sesi akun berubah; operasi dibatalkan." }
        return uid
    }

    fun observeProfile(userId: String): Flow<WorkspaceProfile?> = flow {
        val uid = requireUserId(userId)
        val docRef = db.collection("users").document(uid)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            WorkspaceProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    handleFlowException(error, OperationType.GET, docRef.path, uid)
                    throw error
                }
        )
    }

    fun observeTeamMembers(userId: String): Flow<List<TeamMember>> = flow {
        val uid = requireUserId(userId)
        val colRef = sharedWorkspaceRef.collection("team_members")
        emitAll(
            colRef.whereEqualTo("workspaceId", EiSeedDefaults.SHARED_WORKSPACE_ID)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TeamMember::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(compareBy<TeamMember> { it.sortOrder }.thenBy { it.name })
                }
                .catch { error ->
                    handleFlowException(error, OperationType.LIST, colRef.path, uid)
                    throw error
                }
        )
    }

    fun observeWorkOrders(userId: String): Flow<List<WorkOrder>> = flow {
        val uid = requireUserId(userId)
        val colRef = sharedWorkspaceRef.collection("work_orders")
        emitAll(
            colRef.whereEqualTo("workspaceId", EiSeedDefaults.SHARED_WORKSPACE_ID)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        WorkOrder::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).let { EiSortHelpers.sortWorkOrders(it) }
                }
                .catch { error ->
                    handleFlowException(error, OperationType.LIST, colRef.path, uid)
                    throw error
                }
        )
    }

    fun observeWorkStatusLogs(userId: String): Flow<List<WorkStatusLog>> = flow {
        val uid = requireUserId(userId)
        val colRef = sharedWorkspaceRef.collection("work_status_logs")
        emitAll(
            colRef.whereEqualTo("workspaceId", EiSeedDefaults.SHARED_WORKSPACE_ID)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        WorkStatusLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                }
                .catch { error ->
                    handleFlowException(error, OperationType.LIST, colRef.path, uid)
                    throw error
                }
        )
    }

    fun observeBriefings(userId: String): Flow<List<BriefingNotice>> = flow {
        val uid = requireUserId(userId)
        val colRef = sharedWorkspaceRef.collection("briefings")
        emitAll(
            colRef.whereEqualTo("workspaceId", EiSeedDefaults.SHARED_WORKSPACE_ID)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        BriefingNotice::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.sortOrder }
                }
                .catch { error ->
                    handleFlowException(error, OperationType.LIST, colRef.path, uid)
                    throw error
                }
        )
    }

    fun observeScmJobs(userId: String): Flow<List<ScmJob>> = flow {
        val uid = requireUserId(userId)
        val colRef = sharedWorkspaceRef.collection("scm_jobs")
        emitAll(
            colRef.whereEqualTo("workspaceId", EiSeedDefaults.SHARED_WORKSPACE_ID)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ScmJob::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).let { EiSortHelpers.sortScmJobs(it) }
                }
                .catch { error ->
                    handleFlowException(error, OperationType.LIST, colRef.path, uid)
                    throw error
                }
        )
    }

    fun observeDailyWorkReports(userId: String): Flow<List<DailyWorkReport>> = flow {
        val uid = requireUserId(userId)
        val colRef = sharedWorkspaceRef.collection("daily_work_reports")
        emitAll(
            colRef.whereEqualTo("workspaceId", EiSeedDefaults.SHARED_WORKSPACE_ID)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        DailyWorkReport::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).let { EiSortHelpers.sortDailyWorkReports(it) }
                }
                .catch { error ->
                    handleFlowException(error, OperationType.LIST, colRef.path, uid)
                    throw error
                }
        )
    }

    suspend fun ensureWorkspaceSeeded(
        userId: String,
        defaultDisplayName: String
    ): Result<Unit> {
        val userDocPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val cleanDisplayName = defaultDisplayName.trim().ifEmpty { EiSeedDefaults.SUPERVISOR_NAME }.take(80)
            val serverNow = FieldValue.serverTimestamp()

            // 1. Ensure personal worker profile document exists
            val userRef = db.collection("users").document(uid)
            val userSnap = userRef.get().await()
            if (!userSnap.exists()) {
                userRef.set(
                    mapOf(
                        "userId" to uid,
                        "displayName" to cleanDisplayName,
                        "jobTitle" to EiSeedDefaults.DEFAULT_JOB_TITLE,
                        "activeRole" to RoleMode.SUPERVISOR.label,
                        "activeMemberName" to cleanDisplayName,
                        "initialized" to true,
                        "profileCompleted" to true,
                        "createdAt" to serverNow,
                        "updatedAt" to serverNow
                    )
                ).await()
            }

            // 2. Ensure shared team workspace exists & is seeded
            val wsRef = sharedWorkspaceRef
            val wsSnap = wsRef.get().await()
            if (wsSnap.exists()) {
                return Result.success(Unit)
            }

            seedSharedWorkspace(wsRef, userRef, uid)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userDocPath)
            Result.failure(e)
        }
    }

    private suspend fun seedSharedWorkspace(
        wsRef: DocumentReference,
        legacyUserRef: DocumentReference,
        uid: String
    ) {
        val batch = db.batch()
        val serverNow = FieldValue.serverTimestamp()
        val wsId = EiSeedDefaults.SHARED_WORKSPACE_ID

        batch.set(
            wsRef,
            mapOf(
                "workspaceId" to wsId,
                "userId" to uid,
                "initialized" to true,
                "createdAt" to serverNow,
                "updatedAt" to serverNow
            )
        )

        // Check if legacy user-scoped data exists from earlier single-user schema and migrate it cleanly
        val legacyTeam = runCatching { legacyUserRef.collection("team_members").get().await().documents }.getOrDefault(emptyList())
        val legacyWos = runCatching { legacyUserRef.collection("work_orders").get().await().documents }.getOrDefault(emptyList())
        val legacyBriefings = runCatching { legacyUserRef.collection("briefings").get().await().documents }.getOrDefault(emptyList())
        val legacyScm = runCatching { legacyUserRef.collection("scm_jobs").get().await().documents }.getOrDefault(emptyList())

        if (legacyTeam.isNotEmpty()) {
            legacyTeam.forEach { doc ->
                val data = doc.data.orEmpty()
                val rawName = data["name"] as? String ?: ""
                val resolvedName = (LEGACY_NAME_MIGRATIONS[rawName] ?: rawName).ifBlank { "Anggota Team" }.take(80)
                batch.set(
                    wsRef.collection("team_members").document(doc.id),
                    mapOf(
                        "workspaceId" to wsId,
                        "userId" to uid,
                        "name" to resolvedName,
                        "role" to ((data["role"] as? String)?.take(80)?.ifBlank { "Teknisi Electrical & Instrumentation" } ?: "Teknisi Electrical & Instrumentation"),
                        "specialty" to ((data["specialty"] as? String)?.take(100)?.ifBlank { "SCM & EII Operations" } ?: "SCM & EII Operations"),
                        "whatsappNumber" to EiSeedDefaults.normalizeWhatsappDisplay(data["whatsappNumber"] as? String ?: ""),
                        "status" to ((data["status"] as? String)?.takeIf { raw ->
                            WorkPositionStatus.entries.any { it.label == raw }
                        } ?: WorkPositionStatus.KANTOR.label),
                        "workNote" to "",
                        "lastTime" to ((data["lastTime"] as? String)?.take(40)?.ifBlank { "08:00" } ?: "08:00"),
                        "sortOrder" to ((data["sortOrder"] as? Number)?.toLong()?.coerceIn(0L, 500L) ?: 0L),
                        "createdAt" to serverNow,
                        "updatedAt" to serverNow
                    )
                )
            }
        } else {
            val initialTeam = listOf(
                Triple(EiSeedDefaults.SUPERVISOR_NAME, "Supervisor E&II", "Lead SCM & Electrical Instrumentation") to (WorkPositionStatus.KANTOR.label to "08:00"),
                Triple(EiSeedDefaults.TEKNISI_1_NAME, "Teknisi Electrical & Instrumentation", "Instrument & Calibration") to (WorkPositionStatus.WORKSHOP.label to "08:00"),
                Triple(EiSeedDefaults.TEKNISI_2_NAME, "Teknisi Build & Wiring", "Electrical Power & Motor") to (WorkPositionStatus.WORKSHOP.label to "08:00"),
                Triple(EiSeedDefaults.TEKNISI_3_NAME, "Teknisi Calibration Instrumen", "Instrument & Transmitter") to (WorkPositionStatus.KANTOR.label to "08:00"),
                Triple(EiSeedDefaults.TEKNISI_4_NAME, "Teknisi Electrical & Instrumentation", "Electrical Panel & Wiring") to (WorkPositionStatus.WORKSHOP.label to "08:00"),
                Triple(EiSeedDefaults.TEKNISI_5_NAME, "Teknisi Mechanical", "Rotating & PLC Skid") to (WorkPositionStatus.WORKSHOP.label to "08:00"),
                Triple(EiSeedDefaults.TEKNISI_6_NAME, "Admin SCM", "Pengadaan & Logistik") to (WorkPositionStatus.KANTOR.label to "08:00")
            )
            initialTeam.forEachIndexed { index, (identity, statusPair) ->
                val memberRef = wsRef.collection("team_members").document("member_$index")
                batch.set(
                    memberRef,
                    mapOf(
                        "workspaceId" to wsId,
                        "userId" to uid,
                        "name" to identity.first,
                        "role" to identity.second,
                        "specialty" to identity.third,
                        "whatsappNumber" to "",
                        "status" to statusPair.first,
                        "workNote" to "",
                        "lastTime" to statusPair.second,
                        "sortOrder" to index.toLong(),
                        "createdAt" to serverNow,
                        "updatedAt" to serverNow
                    )
                )
            }
        }

        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date())
        if (legacyWos.isNotEmpty()) {
            legacyWos.forEach { doc ->
                val data = doc.data.orEmpty()
                val rawTech = data["technician"] as? String ?: ""
                val resolvedTech = (LEGACY_NAME_MIGRATIONS[rawTech] ?: rawTech).ifBlank { EiSeedDefaults.TEKNISI_1_NAME }.take(80)
                val map = mutableMapOf<String, Any>(
                    "workspaceId" to wsId,
                    "userId" to uid,
                    "woCode" to ((data["woCode"] as? String)?.take(30)?.ifBlank { "WO-001" } ?: "WO-001"),
                    "category" to ((data["category"] as? String) ?: WorkOrderCategory.PM.label),
                    "equipmentTag" to ((data["equipmentTag"] as? String)?.take(120)?.ifBlank { "-" } ?: "-"),
                    "technician" to resolvedTech,
                    "description" to ((data["description"] as? String)?.take(1000)?.ifBlank { "-" } ?: "-"),
                    "status" to ((data["status"] as? String) ?: WorkOrderStatus.OPEN.label),
                    "reportDate" to ((data["reportDate"] as? String)?.take(40)?.ifBlank { dateStr } ?: dateStr),
                    "createdAt" to serverNow,
                    "updatedAt" to serverNow
                )
                val beforeList = sanitizePhotoList(
                    (data["beforePhotoUris"] as? List<*>)?.filterIsInstance<String>().orEmpty() +
                        listOfNotNull(data["beforePhotoUri"] as? String)
                )
                val afterList = sanitizePhotoList(
                    (data["afterPhotoUris"] as? List<*>)?.filterIsInstance<String>().orEmpty() +
                        listOfNotNull(data["afterPhotoUri"] as? String)
                )
                if (beforeList.isNotEmpty()) {
                    map["beforePhotoUri"] = beforeList.first()
                    map["beforePhotoUris"] = beforeList
                }
                if (afterList.isNotEmpty()) {
                    map["afterPhotoUri"] = afterList.first()
                    map["afterPhotoUris"] = afterList
                }
                batch.set(wsRef.collection("work_orders").document(doc.id), map)
            }
        } else {
            val initialWorkOrders = listOf(
                listOf("WO-001", WorkOrderCategory.PM.label, "PT-101A (Suction Pressure)", EiSeedDefaults.TEKNISI_1_NAME, "Cek kalibrasi 4-20mA dan pembersihan impulse line.", WorkOrderStatus.DONE.label),
                listOf("WO-002", WorkOrderCategory.BREAKDOWN.label, "MTR-204 (Cooling Fan)", EiSeedDefaults.TEKNISI_2_NAME, "Thermal overload trip, pengecekan megger insulasi motor & contactor.", WorkOrderStatus.IN_PROGRESS.label),
                listOf("WO-003", WorkOrderCategory.CM.label, "PLC-S7-1200 Skid 02", EiSeedDefaults.TEKNISI_5_NAME, "Penggantian module analog input SM1231 channel 2 rusak.", WorkOrderStatus.OPEN.label),
                listOf("WO-004", WorkOrderCategory.FUNCTION_TEST.label, "PSV-301 & Solenoid Valve", EiSeedDefaults.TEKNISI_3_NAME, "Inspeksi visual dan function test solenoid shutdown valve.", WorkOrderStatus.IN_PROGRESS.label)
            )
            initialWorkOrders.forEachIndexed { index, item ->
                val woRef = wsRef.collection("work_orders").document("wo_seed_${index + 1}")
                batch.set(
                    woRef,
                    mapOf(
                        "workspaceId" to wsId,
                        "userId" to uid,
                        "woCode" to item[0],
                        "category" to item[1],
                        "equipmentTag" to item[2],
                        "technician" to item[3],
                        "description" to item[4],
                        "status" to item[5],
                        "reportDate" to dateStr,
                        "createdAt" to serverNow,
                        "updatedAt" to serverNow
                    )
                )
            }
        }

        val initialWorkStatuses = listOf(
            Triple(EiSeedDefaults.TEKNISI_1_NAME, WorkPositionStatus.WORKSHOP.label, "") to "08:00 • $dateStr",
            Triple(EiSeedDefaults.TEKNISI_6_NAME, WorkPositionStatus.KANTOR.label, "") to "08:00 • $dateStr"
        )
        initialWorkStatuses.forEachIndexed { index, (meta, time) ->
            val logRef = wsRef.collection("work_status_logs").document("ws_seed_${index + 1}")
            batch.set(
                logRef,
                mapOf(
                    "workspaceId" to wsId,
                    "userId" to uid,
                    "personName" to meta.first,
                    "workStatus" to meta.second,
                    "destinationNote" to meta.third,
                    "timeLabel" to time.take(40),
                    "createdAt" to serverNow,
                    "updatedAt" to serverNow
                )
            )
        }

        if (legacyBriefings.isNotEmpty()) {
            legacyBriefings.forEach { doc ->
                val data = doc.data.orEmpty()
                batch.set(
                    wsRef.collection("briefings").document(doc.id),
                    mapOf(
                        "workspaceId" to wsId,
                        "userId" to uid,
                        "badge" to ((data["badge"] as? String)?.take(40)?.ifBlank { "BRIEFING" } ?: "BRIEFING"),
                        "timeTag" to ((data["timeTag"] as? String)?.take(40)?.ifBlank { "Hari Ini" } ?: "Hari Ini"),
                        "title" to ((data["title"] as? String)?.take(100)?.ifBlank { "Info Briefing" } ?: "Info Briefing"),
                        "summary" to ((data["summary"] as? String)?.take(500)?.ifBlank { "-" } ?: "-"),
                        "isSafetyAlert" to ((data["isSafetyAlert"] as? Boolean) ?: false),
                        "sortOrder" to ((data["sortOrder"] as? Number)?.toLong()?.coerceIn(0L, 500L) ?: 0L),
                        "createdAt" to serverNow,
                        "updatedAt" to serverNow
                    )
                )
            }
        } else {
            EiSeedDefaults.briefings.forEachIndexed { index, notice ->
                val briefingRef = wsRef.collection("briefings").document("briefing_$index")
                batch.set(
                    briefingRef,
                    mapOf(
                        "workspaceId" to wsId,
                        "userId" to uid,
                        "badge" to notice.badge,
                        "timeTag" to notice.timeTag,
                        "title" to notice.title,
                        "summary" to notice.summary,
                        "isSafetyAlert" to notice.isSafetyAlert,
                        "sortOrder" to index.toLong(),
                        "createdAt" to serverNow,
                        "updatedAt" to serverNow
                    )
                )
            }
        }

        if (legacyScm.isNotEmpty()) {
            legacyScm.forEach { doc ->
                val data = doc.data.orEmpty()
                val map = mutableMapOf<String, Any>(
                    "workspaceId" to wsId,
                    "userId" to uid,
                    "jobCode" to ((data["jobCode"] as? String)?.take(30)?.ifBlank { "SCM-001" } ?: "SCM-001"),
                    "category" to ((data["category"] as? String) ?: ScmJobCategory.PURCHASE_REQUEST.label),
                    "title" to ((data["title"] as? String)?.take(140)?.ifBlank { "-" } ?: "-"),
                    "referenceNumber" to ((data["referenceNumber"] as? String)?.take(80) ?: ""),
                    "picName" to ((data["picName"] as? String)?.take(80)?.ifBlank { "Admin SCM" } ?: "Admin SCM"),
                    "destinationOrVendor" to ((data["destinationOrVendor"] as? String)?.take(120) ?: ""),
                    "description" to ((data["description"] as? String)?.take(1000)?.ifBlank { "-" } ?: "-"),
                    "status" to ((data["status"] as? String) ?: ScmJobStatus.OPEN.label),
                    "jobDate" to ((data["jobDate"] as? String)?.take(40)?.ifBlank { dateStr } ?: dateStr),
                    "createdAt" to serverNow,
                    "updatedAt" to serverNow
                )
                val beforeList = sanitizePhotoList(
                    (data["beforePhotoUris"] as? List<*>)?.filterIsInstance<String>().orEmpty() +
                        listOfNotNull(data["beforePhotoUri"] as? String)
                )
                val afterList = sanitizePhotoList(
                    (data["afterPhotoUris"] as? List<*>)?.filterIsInstance<String>().orEmpty() +
                        listOfNotNull(data["afterPhotoUri"] as? String)
                )
                if (beforeList.isNotEmpty()) {
                    map["beforePhotoUri"] = beforeList.first()
                    map["beforePhotoUris"] = beforeList
                }
                if (afterList.isNotEmpty()) {
                    map["afterPhotoUri"] = afterList.first()
                    map["afterPhotoUris"] = afterList
                }
                batch.set(wsRef.collection("scm_jobs").document(doc.id), map)
            }
        } else {
            EiSeedDefaults.scmJobs.forEachIndexed { index, (meta, details) ->
                val scmRef = wsRef.collection("scm_jobs").document("scm_seed_${index + 1}")
                val statusLabel = when (index) {
                    0 -> ScmJobStatus.PROSES.label
                    2 -> ScmJobStatus.SELESAI.label
                    else -> ScmJobStatus.OPEN.label
                }
                batch.set(
                    scmRef,
                    mapOf(
                        "workspaceId" to wsId,
                        "userId" to uid,
                        "jobCode" to String.format(Locale.US, "SCM-%03d", index + 1),
                        "category" to meta.first.label,
                        "title" to meta.second,
                        "referenceNumber" to meta.third,
                        "picName" to details.first,
                        "destinationOrVendor" to details.second,
                        "description" to details.third,
                        "status" to statusLabel,
                        "jobDate" to dateStr,
                        "createdAt" to serverNow,
                        "updatedAt" to serverNow
                    )
                )
            }
        }

        val seedDailyReports = listOf(
            listOf(
                "DWR-001",
                dateStr,
                EiSeedDefaults.TEKNISI_1_NAME,
                DailyReportCategory.OTHER_ACTIVITY.label,
                "All Plant Area",
                "Control Room / Workshop",
                "Morning briefing & koordinasi safety harian",
                "08.00",
                "08.30",
                "Rencana kerja shift disepakati seluruh personel",
                DailyReportStatus.SELESAI.label,
                "-"
            ),
            listOf(
                "DWR-002",
                dateStr,
                EiSeedDefaults.TEKNISI_1_NAME,
                DailyReportCategory.DAILY_INSPECTION.label,
                "PT-101 Pressure Transmitter",
                "Compressor Skid 01",
                "Pemeriksaan pressure transmitter dan pengecekan impulse line",
                "08.30",
                "10.00",
                "Zero drift normal, pembacaan 4-20mA akurat",
                DailyReportStatus.SELESAI.label,
                "-"
            ),
            listOf(
                "DWR-003",
                dateStr,
                EiSeedDefaults.TEKNISI_3_NAME,
                DailyReportCategory.CALIBRATION.label,
                "TT-204 Temperature Transmitter",
                "Process Line Area B",
                "Kalibrasi temperature transmitter",
                "10.00",
                "12.00",
                "Terkalibrasi 5 titik (0-150 C) deviasi < 0.2%",
                DailyReportStatus.SELESAI.label,
                "-"
            )
        )
        seedDailyReports.forEachIndexed { index, row ->
            val dwrRef = wsRef.collection("daily_work_reports").document("dwr_seed_${index + 1}")
            batch.set(
                dwrRef,
                mapOf(
                    "workspaceId" to wsId,
                    "userId" to uid,
                    "reportCode" to row[0],
                    "workDate" to row[1],
                    "technicianName" to row[2],
                    "category" to row[3],
                    "equipmentName" to row[4],
                    "workLocation" to row[5],
                    "activityDescription" to row[6],
                    "startTime" to row[7],
                    "endTime" to row[8],
                    "workResult" to row[9],
                    "status" to row[10],
                    "issuesFound" to row[11],
                    "createdAt" to serverNow,
                    "updatedAt" to serverNow
                )
            )
        }

        batch.commit().await()
    }

    suspend fun completeWorkerProfile(
        userId: String,
        workerName: String,
        jobTitle: String
    ): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val cleanName = workerName.trim().take(80)
            val cleanJob = jobTitle.trim().ifEmpty { EiSeedDefaults.DEFAULT_JOB_TITLE }.take(80)
            require(cleanName.isNotEmpty()) { "Nama pekerja wajib diisi." }
            val derivedRole = EiSeedDefaults.roleForJobTitle(cleanJob)
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "displayName" to cleanName,
                        "jobTitle" to cleanJob,
                        "activeRole" to derivedRole.label,
                        "activeMemberName" to cleanName,
                        "profileCompleted" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addTeamMember(
        userId: String,
        name: String,
        role: String,
        specialty: String,
        whatsappNumber: String = "",
        nextSortOrder: Long
    ): Result<Unit> {
        var docPath = "${sharedWorkspaceRef.path}/team_members"
        return try {
            val uid = requireUserId(userId)
            val cleanName = name.trim().take(80)
            val cleanRole = role.trim().ifEmpty { EiSeedDefaults.jobTitleOptions.first() }.take(80)
            val cleanSpecialty = specialty.trim().ifEmpty { "SCM & EII Team" }.take(100)
            val cleanWhatsapp = EiSeedDefaults.normalizeWhatsappDisplay(whatsappNumber)
            require(cleanName.isNotEmpty()) { "Nama anggota team wajib diisi." }

            val docRef = sharedWorkspaceRef.collection("team_members").document()
            docPath = docRef.path
            docRef.set(
                mapOf(
                    "workspaceId" to EiSeedDefaults.SHARED_WORKSPACE_ID,
                    "userId" to uid,
                    "name" to cleanName,
                    "role" to cleanRole,
                    "specialty" to cleanSpecialty,
                    "whatsappNumber" to cleanWhatsapp,
                    "status" to WorkPositionStatus.KANTOR.label,
                    "workNote" to "",
                    "lastTime" to "08:00",
                    "sortOrder" to nextSortOrder.coerceIn(0L, 500L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateTeamMember(
        userId: String,
        memberId: String,
        name: String,
        role: String,
        specialty: String,
        whatsappNumber: String = ""
    ): Result<Unit> {
        val docPath = "${sharedWorkspaceRef.path}/team_members/$memberId"
        return try {
            requireUserId(userId)
            require(memberId.isNotBlank()) { "ID anggota team tidak valid." }
            val cleanName = name.trim().take(80)
            val cleanRole = role.trim().ifEmpty { EiSeedDefaults.jobTitleOptions.first() }.take(80)
            val cleanSpecialty = specialty.trim().ifEmpty { "SCM & EII Team" }.take(100)
            val cleanWhatsapp = EiSeedDefaults.normalizeWhatsappDisplay(whatsappNumber)
            require(cleanName.isNotEmpty()) { "Nama anggota team wajib diisi." }

            sharedWorkspaceRef.collection("team_members").document(memberId)
                .update(
                    mapOf(
                        "name" to cleanName,
                        "role" to cleanRole,
                        "specialty" to cleanSpecialty,
                        "whatsappNumber" to cleanWhatsapp,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteTeamMember(
        userId: String,
        memberId: String
    ): Result<Unit> {
        val docPath = "${sharedWorkspaceRef.path}/team_members/$memberId"
        return try {
            requireUserId(userId)
            require(memberId.isNotBlank()) { "ID anggota team tidak valid." }
            sharedWorkspaceRef.collection("team_members").document(memberId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private fun sanitizePhotoList(uris: List<String>): List<String> =
        uris.map { it.trim().take(500) }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)

    suspend fun addWorkOrder(
        userId: String,
        category: WorkOrderCategory,
        equipmentTag: String,
        technician: String,
        description: String,
        status: WorkOrderStatus,
        nextCodeNumber: Int,
        beforePhotoUris: List<String> = emptyList(),
        afterPhotoUris: List<String> = emptyList()
    ): Result<Unit> {
        var docPath = "${sharedWorkspaceRef.path}/work_orders"
        return try {
            val uid = requireUserId(userId)
            val cleanTag = equipmentTag.trim().take(120)
            val cleanTech = technician.trim().take(80)
            val cleanDesc = description.trim().take(1000)
            val cleanBeforeList = sanitizePhotoList(beforePhotoUris)
            val cleanAfterList = sanitizePhotoList(afterPhotoUris)
            require(cleanTag.isNotEmpty()) { "Tag Number / Pekerjaan wajib diisi." }
            require(cleanTech.isNotEmpty()) { "Teknisi pelaksana wajib dipilih." }
            require(cleanDesc.isNotEmpty()) { "Deskripsi temuan wajib diisi." }

            val woCode = String.format(Locale.US, "WO-%03d", nextCodeNumber).take(30)
            val reportDate = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date()).take(40)
            val docRef = sharedWorkspaceRef.collection("work_orders").document()
            docPath = docRef.path

            val payload = mapOf(
                "workspaceId" to EiSeedDefaults.SHARED_WORKSPACE_ID,
                "userId" to uid,
                "woCode" to woCode,
                "category" to category.label,
                "equipmentTag" to cleanTag,
                "technician" to cleanTech,
                "description" to cleanDesc,
                "status" to status.label,
                "reportDate" to reportDate,
                "beforePhotoUri" to cleanBeforeList.firstOrNull(),
                "afterPhotoUri" to cleanAfterList.firstOrNull(),
                "beforePhotoUris" to cleanBeforeList.ifEmpty { null },
                "afterPhotoUris" to cleanAfterList.ifEmpty { null },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateWorkOrder(
        userId: String,
        existingWorkOrder: WorkOrder,
        category: WorkOrderCategory,
        equipmentTag: String,
        technician: String,
        description: String,
        status: WorkOrderStatus,
        beforePhotoUris: List<String>,
        afterPhotoUris: List<String>
    ): Result<Unit> {
        val workOrderId = existingWorkOrder.id
        val docPath = "${sharedWorkspaceRef.path}/work_orders/$workOrderId"
        return try {
            requireUserId(userId)
            require(workOrderId.isNotBlank()) { "Work order ID tidak valid." }
            val cleanTag = equipmentTag.trim().take(120)
            val cleanTech = technician.trim().take(80)
            val cleanDesc = description.trim().take(1000)
            val cleanBeforeList = sanitizePhotoList(beforePhotoUris)
            val cleanAfterList = sanitizePhotoList(afterPhotoUris)
            require(cleanTag.isNotEmpty()) { "Tag Number / Pekerjaan wajib diisi." }
            require(cleanTech.isNotEmpty()) { "Teknisi pelaksana wajib dipilih." }
            require(cleanDesc.isNotEmpty()) { "Deskripsi temuan wajib diisi." }

            val updates = mutableMapOf<String, Any>(
                "category" to category.label,
                "equipmentTag" to cleanTag,
                "technician" to cleanTech,
                "description" to cleanDesc,
                "status" to status.label,
                "beforePhotoUris" to cleanBeforeList,
                "afterPhotoUris" to cleanAfterList,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val firstBefore = cleanBeforeList.firstOrNull()
            if (firstBefore != null) {
                updates["beforePhotoUri"] = firstBefore
            } else if (!existingWorkOrder.beforePhotoUri.isNullOrBlank()) {
                updates["beforePhotoUri"] = FieldValue.delete()
            }
            val firstAfter = cleanAfterList.firstOrNull()
            if (firstAfter != null) {
                updates["afterPhotoUri"] = firstAfter
            } else if (!existingWorkOrder.afterPhotoUri.isNullOrBlank()) {
                updates["afterPhotoUri"] = FieldValue.delete()
            }

            sharedWorkspaceRef.collection("work_orders").document(workOrderId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateWorkOrderPhotos(
        userId: String,
        existingWorkOrder: WorkOrder,
        isBeforePhoto: Boolean,
        photoUris: List<String>
    ): Result<Unit> {
        val workOrderId = existingWorkOrder.id
        val docPath = "${sharedWorkspaceRef.path}/work_orders/$workOrderId"
        return try {
            requireUserId(userId)
            require(workOrderId.isNotBlank()) { "Work order ID tidak valid." }
            val cleanList = sanitizePhotoList(photoUris)
            val singleField = if (isBeforePhoto) "beforePhotoUri" else "afterPhotoUri"
            val listField = if (isBeforePhoto) "beforePhotoUris" else "afterPhotoUris"
            val existingSingle = if (isBeforePhoto) existingWorkOrder.beforePhotoUri else existingWorkOrder.afterPhotoUri

            val updates = mutableMapOf<String, Any>(
                listField to cleanList,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val firstPhoto = cleanList.firstOrNull()
            if (firstPhoto != null) {
                updates[singleField] = firstPhoto
            } else if (!existingSingle.isNullOrBlank()) {
                updates[singleField] = FieldValue.delete()
            }

            sharedWorkspaceRef.collection("work_orders").document(workOrderId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun cycleWorkOrderStatus(userId: String, workOrder: WorkOrder): Result<Unit> {
        val docPath = "${sharedWorkspaceRef.path}/work_orders/${workOrder.id}"
        return try {
            requireUserId(userId)
            require(workOrder.id.isNotBlank()) { "Work order ID tidak valid." }
            val nextStatus = WorkOrderStatus.fromLabel(workOrder.status).next()
            sharedWorkspaceRef.collection("work_orders").document(workOrder.id)
                .update(
                    mapOf(
                        "status" to nextStatus.label,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun submitWorkStatus(
        userId: String,
        personName: String,
        workStatus: WorkPositionStatus,
        destinationNote: String,
        matchingMemberId: String?
    ): Result<Unit> {
        var docPath = "${sharedWorkspaceRef.path}/work_status_logs"
        return try {
            val uid = requireUserId(userId)
            val cleanPerson = personName.trim().take(80)
            val cleanNote = destinationNote.trim().take(200)
            require(cleanPerson.isNotEmpty()) { "Nama personel wajib dipilih." }
            if (workStatus.requiresDestinationNote) {
                require(cleanNote.isNotEmpty()) {
                    "Keterangan tujuan / kemana wajib diisi untuk ${workStatus.label}."
                }
            }

            val timeLabel = SimpleDateFormat("HH:mm '•' dd MMM", Locale.forLanguageTag("id-ID"))
                .format(Date())
                .take(40)
            val logRef = sharedWorkspaceRef.collection("work_status_logs").document()
            docPath = logRef.path

            val batch = db.batch()
            batch.set(
                logRef,
                mapOf(
                    "workspaceId" to EiSeedDefaults.SHARED_WORKSPACE_ID,
                    "userId" to uid,
                    "personName" to cleanPerson,
                    "workStatus" to workStatus.label,
                    "destinationNote" to cleanNote,
                    "timeLabel" to timeLabel,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            if (!matchingMemberId.isNullOrBlank()) {
                val memberRef = sharedWorkspaceRef.collection("team_members").document(matchingMemberId)
                batch.update(
                    memberRef,
                    mapOf(
                        "status" to workStatus.label,
                        "workNote" to cleanNote,
                        "lastTime" to timeLabel,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    private fun handleFlowException(
        error: Throwable,
        operationType: OperationType,
        path: String,
        uid: String
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
    }

    suspend fun addBriefing(
        userId: String,
        badge: String,
        timeTag: String,
        title: String,
        summary: String,
        isSafetyAlert: Boolean,
        nextSortOrder: Long
    ): Result<Unit> {
        var docPath = "${sharedWorkspaceRef.path}/briefings"
        return try {
            val uid = requireUserId(userId)
            val cleanBadge = badge.trim().take(40)
            val cleanTimeTag = timeTag.trim().take(40)
            val cleanTitle = title.trim().take(100)
            val cleanSummary = summary.trim().take(500)
            require(cleanBadge.isNotEmpty()) { "Badge label wajib diisi." }
            require(cleanTimeTag.isNotEmpty()) { "Waktu / Shift wajib diisi." }
            require(cleanTitle.isNotEmpty()) { "Judul briefing wajib diisi." }
            require(cleanSummary.isNotEmpty()) { "Isi briefing wajib diisi." }

            val docRef = sharedWorkspaceRef.collection("briefings").document()
            docPath = docRef.path
            docRef.set(
                mapOf(
                    "workspaceId" to EiSeedDefaults.SHARED_WORKSPACE_ID,
                    "userId" to uid,
                    "badge" to cleanBadge,
                    "timeTag" to cleanTimeTag,
                    "title" to cleanTitle,
                    "summary" to cleanSummary,
                    "isSafetyAlert" to isSafetyAlert,
                    "sortOrder" to nextSortOrder.coerceIn(0L, 500L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateBriefing(
        userId: String,
        briefingId: String,
        badge: String,
        timeTag: String,
        title: String,
        summary: String,
        isSafetyAlert: Boolean
    ): Result<Unit> {
        val docPath = "${sharedWorkspaceRef.path}/briefings/$briefingId"
        return try {
            requireUserId(userId)
            require(briefingId.isNotBlank()) { "ID briefing tidak valid." }
            val cleanBadge = badge.trim().take(40)
            val cleanTimeTag = timeTag.trim().take(40)
            val cleanTitle = title.trim().take(100)
            val cleanSummary = summary.trim().take(500)
            require(cleanBadge.isNotEmpty()) { "Badge label wajib diisi." }
            require(cleanTimeTag.isNotEmpty()) { "Waktu / Shift wajib diisi." }
            require(cleanTitle.isNotEmpty()) { "Judul briefing wajib diisi." }
            require(cleanSummary.isNotEmpty()) { "Isi briefing wajib diisi." }

            sharedWorkspaceRef.collection("briefings").document(briefingId)
                .update(
                    mapOf(
                        "badge" to cleanBadge,
                        "timeTag" to cleanTimeTag,
                        "title" to cleanTitle,
                        "summary" to cleanSummary,
                        "isSafetyAlert" to isSafetyAlert,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteBriefing(
        userId: String,
        briefingId: String
    ): Result<Unit> {
        val docPath = "${sharedWorkspaceRef.path}/briefings/$briefingId"
        return try {
            requireUserId(userId)
            require(briefingId.isNotBlank()) { "ID briefing tidak valid." }
            sharedWorkspaceRef.collection("briefings").document(briefingId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addScmJob(
        userId: String,
        category: ScmJobCategory,
        title: String,
        referenceNumber: String,
        picName: String,
        destinationOrVendor: String,
        description: String,
        status: ScmJobStatus,
        nextCodeNumber: Int,
        beforePhotoUris: List<String> = emptyList(),
        afterPhotoUris: List<String> = emptyList()
    ): Result<Unit> {
        var docPath = "${sharedWorkspaceRef.path}/scm_jobs"
        return try {
            val uid = requireUserId(userId)
            val cleanTitle = title.trim().take(140)
            val cleanRef = referenceNumber.trim().take(80)
            val cleanPic = picName.trim().take(80)
            val cleanDest = destinationOrVendor.trim().take(120)
            val cleanDesc = description.trim().take(1000)
            val cleanBeforeList = sanitizePhotoList(beforePhotoUris)
            val cleanAfterList = sanitizePhotoList(afterPhotoUris)
            require(cleanTitle.isNotEmpty()) { "Judul job / nama barang wajib diisi." }
            require(cleanPic.isNotEmpty()) { "PIC / Pemohon wajib diisi." }
            require(cleanDesc.isNotEmpty()) { "Rincian pekerjaan SCM wajib diisi." }

            val jobCode = String.format(Locale.US, "SCM-%03d", nextCodeNumber).take(30)
            val jobDate = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date()).take(40)
            val docRef = sharedWorkspaceRef.collection("scm_jobs").document()
            docPath = docRef.path

            val payload = mapOf(
                "workspaceId" to EiSeedDefaults.SHARED_WORKSPACE_ID,
                "userId" to uid,
                "jobCode" to jobCode,
                "category" to category.label,
                "title" to cleanTitle,
                "referenceNumber" to cleanRef,
                "picName" to cleanPic,
                "destinationOrVendor" to cleanDest,
                "description" to cleanDesc,
                "status" to status.label,
                "jobDate" to jobDate,
                "beforePhotoUri" to cleanBeforeList.firstOrNull(),
                "afterPhotoUri" to cleanAfterList.firstOrNull(),
                "beforePhotoUris" to cleanBeforeList.ifEmpty { null },
                "afterPhotoUris" to cleanAfterList.ifEmpty { null },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateScmJob(
        userId: String,
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
    ): Result<Unit> {
        val scmJobId = existingScmJob.id
        val docPath = "${sharedWorkspaceRef.path}/scm_jobs/$scmJobId"
        return try {
            requireUserId(userId)
            require(scmJobId.isNotBlank()) { "ID job SCM tidak valid." }
            val cleanTitle = title.trim().take(140)
            val cleanRef = referenceNumber.trim().take(80)
            val cleanPic = picName.trim().take(80)
            val cleanDest = destinationOrVendor.trim().take(120)
            val cleanDesc = description.trim().take(1000)
            val cleanBeforeList = sanitizePhotoList(beforePhotoUris)
            val cleanAfterList = sanitizePhotoList(afterPhotoUris)
            require(cleanTitle.isNotEmpty()) { "Judul job / nama barang wajib diisi." }
            require(cleanPic.isNotEmpty()) { "PIC / Pemohon wajib diisi." }
            require(cleanDesc.isNotEmpty()) { "Rincian pekerjaan SCM wajib diisi." }

            val updates = mutableMapOf<String, Any>(
                "category" to category.label,
                "title" to cleanTitle,
                "referenceNumber" to cleanRef,
                "picName" to cleanPic,
                "destinationOrVendor" to cleanDest,
                "description" to cleanDesc,
                "status" to status.label,
                "beforePhotoUris" to cleanBeforeList,
                "afterPhotoUris" to cleanAfterList,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val firstBefore = cleanBeforeList.firstOrNull()
            if (firstBefore != null) {
                updates["beforePhotoUri"] = firstBefore
            } else if (!existingScmJob.beforePhotoUri.isNullOrBlank()) {
                updates["beforePhotoUri"] = FieldValue.delete()
            }
            val firstAfter = cleanAfterList.firstOrNull()
            if (firstAfter != null) {
                updates["afterPhotoUri"] = firstAfter
            } else if (!existingScmJob.afterPhotoUri.isNullOrBlank()) {
                updates["afterPhotoUri"] = FieldValue.delete()
            }

            sharedWorkspaceRef.collection("scm_jobs").document(scmJobId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateScmJobPhotos(
        userId: String,
        existingScmJob: ScmJob,
        isBeforePhoto: Boolean,
        photoUris: List<String>
    ): Result<Unit> {
        val scmJobId = existingScmJob.id
        val docPath = "${sharedWorkspaceRef.path}/scm_jobs/$scmJobId"
        return try {
            requireUserId(userId)
            require(scmJobId.isNotBlank()) { "ID job SCM tidak valid." }
            val cleanList = sanitizePhotoList(photoUris)
            val singleField = if (isBeforePhoto) "beforePhotoUri" else "afterPhotoUri"
            val listField = if (isBeforePhoto) "beforePhotoUris" else "afterPhotoUris"
            val existingSingle = if (isBeforePhoto) existingScmJob.beforePhotoUri else existingScmJob.afterPhotoUri

            val updates = mutableMapOf<String, Any>(
                listField to cleanList,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val firstPhoto = cleanList.firstOrNull()
            if (firstPhoto != null) {
                updates[singleField] = firstPhoto
            } else if (!existingSingle.isNullOrBlank()) {
                updates[singleField] = FieldValue.delete()
            }

            sharedWorkspaceRef.collection("scm_jobs").document(scmJobId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun cycleScmJobStatus(userId: String, scmJob: ScmJob): Result<Unit> {
        val docPath = "${sharedWorkspaceRef.path}/scm_jobs/${scmJob.id}"
        return try {
            requireUserId(userId)
            require(scmJob.id.isNotBlank()) { "ID job SCM tidak valid." }
            val nextStatus = ScmJobStatus.fromLabel(scmJob.status).next()
            sharedWorkspaceRef.collection("scm_jobs").document(scmJob.id)
                .update(
                    mapOf(
                        "status" to nextStatus.label,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteScmJob(userId: String, scmJobId: String): Result<Unit> {
        val docPath = "${sharedWorkspaceRef.path}/scm_jobs/$scmJobId"
        return try {
            requireUserId(userId)
            require(scmJobId.isNotBlank()) { "ID job SCM tidak valid." }
            sharedWorkspaceRef.collection("scm_jobs").document(scmJobId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addDailyWorkReport(
        userId: String,
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
        nextCodeNumber: Int,
        photoUris: List<String> = emptyList()
    ): Result<Unit> {
        var docPath = "${sharedWorkspaceRef.path}/daily_work_reports"
        return try {
            val uid = requireUserId(userId)
            val fallbackDate = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date())
            val cleanDate = workDate.trim().ifEmpty { fallbackDate }.take(40)
            val cleanTech = EiSeedDefaults.sanitizePersonNameOnly(technicianName).ifEmpty { technicianName.trim() }.take(80)
            val cleanEquip = equipmentName.trim().take(140)
            val cleanLocation = workLocation.trim().take(140)
            val cleanActivity = activityDescription.trim().take(1000)
            val cleanStart = startTime.trim().ifEmpty { "08.00" }.take(20)
            val cleanEnd = endTime.trim().ifEmpty { "09.00" }.take(20)
            val cleanResult = workResult.trim().take(500)
            val cleanIssues = issuesFound.trim().ifEmpty { "-" }.take(500)
            val cleanPhotos = sanitizePhotoList(photoUris)

            require(cleanTech.isNotEmpty()) { "Nama teknisi wajib dipilih." }
            require(cleanEquip.isNotEmpty()) { "Nama equipment wajib diisi." }
            require(cleanLocation.isNotEmpty()) { "Lokasi pekerjaan wajib diisi." }
            require(cleanActivity.isNotEmpty()) { "Deskripsi aktivitas wajib diisi." }
            require(cleanResult.isNotEmpty()) { "Hasil pekerjaan wajib diisi." }

            val reportCode = String.format(Locale.US, "DWR-%03d", nextCodeNumber).take(30)
            val docRef = sharedWorkspaceRef.collection("daily_work_reports").document()
            docPath = docRef.path

            val payload = mapOf(
                "workspaceId" to EiSeedDefaults.SHARED_WORKSPACE_ID,
                "userId" to uid,
                "reportCode" to reportCode,
                "workDate" to cleanDate,
                "technicianName" to cleanTech,
                "category" to category.label,
                "equipmentName" to cleanEquip,
                "workLocation" to cleanLocation,
                "activityDescription" to cleanActivity,
                "startTime" to cleanStart,
                "endTime" to cleanEnd,
                "workResult" to cleanResult,
                "status" to status.label,
                "issuesFound" to cleanIssues,
                "photoUris" to cleanPhotos.ifEmpty { null },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateDailyWorkReport(
        userId: String,
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
    ): Result<Unit> {
        val reportId = existingReport.id
        val docPath = "${sharedWorkspaceRef.path}/daily_work_reports/$reportId"
        return try {
            requireUserId(userId)
            require(reportId.isNotBlank()) { "ID laporan harian tidak valid." }
            val fallbackDate = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date())
            val cleanDate = workDate.trim().ifEmpty { fallbackDate }.take(40)
            val cleanTech = EiSeedDefaults.sanitizePersonNameOnly(technicianName).ifEmpty { technicianName.trim() }.take(80)
            val cleanEquip = equipmentName.trim().take(140)
            val cleanLocation = workLocation.trim().take(140)
            val cleanActivity = activityDescription.trim().take(1000)
            val cleanStart = startTime.trim().ifEmpty { "08.00" }.take(20)
            val cleanEnd = endTime.trim().ifEmpty { "09.00" }.take(20)
            val cleanResult = workResult.trim().take(500)
            val cleanIssues = issuesFound.trim().ifEmpty { "-" }.take(500)
            val cleanPhotos = sanitizePhotoList(photoUris)

            require(cleanTech.isNotEmpty()) { "Nama teknisi wajib dipilih." }
            require(cleanEquip.isNotEmpty()) { "Nama equipment wajib diisi." }
            require(cleanLocation.isNotEmpty()) { "Lokasi pekerjaan wajib diisi." }
            require(cleanActivity.isNotEmpty()) { "Deskripsi aktivitas wajib diisi." }
            require(cleanResult.isNotEmpty()) { "Hasil pekerjaan wajib diisi." }

            sharedWorkspaceRef.collection("daily_work_reports").document(reportId)
                .update(
                    mapOf(
                        "workDate" to cleanDate,
                        "technicianName" to cleanTech,
                        "category" to category.label,
                        "equipmentName" to cleanEquip,
                        "workLocation" to cleanLocation,
                        "activityDescription" to cleanActivity,
                        "startTime" to cleanStart,
                        "endTime" to cleanEnd,
                        "workResult" to cleanResult,
                        "status" to status.label,
                        "issuesFound" to cleanIssues,
                        "photoUris" to cleanPhotos,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateDailyWorkReportPhotos(
        userId: String,
        reportId: String,
        photoUris: List<String>
    ): Result<Unit> {
        val docPath = "${sharedWorkspaceRef.path}/daily_work_reports/$reportId"
        return try {
            requireUserId(userId)
            require(reportId.isNotBlank()) { "ID laporan harian tidak valid." }
            val cleanPhotos = sanitizePhotoList(photoUris)
            sharedWorkspaceRef.collection("daily_work_reports").document(reportId)
                .update(
                    mapOf(
                        "photoUris" to cleanPhotos,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun cycleDailyWorkReportStatus(userId: String, report: DailyWorkReport): Result<Unit> {
        val docPath = "${sharedWorkspaceRef.path}/daily_work_reports/${report.id}"
        return try {
            requireUserId(userId)
            require(report.id.isNotBlank()) { "ID laporan harian tidak valid." }
            val nextStatus = DailyReportStatus.fromLabel(report.status).next()
            sharedWorkspaceRef.collection("daily_work_reports").document(report.id)
                .update(
                    mapOf(
                        "status" to nextStatus.label,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteDailyWorkReport(userId: String, reportId: String): Result<Unit> {
        val docPath = "${sharedWorkspaceRef.path}/daily_work_reports/$reportId"
        return try {
            requireUserId(userId)
            require(reportId.isNotBlank()) { "ID laporan harian tidak valid." }
            sharedWorkspaceRef.collection("daily_work_reports").document(reportId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private companion object {
        val LEGACY_NAME_MIGRATIONS = mapOf(
            "Supervisor E&I" to EiSeedDefaults.SUPERVISOR_NAME,
            "Teknisi 1 (Andi)" to EiSeedDefaults.TEKNISI_1_NAME,
            "Teknisi 2 (Budi)" to EiSeedDefaults.TEKNISI_2_NAME,
            "Teknisi 3 (Candra)" to EiSeedDefaults.TEKNISI_3_NAME,
            "Teknisi 4 (Dedi)" to EiSeedDefaults.TEKNISI_4_NAME,
            "Teknisi 4 (Khadam)" to EiSeedDefaults.TEKNISI_4_NAME,
            "Teknisi 5 (Eko)" to EiSeedDefaults.TEKNISI_5_NAME,
            "Teknisi 5 (Ermizi)" to EiSeedDefaults.TEKNISI_5_NAME,
            "Teknisi 6 (Fajar)" to EiSeedDefaults.TEKNISI_6_NAME
        )
    }
}
