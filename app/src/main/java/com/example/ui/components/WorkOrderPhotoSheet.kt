package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.EiSeedDefaults
import com.example.data.model.WorkOrder
import com.example.data.model.WorkOrderStatus
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SlateSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderPhotoSheet(
    workOrder: WorkOrder,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onPhotosUpdated: (isBeforePhoto: Boolean, photoUris: List<String>) -> Unit,
    onCycleStatus: () -> Unit,
    onEditWorkOrder: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var previewPhoto by remember { mutableStateOf<Pair<String, String>?>(null) }
    val beforePhotos = remember(workOrder) { workOrder.resolvedBeforePhotos() }
    val afterPhotos = remember(workOrder) { workOrder.resolvedAfterPhotos() }

    val beforeActions = rememberMultiPhotoPickerState(
        prefix = "before_${workOrder.woCode}",
        currentCountProvider = { beforePhotos.size },
        onPhotosAdded = { added ->
            val updated = (beforePhotos + added)
                .distinct()
                .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
            onPhotosUpdated(true, updated)
        }
    )

    val afterActions = rememberMultiPhotoPickerState(
        prefix = "after_${workOrder.woCode}",
        currentCountProvider = { afterPhotos.size },
        onPhotosAdded = { added ->
            val updated = (afterPhotos + added)
                .distinct()
                .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
            onPhotosUpdated(false, updated)
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dokumentasi Foto Pekerjaan",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${workOrder.woCode} • ${workOrder.equipmentTag}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CobaltPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SlateSurfaceVariant)
                        .testTag("close_wo_photo_modal")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup dokumentasi foto"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick status bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${workOrder.category} • ${EiSeedDefaults.sanitizePersonNameOnly(workOrder.technician)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = onCycleStatus,
                    enabled = !isSubmitting,
                    color = CobaltLight,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("photo_sheet_cycle_status")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = WorkOrderStatus.fromLabel(workOrder.status).label,
                            style = MaterialTheme.typography.labelSmall,
                            color = CobaltPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Ubah status",
                            tint = CobaltPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            WorkOrderMultiPhotoSlot(
                title = "1. Foto Sebelum Dikerjakan",
                subtitle = "Foto langsung dengan kamera atau unggah dari galeri/file (maks. 4 foto)",
                photoUris = beforePhotos,
                testTagPrefix = "detail_before_photo",
                actions = beforeActions,
                onRemovePhotoAt = { idx ->
                    if (idx in beforePhotos.indices) {
                        val updated = beforePhotos.toMutableList().apply { removeAt(idx) }
                        onPhotosUpdated(true, updated)
                    }
                },
                onPreviewPhoto = { idx, uri ->
                    previewPhoto = "Foto Sebelum #${idx + 1} (${workOrder.woCode})" to uri
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            WorkOrderMultiPhotoSlot(
                title = "2. Foto Sesudah Dikerjakan",
                subtitle = "Foto langsung dengan kamera atau unggah dari galeri/file (maks. 4 foto)",
                photoUris = afterPhotos,
                testTagPrefix = "detail_after_photo",
                actions = afterActions,
                onRemovePhotoAt = { idx ->
                    if (idx in afterPhotos.indices) {
                        val updated = afterPhotos.toMutableList().apply { removeAt(idx) }
                        onPhotosUpdated(false, updated)
                    }
                },
                onPreviewPhoto = { idx, uri ->
                    previewPhoto = "Foto Sesudah #${idx + 1} (${workOrder.woCode})" to uri
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onEditWorkOrder,
                enabled = !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CobaltPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("photo_sheet_edit_wo_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = CobaltPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Detail, Teknisi & Status Pekerjaan",
                    color = CobaltPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    val activePreview = previewPhoto
    if (activePreview != null) {
        FullScreenPhotoDialog(
            title = activePreview.first,
            photoUri = activePreview.second,
            onDismiss = { previewPhoto = null }
        )
    }
}
