package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.EiSeedDefaults
import com.example.data.model.ScmJob
import com.example.data.model.ScmJobCategory
import com.example.data.model.ScmJobStatus
import com.example.data.model.TeamMember
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
fun ScmJobCard(
    scmJob: ScmJob,
    onCycleStatus: () -> Unit,
    onOpenPhotoSheet: () -> Unit,
    onEditScmJob: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = ScmJobCategory.fromLabel(scmJob.category)
    val status = ScmJobStatus.fromLabel(scmJob.status)

    val (catBg, catFg, catBorder) = when (category) {
        ScmJobCategory.PURCHASE_REQUEST -> Triple(CobaltLight, CobaltPrimary, CobaltContainer)
        ScmJobCategory.PENGIRIMAN_BARANG -> Triple(EmeraldSurface, EmeraldSuccess, EmeraldBorder)
        ScmJobCategory.PENERIMAAN_MATERIAL -> Triple(PurpleSurface, PurplePdf, PurpleBorder)
        ScmJobCategory.WAREHOUSE_LOGISTIK -> Triple(AmberSurface, AmberWarning, AmberBorder)
        ScmJobCategory.MOB -> Triple(CyanSurface, CyanAccent, CyanAccent.copy(alpha = 0.35f))
        ScmJobCategory.DEMOB -> Triple(RoseSurface, RoseAlert, RoseBorder)
        ScmJobCategory.PLAN -> Triple(IndigoSurface, IndigoAccent, IndigoAccent.copy(alpha = 0.35f))
        ScmJobCategory.FORECAST -> Triple(AmberSurface, AmberWarning, AmberBorder)
        ScmJobCategory.MEETING -> Triple(SlateSurfaceVariant, Color(0xFF2B5B9E), SlateBorder)
    }

    val (statusBg, statusFg) = when (status) {
        ScmJobStatus.SELESAI -> EmeraldSurface to EmeraldSuccess
        ScmJobStatus.PROSES -> CobaltLight to CobaltPrimary
        ScmJobStatus.OPEN -> SlateSurfaceVariant to Color(0xFF4B6488)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenPhotoSheet)
            .testTag("scm_job_card_${scmJob.jobCode}"),
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
                    Surface(
                        color = CobaltLight,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, CobaltContainer),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onEditScmJob)
                            .testTag("scm_edit_${scmJob.jobCode}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit job SCM ${scmJob.jobCode}",
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

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onCycleStatus)
                            .testTag("scm_status_${scmJob.jobCode}")
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
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Ubah status SCM",
                                tint = statusFg,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scmJob.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (scmJob.referenceNumber.isNotBlank() || scmJob.destinationOrVendor.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (scmJob.referenceNumber.isNotBlank()) {
                        Surface(
                            color = SlateSurfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ReceiptLong,
                                    contentDescription = null,
                                    tint = CobaltPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = scmJob.referenceNumber,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    if (scmJob.destinationOrVendor.isNotBlank()) {
                        Surface(
                            color = SlateSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (category == ScmJobCategory.PENGIRIMAN_BARANG) {
                                        Icons.Rounded.LocalShipping
                                    } else {
                                        Icons.Rounded.Inventory2
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = scmJob.destinationOrVendor,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = scmJob.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Before & After Photo Documentation Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkOrderMiniPhotoBadge(
                    label = "Foto Sebelum",
                    photoUris = scmJob.resolvedBeforePhotos(),
                    onClick = onOpenPhotoSheet,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("scm_before_photo_chip_${scmJob.jobCode}")
                )
                WorkOrderMiniPhotoBadge(
                    label = "Foto Sesudah",
                    photoUris = scmJob.resolvedAfterPhotos(),
                    onClick = onOpenPhotoSheet,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("scm_after_photo_chip_${scmJob.jobCode}")
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
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = EiSeedDefaults.sanitizePersonNameOnly(scmJob.picName),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${scmJob.jobCode} • ${scmJob.jobDate}",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScmJobInputSheet(
    initialCategory: ScmJobCategory,
    defaultPicName: String,
    teamMembers: List<TeamMember> = emptyList(),
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        category: ScmJobCategory,
        title: String,
        referenceNumber: String,
        picName: String,
        destinationOrVendor: String,
        description: String,
        status: ScmJobStatus,
        beforePhotoUris: List<String>,
        afterPhotoUris: List<String>
    ) -> Unit,
    existingScmJob: ScmJob? = null,
    onDelete: (() -> Unit)? = null
) {
    val isEditMode = existingScmJob != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val picOptions = remember(teamMembers, existingScmJob) {
        val fromTeam = teamMembers
            .map { EiSeedDefaults.sanitizePersonNameOnly(it.name) }
            .filter { it.isNotBlank() }
            .distinct()
        val existingClean = existingScmJob?.picName
            ?.let { EiSeedDefaults.sanitizePersonNameOnly(it) }
            ?.takeIf { it.isNotBlank() }
        val base = fromTeam.ifEmpty { EiSeedDefaults.technicianOptions.map { it.first } }
        if (existingClean != null && base.none { it.equals(existingClean, ignoreCase = true) }) {
            listOf(existingClean) + base
        } else {
            base
        }
    }

    var selectedCategory by remember(existingScmJob, initialCategory) {
        mutableStateOf(existingScmJob?.let { ScmJobCategory.fromLabel(it.category) } ?: initialCategory)
    }
    var title by remember(existingScmJob) {
        mutableStateOf(existingScmJob?.title ?: "")
    }
    var referenceNumber by remember(existingScmJob) {
        mutableStateOf(existingScmJob?.referenceNumber ?: "")
    }
    var picName by remember(existingScmJob, defaultPicName, picOptions) {
        val existingClean = existingScmJob?.picName
            ?.let { EiSeedDefaults.sanitizePersonNameOnly(it) }
            ?.takeIf { it.isNotBlank() }
        val cleanDefault = EiSeedDefaults.sanitizePersonNameOnly(defaultPicName)
        val matchedDefault = picOptions.firstOrNull { it.equals(cleanDefault, ignoreCase = true) }
        mutableStateOf(existingClean ?: matchedDefault ?: picOptions.first())
    }
    var picExpanded by remember { mutableStateOf(false) }
    var destinationOrVendor by remember(existingScmJob) {
        mutableStateOf(existingScmJob?.destinationOrVendor ?: "")
    }
    var description by remember(existingScmJob) {
        mutableStateOf(existingScmJob?.description ?: "")
    }
    var selectedStatus by remember(existingScmJob) {
        mutableStateOf(existingScmJob?.let { ScmJobStatus.fromLabel(it.status) } ?: ScmJobStatus.OPEN)
    }
    var beforePhotoUris by remember(existingScmJob) {
        mutableStateOf(existingScmJob?.resolvedBeforePhotos() ?: emptyList())
    }
    var afterPhotoUris by remember(existingScmJob) {
        mutableStateOf(existingScmJob?.resolvedAfterPhotos() ?: emptyList())
    }
    var previewPhoto by remember { mutableStateOf<Pair<String, String>?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val beforeActions = rememberMultiPhotoPickerState(
        prefix = "scm_before",
        currentCountProvider = { beforePhotoUris.size },
        onPhotosAdded = { added ->
            beforePhotoUris = (beforePhotoUris + added)
                .distinct()
                .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
        }
    )
    val afterActions = rememberMultiPhotoPickerState(
        prefix = "scm_after",
        currentCountProvider = { afterPhotoUris.size },
        onPhotosAdded = { added ->
            afterPhotoUris = (afterPhotoUris + added)
                .distinct()
                .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
        }
    )

    val (badgeBg, badgeFg) = when (selectedCategory) {
        ScmJobCategory.PURCHASE_REQUEST -> CobaltLight to CobaltPrimary
        ScmJobCategory.PENGIRIMAN_BARANG -> EmeraldSurface to EmeraldSuccess
        ScmJobCategory.PENERIMAAN_MATERIAL -> PurpleSurface to PurplePdf
        ScmJobCategory.WAREHOUSE_LOGISTIK -> AmberSurface to AmberWarning
        ScmJobCategory.MOB -> CyanSurface to CyanAccent
        ScmJobCategory.DEMOB -> RoseSurface to RoseAlert
        ScmJobCategory.PLAN -> IndigoSurface to IndigoAccent
        ScmJobCategory.FORECAST -> AmberSurface to AmberWarning
        ScmJobCategory.MEETING -> SlateSurfaceVariant to Color(0xFF2B5B9E)
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
                        text = if (isEditMode) "Edit ${existingScmJob?.jobCode}" else "Tambah Job SCM",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Catat Purchase Request, pengiriman barang, penerimaan material, atau stok warehouse.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SlateSurfaceVariant)
                        .testTag("close_scm_modal")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup form SCM"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Kategori SCM",
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
                ScmJobCategory.entries.forEach { catOption ->
                    FilterChip(
                        selected = selectedCategory == catOption,
                        onClick = { selectedCategory = catOption },
                        label = { Text(catOption.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CobaltPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("scm_category_option_${catOption.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it.take(140)
                    validationError = null
                },
                label = { Text("Judul Job / Nama Barang") },
                placeholder = { Text("Contoh: PR Kabel Instrument / Pengiriman Panel MCC") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scm_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = referenceNumber,
                onValueChange = { referenceNumber = it.take(80) },
                label = { Text("No. PR / PO / Surat Jalan (Opsional)") },
                placeholder = { Text("Contoh: PR-2026-045 / SJ-2026-112") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scm_ref_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = picExpanded,
                onExpandedChange = { picExpanded = !picExpanded }
            ) {
                OutlinedTextField(
                    value = picName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("PIC (Dari List Team)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = picExpanded) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("scm_pic_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = picExpanded,
                    onDismissRequest = { picExpanded = false }
                ) {
                    picOptions.forEach { nameOnly ->
                        DropdownMenuItem(
                            text = { Text(nameOnly) },
                            onClick = {
                                picName = nameOnly
                                picExpanded = false
                                validationError = null
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = destinationOrVendor,
                onValueChange = { destinationOrVendor = it.take(120) },
                label = { Text("Vendor / Tujuan Pengiriman / Gudang") },
                placeholder = { Text("Contoh: PT Sinerco / Site Plant B / Gudang Utama") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scm_dest_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it.take(1000)
                    validationError = null
                },
                label = { Text("Rincian Barang & Catatan") },
                placeholder = { Text("Tuliskan item, jumlah qty, estimasi tiba, atau catatan pengadaan...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scm_desc_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Status Pekerjaan SCM",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScmJobStatus.entries.forEach { statusOption ->
                    FilterChip(
                        selected = selectedStatus == statusOption,
                        onClick = { selectedStatus = statusOption },
                        label = { Text(statusOption.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CobaltPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("scm_status_option_${statusOption.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Dokumentasi Foto SCM (Maks. 4 Sebelum & 4 Sesudah)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            WorkOrderMultiPhotoSlot(
                title = "Foto Sebelum (Pengiriman / Proses)",
                subtitle = "Foto langsung atau unggah dari galeri (maks. 4 foto)",
                photoUris = beforePhotoUris,
                testTagPrefix = "scm_input_before_photo",
                actions = beforeActions,
                onRemovePhotoAt = { idx ->
                    if (idx in beforePhotoUris.indices) {
                        beforePhotoUris = beforePhotoUris.toMutableList().apply { removeAt(idx) }
                    }
                },
                onPreviewPhoto = { idx, uri ->
                    previewPhoto = "Foto Sebelum SCM (${idx + 1}/4)" to uri
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            WorkOrderMultiPhotoSlot(
                title = "Foto Sesudah (Selesai / Diterima)",
                subtitle = "Foto langsung atau unggah dari galeri (maks. 4 foto)",
                photoUris = afterPhotoUris,
                testTagPrefix = "scm_input_after_photo",
                actions = afterActions,
                onRemovePhotoAt = { idx ->
                    if (idx in afterPhotoUris.indices) {
                        afterPhotoUris = afterPhotoUris.toMutableList().apply { removeAt(idx) }
                    }
                },
                onPreviewPhoto = { idx, uri ->
                    previewPhoto = "Foto Sesudah SCM (${idx + 1}/4)" to uri
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
                        title.isBlank() -> validationError = "Judul job / nama barang wajib diisi."
                        picName.isBlank() -> validationError = "PIC / Pemohon wajib diisi."
                        description.isBlank() -> validationError = "Rincian barang & catatan wajib diisi."
                        else -> onSubmit(
                            selectedCategory,
                            title.trim(),
                            referenceNumber.trim(),
                            picName.trim(),
                            destinationOrVendor.trim(),
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
                    .testTag("scm_submit_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Simpan Perubahan SCM" else "Simpan Job SCM",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isEditMode && onDelete != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDelete,
                    enabled = !isSubmitting,
                    border = BorderStroke(1.dp, RoseAlert.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("scm_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Hapus job SCM",
                        tint = RoseAlert,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hapus Job SCM", color = RoseAlert, fontWeight = FontWeight.SemiBold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScmJobPhotoSheet(
    scmJob: ScmJob,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onPhotosUpdated: (isBeforePhoto: Boolean, photoUris: List<String>) -> Unit,
    onCycleStatus: () -> Unit,
    onEditScmJob: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var previewPhoto by remember { mutableStateOf<Pair<String, String>?>(null) }
    val beforePhotos = remember(scmJob) { scmJob.resolvedBeforePhotos() }
    val afterPhotos = remember(scmJob) { scmJob.resolvedAfterPhotos() }

    val beforeActions = rememberMultiPhotoPickerState(
        prefix = "scm_before_${scmJob.jobCode}",
        currentCountProvider = { beforePhotos.size },
        onPhotosAdded = { added ->
            val updated = (beforePhotos + added)
                .distinct()
                .take(EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
            onPhotosUpdated(true, updated)
        }
    )

    val afterActions = rememberMultiPhotoPickerState(
        prefix = "scm_after_${scmJob.jobCode}",
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
                        text = "Dokumentasi Foto SCM",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${scmJob.jobCode} • ${scmJob.title}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CobaltPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SlateSurfaceVariant)
                        .testTag("close_scm_photo_modal")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup dokumentasi foto SCM"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${scmJob.category} • ${EiSeedDefaults.sanitizePersonNameOnly(scmJob.picName)}",
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
                    modifier = Modifier.testTag("scm_photo_sheet_cycle_status")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ScmJobStatus.fromLabel(scmJob.status).label,
                            style = MaterialTheme.typography.labelSmall,
                            color = CobaltPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Ubah status SCM",
                            tint = CobaltPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            WorkOrderMultiPhotoSlot(
                title = "1. Foto Sebelum (Pengiriman / Proses)",
                subtitle = "Foto langsung dengan kamera atau unggah dari galeri/file (maks. 4 foto)",
                photoUris = beforePhotos,
                testTagPrefix = "scm_detail_before_photo",
                actions = beforeActions,
                onRemovePhotoAt = { idx ->
                    if (idx in beforePhotos.indices) {
                        val updated = beforePhotos.toMutableList().apply { removeAt(idx) }
                        onPhotosUpdated(true, updated)
                    }
                },
                onPreviewPhoto = { idx, uri ->
                    previewPhoto = "Foto Sebelum #${idx + 1} (${scmJob.jobCode})" to uri
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            WorkOrderMultiPhotoSlot(
                title = "2. Foto Sesudah (Selesai / Diterima)",
                subtitle = "Foto langsung dengan kamera atau unggah dari galeri/file (maks. 4 foto)",
                photoUris = afterPhotos,
                testTagPrefix = "scm_detail_after_photo",
                actions = afterActions,
                onRemovePhotoAt = { idx ->
                    if (idx in afterPhotos.indices) {
                        val updated = afterPhotos.toMutableList().apply { removeAt(idx) }
                        onPhotosUpdated(false, updated)
                    }
                },
                onPreviewPhoto = { idx, uri ->
                    previewPhoto = "Foto Sesudah #${idx + 1} (${scmJob.jobCode})" to uri
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onEditScmJob,
                enabled = !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CobaltPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("scm_photo_sheet_edit_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = CobaltPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Detail, PIC & Status Job SCM",
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
