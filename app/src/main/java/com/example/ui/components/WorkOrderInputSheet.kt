package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.EiSeedDefaults
import com.example.data.model.TeamMember
import com.example.data.model.WorkOrder
import com.example.data.model.WorkOrderCategory
import com.example.data.model.WorkOrderStatus
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoSurface
import com.example.ui.theme.PurplePdf
import com.example.ui.theme.PurpleSurface
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.RoseSurface
import com.example.ui.theme.SlateSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderInputSheet(
    category: WorkOrderCategory,
    teamMembers: List<TeamMember>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        category: WorkOrderCategory,
        equipmentTag: String,
        technician: String,
        description: String,
        status: WorkOrderStatus,
        beforePhotoUris: List<String>,
        afterPhotoUris: List<String>
    ) -> Unit,
    existingWorkOrder: WorkOrder? = null
) {
    val isEditMode = existingWorkOrder != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember(existingWorkOrder, category) {
        mutableStateOf(existingWorkOrder?.let { WorkOrderCategory.fromLabel(it.category) } ?: category)
    }
    var equipmentTag by remember(existingWorkOrder) {
        mutableStateOf(existingWorkOrder?.equipmentTag ?: "")
    }
    var description by remember(existingWorkOrder) {
        mutableStateOf(existingWorkOrder?.description ?: "")
    }

    val executorNames = remember(teamMembers) {
        val fromTeam = teamMembers
            .map { EiSeedDefaults.sanitizePersonNameOnly(it.name) }
            .filter { it.isNotBlank() }
            .distinct()
        fromTeam.ifEmpty { EiSeedDefaults.technicianOptions.map { it.first } }
    }

    var selectedTechName by remember(existingWorkOrder, executorNames) {
        val currentTech = existingWorkOrder?.technician
            ?.let { EiSeedDefaults.sanitizePersonNameOnly(it) }
            .orEmpty()
        val initial = if (currentTech.isNotBlank() && executorNames.contains(currentTech)) {
            currentTech
        } else if (currentTech.isNotBlank()) {
            currentTech
        } else {
            executorNames.first()
        }
        mutableStateOf(initial)
    }
    var selectedStatus by remember(existingWorkOrder) {
        mutableStateOf(existingWorkOrder?.let { WorkOrderStatus.fromLabel(it.status) } ?: WorkOrderStatus.OPEN)
    }
    var techExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    var beforePhotoUris by remember(existingWorkOrder) {
        mutableStateOf(existingWorkOrder?.resolvedBeforePhotos() ?: emptyList())
    }
    var afterPhotoUris by remember(existingWorkOrder) {
        mutableStateOf(existingWorkOrder?.resolvedAfterPhotos() ?: emptyList())
    }
    var previewPhoto by remember { mutableStateOf<Pair<String, String>?>(null) }

    val beforeActions = rememberMultiPhotoPickerState(
        prefix = "wo_before",
        currentCountProvider = { beforePhotoUris.size },
        onPhotosAdded = { added ->
            beforePhotoUris = (beforePhotoUris + added)
                .distinct()
                .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
        }
    )
    val afterActions = rememberMultiPhotoPickerState(
        prefix = "wo_after",
        currentCountProvider = { afterPhotoUris.size },
        onPhotosAdded = { added ->
            afterPhotoUris = (afterPhotoUris + added)
                .distinct()
                .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
        }
    )

    val (badgeBg, badgeFg) = when (selectedCategory) {
        WorkOrderCategory.PM -> CobaltLight to CobaltPrimary
        WorkOrderCategory.CM -> AmberSurface to AmberWarning
        WorkOrderCategory.BREAKDOWN -> RoseSurface to RoseAlert
        WorkOrderCategory.PERSIAPAN -> CyanSurface to CyanAccent
        WorkOrderCategory.PEMBELIAN -> PurpleSurface to PurplePdf
        WorkOrderCategory.FUNCTION_TEST -> EmeraldSurface to EmeraldSuccess
        WorkOrderCategory.INSTALASI -> IndigoSurface to IndigoAccent
        WorkOrderCategory.MEETING -> SlateSurfaceVariant to Color(0xFF2B5B9E)
    }

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
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(color = badgeBg, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = selectedCategory.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isEditMode) "Edit ${existingWorkOrder?.woCode}" else "Input ${selectedCategory.label}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isEditMode) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pekerjaan Open maupun Done tetap dapat diedit oleh team",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SlateSurfaceVariant)
                        .testTag("close_wo_modal")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup form"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Kategori Pekerjaan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkOrderCategory.entries.forEach { catOption ->
                    FilterChip(
                        selected = selectedCategory == catOption,
                        onClick = { selectedCategory = catOption },
                        label = { Text(catOption.shortLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CobaltPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("wo_category_option_${catOption.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = equipmentTag,
                onValueChange = {
                    equipmentTag = it.take(120)
                    validationError = null
                },
                label = { Text("Tag Number / Subjek Pekerjaan") },
                placeholder = { Text("Contoh: PT-102 / Motor Pump A / PLC-01") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wo_tag_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = techExpanded,
                onExpandedChange = { techExpanded = !techExpanded }
            ) {
                OutlinedTextField(
                    value = selectedTechName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("PIC") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = techExpanded) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("wo_tech_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = techExpanded,
                    onDismissRequest = { techExpanded = false }
                ) {
                    executorNames.forEach { nameOnly ->
                        DropdownMenuItem(
                            text = { Text(nameOnly) },
                            onClick = {
                                selectedTechName = nameOnly
                                techExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it.take(1000)
                    validationError = null
                },
                label = { Text("Deskripsi Temuan & Tindakan") },
                placeholder = { Text("Jelaskan kondisi alat, parameter, dan tindakan pelaksanaan...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wo_desc_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Status Pekerjaan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkOrderStatus.entries.forEach { statusOption ->
                    val selected = selectedStatus == statusOption
                    FilterChip(
                        selected = selected,
                        onClick = { selectedStatus = statusOption },
                        label = { Text(statusOption.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CobaltPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("wo_status_option_${statusOption.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Dokumentasi Foto Pekerjaan (Maks. 4 Sebelum & 4 Sesudah)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            WorkOrderMultiPhotoSlot(
                title = "Foto Sebelum Dikerjakan",
                subtitle = "Foto langsung atau unggah dari galeri (maks. 4 foto, terlihat semua team)",
                photoUris = beforePhotoUris,
                testTagPrefix = "input_before_photo",
                actions = beforeActions,
                onRemovePhotoAt = { idx ->
                    if (idx in beforePhotoUris.indices) {
                        beforePhotoUris = beforePhotoUris.toMutableList().apply { removeAt(idx) }
                    }
                },
                onPreviewPhoto = { idx, uri ->
                    previewPhoto = "Foto Sebelum (${idx + 1}/4)" to uri
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            WorkOrderMultiPhotoSlot(
                title = "Foto Sesudah Dikerjakan",
                subtitle = "Foto langsung atau unggah dari galeri (maks. 4 foto, terlihat semua team)",
                photoUris = afterPhotoUris,
                testTagPrefix = "input_after_photo",
                actions = afterActions,
                onRemovePhotoAt = { idx ->
                    if (idx in afterPhotoUris.indices) {
                        afterPhotoUris = afterPhotoUris.toMutableList().apply { removeAt(idx) }
                    }
                },
                onPreviewPhoto = { idx, uri ->
                    previewPhoto = "Foto Sesudah (${idx + 1}/4)" to uri
                }
            )

            if (validationError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = validationError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = RoseAlert
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    when {
                        equipmentTag.isBlank() -> validationError = "Tag Number / Subjek wajib diisi."
                        description.isBlank() -> validationError = "Deskripsi temuan & tindakan wajib diisi."
                        else -> onSubmit(
                            selectedCategory,
                            equipmentTag.trim(),
                            selectedTechName.trim(),
                            description.trim(),
                            selectedStatus,
                            beforePhotoUris,
                            afterPhotoUris
                        )
                    }
                },
                enabled = !isSubmitting && !beforeActions.isUploading && !afterActions.isUploading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CobaltPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("wo_submit_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Simpan Perubahan" else "Simpan Laporan Kerja",
                        fontWeight = FontWeight.Bold
                    )
                }
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
