package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VideoItem(
    val id: Int,
    val creatorName: String,
    val creatorHandle: String,
    val creatorAvatar: String,
    val isVerified: Boolean = false,
    val description: String,
    val tags: String,
    val songTitle: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val isLive: Boolean = false,
    var likesCount: Int,
    var commentsCount: Int,
    var sharesCount: Int,
    var savesCount: Int,
    var repostsCount: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isReposted: Boolean = false,
    val category: String = "Trending"
)

data class VideoComment(
    val id: Int,
    val videoId: Int,
    val author: String,
    val avatarColor: String,
    val content: String,
    var likes: Int,
    val isPinned: Boolean = false,
    var likedByUser: Boolean = false,
    val timestamp: String = "2h ago"
)

data class AdCampaign(
    val id: Int,
    val campaignName: String,
    val adType: String, // "In-Feed Video", "Brand Takeover", "Sponsored Tag"
    val budget: Double,
    val targetCPA: Double,
    val impressions: Int,
    val clicks: Int,
    val status: String // "Active", "Pending", "Completed"
)

data class ReportItem(
    val id: Int,
    val videoId: Int,
    val creatorHandle: String,
    val reason: String,
    val reportsCount: Int,
    val timestamp: String,
    var isResolved: Boolean = false
)

class PikPakViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PikPakDatabase.getDatabase(application)
    private val repository = PikPakRepository(db)

    // Room Persistent States
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val draftVideos: StateFlow<List<DraftVideo>> = repository.allDrafts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletTransactions: StateFlow<List<WalletTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Interactive States
    private val _videoFeeds = MutableStateFlow<List<VideoItem>>(emptyList())
    val videoFeeds: StateFlow<List<VideoItem>> = _videoFeeds.asStateFlow()

    private val _comments = MutableStateFlow<List<VideoComment>>(emptyList())
    val comments: StateFlow<List<VideoComment>> = _comments.asStateFlow()

    private val _adCampaigns = MutableStateFlow<List<AdCampaign>>(emptyList())
    val adCampaigns: StateFlow<List<AdCampaign>> = _adCampaigns.asStateFlow()

    private val _moderationReports = MutableStateFlow<List<ReportItem>>(emptyList())
    val moderationReports: StateFlow<List<ReportItem>> = _moderationReports.asStateFlow()

    // Active Messaging State
    private val _currentChatId = MutableStateFlow("chef_marcus")
    val currentChatId: StateFlow<String> = _currentChatId.asStateFlow()

    val currentChatMessages: StateFlow<List<ChatMessage>> = _currentChatId
        .flatMapLatest { chatId -> repository.getChatMessages(chatId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin & Moderation Config Options
    private val _aiModerationEnabled = MutableStateFlow(true)
    val aiModerationEnabled = _aiModerationEnabled.asStateFlow()

    private val _fakeAccountDetector = MutableStateFlow(true)
    val fakeAccountDetector = _fakeAccountDetector.asStateFlow()

    private val _copyrightScanner = MutableStateFlow(true)
    val copyrightScanner = _copyrightScanner.asStateFlow()

    // Creator Stats Simulation
    private val _creatorViewsCount = MutableStateFlow(1248500)
    val creatorViewsCount = _creatorViewsCount.asStateFlow()

    private val _creatorFollowersGained = MutableStateFlow(8430)
    val creatorFollowersGained = _creatorFollowersGained.asStateFlow()

    private val _creatorWatchTime = MutableStateFlow(42100.5) // in hours
    val creatorWatchTime = _creatorWatchTime.asStateFlow()

    init {
        // Hydrate default user profiles, chat messages, and transactions
        viewModelScope.launch {
            // 1. Initial User
            val initialProfile = UserProfile(
                id = "me",
                username = "pikpak_star",
                displayName = "Emily Roberts",
                bio = "Short-form filmmaker & lifestyle Creator 🎞️✨\nDream big, dance often!\nBased in LA 🌴",
                followers = 1420500,
                following = 384,
                likes = 28900400,
                isVerified = true,
                creatorCategory = "Dancer & Filmmaker",
                balanceCoins = 3850.0,
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
                coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&q=80&w=800"
            )
            repository.saveProfile(initialProfile)

            // 2. Chat Seed Messages
            repository.sendMessage(ChatMessage(chatId = "chef_marcus", sender = "Creator", text = "Hey Emily! Loved your recipe duet! We should collaborate on a baking challenge next week."))
            repository.sendMessage(ChatMessage(chatId = "chef_marcus", sender = "Me", text = "Omg Marcus! Thank you so much! I'm absolutely down. Are we thinking cakes or cookies?"))
            repository.sendMessage(ChatMessage(chatId = "chef_marcus", sender = "Creator", text = "Definitely premium French pastries! Let's schedule it."))

            // 3. Transactions Seed
            repository.addTransaction(WalletTransaction(type = "RECHARGE", amount = 1000.0, methodOrGift = "Stripe", description = "Acquired 1,000 Coins Package"))
            repository.addTransaction(WalletTransaction(type = "GIFT_SENT", amount = -120.0, methodOrGift = "Super Dragon Sparkle", description = "Sent Live Gift to @chef_marcus"))
            repository.addTransaction(WalletTransaction(type = "CREATOR_PAYOUT", amount = 850.50, methodOrGift = "PayPal", description = "Creator Revenue distribution deposit"))
        }

        // Initialize Feed Videos
        _videoFeeds.value = listOf(
            VideoItem(
                id = 1,
                creatorName = "Chef Marcus",
                creatorHandle = "chef_marcus",
                creatorAvatar = "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?auto=format&fit=crop&q=80&w=200",
                isVerified = true,
                description = "Unlocking the secrete to the perfect 60-second chocolate lava cup 🍫🤤! Save this recipe immediately!",
                tags = "#baking #chocolate #lava #cooking #yummy #viral",
                songTitle = "Marcus' Culinary Beats - Original Sound",
                primaryColorHex = "FF512F",
                secondaryColorHex = "DD2476",
                isLive = false,
                likesCount = 824300,
                commentsCount = 4210,
                sharesCount = 9840,
                savesCount = 35200,
                repostsCount = 1420
            ),
            VideoItem(
                id = 2,
                creatorName = "Aria Symphony",
                creatorHandle = "aria_sounds",
                creatorAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&q=80&w=200",
                isVerified = true,
                description = "Singing 'La Vie En Rose' inside an echo chamber with deep reverb synth 🎙️🎷. Grab headphones!",
                tags = "#singing #coversong #music #acoustic #vibes #echolove",
                songTitle = "Symphonic Echo Reverb Session Vol 4",
                primaryColorHex = "1A2A6C",
                secondaryColorHex = "F27121",
                isLive = true, // Shows Live Pulsing Tag
                likesCount = 1205300,
                commentsCount = 9520,
                sharesCount = 48102,
                savesCount = 94800,
                repostsCount = 6720
            ),
            VideoItem(
                id = 3,
                creatorName = "Pixel Architect",
                creatorHandle = "code_canvas",
                creatorAvatar = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&q=80&w=200",
                isVerified = false,
                description = "How I designed a fully operational short video engine using Jetpack Compose, Canvas graphics, and Room databases in less than 48 hours! 🚀💻 Code schema shown in Hub tab!",
                tags = "#development #kotlin #android #uidesign #programmer #pikpak",
                songTitle = "SynthWave Retro Cyber - TechLab Tracks",
                primaryColorHex = "00F260",
                secondaryColorHex = "0575E6",
                likesCount = 42800,
                commentsCount = 310,
                sharesCount = 1200,
                savesCount = 8900,
                repostsCount = 230
            ),
            VideoItem(
                id = 4,
                creatorName = "Earth Odyssey",
                creatorHandle = "planet_wild",
                creatorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200",
                isVerified = true,
                description = "The absolute hidden jewel of Iceland: a turquoise hot water springs embedded in obsidian volcanic sand lines 🌋🌿.",
                tags = "#travel #iceland #nature #adventure #hotspring #earth",
                songTitle = "Soothing Flute of the Frozen Valleys",
                primaryColorHex = "11998e",
                secondaryColorHex = "38ef7d",
                likesCount = 943600,
                commentsCount = 2410,
                sharesCount = 20110,
                savesCount = 47800,
                repostsCount = 4120
            )
        )

        // Populate Mock comments for Chef Marcus (VideoId=1)
        _comments.value = listOf(
            VideoComment(1, 1, "@gourmet_gal", "4CAF50", "I tried this and literally burned my kitchen down BUT the core of the chocolate was absolute perfection! 😂😋", 1432, isPinned = true),
            VideoComment(2, 1, "@chef_alex", "FF9800", "Pro tip: add a pinch of espresso powder to the batter. It enhances the cocoa profiles intensely! Great video Marcus.", 821, isPinned = false),
            VideoComment(3, 1, "@sweet_tooth_em", "E91E63", "Waiting for my oven to preheat right now! This video is dangerous for my late-night cravings.", 450, isPinned = false),
            VideoComment(4, 1, "@baking_nerd", "00BCD4", "Can we replace butter with coconut oil? Need a vegan adaptation please!", 112, isPinned = false)
        )

        // Set up mock Ad campaigns
        _adCampaigns.value = listOf(
            AdCampaign(1, "Nike Air Zoom Video Launch", "In-Feed Video", 7500.0, 0.45, 120400, 18450, "Active"),
            AdCampaign(2, "Duolingo Green Owl Takeover", "Brand Takeover", 15000.0, 1.20, 310200, 48300, "Active"),
            AdCampaign(3, "Xbox GamePass Tag Campaign", "Sponsored Tag", 4200.0, 0.35, 78400, 10230, "Completed")
        )

        // Set up mock moderation reports
        _moderationReports.value = listOf(
            ReportItem(1, 4, "@toxic_scrapper", "Harassment & Hate Speech in comments", 12, "20 mins ago"),
            ReportItem(2, 2, "@pirate_streamer", "Infringing copyright audio materials", 8, "45 mins ago"),
            ReportItem(3, 1, "@bot_pumper", "Automated engagement spam activity", 35, "1 hour ago")
        )
    }

    // Interactive functions
    fun toggleLikeVideo(videoId: Int) {
        _videoFeeds.update { list ->
            list.map { video ->
                if (video.id == videoId) {
                    val previouslyLiked = video.isLiked
                    val newCount = if (previouslyLiked) video.likesCount - 1 else video.likesCount + 1
                    video.copy(isLiked = !previouslyLiked, likesCount = newCount)
                } else video
            }
        }
    }

    fun toggleSaveVideo(videoId: Int) {
        _videoFeeds.update { list ->
            list.map { video ->
                if (video.id == videoId) {
                    val previouslySaved = video.isSaved
                    val newCount = if (previouslySaved) video.savesCount - 1 else video.savesCount + 1
                    video.copy(isSaved = !previouslySaved, savesCount = newCount)
                } else video
            }
        }
    }

    fun toggleRepostVideo(videoId: Int) {
        _videoFeeds.update { list ->
            list.map { video ->
                if (video.id == videoId) {
                    val previouslyReposted = video.isReposted
                    val newCount = if (previouslyReposted) video.repostsCount - 1 else video.repostsCount + 1
                    video.copy(isReposted = !previouslyReposted, repostsCount = newCount)
                } else video
            }
        }
    }

    fun addVideoComment(videoId: Int, author: String, text: String) {
        val newComment = VideoComment(
            id = (_comments.value.maxOfOrNull { it.id } ?: 0) + 1,
            videoId = videoId,
            author = author,
            avatarColor = "FF2C55",
            content = text,
            likes = 0,
            timestamp = "Just now"
        )
        _comments.update { listOf(newComment) + it }

        // Increment video comment counters
        _videoFeeds.update { list ->
            list.map { video ->
                if (video.id == videoId) {
                    video.copy(commentsCount = video.commentsCount + 1)
                } else video
            }
        }
    }

    fun toggleLikeComment(commentId: Int) {
        _comments.update { list ->
            list.map { comment ->
                if (comment.id == commentId) {
                    val liked = comment.likedByUser
                    val dLike = if (liked) -1 else 1
                    comment.copy(likedByUser = !liked, likes = comment.likes + dLike)
                } else comment
            }
        }
    }

    // Wallet System recharge & Withdrawal
    fun rechargeCoins(amountCoins: Double, payAmount: Double, method: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val updatedCoins = profile.balanceCoins + amountCoins

            // update profile coins
            repository.updateProfileCoins(updatedCoins)
            repository.saveProfile(profile.copy(balanceCoins = updatedCoins))

            // log transaction
            val rechargeTx = WalletTransaction(
                type = "RECHARGE",
                amount = amountCoins,
                methodOrGift = method,
                description = "Purchased $amountCoins Coins package via $method ($$payAmount)"
            )
            repository.addTransaction(rechargeTx)
        }
    }

    fun withdrawFunds(withdrawAmountCoins: Double, convertedValue: Double, method: String, emailOrBank: String) : Boolean {
        val currentCoins = userProfile.value?.balanceCoins ?: 0.0
        if (currentCoins < withdrawAmountCoins) {
            return false // insufficient funds
        }

        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val updatedCoins = profile.balanceCoins - withdrawAmountCoins

            repository.updateProfileCoins(updatedCoins)
            repository.saveProfile(profile.copy(balanceCoins = updatedCoins))

            // log transaction
            val withdrawTx = WalletTransaction(
                type = "WITHDRAWAL",
                amount = -withdrawAmountCoins,
                methodOrGift = method,
                description = "Withdrew $$convertedValue USD ($withdrawAmountCoins Coins) to $method ($emailOrBank)"
            )
            repository.addTransaction(withdrawTx)
        }
        return true
    }

    // Direct Messaging with Simulated Server WebSocket Replies
    fun sendDirectMessage(text: String) {
        if (text.isBlank()) return
        val activeChat = _currentChatId.value

        viewModelScope.launch {
            // 1. Send user message
            val userMsg = ChatMessage(
                chatId = activeChat,
                sender = "Me",
                text = text
            )
            repository.sendMessage(userMsg)

            // 2. Mock Server WebSocket Auto Reply after 1 second
            kotlinx.coroutines.delay(1000)
            val replyText = when {
                text.contains("collab", ignoreCase = true) -> "That baking challenge sounds incredible! Let's build a recipe draft right now inside our Studio editor."
                text.contains("hi", ignoreCase = true) || text.contains("hello", ignoreCase = true) -> "Hello Emily! Ready to check our joint followers analytics today?"
                text.contains("how", ignoreCase = true) -> "I'm doing awesome! Polishing a brand promotion for Nike on my creator dashboard. Let me know what you think!"
                text.contains("coins", ignoreCase = true) || text.contains("gift", ignoreCase = true) -> "OMG thank you! Live gifts are converted directly into user coins, which can be withdrawn securely via Paypal/Stripe!"
                else -> "Got it! Real-time WebSockets keep this message session perfectly synchronized with our PikPak Android node servers. 🌐⚡"
            }

            val botMsg = ChatMessage(
                chatId = activeChat,
                sender = "Creator",
                text = replyText
            )
            repository.sendMessage(botMsg)
        }
    }

    fun selectActiveChat(chatId: String) {
        _currentChatId.value = chatId
    }

    // Ad campaigns creation
    fun createAdCampaign(name: String, type: String, budget: Double, cpa: Double) {
        val newCampaign = AdCampaign(
            id = _adCampaigns.value.size + 1,
            campaignName = name,
            adType = type,
            budget = budget,
            targetCPA = cpa,
            impressions = 0,
            clicks = 0,
            status = "Pending Setup"
        )
        _adCampaigns.update { it + newCampaign }
    }

    // Drafting save video system
    fun saveVideoDraft(title: String, desc: String, tags: String, resolution: String, duration: Int, filter: String, beauty: Int, location: String) {
        viewModelScope.launch {
            val draft = DraftVideo(
                title = title,
                description = desc,
                tags = tags,
                resolution = resolution,
                duration = duration,
                filterName = filter,
                beautyLevel = beauty,
                location = location
            )
            repository.addDraft(draft)
        }
    }

    fun deleteVideoDraft(draft: DraftVideo) {
        viewModelScope.launch {
            repository.deleteDraft(draft)
        }
    }

    // Safe Moderation Action
    fun resolveReport(reportId: Int, removeContent: Boolean) {
        _moderationReports.update { list ->
            list.map { report ->
                if (report.id == reportId) {
                    report.copy(isResolved = true)
                } else report
            }
        }
        if (removeContent) {
            // Simulated removal action message
        }
    }

    fun toggleAiModeration() {
        _aiModerationEnabled.value = !_aiModerationEnabled.value
    }

    fun toggleFakeAccountDetector() {
        _fakeAccountDetector.value = !_fakeAccountDetector.value
    }

    fun toggleCopyrightScanner() {
        _copyrightScanner.value = !_copyrightScanner.value
    }
}
