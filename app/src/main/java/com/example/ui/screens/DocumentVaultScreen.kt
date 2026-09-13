package com.example.ui.screens

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.VaultDocType
import com.example.data.local.VaultDocumentEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.AppLanguage
import com.example.util.Strings
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
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var docTitle by remember { mutableStateOf("") }
    var docDescription by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(VaultDocType.CNIC_FRONT) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    // Android Photo Picker launcher (zero permission, compliant with Google Play Policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            if (docTitle.isBlank()) {
                docTitle = if (language == AppLanguage.URDU) selectedType.labelUr else selectedType.labelEn
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.get("vault_heading", language),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = Strings.get("vault_subheading", language),
                        fontSize = 13.sp,
                        color = Slate400,
                        lineHeight = 18.sp
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald400,
                        contentColor = Slate900
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("upload_document_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.URDU) "شامل کریں" else "Add Doc",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Security & AI Context Banner
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Indigo400.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (language == AppLanguage.URDU)
                            "والٹ میں محفوظ دستاویزات اے آئی حکمت عملی اور قانونی خطوط میں خودکار طور پر شامل کی جاتی ہیں۔"
                        else
                            "Verified documents in your Vault are automatically referenced in action checklists and appeal letters.",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Empty state
        if (documents.isEmpty()) {
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White.copy(alpha = 0.03f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = Strings.get("empty_vault", language),
                            fontSize = 14.sp,
                            color = Slate400,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald400,
                                contentColor = Slate900
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (language == AppLanguage.URDU) "شناختی کارڈ یا دستاویز شامل کریں" else "Add CNIC or Letter")
                        }
                    }
                }
            }
        } else {
            items(
                items = documents,
                key = { it.id },
                contentType = { "vault_doc" }
            ) { doc ->
                VaultDocCard(
                    document = doc,
                    language = language,
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
                    text = if (language == AppLanguage.URDU) "نئی دستاویز محفوظ کریں" else "Add Document to Vault",
                    fontSize = 18.sp,
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
                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (language == AppLanguage.URDU) selectedType.labelUr else selectedType.labelEn,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (language == AppLanguage.URDU) "دستاویز کی قسم" else "Document Category", color = Slate400) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100,
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Slate700
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false },
                            modifier = Modifier.background(Slate800)
                        ) {
                            VaultDocType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(if (language == AppLanguage.URDU) type.labelUr else type.labelEn, color = Slate100) },
                                    onClick = {
                                        selectedType = type
                                        docTitle = if (language == AppLanguage.URDU) type.labelUr else type.labelEn
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
                        label = { Text(if (language == AppLanguage.URDU) "دستاویز کا عنوان" else "Document Title", color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Slate700
                        )
                    )

                    // Photo Picker Button
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedUri != null) Emerald500.copy(alpha = 0.3f) else Slate800,
                            contentColor = if (selectedUri != null) Emerald400 else Slate200
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedUri != null) {
                                if (language == AppLanguage.URDU) "تصویر منتخب ہو گئی ✓" else "Photo Selected ✓"
                            } else {
                                if (language == AppLanguage.URDU) "تصویر منتخب کریں / کیمرہ" else "Pick Photo / Scan"
                            },
                            fontSize = 13.sp
                        )
                    }

                    // Description / Details
                    OutlinedTextField(
                        value = docDescription,
                        onValueChange = { docDescription = it },
                        label = { Text(if (language == AppLanguage.URDU) "تفصیل یا شناختی کارڈ نمبر" else "Notes / CNIC Details", color = Slate400) },
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
                        val title = docTitle.ifBlank {
                            if (language == AppLanguage.URDU) selectedType.labelUr else selectedType.labelEn
                        }
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald400,
                        contentColor = Slate900
                    )
                ) {
                    Text(if (language == AppLanguage.URDU) "محفوظ کریں" else "Save to Vault")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (language == AppLanguage.URDU) "منسوخ" else "Cancel", color = Slate400)
                }
            }
        )
    }
}

@Composable
fun VaultDocCard(
    document: VaultDocumentEntity,
    language: AppLanguage,
    onDelete: () -> Unit
) {
    val dateString = remember(document.uploadedAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
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
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Emerald400.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Emerald400,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = document.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Emerald400,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (document.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = document.description,
                        fontSize = 12.sp,
                        color = Slate300
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateString,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Indigo400.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = Strings.get("doc_referenced_badge", language),
                            fontSize = 10.sp,
                            color = Indigo400
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = Slate500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
