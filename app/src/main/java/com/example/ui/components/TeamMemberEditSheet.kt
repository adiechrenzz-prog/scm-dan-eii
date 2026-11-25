package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.EiSeedDefaults
import com.example.data.model.TeamMember
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMemberEditSheet(
    existingMember: TeamMember?,
    jobTitleOptions: List<String> = EiSeedDefaults.jobTitleOptions,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onAddCustomJobTitle: (String) -> Unit = {},
    onSave: (name: String, role: String, specialty: String, whatsappNumber: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isEditMode = existingMember != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val availableRoles = remember(jobTitleOptions, existingMember) {
        val base = jobTitleOptions.ifEmpty { EiSeedDefaults.jobTitleOptions }
        val existingRole = existingMember?.role?.trim().orEmpty()
        if (existingRole.isNotEmpty() && base.none { it.equals(existingRole, ignoreCase = true) }) {
            base + existingRole
        } else {
            base
        }
    }

    var name by remember(existingMember) {
        mutableStateOf(existingMember?.name ?: "")
    }
    var selectedRole by remember(existingMember) {
        mutableStateOf(
            existingMember?.role?.takeIf { it.isNotBlank() }
                ?: availableRoles.first()
        )
    }
    var specialty by remember(existingMember) {
        mutableStateOf(existingMember?.specialty ?: "")
    }
    var whatsappNumber by remember(existingMember) {
        mutableStateOf(existingMember?.whatsappNumber ?: "")
    }
    var roleExpanded by remember { mutableStateOf(false) }
    var showCustomRoleInput by remember { mutableStateOf(false) }
    var customRoleDraft by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

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
                    Text(
                        text = if (isEditMode) "Edit Anggota Team" else "Tambah Anggota Team",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Daftar nama otomatis tersedia di dropdown PIC & Reminder WhatsApp 16:00.",
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
                        .testTag("close_team_edit_modal")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup editor anggota")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.take(80)
                    validationError = null
                },
                label = { Text("Nama Personel") },
                placeholder = { Text("Contoh: Ahmad Fauzi") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("team_member_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jabatan / Posisi",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = CobaltLight,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CobaltContainer),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            showCustomRoleInput = !showCustomRoleInput
                            validationError = null
                        }
                        .testTag("team_member_add_role_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Tambah jabatan manual",
                            tint = CobaltPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (showCustomRoleInput) "Tutup Input" else "+ Jabatan",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CobaltPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = roleExpanded,
                onExpandedChange = { roleExpanded = !roleExpanded }
            ) {
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pilih Jabatan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("team_member_role_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = roleExpanded,
                    onDismissRequest = { roleExpanded = false }
                ) {
                    availableRoles.forEach { roleOption ->
                        DropdownMenuItem(
                            text = { Text(roleOption) },
                            onClick = {
                                selectedRole = roleOption
                                showCustomRoleInput = false
                                roleExpanded = false
                            }
                        )
                    }
                    HorizontalDivider(color = SlateBorder)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "+ Tambah Jabatan Manual",
                                color = CobaltPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onClick = {
                            roleExpanded = false
                            showCustomRoleInput = true
                        }
                    )
                }
            }

            if (showCustomRoleInput) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customRoleDraft,
                        onValueChange = {
                            customRoleDraft = it.take(80)
                            validationError = null
                        },
                        label = { Text("Jabatan Baru (Manual)") },
                        placeholder = { Text("Contoh: Lead Instrument / QA/QC") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("team_member_custom_role_input")
                    )
                    Button(
                        onClick = {
                            val cleanCustom = customRoleDraft.trim().take(80)
                            if (cleanCustom.isEmpty()) {
                                validationError = "Nama jabatan manual belum diisi."
                            } else {
                                onAddCustomJobTitle(cleanCustom)
                                selectedRole = cleanCustom
                                customRoleDraft = ""
                                showCustomRoleInput = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CobaltPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("team_member_save_custom_role_button")
                    ) {
                        Text("Pakai", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = specialty,
                onValueChange = {
                    specialty = it.take(100)
                    validationError = null
                },
                label = { Text("Keahlian / Divisi") },
                placeholder = { Text("Contoh: Calibration, PLC, Pengadaan, Warehouse") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("team_member_specialty_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = whatsappNumber,
                onValueChange = {
                    whatsappNumber = EiSeedDefaults.normalizeWhatsappDisplay(it)
                    validationError = null
                },
                label = { Text("Nomor WhatsApp") },
                placeholder = { Text("Contoh: 08988319566") },
                supportingText = {
                    Text("Digunakan untuk pengingat & kirim rekap aktivitas harian jam 16:00.")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = "Nomor WhatsApp",
                        tint = EmeraldSuccess
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("team_member_whatsapp_input")
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
                    val typedCustom = customRoleDraft.trim().take(80)
                    val resolvedRole = if (showCustomRoleInput && typedCustom.isNotEmpty()) {
                        onAddCustomJobTitle(typedCustom)
                        typedCustom
                    } else {
                        selectedRole.trim()
                    }
                    if (name.isBlank()) {
                        validationError = "Nama personel wajib diisi."
                    } else if (resolvedRole.isBlank()) {
                        validationError = "Jabatan wajib diisi."
                    } else {
                        onSave(
                            name.trim(),
                            resolvedRole,
                            specialty.trim().ifEmpty { resolvedRole },
                            whatsappNumber.trim()
                        )
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CobaltPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("team_member_save_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Simpan Perubahan" else "Tambah Anggota",
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
                        .testTag("team_member_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Hapus anggota",
                        tint = RoseAlert,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hapus Anggota Team", color = RoseAlert, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
