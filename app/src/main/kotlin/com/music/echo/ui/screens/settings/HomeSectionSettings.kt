package echo.music.iad1tya.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import echo.music.iad1tya.LocalPlayerAwareWindowInsets
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.HiddenHomeSectionsKey
import echo.music.iad1tya.constants.HomeSectionOrderKey
import echo.music.iad1tya.ui.component.DraggableHomeSectionItem
import echo.music.iad1tya.ui.component.DraggableHomeSectionList
import echo.music.iad1tya.ui.component.IconButton
import echo.music.iad1tya.ui.utils.backToMain
import echo.music.iad1tya.utils.rememberPreference

/** The 8 fixed, individually customizable Home sections. Dynamic sections
 *  (server-provided YT Music feed content, whose count/identity varies per
 *  session) are not included here and keep their existing relative-position
 *  behavior in HomeScreen.kt. */
private data class CustomizableSection(
    val id: String,
    val titleRes: Int,
    val iconRes: Int,
)

private val CUSTOMIZABLE_SECTIONS = listOf(
    CustomizableSection("speed_dial", R.string.speed_dial, R.drawable.speed),
    CustomizableSection("quick_picks", R.string.quick_picks, R.drawable.shuffle),
    CustomizableSection("daily_discover", R.string.your_daily_discover, R.drawable.explore_outlined),
    CustomizableSection("keep_listening", R.string.keep_listening, R.drawable.history),
    CustomizableSection("account_playlists", R.string.your_youtube_playlists, R.drawable.library_music_outlined),
    CustomizableSection("forgotten_favorites", R.string.forgotten_favorites, R.drawable.replay),
    CustomizableSection("from_the_community", R.string.from_the_community, R.drawable.group_outlined),
    CustomizableSection("mood_and_genres", R.string.mood_and_genres, R.drawable.star),
)

@Composable
fun HomeSectionSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val scrollState = rememberScrollState()

    val (homeSectionOrder, onHomeSectionOrderChange) = rememberPreference(HomeSectionOrderKey, "")
    val (hiddenHomeSections, onHiddenHomeSectionsChange) = rememberPreference(HiddenHomeSectionsKey, "")

    val savedOrder = remember(homeSectionOrder) {
        homeSectionOrder.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val hiddenIds = remember(hiddenHomeSections) {
        hiddenHomeSections.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }
    val normalizedOrder = remember(savedOrder) {
        val known = CUSTOMIZABLE_SECTIONS.map { it.id }
        savedOrder.filter { it in known } + known.filter { it !in savedOrder }
    }

    val sectionNames = CUSTOMIZABLE_SECTIONS.associate { it.id to stringResource(it.titleRes) }
    val sectionIcons = CUSTOMIZABLE_SECTIONS.associate { it.id to painterResource(it.iconRes) }

    val draggableItems = remember { mutableStateListOf<DraggableHomeSectionItem>() }

    LaunchedEffect(normalizedOrder, hiddenIds, sectionNames, sectionIcons) {
        draggableItems.clear()
        draggableItems.addAll(
            normalizedOrder.map { id ->
                DraggableHomeSectionItem(
                    id = id,
                    name = sectionNames.getValue(id),
                    icon = sectionIcons.getValue(id),
                    visible = id !in hiddenIds,
                )
            }
        )
    }

    fun persistOrder(order: List<String>) {
        onHomeSectionOrderChange(order.joinToString(","))
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.customize_home_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .height((CUSTOMIZABLE_SECTIONS.size * 76).dp),
        ) {
            DraggableHomeSectionList(
                items = draggableItems,
                onItemsReordered = { reordered ->
                    persistOrder(reordered.map { it.id })
                },
                onVisibilityChanged = { id, visible ->
                    val newHidden = if (visible) hiddenIds - id else hiddenIds + id
                    onHiddenHomeSectionsChange(newHidden.joinToString(","))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                onHomeSectionOrderChange("")
                onHiddenHomeSectionsChange("")
            }
        ) {
            Text(stringResource(R.string.reset_home_customization))
        }

        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.customize_home)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.nav_back)
                )
            }
        }
    )
}
