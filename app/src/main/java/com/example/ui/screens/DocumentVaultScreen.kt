package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.VaultDocType
import com.example.data.local.VaultDocumentEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.AppLanguage
import com.example.util.Strings
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentVaultScreen(
    documents: List<VaultDocumentEntity>,
    language: AppLanguage,
    onAddDocument: (title: String, docType: String, uriString: String?, description: String) -> Unit,
    onDeleteDocument: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var docTitle by remember { mutableStateOf("") }
    var docDescription by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(VaultDocType.CNIC_FRONT) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    // Android Photo Picker (zero-permission)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            showAddDialog = true
        }
    }

    // Android PDF / Document Picker
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            if (docTitle.isBlank()) docTitle = "Legal Document"
            showAddDialog = true
        }
    }

    // Camera Capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "vault_capture_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                selectedUri = Uri.fromFile(file)
                if (docTitle.isBlank()) docTitle = "Document Photo"
                showAddDialog = true
            } catch (e: Exception) {
                // Ignore fallback
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 84.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header span
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Strings.get("vault_title", language),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(
                            text = "256-Bit Encrypted Sovereign Vault",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Emerald400.copy(alpha = 0.15f))
                            .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald400, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SQLCipher", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald400)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bar: Camera & PDF Picker Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = Slate900),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("vault_camera_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan / Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { pdfPickerLauncher.launch("application/pdf") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate100),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("vault_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Indigo400, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload PDF", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Security Info Banner span
        item(span = { GridItemSpan(maxLineSpan) }) {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Indigo400.copy(alpha = 0.08f),
                borderColor = Indigo400.copy(alpha = 0.25f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (language == AppLanguage.URDU)
                            "والٹ کی تمام دستاویزات فون میں خفیہ رکھی جاتی ہیں اور قانونی درخواستوں میں استعمال ہوتی ہیں۔"
                        else
                            "Vault evidence is encrypted on-device and automatically verified in your formal appeals.",
                        fontSize = 11.sp,
                        color = Slate300,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Grid Items or Empty State
        if (documents.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.03f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Strings.get("empty_vault", language),
                            fontSize = 13.sp,
                            color = Slate400,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            items(documents, key = { it.id }) { doc ->
                VaultGridDocCard(
                    document = doc,
                    onDelete = { onDeleteDocument(doc.id) }
                )
            }
        }
    }

    // Add Document Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Slate900,
            title = {
                Text(
                    text = if (language == AppLanguage.URDU) "نئی دستاویز محفوظ کریں" else "Encrypt & Save Document",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (language == AppLanguage.URDU) selectedType.labelUr else selectedType.labelEn,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category", color = Slate400, fontSize = 12.sp) },
                            trailingIcon = {
                                IconButton(onClick = { typeDropdownExpanded = !typeDropdownExpanded }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Category",
                                        tint = Slate400
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { typeDropdownExpanded = !typeDropdownExpanded },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100,
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Slate700
                            )
                        )
                        DropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false },
                            modifier = Modifier.background(Slate800)
                        ) {
                            VaultDocType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(if (language == AppLanguage.URDU) type.labelUr else type.labelEn, color = Slate100) },
                                    onClick = {
                                        selectedType = type
                                        if (docTitle.isBlank()) {
                                            docTitle = if (language == AppLanguage.URDU) type.labelUr else type.labelEn
                                        }
                                        typeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Title
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Title", color = Slate400, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Slate700
                        )
                    )

                    // Notes
                    OutlinedTextField(
                        value = docDescription,
                        onValueChange = { docDescription = it },
                        label = { Text("Notes / Reference No.", color = Slate400, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Slate700
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = docTitle.ifBlank { selectedType.labelEn }
                        onAddDocument(
                            title,
                            selectedType.name,
                            selectedUri?.toString(),
                            docDescription
                        )
                        showAddDialog = false
                        docTitle = ""
                        docDescription = ""
                        selectedUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = Slate900)
                ) {
                    Text("Save to Vault", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }
}

@Composable
private fun VaultGridDocCard(
    document: VaultDocumentEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateString = remember(document.uploadedAt) {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        sdf.format(Date(document.uploadedAt))
    }

    val icon: ImageVector = when {
        document.docType.contains("CNIC") -> Icons.Default.CreditCard
        document.docType.contains("SEHAT") -> Icons.Default.HealthAndSafety
        document.docType.contains("RENT") -> Icons.Default.Home
        document.docType.contains("BILL") -> Icons.Default.Receipt
        document.docType.contains("BISP") -> Icons.AutoMirrored.Filled.Assignment
        else -> Icons.Default.Description
    }

    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vault_doc_${document.id}"),
        backgroundColor = Slate800.copy(alpha = 0.5f),
        borderColor = Slate700.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Preview / Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate900.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                if (!document.uriString.isNullOrBlank()) {
                    AsyncImage(
                        model = document.uriString,
                        contentDescription = document.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Encrypted Lock Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(Slate900.copy(alpha = 0.8f))
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = Emerald400,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = document.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Date and Type
            Text(
                text = "${document.docType} • $dateString",
                fontSize = 10.sp,
                color = Slate400,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Footer row with delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Emerald400.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Verified", fontSize = 9.sp, color = Emerald400, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Slate500,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
