package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.CardAuraStyle
import com.example.domain.VipTier

@Entity(tableName = "match_records")
data class MatchRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val gameTitle: String,
    val score: Int,
    val crownsEarned: Int,
    val xpEarned: Int,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "vip_perks")
data class PerkItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String, // "TITLE" or "AURA"
    val costCrowns: Int,
    val unlocked: Boolean,
    val equipped: Boolean,
    val sortOrder: Int
)

@Entity(tableName = "vip_tournaments")
data class TournamentEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val gameId: String,
    val prizePoolLabel: String,
    val scheduleLabel: String,
    val minTierLabel: String,
    val joined: Boolean,
    val entrantsCount: Int,
    val sortOrder: Int
)

data class PlayerProfile(
    val gamerTag: String = "JAHAN_A9X",
    val memberId: String = "A9X-7749-VIP",
    val crownPoints: Int = 400,
    val vipXp: Int = 180,
    val dailyStreak: Int = 1,
    val lastDailyClaimEpochDay: Long = -1L,
    val equippedTitle: String = "Shadow Challenger",
    val equippedAuraId: String = CardAuraStyle.OBSIDIAN_VELVET.id,
    val avatarIndex: Int = 0
) {
    val tier: VipTier
        get() = VipTier.fromXp(vipXp)

    val auraStyle: CardAuraStyle
        get() = CardAuraStyle.fromId(equippedAuraId)
}
