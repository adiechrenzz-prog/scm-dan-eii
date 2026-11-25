package com.example.ui.components

import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.BriefingNotice
import com.example.data.model.EiSeedDefaults
import com.example.data.model.WorkOrder
import com.example.data.model.WorkOrderCategory
import com.example.data.model.WorkOrderStatus
import com.example.ui.theme.AmberBorder
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanSurface
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoSurface
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.PurplePdf
import com.example.ui.theme.PurpleSurface
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseBorder
import com.example.ui.theme.RoseSurface
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant

@Composable
fun BriefingSliderSection(
    briefings: List<BriefingNotice>,
    onEditBriefing: (BriefingNotice) -> Unit,
    onAddBriefing: () -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayList = briefings.ifEmpty { EiSeedDefaults.briefings }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.briefing_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tersimpan di Aktivitas & ikut ter-export laporan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = CobaltLight,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CobaltContainer),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onAddBriefing)
                        .testTag("add_briefing_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Tambah briefing",
                            tint = CobaltPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Briefing",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CobaltPrimary
                        )
                    }
                }
                TextButton(
                    onClick = onViewAllClick,
                    modifier = Modifier.testTag("briefing_view_all_button")
                ) {
                    Text(
                        text = "Lihat Semua",
                        style = MaterialTheme.typography.labelLarge,
                        color = CobaltPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(displayList, key = { it.id.ifEmpty { "${it.badge}_${it.title}" } }) { notice ->
                BriefingCard(
                    notice = notice,
                    onClick = { onEditBriefing(notice) }
                )
            }
        }
    }
}

@Composable
private fun BriefingCard(
    notice: BriefingNotice,
    onClick: () -> Unit
) {
    val badgeBg = if (notice.isSafetyAlert) RoseSurface else CobaltLight
    val badgeColor = if (notice.isSafetyAlert) RoseAlert else CobaltPrimary
    val accentBorder = if (notice.isSafetyAlert) RoseBorder else CobaltContainer

    Card(
        modifier = Modifier
            .width(296.dp)
            .clickable(onClick = onClick)
            .testTag("briefing_card_${notice.id.ifEmpty { notice.title }}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accentBorder.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(126.dp)
                    .background(badgeColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = notice.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = notice.timeTag,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            color = SlateSurfaceVariant,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onClick)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Edit briefing ${notice.title}",
                                    tint = CobaltPrimary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CobaltPrimary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notice.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun WorkOrderCard(
    workOrder: WorkOrder,
    allowStatusCycle: Boolean,
    onCycleStatus: (() -> Unit)? = null,
    onOpenPhotoSheet: (() -> Unit)? = null,
    onEditWorkOrder: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val category = WorkOrderCategory.fromLabel(workOrder.category)
    val status = WorkOrderStatus.fromLabel(workOrder.status)

    val (catBg, catFg, catBorder) = when (category) {
        WorkOrderCategory.BREAKDOWN -> Triple(RoseSurface, RoseAlert, RoseBorder)
        WorkOrderCategory.CM -> Triple(AmberSurface, AmberWarning, AmberBorder)
        WorkOrderCategory.PM -> Triple(CobaltLight, CobaltPrimary, CobaltContainer)
        WorkOrderCategory.PERSIAPAN -> Triple(CyanSurface, CyanAccent, CyanAccent.copy(alpha = 0.35f))
        WorkOrderCategory.PEMBELIAN -> Triple(PurpleSurface, PurplePdf, PurpleBorder)
        WorkOrderCategory.FUNCTION_TEST -> Triple(EmeraldSurface, EmeraldSuccess, EmeraldBorder)
        WorkOrderCategory.INSTALASI -> Triple(IndigoSurface, IndigoAccent, IndigoAccent.copy(alpha = 0.35f))
        WorkOrderCategory.MEETING -> Triple(SlateSurfaceVariant, Color(0xFF2B5B9E), SlateBorder)
    }

    val (statusBg, statusFg) = when (status) {
        WorkOrderStatus.DONE -> EmeraldSurface to EmeraldSuccess
        WorkOrderStatus.IN_PROGRESS -> CobaltLight to CobaltPrimary
        WorkOrderStatus.OPEN -> SlateSurfaceVariant to Color(0xFF4B6488)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onOpenPhotoSheet != null) {
                    Modifier.clickable(onClick = onOpenPhotoSheet)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.93f)),
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = catBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, catBorder)
                ) {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = catFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onEditWorkOrder != null) {
                        Surface(
                            color = CobaltLight,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, CobaltContainer),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onEditWorkOrder)
                                .testTag("wo_edit_${workOrder.woCode}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Edit pekerjaan ${workOrder.woCode}",
                                    tint = CobaltPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CobaltPrimary
                                )
                            }
                        }
                    }

                    val statusModifier = if (allowStatusCycle && onCycleStatus != null) {
                        Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onCycleStatus)
                            .testTag("wo_status_cycle_${workOrder.woCode}")
                    } else {
                        Modifier.clip(RoundedCornerShape(50))
                    }

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(50),
                        modifier = statusModifier
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = status.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusFg
                            )
                            if (allowStatusCycle) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Ubah status",
                                    tint = statusFg,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = workOrder.equipmentTag,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = workOrder.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Before & After Photo Documentation Strip
            val beforePhotos = workOrder.resolvedBeforePhotos()
            val afterPhotos = workOrder.resolvedAfterPhotos()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkOrderMiniPhotoBadge(
                    label = "Foto Sebelum",
                    photoUris = beforePhotos,
                    onClick = onOpenPhotoSheet,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wo_before_photo_chip_${workOrder.woCode}")
                )
                WorkOrderMiniPhotoBadge(
                    label = "Foto Sesudah",
                    photoUris = afterPhotos,
                    onClick = onOpenPhotoSheet,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wo_after_photo_chip_${workOrder.woCode}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SlateBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Engineering,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = EiSeedDefaults.sanitizePersonNameOnly(workOrder.technician),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${workOrder.woCode} • ${workOrder.reportDate}",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun WorkOrderMiniPhotoBadge(
    label: String,
    photoUris: List<String>,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val firstPhoto = photoUris.firstOrNull()
    val hasPhoto = !firstPhoto.isNullOrBlank()
    val maxPhotos = EiSeedDefaults.MAX_PHOTOS_PER_SECTION
    Surface(
        color = if (hasPhoto) EmeraldSurface else SlateSurfaceVariant.copy(alpha = 0.7f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            if (hasPhoto) EmeraldSuccess.copy(alpha = 0.35f) else SlateBorder
        ),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasPhoto) {
                CloudPhotoImage(
                    photoUri = firstPhoto!!,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$label (${photoUris.size}/$maxPhotos)",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldSuccess,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.AddAPhoto,
                    contentDescription = "Tambah $label",
                    tint = CobaltPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "+ $label (0/$maxPhotos)",
                    style = MaterialTheme.typography.labelSmall,
                    color = CobaltPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
