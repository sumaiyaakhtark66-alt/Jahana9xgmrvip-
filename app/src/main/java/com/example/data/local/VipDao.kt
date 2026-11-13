package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VipDao {
    @Query("SELECT * FROM match_records ORDER BY timestampMillis DESC LIMIT 30")
    fun observeRecentMatches(): Flow<List<MatchRecordEntity>>

    @Query("SELECT gameId, MAX(score) as bestScore FROM match_records GROUP BY gameId")
    fun observeBestScores(): Flow<List<GameBestScore>>

    @Query("SELECT COUNT(*) FROM match_records")
    fun observeTotalMatches(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchRecord(record: MatchRecordEntity)

    @Query("DELETE FROM match_records")
    suspend fun clearAllMatches()

    @Query("SELECT * FROM vip_perks ORDER BY sortOrder ASC")
    fun observePerks(): Flow<List<PerkItemEntity>>

    @Query("SELECT COUNT(*) FROM vip_perks")
    suspend fun countPerks(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPerks(perks: List<PerkItemEntity>)

    @Query("SELECT * FROM vip_perks WHERE id = :perkId LIMIT 1")
    suspend fun getPerkById(perkId: String): PerkItemEntity?

    @Query("UPDATE vip_perks SET unlocked = 1 WHERE id = :perkId")
    suspend fun unlockPerk(perkId: String)

    @Query("UPDATE vip_perks SET equipped = CASE WHEN id = :perkId THEN 1 ELSE 0 END WHERE category = :category")
    suspend fun equipPerkInCategory(perkId: String, category: String)

    @Query("SELECT * FROM vip_tournaments ORDER BY sortOrder ASC")
    fun observeTournaments(): Flow<List<TournamentEntryEntity>>

    @Query("SELECT COUNT(*) FROM vip_tournaments")
    suspend fun countTournaments(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTournaments(tournaments: List<TournamentEntryEntity>)

    @Query("SELECT * FROM vip_tournaments WHERE id = :tournamentId LIMIT 1")
    suspend fun getTournamentById(tournamentId: String): TournamentEntryEntity?

    @Query("UPDATE vip_tournaments SET joined = :joined, entrantsCount = :entrantsCount WHERE id = :tournamentId")
    suspend fun updateTournamentJoined(tournamentId: String, joined: Boolean, entrantsCount: Int)
}

data class GameBestScore(
    val gameId: String,
    val bestScore: Int
)
