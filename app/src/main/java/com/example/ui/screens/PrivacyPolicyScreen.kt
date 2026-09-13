package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

/**
 * Enterprise Google Play compliant Privacy Policy screen.
 * Discloses data handling practices, Gemini API ephemeral processing,
 * zero-tracker policies, and user data deletion rights.
 */
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    onDeleteAllData: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showConfirmDeleteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "header") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("privacy_policy_back_button")
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Slate800.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Settings",
                        tint = Slate100
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Privacy & Data Safety Policy",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Text(
                        text = "Google Play Policy Compliance & Client Data Disclosures",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }
        }

        // Compliance overview summary banner
        item(key = "summary_card") {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_summary_card"),
                backgroundColor = Indigo400.copy(alpha = 0.08f),
                borderColor = Indigo400.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Privacy-First, Local-First Commitment",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "CivicSync Global operates under strict data minimization guidelines. We never sell your personal information, deploy advertising trackers, or share confidential hardship records. All casework is saved first into an encrypted, sandboxed local Room vault with optional user-controlled Supabase cloud synchronization.",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section 1: Non-Attorney Legal Information Disclaimer
        item(key = "legal_disclaimer") {
            PolicySectionCard(
                icon = Icons.Default.Gavel,
                iconTint = Amber400,
                title = "1. Non-Attorney Legal Practice Notice",
                content = "CivicSync Global is an algorithmic self-help platform providing administrative guidance, public statutory references, and template assistance for welfare navigation. Use of CivicSync does NOT establish an attorney-client relationship, does not constitute licensed legal counsel, and cannot substitute for an authorized advocate in formal judicial or tribunal proceedings."
            )
        }

        // Section 2: Processing Sensitive Data
        item(key = "sensitive_data") {
            PolicySectionCard(
                icon = Icons.Default.Security,
                iconTint = Indigo400,
                title = "2. Processing of Sensitive Citizen Data",
                content = "When organizing an action plan, citizens may enter sensitive identifiers such as CNIC numbers, household income levels, disability statuses, or eviction notices. This data is utilized solely for:\n" +
                        "• Matching statutory eligibility thresholds (e.g. BISP PMT scores, Sehat Card coverage)\n" +
                        "• Populating formal administrative representation letters to ombudsmen\n" +
                        "• Creating citizen action checklists\n\n" +
                        "This data is never linked to advertising profiles or commercial data brokers."
            )
        }

        // Section 3: Google Gemini API & Generative AI Data Handling
        item(key = "gemini_ai_handling") {
            PolicySectionCard(
                icon = Icons.Default.Psychology,
                iconTint = Emerald400,
                title = "3. Google Gemini API & AI Governance",
                content = "To draft customized representation letters and statutory analyses, case narratives are transmitted securely to the Google Gemini API over Transport Layer Security (TLS 1.3).\n\n" +
                        "• Ephemeral Transit: Case prompts are processed in real-time and immediately returned.\n" +
                        "• No Foundation Model Training: Data submitted through the enterprise API is not utilized to train or fine-tune public Gemini foundation models.\n" +
                        "• Autonomous Contingency Engine: In zero-connectivity conditions, the app utilizes deterministic on-device statutory fallback plans without external network calls."
            )
        }

        // Section 4: On-Device Room Database & SQLCipher Encryption
        item(key = "local_storage") {
            PolicySectionCard(
                icon = Icons.Default.Lock,
                iconTint = Emerald400,
                title = "4. Encrypted Local Storage (SQLCipher)",
                content = "All saved cases, active grievances, and Document Vault attachments are encrypted directly on your device using 256-bit AES SQLCipher Room database encryption.\n\n" +
                        "• Optional Supabase Sync: If you sign in, your cases synchronize to a private Supabase partition protected by Row-Level Security (RLS) where only your UID has read/write privileges.\n" +
                        "• Sandboxed Isolation: Protected by Android operating system internal app sandboxing."
            )
        }

        // Section 5: Permissions & Android Least-Privilege
        item(key = "permissions_disclosure") {
            PolicySectionCard(
                icon = Icons.Default.Shield,
                iconTint = Slate100,
                title = "5. Device Permissions & Least-Privilege Compliance",
                content = "CivicSync requests strictly two permissions:\n" +
                        "• INTERNET: Communicating with the Gemini API and secure Supabase synchronization.\n" +
                        "• CAMERA (Runtime): Used solely when you take a photo of a physical receipt or certificate for your personal encrypted Document Vault.\n" +
                        "• Document Vault (Zero-Permission): Uses the Android Photo Picker / System Storage Framework, requiring zero broad storage permissions."
            )
        }

        // Section 6: User Data Deletion & Google Play Compliance
        item(key = "data_deletion") {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_deletion_card"),
                backgroundColor = Rose400.copy(alpha = 0.08f),
                borderColor = Rose400.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Rose400.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = Rose400,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "6. User Data Deletion Rights (Play Policy)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Under Google Play's User Data policy and global privacy laws (GDPR/CCPA), citizens have complete sovereignty over their data. You can permanently erase all local encrypted Room records and Supabase cloud records at any time.",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    androidx.compose.material3.Button(
                        onClick = { showConfirmDeleteDialog = true },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Rose400
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_account_and_data_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = Slate100,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete My Account & Data",
                            fontWeight = FontWeight.Bold,
                            color = Slate100,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Footer version info
        item(key = "footer") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CivicSync Global • Version 1.0 (Build 1)",
                    fontSize = 11.sp,
                    color = Slate500
                )
                Text(
                    text = "Effective Date: September 2026 • Published for Google Play Store",
                    fontSize = 10.sp,
                    color = Slate500
                )
            }
        }
    }

    if (showConfirmDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = {
                Text(
                    text = "Delete All Account & Local Data?",
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
            },
            text = {
                Text(
                    text = "This will permanently purge all saved cases, vault documents, and delete all remote Supabase sync records associated with this citizen profile. This action cannot be undone.",
                    color = Slate300,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        showConfirmDeleteDialog = false
                        onDeleteAllData()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Rose400
                    )
                ) {
                    Text("Permanently Delete", color = Slate100, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showConfirmDeleteDialog = false }
                ) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate800
        )
    }
}

@Composable
private fun PolicySectionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.04f)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = content,
                fontSize = 12.sp,
                color = Slate300,
                lineHeight = 19.sp
            )
        }
    }
}
