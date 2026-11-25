package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class WorkspaceProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = EiSeedDefaults.SUPERVISOR_NAME,
    val jobTitle: String = EiSeedDefaults.DEFAULT_JOB_TITLE,
    val activeRole: String = RoleMode.SUPERVISOR.label,
    val activeMemberName: String = EiSeedDefaults.SUPERVISOR_NAME,
    val initialized: Boolean = false,
    val profileCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TeamMember(
    @DocumentId val id: String = "",
    val workspaceId: String = EiSeedDefaults.SHARED_WORKSPACE_ID,
    val userId: String = "",
    val name: String = "",
    val role: String = "Teknisi Electrical & Instrumentation",
    val specialty: String = "",
    val whatsappNumber: String = "",
    val status: String = WorkPositionStatus.KANTOR.label,
    val workNote: String = "",
    val lastTime: String = "-",
    val sortOrder: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WorkOrder(
    @DocumentId val id: String = "",
    val workspaceId: String = EiSeedDefaults.SHARED_WORKSPACE_ID,
    val userId: String = "",
    val woCode: String = "",
    val category: String = WorkOrderCategory.PM.label,
    val equipmentTag: String = "",
    val technician: String = "",
    val description: String = "",
    val status: String = WorkOrderStatus.OPEN.label,
    val reportDate: String = "",
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null,
    val beforePhotoUris: List<String> = emptyList(),
    val afterPhotoUris: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun resolvedBeforePhotos(): List<String> =
        (beforePhotoUris + listOfNotNull(beforePhotoUri))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)

    fun resolvedAfterPhotos(): List<String> =
        (afterPhotoUris + listOfNotNull(afterPhotoUri))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
}

@Keep
@IgnoreExtraProperties
data class WorkStatusLog(
    @DocumentId val id: String = "",
    val workspaceId: String = EiSeedDefaults.SHARED_WORKSPACE_ID,
    val userId: String = "",
    val personName: String = "",
    val workStatus: String = WorkPositionStatus.KANTOR.label,
    val destinationNote: String = "",
    val timeLabel: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class RoleMode(val label: String) {
    SUPERVISOR("Supervisor"),
    TEKNISI("Teknisi");

    companion object {
        fun fromLabel(raw: String?): RoleMode =
            entries.firstOrNull { it.label == raw } ?: SUPERVISOR
    }
}

enum class WorkPositionStatus(
    val label: String,
    val requiresDestinationNote: Boolean
) {
    KANTOR("Kantor", false),
    WORKSHOP("Workshop", false),
    LUAR_AREA_KERJA("Luar Area Kerja", true),
    DINAS_LUAR_KOTA("Dinas Luar Kota", true),
    CUTI("Cuti", false),
    SAKIT("Sakit", false),
    IZIN("Izin", true);

    companion object {
        fun fromLabel(raw: String?): WorkPositionStatus =
            entries.firstOrNull { it.label.equals(raw?.trim(), ignoreCase = true) } ?: KANTOR
    }
}

enum class WorkOrderCategory(val label: String, val shortLabel: String) {
    PM("Preventive Maintenance", "PM"),
    CM("Corrective Maintenance", "CM"),
    BREAKDOWN("Breakdown Report", "Breakdown"),
    PERSIAPAN("Persiapan", "Persiapan"),
    PEMBELIAN("Pembelian", "Pembelian"),
    FUNCTION_TEST("Function Test", "Function Test"),
    INSTALASI("Instalasi", "Instalasi"),
    MEETING("Meeting", "Meeting");

    companion object {
        fun fromLabel(raw: String?): WorkOrderCategory =
            entries.firstOrNull { it.label == raw } ?: PM
    }
}

enum class WorkOrderStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    DONE("Done");

    fun next(): WorkOrderStatus {
        val list = entries
        return list[(ordinal + 1) % list.size]
    }

    companion object {
        fun fromLabel(raw: String?): WorkOrderStatus =
            entries.firstOrNull { it.label == raw } ?: OPEN
    }
}

@Keep
@IgnoreExtraProperties
data class BriefingNotice(
    @DocumentId val id: String = "",
    val workspaceId: String = EiSeedDefaults.SHARED_WORKSPACE_ID,
    val userId: String = "",
    val badge: String = "SHIFT BRIEFING",
    val timeTag: String = "Hari Ini",
    val title: String = "",
    val summary: String = "",
    @PropertyName("isSafetyAlert") @JvmField val isSafetyAlert: Boolean = false,
    val sortOrder: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object EiSeedDefaults {
    const val SHARED_WORKSPACE_ID = "scm_eii"
    const val MAX_PHOTOS_PER_SECTION = 4
    const val DEFAULT_WA_SENDER = "08988319566"
    const val SUPERVISOR_NAME = "Supervisor Team"
    const val TEKNISI_1_NAME = "Andi Pratama"
    const val TEKNISI_2_NAME = "Budi Santoso"
    const val TEKNISI_3_NAME = "Candra Wijaya"
    const val TEKNISI_4_NAME = "Khadam"
    const val TEKNISI_5_NAME = "Ermizi"
    const val TEKNISI_6_NAME = "Fajar Nugroho"

    val briefings = listOf(
        BriefingNotice(
            badge = "PREVENTIVE SCHEDULE",
            timeTag = "Hari Ini",
            title = "Kalibrasi Pressure Transmitter",
            summary = "Cek loop 4-20mA & zero calibration pada Skid Compressor Unit 01 - 03.",
            isSafetyAlert = false
        ),
        BriefingNotice(
            badge = "SAFETY REMINDER",
            timeTag = "Single Shift",
            title = "Wajib LOTO Sebelum Kerja Panel",
            summary = "Pastikan Lock Out Tag Out terpasang dan verifikasi zero voltage sebelum terminasi.",
            isSafetyAlert = true
        ),
        BriefingNotice(
            badge = "SHIFT BRIEFING",
            timeTag = "08:00 - 17:00",
            title = "Target Penyelesaian Pekerjaan Prioritas",
            summary = "Prioritaskan pemeriksaan motor MTR-204 & pengiriman spare module PLC Skid 02.",
            isSafetyAlert = false
        )
    )

    val technicianOptions = listOf(
        TEKNISI_1_NAME to TEKNISI_1_NAME,
        TEKNISI_2_NAME to TEKNISI_2_NAME,
        TEKNISI_3_NAME to TEKNISI_3_NAME,
        TEKNISI_4_NAME to TEKNISI_4_NAME,
        TEKNISI_5_NAME to TEKNISI_5_NAME,
        TEKNISI_6_NAME to TEKNISI_6_NAME
    )

    const val DEFAULT_JOB_TITLE = "Supervisor E&II"

    val jobTitleOptions = listOf(
        "Senior Manager",
        "Manager",
        "Asst. Manager",
        "Supervisor SCM",
        "Supervisor E&II",
        "Supervisor Warehouse",
        "Admin SCM",
        "Admin Warehouse",
        "Teknisi Electrical & Instrumentation",
        "Teknisi Build & Wiring",
        "Teknisi Mechanical",
        "Teknisi Calibration Instrumen"
    )

    fun roleForJobTitle(jobTitle: String): RoleMode =
        if (jobTitle.trim().startsWith("Teknisi", ignoreCase = true)) {
            RoleMode.TEKNISI
        } else {
            RoleMode.SUPERVISOR
        }

    fun badgeCodeForRole(role: String): String {
        val clean = role.trim().lowercase()
        return when {
            clean.contains("senior manager") || clean.contains("sr manager") || clean.contains("sr. manager") -> "SM"
            clean.contains("asst") -> "AM"
            clean.contains("manager") -> "MGR"
            clean.contains("supervisor") || clean == "spv" -> "SPV"
            clean.contains("admin") -> "ADM"
            clean.contains("teknisi") || clean == "tek" -> "TEK"
            clean.isEmpty() -> "TM"
            else -> {
                val words = role.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                if (words.size >= 2) {
                    "${words[0].first()}${words[1].first()}".uppercase()
                } else {
                    words.first().take(3).uppercase()
                }
            }
        }
    }

    private val ROLE_KEYWORDS = listOf(
        "senior manager", "sr manager", "asst. manager", "asst manager",
        "manager", "supervisor", "admin", "teknisi", "spv", "warehouse"
    )

    fun sanitizePersonNameOnly(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""

        // Legacy pattern: "Teknisi 4 (Khadam)" -> "Khadam"
        val legacyPrefixParen = Regex("^(Teknisi\\s*\\d+|Supervisor[^()]*)\\s*\\(([^)]+)\\)$", RegexOption.IGNORE_CASE)
            .matchEntire(trimmed)
        if (legacyPrefixParen != null) {
            return legacyPrefixParen.groupValues[2].trim()
        }

        // Trailing parenthesized role: "Budi Santoso (Teknisi Mechanical)" -> "Budi Santoso"
        val trailingParen = Regex("^(.+?)\\s*\\(([^)]+)\\)$").matchEntire(trimmed)
        if (trailingParen != null) {
            val left = trailingParen.groupValues[1].trim()
            val inside = trailingParen.groupValues[2].trim().lowercase()
            if (ROLE_KEYWORDS.any { inside.contains(it) }) {
                return left
            }
        }

        // Dash/bullet separated role: "Budi Santoso - Teknisi Mechanical" -> "Budi Santoso"
        val dashSplit = Regex("^(.+?)\\s*[-•|]\\s*(.+)$").matchEntire(trimmed)
        if (dashSplit != null) {
            val left = dashSplit.groupValues[1].trim()
            val right = dashSplit.groupValues[2].trim().lowercase()
            if (ROLE_KEYWORDS.any { right.contains(it) }) {
                return left
            }
        }

        return trimmed
    }

    fun normalizeWhatsappDisplay(raw: String): String {
        val cleaned = raw.trim().replace(Regex("[^0-9+]"), "")
        return cleaned.take(30)
    }

    fun formatWhatsappWaId(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.length < 8) return ""
        return when {
            digits.startsWith("62") -> digits
            digits.startsWith("0") -> "62${digits.drop(1)}"
            digits.startsWith("8") -> "62$digits"
            else -> digits
        }
    }

    val scmJobs = listOf(
        Triple(ScmJobCategory.PURCHASE_REQUEST, "Pengadaan Pressure Transmitter & Cable Gland", "PR-2026-041") to
            Triple("Admin SCM", "PT Instrumentation Jaya", "PR 2 unit Pressure Transmitter 4-20mA & 20 pcs cable gland Ex-d untuk Skid 02."),
        Triple(ScmJobCategory.PENGIRIMAN_BARANG, "Pengiriman Panel Star-Delta & Spare MCB ke Site", "SJ-2026-109") to
            Triple("Supervisor SCM", "Area Compressor Plant B", "Pengiriman 1 unit panel kontrol dan 6 pcs MCCB/MCB via armada logistik pagi."),
        Triple(ScmJobCategory.PENERIMAAN_MATERIAL, "Kedatangan Kabel Instrument Belden 1 Pair", "GR-2026-088") to
            Triple("Admin Warehouse", "Gudang Utama SCM", "Pengecekan fisik 3 roll kabel instrument 18 AWG & serah terima ke tim.")
    )
}

enum class ScmJobCategory(val label: String, val shortLabel: String) {
    PURCHASE_REQUEST("Purchase Request", "PR"),
    PENGIRIMAN_BARANG("Pengiriman Barang", "Pengiriman"),
    PENERIMAAN_MATERIAL("Penerimaan Material", "Penerimaan"),
    WAREHOUSE_LOGISTIK("Warehouse & Stok", "Warehouse"),
    MOB("Mob", "Mob"),
    DEMOB("Demob", "Demob"),
    PLAN("Plan", "Plan"),
    FORECAST("Forecast", "Forecast"),
    MEETING("Meeting", "Meeting");

    companion object {
        fun fromLabel(raw: String?): ScmJobCategory =
            entries.firstOrNull { it.label == raw } ?: PURCHASE_REQUEST
    }
}

enum class ScmJobStatus(val label: String) {
    OPEN("Open"),
    PROSES("Proses"),
    SELESAI("Selesai");

    fun next(): ScmJobStatus {
        val list = entries
        return list[(ordinal + 1) % list.size]
    }

    companion object {
        fun fromLabel(raw: String?): ScmJobStatus =
            entries.firstOrNull { it.label == raw } ?: OPEN
    }
}

@Keep
@IgnoreExtraProperties
data class ScmJob(
    @DocumentId val id: String = "",
    val workspaceId: String = EiSeedDefaults.SHARED_WORKSPACE_ID,
    val userId: String = "",
    val jobCode: String = "",
    val category: String = ScmJobCategory.PURCHASE_REQUEST.label,
    val title: String = "",
    val referenceNumber: String = "",
    val picName: String = "",
    val destinationOrVendor: String = "",
    val description: String = "",
    val status: String = ScmJobStatus.OPEN.label,
    val jobDate: String = "",
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null,
    val beforePhotoUris: List<String> = emptyList(),
    val afterPhotoUris: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun resolvedBeforePhotos(): List<String> =
        (beforePhotoUris + listOfNotNull(beforePhotoUri))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)

    fun resolvedAfterPhotos(): List<String> =
        (afterPhotoUris + listOfNotNull(afterPhotoUri))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
}

enum class DailyReportCategory(val label: String, val shortLabel: String) {
    DAILY_INSPECTION("Daily Inspection", "Daily Inspection"),
    PREVENTIVE_MAINTENANCE("Preventive Maintenance", "Preventive"),
    CORRECTIVE_MAINTENANCE("Corrective Maintenance", "Corrective"),
    BREAKDOWN("Breakdown", "Breakdown"),
    CALIBRATION("Calibration", "Calibration"),
    TROUBLESHOOTING("Troubleshooting", "Troubleshooting"),
    INSTALLATION("Installation", "Installation"),
    MODIFICATION("Modification", "Modification"),
    OTHER_ACTIVITY("Other Activity", "Other Activity");

    companion object {
        fun fromLabel(raw: String?): DailyReportCategory =
            entries.firstOrNull { it.label.equals(raw?.trim(), ignoreCase = true) } ?: DAILY_INSPECTION
    }
}

enum class DailyReportStatus(val label: String) {
    OPEN("Open"),
    PROSES("Proses"),
    SELESAI("Selesai");

    fun next(): DailyReportStatus {
        val list = entries
        return list[(ordinal + 1) % list.size]
    }

    companion object {
        fun fromLabel(raw: String?): DailyReportStatus =
            entries.firstOrNull { it.label.equals(raw?.trim(), ignoreCase = true) } ?: PROSES
    }
}

@Keep
@IgnoreExtraProperties
data class DailyWorkReport(
    @DocumentId val id: String = "",
    val workspaceId: String = EiSeedDefaults.SHARED_WORKSPACE_ID,
    val userId: String = "",
    val reportCode: String = "",
    val workDate: String = "",
    val technicianName: String = "",
    val category: String = DailyReportCategory.DAILY_INSPECTION.label,
    val equipmentName: String = "",
    val workLocation: String = "",
    val activityDescription: String = "",
    val startTime: String = "08.00",
    val endTime: String = "09.00",
    val workResult: String = "",
    val status: String = DailyReportStatus.PROSES.label,
    val issuesFound: String = "-",
    val photoUris: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun resolvedPhotos(): List<String> =
        photoUris
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)

    fun formattedTimeRange(): String = "${startTime.trim().ifEmpty { "-" }}–${endTime.trim().ifEmpty { "-" }}"
}

object EiSortHelpers {
    private val MONTH_MAP = mapOf(
        "jan" to 1, "januari" to 1, "january" to 1,
        "feb" to 2, "februari" to 2, "february" to 2, "peb" to 2,
        "mar" to 3, "maret" to 3, "march" to 3,
        "apr" to 4, "april" to 4,
        "mei" to 5, "may" to 5,
        "jun" to 6, "juni" to 6, "june" to 6,
        "jul" to 7, "juli" to 7, "july" to 7,
        "agu" to 8, "agt" to 8, "agustus" to 8, "aug" to 8, "august" to 8,
        "sep" to 9, "sept" to 9, "september" to 9,
        "okt" to 10, "oktober" to 10, "oct" to 10, "october" to 10,
        "nov" to 11, "nop" to 11, "november" to 11, "nopember" to 11,
        "des" to 12, "desember" to 12, "dec" to 12, "december" to 12
    )

    fun parseDateSortKey(raw: String): Long {
        val cleaned = raw.trim()
        if (cleaned.isEmpty()) return 0L

        // Format: yyyy-MM-dd or yyyy/MM/dd
        Regex("^(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})$").matchEntire(cleaned)?.let { m ->
            val y = m.groupValues[1].toLongOrNull() ?: return 0L
            val mo = m.groupValues[2].toLongOrNull()?.coerceIn(1L, 12L) ?: return 0L
            val d = m.groupValues[3].toLongOrNull()?.coerceIn(1L, 31L) ?: return 0L
            return y * 10000L + mo * 100L + d
        }

        // Format: dd-MM-yyyy or dd/MM/yyyy
        Regex("^(\\d{1,2})[-/](\\d{1,2})[-/](\\d{4})$").matchEntire(cleaned)?.let { m ->
            val d = m.groupValues[1].toLongOrNull()?.coerceIn(1L, 31L) ?: return 0L
            val mo = m.groupValues[2].toLongOrNull()?.coerceIn(1L, 12L) ?: return 0L
            val y = m.groupValues[3].toLongOrNull() ?: return 0L
            return y * 10000L + mo * 100L + d
        }

        // Format: "25 Nov 2026" or "1 Des 2026" or "25 November 2026"
        val tokens = cleaned.replace(",", " ").split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.size >= 3) {
            val day = tokens[0].toLongOrNull()?.coerceIn(1L, 31L)
            val month = MONTH_MAP[tokens[1].lowercase().trimEnd('.')]?.toLong()
            val year = tokens[2].toLongOrNull()
            if (day != null && month != null && year != null) {
                return year * 10000L + month * 100L + day
            }
        }
        return 0L
    }

    fun parseTimeMinutes(raw: String): Int {
        val cleaned = raw.trim().replace(':', '.')
        val parts = cleaned.split('.')
        val h = parts.getOrNull(0)?.filter(Char::isDigit)?.toIntOrNull()?.coerceIn(0, 23) ?: 0
        val m = parts.getOrNull(1)?.filter(Char::isDigit)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        return h * 60 + m
    }

    fun parseCodeNumber(code: String): Int {
        val digits = code.substringAfterLast('-', code).filter(Char::isDigit)
        return digits.toIntOrNull() ?: 0
    }

    fun sortDailyWorkReports(reports: List<DailyWorkReport>): List<DailyWorkReport> =
        reports.sortedWith(
            compareByDescending<DailyWorkReport> { parseDateSortKey(it.workDate) }
                .thenBy { parseTimeMinutes(it.startTime) }
                .thenBy { parseTimeMinutes(it.endTime) }
                .thenByDescending { it.updatedAt?.toDate()?.time ?: it.createdAt?.toDate()?.time ?: 0L }
                .thenByDescending { parseCodeNumber(it.reportCode) }
        )

    fun sortWorkOrders(workOrders: List<WorkOrder>): List<WorkOrder> =
        workOrders.sortedWith(
            compareByDescending<WorkOrder> { parseDateSortKey(it.reportDate) }
                .thenByDescending { it.updatedAt?.toDate()?.time ?: it.createdAt?.toDate()?.time ?: 0L }
                .thenByDescending { parseCodeNumber(it.woCode) }
        )

    fun sortScmJobs(scmJobs: List<ScmJob>): List<ScmJob> =
        scmJobs.sortedWith(
            compareByDescending<ScmJob> { parseDateSortKey(it.jobDate) }
                .thenByDescending { it.updatedAt?.toDate()?.time ?: it.createdAt?.toDate()?.time ?: 0L }
                .thenByDescending { parseCodeNumber(it.jobCode) }
        )
}

data class PicActivitySummary(
    val picName: String,
    val openCount: Int,
    val progressCount: Int,
    val doneCount: Int
)

object EiActivitySummaryHelpers {
    private data class MutablePicCounter(
        val displayName: String,
        var openCount: Int = 0,
        var progressCount: Int = 0,
        var doneCount: Int = 0
    )

    fun buildPicTickerSummaries(
        workOrders: List<WorkOrder>,
        dailyWorkReports: List<DailyWorkReport>,
        scmJobs: List<ScmJob>
    ): List<PicActivitySummary> {
        val buckets = linkedMapOf<String, MutablePicCounter>()

        fun bucketFor(rawName: String): MutablePicCounter? {
            val clean = EiSeedDefaults.sanitizePersonNameOnly(rawName)
            if (clean.isBlank()) return null
            val key = clean.lowercase()
            return buckets.getOrPut(key) { MutablePicCounter(displayName = clean) }
        }

        workOrders.forEach { wo ->
            val bucket = bucketFor(wo.technician) ?: return@forEach
            when (WorkOrderStatus.fromLabel(wo.status)) {
                WorkOrderStatus.OPEN -> bucket.openCount += 1
                WorkOrderStatus.IN_PROGRESS -> bucket.progressCount += 1
                WorkOrderStatus.DONE -> bucket.doneCount += 1
            }
        }

        dailyWorkReports.forEach { rep ->
            val bucket = bucketFor(rep.technicianName) ?: return@forEach
            when (DailyReportStatus.fromLabel(rep.status)) {
                DailyReportStatus.OPEN -> bucket.openCount += 1
                DailyReportStatus.PROSES -> bucket.progressCount += 1
                DailyReportStatus.SELESAI -> bucket.doneCount += 1
            }
        }

        scmJobs.forEach { scm ->
            val bucket = bucketFor(scm.picName) ?: return@forEach
            when (ScmJobStatus.fromLabel(scm.status)) {
                ScmJobStatus.OPEN -> bucket.openCount += 1
                ScmJobStatus.PROSES -> bucket.progressCount += 1
                ScmJobStatus.SELESAI -> bucket.doneCount += 1
            }
        }

        return buckets.values
            .map {
                PicActivitySummary(
                    picName = it.displayName,
                    openCount = it.openCount,
                    progressCount = it.progressCount,
                    doneCount = it.doneCount
                )
            }
            .sortedWith(
                compareByDescending<PicActivitySummary> { it.openCount + it.progressCount }
                    .thenBy { it.picName.lowercase() }
            )
    }

    fun formatPicTickerText(summaries: List<PicActivitySummary>): String {
        if (summaries.isEmpty()) {
            return "Belum ada aktivitas PIC di halaman Aktivitas (EII, Harian, SCM)"
        }
        return summaries.joinToString("   •   ") {
            "${it.picName} Open (${it.openCount}) Progress (${it.progressCount})"
        }
    }
}

