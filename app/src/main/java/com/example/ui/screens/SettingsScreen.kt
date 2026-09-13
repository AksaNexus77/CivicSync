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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
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
import com.example.ui.theme.Slate900

@Composable
fun SettingsScreen(
    onOpenPrivacyPolicy: () -> Unit = {},
    onClearAllData: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showClearDataDialog by remember { mutableStateOf(false) }

    val isKeyConfigured = try {
        BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    } catch (e: Throwable) {
        false
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            containerColor = Slate900,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Rose400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Permanent Data Purge",
                        color = Slate100,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently erase all saved cases, document vault records, and offline progress from this device? This action complies with Google Play's User Data Deletion standards and cannot be undone.",
                    color = Slate300,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDataDialog = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose400),
                    modifier = Modifier.testTag("confirm_clear_data_button")
                ) {
                    Text("Delete Everything", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDataDialog = false }
                ) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "System Settings & Transparency",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
                Text(
                    text = "CivicSync AI system configurations, privacy architecture, and disclosures",
                    fontSize = 12.sp,
                    color = Slate400
                )
            }
        }

        // AI Engine Status
        item {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_ai_engine_card"),
                backgroundColor = Color.White.copy(alpha = 0.04f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Gemini Caseworker Engine",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isKeyConfigured) Emerald400.copy(alpha = 0.15f) else Amber400.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isKeyConfigured) "Live API Ready" else "Autonomous Mode",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isKeyConfigured) Emerald400 else Amber400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Target Model: gemini-3.5-flash\n" +
                                "System Directive: Empathetic Legal Aid & Social Worker Guidance\n" +
                                "Response Format: Structured Civic Action Plan (Raw JSON)",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isKeyConfigured) {
                            "✔ Gemini API key is linked via AI Studio Secrets."
                        } else {
                            "ℹ️ Autonomous Mode active: The app includes intelligent legal synthesizers and allows live Gemini API keys via AI Studio Secrets panel."
                        },
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }
        }

        // Privacy & Client Confidentiality
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.04f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Indigo400,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Confidentiality Architecture",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• Client Data Minimization: Case narratives and letters are stored in memory and local session cache only.\n" +
                                "• No Tracking or Telemetry: Your sensitive hardship records are never shared with advertising networks or third parties.\n" +
                                "• Clean Export: All draft letters and advocacy scripts can be copied or printed directly to local PDF without server intermediaries.",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Non-Legal Advice Bar Disclaimer
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.04f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Amber400,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Legal Aid Society Practice Notice",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "CivicSync AI is an automated self-help tool designed to assist individuals in organizing their thoughts, discovering public programs, and preparing administrative correspondence. It does not establish an attorney-client relationship and is not a substitute for formal representation in court.",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // Privacy Policy & Google Play Disclosures Card
        item {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_privacy_policy_card"),
                backgroundColor = Color.White.copy(alpha = 0.04f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Privacy Policy & Disclosures",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Review our Google Play compliant disclosures on data minimization, ephemeral Gemini AI processing, and zero third-party data broker sharing.",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenPrivacyPolicy,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald400),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("privacy_policy_button")
                    ) {
                        Text(
                            text = "Read Full Privacy Policy",
                            color = Slate900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // User Data Deletion & Account Erasure (Play Store Compliance)
        item {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_data_erasure_card"),
                backgroundColor = Rose400.copy(alpha = 0.05f),
                borderColor = Rose400.copy(alpha = 0.25f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = Rose400,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Data Safety & Permanent Deletion",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Rose400
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "In compliance with Google Play's User Data & Account Deletion policy, you may exercise your right to erasure. Purging your data will permanently delete all stored cases, document vault records, and procedural checklists from this device.",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { showClearDataDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose400),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Rose400.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_data_button")
                    ) {
                        Text(
                            text = "Clear All My Data",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
