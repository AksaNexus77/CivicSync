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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val isKeyConfigured = try {
        BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    } catch (e: Throwable) {
        false
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
    }
}
