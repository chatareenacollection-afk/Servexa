package com.example.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.WorkVideoEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ServexaTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.ServexaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkVideosFeedScreen(
    viewModel: ServexaViewModel,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.workVideos.collectAsState()
    var activeCommentVideoId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            ServexaTopBar(
                title = "Work in Action Reels",
                subtitle = "Discover real project walkthroughs",
                showBack = true,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (videos.isEmpty()) {
            EmptyStateView(
                title = "No work videos available",
                subtitle = "Service professionals upload their completed project videos here.",
                icon = Icons.Default.VideoLibrary,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(videos, key = { it.id }) { video ->
                    VideoReelCard(
                        video = video,
                        onLike = { viewModel.toggleVideoLike(video.id) },
                        onCommentClick = { activeCommentVideoId = video.id },
                        onViewProvider = { viewModel.navigateTo(ScreenDestination.ProviderProfile(video.providerId)) }
                    )
                }
            }
        }
    }

    if (activeCommentVideoId != null) {
        val currentVideoId = activeCommentVideoId!!
        var newCommentText by remember { mutableStateOf("") }
        val activeVideo = remember(videos, currentVideoId) { videos.find { it.id == currentVideoId } }

        ModalBottomSheet(
            onDismissRequest = { activeCommentVideoId = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Comments (${activeVideo?.commentsCount ?: 0})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Add a comment...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("video_comment_input"),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                viewModel.addVideoComment(currentVideoId, newCommentText)
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier.testTag("post_comment_button")
                    ) {
                        Text("Post")
                    }
                }
            }
        }
    }
}

@Composable
fun VideoReelCard(
    video: WorkVideoEntity,
    onLike: () -> Unit,
    onCommentClick: () -> Unit,
    onViewProvider: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_reel_${video.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Simulated Video Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), ServexaIndigoDark, Color(0xFF020617))
                        )
                    )
            ) {
                // Play Icon Center
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play Video",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                )

                // Category Badge Top Left
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = ServexaIndigo.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = video.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Provider Badge Bottom Overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onViewProvider)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(ServexaTeal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = video.providerName.take(1).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = video.providerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Description & Social Action Bar
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = video.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bar (Likes, Comments, Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable(onClick = onLike)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Like",
                                tint = ServexaRose,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "${video.likesCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Comment Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable(onClick = onCommentClick)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Comment",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${video.commentsCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onViewProvider,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ServexaTealLight)
                    ) {
                        Text("View Profile", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
