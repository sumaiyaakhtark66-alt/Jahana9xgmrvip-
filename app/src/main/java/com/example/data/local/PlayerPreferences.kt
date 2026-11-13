package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.CardAuraStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.vipDataStore: DataStore<Preferences> by preferencesDataStore(name = "jahan_vip_prefs")

class PlayerPreferences(private val context: Context) {

    private object Keys {
        val GAMER_TAG = stringPreferencesKey("gamer_tag")
        val MEMBER_ID = stringPreferencesKey("member_id")
        val CROWN_POINTS = intPreferencesKey("crown_points")
        val VIP_XP = intPreferencesKey("vip_xp")
        val DAILY_STREAK = intPreferencesKey("daily_streak")
        val LAST_DAILY_CLAIM_DAY = longPreferencesKey("last_daily_claim_day")
        val EQUIPPED_TITLE = stringPreferencesKey("equipped_title")
        val EQUIPPED_AURA_ID = stringPreferencesKey("equipped_aura_id")
        val AVATAR_INDEX = intPreferencesKey("avatar_index")
    }

    val profileFlow: Flow<PlayerProfile> = context.vipDataStore.data.map { prefs ->
        PlayerProfile(
            gamerTag = prefs[Keys.GAMER_TAG] ?: "JAHAN_A9X",
            memberId = prefs[Keys.MEMBER_ID] ?: "A9X-7749-VIP",
            crownPoints = prefs[Keys.CROWN_POINTS] ?: 400,
            vipXp = prefs[Keys.VIP_XP] ?: 180,
            dailyStreak = prefs[Keys.DAILY_STREAK] ?: 1,
            lastDailyClaimEpochDay = prefs[Keys.LAST_DAILY_CLAIM_DAY] ?: -1L,
            equippedTitle = prefs[Keys.EQUIPPED_TITLE] ?: "Shadow Challenger",
            equippedAuraId = prefs[Keys.EQUIPPED_AURA_ID] ?: CardAuraStyle.OBSIDIAN_VELVET.id,
            avatarIndex = prefs[Keys.AVATAR_INDEX] ?: 0
        )
    }

    suspend fun addRewards(crownsDelta: Int, xpDelta: Int) {
        context.vipDataStore.edit { prefs ->
            val currentCrowns = prefs[Keys.CROWN_POINTS] ?: 400
            val currentXp = prefs[Keys.VIP_XP] ?: 180
            prefs[Keys.CROWN_POINTS] = (currentCrowns + crownsDelta).coerceAtLeast(0)
            prefs[Keys.VIP_XP] = (currentXp + xpDelta).coerceAtLeast(0)
        }
    }

    suspend fun spendCrowns(cost: Int): Boolean {
        var success = false
        context.vipDataStore.edit { prefs ->
            val currentCrowns = prefs[Keys.CROWN_POINTS] ?: 400
            if (currentCrowns >= cost) {
                prefs[Keys.CROWN_POINTS] = currentCrowns - cost
                success = true
            }
        }
        return success
    }

    suspend fun claimDailyVault(todayEpochDay: Long, crownsBonus: Int, xpBonus: Int): Boolean {
        var claimed = false
        context.vipDataStore.edit { prefs ->
            val lastDay = prefs[Keys.LAST_DAILY_CLAIM_DAY] ?: -1L
            if (lastDay < todayEpochDay) {
                val currentStreak = prefs[Keys.DAILY_STREAK] ?: 0
                val nextStreak = if (lastDay == todayEpochDay - 1L) currentStreak + 1 else 1
                val currentCrowns = prefs[Keys.CROWN_POINTS] ?: 400
                val currentXp = prefs[Keys.VIP_XP] ?: 180
                prefs[Keys.LAST_DAILY_CLAIM_DAY] = todayEpochDay
                prefs[Keys.DAILY_STREAK] = nextStreak
                prefs[Keys.CROWN_POINTS] = currentCrowns + crownsBonus
                prefs[Keys.VIP_XP] = currentXp + xpBonus
                claimed = true
            }
        }
        return claimed
    }

    suspend fun updateEquippedTitle(title: String) {
        context.vipDataStore.edit { prefs ->
            prefs[Keys.EQUIPPED_TITLE] = title
        }
    }

    suspend fun updateEquippedAura(auraId: String) {
        context.vipDataStore.edit { prefs ->
            prefs[Keys.EQUIPPED_AURA_ID] = auraId
        }
    }

    suspend fun updateGamerProfile(gamerTag: String, avatarIndex: Int) {
        context.vipDataStore.edit { prefs ->
            prefs[Keys.GAMER_TAG] = gamerTag.trim().ifBlank { "JAHAN_A9X" }
            prefs[Keys.AVATAR_INDEX] = avatarIndex
        }
    }
}
