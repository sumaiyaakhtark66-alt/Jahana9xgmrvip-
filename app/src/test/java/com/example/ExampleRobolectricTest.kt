package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PlayerPreferences
import com.example.data.local.VipDatabase
import com.example.data.repository.PerkActionResult
import com.example.data.repository.VipRepository
import com.example.domain.VipGameId
import com.example.domain.VipTier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: VipDatabase
    private lateinit var repository: VipRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VipDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = VipRepository(db.vipDao(), PlayerPreferences(context))
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JAHAN_A9XGMR_WORLD", appName)
    }

    @Test
    fun `vip tier resolves accurately from xp`() {
        assertEquals(VipTier.OBSIDIAN_INITIATE, VipTier.fromXp(0))
        assertEquals(VipTier.AMETHYST_ELITE, VipTier.fromXp(500))
        assertEquals(VipTier.IMPERIAL_GOLD, VipTier.fromXp(1500))
        assertEquals(VipTier.A9X_SOVEREIGN, VipTier.fromXp(3500))
    }

    @Test
    fun `seeding and recording match updates crowns and best scores`() = runBlocking {
        repository.ensureSeeded()
        val perks = repository.perksFlow.first()
        val tournaments = repository.tournamentsFlow.first()
        assertTrue(perks.isNotEmpty())
        assertTrue(tournaments.isNotEmpty())

        val initialCrowns = repository.profileFlow.first().crownPoints
        repository.recordMatchResult(
            gameId = VipGameId.CROWN_REFLEX,
            score = 1850,
            crownsEarned = 140,
            xpEarned = 95
        )
        val updatedProfile = repository.profileFlow.first()
        val bestScores = repository.bestScoresFlow.first()
        assertEquals(initialCrowns + 140, updatedProfile.crownPoints)
        assertEquals(1850, bestScores[VipGameId.CROWN_REFLEX.id])

        val perkResult = repository.unlockOrEquipPerk("title_amethyst_striker")
        assertTrue(perkResult is PerkActionResult.UnlockedAndEquipped)
    }
}
