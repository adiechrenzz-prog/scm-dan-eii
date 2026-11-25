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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.EiSeedDefaults
import com.example.data.model.TeamMember
import com.example.data.model.WorkPositionStatus
import com.example.data.model.WorkStatusLog
import com.example.ui.theme.CobaltBright
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SinercoTeal
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant

private data class ResolvedTeamPosition(
    val id: String,
    val name: String,
    val role: String,
    val status: WorkPositionStatus,
    val locationNote: String,
    val lastTime: String
)

@Composable
fun WorkPositionsTabContent(
    teamMembers: List<TeamMember>,
    workStatusLogs: List<WorkStatusLog>,
    onUpdateStatusClick: (personName: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatusFilter by remember { mutableStateOf<WorkPositionStatus?>(null) }

    val resolvedPositions = remember(teamMembers, workStatusLogs) {
        val latestLogByPerson = mutableMapOf<String, WorkStatusLog>()
        workStatusLogs.forEach { log ->
            val clean = EiSeedDefaults.sanitizePersonNameOnly(log.personName).lowercase()
            if (clean.isNotBlank() && !latestLogByPerson.containsKey(clean)) {
                latestLogByPerson[clean] = log
            }
        }

        val seenKeys = mutableSetOf<String>()
        val list = mutableListOf<ResolvedTeamPosition>()

        teamMembers.forEach { member ->
            val cleanName = EiSeedDefaults.sanitizePersonNameOnly(member.name).ifBlank { member.name }
            val key = cleanName.lowercase()
            seenKeys.add(key)
            val log = latestLogByPerson[key]
            val resolvedStatus = WorkPositionStatus.fromLabel(
                log?.workStatus ?: member.status
            )
            val resolvedNote = log?.destinationNote?.takeIf { it.isNotBlank() }
                ?: member.workNote.takeIf { it.isNotBlank() }
                ?: ""
            val resolvedTime = log?.timeLabel?.takeIf { it.isNotBlank() }
                ?: member.lastTime.takeIf { it.isNotBlank() && it != "-" }
                ?: "-"

            list.add(
                ResolvedTeamPosition(
                    id = member.id.ifEmpty { cleanName },
                    name = cleanName,
                    role = member.role,
                    status = resolvedStatus,
                    locationNote = resolvedNote,
                    lastTime = resolvedTime
                )
            )
        }

        // Also include any personnel who submitted a WorkStatusLog even if not in team_members yet
        latestLogByPerson.forEach { (key, log) ->
            if (!seenKeys.contains(key)) {
                val cleanName = EiSeedDefaults.sanitizePersonNameOnly(log.personName).ifBlank { log.personName }
                list.add(
                    ResolvedTeamPosition(
                        id = "log_$key",
                        name = cleanName,
                        role = "Anggota Team",
                        status = WorkPositionStatus.fromLabel(log.workStatus),
                        locationNote = log.destinationNote,
                        lastTime = log.timeLabel.ifBlank { "-" }
                    )
                )
            }
        }

        list
    }

    val filteredPositions = remember(resolvedPositions, selectedStatusFilter) {
        val filter = selectedStatusFilter
        if (filter == null) resolvedPositions else resolvedPositions.filter { it.status == filter }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
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
                            text = "Posisi Kerja Team",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${resolvedPositions.size} Personel • Pantau posisi & tujuan seluruh team",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFDBEAFE)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = { onUpdateStatusClick(null) },
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("positions_tab_update_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Isi Status",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("Semua (${resolvedPositions.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CobaltPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("posisi_filter_all")
                    )
                }
                items(WorkPositionStatus.entries) { statusOption ->
                    val count = resolvedPositions.count { it.status == statusOption }
                    FilterChip(
                        selected = selectedStatusFilter == statusOption,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == statusOption) null else statusOption
                        },
                        label = { Text("${statusOption.label} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CobaltPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("posisi_filter_${statusOption.name}")
                    )
                }
            }
        }

        if (filteredPositions.isEmpty()) {
            item {
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
                        Text(
                            text = "Tidak ada personel pada status ini",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pilih filter Semua untuk melihat seluruh anggota team.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredPositions, key = { it.id }) { person ->
                val roleCode = EiSeedDefaults.badgeCodeForRole(person.role)
                val badgeCircleBg = when (roleCode) {
                    "SM" -> IndigoAccent
                    "AM", "MGR", "SPV" -> CobaltPrimary
                    "ADM" -> EmeraldSuccess
                    else -> Color(0xFF2B5B9E)
                }
                val (statusBg, statusFg, statusIcon) = workStatusVisualStyle(person.status)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clickable { onUpdateStatusClick(person.name) }
                        .testTag("posisi_member_card_${person.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.93f)),
                    border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(badgeCircleBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = roleCode,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = person.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = person.role,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                color = statusBg,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, statusFg.copy(alpha = 0.28f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = person.status.label,
                                        tint = statusFg,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = person.status.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusFg
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = if (person.locationNote.isNotBlank()) statusBg.copy(alpha = 0.55f) else SlateSurfaceVariant.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Place,
                                        contentDescription = null,
                                        tint = if (person.locationNote.isNotBlank()) statusFg else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val displayLocation = if (person.locationNote.isNotBlank()) {
                                        "Sedang di / Tujuan: ${person.locationNote}"
                                    } else {
                                        "Lokasi saat ini: ${person.status.label}"
                                    }
                                    Text(
                                        text = displayLocation,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (person.locationNote.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (person.locationNote.isNotBlank()) statusFg else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (person.lastTime != "-") {
                                        Text(
                                            text = person.lastTime,
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Ubah status ${person.name}",
                                        tint = CobaltPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (workStatusLogs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = CobaltPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Riwayat Update Posisi Terbaru (${workStatusLogs.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            items(workStatusLogs.take(10), key = { it.id.ifEmpty { "${it.personName}_${it.timeLabel}" } }) { log ->
                val statusEnum = WorkPositionStatus.fromLabel(log.workStatus)
                val (badgeBg, badgeFg, icon) = workStatusVisualStyle(statusEnum)
                Surface(
                    color = SlateSurfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(badgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = statusEnum.label,
                                    tint = badgeFg,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = EiSeedDefaults.sanitizePersonNameOnly(log.personName),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (log.destinationNote.isNotBlank()) {
                                    Text(
                                        text = "Tujuan: ${log.destinationNote}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(color = badgeBg, shape = RoundedCornerShape(50)) {
                                Text(
                                    text = statusEnum.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = badgeFg,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (log.timeLabel.isNotBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = log.timeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
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
