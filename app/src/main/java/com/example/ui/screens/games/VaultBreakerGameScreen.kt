package com.example.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.VipGameId
import com.example.ui.components.GoldPrimaryButton
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class VaultPhase {
    READY,
    PLAYING,
    FINISHED
}

@Composable
fun VaultBreakerGameScreen(
    previousBest: Int,
    onMatchFinished: (score: Int, crowns: Int, xp: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var phase by remember { mutableStateOf(VaultPhase.READY) }
    var level by remember { mutableIntStateOf(1) }
    var shields by remember { mutableIntStateOf(3) }
    var score by remember { mutableIntStateOf(0) }
    var needleAngle by remember { mutableFloatStateOf(0f) }
    var targetStartAngle by remember { mutableFloatStateOf(35f) }
    var targetSweepAngle by remember { mutableFloatStateOf(52f) }
    var direction by remember { mutableIntStateOf(1) }
    var feedback by remember { mutableStateOf("Tap STRIKE LOCK inside the 24K gold zone!") }
    var recordedResult by remember { mutableStateOf(false) }

    fun spawnNextLock(nextLevel: Int) {
        level = nextLevel
        targetSweepAngle = (54f - (nextLevel - 1) * 3.5f).coerceAtLeast(20f)
        targetStartAngle = Random.nextFloat() * 360f
        direction = if (nextLevel % 2 == 0) -1 else 1
    }

    fun startVaultRun() {
        score = 0
        shields = 3
        needleAngle = 0f
        recordedResult = false
        feedback = "Align the needle with the golden arc!"
        spawnNextLock(1)
        phase = VaultPhase.PLAYING
    }

    LaunchedEffect(phase, level) {
        if (phase != VaultPhase.PLAYING) return@LaunchedEffect
        var lastTimeNanos = 0L
        val degreesPerSecond = (145f + (level - 1) * 22f).coerceAtMost(350f)
        while (phase == VaultPhase.PLAYING) {
            withFrameNanos { frameNanos ->
                if (lastTimeNanos != 0L) {
                    val dt = (frameNanos - lastTimeNanos) / 1_000_000_000f
                    needleAngle = (needleAngle + direction * degreesPerSecond * dt + 360f) % 360f
                }
                lastTimeNanos = frameNanos
            }
        }
    }

    fun attemptStrike() {
        if (phase != VaultPhase.PLAYING) return
        val center = (targetStartAngle + targetSweepAngle / 2f) % 360f
        var diff = abs(needleAngle - center)
        if (diff > 180f) diff = 360f - diff

        if (diff <= targetSweepAngle / 2f + 4f) {
            val pts = 120 * level
            score += pts
            feedback = "LOCK $level CRACKED! +$pts pts"
            spawnNextLock(level + 1)
        } else {
            shields -= 1
            if (shields <= 0) {
                feedback = "Vault seals locked!"
                phase = VaultPhase.FINISHED
            } else {
                feedback = "Missed! $shields Shield(s) remaining"
            }
        }
    }

    val crownsEarned = remember(score, phase) {
        if (phase == VaultPhase.FINISHED) (score / 15).coerceIn(25, 280) else 0
    }
    val xpEarned = remember(score, phase) {
        if (phase == VaultPhase.FINISHED) (score / 17).coerceIn(25, 220) else 0
    }

    LaunchedEffect(phase) {
        if (phase == VaultPhase.FINISHED && !recordedResult) {
            recordedResult = true
            onMatchFinished(score, crownsEarned, xpEarned)
        }
    }

    val goldColor = MaterialTheme.colorScheme.primary
    val amethystColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val coreColor = MaterialTheme.colorScheme.surfaceContainer

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
                title = VipGameId.VAULT_BREAKER.title,
                subtitle = "Precision Orbital Lockpick",
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
                    label = "VAULT LVL",
                    value = "$level",
                    modifier = Modifier.weight(1f)
                )
                GameHudStatCard(
                    label = "SHIELDS",
                    value = "$shields / 3",
                    highlightGold = shields == 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = feedback,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (phase) {
                VaultPhase.READY -> {
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
                                text = "CRACK THE IMPERIAL VAULT",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Watch the orbiting golden needle sweep the amethyst ring. Tap STRIKE LOCK when it passes over the glowing gold arc. Each level speeds up!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            GoldPrimaryButton(
                                text = "BEGIN HEIST",
                                icon = Icons.Filled.PlayArrow,
                                onClick = { startVaultRun() },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "vault_start_button"
                            )
                        }
                    }
                }
                VaultPhase.PLAYING -> {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val dialSize = (maxWidth * 0.78f).coerceAtMost(290.dp)
                        Box(
                            modifier = Modifier.size(dialSize),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = size.minDimension * 0.11f
                                val inset = strokeWidth / 2f + 8f
                                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                                val topLeft = Offset(inset, inset)

                                // Outer ring track
                                drawArc(
                                    color = trackColor,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth)
                                )

                                // Target gold sweet spot arc
                                drawArc(
                                    color = goldColor,
                                    startAngle = targetStartAngle,
                                    sweepAngle = targetSweepAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // Inner core circle
                                drawCircle(
                                    color = coreColor,
                                    radius = size.minDimension * 0.28f
                                )
                                drawCircle(
                                    color = amethystColor.copy(alpha = 0.55f),
                                    radius = size.minDimension * 0.28f,
                                    style = Stroke(width = 4f)
                                )

                                // Orbiting needle
                                val rad = (needleAngle * PI / 180.0).toFloat()
                                val ringRadius = (size.minDimension - inset * 2) / 2f
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val innerPoint = Offset(
                                    center.x + cos(rad) * (ringRadius * 0.58f),
                                    center.y + sin(rad) * (ringRadius * 0.58f)
                                )
                                val outerPoint = Offset(
                                    center.x + cos(rad) * (ringRadius + strokeWidth * 0.45f),
                                    center.y + sin(rad) * (ringRadius + strokeWidth * 0.45f)
                                )
                                drawLine(
                                    color = amethystColor,
                                    start = innerPoint,
                                    end = outerPoint,
                                    strokeWidth = 10f,
                                    cap = StrokeCap.Round
                                )
                                drawCircle(
                                    color = goldColor,
                                    radius = 12f,
                                    center = Offset(
                                        center.x + cos(rad) * ringRadius,
                                        center.y + sin(rad) * ringRadius
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "LVL $level",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "LOCKED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    GoldPrimaryButton(
                        text = "STRIKE LOCK",
                        icon = Icons.Filled.LockOpen,
                        onClick = { attemptStrike() },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "vault_strike_button"
                    )
                }
                VaultPhase.FINISHED -> {
                    GameResultCard(
                        gameTitle = VipGameId.VAULT_BREAKER.title,
                        score = score,
                        previousBest = previousBest,
                        crownsEarned = crownsEarned,
                        xpEarned = xpEarned,
                        onPlayAgain = { startVaultRun() },
                        onExitToArena = onBack
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
