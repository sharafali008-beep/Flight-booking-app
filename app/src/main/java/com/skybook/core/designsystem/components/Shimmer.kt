package com.skybook.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skybook.R
import com.skybook.core.designsystem.theme.SkyBookTheme
import com.skybook.core.designsystem.theme.Spacing

/** Paints a moving light gradient over a placeholder shape (skeleton loader). */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX"
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    background(
        Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(x, 0f),
            end = Offset(x + 400f, 200f)
        )
    )
}

@Composable
private fun Bone(modifier: Modifier) {
    Box(modifier.clip(MaterialTheme.shapes.small).shimmer())
}

/** Skeleton version of [FlightCard] shown while flights load. */
@Composable
fun FlightCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(Spacing.lg)) {
            Row {
                Bone(Modifier.size(36.dp))
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Bone(Modifier.width(100.dp).height(14.dp))
                    Spacer(Modifier.height(6.dp))
                    Bone(Modifier.width(60.dp).height(12.dp))
                }
                Bone(Modifier.width(70.dp).height(22.dp))
            }
            Spacer(Modifier.height(Spacing.lg))
            Row {
                Bone(Modifier.width(56.dp).height(36.dp))
                Spacer(Modifier.weight(1f))
                Bone(Modifier.width(80.dp).height(14.dp).padding(top = 10.dp))
                Spacer(Modifier.weight(1f))
                Bone(Modifier.width(56.dp).height(36.dp))
            }
        }
    }
}

/** A column of skeleton cards. */
@Composable
fun FlightListSkeleton(modifier: Modifier = Modifier, count: Int = 4) {
    val loading = stringResource(R.string.loading)
    Column(modifier.semantics { contentDescription = loading }) {
        repeat(count) {
            FlightCardSkeleton()
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Preview
@Composable
private fun SkeletonPreview() {
    SkyBookTheme { FlightListSkeleton(Modifier.padding(Spacing.lg), count = 2) }
}
