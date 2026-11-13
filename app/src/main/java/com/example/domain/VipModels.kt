package com.example.domain

enum class VipTier(
    val displayName: String,
    val minXp: Int,
    val bonusMultiplierLabel: String,
    val perkSummary: String
) {
    OBSIDIAN_INITIATE(
        displayName = "Obsidian Initiate",
        minXp = 0,
        bonusMultiplierLabel = "1.0x XP",
        perkSummary = "Access to all A9X Arena challenges & Daily Royal Vault"
    ),
    AMETHYST_ELITE(
        displayName = "Amethyst Elite",
        minXp = 500,
        bonusMultiplierLabel = "1.25x XP",
        perkSummary = "Priority tournament seeding & Amethyst Card Aura"
    ),
    IMPERIAL_GOLD(
        displayName = "Imperial Gold",
        minXp = 1500,
        bonusMultiplierLabel = "1.5x XP",
        perkSummary = "24K Gilded Pass border & +25% Vault bonus boost"
    ),
    A9X_SOVEREIGN(
        displayName = "A9X Sovereign",
        minXp = 3500,
        bonusMultiplierLabel = "2.0x XP",
        perkSummary = "Maximum Sovereign prestige, Crown Emblem & Hall of Fame glow"
    );

    fun nextTier(): VipTier? = when (this) {
        OBSIDIAN_INITIATE -> AMETHYST_ELITE
        AMETHYST_ELITE -> IMPERIAL_GOLD
        IMPERIAL_GOLD -> A9X_SOVEREIGN
        A9X_SOVEREIGN -> null
    }

    companion object {
        fun fromXp(xp: Int): VipTier {
            return entries.lastOrNull { xp >= it.minXp } ?: OBSIDIAN_INITIATE
        }
    }
}

enum class VipGameId(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val rewardPreview: String,
    val difficulty: String
) {
    CROWN_REFLEX(
        id = "crown_reflex",
        title = "A9X Crown Reflex",
        subtitle = "30s high-tempo grid strike. Tap 24K crowns & amethyst time shards while dodging obsidian traps.",
        badge = "SPEED • COMBO",
        rewardPreview = "Up to +220 Crowns",
        difficulty = "High Tempo"
    ),
    VAULT_BREAKER(
        id = "vault_breaker",
        title = "Imperial Vault Breaker",
        subtitle = "Precision orbital lockpick. Strike the gilded target zone as the royal dial accelerates.",
        badge = "TIMING • SKILL",
        rewardPreview = "Up to +280 Crowns",
        difficulty = "Precision"
    ),
    RUNIC_MATRIX(
        id = "runic_matrix",
        title = "Royal Runic Matrix",
        subtitle = "Decode escalating amethyst & gold starlight sequences across the 3x3 sovereign grid.",
        badge = "MEMORY • FOCUS",
        rewardPreview = "Up to +300 Crowns",
        difficulty = "Tactical"
    );

    companion object {
        fun fromId(id: String): VipGameId =
            entries.firstOrNull { it.id == id } ?: CROWN_REFLEX
    }
}

enum class CardAuraStyle(val id: String, val label: String) {
    OBSIDIAN_VELVET("aura_obsidian", "Obsidian Velvet"),
    AMETHYST_PULSE("aura_amethyst", "Amethyst Pulse"),
    SOLAR_24K("aura_solar_gold", "24K Solar Gold"),
    SOVEREIGN_PRISM("aura_sovereign", "A9X Sovereign Prism");

    companion object {
        fun fromId(id: String): CardAuraStyle =
            entries.firstOrNull { it.id == id } ?: OBSIDIAN_VELVET
    }
}

data class HallOfFameContender(
    val rank: Int,
    val gamerTag: String,
    val title: String,
    val vipXp: Int,
    val isCurrentUser: Boolean = false
)

val BaseContenders = listOf(
    HallOfFameContender(0, "VEX_SOVEREIGN", "Grand Regent", 4120),
    HallOfFameContender(0, "NYX_AMETHYST", "Royal Tactician", 2680),
    HallOfFameContender(0, "KAEL_24K", "Vault Breaker", 1840),
    HallOfFameContender(0, "ZORA_A9X", "Arcane Striker", 920),
    HallOfFameContender(0, "ORION_PLUM", "VIP Contender", 340)
)
