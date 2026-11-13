package com.example.data.repository

import android.content.Context
import com.example.data.local.MatchRecordEntity
import com.example.data.local.PerkItemEntity
import com.example.data.local.PlayerPreferences
import com.example.data.local.PlayerProfile
import com.example.data.local.TournamentEntryEntity
import com.example.data.local.VipDao
import com.example.data.local.VipDatabase
import com.example.domain.CardAuraStyle
import com.example.domain.VipGameId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VipRepository(
    private val dao: VipDao,
    private val preferences: PlayerPreferences
) {
    constructor(context: Context) : this(
        dao = VipDatabase.getInstance(context).vipDao(),
        preferences = PlayerPreferences(context.applicationContext)
    )

    val profileFlow: Flow<PlayerProfile> = preferences.profileFlow
    val perksFlow: Flow<List<PerkItemEntity>> = dao.observePerks()
    val tournamentsFlow: Flow<List<TournamentEntryEntity>> = dao.observeTournaments()
    val recentMatchesFlow: Flow<List<MatchRecordEntity>> = dao.observeRecentMatches()
    val totalMatchesFlow: Flow<Int> = dao.observeTotalMatches()

    val bestScoresFlow: Flow<Map<String, Int>> = dao.observeBestScores().map { list ->
        list.associate { it.gameId to it.bestScore }
    }

    suspend fun ensureSeeded() {
        if (dao.countPerks() == 0) {
            dao.insertPerks(defaultPerks())
        }
        if (dao.countTournaments() == 0) {
            dao.insertTournaments(defaultTournaments())
        }
    }

    suspend fun claimDailyVault(todayEpochDay: Long): Boolean {
        return preferences.claimDailyVault(
            todayEpochDay = todayEpochDay,
            crownsBonus = 250,
            xpBonus = 120
        )
    }

    suspend fun recordMatchResult(
        gameId: VipGameId,
        score: Int,
        crownsEarned: Int,
        xpEarned: Int
    ) {
        dao.insertMatchRecord(
            MatchRecordEntity(
                gameId = gameId.id,
                gameTitle = gameId.title,
                score = score,
                crownsEarned = crownsEarned,
                xpEarned = xpEarned
            )
        )
        preferences.addRewards(crownsDelta = crownsEarned, xpDelta = xpEarned)
    }

    suspend fun unlockOrEquipPerk(perkId: String): PerkActionResult {
        val perk = dao.getPerkById(perkId) ?: return PerkActionResult.NotFound
        if (!perk.unlocked) {
            val paid = preferences.spendCrowns(perk.costCrowns)
            if (!paid) {
                return PerkActionResult.InsufficientCrowns(perk.costCrowns)
            }
            dao.unlockPerk(perk.id)
        }
        dao.equipPerkInCategory(perk.id, perk.category)
        if (perk.category == CATEGORY_TITLE) {
            preferences.updateEquippedTitle(perk.name)
        } else if (perk.category == CATEGORY_AURA) {
            preferences.updateEquippedAura(perk.id)
        }
        return if (perk.unlocked) PerkActionResult.Equipped(perk.name) else PerkActionResult.UnlockedAndEquipped(perk.name)
    }

    suspend fun toggleTournamentRegistration(tournamentId: String): Boolean? {
        val entry = dao.getTournamentById(tournamentId) ?: return null
        val newJoined = !entry.joined
        val newCount = (entry.entrantsCount + if (newJoined) 1 else -1).coerceAtLeast(0)
        dao.updateTournamentJoined(tournamentId, newJoined, newCount)
        return newJoined
    }

    suspend fun updateGamerProfile(gamerTag: String, avatarIndex: Int) {
        preferences.updateGamerProfile(gamerTag, avatarIndex)
    }

    suspend fun clearMatchHistory() {
        dao.clearAllMatches()
    }

    companion object {
        const val CATEGORY_TITLE = "TITLE"
        const val CATEGORY_AURA = "AURA"

        private fun defaultPerks(): List<PerkItemEntity> = listOf(
            PerkItemEntity(
                id = "title_shadow_challenger",
                name = "Shadow Challenger",
                description = "Starter title bestowed upon entering JAHAN_A9XGMR_WORLD.",
                category = CATEGORY_TITLE,
                costCrowns = 0,
                unlocked = true,
                equipped = true,
                sortOrder = 1
            ),
            PerkItemEntity(
                id = "title_amethyst_striker",
                name = "Amethyst Striker",
                description = "Forged for high-tempo reflex contenders in the Royal Grid.",
                category = CATEGORY_TITLE,
                costCrowns = 300,
                unlocked = false,
                equipped = false,
                sortOrder = 2
            ),
            PerkItemEntity(
                id = "title_gilded_tactician",
                name = "Gilded Tactician",
                description = "Elite honor for precision vault breakers & matrix decoders.",
                category = CATEGORY_TITLE,
                costCrowns = 650,
                unlocked = false,
                equipped = false,
                sortOrder = 3
            ),
            PerkItemEntity(
                id = "title_jahan_sovereign",
                name = "JAHAN Sovereign",
                description = "The pinnacle crest of the A9X Royal Arena.",
                category = CATEGORY_TITLE,
                costCrowns = 1200,
                unlocked = false,
                equipped = false,
                sortOrder = 4
            ),
            PerkItemEntity(
                id = CardAuraStyle.OBSIDIAN_VELVET.id,
                name = CardAuraStyle.OBSIDIAN_VELVET.label,
                description = "Deep obsidian plum sheen with subtle amethyst rim.",
                category = CATEGORY_AURA,
                costCrowns = 0,
                unlocked = true,
                equipped = true,
                sortOrder = 5
            ),
            PerkItemEntity(
                id = CardAuraStyle.AMETHYST_PULSE.id,
                name = CardAuraStyle.AMETHYST_PULSE.label,
                description = "Radiant royal violet glow pulsing across your VIP Pass.",
                category = CATEGORY_AURA,
                costCrowns = 350,
                unlocked = false,
                equipped = false,
                sortOrder = 6
            ),
            PerkItemEntity(
                id = CardAuraStyle.SOLAR_24K.id,
                name = CardAuraStyle.SOLAR_24K.label,
                description = "Burnished 24K liquid gold trim with warm champagne shimmer.",
                category = CATEGORY_AURA,
                costCrowns = 750,
                unlocked = false,
                equipped = false,
                sortOrder = 7
            ),
            PerkItemEntity(
                id = CardAuraStyle.SOVEREIGN_PRISM.id,
                name = CardAuraStyle.SOVEREIGN_PRISM.label,
                description = "Imperial purple-and-gold dual prism reserved for legends.",
                category = CATEGORY_AURA,
                costCrowns = 1400,
                unlocked = false,
                equipped = false,
                sortOrder = 8
            )
        )

        private fun defaultTournaments(): List<TournamentEntryEntity> = listOf(
            TournamentEntryEntity(
                id = "tourn_sovereign_cup",
                title = "A9X Sovereign Reflex Cup",
                gameId = VipGameId.CROWN_REFLEX.id,
                prizePoolLabel = "5,000 Crowns Pool",
                scheduleLabel = "Live Season • Ends Sunday",
                minTierLabel = "Open to All VIPs",
                joined = false,
                entrantsCount = 128,
                sortOrder = 1
            ),
            TournamentEntryEntity(
                id = "tourn_vault_gp",
                title = "Imperial Vault Heist Grand Prix",
                gameId = VipGameId.VAULT_BREAKER.id,
                prizePoolLabel = "8,500 Crowns Pool",
                scheduleLabel = "Featured • Daily Qualifiers",
                minTierLabel = "Amethyst Elite+",
                joined = false,
                entrantsCount = 84,
                sortOrder = 2
            ),
            TournamentEntryEntity(
                id = "tourn_runic_masters",
                title = "Royal Runic Masters Invitational",
                gameId = VipGameId.RUNIC_MATRIX.id,
                prizePoolLabel = "12,000 Crowns Pool",
                scheduleLabel = "Weekend Finals • High Prestige",
                minTierLabel = "Imperial Gold+",
                joined = false,
                entrantsCount = 46,
                sortOrder = 3
            )
        )
    }
}

sealed interface PerkActionResult {
    data class UnlockedAndEquipped(val perkName: String) : PerkActionResult
    data class Equipped(val perkName: String) : PerkActionResult
    data class InsufficientCrowns(val required: Int) : PerkActionResult
    data object NotFound : PerkActionResult
}
