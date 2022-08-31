@file:OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)

package fr.simon.marquis.preferencesmanager.ui.preferences

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import kotlinx.coroutines.launch

typealias ScreenFragment = @Composable (
    onClick: (preferenceFile: PreferenceFile?) -> Unit,
    onLongClick: (preferenceFile: PreferenceFile?) -> Unit
) -> Unit

data class TabItem(
    val pkgName: String,
    val preferenceFile: PreferenceFile? = null,
    val screen: ScreenFragment = { onClick, onLongClick ->
        PreferenceFragment(
            list = preferenceFile?.list.orEmpty(),
            onClick = { onClick(preferenceFile) },
            onLongClick = { onLongClick(preferenceFile) }
        )
    }
)

@Composable
fun TabsContent(
    tabs: List<TabItem>,
    pagerState: PagerState,
    onClick: (preferenceFile: PreferenceFile?) -> Unit,
    onLongClick: (preferenceFile: PreferenceFile?) -> Unit
) {
    HorizontalPager(state = pagerState, count = tabs.size) { page ->
        tabs[page].screen(onClick, onLongClick)
    }
}

@Composable
fun Tabs(scrollBehavior: TopAppBarScrollBehavior, tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    val backgroundColors = TopAppBarDefaults.centerAlignedTopAppBarColors()
    val minColor = backgroundColors.containerColor(colorTransitionFraction = 0f).value
    val maxColor = backgroundColors.containerColor(colorTransitionFraction = 1f).value
    val easing = FastOutLinearInEasing.transform(scrollBehavior.state.overlappedFraction)
    val backgroundColor = lerp(minColor, maxColor, easing)

    ScrollableTabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = pagerState.currentPage,
        containerColor = backgroundColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions))
        }
    ) {
        tabs.mapIndexed { index, tabItem ->
            Tab(
                text = { Text(tabItem.pkgName.substringAfterLast("/")) },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}
