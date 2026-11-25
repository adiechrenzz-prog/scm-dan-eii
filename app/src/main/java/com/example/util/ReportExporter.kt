package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.BriefingNotice
import com.example.data.model.DailyWorkReport
import com.example.data.model.EiSeedDefaults
import com.example.data.model.EiSortHelpers
import com.example.data.model.ScmJob
import com.example.data.model.WorkOrder
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ReportExporter {

    private const val EXCEL_MIME =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    private const val PDF_MIME = "application/pdf"

    fun excelMimeType(): String = EXCEL_MIME
    fun pdfMimeType(): String = PDF_MIME

    fun buildReportTitle(now: Date = Date()): String {
        val fmt = SimpleDateFormat("d MMM yyyy H:mm", Locale.forLanguageTag("id-ID"))
        return "SCM dan EII Report ${fmt.format(now)}"
    }

    fun buildExportFileName(extension: String, now: Date = Date()): String {
        val fmt = SimpleDateFormat("d MMM yyyy H.mm", Locale.forLanguageTag("id-ID"))
        return "SCM dan EII Report ${fmt.format(now)}.$extension"
    }

    fun exportExcel(
        context: Context,
        workOrders: List<WorkOrder>,
        scmJobs: List<ScmJob>,
        dailyWorkReports: List<DailyWorkReport> = emptyList(),
        briefings: List<BriefingNotice> = emptyList(),
        now: Date = Date()
    ): Result<File> = runCatching {
        val outDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val reportTitle = buildReportTitle(now)
        val file = File(outDir, buildExportFileName("xlsx", now))

        val sortedWorkOrders = EiSortHelpers.sortWorkOrders(workOrders)
        val sortedScmJobs = EiSortHelpers.sortScmJobs(scmJobs)
        val sortedDailyReports = EiSortHelpers.sortDailyWorkReports(dailyWorkReports)

        val woRows = buildList {
            add(listOf(reportTitle))
            add(listOf("ID", "Kategori", "Tag / Pekerjaan", "PIC", "Status", "Tanggal", "Foto Sebelum", "Foto Sesudah", "Keterangan"))
            sortedWorkOrders.forEach { wo ->
                val beforeCount = wo.resolvedBeforePhotos().size
                val afterCount = wo.resolvedAfterPhotos().size
                add(
                    listOf(
                        wo.woCode,
                        wo.category,
                        wo.equipmentTag,
                        EiSeedDefaults.sanitizePersonNameOnly(wo.technician),
                        wo.status,
                        wo.reportDate,
                        if (beforeCount > 0) "Ada ($beforeCount/4)" else "-",
                        if (afterCount > 0) "Ada ($afterCount/4)" else "-",
                        wo.description
                    )
                )
            }
        }

        val scmRows = buildList {
            add(listOf(reportTitle))
            add(listOf("ID", "Kategori", "Judul Job / Barang", "No. Referensi", "PIC", "Vendor / Tujuan", "Status", "Tanggal", "Foto Sebelum", "Foto Sesudah", "Rincian"))
            sortedScmJobs.forEach { scm ->
                val beforeCount = scm.resolvedBeforePhotos().size
                val afterCount = scm.resolvedAfterPhotos().size
                add(
                    listOf(
                        scm.jobCode,
                        scm.category,
                        scm.title,
                        scm.referenceNumber.ifBlank { "-" },
                        EiSeedDefaults.sanitizePersonNameOnly(scm.picName),
                        scm.destinationOrVendor.ifBlank { "-" },
                        scm.status,
                        scm.jobDate,
                        if (beforeCount > 0) "Ada ($beforeCount/4)" else "-",
                        if (afterCount > 0) "Ada ($afterCount/4)" else "-",
                        scm.description
                    )
                )
            }
        }

        val dailyRows = buildList {
            add(listOf(reportTitle))
            add(
                listOf(
                    "Tanggal",
                    "Jam Mulai",
                    "Jam Selesai",
                    "ID",
                    "Teknisi",
                    "Kategori",
                    "Equipment",
                    "Lokasi",
                    "Aktivitas",
                    "Hasil Pekerjaan",
                    "Status",
                    "Kendala",
                    "Foto"
                )
            )
            sortedDailyReports.forEach { rep ->
                val photoCount = rep.resolvedPhotos().size
                add(
                    listOf(
                        rep.workDate,
                        rep.startTime,
                        rep.endTime,
                        rep.reportCode,
                        EiSeedDefaults.sanitizePersonNameOnly(rep.technicianName),
                        rep.category,
                        rep.equipmentName,
                        rep.workLocation,
                        rep.activityDescription,
                        rep.workResult.ifBlank { "-" },
                        rep.status,
                        rep.issuesFound.ifBlank { "-" },
                        if (photoCount > 0) "Ada ($photoCount/4)" else "-"
                    )
                )
            }
        }

        val briefingRows = buildList {
            add(listOf(reportTitle))
            add(listOf("No", "Badge", "Waktu / Shift", "Judul Briefing", "Isi / Arahan", "Prioritas Safety"))
            briefings.sortedBy { it.sortOrder }.forEachIndexed { idx, notice ->
                add(
                    listOf(
                        "${idx + 1}",
                        notice.badge,
                        notice.timeTag,
                        notice.title,
                        notice.summary,
                        if (notice.isSafetyAlert) "Safety Alert" else "Umum"
                    )
                )
            }
        }

        FileOutputStream(file).use { fos ->
            ZipOutputStream(fos).use { zos ->
                writeZipEntry(zos, "[Content_Types].xml", buildContentTypesXml())
                writeZipEntry(zos, "_rels/.rels", buildRootRelsXml())
                writeZipEntry(zos, "xl/workbook.xml", buildWorkbookXml())
                writeZipEntry(zos, "xl/_rels/workbook.xml.rels", buildWorkbookRelsXml())
                writeZipEntry(zos, "xl/styles.xml", buildStylesXml())
                writeZipEntry(
                    zos,
                    "xl/worksheets/sheet1.xml",
                    buildSheetXml(woRows, listOf(12, 24, 28, 22, 15, 16, 15, 15, 48))
                )
                writeZipEntry(
                    zos,
                    "xl/worksheets/sheet2.xml",
                    buildSheetXml(scmRows, listOf(12, 22, 30, 18, 20, 24, 14, 16, 15, 15, 44))
                )
                writeZipEntry(
                    zos,
                    "xl/worksheets/sheet3.xml",
                    buildSheetXml(dailyRows, listOf(15, 12, 12, 12, 20, 24, 24, 20, 36, 36, 12, 24, 12))
                )
                writeZipEntry(
                    zos,
                    "xl/worksheets/sheet4.xml",
                    buildSheetXml(briefingRows, listOf(8, 22, 18, 32, 54, 16))
                )
            }
        }
        file
    }

    fun exportPdf(
        context: Context,
        workOrders: List<WorkOrder>,
        scmJobs: List<ScmJob>,
        dailyWorkReports: List<DailyWorkReport> = emptyList(),
        briefings: List<BriefingNotice> = emptyList(),
        now: Date = Date()
    ): Result<File> = runCatching {
        val outDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val reportTitle = buildReportTitle(now)
        val file = File(outDir, buildExportFileName("pdf", now))

        try {
            writeStyledAndroidPdf(file, reportTitle, workOrders, scmJobs, dailyWorkReports, briefings)
        } catch (e: Throwable) {
            if (e is IllegalStateException || e is UnsatisfiedLinkError || e is UnsupportedOperationException) {
                writePlainPdfFallback(file, reportTitle, workOrders, scmJobs, dailyWorkReports, briefings)
            } else {
                throw e
            }
        }
        file
    }

    private fun writeStyledAndroidPdf(
        file: File,
        reportTitle: String,
        workOrders: List<WorkOrder>,
        scmJobs: List<ScmJob>,
        dailyWorkReports: List<DailyWorkReport> = emptyList(),
        briefings: List<BriefingNotice> = emptyList()
    ) {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 32f
        val contentWidth = pageWidth - (margin * 2)

        val sortedWorkOrders = EiSortHelpers.sortWorkOrders(workOrders)
        val sortedScmJobs = EiSortHelpers.sortScmJobs(scmJobs)
        val sortedDailyReports = EiSortHelpers.sortDailyWorkReports(dailyWorkReports)

        val pdf = PdfDocument()
        try {
            var pageNum = 1
            var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
            var canvas = page.canvas
            var y = 40f

            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(11, 77, 179)
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(71, 85, 105)
                textSize = 9.5f
            }
            val sectionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 11.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val headerBgBlue = Paint().apply { color = Color.rgb(11, 77, 179) }
            val headerBgTeal = Paint().apply { color = Color.rgb(15, 118, 110) }
            val headerBgEmerald = Paint().apply { color = Color.rgb(5, 150, 105) }
            val headerBgIndigo = Paint().apply { color = Color.rgb(79, 70, 229) }
            val rowStripePaint = Paint().apply { color = Color.rgb(248, 250, 252) }
            val borderPaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }
            val headerTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val cellTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(30, 41, 59)
                textSize = 8.2f
            }

            fun ensureSpace(neededHeight: Float) {
                if (y + neededHeight > pageHeight - 48f) {
                    pdf.finishPage(page)
                    pageNum += 1
                    page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
                    canvas = page.canvas
                    y = 40f
                }
            }

            fun drawTableRow(
                cols: List<String>,
                widths: List<Float>,
                isHeader: Boolean,
                headerPaint: Paint,
                striped: Boolean
            ) {
                val layouts = cols.mapIndexed { idx, text ->
                    val w = (widths[idx] - 8f).toInt().coerceAtLeast(20)
                    StaticLayout.Builder.obtain(
                        text,
                        0,
                        text.length,
                        if (isHeader) headerTextPaint else cellTextPaint,
                        w
                    )
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0f, 1.15f)
                        .build()
                }
                val rowHeight = (layouts.maxOfOrNull { it.height } ?: 12) + 10f
                ensureSpace(rowHeight + 4f)

                var x = margin
                if (isHeader) {
                    canvas.drawRect(margin, y, margin + contentWidth, y + rowHeight, headerPaint)
                } else {
                    if (striped) {
                        canvas.drawRect(margin, y, margin + contentWidth, y + rowHeight, rowStripePaint)
                    }
                    canvas.drawRect(margin, y, margin + contentWidth, y + rowHeight, borderPaint)
                }

                layouts.forEachIndexed { idx, layout ->
                    canvas.save()
                    canvas.translate(x + 4f, y + 5f)
                    layout.draw(canvas)
                    canvas.restore()
                    x += widths[idx]
                }
                y += rowHeight
            }

            // Document Header
            canvas.drawText(reportTitle, margin, y, titlePaint)
            y += 16f
            canvas.drawText(
                "LAPORAN HARIAN SCM DAN EII TEAM  •  EII: ${workOrders.size} | SCM: ${scmJobs.size} | Harian: ${dailyWorkReports.size} | Briefing: ${briefings.size}",
                margin,
                y,
                subtitlePaint
            )
            y += 22f

            // Section 1: Aktivitas Electrikal & Instrumentasi
            canvas.drawText("1. REKAP AKTIVITAS ELECTRIKAL & INSTRUMENTASI", margin, y, sectionPaint)
            y += 10f
            val woWidths = listOf(45f, 74f, 88f, 72f, 50f, 56f, contentWidth - 385f)
            drawTableRow(
                listOf("ID", "Kategori", "Tag / Alat", "PIC", "Status", "Foto", "Keterangan"),
                woWidths,
                isHeader = true,
                headerPaint = headerBgBlue,
                striped = false
            )
            if (sortedWorkOrders.isEmpty()) {
                drawTableRow(
                    listOf("-", "-", "Belum ada data pekerjaan EII", "-", "-", "-", "-"),
                    woWidths,
                    isHeader = false,
                    headerPaint = headerBgBlue,
                    striped = false
                )
            } else {
                sortedWorkOrders.forEachIndexed { index, wo ->
                    val beforeCount = wo.resolvedBeforePhotos().size
                    val afterCount = wo.resolvedAfterPhotos().size
                    val beforeMark = if (beforeCount > 0) "✓ ($beforeCount)" else "-"
                    val afterMark = if (afterCount > 0) "✓ ($afterCount)" else "-"
                    val photoSummary = "Sblm: $beforeMark\nSsdh: $afterMark"
                    drawTableRow(
                        listOf(
                            wo.woCode,
                            wo.category,
                            wo.equipmentTag,
                            EiSeedDefaults.sanitizePersonNameOnly(wo.technician),
                            wo.status,
                            photoSummary,
                            wo.description
                        ),
                        woWidths,
                        isHeader = false,
                        headerPaint = headerBgBlue,
                        striped = index % 2 == 1
                    )
                }
            }

            y += 22f
            ensureSpace(70f)

            // Section 2: SCM Jobs
            canvas.drawText("2. REKAP AKTIVITAS SCM & LOGISTIK", margin, y, sectionPaint)
            y += 10f
            val scmWidths = listOf(48f, 72f, 92f, 68f, 50f, 54f, contentWidth - 384f)
            drawTableRow(
                listOf("ID", "Kategori", "Judul & Ref", "PIC", "Status", "Foto", "Rincian & Tujuan"),
                scmWidths,
                isHeader = true,
                headerPaint = headerBgTeal,
                striped = false
            )
            if (sortedScmJobs.isEmpty()) {
                drawTableRow(
                    listOf("-", "-", "Belum ada aktivitas SCM", "-", "-", "-", "-"),
                    scmWidths,
                    isHeader = false,
                    headerPaint = headerBgTeal,
                    striped = false
                )
            } else {
                sortedScmJobs.forEachIndexed { index, scm ->
                    val beforeCount = scm.resolvedBeforePhotos().size
                    val afterCount = scm.resolvedAfterPhotos().size
                    val beforeMark = if (beforeCount > 0) "✓ ($beforeCount)" else "-"
                    val afterMark = if (afterCount > 0) "✓ ($afterCount)" else "-"
                    val photoSummary = "Sblm: $beforeMark\nSsdh: $afterMark"
                    val titleAndRef = if (scm.referenceNumber.isNotBlank()) {
                        "${scm.title}\nRef: ${scm.referenceNumber}"
                    } else {
                        scm.title
                    }
                    val detailAndDest = if (scm.destinationOrVendor.isNotBlank()) {
                        "[${scm.destinationOrVendor}] ${scm.description}"
                    } else {
                        scm.description
                    }
                    drawTableRow(
                        listOf(
                            scm.jobCode,
                            scm.category,
                            titleAndRef,
                            EiSeedDefaults.sanitizePersonNameOnly(scm.picName),
                            scm.status,
                            photoSummary,
                            detailAndDest
                        ),
                        scmWidths,
                        isHeader = false,
                        headerPaint = headerBgTeal,
                        striped = index % 2 == 1
                    )
                }
            }

            y += 22f
            ensureSpace(70f)

            // Section 3: Daily Work Report
            canvas.drawText("3. REKAP DAILY WORK REPORT (HASIL HARIAN)", margin, y, sectionPaint)
            y += 10f
            val dailyWidths = listOf(66f, 68f, 94f, contentWidth - 364f, 88f, 48f)
            drawTableRow(
                listOf("Waktu & Tgl", "Teknisi", "Kategori & Alat", "Aktivitas & Hasil Pekerjaan", "Kendala & Foto", "Status"),
                dailyWidths,
                isHeader = true,
                headerPaint = headerBgEmerald,
                striped = false
            )
            if (sortedDailyReports.isEmpty()) {
                drawTableRow(
                    listOf("-", "-", "Belum ada laporan harian", "-", "-", "-"),
                    dailyWidths,
                    isHeader = false,
                    headerPaint = headerBgEmerald,
                    striped = false
                )
            } else {
                sortedDailyReports.forEachIndexed { index, rep ->
                    val photoCount = rep.resolvedPhotos().size
                    val photoLabel = if (photoCount > 0) "Foto: $photoCount/4" else "Foto: -"
                    val kendalaText = if (rep.issuesFound.isNotBlank() && rep.issuesFound != "-") {
                        "${rep.issuesFound}\n$photoLabel"
                    } else {
                        "-\n$photoLabel"
                    }
                    val hasilText = rep.workResult.ifBlank { "-" }
                    drawTableRow(
                        listOf(
                            "${rep.formattedTimeRange()}\n${rep.workDate}",
                            EiSeedDefaults.sanitizePersonNameOnly(rep.technicianName),
                            "${rep.category}\n${rep.equipmentName}\n(${rep.workLocation})",
                            "${rep.activityDescription}\nHasil: $hasilText",
                            kendalaText,
                            rep.status
                        ),
                        dailyWidths,
                        isHeader = false,
                        headerPaint = headerBgEmerald,
                        striped = index % 2 == 1
                    )
                }
            }

            y += 22f
            ensureSpace(70f)

            // Section 4: Info & Briefing Team
            canvas.drawText("4. REKAP INFO & BRIEFING TEAM", margin, y, sectionPaint)
            y += 10f
            val briefingWidths = listOf(28f, 90f, 68f, 120f, contentWidth - 306f)
            drawTableRow(
                listOf("No", "Kategori / Badge", "Waktu", "Judul Briefing", "Isi & Arahan Team"),
                briefingWidths,
                isHeader = true,
                headerPaint = headerBgIndigo,
                striped = false
            )
            if (briefings.isEmpty()) {
                drawTableRow(
                    listOf("-", "-", "-", "Belum ada info & briefing team", "-"),
                    briefingWidths,
                    isHeader = false,
                    headerPaint = headerBgIndigo,
                    striped = false
                )
            } else {
                briefings.sortedBy { it.sortOrder }.forEachIndexed { index, notice ->
                    val badgeLabel = if (notice.isSafetyAlert) {
                        "${notice.badge}\n[SAFETY]"
                    } else {
                        notice.badge
                    }
                    drawTableRow(
                        listOf(
                            "${index + 1}",
                            badgeLabel,
                            notice.timeTag,
                            notice.title,
                            notice.summary
                        ),
                        briefingWidths,
                        isHeader = false,
                        headerPaint = headerBgIndigo,
                        striped = index % 2 == 1
                    )
                }
            }

            // Signature Block
            y += 30f
            ensureSpace(80f)
            val sigX = pageWidth - margin - 170f
            canvas.drawText("Mengetahui / Disetujui Oleh,", sigX, y, subtitlePaint)

            pdf.finishPage(page)
            FileOutputStream(file).use { pdf.writeTo(it) }
        } finally {
            pdf.close()
        }
    }

    private fun escapePdfText(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                '\\' -> sb.append("\\\\")
                '(' -> sb.append("\\(")
                ')' -> sb.append("\\)")
                '\n', '\r' -> sb.append(' ')
                else -> if (ch.code in 32..126) sb.append(ch) else sb.append(' ')
            }
        }
        return sb.toString()
    }

    private fun writePlainPdfFallback(
        file: File,
        reportTitle: String,
        workOrders: List<WorkOrder>,
        scmJobs: List<ScmJob>,
        dailyWorkReports: List<DailyWorkReport> = emptyList(),
        briefings: List<BriefingNotice> = emptyList()
    ) {
        val sortedWorkOrders = EiSortHelpers.sortWorkOrders(workOrders)
        val sortedScmJobs = EiSortHelpers.sortScmJobs(scmJobs)
        val sortedDailyReports = EiSortHelpers.sortDailyWorkReports(dailyWorkReports)

        val lines = buildList {
            add(reportTitle)
            add("LAPORAN HARIAN SCM DAN EII TEAM")
            add("1. REKAP AKTIVITAS ELECTRIKAL & INSTRUMENTASI (${sortedWorkOrders.size})")
            if (sortedWorkOrders.isEmpty()) {
                add("- Belum ada data pekerjaan")
            } else {
                sortedWorkOrders.forEach { wo ->
                    val bCount = wo.resolvedBeforePhotos().size
                    val aCount = wo.resolvedAfterPhotos().size
                    add("${wo.woCode} | ${wo.category} | ${wo.equipmentTag} | ${EiSeedDefaults.sanitizePersonNameOnly(wo.technician)} | ${wo.status} | Foto Sblm:$bCount/4 Ssdh:$aCount/4")
                }
            }
            add("2. REKAP AKTIVITAS SCM & LOGISTIK (${sortedScmJobs.size})")
            if (sortedScmJobs.isEmpty()) {
                add("- Belum ada aktivitas SCM")
            } else {
                sortedScmJobs.forEach { scm ->
                    val bCount = scm.resolvedBeforePhotos().size
                    val aCount = scm.resolvedAfterPhotos().size
                    add("${scm.jobCode} | ${scm.category} | ${scm.title} | ${EiSeedDefaults.sanitizePersonNameOnly(scm.picName)} | ${scm.status} | Foto Sblm:$bCount/4 Ssdh:$aCount/4")
                }
            }
            add("3. REKAP DAILY WORK REPORT (${sortedDailyReports.size})")
            if (sortedDailyReports.isEmpty()) {
                add("- Belum ada laporan harian")
            } else {
                sortedDailyReports.forEach { rep ->
                    val pCount = rep.resolvedPhotos().size
                    val hasil = rep.workResult.ifBlank { "-" }
                    add("${rep.formattedTimeRange()} | ${rep.workDate} | ${EiSeedDefaults.sanitizePersonNameOnly(rep.technicianName)} | ${rep.category} | ${rep.equipmentName} | ${rep.activityDescription} | Hasil: $hasil | ${rep.status} | Foto:$pCount/4")
                }
            }
            add("4. REKAP INFO & BRIEFING TEAM (${briefings.size})")
            if (briefings.isEmpty()) {
                add("- Belum ada info briefing")
            } else {
                briefings.sortedBy { it.sortOrder }.forEach { br ->
                    add("[${br.badge}] ${br.timeTag} | ${br.title} | ${br.summary}")
                }
            }
            add("Mengetahui / Disetujui Oleh,")
        }

        val streamBody = buildString {
            append("BT\n/F1 10 Tf\n36 800 Td\n14 TL\n")
            lines.forEachIndexed { idx, line ->
                if (idx > 0) append("T*\n")
                append("(${escapePdfText(line)}) Tj\n")
            }
            append("ET\n")
        }

        val objects = listOf(
            "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
            "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
            "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n",
            "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n",
            "5 0 obj\n<< /Length ${streamBody.toByteArray(Charsets.US_ASCII).size} >>\nstream\n${streamBody}endstream\nendobj\n"
        )

        val sb = StringBuilder("%PDF-1.4\n")
        val offsets = mutableListOf(0)
        objects.forEach { obj ->
            offsets.add(sb.toString().toByteArray(Charsets.US_ASCII).size)
            sb.append(obj)
        }
        val xrefStart = sb.toString().toByteArray(Charsets.US_ASCII).size
        sb.append("xref\n0 ${objects.size + 1}\n")
        sb.append("0000000000 65535 f \n")
        offsets.drop(1).forEach { off ->
            sb.append(String.format(Locale.US, "%010d 00000 n \n", off))
        }
        sb.append("trailer\n<< /Size ${objects.size + 1} /Root 1 0 R >>\n")
        sb.append("startxref\n$xrefStart\n%%EOF\n")

        FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.US_ASCII)) }
    }

    fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    fun writeFileToUri(context: Context, sourceFile: File, targetUri: Uri): Result<Unit> = runCatching {
        context.contentResolver.openOutputStream(targetUri)?.use { output ->
            sourceFile.inputStream().use { input ->
                input.copyTo(output)
            }
        } ?: error("Tidak dapat menulis ke lokasi tujuan.")
    }

    private fun writeZipEntry(zos: ZipOutputStream, name: String, content: String) {
        zos.putNextEntry(ZipEntry(name))
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }

    private fun escapeXml(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                '\'' -> sb.append("&apos;")
                else -> if (ch.code >= 0x20 || ch == '\n' || ch == '\r' || ch == '\t') {
                    sb.append(ch)
                }
            }
        }
        return sb.toString()
    }

    private fun columnRef(colIndex: Int): String {
        var n = colIndex + 1
        val sb = StringBuilder()
        while (n > 0) {
            val rem = (n - 1) % 26
            sb.append(('A'.code + rem).toChar())
            n = (n - 1) / 26
        }
        return sb.reverse().toString()
    }

    private fun buildSheetXml(rows: List<List<String>>, colWidths: List<Int>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
        sb.append("<cols>")
        colWidths.forEachIndexed { idx, w ->
            sb.append("<col min=\"${idx + 1}\" max=\"${idx + 1}\" width=\"$w\" customWidth=\"1\"/>")
        }
        sb.append("</cols>")
        sb.append("<sheetData>")
        rows.forEachIndexed { rowIdx, row ->
            val r = rowIdx + 1
            sb.append("<row r=\"$r\">")
            row.forEachIndexed { colIdx, value ->
                val ref = "${columnRef(colIdx)}$r"
                val styleAttr = if (rowIdx == 1) " s=\"1\"" else ""
                sb.append("<c r=\"$ref\" t=\"inlineStr\"$styleAttr><is><t>${escapeXml(value)}</t></is></c>")
            }
            sb.append("</row>")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun buildContentTypesXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
          <Default Extension="xml" ContentType="application/xml"/>
          <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
          <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
          <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
          <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
          <Override PartName="/xl/worksheets/sheet4.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
          <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
        </Types>
    """.trimIndent()

    private fun buildRootRelsXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
        </Relationships>
    """.trimIndent()

    private fun buildWorkbookXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
          <sheets>
            <sheet name="EII" sheetId="1" r:id="rId1"/>
            <sheet name="Aktivitas_SCM" sheetId="2" r:id="rId2"/>
            <sheet name="Daily_Work_Report" sheetId="3" r:id="rId3"/>
            <sheet name="Info_Briefing" sheetId="4" r:id="rId4"/>
          </sheets>
        </workbook>
    """.trimIndent()

    private fun buildWorkbookRelsXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
          <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
          <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
          <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet4.xml"/>
          <Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
        </Relationships>
    """.trimIndent()

    private fun buildStylesXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
          <fonts count="2">
            <font><sz val="11"/><name val="Calibri"/></font>
            <font><b/><color rgb="FFFFFFFF"/><sz val="11"/><name val="Calibri"/></font>
          </fonts>
          <fills count="3">
            <fill><patternFill patternType="none"/></fill>
            <fill><patternFill patternType="gray125"/></fill>
            <fill><patternFill patternType="solid"><fgColor rgb="FF0B4DB3"/></patternFill></fill>
          </fills>
          <borders count="1">
            <border><left/><right/><top/><bottom/><diagonal/></border>
          </borders>
          <cellStyleXfs count="1">
            <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
          </cellStyleXfs>
          <cellXfs count="2">
            <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
            <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
          </cellXfs>
        </styleSheet>
    """.trimIndent()
}
