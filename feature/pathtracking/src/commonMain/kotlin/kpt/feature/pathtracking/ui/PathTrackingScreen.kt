/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.pathtracking.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.RefreshableScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.objects.users.UserLatLng
import kpt.feature.pathtracking.generated.resources.Res
import kpt.feature.pathtracking.generated.resources.feature_path_tracking_no_path_tracking_body
import kpt.feature.pathtracking.generated.resources.feature_path_tracking_no_path_tracking_found
import kpt.feature.pathtracking.generated.resources.feature_path_tracking_route
import kpt.feature.pathtracking.generated.resources.feature_path_tracking_track_my_path
import kpt.feature.pathtracking.generated.resources.feature_path_tracking_when
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * Ported from `6b66e8a43:feature/path-tracking/…/PathTrackingScreen.kt` (+ its Android actual).
 *
 * The original's per-platform `PathTrackingMapView` was a Google Map on Android and NOTHING on the
 * other four targets, and on Android it needs a Maps API key this fork does not ship — a blank grey
 * tile. Each card now draws its route itself (start dot → polyline → end dot) on every platform, and
 * the tap opens the route in Maps through a plain URL, which is what the original's Android intent did.
 *
 * The original also reverse-geocoded each end through Nominatim from the composable, sending the
 * officer's location to a third party. Not ported: a card shows the stored address, else the
 * coordinates.
 */
@Composable
internal fun PathTrackingScreen(
    onNavigateBack: () -> Unit,
    viewModel: PathTrackingViewModel = koinViewModel(),
) {
    val paths by viewModel.paths.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    PathTrackingScreenContent(
        paths = paths,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::onRefresh,
        onOpenRoute = { path -> path.mapsUrl()?.let { runCatching { uriHandler.openUri(it) } } },
    )
}

@Composable
internal fun PathTrackingScreenContent(
    paths: ScreenState<List<TrackedPath>>,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenRoute: (TrackedPath) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_path_tracking_track_my_path),
        onBackPressed = onNavigateBack,
        modifier = modifier.testTag(PathTrackingTestTags.SCREEN),
    ) { padding ->
        RefreshableScreenContent(
            state = paths,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding),
            empty = { NoPaths() },
        ) { list, _ ->
            if (list.isEmpty()) {
                NoPaths()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag(PathTrackingTestTags.LIST),
                    contentPadding = PaddingValues(KptTheme.spacing.sm),
                ) {
                    items(list) { path -> PathCard(path, onOpenRoute) }
                }
            }
        }
    }
}

/** The original's `PathTrackingItem`: map · "start to end" · "date from start to stop". */
@Composable
private fun PathCard(path: TrackedPath, onOpenRoute: (TrackedPath) -> Unit) {
    val sp = KptTheme.spacing
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(sp.sm).testTag(PathTrackingTestTags.CARD),
        onClick = { onOpenRoute(path) },
        colors = CardDefaults.outlinedCardColors(KptTheme.colorScheme.surface),
    ) {
        RouteSketch(path.points)
        Text(
            modifier = Modifier.padding(vertical = sp.xs, horizontal = sp.sm),
            text = stringResource(
                Res.string.feature_path_tracking_route,
                path.startAddress ?: path.points.firstOrNull()?.label().orEmpty(),
                path.endAddress ?: path.points.lastOrNull()?.label().orEmpty(),
            ),
            style = KptTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            modifier = Modifier.padding(sp.sm),
            text = stringResource(
                Res.string.feature_path_tracking_when,
                path.date.orEmpty(),
                path.startTime.orEmpty(),
                path.stopTime.orEmpty(),
            ),
            style = KptTheme.typography.bodySmall,
        )
    }
}

/**
 * The route, fitted to the card: the points scaled into the box (lat inverted so north is up),
 * a stroke through them, the start in green and the end in amber. One point draws one dot.
 */
@Composable
private fun RouteSketch(points: List<UserLatLng>) {
    val c = KptTheme.colorScheme
    val line = c.primary
    val start = c.secondary
    val end = c.tertiary
    val inset = KptTheme.spacing.md
    Box(
        Modifier.fillMaxWidth().height(MAP_HEIGHT).clip(KptTheme.shapes.small).background(c.surfaceContainer)
            .testTag(PathTrackingTestTags.ROUTE),
    ) {
        if (points.isEmpty()) return@Box
        Canvas(Modifier.fillMaxSize().padding(inset)) {
            val minLat = points.minOf { it.lat }
            val maxLat = points.maxOf { it.lat }
            val minLng = points.minOf { it.lng }
            val maxLng = points.maxOf { it.lng }
            // One scale for both axes keeps the route's shape; a single point sits in the middle.
            val span = max(maxLat - minLat, maxLng - minLng).takeIf { it > 0 } ?: 1.0
            val scale = minOf(size.width, size.height) / span.toFloat()
            val offX = (size.width - ((maxLng - minLng) * scale).toFloat()) / 2
            val offY = (size.height - ((maxLat - minLat) * scale).toFloat()) / 2
            fun at(p: UserLatLng) = Offset(
                offX + ((p.lng - minLng) * scale).toFloat(),
                offY + ((maxLat - p.lat) * scale).toFloat(),
            )
            val dot = inset.toPx() / 3
            if (points.size > 1) {
                val route = Path().apply {
                    moveTo(at(points.first()).x, at(points.first()).y)
                    points.drop(1).forEach { lineTo(at(it).x, at(it).y) }
                }
                drawPath(
                    route,
                    line,
                    style = Stroke(width = dot * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
            drawCircle(start, radius = dot, center = at(points.first()))
            if (points.size > 1) drawCircle(end, radius = dot, center = at(points.last()))
        }
    }
}

@Composable
private fun NoPaths() {
    val sp = KptTheme.spacing
    Column(
        Modifier.fillMaxSize().padding(sp.xl).testTag(PathTrackingTestTags.EMPTY),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(sp.sm, Alignment.CenterVertically),
    ) {
        Box(
            Modifier.size(EMPTY_BOX).background(KptTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                AppIcons.PersonPinCircle,
                contentDescription = null,
                tint = KptTheme.colorScheme.primary,
                modifier = Modifier.size(sp.lg + sp.xs),
            )
        }
        Text(
            stringResource(Res.string.feature_path_tracking_no_path_tracking_found),
            style = KptTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(Res.string.feature_path_tracking_no_path_tracking_body),
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** "19.43260, -99.13320" — five decimals, about a metre. */
private fun UserLatLng.label(): String = "${lat.round5()}, ${lng.round5()}"

private fun Double.round5(): Double = (this * COORD_SCALE).roundToLong() / COORD_SCALE

/** Directions start → end, as the original's Android intent built them; null for an empty path. */
internal fun TrackedPath.mapsUrl(): String? {
    val from = points.firstOrNull() ?: return null
    val to = points.last()
    return "https://www.google.com/maps/dir/?api=1&origin=${from.lat},${from.lng}&destination=${to.lat},${to.lng}"
}

internal object PathTrackingTestTags {
    const val SCREEN = "path_tracking_screen"
    const val LIST = "path_tracking_list"
    const val CARD = "path_tracking_card"
    const val ROUTE = "path_tracking_route"
    const val EMPTY = "path_tracking_empty"
}

private const val COORD_SCALE = 100_000.0

// 150 dp route preview (the original's map height) and a 64 dp empty-state badge, from the spacing scale.
private val MAP_HEIGHT: Dp @Composable get() = KptTheme.spacing.xxl * 2 + KptTheme.spacing.lg - KptTheme.spacing.xs / 2
private val EMPTY_BOX: Dp @Composable get() = KptTheme.spacing.xxl
