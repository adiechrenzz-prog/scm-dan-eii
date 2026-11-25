package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.TableView
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CobaltBright
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanSurface
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoSurface
import com.example.ui.theme.PurplePdf
import com.example.ui.theme.PurpleSurface
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseSurface
import com.example.ui.theme.SinercoTeal
import com.example.ui.theme.SlateBorder

data class MenuShortcut(
    val tag: String,
    val label: String,
    val icon: ImageVector,
    val iconTint: Color,
    val containerColor: Color,
    val onClick: () -> Unit
)

@Composable
fun OperationalMenuGrid(
    totalPersonnelCount: Int,
    dailyReportCount: Int = 0,
    onOpenDailyReportTab: () -> Unit = {},
    onOpenDailyReportInput: () -> Unit = {},
    onOpenWaReminder: () -> Unit = {},
    onOpenPm: () -> Unit,
    onOpenCm: () -> Unit,
    onOpenBreakdown: () -> Unit,
    onOpenMonitoring: () -> Unit,
    onOpenWorkStatus: () -> Unit,
    onOpenTeamRoster: () -> Unit,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shortcuts = listOf(
        MenuShortcut(
            tag = "menu_pm",
            label = "Preventive\n(PM)",
            icon = Icons.Rounded.FactCheck,
            iconTint = CobaltPrimary,
            containerColor = CobaltLight,
            onClick = onOpenPm
        ),
        MenuShortcut(
            tag = "menu_cm",
            label = "Corrective\n(CM)",
            icon = Icons.Rounded.Build,
            iconTint = AmberWarning,
            containerColor = AmberSurface,
            onClick = onOpenCm
        ),
        MenuShortcut(
            tag = "menu_breakdown",
            label = "Breakdown\nReport",
            icon = Icons.Rounded.WarningAmber,
            iconTint = RoseAlert,
            containerColor = RoseSurface,
            onClick = onOpenBreakdown
        ),
        MenuShortcut(
            tag = "menu_monitoring",
            label = "Monitoring\nKerja",
            icon = Icons.Rounded.Assessment,
            iconTint = IndigoAccent,
            containerColor = IndigoSurface,
            onClick = onOpenMonitoring
        ),
        MenuShortcut(
            tag = "menu_status_posisi",
            label = "Status Posisi\nKerja",
            icon = Icons.Rounded.LocationOn,
            iconTint = EmeraldSuccess,
            containerColor = EmeraldSurface,
            onClick = onOpenWorkStatus
        ),
        MenuShortcut(
            tag = "menu_personel",
            label = "Personel\nTeam ($totalPersonnelCount)",
            icon = Icons.Rounded.Groups,
            iconTint = CyanAccent,
            containerColor = CyanSurface,
            onClick = onOpenTeamRoster
        ),
        MenuShortcut(
            tag = "menu_export_excel",
            label = "Export\nExcel",
            icon = Icons.Rounded.TableView,
            iconTint = EmeraldSuccess,
            containerColor = EmeraldSurface,
            onClick = onExportExcel
        ),
        MenuShortcut(
            tag = "menu_export_pdf",
            label = "Export\nPDF",
            icon = Icons.Rounded.PictureAsPdf,
            iconTint = PurplePdf,
            containerColor = PurpleSurface,
            onClick = onExportPdf
        )
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.menu_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Akses cepat operasional harian",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                onClick = onOpenWaReminder,
                color = EmeraldSurface,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, EmeraldBorder),
                modifier = Modifier.testTag("home_wa_reminder_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AlarmOn,
                        contentDescription = "Reminder WA 16:00",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WA 16:00",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spotlight Daily Work Report Card with gradient finish
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onOpenDailyReportTab)
                .testTag("menu_daily_work_report"),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CobaltPrimary, CobaltBright, SinercoTeal)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AssignmentTurnedIn,
                                contentDescription = "Daily Work Report",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Daily Work Report ($dailyReportCount)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Laporan harian personil • Waktu, Hasil & Foto",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFDBEAFE)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = onOpenDailyReportInput,
                        color = Color.White,
                        shape = RoundedCornerShape(50),
                        shadowElevation = 2.dp,
                        modifier = Modifier.testTag("menu_daily_report_add_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Isi Daily Work Report",
                                tint = CobaltPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Isi",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = CobaltPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Polished Elevated Tile Grid
        Surface(
            color = Color.White.copy(alpha = 0.93f),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.9f)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)) {
                shortcuts.chunked(4).forEachIndexed { rowIdx, rowItems ->
                    if (rowIdx > 0) Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowItems.forEach { item ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(onClick = item.onClick)
                                    .padding(vertical = 4.dp)
                                    .testTag(item.tag)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(15.dp))
                                        .background(item.containerColor)
                                        .border(
                                            1.dp,
                                            item.iconTint.copy(alpha = 0.18f),
                                            RoundedCornerShape(15.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label.replace("\n", " "),
                                        tint = item.iconTint,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
