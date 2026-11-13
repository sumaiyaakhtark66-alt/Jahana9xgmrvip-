package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.VipGameId
import com.example.ui.components.AmethystSecondaryButton
import com.example.ui.components.GoldPrimaryButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private enum class MatrixPhase {
    READY,
    SHOWING_SEQUENCE,
    PLAYER_INPUT,
    FINISHED
}

@Composable
fun RunicMatrixGameScreen(
    previousBest: Int,
    onMatchFinished: (score: Int, crowns: Int, xp: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val scope = rememberCoroutineScope()
    var phase by remember { mutableStateOf(MatrixPhase.READY) }
    var round by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var inputIndex by remember { mutableIntStateOf(0) }
    var litTile by remember { mutableIntStateOf(-1) }
    var errorTile by remember { mutableIntStateOf(-1) }
    var statusText by remember { mutableStateOf("Decode the glowing golden rune order!") }
    var recordedResult by remember { mutableStateOf(false) }

    val runeIcons = remember {
        listOf(
            Icons.Filled.EmojiEvents,
            Icons.Filled.AutoAwesome,
            Icons.Filled.Diamond,
            Icons.Filled.Bolt,
            Icons.Filled.Stars,
            Icons.Filled.Shield,
            Icons.Filled.Explore,
            Icons.Filled.Flare,
            Icons.Filled.Verified
        )
    }

    fun startRoundSequence(nextRound: Int, currentSequence: List<Int>) {
        round = nextRound
        val nextSeq = if (currentSequence.isEmpty()) {
            List(3) { Random.nextInt(9) }
        } else {
            currentSequence + Random.nextInt(9)
        }
        sequence = nextSeq
        inputIndex = 0
        errorTile = -1
        phase = MatrixPhase.SHOWING_SEQUENCE
    }

    fun startNewMatch() {
        score = 0
        recordedResult = false
        startRoundSequence(1, emptyList())
    }

    LaunchedEffect(phase, sequence) {
        if (phase != MatrixPhase.SHOWING_SEQUENCE) return@LaunchedEffect
        statusText = "MEMORIZE ROUND $round (${sequence.size} Runes)..."
        delay(450L)
        for (tile in sequence) {
            litTile = tile
            delay(460L)
            litTile = -1
            delay(170L)
        }
        if (phase == MatrixPhase.SHOWING_SEQUENCE) {
            statusText = "YOUR TURN: Tap ${sequence.size} Runes in order"
            phase = MatrixPhase.PLAYER_INPUT
        }
    }

    fun onTileTap(tileIndex: Int) {
        if (phase != MatrixPhase.PLAYER_INPUT) return
        val expected = sequence.getOrNull(inputIndex) ?: return
        if (tileIndex == expected) {
            val nextIdx = inputIndex + 1
            scope.launch {
                litTile = tileIndex
                delay(180L)
                if (litTile == tileIndex) litTile = -1
            }
            if (nextIdx >= sequence.size) {
                val earned = round * 180
                score += earned
                statusText = "ROUND $round CLEARED! +$earned pts"
                scope.launch {
                    delay(550L)
                    startRoundSequence(round + 1, sequence)
                }
            } else {
                inputIndex = nextIdx
                statusText = "Rune $nextIdx / ${sequence.size} verified"
            }
        } else {
            errorTile = tileIndex
            statusText = "Runic mismatch! Matrix locked."
            phase = MatrixPhase.FINISHED
        }
    }

    val crownsEarned = remember(score, phase) {
        if (phase == MatrixPhase.FINISHED) (score / 14).coerceIn(20, 300) else 0
    }
    val xpEarned = remember(score, phase) {
        if (phase == MatrixPhase.FINISHED) (score / 16).coerceIn(20, 240) else 0
    }

    LaunchedEffect(phase) {
        if (phase == MatrixPhase.FINISHED && !recordedResult) {
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
                title = VipGameId.RUNIC_MATRIX.title,
                subtitle = "Amethyst Constellation Memory",
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
                    label = "ROUND",
                    value = "$round",
                    modifier = Modifier.weight(1f)
                )
                GameHudStatCard(
                    label = "SEQUENCE",
                    value = "${sequence.size}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (phase) {
                MatrixPhase.READY -> {
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
                                text = "DECODE THE ROYAL MATRIX",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Watch the 3x3 royal glyphs flash in gold, then repeat the exact sequence. Each round adds 1 more rune and higher Crown payouts!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            GoldPrimaryButton(
                                text = "START DECODING",
                                icon = Icons.Filled.PlayArrow,
                                onClick = { startNewMatch() },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "matrix_start_button"
                            )
                        }
                    }
                }
                MatrixPhase.SHOWING_SEQUENCE, MatrixPhase.PLAYER_INPUT -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (r in 0 until 3) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (c in 0 until 3) {
                                    val idx = r * 3 + c
                                    val isLit = litTile == idx
                                    val isError = errorTile == idx
                                    val bg by animateColorAsState(
                                        targetValue = when {
                                            isError -> MaterialTheme.colorScheme.errorContainer
                                            isLit -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceContainer
                                        },
                                        label = "matrix_tile_bg"
                                    )
                                    val border = when {
                                        isError -> MaterialTheme.colorScheme.error
                                        isLit -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outline
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(18.dp))
                                            .clickable(enabled = phase == MatrixPhase.PLAYER_INPUT) {
                                                onTileTap(idx)
                                            }
                                            .testTag("matrix_tile_$idx"),
                                        shape = RoundedCornerShape(18.dp),
                                        color = bg,
                                        border = BorderStroke(if (isLit) 2.5.dp else 1.2.dp, border)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = runeIcons[idx],
                                                contentDescription = "Rune ${idx + 1}",
                                                tint = if (isLit) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.secondary
                                                },
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (score > 0 && phase == MatrixPhase.PLAYER_INPUT) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AmethystSecondaryButton(
                            text = "BANK SCORE & CASH OUT",
                            onClick = { phase = MatrixPhase.FINISHED },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "matrix_cash_out_button"
                        )
                    }
                }
                MatrixPhase.FINISHED -> {
                    GameResultCard(
                        gameTitle = VipGameId.RUNIC_MATRIX.title,
                        score = score,
                        previousBest = previousBest,
                        crownsEarned = crownsEarned,
                        xpEarned = xpEarned,
                        onPlayAgain = { startNewMatch() },
                        onExitToArena = onBack
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
