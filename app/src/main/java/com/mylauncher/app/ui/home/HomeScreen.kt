package com.mylauncher.app.ui.home

import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylauncher.app.ui.LauncherViewModel
import com.mylauncher.app.services.MediaNotificationListener
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    onAddFavoritesClick: () -> Unit = {},
    onPhoneClick: () -> Unit = {},
    onMessageClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val notificationSummaries by MediaNotificationListener.notificationSummaries.collectAsState()

    // Current date formatter for top header
    val dateFormat = remember { SimpleDateFormat("EEEE, d'th' MMMM", Locale.getDefault()) }
    val currentDateStr = remember { dateFormat.format(Date()) }

    // Pick the next calendar agenda or show fallback text
    const val defaultAgenda = "No upcoming events"
    val nextEvent = upcomingEvents.firstOrNull()
    val agendaText = if (nextEvent != null) {
        "${nextEvent.title} • ${nextEvent.timeString}"
    } else {
        defaultAgenda
    }

    // Active media track info if available
    val activeMedia = notificationSummaries.values.firstOrNull()
    val mediaText = activeMedia?.let { "${it.title} - ${it.artist}" } ?: "Smart Media / Audio"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        // --- TOP SECTION ---
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 32.dp)
        ) {
            // Minimalist Clock & Date (Zero settings icon as requested)
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.clickable {
                    // Open system clock/alarm on clock tap
                    try {
                        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentDateStr,
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Niagara-style Calendar Agenda Pill (Replacing screen time)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.DarkGray.copy(alpha = 0.4f),
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable {
                        if (nextEvent != null) {
                            viewModel.openCalendarEvent(context, nextEvent.id)
                        } else {
                            viewModel.launchCalendarNewEvent(context)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = agendaText,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // --- MIDDLE SECTION (Favorites Unselected State) ---
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preview App Circles
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val previewColors = listOf(Color.Green, Color(0xFFFF9800), Color.Blue, Color.Gray)
                previewColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.8f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Empty State Prompt
            Text(
                text = "Your most used apps appear here",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Choose upto 5 apps most used by you that will remain in home screen.",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Add Favorites Button
            Button(
                onClick = onAddFavoritesClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "Add Favorites",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // --- BOTTOM DOCK SECTION ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Smart Media Widget Pill (Replacing Focus Mode with matching pill geometry)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.DarkGray.copy(alpha = 0.4f),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clickable {
                        // Open active media app or fallback to audio settings
                        activeMedia?.packageName?.let { pkg ->
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                            if (launchIntent != null) context.startActivity(launchIntent)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Media",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = mediaText,
                        color = Color.White,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Persistent Quick Launch Dock Icons (Right Side)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = onMessageClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Messages",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onPhoneClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Phone",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
