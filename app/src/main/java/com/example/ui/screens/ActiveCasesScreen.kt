package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaseRecord
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun ActiveCasesScreen(
    cases: List<CaseRecord>,
    onSelectCase: (CaseRecord) -> Unit,
    onDeleteCase: (String) -> Unit,
    onNewCase: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Cases & Files",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Text(
                        text = "${cases.size} case file(s) recorded in session",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }

                Button(
                    onClick = onNewCase,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald400,
                        contentColor = Slate900
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("create_new_case_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Intake", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (cases.isEmpty()) {
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
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Active Cases Saved Yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Describe your situation in the Intake form to generate your personalized relief strategy and legal draft.",
                            fontSize = 13.sp,
                            color = Slate400,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onNewCase,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = Slate900),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Start First Intake", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(cases) { caseRecord ->
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCase(caseRecord) }
                        .testTag("case_record_${caseRecord.id}"),
                    backgroundColor = Color.White.copy(alpha = 0.04f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (caseRecord.urgency) {
                                                "Immediate Crisis" -> Rose400.copy(alpha = 0.2f)
                                                "This Week" -> Amber400.copy(alpha = 0.2f)
                                                else -> Emerald400.copy(alpha = 0.2f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = caseRecord.urgency,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (caseRecord.urgency) {
                                            "Immediate Crisis" -> Rose400
                                            "This Week" -> Amber400
                                            else -> Emerald400
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Slate500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = caseRecord.location,
                                        fontSize = 11.sp,
                                        color = Slate400
                                    )
                                }
                            }

                            Text(
                                text = caseRecord.dateFormatted,
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = caseRecord.situation,
                            fontSize = 13.sp,
                            color = Slate100,
                            lineHeight = 19.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${caseRecord.plan.eligibilitySummary.size} Entitlements · ${caseRecord.plan.actionChecklist.size} Tasks",
                                fontSize = 11.sp,
                                color = Emerald400,
                                fontWeight = FontWeight.Medium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onDeleteCase(caseRecord.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete case",
                                        tint = Slate500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                OutlinedButton(
                                    onClick = { onSelectCase(caseRecord) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate600),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Plan", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
