package com.mkdirchip.tilt

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mkdirchip.tilt.data.TilRepository
import com.mkdirchip.tilt.ui.capture.CaptureScreen
import com.mkdirchip.tilt.ui.capture.CaptureViewModel
import com.mkdirchip.tilt.ui.stats.StatsScreen
import com.mkdirchip.tilt.ui.stats.StatsViewModel
import com.mkdirchip.tilt.ui.theme.LocalReducedMotion
import com.mkdirchip.tilt.ui.theme.TiltMotion
import com.mkdirchip.tilt.ui.theme.TiltTheme
import com.mkdirchip.tilt.ui.timeline.TimelineScreen
import com.mkdirchip.tilt.ui.timeline.TimelineViewModel
import com.mkdirchip.tilt.ui.timeline.localeWeekStart
import com.mkdirchip.tilt.widget.EXTRA_ENTRY_ID

private object Routes {
    const val TIMELINE = "timeline"
    const val STATS = "stats"
    const val CAPTURE = "capture"
    const val ENTRY_ID = "entryId"

    /** A null id means a new entry. */
    fun capture(entryId: Long? = null): String = "$CAPTURE?$ENTRY_ID=${entryId ?: -1L}"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force dark system bars. The default enableEdgeToEdge() picks its appearance from the
        // system light/dark setting, which would put dark status-bar icons on our navy ground
        // whenever the device is in light mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        val repository = tiltContainer.repository
        // The widget passes the entry it was showing, so tapping it opens that entry.
        val openEntryId = intent?.getLongExtra(EXTRA_ENTRY_ID, -1L)?.takeIf { it > 0L }

        setContent {
            TiltTheme {
                TiltNavGraph(repository, openEntryId)
            }
        }
    }
}

@Composable
private fun TiltNavGraph(repository: TilRepository, openEntryId: Long?) {
    val navController = rememberNavController()
    val reduced = LocalReducedMotion.current
    val weekStart = localeWeekStart()
    val duration = if (reduced) 0 else TiltMotion.STANDARD

    // Honour the widget's request once, on first composition.
    LaunchedEffect(openEntryId) {
        if (openEntryId != null) navController.navigate(Routes.capture(openEntryId))
    }

    NavHost(
        navController = navController,
        startDestination = Routes.TIMELINE,
        // Screen changes are carried by motion, collapsing to a plain fade when motion is reduced.
        enterTransition = {
            if (reduced) fadeIn(tween(0)) else {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(duration),
                ) + fadeIn(tween(duration))
            }
        },
        exitTransition = {
            if (reduced) fadeOut(tween(0)) else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(duration),
                ) + fadeOut(tween(duration))
            }
        },
        popEnterTransition = {
            if (reduced) fadeIn(tween(0)) else {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(duration),
                ) + fadeIn(tween(duration))
            }
        },
        popExitTransition = {
            if (reduced) fadeOut(tween(0)) else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(duration),
                ) + fadeOut(tween(duration))
            }
        },
    ) {
        composable(Routes.TIMELINE) {
            val viewModel: TimelineViewModel = viewModel(
                factory = TimelineViewModel.factory(repository, weekStart),
            )
            TimelineScreen(
                viewModel = viewModel,
                onCapture = { navController.navigate(Routes.capture()) },
                onOpenEntry = { id -> navController.navigate(Routes.capture(id)) },
                onOpenStats = { navController.navigate(Routes.STATS) },
            )
        }

        composable(Routes.STATS) {
            val viewModel: StatsViewModel = viewModel(
                factory = StatsViewModel.factory(repository, weekStart),
            )
            val state by viewModel.state.collectAsState()
            StatsScreen(
                state = state,
                weekStart = weekStart,
                onPreviousMonth = viewModel::showPreviousMonth,
                onNextMonth = viewModel::showNextMonth,
            )
        }

        composable(
            route = "${Routes.CAPTURE}?${Routes.ENTRY_ID}={${Routes.ENTRY_ID}}",
            arguments = listOf(
                navArgument(Routes.ENTRY_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getLong(Routes.ENTRY_ID) ?: -1L
            val entryId = rawId.takeIf { it > 0L }
            val viewModel: CaptureViewModel = viewModel(
                factory = CaptureViewModel.factory(repository, entryId),
            )
            CaptureScreen(
                viewModel = viewModel,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
