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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OfflineChecklistEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.util.AppLanguage
import com.example.util.Strings

@Composable
fun OfflineChecklistScreen(
    checklists: List<OfflineChecklistEntity>,
    language: AppLanguage,
    onToggleChecklist: (id: String, completed: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.OfflinePin,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.get("offline_heading", language),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Strings.get("offline_subheading", language),
                    fontSize = 13.sp,
                    color = Slate400,
                    lineHeight = 18.sp
                )
            }
        }

        // Offline Banner
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Emerald500.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Emerald400)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (language == AppLanguage.URDU)
                            "یہ ڈیٹا آپ کے فون کی لوکل میموری (Room DB) میں محفوظ ہے اور بغیر انٹرنیٹ ہمیشہ دستیاب ہے۔"
                        else
                            "Persisted in local Room Database. Access standard civil procedures anytime without an active internet connection.",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        items(
            items = checklists,
            key = { it.id },
            contentType = { "offline_checklist_item" }
        ) { item ->
            val isUrdu = language == AppLanguage.URDU
            val category = if (isUrdu) item.categoryUr else item.category
            val title = if (isUrdu) item.titleUr else item.title
            val description = if (isUrdu) item.descriptionUr else item.description

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (item.isCompleted) Emerald500.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.04f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Category & Checkbox Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate800)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Emerald400
                            )
                        }

                        Icon(
                            imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (item.isCompleted) "Completed" else "Incomplete",
                            tint = if (item.isCompleted) Emerald400 else Slate600,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onToggleChecklist(item.id, !item.isCompleted) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isCompleted) Slate400 else Slate100,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = if (item.isCompleted) Slate500 else Slate300,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Helpline Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val cleanNum = item.helpline.filter { it.isDigit() }
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNum"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Slate800,
                                contentColor = Emerald400
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.helpline,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (item.isCompleted) (if (isUrdu) "مکمل شدہ ✓" else "Completed ✓") else (if (isUrdu) "زیرِ تکمیل" else "Pending"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (item.isCompleted) Emerald400 else Slate500
                        )
                    }
                }
            }
        }
    }
}
