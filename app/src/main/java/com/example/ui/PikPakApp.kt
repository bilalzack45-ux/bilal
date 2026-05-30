package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.DraftVideo
import com.example.data.UserProfile
import com.example.data.WalletTransaction
import com.example.data.ChatMessage
import com.example.ui.theme.*
import com.example.ui.viewmodel.*

enum class PikPakTab(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Feed", Icons.Default.PlayArrow),
    DISCOVER("Explore", Icons.Default.Search),
    STUDIO("Upload", Icons.Default.AddCircle),
    INBOX("Direct DMs", Icons.Default.Email),
    WALLET("Finance", Icons.Default.AccountBalanceWallet),
    ADMIN("Admin Desk", Icons.Default.AdminPanelSettings),
    BLUEPRINT("Blueprint", Icons.Default.Description)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PikPakApp(viewModel: PikPakViewModel) {
    var activeTab by remember { mutableStateOf(PikPakTab.HOME) }
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DeepGrey,
                contentColor = WhitePure,
                tonalElevation = 12.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                PikPakTab.values().forEach { tab ->
                    val isSelected = activeTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activeTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.displayName,
                                tint = if (isSelected) NeonCherry else MutedSlate
                            )
                        },
                        label = {
                            Text(
                                text = tab.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) WhitePure else MutedSlate
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = ActiveGrey
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PremiumBlack)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.96f, animationSpec = tween(220)))
                        .togetherWith(fadeOut(animationSpec = tween(120)))
                },
                label = "MainAppTransitions"
            ) { targetTab ->
                when (targetTab) {
                    PikPakTab.HOME -> FeedScreen(viewModel)
                    PikPakTab.DISCOVER -> DiscoverScreen(viewModel)
                    PikPakTab.STUDIO -> StudioScreen(viewModel)
                    PikPakTab.INBOX -> DirectMessageScreen(viewModel)
                    PikPakTab.WALLET -> WalletFinanceScreen(viewModel)
                    PikPakTab.ADMIN -> AdminDashboardScreen(viewModel)
                    PikPakTab.BLUEPRINT -> TechnicalBlueprintScreen()
                }
            }
        }
    }
}

// ---------------- HOME FEED SCREEN ----------------
@Composable
fun FeedScreen(viewModel: PikPakViewModel) {
    val videos by viewModel.videoFeeds.collectAsState()
    var activeVideoIdx by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf("For You") }
    
    var showCommentSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (videos.isNotEmpty()) {
            val currentVideo = videos[activeVideoIdx]
            
            // Simulated Video Display Canvas
            VideoSimulatorCanvas(
                video = currentVideo,
                musicSpinDegrees = if (true) 360f else 0f,
                onDoubleTap = { viewModel.toggleLikeVideo(currentVideo.id) }
            )

            // Category Bar Top Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Following", "For You", "Trending").forEach { cat ->
                    val isCatActive = selectedCategory == cat
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isCatActive) WhitePure else MutedSlate,
                            fontWeight = if (isCatActive) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 17.sp
                        )
                        if (isCatActive) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(width = 16.dp, height = 2.dp)
                                    .background(NeonCherry)
                            )
                        }
                    }
                }
            }

            // Right Aspect Actions Console
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Avatar with Verification Badge plus "+" symbol
                Box(contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, NeonCyan, CircleShape)
                            .background(ActiveGrey)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentVideo.creatorAvatar)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (currentVideo.isVerified) {
                        Box(
                            modifier = Modifier
                                .offset(y = 4.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(NeonCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Verified",
                                tint = PremiumBlack,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Like Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { viewModel.toggleLikeVideo(currentVideo.id) },
                        modifier = Modifier.testTag("like_btn_${currentVideo.id}")
                    ) {
                        Icon(
                            imageVector = if (currentVideo.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (currentVideo.isLiked) NeonCherry else WhitePure,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Text(
                        text = formatIntCompact(currentVideo.likesCount),
                        color = WhitePure,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Comments Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { showCommentSheet = true },
                        modifier = Modifier.testTag("comments_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Comments",
                            tint = WhitePure,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = formatIntCompact(currentVideo.commentsCount),
                        color = WhitePure,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Save Bracket
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { viewModel.toggleSaveVideo(currentVideo.id) },
                        modifier = Modifier.testTag("save_btn")
                    ) {
                        Icon(
                            imageVector = if (currentVideo.isSaved) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Save",
                            tint = if (currentVideo.isSaved) NeonGold else WhitePure,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = formatIntCompact(currentVideo.savesCount),
                        color = WhitePure,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Share Bracket
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { showShareSheet = true },
                        modifier = Modifier.testTag("share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = WhitePure,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text(
                        text = formatIntCompact(currentVideo.sharesCount),
                        color = WhitePure,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Spinning Vinyl Logo
                SpinningMusicRecordDisk(songTitle = currentVideo.songTitle)
            }

            // Left Bottom Metadata Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.75f)
                    .padding(start = 16.dp, bottom = 24.dp)
            ) {
                // Handle Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "@${currentVideo.creatorHandle}",
                        color = WhitePure,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (currentVideo.isLive) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonCherry)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "LIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = WhitePure,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Video Description
                Text(
                    text = currentVideo.description,
                    color = WhitePure,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Video Tags
                Text(
                    text = currentVideo.tags,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Soundtrack scrolling effect or text
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Soundtrack",
                        tint = WhitePure,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentVideo.songTitle,
                        color = WhitePure,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee()
                    )
                }
            }

            // Carousel Pager navigation arrows for emulation convenience
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (activeVideoIdx > 0) {
                        IconButton(
                            onClick = { activeVideoIdx-- },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ActiveGrey.copy(alpha = 0.7f))
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, "Prev Video", tint = WhitePure)
                        }
                    }
                    if (activeVideoIdx < videos.size - 1) {
                        IconButton(
                            onClick = { activeVideoIdx++ },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ActiveGrey.copy(alpha = 0.7f))
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, "Next Video", tint = WhitePure)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Initializing cinematic streams...", color = MutedSlate)
            }
        }

        // --- COMMENTS BOTTOM DRAWER SHEET ---
        if (showCommentSheet) {
            CommentsDrawer(
                videoId = videos[activeVideoIdx].id,
                viewModel = viewModel,
                onDismiss = { showCommentSheet = false }
            )
        }

        // --- SHARE SHEETS DIALOG ---
        if (showShareSheet) {
            ShareDialog(
                video = videos[activeVideoIdx],
                onDismiss = { showShareSheet = false }
            )
        }
    }
}

@Composable
fun VideoSimulatorCanvas(video: VideoItem, musicSpinDegrees: Float, onDoubleTap: () -> Unit) {
    // Beautiful dynamic motion Canvas imitating real-time video textures
    val infiniteTransition = rememberInfiniteTransition(label = "VideoTextures")
    val gradientX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GradMove"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDoubleTap() }
    ) {
        val color1 = Color(android.graphics.Color.parseColor("#" + video.primaryColorHex))
        val color2 = Color(android.graphics.Color.parseColor("#" + video.secondaryColorHex))

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(color1, color2, PremiumBlack),
                    center = androidx.compose.ui.geometry.Offset(gradientX, size.height / 2f),
                    radius = size.width * 1.5f
                )
            )
        }

        // Live grid texture lines representing cool cinematic grid details
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PremiumBlack.copy(alpha = 0.4f),
                            Color.Transparent,
                            PremiumBlack.copy(alpha = 0.7f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "🎥 STREAMING ${video.id} \n[1080P @60FPS HDR]",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WhitePure.copy(alpha = 0.15f),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
fun SpinningMusicRecordDisk(songTitle: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "MusicDisk")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DiskAngle"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .rotate(angle)
            .clip(CircleShape)
            .background(PremiumBlack)
            .border(2.dp, NeonCherry, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Inner vinyl details
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(NeonCyan),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(PremiumBlack)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommentsDrawer(videoId: Int, viewModel: PikPakViewModel, onDismiss: () -> Unit) {
    val comments by viewModel.comments.collectAsState()
    var commentText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBlack.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(DeepGrey)
                .padding(16.dp)
                .clickable(enabled = false) { /* Prevent bubble click */ }
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comments (${comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WhitePure
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WhitePure)
                }
            }

            Divider(color = ActiveGrey, thickness = 1.dp)

            // Scrollable List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(comments.filter { it.videoId == videoId }) { comment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val colorOfAvatar = Color(android.graphics.Color.parseColor("#" + comment.avatarColor))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorOfAvatar),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = comment.author.take(2).uppercase().replace("@", ""),
                                color = WhitePure,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Text content block
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    comment.author,
                                    color = MutedSlate,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                if (comment.isPinned) {
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(NeonGold.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            "PINNED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                comment.content,
                                color = WhitePure,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                comment.timestamp,
                                color = MutedSlate,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        IconButton(onClick = { viewModel.toggleLikeComment(comment.id) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (comment.likedByUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "LikeComment",
                                    tint = if (comment.likedByUser) NeonCherry else MutedSlate,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    comment.likes.toString(),
                                    color = MutedSlate,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = ActiveGrey, thickness = 1.dp)

            // Submit input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Add comment...", color = MutedSlate) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhitePure,
                        unfocusedTextColor = WhitePure,
                        focusedBorderColor = NeonCherry,
                        unfocusedBorderColor = ActiveGrey,
                        focusedContainerColor = ActiveGrey,
                        unfocusedContainerColor = ActiveGrey
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_box"),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.addVideoComment(videoId, "@me", commentText)
                            commentText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonCherry)
                        .testTag("comment_submit_btn")
                ) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Send", tint = WhitePure)
                }
            }
        }
    }
}

@Composable
fun ShareDialog(video: VideoItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Express Share video", color = WhitePure) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Republish and circulate @${video.creatorHandle}'s digital stream across your standard monetization channels.", color = MutedSlate)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        "Stitched Duet" to Icons.Default.Flip,
                        "Repost" to Icons.Default.DoubleArrow,
                        "WhatsApp" to Icons.Default.Call,
                        "Direct Link" to Icons.Default.Link
                    ).forEach { (label, icon) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onDismiss() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(ActiveGrey),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, label, tint = NeonCyan)
                            }
                            Text(label, color = MutedSlate, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = NeonCherry)) {
                Text("Dismiss")
            }
        },
        containerColor = DeepGrey
    )
}

// ---------------- DISCOVER SCREEN ----------------
@Composable
fun DiscoverScreen(viewModel: PikPakViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val campaigns by viewModel.adCampaigns.collectAsState()
    
    // Ad campaign generator states
    var showAdGenerator by remember { mutableStateOf(false) }
    var campaignName by remember { mutableStateOf("") }
    var adType by remember { mutableStateOf("In-Feed Video") }
    var adBudget by remember { mutableStateOf("") }
    var adCpa by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Head
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search users, hashtags, and sounds...", color = MutedSlate) },
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = NeonCherry) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WhitePure,
                unfocusedTextColor = WhitePure,
                focusedBorderColor = NeonCherry,
                unfocusedBorderColor = ActiveGrey,
                focusedContainerColor = DeepGrey,
                unfocusedContainerColor = DeepGrey
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_search_field")
        )

        // Trending Hot Labels Block
        Text("Trending Categories", color = WhitePure, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(
                "🔥 #baking lava" to NeonCherry,
                "🎸 #synth reverb" to NeonCyan,
                "🤖 #pikpak compose" to NeonGold,
                "🏞️ #iceland glacier" to NeonGreen
            ).forEach { (tag, tintColor) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ActiveGrey)
                        .border(1.dp, tintColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(tag, color = WhitePure, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Ads Section Head
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "In-Feed Ads Campaigns",
                color = WhitePure,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showAdGenerator = true },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCherry),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, "New Ad", tint = WhitePure, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Provision Ad", fontSize = 12.sp)
            }
        }

        if (showAdGenerator) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepGrey),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Provision New Sponsor Ad System", color = NeonCyan, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = campaignName,
                        onValueChange = { campaignName = it },
                        label = { Text("Brand Campaign Name", color = MutedSlate) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhitePure, unfocusedTextColor = WhitePure)
                    )

                    // Target Type selector
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("In-Feed Video", "Brand Takeover", "Sponsored Tag").forEach { type ->
                            ElevatedFilterChip(
                                selected = adType == type,
                                onClick = { adType = type },
                                label = { Text(type, fontSize = 11.sp, color = WhitePure) },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = NeonCherry
                                )
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = adBudget,
                            onValueChange = { adBudget = it },
                            label = { Text("Budget ($)", color = MutedSlate) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhitePure, unfocusedTextColor = WhitePure)
                        )
                        OutlinedTextField(
                            value = adCpa,
                            onValueChange = { adCpa = it },
                            label = { Text("Target CPC ($)", color = MutedSlate) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhitePure, unfocusedTextColor = WhitePure)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAdGenerator = false }, colors = ButtonDefaults.textButtonColors(contentColor = MutedSlate)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (campaignName.isNotBlank() && adBudget.isNotBlank()) {
                                    viewModel.createAdCampaign(
                                        campaignName,
                                        adType,
                                        adBudget.toDoubleOrNull() ?: 1000.0,
                                        adCpa.toDoubleOrNull() ?: 0.5
                                    )
                                    // Reset and Close
                                    campaignName = ""
                                    adBudget = ""
                                    adCpa = ""
                                    showAdGenerator = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCherry)
                        ) {
                            Text("Activate Ads")
                        }
                    }
                }
            }
        }

        // Ads Lists Metrics Card
        campaigns.forEach { camp ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepGrey),
                border = BorderStroke(1.dp, if (camp.status == "Active") NeonGreen else MutedSlate),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(camp.campaignName, color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (camp.status == "Active") NeonGreen.copy(alpha = 0.15f) else ActiveGrey)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                camp.status.uppercase(),
                                fontSize = 10.sp,
                                color = if (camp.status == "Active") NeonGreen else WhitePure,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Campaign Category: ${camp.adType}", color = MutedSlate, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Aggregated Budget", color = MutedSlate, fontSize = 10.sp)
                            Text("$${camp.budget}", color = WhitePure, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Impressions", color = MutedSlate, fontSize = 10.sp)
                            Text(formatIntCompact(camp.impressions), color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Click-Throughs", color = MutedSlate, fontSize = 10.sp)
                            Text(formatIntCompact(camp.clicks), color = NeonGold, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Avg CPA CPC", color = MutedSlate, fontSize = 10.sp)
                            Text("$${camp.targetCPA}", color = WhitePure, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Fake Discover placeholder visual list
        Text("Sound Library Pickers", color = WhitePure, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepGrey),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "Marcus' Culinary Beats" to "4k+ Videos • Creator Exclusive",
                    "Symphonic Echo Session" to "1.2M+ Videos • Trending sound",
                    "SynthWave Retro Cyber" to "230k+ Videos • Techno Pop",
                    "Flute of the Frozen Valleys" to "10M+ Videos • Chill relaxation"
                ).forEach { (trackName, useCount) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(NeonCherry),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MusicNote, "SoundTrack", tint = WhitePure, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(trackName, color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(useCount, color = MutedSlate, fontSize = 11.sp)
                            }
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.PlayArrow, "Listen", tint = NeonCyan)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- STUDIO UPLOAD SCREEN ----------------
@Composable
fun StudioScreen(viewModel: PikPakViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("#dance #creative") }
    var duration by remember { mutableStateOf(15) }
    var resolution by remember { mutableStateOf("1080p") }
    var activeFilter by remember { mutableStateOf("Glamour") }
    var beautyLevel by remember { mutableStateOf(75f) }
    var locationTag by remember { mutableStateOf("Seattle, WA") }

    val drafts by viewModel.draftVideos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero visual creator camera mock box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepGrey, PremiumBlack)
                    )
                )
                .border(1.5.dp, NeonCherry, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Videocam, "Camera Node", tint = NeonCherry, modifier = Modifier.size(48.dp))
                Text("CAMERA ENGINE ACTIVE", color = WhitePure, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                Text("Adaptive Filters & Real-time Edge Capture", color = MutedSlate, fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("ACTIVE FILTER: $activeFilter", color = NeonCyan, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Interactive Studio Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepGrey),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Beauty, Filter & Auto-Captions Panel", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                
                // Selectable filter capsules
                Text("Select Special FX / Greenscreen Filter", color = MutedSlate, fontSize = 12.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Glamour", "Green Screen", "Vintage Cinema", "Cyber Glow", "Feline Blur").forEach { filt ->
                        val isFeltActive = activeFilter == filt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isFeltActive) NeonCherry else ActiveGrey)
                                .clickable { activeFilter = filt }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(filt, color = WhitePure, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Slider beauty setting
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Beauty intensity", color = MutedSlate, fontSize = 12.sp, modifier = Modifier.width(100.dp))
                    Slider(
                        value = beautyLevel,
                        onValueChange = { beautyLevel = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = NeonCherry, activeTrackColor = NeonCherry),
                        modifier = Modifier.weight(1f)
                    )
                    Text("${beautyLevel.toInt()}%", color = WhitePure, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                }

                // HD/4K Selection and Duration Selectors
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Export Spec", color = MutedSlate, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1080p", "4K Ultra").forEach { res ->
                                val active = resolution == res
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) NeonCyan.copy(alpha = 0.2f) else ActiveGrey)
                                        .border(1.dp, if (active) NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { resolution = res }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(res, color = if (active) NeonCyan else WhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clip Length (s)", color = MutedSlate, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(15, 60).forEach { sec ->
                                val active = duration == sec
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) NeonCherry.copy(alpha = 0.2f) else ActiveGrey)
                                        .border(1.dp, if (active) NeonCherry else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { duration = sec }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${sec}s", color = if (active) NeonCherry else WhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Form fields info save draft
        Text("Creator Video Details", color = WhitePure, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Video Title", color = MutedSlate) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhitePure, unfocusedTextColor = WhitePure, focusedBorderColor = NeonCherry, unfocusedBorderColor = ActiveGrey),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Long Video Description", color = MutedSlate) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhitePure, unfocusedTextColor = WhitePure, focusedBorderColor = NeonCherry, unfocusedBorderColor = ActiveGrey),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Hashtags / Mentions", color = MutedSlate) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhitePure, unfocusedTextColor = WhitePure, focusedBorderColor = NeonCherry, unfocusedBorderColor = ActiveGrey),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = locationTag,
                onValueChange = { locationTag = it },
                label = { Text("Location Tag", color = MutedSlate) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhitePure, unfocusedTextColor = WhitePure, focusedBorderColor = NeonCherry, unfocusedBorderColor = ActiveGrey),
                modifier = Modifier.weight(1f)
            )
        }

        // Primary Buttons (SAVE DRAFT and LIVE PUBLISH)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.saveVideoDraft(
                            title, description, tags, resolution, duration, activeFilter, beautyLevel.toInt(), locationTag
                        )
                        title = ""
                        description = ""
                        tags = ""
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("save_draft_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ActiveGrey),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, "Save Draft")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save to Drafts")
            }

            Button(
                onClick = {
                    // Trigger a mock direct publish dialogue
                    title = ""
                    description = ""
                    tags = ""
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCherry),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CloudUpload, "Publish Live")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Publish Stream")
            }
        }

        // Room Database Draft lists persisted inside device
        Text("Saved Drafts Directory (${drafts.size})", color = WhitePure, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (drafts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepGrey),
                contentAlignment = Alignment.Center
            ) {
                Text("No pending drafts detected in SQLite Storage.", color = MutedSlate, fontSize = 13.sp)
            }
        } else {
            drafts.forEach { draft ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepGrey),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(draft.title, color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Format: ${draft.resolution} • ${draft.duration}s", color = MutedSlate, fontSize = 12.sp)
                            }
                            IconButton(onClick = { viewModel.deleteVideoDraft(draft) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                            }
                        }
                        if (draft.description.isNotBlank()) {
                            Text(draft.description, color = MutedSlate, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(ActiveGrey).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("Beauty: ${draft.beautyLevel}%", color = NeonCyan, style = MaterialTheme.typography.labelSmall)
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(ActiveGrey).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("FX: ${draft.filterName}", color = NeonCherry, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- WALLET & MONETIZATION SCREEN ----------------
@Composable
fun WalletFinanceScreen(viewModel: PikPakViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val txs by viewModel.walletTransactions.collectAsState()

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawalDialog by remember { mutableStateOf(false) }

    // Withdrawal Form Values
    var withdrawAmount by remember { mutableStateOf("") }
    var withdrawChannel by remember { mutableStateOf("PayPal") }
    var accountIdOrEmail by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balances Header Frame
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepGrey),
            border = BorderStroke(1.5.dp, NeonCherry),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("PikPak Secure Crypto Wallet", color = MutedSlate, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Stars, "Coins", tint = NeonGold, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${formatDoubleCompact(profile?.balanceCoins ?: 0.0)} Coins",
                        color = WhitePure,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    )
                }

                // USD Estimate
                val usdVal = (profile?.balanceCoins ?: 0.0) * 0.01
                Text(
                    text = "≈ $${formatDoubleCompact(usdVal)} USD Equivalent",
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Wallet primary buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showDepositDialog = true },
                        modifier = Modifier.weight(1f).testTag("recharge_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCherry),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Payment, "Recharge")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recharge Coins")
                    }

                    Button(
                        onClick = { showWithdrawalDialog = true },
                        modifier = Modifier.weight(1f).testTag("withdraw_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Outbox, "Withdraw", tint = PremiumBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Instant Cash-out", color = PremiumBlack)
                    }
                }
            }
        }

        // Creator Rewards dashboard block
        Text("Creator rewards & Tokenomics Analytics", color = WhitePure, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepGrey),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Monthly Performance metrics", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(14.dp))

                // Grid Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Views (May)", color = MutedSlate, fontSize = 11.sp)
                        Text("1.25M views", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Watch Time", color = MutedSlate, fontSize = 11.sp)
                        Text("42.1K hrs", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ad Earnings", color = MutedSlate, fontSize = 11.sp)
                        Text("+$850.50", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = ActiveGrey)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Creator rewards program levels", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NeonGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EmojiEvents, "Gold Rewards", tint = NeonGold, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Partner Program Level 2", color = WhitePure, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Unlocked revenue distribution sharing", color = MutedSlate, fontSize = 10.sp)
                        }
                    }
                    Text("Eligible", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Ledger Transactions directory
        Text("Transaction Ledgers", color = WhitePure, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (txs.isEmpty()) {
            Text("No transactions logged inside local Room DB.", color = MutedSlate, fontSize = 12.sp)
        } else {
            txs.forEach { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepGrey),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (tx.type) {
                                            "RECHARGE" -> NeonGreen.copy(alpha = 0.15f)
                                            "WITHDRAWAL" -> Color.Red.copy(alpha = 0.15f)
                                            else -> NeonGold.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (tx.type) {
                                        "RECHARGE" -> Icons.Default.Add
                                        "WITHDRAWAL" -> Icons.Default.Remove
                                        else -> Icons.Default.CardGiftcard
                                    },
                                    contentDescription = "TxIcon",
                                    tint = when (tx.type) {
                                        "RECHARGE" -> NeonGreen
                                        "WITHDRAWAL" -> Color.Red
                                        else -> NeonGold
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = tx.description,
                                    color = WhitePure,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("Via: ${tx.methodOrGift}", color = MutedSlate, fontSize = 11.sp)
                            }
                        }

                        val isPositive = tx.amount > 0
                        Text(
                            text = "${if (isPositive) "+" else ""}${tx.amount.toInt()} C",
                            color = if (isPositive) NeonGreen else Color.Red,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Recharge package selection dialog
    if (showDepositDialog) {
        AlertDialog(
            onDismissRequest = { showDepositDialog = false },
            title = { Text("Buy Coins Packages", color = WhitePure, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Recharge coins package instantly via our secure merchant Stripe portal.", color = MutedSlate)
                    listOf(
                        Triple(100.0, 0.99, "Starter Kit"),
                        Triple(1000.0, 9.99, "Creator Standard"),
                        Triple(5000.0, 48.99, "Elite Gifter Bonus")
                    ).forEach { (coins, price, label) ->
                        Button(
                            onClick = {
                                viewModel.rechargeCoins(coins, price, "Stripe Portal")
                                showDepositDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ActiveGrey),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Stars, "Coins", tint = NeonGold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("${coins.toInt()} Coins", color = WhitePure, fontWeight = FontWeight.Bold)
                                        Text(label, color = MutedSlate, fontSize = 10.sp)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonCherry)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("$$price", color = WhitePure, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDepositDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = MutedSlate)) {
                    Text("Close")
                }
            },
            containerColor = DeepGrey
        )
    }

    // Cash withdrawal dialog
    if (showWithdrawalDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawalDialog = false },
            title = { Text("Secure Financial Cash-out", color = WhitePure, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Convert your Creator Fund tokens into USD currency instantly. Minimum cashout: 500 Coins ($5.00 USD).", color = MutedSlate)
                    
                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = { Text("Amount of Coins (e.g. 1000)", color = MutedSlate) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhitePure, unfocusedTextColor = WhitePure, focusedBorderColor = NeonCherry),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Withdrawal Gateway Provider", color = MutedSlate, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("PayPal", "Wise Transfer", "Payoneer", "Direct Bank Wire").forEach { path ->
                            val active = withdrawChannel == path
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (active) NeonCyan else ActiveGrey)
                                    .clickable { withdrawChannel = path }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(path, color = if (active) PremiumBlack else WhitePure, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = accountIdOrEmail,
                        onValueChange = { accountIdOrEmail = it },
                        label = { Text("Account Identifier Info / IBAN Email", color = MutedSlate) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhitePure, unfocusedTextColor = WhitePure, focusedBorderColor = NeonCherry),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountDouble = withdrawAmount.toDoubleOrNull()
                        if (amountDouble != null && amountDouble >= 500.0 && accountIdOrEmail.isNotBlank()) {
                            val ok = viewModel.withdrawFunds(
                                amountDouble,
                                amountDouble * 0.01,
                                withdrawChannel,
                                accountIdOrEmail
                            )
                            if (ok) {
                                showWithdrawalDialog = false
                                withdrawAmount = ""
                                accountIdOrEmail = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCherry)
                ) {
                    Text("Initiate Cash-out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawalDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = MutedSlate)) {
                    Text("Cancel")
                }
            },
            containerColor = DeepGrey
        )
    }
}

// ---------------- DIRECT CHAT / INBOX MESSAGES SCREEN ----------------
@Composable
fun DirectMessageScreen(viewModel: PikPakViewModel) {
    val messages by viewModel.currentChatMessages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll bottom when comments arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepGrey)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonCyan),
                contentAlignment = Alignment.Center
            ) {
                Text("CM", color = PremiumBlack, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Chef Marcus", color = WhitePure, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(NeonCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, "Ver", tint = PremiumBlack, modifier = Modifier.size(8.dp))
                    }
                }
                Text("Online • Host of Pastry Fusion Live", color = NeonCyan, fontSize = 11.sp)
            }
        }

        // Messages scrolling view
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isMyMessage = msg.sender == "Me"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMyMessage) 16.dp else 2.dp,
                                    bottomEnd = if (isMyMessage) 2.dp else 16.dp
                                )
                            )
                            .background(if (isMyMessage) NeonCherry else ActiveGrey)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .widthIn(max = 260.dp)
                    ) {
                        Column {
                            Text(msg.text, color = WhitePure, fontSize = 14.sp)
                            Text(
                                text = if (isMyMessage) "Read" else "Chef Marcus",
                                color = if (isMyMessage) WhitePure.copy(alpha = 0.6f) else MutedSlate,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = if (isMyMessage) TextAlign.End else TextAlign.Start
                            )
                        }
                    }
                }
            }
        }

        // Send Text Console
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepGrey)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.PhotoLibrary, "Send Pic", tint = MutedSlate)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Mic, "Send Voice", tint = MutedSlate)
            }
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Synchronize via WebSockets...", color = MutedSlate, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = WhitePure,
                    unfocusedTextColor = WhitePure,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = ActiveGrey,
                    unfocusedContainerColor = ActiveGrey
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("dm_input_field"),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendDirectMessage(messageText)
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
                    .testTag("dm_send_btn")
            ) {
                Icon(Icons.Default.Send, "Send DM", tint = PremiumBlack, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ---------------- ADMIN DASHBOARD MODERATION SCREEN ----------------
@Composable
fun AdminDashboardScreen(viewModel: PikPakViewModel) {
    val reports by viewModel.moderationReports.collectAsState()
    
    val aiMode by viewModel.aiModerationEnabled.collectAsState()
    val fakeDetector by viewModel.fakeAccountDetector.collectAsState()
    val copyrightScan by viewModel.copyrightScanner.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Administrative stats banner
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepGrey),
            border = BorderStroke(1.5.dp, NeonCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("PikPak Moderator Command Desk", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Operational status checks the artificial intelligence safety layers, reported media queue buffers, and creator copyright violations.", color = MutedSlate, fontSize = 12.sp)
            }
        }

        // Toggles safety
        Text("AI Safety Auto-Shields", color = WhitePure, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepGrey),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Neural AI Content Moderation", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Auto-quarantine obscene uploads", color = MutedSlate, fontSize = 11.sp)
                    }
                    Switch(checked = aiMode, onCheckedChange = { viewModel.toggleAiModeration() }, colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan))
                }

                Divider(color = ActiveGrey)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dynamic Fake Account Detector", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Anti-sybil fake bot mitigation", color = MutedSlate, fontSize = 11.sp)
                    }
                    Switch(checked = fakeDetector, onCheckedChange = { viewModel.toggleFakeAccountDetector() }, colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan))
                }

                Divider(color = ActiveGrey)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Automated Audio Copyright Checker", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Silence unauthorized music claims", color = MutedSlate, fontSize = 11.sp)
                    }
                    Switch(checked = copyrightScan, onCheckedChange = { viewModel.toggleCopyrightScanner() }, colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan))
                }
            }
        }

        // Active Reports Lists
        Text("Active Media Escalation Queue (${reports.filter { !it.isResolved }.size})", color = WhitePure, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        reports.forEach { report ->
            AnimatedVisibility(!report.isResolved) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepGrey),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Reporter: ${report.creatorHandle}", color = NeonCherry, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(report.timestamp, color = MutedSlate, fontSize = 11.sp)
                        }
                        Text("Violation Flag: ${report.reason}", color = WhitePure, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                        Text("Cumulative Reports: ${report.reportsCount} hits", color = MutedSlate, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { viewModel.resolveReport(report.id, false) }) {
                                Text("Acknowledge & Safe", color = NeonCyan)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.resolveReport(report.id, true) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCherry)
                            ) {
                                Text("Ban & Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TECHNICAL BLUEPRINTS HUB (DOCS) ----------------
@Composable
fun TechnicalBlueprintScreen() {
    var selectedPanel by remember { mutableStateOf("SCHEMA") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Hub Category Selection Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepGrey)
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "SCHEMA" to "SQLite/Postgres Schema",
                "API" to "REST Router Code",
                "FLUTTER" to "Flutter Code Structure",
                "NODE" to "WebSocket Engine",
                "DEPLOYS" to "App Store Checklist"
            ).forEach { (code, title) ->
                val active = selectedPanel == code
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) NeonCherry else ActiveGrey)
                        .clickable { selectedPanel = code }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(title, color = WhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Schema content details
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(PremiumBlack)
        ) {
            when (selectedPanel) {
                "SCHEMA" -> SchemaViewDocs()
                "API" -> NodeApiRoutingDocs()
                "FLUTTER" -> FlutterStructureDocs()
                "NODE" -> NodeWebSocketDocs()
                "DEPLOYS" -> StoreDeploysDocs()
            }
        }
    }
}

@Composable
fun SchemaViewDocs() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PikPak Full Schema Relational Blueprints", color = NeonCyan, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Production ready postgres schemas optimized for high speed read/writes on AWS Aurora:", color = MutedSlate, fontSize = 12.sp)

        CodeBox(
            code = """
-- 1. USERS PROFILE TABLE
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(30) UNIQUE NOT NULL,
    display_name VARCHAR(60) NOT NULL,
    bio TEXT,
    avatar_url VARCHAR(255),
    cover_url VARCHAR(255),
    is_verified BOOLEAN DEFAULT FALSE,
    coins_balance DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. VIDEOS TABLE (With HD/4K tags & indexes)
CREATE TABLE videos (
    id SERIAL PRIMARY KEY,
    owner_id VARCHAR(50) REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150),
    description TEXT,
    tags TEXT[],
    resolution VARCHAR(10) DEFAULT '1080p',
    duration INT NOT NULL,
    primary_color VARCHAR(6),
    secondary_color VARCHAR(6),
    likes_count INT DEFAULT 0,
    comments_count INT DEFAULT 0,
    shares_count INT DEFAULT 0,
    saves_count INT DEFAULT 0,
    reposts_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_videos_owner ON videos(owner_id);
CREATE INDEX idx_videos_created ON videos(created_at DESC);

-- 3. WALLET TRANSACTION SECURE LEDGER
CREATE TABLE wallet_transactions (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) REFERENCES users(id) ON DELETE CASCADE,
    transaction_type VARCHAR(20) NOT NULL, -- RECHARGE, WITHDRAWAL, GIFT
    coin_amount DOUBLE PRECISION NOT NULL,
    payout_dollar_value DOUBLE PRECISION DEFAULT 0.0,
    payment_method VARCHAR(50) NOT NULL, -- PayPal, Stripe, Wise
    recipient_identifier VARCHAR(100),
    status VARCHAR(15) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
            """.trimIndent()
        )
    }
}

@Composable
fun NodeApiRoutingDocs() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PikPak REST APIs Router Specification", color = NeonCyan, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Expedited endpoints written in Fastify / Node Express supporting video uploads and user coin ledger syncing:", color = MutedSlate, fontSize = 12.sp)

        CodeBox(
            code = """
const express = require('express');
const router = express.Router();
const { authMiddleware } = require('../middlewares/auth');
const { db } = require('../services/db');

// --- 1. INFINITE STREAM RECOMMENDATIONS ---
router.get('/feed/for-you', authMiddleware, async (req, res) => {
    try {
        const userId = req.user.id;
        // AI model recommendation weights feed query
        const videos = await db.query(`
            SELECT v.*, u.username, u.is_verified, u.avatar_url,
            EXISTS(SELECT 1 FROM likes WHERE video_id = v.id AND user_id = ${'$'}1) AS is_liked
            FROM videos v
            JOIN users u ON v.owner_id = u.id
            ORDER BY RANDOM() LIMIT 10
        `, [userId]);
        res.json({ success: true, count: videos.length, data: videos });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
});

// --- 2. INSTANT LEDGER WALLET CASH WITHDRAWAL ---
router.post('/wallet/withdraw', authMiddleware, async (req, res) => {
    const { amountCoins, gateway, accountInfo } = req.body;
    const userId = req.user.id;
    if (amountCoins < 500) {
        return res.status(400).json({ error: 'Minimum cashout boundary is 500 coins' });
    }
    
    const client = await db.pool.connect();
    try {
        await client.query('BEGIN');
        const userBal = await client.query('SELECT coins_balance FROM users WHERE id = ${'$'}1 FOR UPDATE', [userId]);
        if (userBal.rows[0].coins_balance < amountCoins) {
            throw new Error('Insufficient wallet funds');
        }
        
        await client.query('UPDATE users SET coins_balance = coins_balance - ${'$'}1 WHERE id = ${'$'}2', [amountCoins, userId]);
        await client.query(`
            INSERT INTO wallet_transactions(user_id, transaction_type, coin_amount, payout_dollar_value, payment_method, recipient_identifier)
            VALUES(${'$'}1, 'WITHDRAWAL', -${'$'}2, ${'$'}3, ${'$'}4, ${'$'}5)
        `, [userId, amountCoins, amountCoins * 0.01, gateway, accountInfo]);
        
        await client.query('COMMIT');
        res.json({ success: true, message: 'Withdrawal initiated successfully for approval audit' });
    } catch (e) {
        await client.query('ROLLBACK');
        res.status(400).json({ success: false, error: e.message });
    } finally {
        client.release();
    }
});
            """.trimIndent()
        )
    }
}

@Composable
fun FlutterStructureDocs() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PikPak Full Flutter Frontend Architecture", color = NeonCyan, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Complete Source Tree and main feed consumer widget tree layout matching our precise Compose design:", color = MutedSlate, fontSize = 12.sp)

        CodeBox(
            code = """
lib/
├── main.dart                 # Application boots, runs MaterialApp
├── models/
│   ├── video.dart            # JSON serializers for Feed videos
│   ├── wallet.dart           # Transaction records models
│   └── chat.dart             # Message containers models
├── providers/
│   ├── feed_provider.dart    # Infinite scroll pagination system
│   └── wallet_provider.dart  # Tracks current coin balances state
├── screens/
│   ├── feed_screen.dart      # Vertical PageView for reels
│   ├── studio_screen.dart    # Camera filter inputs
│   ├── wallet_screen.dart    # Transaction ledgers
│   └── chat_screen.dart      # Real-time WebSocket terminal
└── widgets/
    ├── coin_package_button.dart
    └── comments_drawer.dart

--- Feed Dynamic PageView Snippet ---
class ForYouReelsView extends StatefulWidget {
  @override
  _ForYouReelsViewState createState() => _ForYouReelsViewState();
}

class _ForYouReelsViewState extends State<ForYouReelsView> {
  late PageController _pageController;
  int _activeIdx = 0;

  @override
  void initState() {
    super.initState();
    _pageController = PageController();
  }

  @override
  Widget build(BuildContext context) {
    return PageView.builder(
      scrollDirection: Axis.vertical,
      controller: _pageController,
      onPageChanged: (index) => setState(() => _activeIdx = index),
      itemBuilder: (context, index) {
        final video = mockVideos[index];
        return VideoPlayerScreen(video: video);
      },
    );
  }
}
            """.trimIndent()
        )
    }
}

@Composable
fun NodeWebSocketDocs() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PikPak Real-Time WebSockets Sync Logic", color = NeonCyan, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Scalable Node logic for instant commenting notifications and live video streams interaction packets:", color = MutedSlate, fontSize = 12.sp)

        CodeBox(
            code = """
const WebSocket = require('ws');
const wss = new WebSocket.Server({ port: 8080 });

const activeConnections = new Map(); // userId -> Connection socket

wss.on('connection', (ws, req) => {
    // Authenticate token link
    const urlParams = new URLSearchParams(req.url.split('?')[1]);
    const userId = urlParams.get('userId');
    if (userId) {
        activeConnections.set(userId, ws);
    }

    ws.on('message', (message) => {
        try {
            const data = JSON.parse(message);
            switch(data.type) {
                case 'SEND_DIRECT_MESSAGE':
                    handleDirectMessageSockets(userId, data.payload);
                    break;
                case 'SEND_LIVE_GIFT':
                    circulateLiveGift(userId, data.payload);
                    break;
            }
        } catch (err) {
            ws.send(JSON.stringify({ error: 'Payload must be serialized JSON' }));
        }
    });

    ws.on('close', () => {
        activeConnections.delete(userId);
    });
});

function handleDirectMessageSockets(senderId, payload) {
    const { recipientId, text } = payload;
    const client = activeConnections.get(recipientId);
    if (client && client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify({
            type: 'INCOMING_DM',
            senderId,
            text,
            timestamp: Date.now()
        }));
    }
}
            """.trimIndent()
        )
    }
}

@Composable
fun StoreDeploysDocs() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Play Store & App Store Live Deployment Guide", color = NeonCyan, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Follow this comprehensive checklist to release PikPak production builds globally:", color = MutedSlate, fontSize = 12.sp)

        Card(
            colors = CardDefaults.cardColors(containerColor = DeepGrey),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🤖 Google Play Store Release Checklist:", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                listOf(
                    "1. Set up a secure custom Google Play Developer Account.",
                    "2. Inject dynamic App Icon PNG sizes into hdpi/xxhdpi directories using guidelines.",
                    "3. Configure targetSdkVersion to match minimum Play criteria (targetSdk = 36).",
                    "4. Create unique keystores using keytool matching systems.",
                    "5. Build release AAB using: 'gradle bundleRelease'.",
                    "6. Fill out content safety forms confirming AI Moderation is actively active.",
                    "7. Launch internal testing track first with 20 registered testers."
                ).forEach { item ->
                    Text(item, color = MutedSlate, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = ActiveGrey)
                Spacer(modifier = Modifier.height(8.dp))

                Text("🍏 Apple App Store Release Checklist:", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                listOf(
                    "1. Secure Apple Developer Membership credentials.",
                    "2. Adjust bundle identifiers and configure App Groups keys.",
                    "3. Provision certificates and download safe provisioning profiles.",
                    "4. Ensure camera, microphone, photo attachments usages descriptions exist in Info.plist.",
                    "5. Compile archive inside Xcode and perform App Store validation audits.",
                    "6. Configure App Store In-App Purchases (IAP) to map the 'PikPak Coins Package' levels.",
                    "7. Verify dynamic Dark Mode conforms to human interface style parameters."
                ).forEach { item ->
                    Text(item, color = MutedSlate, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CodeBox(code: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PremiumBlack),
        border = BorderStroke(1.dp, ActiveGrey),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                color = WhitePure,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}

// ---------------- MATHEMATICAL COMPACT FORMATTERS ----------------
private fun formatIntCompact(num: Int): String {
    return when {
        num >= 1_000_000 -> String.format("%.1fM", num.toDouble() / 1_000_000)
        num >= 1_000 -> String.format("%.1fK", num.toDouble() / 1_000)
        else -> num.toString()
    }
}

private fun formatDoubleCompact(num: Double): String {
    return when {
        num >= 1_000_000 -> String.format("%.2fM", num / 1_000_000)
        num >= 1_000 -> String.format("%.2fK", num / 1_000)
        else -> String.format("%.2f", num)
    }
}
