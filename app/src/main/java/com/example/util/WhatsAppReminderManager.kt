package com.example.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.BriefingNotice
import com.example.data.model.DailyWorkReport
import com.example.data.model.EiSeedDefaults
import com.example.data.model.EiSortHelpers
import com.example.data.model.ScmJob
import com.example.data.model.TeamMember
import com.example.data.model.WorkOrder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WhatsAppReminderManager {

    private const val PREFS_NAME = "wa_reminder_prefs"
    private const val KEY_ENABLED = "reminder_1600_enabled"
    private const val KEY_SENDER = "sender_wa_number"
    private const val KEY_SENDER_NAME = "sender_wa_name"

    const val EXTRA_OPEN_WA_REMINDER = "extra_open_wa_reminder"
    const val CHANNEL_ID = "daily_wa_reminder_channel"
    private const val NOTIFICATION_ID = 1600
    private const val ALARM_REQUEST_CODE = 1601

    fun isReminderEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, true)
    }

    fun setReminderEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
        if (enabled) {
            scheduleDaily1600Reminder(context)
        } else {
            cancelDailyReminder(context)
        }
    }

    fun getSenderNumber(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SENDER, "").orEmpty()
    }

    fun getSenderName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SENDER_NAME, "").orEmpty()
    }

    fun setSenderNumber(context: Context, number: String) {
        val normalized = EiSeedDefaults.normalizeWhatsappDisplay(number)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SENDER, normalized)
            .apply()
    }

    fun setSenderSelection(context: Context, senderName: String, senderNumber: String) {
        val cleanName = EiSeedDefaults.sanitizePersonNameOnly(senderName).ifBlank { senderName.trim() }.take(80)
        val normalized = EiSeedDefaults.normalizeWhatsappDisplay(senderNumber)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SENDER_NAME, cleanName)
            .putString(KEY_SENDER, normalized)
            .apply()
    }

    fun resolveSender(
        context: Context,
        currentUserName: String,
        teamMembers: List<TeamMember>
    ): Pair<String, String> {
        val cleanLoggedIn = EiSeedDefaults.sanitizePersonNameOnly(currentUserName)
            .ifBlank { currentUserName.trim() }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_SENDER_NAME, "").orEmpty().trim()
        val savedNumber = prefs.getString(KEY_SENDER, "").orEmpty().trim()

        if (savedName.isNotEmpty() || savedNumber.isNotEmpty()) {
            val resolvedName = savedName.ifBlank { cleanLoggedIn }
            val matchedByName = teamMembers.firstOrNull {
                EiSeedDefaults.sanitizePersonNameOnly(it.name).equals(resolvedName, ignoreCase = true)
            }
            val resolvedNumber = savedNumber.ifBlank { matchedByName?.whatsappNumber.orEmpty() }
            return resolvedName to resolvedNumber
        }

        val matchedMember = teamMembers.firstOrNull {
            EiSeedDefaults.sanitizePersonNameOnly(it.name).equals(cleanLoggedIn, ignoreCase = true)
        }
        val finalName = matchedMember?.name?.takeIf { it.isNotBlank() } ?: cleanLoggedIn
        val finalNumber = matchedMember?.whatsappNumber.orEmpty()
        return finalName to finalNumber
    }

    fun ensureScheduledIfEnabled(context: Context) {
        if (isReminderEnabled(context)) {
            scheduleDaily1600Reminder(context)
        }
    }

    fun computeNext1600Millis(nowMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= nowMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    fun scheduleDaily1600Reminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildAlarmPendingIntent(context)
        val triggerAt = computeNext1600Millis()
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(buildAlarmPendingIntent(context))
    }

    private fun buildAlarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailyReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun postReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reminder WhatsApp 16:00",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat harian jam 16:00 untuk kirim rekap aktivitas ke seluruh anggota team"
            }
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_WA_REMINDER, true)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val senderName = getSenderName(context)
        val senderNumber = getSenderNumber(context)
        val senderLabel = formatSenderLabel(senderName, senderNumber).ifBlank { "Akun Login" }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Reminder 16:00 • Rekap Aktivitas Team")
            .setContentText("Ketuk untuk kirim ringkasan EII, Harian, SCM & Briefing ($senderLabel).")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Waktunya kirim laporan aktivitas harian jam 16:00 WIB dari $senderLabel ke seluruh anggota SCM & EII Team."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    fun formatSenderLabel(senderName: String, senderNumber: String): String {
        val cleanName = senderName.trim()
        val cleanNum = senderNumber.trim()
        return when {
            cleanName.isNotEmpty() && cleanNum.isNotEmpty() -> "$cleanName ($cleanNum)"
            cleanName.isNotEmpty() -> cleanName
            cleanNum.isNotEmpty() -> cleanNum
            else -> "-"
        }
    }

    fun buildActivitySummaryMessage(
        senderNumber: String,
        workOrders: List<WorkOrder>,
        dailyWorkReports: List<DailyWorkReport>,
        scmJobs: List<ScmJob>,
        briefings: List<BriefingNotice>,
        senderName: String = "",
        dateLabel: String = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date())
    ): String {
        val sortedWorkOrders = EiSortHelpers.sortWorkOrders(workOrders)
        val sortedDaily = EiSortHelpers.sortDailyWorkReports(dailyWorkReports)
        val sortedScm = EiSortHelpers.sortScmJobs(scmJobs)
        val senderDisplay = formatSenderLabel(senderName, senderNumber)

        val sb = StringBuilder()
        sb.append("*REMINDER & REKAP AKTIVITAS HARIAN (16:00 WIB)*\n")
        sb.append("Departemen: SCM dan EII Team\n")
        sb.append("Tanggal: $dateLabel\n")
        sb.append("Pengirim: $senderDisplay\n")
        sb.append("------------------------------\n")

        sb.append("*1. AKTIVITAS EII (${sortedWorkOrders.size})*\n")
        if (sortedWorkOrders.isEmpty()) {
            sb.append("- Belum ada pekerjaan EII.\n")
        } else {
            sortedWorkOrders.take(10).forEach { wo ->
                val pic = EiSeedDefaults.sanitizePersonNameOnly(wo.technician)
                sb.append("- [${wo.woCode}] ${wo.category} | ${wo.equipmentTag} | PIC: $pic | Status: ${wo.status}\n")
            }
            if (sortedWorkOrders.size > 10) {
                sb.append("  (+${sortedWorkOrders.size - 10} pekerjaan lainnya)\n")
            }
        }

        sb.append("\n*2. DAILY WORK REPORT / HARIAN (${sortedDaily.size})*\n")
        if (sortedDaily.isEmpty()) {
            sb.append("- Belum ada laporan harian.\n")
        } else {
            sortedDaily.take(12).forEach { rep ->
                val tech = EiSeedDefaults.sanitizePersonNameOnly(rep.technicianName)
                val hasil = rep.workResult.ifBlank { "-" }
                val datePart = rep.workDate.takeIf { it.isNotBlank() }?.let { "[$it] " } ?: ""
                sb.append("- $datePart${rep.formattedTimeRange()} | $tech | ${rep.activityDescription} | Hasil: $hasil | Status: ${rep.status}\n")
            }
            if (sortedDaily.size > 12) {
                sb.append("  (+${sortedDaily.size - 12} laporan lainnya)\n")
            }
        }

        sb.append("\n*3. AKTIVITAS SCM & LOGISTIK (${sortedScm.size})*\n")
        if (sortedScm.isEmpty()) {
            sb.append("- Belum ada job SCM.\n")
        } else {
            sortedScm.take(10).forEach { scm ->
                val pic = EiSeedDefaults.sanitizePersonNameOnly(scm.picName)
                sb.append("- [${scm.jobCode}] ${scm.category} | ${scm.title} | PIC: $pic | Status: ${scm.status}\n")
            }
            if (sortedScm.size > 10) {
                sb.append("  (+${sortedScm.size - 10} job SCM lainnya)\n")
            }
        }

        sb.append("\n*4. INFO & BRIEFING TEAM (${briefings.size})*\n")
        if (briefings.isEmpty()) {
            sb.append("- Belum ada info briefing.\n")
        } else {
            briefings.sortedBy { it.sortOrder }.take(6).forEach { br ->
                sb.append("- [${br.badge}] ${br.title} (${br.timeTag}): ${br.summary}\n")
            }
        }

        sb.append("\nMohon pastikan seluruh pekerjaan & Daily Work Report hari ini sudah diupdate sebelum pulang. Terima kasih.")
        return sb.toString().trim()
    }

    fun openWhatsAppForMember(context: Context, targetPhone: String, message: String): Boolean {
        val waId = EiSeedDefaults.formatWhatsappWaId(targetPhone)
        if (waId.isEmpty()) return false
        val encoded = URLEncoder.encode(message, "UTF-8")
        val uri = Uri.parse("https://wa.me/$waId?text=$encoded")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun shareBroadcastMessage(context: Context, message: String) {
        val waIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val launchedWa = runCatching {
            context.startActivity(waIntent)
            true
        }.getOrDefault(false)
        if (!launchedWa) {
            val chooser = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                },
                "Kirim Rekap Aktivitas ke WhatsApp Team"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }
}

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (WhatsAppReminderManager.isReminderEnabled(context)) {
            WhatsAppReminderManager.postReminderNotification(context)
        }
    }
}
