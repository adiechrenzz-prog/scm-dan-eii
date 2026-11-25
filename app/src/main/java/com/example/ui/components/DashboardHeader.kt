package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EiSeedDefaults
import com.example.data.model.WorkPositionStatus
import com.example.ui.theme.CobaltBright
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SinercoTeal
import com.example.ui.theme.SlateBorder
import java.util.Calendar

@Composable
fun DashboardHeaderWithCards(
    profileDisplayName: String?,
    profileJobTitle: String?,
    currentWorkStatus: WorkPositionStatus,
    currentWorkNote: String,
    activeWoCount: Int,
    onNotificationClick: () -> Unit,
    onWhatsAppReminderClick: () -> Unit = {},
    onSignOutClick: () -> Unit,
    onWorkStatusCardClick: () -> Unit,
    onWorkOrderCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = androidx.compose.runtime.remember { rememberTimeGreeting() }
    val userName = profileDisplayName?.takeIf { it.isNotBlank() } ?: EiSeedDefaults.SUPERVISOR_NAME
    val savedJob = profileJobTitle?.takeIf { it.isNotBlank() } ?: EiSeedDefaults.DEFAULT_JOB_TITLE
    val subtitle = "$savedJob • Single Shift (08:00 - 17:00)"
    val avatarInitials = computeInitials(userName)
    val (statusBg, statusFg, statusIcon) = workStatusVisualStyle(currentWorkStatus)
    val statusDisplayValue = if (currentWorkNote.isNotBlank()) {
        "${currentWorkStatus.label} • $currentWorkNote"
    } else {
        currentWorkStatus.label
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // Executive Deep-Navy to Cobalt Hero Header with subtle precision engineering geometry
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            CobaltPrimary,
                            CobaltBright,
                            SinercoTeal
                        )
                    )
                )
                .drawBehind {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = size.width * 0.42f,
                        center = Offset(size.width * 0.92f, size.height * 0.14f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.045f),
                        radius = size.width * 0.28f,
                        center = Offset(size.width * 0.12f, size.height * 0.88f)
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.06f),
                        start = Offset(0f, size.height * 0.65f),
                        end = Offset(size.width, size.height * 0.22f),
                        strokeWidth = 2f
                    )
                }
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 68.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(11.dp),
                            shadowElevation = 3.dp
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_sinerco),
                                contentDescription = "Logo Sinerco",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(86.dp)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                    .testTag("dashboard_sinerco_logo")
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f))
                        ) {
                            Text(
                                text = "SCM dan EII Team",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        onClick = onWhatsAppReminderClick,
                        color = EmeraldBright.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, EmeraldBright.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("header_wa_reminder_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AlarmOn,
                                contentDescription = "Reminder WA 16:00",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "16:00 WA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFBFDBFE)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFDBEAFE),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box {
                            IconButton(
                                onClick = onNotificationClick,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.14f))
                                    .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                                    .testTag("notification_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = "Notifikasi status tim",
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-6).dp, y = 6.dp)
                                    .clip(CircleShape)
                                    .background(RoseAlert)
                                    .border(1.5.dp, CobaltBright, CircleShape)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.24f))
                                .border(1.5.dp, Color.White.copy(alpha = 0.72f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarInitials,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = onSignOutClick,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                                .testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = "Keluar akun",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Overlapping 2 Elevated Status Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = 34.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusSummaryCard(
                icon = statusIcon,
                iconTint = statusFg,
                iconBg = statusBg,
                label = "Status Posisi Kerja",
                value = statusDisplayValue,
                valueColor = statusFg,
                onClick = onWorkStatusCardClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_work_status_summary")
            )

            StatusSummaryCard(
                icon = Icons.Rounded.Build,
                iconTint = CobaltPrimary,
                iconBg = CobaltLight,
                label = "WO Aktif",
                value = "$activeWoCount Pekerjaan",
                valueColor = CobaltPrimary,
                onClick = onWorkOrderCardClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_work_summary")
            )
        }
    }
}

@Composable
private fun StatusSummaryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
    valueColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.minimumInteractiveComponentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun rememberTimeGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..10 -> "Halo, Selamat Pagi"
        in 11..14 -> "Halo, Selamat Siang"
        in 15..17 -> "Halo, Selamat Sore"
        else -> "Halo, Selamat Malam"
    }
}

private fun computeInitials(name: String): String {
    val words = name.trim()
        .replace("(", " ")
        .replace(")", " ")
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() && it.any(Char::isLetterOrDigit) }
    return when {
        words.isEmpty() -> "EI"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> "${words[0].first()}${words[1].first()}".uppercase()
    }
}
