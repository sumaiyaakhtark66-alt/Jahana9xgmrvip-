package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.MatchRecordEntity
import com.example.data.local.PerkItemEntity
import com.example.data.local.PlayerProfile
import com.example.data.local.TournamentEntryEntity
import com.example.data.repository.PerkActionResult
import com.example.data.repository.VipRepository
import com.example.domain.BaseContenders
import com.example.domain.HallOfFameContender
import com.example.domain.VipGameId
import com.example.domain.VipTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VipUiState(
    val isLoading: Boolean = true,
    val profile: PlayerProfile = PlayerProfile(),
    val perks: List<PerkItemEntity> = emptyList(),
    val tournaments: List<TournamentEntryEntity> = emptyList(),
    val recentMatches: List<MatchRecordEntity> = emptyList(),
    val bestScores: Map<String, Int> = emptyMap(),
    val totalMatches: Int = 0,
    val isDailyVaultAvailable: Boolean = true,
    val leaderboard: List<HallOfFameContender> = emptyList(),
    val statusMessage: String? = null
)

class VipViewModel(
    private val repository: VipRepository
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private data class CoreSnapshot(
        val profile: PlayerProfile,
        val perks: List<PerkItemEntity>,
        val tournaments: List<TournamentEntryEntity>,
        val recentMatches: List<MatchRecordEntity>,
        val bestScores: Map<String, Int>
    )

    private val coreSnapshotFlow = combine(
        repository.profileFlow,
        repository.perksFlow,
        repository.tournamentsFlow,
        repository.recentMatchesFlow,
        repository.bestScoresFlow
    ) { profile, perks, tournaments, matches, bestScores ->
        CoreSnapshot(profile, perks, tournaments, matches, bestScores)
    }

    val uiState: StateFlow<VipUiState> = combine(
        coreSnapshotFlow,
        repository.totalMatchesFlow,
        _statusMessage
    ) { core, totalMatches, message ->
        val todayEpochDay = currentEpochDay()
        val dailyAvailable = core.profile.lastDailyClaimEpochDay < todayEpochDay

        val mergedLeaderboard = (BaseContenders + HallOfFameContender(
            rank = 0,
            gamerTag = core.profile.gamerTag,
            title = core.profile.equippedTitle,
            vipXp = core.profile.vipXp,
            isCurrentUser = true
        )).sortedByDescending { it.vipXp }
            .mapIndexed { index, contender -> contender.copy(rank = index + 1) }

        VipUiState(
            isLoading = false,
            profile = core.profile,
            perks = core.perks,
            tournaments = core.tournaments,
            recentMatches = core.recentMatches,
            bestScores = core.bestScores,
            totalMatches = totalMatches,
            isDailyVaultAvailable = dailyAvailable,
            leaderboard = mergedLeaderboard,
            statusMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = VipUiState()
    )

    fun claimDailyVault() {
        viewModelScope.launch {
            val claimed = repository.claimDailyVault(currentEpochDay())
            _statusMessage.value = if (claimed) {
                "Daily Royal Vault claimed: +250 Crowns & +120 VIP XP!"
            } else {
                "Daily Vault already claimed today. Return tomorrow!"
            }
        }
    }

    fun recordMatchResult(
        gameId: VipGameId,
        rawScore: Int,
        baseCrowns: Int,
        baseXp: Int
    ) {
        viewModelScope.launch {
            val currentTier = uiState.value.profile.tier
            val multiplier = when (currentTier) {
                VipTier.OBSIDIAN_INITIATE -> 1.0f
                VipTier.AMETHYST_ELITE -> 1.25f
                VipTier.IMPERIAL_GOLD -> 1.5f
                VipTier.A9X_SOVEREIGN -> 2.0f
            }
            val finalXp = (baseXp * multiplier).toInt().coerceAtLeast(10)
            repository.recordMatchResult(
                gameId = gameId,
                score = rawScore,
                crownsEarned = baseCrowns,
                xpEarned = finalXp
            )
            _statusMessage.value = "+$baseCrowns Crowns & +$finalXp VIP XP banked from ${gameId.title}!"
        }
    }

    fun unlockOrEquipPerk(perkId: String) {
        viewModelScope.launch {
            when (val result = repository.unlockOrEquipPerk(perkId)) {
                is PerkActionResult.UnlockedAndEquipped -> {
                    _statusMessage.value = "Unlocked & equipped ${result.perkName}!"
                }
                is PerkActionResult.Equipped -> {
                    _statusMessage.value = "Equipped ${result.perkName} on your VIP Pass."
                }
                is PerkActionResult.InsufficientCrowns -> {
                    _statusMessage.value = "Need ${result.required} Crowns — play Arena games or claim Daily Vault!"
                }
                PerkActionResult.NotFound -> {
                    _statusMessage.value = "Perk not found."
                }
            }
        }
    }

    fun toggleTournament(tournamentId: String) {
        viewModelScope.launch {
            val joined = repository.toggleTournamentRegistration(tournamentId)
            if (joined != null) {
                _statusMessage.value = if (joined) {
                    "Entered VIP Tournament bracket! Play its Arena challenge to climb."
                } else {
                    "Withdrew from tournament bracket."
                }
            }
        }
    }

    fun updateGamerProfile(gamerTag: String, avatarIndex: Int) {
        viewModelScope.launch {
            repository.updateGamerProfile(gamerTag, avatarIndex)
            _statusMessage.value = "VIP Identity updated!"
        }
    }

    fun clearMatchHistory() {
        viewModelScope.launch {
            repository.clearMatchHistory()
            _statusMessage.value = "Match ledger cleared."
        }
    }

    fun consumeStatusMessage() {
        _statusMessage.value = null
    }

    private fun currentEpochDay(): Long = System.currentTimeMillis() / 86_400_000L

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                VipViewModel(VipRepository(app.applicationContext))
            }
        }
    }
}
