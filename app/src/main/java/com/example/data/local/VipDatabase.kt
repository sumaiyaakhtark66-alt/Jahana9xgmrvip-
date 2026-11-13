package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MatchRecordEntity::class,
        PerkItemEntity::class,
        TournamentEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VipDatabase : RoomDatabase() {
    abstract fun vipDao(): VipDao

    companion object {
        @Volatile
        private var INSTANCE: VipDatabase? = null

        fun getInstance(context: Context): VipDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VipDatabase::class.java,
                    "jahan_a9xgmr_world.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
