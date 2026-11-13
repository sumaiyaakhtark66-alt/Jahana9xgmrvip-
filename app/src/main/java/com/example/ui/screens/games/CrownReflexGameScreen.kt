package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.VipGameId
import com.example.ui.components.GoldPrimaryButton
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class ReflexTileKind {
    EMPTY,
    GOLD_CROWN,
    AMETHYST_TIME,
    OBSIDIAN_TRAP
}

private data class ReflexCellState(
    val kind: ReflexTileKind = ReflexTileKind.EMPTY,
    val expiresAtMs: Long = 0L
)

private enum class ReflexPhase {
    READY,
    PLAYING,
    FINISHED
}

@Composable
fun CrownReflexGameScreen(
    previousBest: Int,
    onMatchFinished: (score: Int, crowns: Int, xp: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var phase by remember { mutableStateOf(ReflexPhase.READY) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(1) }
    var timeLeftMs by remember { mutableLongStateOf(30_000L) }
    var feedbackLabel by remember { mutableStateOf("Tap 24K Crowns & Amethyst Shards!") }
    var recordedResult by remember { mutableStateOf(false) }

    val gridCells = remember {
        mutableStateListOf<ReflexCellState>().apply {
            repeat(16) { add(ReflexCellState()) }
        }
    }

    fun startRound() {
        score = 0
        combo = 1
        timeLeftMs = 30_000L
        feedbackLabel = "Strike fast for 5x combo!"
        recordedResult = false
        for (i in 0 until 16) {
            gridCells[i] = ReflexCellState()
        }
        phase = ReflexPhase.PLAYING
    }

    LaunchedEffect(phase) {
        if (phase != ReflexPhase.PLAYING) return@LaunchedEffect
        var elapsedSinceSpawn = 0L
        while (timeLeftMs > 0L && phase == ReflexPhase.PLAYING) {
            val now = System.currentTimeMillis()
            // Clear expired cells
            for (i in 0 until 16) {
                val cell = gridCells[i]
                if (cell.kind != ReflexTileKind.EMPTY && now >= cell.expiresAtMs) {
                    gridCells[i] = ReflexCellState()
                }
            }
            // Keep 2-3 active targets on board
            val activeCount = gridCells.count { it.kind != ReflexTileKind.EMPTY }
            if (activeCount < 3 || elapsedSinceSpawn >= 700L) {
                elapsedSinceSpawn = 0L
                val emptyIndices = gridCells.indices.filter { gridCells[it].kind == ReflexTileKind.EMPTY }
                if (emptyIndices.isNotEmpty()) {
                    val idx = emptyIndices.random()
                    val roll = Random.nextInt(100)
                    val kind = when {
                        roll < 56 -> ReflexTileKind.GOLD_CROWN
                        roll < 78 -> ReflexTileKind.AMETHYST_TIME
                        else -> ReflexTileKind.OBSIDIAN_TRAP
                    }
                    val lifetime = if (kind == ReflexTileKind.OBSIDIAN_TRAP) 1550L else 1250L
                    gridCells[idx] = ReflexCellState(kind = kind, expiresAtMs = now + lifetime)
                }
            }
            delay(100L)
            elapsedSinceSpawn += 100L
            timeLeftMs = (timeLeftMs - 100L).coerceAtLeast(0L)
        }
        if (phase == ReflexPhase.PLAYING) {
            phase = ReflexPhase.FINISHED
        }
    }

    val crownsEarned = remember(score, phase) {
        if (phase == ReflexPhase.FINISHED) (score / 18).coerceIn(25, 220) else 0
    }
    val xpEarned = remember(score, phase) {
        if (phase == ReflexPhase.FINISHED) (score / 20).coerceIn(20, 180) else 0
    }

    LaunchedEffect(phase) {
        if (phase == ReflexPhase.FINISHED && !recordedResult) {
            recordedResult = true
            onMatchFinished(score, crownsEarned, xpEarned)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameTopBar(
                title = VipGameId.CROWN_REFLEX.title,
                subtitle = "30s High-Tempo Reflex Trial",
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GameHudStatCard(
                    label = "SCORE",
                    value = "%,d".format(score),
                    highlightGold = true,
                    modifier = Modifier.weight(1f)
                )
                GameHudStatCard(
                    label = "COMBO",
                    value = "${combo}x",
                    modifier = Modifier.weight(1f)
                )
                GameHudStatCard(
                    label = "TIME",
                    value = "%.1fs".format(timeLeftMs / 1000f),
                    highlightGold = timeLeftMs <= 5000L,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (timeLeftMs / 30_000f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = feedbackLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (phase) {
                ReflexPhase.READY -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "HOW TO DOMINATE",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "• Gold Crown: +100 × Combo\n• Amethyst Shard: +60 × Combo & +1.0s Time\n• Obsidian Trap: -120 pts & resets Combo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            GoldPrimaryButton(
                                text = "START 30S TRIAL",
                                icon = Icons.Filled.PlayArrow,
                                onClick = { startRound() },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "reflex_start_button"
                            )
                        }
                    }
                }
                ReflexPhase.PLAYING -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (row in 0 until 4) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (col in 0 until 4) {
                                    val idx = row * 4 + col
                                    val cell = gridCells[idx]
                                    ReflexGridTile(
                                        kind = cell.kind,
                                        onTap = {
                                            when (cell.kind) {
                                                ReflexTileKind.GOLD_CROWN -> {
                                                    val pts = 100 * combo
                                                    score += pts
                                                    combo = (combo + 1).coerceAtMost(5)
                                                    feedbackLabel = "+$pts CROWN STRIKE! (${combo}x)"
                                                    gridCells[idx] = ReflexCellState()
                                                }
                                                ReflexTileKind.AMETHYST_TIME -> {
                                                    val pts = 60 * combo
                                                    score += pts
                                                    timeLeftMs = (timeLeftMs + 1000L).coerceAtMost(35_000L)
                                                    feedbackLabel = "+$pts & +1.0s TIME SHARD!"
                                                    gridCells[idx] = ReflexCellState()
                                                }
                                                ReflexTileKind.OBSIDIAN_TRAP -> {
                                                    score = (score - 120).coerceAtLeast(0)
                                                    combo = 1
                                                    feedbackLabel = "OBSIDIAN TRAP! -120 & Combo Reset"
                                                    gridCells[idx] = ReflexCellState()
                                                }
                                                ReflexTileKind.EMPTY -> {
                                                    combo = 1
                                                    feedbackLabel = "Missed tile • Combo 1x"
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .testTag("reflex_tile_$idx")
                                    )
                                }
                            }
                        }
                    }
                }
                ReflexPhase.FINISHED -> {
                    GameResultCard(
                        gameTitle = VipGameId.CROWN_REFLEX.title,
                        score = score,
                        previousBest = previousBest,
                        crownsEarned = crownsEarned,
                        xpEarned = xpEarned,
                        onPlayAgain = { startRound() },
                        onExitToArena = onBack
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReflexGridTile(
    kind: ReflexTileKind,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = when (kind) {
            ReflexTileKind.EMPTY -> MaterialTheme.colorScheme.surfaceContainer
            ReflexTileKind.GOLD_CROWN -> MaterialTheme.colorScheme.primaryContainer
            ReflexTileKind.AMETHYST_TIME -> MaterialTheme.colorScheme.secondaryContainer
            ReflexTileKind.OBSIDIAN_TRAP -> MaterialTheme.colorScheme.errorContainer
        },
        label = "tile_bg"
    )
    val borderColor = when (kind) {
        ReflexTileKind.EMPTY -> MaterialTheme.colorScheme.outlineVariant
        ReflexTileKind.GOLD_CROWN -> MaterialTheme.colorScheme.primary
        ReflexTileKind.AMETHYST_TIME -> MaterialTheme.colorScheme.secondary
        ReflexTileKind.OBSIDIAN_TRAP -> MaterialTheme.colorScheme.error
    }
    val scale by animateFloatAsState(
        targetValue = if (kind == ReflexTileKind.EMPTY) 0.96f else 1.02f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "tile_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (kind) {
                ReflexTileKind.GOLD_CROWN -> Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Gold Crown Target",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
                ReflexTileKind.AMETHYST_TIME -> Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "Amethyst Time Shard",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
                ReflexTileKind.OBSIDIAN_TRAP -> Icon(
                    imageVector = Icons.Filled.Dangerous,
                    contentDescription = "Obsidian Trap",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                ReflexTileKind.EMPTY -> Unit
            }
        }
    }
}
