package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.LruCache
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.EiSeedDefaults
import com.example.ui.theme.CobaltLight
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

const val FB_PHOTO_SCHEME = "fbphoto://"

object CloudPhotoCache {
    private val memoryCache = object : LruCache<String, ByteArray>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ByteArray): Int = value.size
    }

    fun getMemory(photoId: String): ByteArray? = memoryCache.get(photoId)

    fun put(context: Context, photoId: String, bytes: ByteArray) {
        memoryCache.put(photoId, bytes)
        runCatching {
            val dir = File(context.cacheDir, "fb_photos").apply { mkdirs() }
            File(dir, "$photoId.jpg").writeBytes(bytes)
        }
    }

    fun getDisk(context: Context, photoId: String): ByteArray? {
        val file = File(File(context.cacheDir, "fb_photos"), "$photoId.jpg")
        return if (file.exists() && file.length() > 0L) {
            runCatching { file.readBytes() }.getOrNull()?.also { bytes ->
                memoryCache.put(photoId, bytes)
            }
        } else {
            null
        }
    }
}

object WorkOrderPhotoStorage {
    private const val MAX_PHOTO_DIMENSION_PX = 960
    private const val MAX_BASE64_CHARS = 700_000

    fun createCaptureDestination(context: Context, prefix: String): Pair<Uri, String> {
        val dir = File(context.filesDir, "work_photos").apply { mkdirs() }
        val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return contentUri to Uri.fromFile(file).toString()
    }

    suspend fun copyPickedImage(
        context: Context,
        sourceUri: Uri,
        prefix: String
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "work_photos").apply { mkdirs() }
            val destFile = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null
            Uri.fromFile(destFile).toString()
        }.getOrNull()
    }

    suspend fun uploadPhotoToFirebase(
        context: Context,
        sourceUri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uid = Firebase.auth.currentUser?.uid
                ?: throw IllegalStateException("Silakan masuk terlebih dahulu untuk mengunggah foto ke Firebase.")
            val compressedBytes = compressUriToJpegBytes(context, sourceUri)
                ?: throw IllegalStateException("Gagal membaca atau memproses berkas foto.")
            val base64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
            require(base64.length in 8..MAX_BASE64_CHARS) {
                "Ukuran foto terlalu besar untuk diunggah."
            }

            val photoId = "photo_${UUID.randomUUID().toString().replace("-", "").take(18)}"
            val db = FirebaseFirestore.getInstance(
                context.applicationContext.getString(R.string.firestore_database_id)
            )
            val docRef = db.collection("workspaces")
                .document(EiSeedDefaults.SHARED_WORKSPACE_ID)
                .collection("photos")
                .document(photoId)

            docRef.set(
                mapOf(
                    "workspaceId" to EiSeedDefaults.SHARED_WORKSPACE_ID,
                    "userId" to uid,
                    "contentBase64" to base64,
                    "mimeType" to "image/jpeg",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            CloudPhotoCache.put(context.applicationContext, photoId, compressedBytes)

            // Remove temporary capture file if inside app filesDir/work_photos
            if (sourceUri.scheme == "file") {
                sourceUri.path?.let { path ->
                    val file = File(path)
                    if (file.parentFile?.name == "work_photos") {
                        file.delete()
                    }
                }
            }

            Result.success("$FB_PHOTO_SCHEME$photoId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCloudPhotoBytes(
        context: Context,
        fbPhotoUri: String
    ): ByteArray? = withContext(Dispatchers.IO) {
        if (!fbPhotoUri.startsWith(FB_PHOTO_SCHEME)) return@withContext null
        val photoId = fbPhotoUri.removePrefix(FB_PHOTO_SCHEME).trim()
        if (photoId.isEmpty()) return@withContext null

        CloudPhotoCache.getMemory(photoId)?.let { return@withContext it }
        CloudPhotoCache.getDisk(context.applicationContext, photoId)?.let { return@withContext it }

        runCatching {
            val db = FirebaseFirestore.getInstance(
                context.applicationContext.getString(R.string.firestore_database_id)
            )
            val snap = db.collection("workspaces")
                .document(EiSeedDefaults.SHARED_WORKSPACE_ID)
                .collection("photos")
                .document(photoId)
                .get()
                .await()
            val base64 = snap.getString("contentBase64")?.takeIf { it.isNotBlank() } ?: return@runCatching null
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            CloudPhotoCache.put(context.applicationContext, photoId, bytes)
            bytes
        }.getOrNull()
    }

    private fun compressUriToJpegBytes(context: Context, uri: Uri): ByteArray? {
        val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        if (rawBytes.isEmpty()) return null

        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOpts)
        if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null

        var sampleSize = 1
        var maxSide = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
        while (maxSide / sampleSize > MAX_PHOTO_DIMENSION_PX) {
            sampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts) ?: return null
        return try {
            val scale = minOf(
                1f,
                MAX_PHOTO_DIMENSION_PX.toFloat() / maxOf(decoded.width, decoded.height).toFloat()
            )
            val target = if (scale < 0.99f) {
                Bitmap.createScaledBitmap(
                    decoded,
                    (decoded.width * scale).toInt().coerceAtLeast(1),
                    (decoded.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                decoded
            }
            try {
                var quality = 74
                var outBytes: ByteArray
                do {
                    val baos = ByteArrayOutputStream()
                    target.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                    outBytes = baos.toByteArray()
                    quality -= 12
                } while (outBytes.size > 380_000 && quality >= 38)
                outBytes
            } finally {
                if (target !== decoded) target.recycle()
            }
        } finally {
            decoded.recycle()
        }
    }
}

@Composable
fun CloudPhotoImage(
    photoUri: String,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (!photoUri.startsWith(FB_PHOTO_SCHEME)) {
        AsyncImage(
            model = Uri.parse(photoUri),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
        return
    }

    val photoId = remember(photoUri) { photoUri.removePrefix(FB_PHOTO_SCHEME).trim() }
    var failed by remember(photoUri) { mutableStateOf(false) }
    val imageBytes by produceState<ByteArray?>(
        initialValue = CloudPhotoCache.getMemory(photoId),
        key1 = photoUri
    ) {
        if (value == null) {
            val loaded = WorkOrderPhotoStorage.fetchCloudPhotoBytes(context, photoUri)
            if (loaded != null) {
                value = loaded
            } else {
                failed = true
            }
        }
    }

    if (imageBytes != null) {
        AsyncImage(
            model = imageBytes,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            if (failed) {
                Icon(
                    imageVector = Icons.Rounded.BrokenImage,
                    contentDescription = contentDescription ?: "Foto tidak tersedia",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
fun rememberMultiPhotoPickerState(
    prefix: String,
    maxPhotos: Int = EiSeedDefaults.MAX_PHOTOS_PER_SECTION,
    currentCountProvider: () -> Int,
    onPhotosAdded: (List<String>) -> Unit
): WorkOrderPhotoActions {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestOnPhotosAdded by rememberUpdatedState(onPhotosAdded)
    val latestCountProvider by rememberUpdatedState(currentCountProvider)
    var pendingCaptureContentUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCaptureFileUri by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    fun uploadSourceUris(sourceUris: List<Uri>) {
        if (sourceUris.isEmpty()) return
        val remaining = (maxPhotos - latestCountProvider()).coerceAtLeast(0)
        if (remaining <= 0) {
            Toast.makeText(
                context,
                "Maksimal $maxPhotos foto sudah tercapai.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val clipped = if (sourceUris.size > remaining) {
            Toast.makeText(
                context,
                "Maksimal $maxPhotos foto; $remaining foto terpilih diunggah.",
                Toast.LENGTH_SHORT
            ).show()
            sourceUris.take(remaining)
        } else {
            sourceUris
        }

        scope.launch {
            isUploading = true
            val uploaded = mutableListOf<String>()
            var lastError: String? = null
            for (uri in clipped) {
                val res = WorkOrderPhotoStorage.uploadPhotoToFirebase(context, uri)
                res.onSuccess { fbUri -> uploaded.add(fbUri) }
                    .onFailure { err -> lastError = err.localizedMessage }
            }
            isUploading = false
            if (uploaded.isNotEmpty()) {
                latestOnPhotosAdded(uploaded)
            }
            if (lastError != null && uploaded.isEmpty()) {
                Toast.makeText(
                    context,
                    lastError ?: "Gagal mengunggah foto ke Firebase.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val fileUri = pendingCaptureFileUri
        if (success && fileUri != null) {
            uploadSourceUris(listOf(Uri.parse(fileUri)))
        }
        pendingCaptureContentUri = null
        pendingCaptureFileUri = null
    }

    fun launchCameraCapture() {
        if (latestCountProvider() >= maxPhotos) {
            Toast.makeText(
                context,
                "Maksimal $maxPhotos foto sudah tercapai.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        runCatching {
            val (contentUri, fileUri) = WorkOrderPhotoStorage.createCaptureDestination(context, prefix)
            pendingCaptureContentUri = contentUri
            pendingCaptureFileUri = fileUri
            takePictureLauncher.launch(contentUri)
        }.onFailure {
            pendingCaptureContentUri = null
            pendingCaptureFileUri = null
            Toast.makeText(
                context,
                "Kamera tidak tersedia atau gagal dibuka.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraCapture()
        } else {
            Toast.makeText(
                context,
                "Izin kamera diperlukan untuk mengambil foto langsung.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = EiSeedDefaults.MAX_PHOTOS_PER_SECTION)
    ) { uris ->
        uploadSourceUris(uris)
    }

    val fallbackFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uploadSourceUris(uris)
    }

    return remember(prefix, isUploading) {
        WorkOrderPhotoActions(
            isUploading = isUploading,
            onTakeCamera = {
                val hasCamera = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (hasCamera) {
                    launchCameraCapture()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onPickGallery = {
                if (latestCountProvider() >= maxPhotos) {
                    Toast.makeText(
                        context,
                        "Maksimal $maxPhotos foto sudah tercapai.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    runCatching {
                        multiPhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }.onFailure {
                        runCatching {
                            fallbackFilePickerLauncher.launch("image/*")
                        }.onFailure {
                            Toast.makeText(
                                context,
                                "Tidak dapat membuka pemilih gambar di perangkat.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun rememberWorkOrderPhotoPickerState(
    prefix: String,
    onPhotoReady: (String) -> Unit
): WorkOrderPhotoActions = rememberMultiPhotoPickerState(
    prefix = prefix,
    maxPhotos = 1,
    currentCountProvider = { 0 },
    onPhotosAdded = { list -> list.firstOrNull()?.let(onPhotoReady) }
)

class WorkOrderPhotoActions(
    val onTakeCamera: () -> Unit,
    val onPickGallery: () -> Unit,
    val isUploading: Boolean = false
)

@Composable
fun WorkOrderMultiPhotoSlot(
    title: String,
    subtitle: String,
    photoUris: List<String>,
    testTagPrefix: String,
    actions: WorkOrderPhotoActions,
    onRemovePhotoAt: (Int) -> Unit,
    onPreviewPhoto: (index: Int, uri: String) -> Unit,
    modifier: Modifier = Modifier,
    maxPhotos: Int = EiSeedDefaults.MAX_PHOTOS_PER_SECTION
) {
    val canAddMore = photoUris.size < maxPhotos && !actions.isUploading

    Surface(
        color = SlateSurfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (photoUris.isNotEmpty()) EmeraldSuccess.copy(alpha = 0.5f) else SlateBorder
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = when {
                        actions.isUploading -> CobaltLight
                        photoUris.isNotEmpty() -> EmeraldSurface
                        else -> SlateSurfaceVariant
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (actions.isUploading) "Mengunggah..." else "${photoUris.size}/$maxPhotos Foto",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            actions.isUploading -> CobaltPrimary
                            photoUris.isNotEmpty() -> EmeraldSuccess
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (actions.isUploading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CobaltLight.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = CobaltPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Mengunggah foto ke Firebase...",
                        style = MaterialTheme.typography.labelMedium,
                        color = CobaltPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (photoUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(photoUris) { idx, uri ->
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .clickable { onPreviewPhoto(idx, uri) }
                                .testTag("${testTagPrefix}_thumb_$idx")
                        ) {
                            CloudPhotoImage(
                                photoUri = uri,
                                contentDescription = "$title ${idx + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            IconButton(
                                onClick = { onRemovePhotoAt(idx) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .testTag("${testTagPrefix}_remove_$idx")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Hapus foto ${idx + 1}",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    if (canAddMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .clickable(onClick = actions.onPickGallery),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Rounded.AddAPhoto,
                                        contentDescription = "Tambah foto $title",
                                        tint = CobaltPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+ Tambah",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CobaltPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else if (!actions.isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable(onClick = actions.onTakeCamera),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.AddAPhoto,
                            contentDescription = "Tambah $title",
                            tint = CobaltPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Foto langsung atau pilih dari galeri (maks. $maxPhotos foto)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = actions.onTakeCamera,
                    enabled = canAddMore,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (canAddMore) CobaltPrimary else SlateBorder),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_camera_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null,
                        tint = if (canAddMore) CobaltPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (photoUris.size >= maxPhotos) "Penuh (4/4)" else "Foto Langsung",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (canAddMore) CobaltPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = actions.onPickGallery,
                    enabled = canAddMore,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_gallery_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoLibrary,
                        contentDescription = null,
                        tint = if (canAddMore) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (photoUris.size >= maxPhotos) "Maks 4 Foto" else "Unggah Foto",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (canAddMore) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun WorkOrderPhotoSlot(
    title: String,
    subtitle: String,
    photoUri: String?,
    testTagPrefix: String,
    actions: WorkOrderPhotoActions,
    onRemovePhoto: (() -> Unit)? = null,
    onPreviewPhoto: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SlateSurfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (!photoUri.isNullOrBlank()) EmeraldSuccess.copy(alpha = 0.5f) else SlateBorder
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (actions.isUploading) {
                    Surface(color = CobaltLight, shape = RoundedCornerShape(50)) {
                        Text(
                            text = "Mengunggah...",
                            style = MaterialTheme.typography.labelSmall,
                            color = CobaltPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (!photoUri.isNullOrBlank()) {
                    Surface(color = EmeraldSurface, shape = RoundedCornerShape(50)) {
                        Text(
                            text = "Tersimpan di Cloud",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (actions.isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CobaltLight.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = CobaltPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Mengunggah foto ke Firebase...",
                            style = MaterialTheme.typography.labelMedium,
                            color = CobaltPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else if (!photoUri.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .clickable { onPreviewPhoto?.invoke(photoUri) }
                ) {
                    CloudPhotoImage(
                        photoUri = photoUri,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (onRemovePhoto != null) {
                        IconButton(
                            onClick = onRemovePhoto,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .testTag("${testTagPrefix}_remove")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Hapus $title",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable(onClick = actions.onTakeCamera),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.AddAPhoto,
                            contentDescription = "Tambah $title",
                            tint = CobaltPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pilih Foto Langsung atau Unggah Foto",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = actions.onTakeCamera,
                    enabled = !actions.isUploading,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CobaltPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_camera_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null,
                        tint = CobaltPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (photoUri.isNullOrBlank()) "Foto Langsung" else "Foto Ulang",
                        style = MaterialTheme.typography.labelMedium,
                        color = CobaltPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = actions.onPickGallery,
                    enabled = !actions.isUploading,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_gallery_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (photoUri.isNullOrBlank()) "Unggah Foto" else "Ganti File",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun FullScreenPhotoDialog(
    title: String,
    photoUri: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup pratinjau foto",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                CloudPhotoImage(
                    photoUri = photoUri,
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CobaltLight.copy(alpha = 0.08f))
                )
            }
        }
    }
}
