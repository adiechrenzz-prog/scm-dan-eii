package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BriefingNotice
import com.example.data.model.DailyReportCategory
import com.example.data.model.DailyReportStatus
import com.example.data.model.DailyWorkReport
import com.example.data.model.EiActivitySummaryHelpers
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
import com.example.ui.components.WorkOrderPhotoStorage
import com.example.ui.viewmodel.BottomTab
import com.example.util.CustomJobTitleStore
import com.example.util.ReportExporter
import com.example.util.WhatsAppReminderManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import java.util.Calendar
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.zip.ZipFile

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read updated navigation and app strings from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("SCM dan EII Departemen", context.getString(R.string.app_name))
        assertEquals("Posisi", context.getString(R.string.nav_positions))
        assertEquals("Team", context.getString(R.string.nav_team))
        assertEquals(
            listOf(
                BottomTab.BERANDA,
                BottomTab.AKTIVITAS,
                BottomTab.POSISI,
                BottomTab.LAPORAN,
                BottomTab.TIM
            ),
            BottomTab.entries
        )
    }

    @Test
    fun `work order, scm, and work position statuses include all required options`() {
        assertEquals(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.OPEN.next())
        assertEquals(WorkOrderStatus.DONE, WorkOrderStatus.IN_PROGRESS.next())
        assertEquals(WorkOrderStatus.OPEN, WorkOrderStatus.DONE.next())

        assertEquals(RoleMode.SUPERVISOR, RoleMode.fromLabel("Unknown"))
        assertEquals(WorkOrderCategory.PM, WorkOrderCategory.fromLabel(null))
        assertEquals(WorkOrderCategory.PERSIAPAN, WorkOrderCategory.fromLabel("Persiapan"))
        assertEquals(WorkOrderCategory.PEMBELIAN, WorkOrderCategory.fromLabel("Pembelian"))
        assertEquals(WorkOrderCategory.FUNCTION_TEST, WorkOrderCategory.fromLabel("Function Test"))
        assertEquals(WorkOrderCategory.INSTALASI, WorkOrderCategory.fromLabel("Instalasi"))
        assertEquals(WorkOrderCategory.MEETING, WorkOrderCategory.fromLabel("Meeting"))

        assertEquals(ScmJobCategory.MOB, ScmJobCategory.fromLabel("Mob"))
        assertEquals(ScmJobCategory.DEMOB, ScmJobCategory.fromLabel("Demob"))
        assertEquals(ScmJobCategory.PLAN, ScmJobCategory.fromLabel("Plan"))
        assertEquals(ScmJobCategory.FORECAST, ScmJobCategory.fromLabel("Forecast"))
        assertEquals(ScmJobCategory.MEETING, ScmJobCategory.fromLabel("Meeting"))
        assertEquals(WorkOrderStatus.OPEN, WorkOrderStatus.fromLabel("Bad"))

        assertEquals(7, WorkPositionStatus.entries.size)
        assertFalse(WorkPositionStatus.KANTOR.requiresDestinationNote)
        assertFalse(WorkPositionStatus.WORKSHOP.requiresDestinationNote)
        assertTrue(WorkPositionStatus.LUAR_AREA_KERJA.requiresDestinationNote)
        assertTrue(WorkPositionStatus.DINAS_LUAR_KOTA.requiresDestinationNote)
        assertFalse(WorkPositionStatus.CUTI.requiresDestinationNote)
        assertFalse(WorkPositionStatus.SAKIT.requiresDestinationNote)
        assertTrue(WorkPositionStatus.IZIN.requiresDestinationNote)
        assertEquals(WorkPositionStatus.DINAS_LUAR_KOTA, WorkPositionStatus.fromLabel("Dinas Luar kota"))
    }

    @Test
    fun `role badge codes and person name sanitization preserve Achmad Rohadi`() {
        assertEquals("SM", EiSeedDefaults.badgeCodeForRole("Senior Manager"))
        assertEquals("MGR", EiSeedDefaults.badgeCodeForRole("Manager"))
        assertEquals("AM", EiSeedDefaults.badgeCodeForRole("Asst. Manager"))
        assertEquals("SPV", EiSeedDefaults.badgeCodeForRole("Supervisor E&II"))
        assertEquals("ADM", EiSeedDefaults.badgeCodeForRole("Admin SCM"))
        assertEquals("TEK", EiSeedDefaults.badgeCodeForRole("Teknisi Electrical & Instrumentation"))
        assertEquals("Achmad Rohadi", EiSeedDefaults.sanitizePersonNameOnly("Achmad Rohadi"))
        assertEquals("Andi Pratama", EiSeedDefaults.sanitizePersonNameOnly("Andi Pratama"))
        assertEquals("Khadam", EiSeedDefaults.sanitizePersonNameOnly("Teknisi 4 (Khadam)"))
        assertEquals("Budi Santoso", EiSeedDefaults.sanitizePersonNameOnly("Budi Santoso (Teknisi Mechanical)"))
        assertEquals("Siti Aminah", EiSeedDefaults.sanitizePersonNameOnly("Siti Aminah - Admin SCM"))
    }

    @Test
    fun `excel export includes only pekerjaan and scm jobs sheets without absensi gps`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val workOrders = listOf(
            WorkOrder(
                id = "wo_1",
                woCode = "WO-001",
                category = WorkOrderCategory.FUNCTION_TEST.label,
                equipmentTag = "PT-101A & Valve",
                technician = "Achmad Rohadi",
                description = "Cek kalibrasi 4-20mA <normal>",
                status = WorkOrderStatus.DONE.label,
                reportDate = "19 Nov 2026",
                beforePhotoUri = "fbphoto://photo_before_1",
                afterPhotoUri = "fbphoto://photo_after_1"
            )
        )
        val scmJobs = listOf(
            ScmJob(
                id = "scm_1",
                jobCode = "SCM-001",
                category = ScmJobCategory.MOB.label,
                title = "Mobilisasi Panel & Valve",
                referenceNumber = "MOB-001",
                picName = "Admin SCM",
                destinationOrVendor = "Site B",
                description = "Pengiriman peralatan ke site",
                status = ScmJobStatus.PROSES.label,
                jobDate = "19 Nov 2026",
                beforePhotoUri = "fbphoto://photo_scm_1"
            )
        )

        val dailyReports = listOf(
            DailyWorkReport(
                id = "dwr_1",
                reportCode = "DWR-001",
                workDate = "19 Nov 2026",
                technicianName = "Achmad Rohadi",
                category = DailyReportCategory.CALIBRATION.label,
                equipmentName = "Temperature Transmitter TT-102",
                workLocation = "Compressor Skid 01",
                activityDescription = "Kalibrasi temperature transmitter",
                startTime = "10.00",
                endTime = "12.00",
                workResult = "Akurasi dalam toleransi +/-0.2C",
                status = DailyReportStatus.SELESAI.label,
                issuesFound = "-",
                photoUris = listOf("fbphoto://daily_1", "fbphoto://daily_2")
            )
        )

        val briefings = listOf(
            BriefingNotice(
                id = "br_1",
                badge = "SAFETY REMINDER",
                timeTag = "Single Shift",
                title = "Wajib LOTO Panel MCC",
                summary = "Pastikan zero voltage check sebelum kerja panel.",
                isSafetyAlert = true,
                sortOrder = 0L
            )
        )

        val result = ReportExporter.exportExcel(context, workOrders, scmJobs, dailyReports, briefings)
        assertTrue(result.isSuccess)
        val file = result.getOrThrow()
        assertTrue(file.exists() && file.length() > 0)

        ZipFile(file).use { zip ->
            val workbook = zip.getInputStream(zip.getEntry("xl/workbook.xml")).bufferedReader().readText()
            val sheet1 = zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml")).bufferedReader().readText()
            val sheet2 = zip.getInputStream(zip.getEntry("xl/worksheets/sheet2.xml")).bufferedReader().readText()
            val sheet3Entry = zip.getEntry("xl/worksheets/sheet3.xml")
            val sheet4Entry = zip.getEntry("xl/worksheets/sheet4.xml")
            assertNotNull(sheet3Entry)
            assertNotNull(sheet4Entry)
            val sheet3 = zip.getInputStream(sheet3Entry).bufferedReader().readText()
            val sheet4 = zip.getInputStream(sheet4Entry).bufferedReader().readText()
            assertTrue(workbook.contains("name=\"EII\""))
            assertTrue(workbook.contains("Aktivitas_SCM"))
            assertTrue(workbook.contains("Daily_Work_Report"))
            assertTrue(workbook.contains("Info_Briefing"))
            assertFalse(workbook.contains("Absensi_GPS"))
            assertTrue(sheet1.contains("Achmad Rohadi"))
            assertTrue(sheet1.contains("PT-101A &amp; Valve"))
            assertTrue(sheet1.contains("&lt;normal&gt;"))
            assertTrue(sheet1.contains("Foto Sebelum"))
            assertTrue(sheet1.contains(">Ada (1/4)<"))
            assertTrue(sheet2.contains("Mobilisasi Panel &amp; Valve"))
            assertTrue(sheet2.contains("MOB-001"))
            assertTrue(sheet3.contains("Kalibrasi temperature transmitter"))
            assertTrue(sheet3.contains("Temperature Transmitter TT-102"))
            assertTrue(sheet3.contains("Akurasi dalam toleransi +/-0.2C"))
            assertTrue(sheet3.contains(">Ada (2/4)<"))
            assertTrue(sheet4.contains("Wajib LOTO Panel MCC"))
            assertTrue(sheet4.contains("Safety Alert"))
        }
    }

    @Test
    fun `pdf export uses updated headings, includes daily result and briefing section, and removes absensi`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = ReportExporter.exportPdf(
            context = context,
            workOrders = listOf(
                WorkOrder(
                    woCode = "WO-010",
                    category = WorkOrderCategory.INSTALASI.label,
                    equipmentTag = "PLC-01",
                    technician = "Achmad Rohadi",
                    description = "Instalasi panel"
                )
            ),
            scmJobs = listOf(
                ScmJob(
                    jobCode = "SCM-010",
                    category = ScmJobCategory.FORECAST.label,
                    title = "Forecast Cable",
                    picName = "Admin SCM",
                    description = "Stok Q4"
                )
            ),
            dailyWorkReports = listOf(
                DailyWorkReport(
                    reportCode = "DWR-010",
                    workDate = "22 Nov 2026",
                    technicianName = "Andi Pratama",
                    category = DailyReportCategory.DAILY_INSPECTION.label,
                    equipmentName = "PT-101",
                    workLocation = "Skid 01",
                    activityDescription = "Cek zero transmitter",
                    startTime = "08.30",
                    endTime = "10.00",
                    workResult = "Loop 4-20mA stabil",
                    status = DailyReportStatus.SELESAI.label,
                    issuesFound = "-"
                )
            ),
            briefings = listOf(
                BriefingNotice(
                    badge = "SHIFT BRIEFING",
                    timeTag = "Hari Ini",
                    title = "Briefing Morning Tool Box",
                    summary = "Pastikan APD lengkap di area workshop.",
                    isSafetyAlert = false
                )
            )
        )
        assertTrue(result.exceptionOrNull()?.stackTraceToString() ?: "PDF export failed", result.isSuccess)
        val file = result.getOrThrow()
        assertTrue(file.exists() && file.length() > 100L)
        val pdfText = file.readText(Charsets.ISO_8859_1)
        assertTrue(pdfText.startsWith("%PDF-"))
        assertTrue(pdfText.contains("1. REKAP AKTIVITAS ELECTRIKAL & INSTRUMENTASI"))
        assertTrue(pdfText.contains("2. REKAP AKTIVITAS SCM & LOGISTIK"))
        assertTrue(pdfText.contains("3. REKAP DAILY WORK REPORT"))
        assertTrue(pdfText.contains("Loop 4-20mA stabil"))
        assertTrue(pdfText.contains("4. REKAP INFO & BRIEFING TEAM"))
        assertTrue(pdfText.contains("Briefing Morning Tool Box"))
        assertTrue(pdfText.contains("Forecast Cable"))
        assertTrue(pdfText.contains("Achmad Rohadi"))
        assertTrue(pdfText.contains("Mengetahui / Disetujui Oleh,"))
        assertFalse(pdfText.contains("REKAP KEHADIRAN"))
        assertFalse(pdfText.contains("\\(                         \\)"))
    }

    @Test
    fun `report exporter mime types and work order photo defaults are valid`() {
        assertEquals(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            ReportExporter.excelMimeType()
        )
        assertEquals("application/pdf", ReportExporter.pdfMimeType())

        val defaultWo = WorkOrder(woCode = "WO-002")
        assertEquals(null, defaultWo.beforePhotoUri)
        assertEquals(null, defaultWo.afterPhotoUri)
    }

    @Test
    fun `scm job supports cloud photo refs and capture destination creates file uri`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scm = ScmJob(
            jobCode = "SCM-001",
            status = ScmJobStatus.OPEN.label,
            beforePhotoUri = "fbphoto://photo_1",
            afterPhotoUri = "fbphoto://photo_2"
        )
        assertEquals("fbphoto://photo_1", scm.beforePhotoUri)
        assertEquals("fbphoto://photo_2", scm.afterPhotoUri)
        assertEquals(ScmJobStatus.PROSES, ScmJobStatus.OPEN.next())

        val (contentUri, fileUri) = WorkOrderPhotoStorage.createCaptureDestination(context, "scm_before")
        assertTrue(contentUri.toString().contains("fileprovider"))
        assertTrue(fileUri.startsWith("file://"))
    }

    @Test
    fun `work order and scm job resolve up to 4 before and 4 after photos safely`() {
        assertEquals(4, EiSeedDefaults.MAX_PHOTOS_PER_SECTION)

        val wo = WorkOrder(
            woCode = "WO-044",
            beforePhotoUri = "fbphoto://legacy_before",
            beforePhotoUris = listOf(
                "fbphoto://b1",
                "fbphoto://b2",
                "fbphoto://b1",
                "fbphoto://b3",
                "fbphoto://b4",
                "fbphoto://b5"
            ),
            afterPhotoUri = "fbphoto://legacy_after"
        )
        assertEquals(
            listOf("fbphoto://b1", "fbphoto://b2", "fbphoto://b3", "fbphoto://b4"),
            wo.resolvedBeforePhotos()
        )
        assertEquals(listOf("fbphoto://legacy_after"), wo.resolvedAfterPhotos())

        val scm = ScmJob(
            jobCode = "SCM-044",
            beforePhotoUri = "fbphoto://sb1",
            beforePhotoUris = listOf("fbphoto://sb1", "fbphoto://sb2"),
            afterPhotoUris = listOf("fbphoto://sa1", "fbphoto://sa2", "fbphoto://sa3", "fbphoto://sa4", "fbphoto://sa5")
        )
        assertEquals(listOf("fbphoto://sb1", "fbphoto://sb2"), scm.resolvedBeforePhotos())
        assertEquals(4, scm.resolvedAfterPhotos().size)
    }

    @Test
    fun `excel and pdf export display multi-photo count formatting`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val wo = WorkOrder(
            woCode = "WO-055",
            category = WorkOrderCategory.PM.label,
            equipmentTag = "XV-101",
            technician = "Achmad Rohadi",
            description = "Preventive check",
            beforePhotoUris = listOf("fbphoto://p1", "fbphoto://p2", "fbphoto://p3", "fbphoto://p4"),
            afterPhotoUris = listOf("fbphoto://a1", "fbphoto://a2")
        )
        val excelFile = ReportExporter.exportExcel(context, listOf(wo), emptyList()).getOrThrow()
        ZipFile(excelFile).use { zip ->
            val sheet1 = zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml")).bufferedReader().readText()
            assertTrue(sheet1.contains(">Ada (4/4)<"))
            assertTrue(sheet1.contains(">Ada (2/4)<"))
        }

        val pdfFile = ReportExporter.exportPdf(context, listOf(wo), emptyList()).getOrThrow()
        val pdfText = pdfFile.readText(Charsets.ISO_8859_1)
        assertTrue(pdfText.contains("Sblm:4/4") || pdfText.contains("\\(4\\)") || pdfText.contains("(4)"))
        assertTrue(pdfText.contains("Ssdh:2/4") || pdfText.contains("\\(2\\)") || pdfText.contains("(2)"))
    }

    @Test
    fun `daily work report supports all 9 categories, time range, version 1_1, and pdf export`() {
        val expectedCategories = listOf(
            "Daily Inspection",
            "Preventive Maintenance",
            "Corrective Maintenance",
            "Breakdown",
            "Calibration",
            "Troubleshooting",
            "Installation",
            "Modification",
            "Other Activity"
        )
        assertEquals(expectedCategories, DailyReportCategory.entries.map { it.label })
        assertEquals("1.2", BuildConfig.VERSION_NAME)

        val report = DailyWorkReport(
            reportCode = "DWR-009",
            workDate = "19 Nov 2026",
            technicianName = "Andi Pratama",
            category = DailyReportCategory.TROUBLESHOOTING.label,
            equipmentName = "Panel MCC-02",
            workLocation = "Substation A",
            activityDescription = "Pengecekan panel MCC",
            startTime = "14.30",
            endTime = "16.00",
            workResult = "Isolasi beban aman",
            status = DailyReportStatus.PROSES.label,
            issuesFound = "Kontaktor aus",
            photoUris = listOf("fbphoto://d1", "fbphoto://d2", "fbphoto://d1", "fbphoto://d3", "fbphoto://d4", "fbphoto://d5")
        )
        assertEquals("14.30–16.00", report.formattedTimeRange())
        assertEquals(4, report.resolvedPhotos().size)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val pdfFile = ReportExporter.exportPdf(
            context = context,
            workOrders = emptyList(),
            scmJobs = emptyList(),
            dailyWorkReports = listOf(report)
        ).getOrThrow()
        val pdfText = pdfFile.readText(Charsets.ISO_8859_1)
        assertTrue(pdfText.contains("3. REKAP DAILY WORK REPORT"))
        assertTrue(pdfText.contains("Pengecekan panel MCC"))
        assertTrue(pdfText.contains("Isolasi beban aman"))
        assertTrue(pdfText.contains("Andi Pratama"))
    }

    @Test
    fun `whatsapp formatting, 16_00 schedule computation, and summary message generation work cleanly`() {
        assertEquals("08988319566", EiSeedDefaults.DEFAULT_WA_SENDER)
        assertEquals("628988319566", EiSeedDefaults.formatWhatsappWaId("08988319566"))
        assertEquals("6281234567890", EiSeedDefaults.formatWhatsappWaId("+62 812-3456-7890"))
        assertEquals("", EiSeedDefaults.formatWhatsappWaId("123"))

        val member = TeamMember(
            name = "Andi Pratama",
            whatsappNumber = "08123456789"
        )
        assertEquals("628123456789", EiSeedDefaults.formatWhatsappWaId(member.whatsappNumber))

        val before16 = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 22, 10, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nextFromMorning = Calendar.getInstance().apply {
            timeInMillis = WhatsAppReminderManager.computeNext1600Millis(before16.timeInMillis)
        }
        assertEquals(22, nextFromMorning.get(Calendar.DAY_OF_MONTH))
        assertEquals(16, nextFromMorning.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, nextFromMorning.get(Calendar.MINUTE))

        val after16 = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 22, 16, 5, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nextFromAfternoon = Calendar.getInstance().apply {
            timeInMillis = WhatsAppReminderManager.computeNext1600Millis(after16.timeInMillis)
        }
        assertEquals(23, nextFromAfternoon.get(Calendar.DAY_OF_MONTH))
        assertEquals(16, nextFromAfternoon.get(Calendar.HOUR_OF_DAY))

        val summary = WhatsAppReminderManager.buildActivitySummaryMessage(
            senderNumber = EiSeedDefaults.DEFAULT_WA_SENDER,
            workOrders = listOf(WorkOrder(woCode = "WO-001", equipmentTag = "PT-101", technician = "Andi Pratama")),
            dailyWorkReports = listOf(
                DailyWorkReport(
                    startTime = "08.00",
                    endTime = "09.00",
                    technicianName = "Budi Santoso",
                    activityDescription = "Inspection Valve",
                    workResult = "Tekanan normal",
                    status = DailyReportStatus.SELESAI.label
                )
            ),
            scmJobs = listOf(ScmJob(jobCode = "SCM-001", title = "PR Cable Gland", picName = "Admin SCM")),
            briefings = listOf(BriefingNotice(badge = "SAFETY", timeTag = "Hari Ini", title = "Wajib APD", summary = "Gunakan helm & kacamata safety."))
        )
        assertTrue(summary.contains("08988319566"))
        assertTrue(summary.contains("WO-001"))
        assertTrue(summary.contains("Inspection Valve"))
        assertTrue(summary.contains("Hasil: Tekanan normal"))
        assertTrue(summary.contains("PR Cable Gland"))
        assertTrue(summary.contains("Wajib APD"))
    }

    @Test
    fun `daily work reports and exports sort newest date first then chronological startTime`() {
        val dwr24Morning = DailyWorkReport(
            id = "d1",
            reportCode = "DWR-001",
            workDate = "24 Nov 2026",
            technicianName = "Andi Pratama",
            activityDescription = "Aktivitas Tanggal 24 Pagi",
            startTime = "08.00",
            endTime = "09.00",
            workResult = "OK"
        )
        val dwr25Afternoon = DailyWorkReport(
            id = "d2",
            reportCode = "DWR-002",
            workDate = "25 Nov 2026",
            technicianName = "Budi Santoso",
            activityDescription = "Aktivitas Tanggal 25 Siang",
            startTime = "13.00",
            endTime = "14.30",
            workResult = "OK"
        )
        val dwr25Morning = DailyWorkReport(
            id = "d3",
            reportCode = "DWR-003",
            workDate = "25 Nov 2026",
            technicianName = "Candra Wijaya",
            activityDescription = "Aktivitas Tanggal 25 Pagi",
            startTime = "08.30",
            endTime = "10.00",
            workResult = "OK"
        )

        val sorted = EiSortHelpers.sortDailyWorkReports(listOf(dwr24Morning, dwr25Afternoon, dwr25Morning))
        assertEquals(listOf("DWR-003", "DWR-002", "DWR-001"), sorted.map { it.reportCode })
        assertTrue(EiSortHelpers.parseDateSortKey("2 Des 2026") > EiSortHelpers.parseDateSortKey("25 Nov 2026"))
        assertTrue(EiSortHelpers.parseDateSortKey("2026-08-17") > EiSortHelpers.parseDateSortKey("1 Mei 2026"))

        val context = ApplicationProvider.getApplicationContext<Context>()
        val excelFile = ReportExporter.exportExcel(
            context = context,
            workOrders = emptyList(),
            scmJobs = emptyList(),
            dailyWorkReports = listOf(dwr24Morning, dwr25Afternoon, dwr25Morning)
        ).getOrThrow()
        ZipFile(excelFile).use { zip ->
            val sheet3 = zip.getInputStream(zip.getEntry("xl/worksheets/sheet3.xml")).bufferedReader().readText()
            val idx25Morning = sheet3.indexOf("Aktivitas Tanggal 25 Pagi")
            val idx25Afternoon = sheet3.indexOf("Aktivitas Tanggal 25 Siang")
            val idx24Morning = sheet3.indexOf("Aktivitas Tanggal 24 Pagi")
            assertTrue(idx25Morning in 0 until idx25Afternoon)
            assertTrue(idx25Afternoon in 0 until idx24Morning)
        }

        val pdfFile = ReportExporter.exportPdf(
            context = context,
            workOrders = emptyList(),
            scmJobs = emptyList(),
            dailyWorkReports = listOf(dwr24Morning, dwr25Afternoon, dwr25Morning)
        ).getOrThrow()
        val pdfText = pdfFile.readText(Charsets.ISO_8859_1)
        val pdfIdx25Morning = pdfText.indexOf("Aktivitas Tanggal 25 Pagi")
        val pdfIdx24Morning = pdfText.indexOf("Aktivitas Tanggal 24 Pagi")
        assertTrue(pdfIdx25Morning in 0 until pdfIdx24Morning)
    }

    @Test
    fun `whatsapp sender resolves logged in user and custom job titles persist cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("wa_reminder_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("custom_job_titles_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        val team = listOf(
            TeamMember(name = "Achmad Rohadi", role = "Supervisor E&II", whatsappNumber = "08111222333"),
            TeamMember(name = "Andi Pratama", role = "Teknisi Electrical & Instrumentation", whatsappNumber = "08222333444")
        )

        val resolved = WhatsAppReminderManager.resolveSender(
            context = context,
            currentUserName = "Achmad Rohadi",
            teamMembers = team
        )
        assertEquals("Achmad Rohadi", resolved.first)
        assertEquals("08111222333", resolved.second)

        val msg = WhatsAppReminderManager.buildActivitySummaryMessage(
            senderNumber = resolved.second,
            senderName = resolved.first,
            workOrders = emptyList(),
            dailyWorkReports = emptyList(),
            scmJobs = emptyList(),
            briefings = emptyList()
        )
        assertTrue(msg.contains("Pengirim: Achmad Rohadi (08111222333)"))
        assertFalse(msg.contains("08988319566"))

        CustomJobTitleStore.addCustomTitle(context, "Lead Instrument Engineer")
        CustomJobTitleStore.addCustomTitle(context, "lead instrument engineer") // duplicate case-insensitive
        val merged = CustomJobTitleStore.mergedJobTitles(
            context,
            listOf(TeamMember(name = "Test", role = "QA/QC Inspector"))
        )
        assertEquals(1, merged.count { it.equals("Lead Instrument Engineer", ignoreCase = true) })
        assertTrue(merged.contains("QA/QC Inspector"))
    }

    @Test
    fun `export title, filenames, and pic ticker summary match expected formats`() {
        val fixedCal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 25, 5, 24, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fixedDate = fixedCal.time

        assertEquals("SCM dan EII Report 25 Nov 2026 5:24", ReportExporter.buildReportTitle(fixedDate))
        assertEquals("SCM dan EII Report 25 Nov 2026 5.24.pdf", ReportExporter.buildExportFileName("pdf", fixedDate))
        assertEquals("SCM dan EII Report 25 Nov 2026 5.24.xlsx", ReportExporter.buildExportFileName("xlsx", fixedDate))

        val context = ApplicationProvider.getApplicationContext<Context>()
        val excelFile = ReportExporter.exportExcel(
            context = context,
            workOrders = listOf(WorkOrder(woCode = "WO-100", technician = "Achmad Rohadi", status = WorkOrderStatus.OPEN.label)),
            scmJobs = listOf(ScmJob(jobCode = "SCM-100", picName = "Khadam", status = ScmJobStatus.PROSES.label)),
            dailyWorkReports = listOf(
                DailyWorkReport(
                    reportCode = "DWR-100",
                    technicianName = "Achmad Rohadi",
                    status = DailyReportStatus.PROSES.label
                )
            ),
            now = fixedDate
        ).getOrThrow()
        assertEquals("SCM dan EII Report 25 Nov 2026 5.24.xlsx", excelFile.name)
        ZipFile(excelFile).use { zip ->
            val sheet1 = zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml")).bufferedReader().readText()
            assertTrue(sheet1.contains("SCM dan EII Report 25 Nov 2026 5:24"))
        }

        val pdfFile = ReportExporter.exportPdf(
            context = context,
            workOrders = listOf(WorkOrder(woCode = "WO-100", technician = "Achmad Rohadi", status = WorkOrderStatus.OPEN.label)),
            scmJobs = emptyList(),
            now = fixedDate
        ).getOrThrow()
        assertEquals("SCM dan EII Report 25 Nov 2026 5.24.pdf", pdfFile.name)
        val pdfText = pdfFile.readText(Charsets.ISO_8859_1)
        assertTrue(pdfText.contains("SCM dan EII Report 25 Nov 2026 5:24"))

        val summaries = EiActivitySummaryHelpers.buildPicTickerSummaries(
            workOrders = listOf(
                WorkOrder(technician = "Achmad Rohadi", status = WorkOrderStatus.OPEN.label),
                WorkOrder(technician = "Achmad Rohadi", status = WorkOrderStatus.IN_PROGRESS.label),
                WorkOrder(technician = "Khadam", status = WorkOrderStatus.OPEN.label)
            ),
            dailyWorkReports = listOf(
                DailyWorkReport(technicianName = "Achmad Rohadi", status = DailyReportStatus.PROSES.label),
                DailyWorkReport(technicianName = "Achmad Rohadi", status = DailyReportStatus.SELESAI.label)
            ),
            scmJobs = listOf(
                ScmJob(picName = "Achmad Rohadi", status = ScmJobStatus.OPEN.label),
                ScmJob(picName = "Khadam", status = ScmJobStatus.PROSES.label)
            )
        )
        val ticker = EiActivitySummaryHelpers.formatPicTickerText(summaries)
        assertTrue(ticker.contains("Achmad Rohadi Open (2) Progress (2)"))
        assertTrue(ticker.contains("Khadam Open (1) Progress (1)"))
    }
}
