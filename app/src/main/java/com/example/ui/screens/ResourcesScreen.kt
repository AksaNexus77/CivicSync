package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CivicResource
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Amber400
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

@Composable
fun ResourcesScreen(
    resources: List<CivicResource>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun openContact(contact: String) {
        if (contact.startsWith("1-") || contact.startsWith("Dial")) {
            val phoneNum = contact.replace("[^0-9]".toRegex(), "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // ignore
            }
        } else {
            val url = if (contact.startsWith("http")) contact else "https://$contact"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // ignore
            }
        }
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
                    text = "Legal Aid & Emergency Resources",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
                Text(
                    text = "Trusted nationwide free civil legal defense networks and social safety nets",
                    fontSize = 12.sp,
                    color = Slate400
                )
            }
        }

        // Emergency 2-1-1 Callout Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Emerald500.copy(alpha = 0.12f))
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2-1-1 ESSENTIAL COMMUNITY SERVICES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Emerald400
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Need Immediate Shelter, Food, or Utility Stay?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Free, confidential referral service operating 24/7 across North America.",
                            fontSize = 12.sp,
                            color = Slate300
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { openContact("211") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald400,
                            contentColor = Slate900
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call 211", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        items(resources) { res ->
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resource_${res.title.take(8)}"),
                backgroundColor = Color.White.copy(alpha = 0.04f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Indigo400.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = res.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Indigo400
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Emerald400.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = res.badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = res.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = res.description,
                        fontSize = 13.sp,
                        color = Slate300,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { openContact(res.contact) },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate600),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald400),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = if (res.contact.startsWith("1-") || res.contact.startsWith("Dial")) Icons.Default.Call else Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = res.contact,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
